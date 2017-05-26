'use strict';
const config = require('config');
const mongoose = require('mongoose');
const chai = require('chai');
const chaiHttp = require('chai-http');
const server = require('../../server');
const Util = require('../../lib/util');
const User = require('../../app/models/user');
const jsonwt = require('jsonwebtoken');
const expect = chai.expect;
chai.should();
chai.use(chaiHttp);
const request = require('supertest');
const when = require('when');

describe('User', () => {

    let u;
    let other;
    let token;
    let otherToken;

    beforeEach((done) => {
        Util.connectDatabase(mongoose).then(function () {
            User.remove({}).then(() => {
                User.create({name: 'peter', email: 'peter123@cool.de', password: 'peter99'}).then((user) => {
                    u = user;
                    token = jsonwt.sign(u.toJSON(), config.jwt.secret, config.jwt.options);
                    User.create({name: 'peter2', email: 'peter1223@cool.de', password: 'peter99'}).then((user) => {
                        other = user;
                        otherToken = jsonwt.sign(other.toJSON(), config.jwt.secret, config.jwt.options);
                        return done();
                    });
                });
            });
        });
    });

    describe('GET user', () => {
        it('should retrieve user information when name given', (done) => {
            request(server)
                .get('/user/'+u.name)
                .end((err, res) => {
                    res.should.have.status(200);
                    res.body.should.be.a('object');
                    res.body.name.should.be.equal(u.name);
                    expect(res.body.email).to.be.undefined;
                    return done();
                });
        });

        it('should respond with 404 if user is unkown', (done) => {
            request(server)
                .get('/user/unkown')
                .end((err, res) => {
                    res.should.have.status(404);
                    return done();
                });
        });

        it('should retrieve own user information when authenticated and no name given', (done) => {
            request(server)
                .get('/user')
                .set('x-auth', token)
                .end((err, res) => {
                    res.should.have.status(200);
                    res.body.should.be.a('object');
                    res.body.name.should.be.equal(u.name);
                    return done();
                });
        });


    });

    describe('POST user', () => {

        it('should register a new user with valid data', (done) => {
            const registrationData = {
                name: 'testTest',
                email: 'mail@online.de',
                password: 'secret'
            };
            request(server)
                .post('/user')
                .send(registrationData)
                .end((err, res) => {
                    res.should.have.status(200);
                    return done();
                });
        });

        it('should not register user with same name', (done) => {
            const registrationData = {
                name: 'peter',
                email: 'mail@online.de',
                password: 'secret'
            };
            request(server)
                .post('/user')
                .send(registrationData)
                .end((err, res) => {
                    res.should.have.status(400);
                    return done();
                });
        });

        it('registration without name should lead to error', (done) => {
            const registrationData = {
                email: 'mail@online.de',
                password: 'secret'
            };
            request(server)
                .post('/user')
                .send(registrationData)
                .end((err, res) => {
                    res.should.have.status(400);
                    return done();
                });
        });

        it('registration without email should lead to error', (done) => {
            const registrationData = {
                name: 'test',
                password: 'secret'
            };
            request(server)
                .post('/user')
                .send(registrationData)
                .end((err, res) => {
                    res.should.have.status(400);
                    return done();
                });
        });

        it('registration without pw should lead to error', (done) => {
            const registrationData = {
                email: 'mail@online.de',
                name: 'test'
            };
            request(server)
                .post('/user')
                .send(registrationData)
                .end((err, res) => {
                    res.should.have.status(400);
                    return done();
                });
        });


    });

    describe('DELETE user', () => {

        it('should delete user if authenticated', (done) => {
            request(server)
                .delete('/user')
                .set('x-auth', token)
                .end((err, res) => {
                    res.should.have.status(200);
                    User.findById(u._doc._id, (error, user) => {
                        expect(user).to.be.null;
                        return done();
                    });
                });
        });

    });

    describe('friend requests', () => {
        it('should be able to send a friend request', (done) => {
            request(server)
                .post('/user/'+other.name+'/friend_requests')
                .set('x-auth', token)
                .end((err, res) => {
                    res.should.have.status(200);
                    User.findOne({name: other.name}).then((other) => {
                        other.friendRequests.should.not.be.empty;
                        return done();
                    });
                });
        });

        describe('when a request already exists', () => {

            beforeEach((done) => {
                other.friendRequests.push(u);
                other.save().then(() => {
                    return done();
                });
            });

            it('should not add another request', (done) => {
                request(server)
                    .post('/user/'+other.name+'/friend_requests')
                    .set('x-auth', token)
                    .end((err, res) => {
                        res.should.have.status(400);
                        User.findOne({name: other.name}).then((u) => {
                            u.friendRequests.length.should.be.equal(1);
                            return done();
                        });
                    });
            });

            it('should be able to accept', (done) => {
                request(server)
                    .put('/profile/friend_requests/'+u.name)
                    .set('x-auth', otherToken)
                    .send({accept: true})
                    .end((err, res) => {
                        res.should.have.status(200);
                        when.all([
                            User.findById(u._id),
                            User.findById(other._id)
                        ]).then((results) => {
                            results[0].friendRequests.length.should.be.equal(0);
                            results[1].friendRequests.length.should.be.equal(0);
                            results[0].friends.length.should.be.equal(1);
                            results[1].friends.length.should.be.equal(1);
                            return done();
                        });
                    });
            });

            it('should be able to decline', (done) => {
                request(server)
                    .put('/profile/friend_requests/'+u.name)
                    .set('x-auth', otherToken)
                    .send({accept: false})
                    .end((err, res) => {
                        res.should.have.status(200);
                        when.all([
                            User.findById(u._id),
                            User.findById(other._id)
                        ]).then((results) => {
                            results[0].friendRequests.length.should.be.equal(0);
                            results[1].friendRequests.length.should.be.equal(0);
                            results[0].friends.length.should.be.equal(0);
                            results[1].friends.length.should.be.equal(0);
                            return done();
                        });
                    });
            });
        });
    });
});