'use strict';
const mongoose = require('mongoose');
const Image = require('../../app/models/image');
const Comment = require('../../app/models/comment');
const Util = require('../../lib/util');
const chai = require('chai');
const chaiHttp = require('chai-http');
const server = require('../../server');
const User = require('../../app/models/user');
const jsonwt = require('jsonwebtoken');
const config = require('config');
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

describe('image', () => {

    let image;
    let user, token;
    beforeEach(mochaAsync(async () => {
        mongoose.Promise = global.Promise;

        await Util.connectDatabase(mongoose);
        await Image.remove({});
        await User.remove({});

        image = await Image.create({
            location: {
                type: 'Point',
                coordinates: [-74.0059, 40.7127]
            }
        }) ;

        user = await User.create({name: 'peter111', email: 'peter123@cool.de', password: 'peter99', gender: 'm'});
        token = jsonwt.sign(user.toJSON(), config.jwt.secret, config.jwt.options);
        const comment = await Comment.create({
            kind: 'ImageComment',
            author: user,
            text: 'this is a comment',
            likes: 0,
            dislikes: 0,
            date: Date.now(),
            image: image
        });
        const bcomment = await Comment.create({
            kind: 'VenueComment',
            author: user,
            text: 'this is a second comment',
            likes: 0,
            dislikes: 0,
            date: Date.now(),
            image: image
        });
        if(comment && bcomment) {
            image.comments.push(comment);
            image.comments.push(bcomment);
            await image.save();
        }
    }));

    it('should get all comments', (mochaAsync(async () => {
        const res = await request(server)
            .get('/images/' + image._id + '/comments');
        res.body.length.should.equal(2);
    })));
});