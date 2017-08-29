'use strict';
const config = require('config');
const mongoose = require('mongoose');
const chai = require('chai');
const chaiHttp = require('chai-http');
const server = require('../../server');
const Util = require('../../lib/util');
const User = require('../../app/models/user');
const Image = require('../../app/models/image');
const jsonwt = require('jsonwebtoken');
const expect = chai.expect;
chai.should();
chai.use(chaiHttp);
chai.use(require('chai-things'));

const request = require('supertest');

const mochaAsync = (fn) => {
    return (done) => {
        fn.call().then(done, (err) => {
            return done(err);
        });
    };
};


describe('Friends', () => {

    let peter;
    let peter2;
    let peterToken;
    let peter2Token;

    beforeEach(mochaAsync(async () => {
        mongoose.Promise = global.Promise;

        await Util.connectDatabase(mongoose);
        await Image.remove({});
        await User.remove({});

        const avatar = await Image.create({});
        peter = await User.create({
            name: 'peter111',
            email: 'peter123@cool.de',
            password: 'peter99',
            gender: 'm',
            avatar: avatar
        });
        peterToken = jsonwt.sign(peter.toJSON(), config.jwt.secret, config.jwt.options);

        peter2 = await User.create({
            name: 'peter1112',
            email: 'peter1223@cool.de',
            password: 'peter99',
            gender: 'f',
            avatar: avatar
        });
        peter2Token = jsonwt.sign(peter2.toJSON(), config.jwt.secret, config.jwt.options);

        peter2.addFriend(peter);
        peter.addFriend(peter2);
        await peter.save();
        await peter2.save();
    }));

    it('GET profile/friends', (mochaAsync(async () => {
        const res = await request(server)
            .get('/profile/friends')
            .set('x-auth', peterToken)
            .send();

        res.should.have.status(200);
        res.body.friends.should.be.a('array');
        res.body.friends.length.should.be.equal(1);
    })));

    it('GET profile/friends only query', (mochaAsync(async () => {
        const res = await request(server)
            .get('/profile/friends')
            .set('x-auth', peterToken)
            .query({'only': peter2._id})
            .send();

        res.should.have.status(200);
        res.body.friends.should.be.a('array');
        res.body.friends.length.should.be.equal(1);
    })));

    it('search nearybyfriends', (mochaAsync(async () => {
        const res = await request(server)
            .post('/searches/nearbyfriends')
            .set('x-auth', peterToken)
            .send({
                type: 'Point',
                coordinates: [0, 0]
            });

        res.should.have.status(200);
        res.body.should.be.a('array');
        res.body.length.should.be.equal(1);
    })));
});