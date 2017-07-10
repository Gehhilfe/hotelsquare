'use strict';
const config = require('config');
const mongoose = require('mongoose');
const Venue = require('../../app/models/venue');
const Image = require('../../app/models/image');
const Comments = require('../../app/models/comments');

const Comment = Comments.Comment;
const TextComment = Comments.TextComment;
const ImageComment = Comments.ImageComment;

const Util = require('../../lib/util');
const chai = require('chai');
const chaiHttp = require('chai-http');
const server = require('../../server');
const User = require('../../app/models/user');
const jsonwt = require('jsonwebtoken');
const expect = chai.expect;
const request = require('supertest');
chai.should();
chai.use(chaiHttp);
chai.use(require('chai-things'));

const imageGenerator = require('js-image-generator');
const tempWrite = require('temp-write');
const fs = require('fs');
const path = require('path');

const mochaAsync = (fn) => {
    return (done) => {
        fn.call().then(done, (err) => {
            return done(err);
        });
    };
};

describe('comment api query', () => {

    let aVenue;
    let u;
    let token;
    let anImage;
    let aComment;
    let bComment;

    beforeEach(mochaAsync(async () => {
        mongoose.Promise = global.Promise;

        await Util.connectDatabase(mongoose);
        await Venue.remove({});
        await User.remove({});
        await Comments.Comment.remove({});

        u = await User.create({name: 'peter', email: 'peter1@cool.de', password: 'peter99'});
        token = jsonwt.sign(u.toJSON(), config.jwt.secret, config.jwt.options);

        aVenue = await Venue.create({
            name: 'aVenue',
            place_id: 'a',
            location: {
                type: 'Point',
                coordinates: [5, 5]
            },
            comments: []
        });

        aComment = await TextComment.build(u, 'test', aVenue);
    }));

    describe('POST text comments to venue', () => {
        it('should add a comment to aVenue', (mochaAsync(async () => {
            const res = await request(server)
                .post('/venues/' + aVenue._id + '/comments/text')
                .set('x-auth', token)
                .send({
                    text: 'this is a comment'
                });
            res.should.have.status(200);
            const v = await Venue.findOne({_id: aVenue._id});
            v.comments.length.should.be.equal(1);
            const c = await Comment.findOne({text: 'this is a comment'});
            c.kind.should.equal('TextComment');
        })));
    });

    describe('POST text comments to comment', () => {
        it('should add a comment to comment', (mochaAsync(async () => {
            const res = await request(server)
                .post('/comments/' + aComment._id + '/comments/text')
                .set('x-auth', token)
                .send({
                    text: 'this is a comment'
                });
            res.should.have.status(200);
            const v = await Comment.findOne({_id: aComment._id});
            v.comments.length.should.be.equal(1);
        })));
    });

    describe('POST image comments to venue', () => {

        let image, imagePath;

        before((done) => {
            imageGenerator.generateImage(1920, 1080, 80, (err, i) => {
                expect(err).to.be.null;
                image;
                image = i;
                imagePath = tempWrite.sync(image.data, 'image.jpeg');
                return done();
            });
        });

        after((done) => {
            fs.unlink(imagePath, () => {
                fs.rmdir(path.dirname(imagePath), () => {
                    return done();
                });
            });
        });

        it('should add a image comment to aVenue', (mochaAsync(async () => {
            const res = await request(server)
                .post('/venues/' + aVenue._id + '/comments/image')
                .set('x-auth', token)
                .attach('image', imagePath)
                .send();
            res.should.have.status(200);
            const v = await Venue.findOne({_id: aVenue._id});
            v.comments.length.should.be.equal(1);
            const c = await Comment.findOne({_id: res.body._id});
            c.kind.should.equal('ImageComment');
        })));
    });
});