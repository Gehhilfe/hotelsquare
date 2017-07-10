'use strict';
const config = require('config');
const mongoose = require('mongoose');
const Venue = require('../../app/models/venue');
const Image = require('../../app/models/image');
const Comment = require('../../app/models/comment');
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

    before(mochaAsync(async () => {
        mongoose.Promise = global.Promise;

        await Util.connectDatabase(mongoose);
        await Venue.remove({});
        await User.remove({});
        await Comment.remove({});

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

        aComment = await Comment.create({kind: 'VenueComment', author: u, text: 'venue1 comment', venue: aVenue});
        bComment = await Comment.create({kind: 'VenueComment', author: u, text: 'venue2 comment', venue: aVenue});

        const venue = await Venue.findOne({_id: aVenue._id});
        venue.comments.push(aComment);
        venue.comments.push(bComment);
        await venue.save();

        anImage = await Image.create({
            location: {
                type: 'Point',
                coordinates: [5, 5]
            }
        });
    }));

    describe('POST comments to venue', () => {
        it('should add a comment to aVenue', (mochaAsync(async () => {
            const res = await request(server)
                .post('/comments')
                .set('x-auth', token)
                .send({
                    venueID: aVenue._id,
                    text: 'this is a comment'
                });
            res.should.have.status(200);
            const v = await Venue.findOne({_id: aVenue._id});
            v.comments.length.should.be.equal(3);
            const c = await Comment.findOne({text: 'this is a comment'});
            c.kind.should.equal('VenueComment');
        })));

        it('should add another comment to aVenue', (mochaAsync(async () => {
            const res = await request(server)
                .post('/comments')
                .set('x-auth', token)
                .send({
                    venueID: aVenue._id,
                    text: 'this is a second comment'
                });
            res.should.have.status(200);
            const v = await Venue.findOne({_id: aVenue._id});
            v.comments.length.should.be.equal(4);
            const c = await Comment.findOne({text: 'this is a comment'});
            c.kind.should.equal('VenueComment');
            const com = await Comment.findOne({text: 'this is a second comment'});
            com.kind.should.equal('VenueComment');
        })));

        it('should add a comment to anImage', (mochaAsync(async () => {
            const res = await request(server)
                .post('/comments')
                .set('x-auth', token)
                .send({
                    imageID: anImage._id,
                    text: 'this is an image comment'
                });
            res.should.have.status(200);
            const i = await Image.findOne({_id: anImage._id}).populate({path: 'comments', model: 'Comment'});
            i.comments.length.should.be.equal(1);
            const c = await Comment.findOne({text: 'this is an image comment'}).populate({path: 'author', model: 'User'});
            c.kind.should.equal('ImageComment');
        })));

        it('should add a comment to the first venue comment', (mochaAsync(async () => {
            const v = await Venue.findOne({_id: aVenue._id}).populate({path: 'comments', model: 'Comment'});
            const res = await request(server)
                .post('/comments')
                .set('x-auth', token)
                .send({
                    textID: v.comments[0]._id,
                    text: 'this is a comment comment'
                });
            res.should.have.status(200);
            const co = await Comment.findOne({_id: v.comments[0]._id});
            co.comments.length.should.be.equal(1);
            const c = await Comment.findOne({text: 'this is a comment comment'});
            c.kind.should.equal('TextComment');
        })));

        it('should return 400 because auth is missing', (mochaAsync(async () => {
            const res = await request(server)
                .post('/comments')
                .send({
                    venueID: aVenue._id,
                    text: 'this is a second comment'
                });
            res.should.have.property('status', 403);
            const v = await Venue.findOne({_id: aVenue._id});
            v.comments.length.should.be.equal(4);
        })));

    });

    describe('GET comment by id', () => {
        it('should get the image comment by id', (mochaAsync(async () => {
            const i = await Image.findOne({_id: anImage._id}).populate({path: 'comments', model: 'Comment'});
            const res = await request(server)
                .get('/comments/' + i.comments[0]._id);
            res.should.have.status(200);
            res.body.should.be.a('object');
            res.body.text.should.be.equal('this is an image comment');
        })));
    });


    describe('GET comments from venue', () => {
        it('should get all comments from aVenue', (mochaAsync(async () => {
            const res = await request(server)
                .get('/venues/' + aVenue._id + '/comments');
            res.should.have.status(200);
            const v = await Venue.findOne({_id: aVenue._id}).populate({path: 'comments', model: 'Comment'});
            v.comments.length.should.be.equal(4);
            res.body.should.be.a('array');
            res.body[0].text.should.be.equal('venue1 comment');
            res.body[1].text.should.be.equal('venue2 comment');
            res.body.length.should.be.equal(4);
        })));
    });

    describe('DEL a comment from venue', () => {
        it('should delete a comment from aVenue', (mochaAsync(async () => {
            const venue = await Venue.findOne({_id: aVenue._id}).populate({path: 'comments', model: 'Comment'});
            const res = await request(server)
                .del('/comments/' + venue.comments[0]._id)
                .set('x-auth', token);
            res.should.have.status(200);
            const v = await Venue.findOne({_id: aVenue._id}).populate({path: 'comments', model: 'Comment'});
            v.comments.length.should.be.equal(3);
            res.body.should.be.a('object');
            res.body.comments[2].text.should.be.equal('this is a comment');
        })));

        it('should not delete a comment from aVenue because user is not authenticated', (mochaAsync(async () => {
            const venue = await Venue.findOne({_id: aVenue._id}).populate({path: 'comments', model: 'Comment'});
            const res = await request(server)
                .del('/comments/' + venue.comments[0]._id);
            res.should.have.property('status', 403);
            const v = await Venue.findOne({_id: aVenue._id}).populate({path: 'comments', model: 'Comment'});
            v.comments.length.should.be.equal(3);
        })));
    });
});