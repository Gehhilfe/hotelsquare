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
chai.use(require('chai-things'));

const request = require('supertest');

describe('User', () => {

    let u;
    let other;
    let token;
    let otherToken;

    beforeEach((done) => {
        Util.connectDatabase(mongoose).then(function () {
            User.remove({}).then(() => {
                User.create({name: 'peter', email: 'peter123@cool.de', password: 'peter99', gender: 'm'}).then((user) => {
                    u = user;
                    token = jsonwt.sign(u.toJSON(), config.jwt.secret, config.jwt.options);
                    User.create({name: 'peter2', email: 'peter1223@cool.de', password: 'peter99', gender: 'f'}).then((user) => {
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

    describe('PUT user', () => {
        it('should change the gender', (done) => {
            const before_updated_at = u.updated_at;

            request(server)
                .put('/user')
                .set('x-auth', token)
                .send({gender: 'm'})
                .end((err, res) => {
                    res.should.have.status(200);
                    res.body.should.be.a('object');
                    res.body.name.should.be.equal(u.name);
                    res.body.gender.should.be.equal('m');
                    res.body.updated_at.should.not.equal(before_updated_at);
                    return done();
                });
        });

        it('should change the password', (done) => {
            const before_password = u.password;

            request(server)
                .put('/user')
                .set('x-auth', token)
                .send({password: 'leetpassword'})
                .end((err, res) => {
                    res.should.have.status(200);
                    return User.findById(u._id).then((found) => {
                        found.password.should.not.equal(before_password);
                        return done();
                    });
                });
        });

        it('should set error when gender not valid', (done) => {
            const before_password = u.password;

            request(server)
                .put('/user')
                .set('x-auth', token)
                .send({gender: 's'})
                .end((err, res) => {
                    res.should.have.status(400);
                    res.body.errors.some((e) => e.field === 'gender').should.be.true;
                    return User.findById(u._id).then((found) => {
                        found.password.should.equal(before_password);
                        return done();
                    });
                });
        });

        it('should set error when password not valid', (done) => {
            const before_password = u.password;

            request(server)
                .put('/user')
                .set('x-auth', token)
                .send({password: 'short'})
                .end((err, res) => {
                    res.should.have.status(400);
                    res.body.errors.some((e) => e.field === 'password').should.be.true;
                    return User.findById(u._id).then((found) => {
                        found.password.should.equal(before_password);
                        return done();
                    });
                });
        });

        it('should not change created_at when nothing changed', (done) => {
            const before_updated_at = u.updated_at;

            request(server)
                .put('/user')
                .set('x-auth', token)
                .send({})
                .end((err, res) => {
                    res.should.have.status(200);
                    res.body.should.be.a('object');
                    res.body.name.should.be.equal(u.name);
                    res.body.updated_at.should.equal(before_updated_at.toJSON());
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
                    res.body.errors.should.contain.an.item.with.property('field', 'name');
                    return done();
                });
        });

        it('should not register user with same email', (done) => {
            const registrationData = {
                name: 'peter2123123',
                email: 'peter123@cool.de',
                password: 'secret'
            };
            request(server)
                .post('/user')
                .send(registrationData)
                .end((err, res) => {
                    res.should.have.status(400);
                    res.body.errors.should.contain.an.item.with.property('field', 'email');
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

    describe('DELETE profile/friend/:name', () => {
        describe('when a friend', () => {
            beforeEach((done) => {
                u.addFriend(other);
                other.addFriend(u);
                Promise.all([
                    u.save(),
                    other.save()
                ]).then(() => done());
            });

            it('should be possible to remove a friend', (done) => {
                request(server)
                    .del('/profile/friends/'+other.name)
                    .set('x-auth', token)
                    .end((err, res) => {
                        res.should.have.status(200);
                        Promise.all([
                            User.findById(u._id),
                            User.findById(other._id)
                        ]).then((results) => {
                            results[0].friends.length.should.be.equal(0);
                            results[1].friends.length.should.be.equal(0);
                            return done();
                        });
                    });
            });
        });

        describe('when not a friend', () => {
            it('should not be possible to remove a friend', (done) => {
                request(server)
                    .del('/profile/friends/'+other.name)
                    .set('x-auth', token)
                    .end((err, res) => {
                        res.should.have.status(200);
                        Promise.all([
                            User.findById(u._id),
                            User.findById(other._id)
                        ]).then((results) => {
                            results[0].friends.length.should.be.equal(0);
                            results[1].friends.length.should.be.equal(0);
                            return done();
                        });
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
                        other.friend_requests.should.not.be.empty;
                        return done();
                    });
                });
        });

        it('should result in error if a non existing friend request is tried to accept', (done) => {
            request(server)
                .put('/profile/friend_requests/'+u.name)
                .set('x-auth', token)
                .end((err, res) => {
                    res.should.have.status(400);
                    return done();
                });
        });

        describe('when a request already exists', () => {

            beforeEach((done) => {
                other.addFriendRequest(u);
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
                            u.friend_requests.length.should.be.equal(1);
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
                        Promise.all([
                            User.findById(u._id),
                            User.findById(other._id)
                        ]).then((results) => {
                            results[0].friend_requests.length.should.be.equal(0);
                            results[1].friend_requests.length.should.be.equal(0);
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
                        Promise.all([
                            User.findById(u._id),
                            User.findById(other._id)
                        ]).then((results) => {
                            results[0].friend_requests.length.should.be.equal(0);
                            results[1].friend_requests.length.should.be.equal(0);
                            results[0].friends.length.should.be.equal(0);
                            results[1].friends.length.should.be.equal(0);
                            return done();
                        });
                    });
            });
        });

        describe('when already friends', () => {
            beforeEach((done) => {
                u.addFriend(other);
                other.addFriend(u);
                Promise.all([
                    u.save(),
                    other.save()
                ]).then(() => done());
            });

            it('should result in error', (done) => {
                request(server)
                    .post('/user/'+other.name+'/friend_requests')
                    .set('x-auth', token)
                    .end((err, res) => {
                        res.should.have.status(400);
                        User.findOne({name: other.name}).then((other) => {
                            other.friend_requests.should.be.empty;
                            return done();
                        });
                    });
            });
        });
    });

    describe('search', () => {
        it('should find both users', (done) => {
            request(server)
                .post('/users')
                .set('x-auth', token)
                .send({ name: 'pet'})
                .end((err, res) => {
                    res.should.have.status(200);
                    res.body.length.should.equal(2);
                    return done();
                });
        });

        it('should find only peter2', (done) => {
            request(server)
                .post('/users')
                .set('x-auth', token)
                .send({ name: 'er2'})
                .end((err, res) => {
                    res.should.have.status(200);
                    res.body.length.should.equal(1);
                    return done();
                });
        });

        it('should filter by gender male', (done) => {
            request(server)
                .post('/users')
                .set('x-auth', token)
                .send({ name: 'p', gender: 'f'})
                .end((err, res) => {
                    res.should.have.status(200);
                    res.body.length.should.equal(1);
                    return done();
                });
        });

        it('should filter by gender female', (done) => {
            request(server)
                .post('/users')
                .set('x-auth', token)
                .send({ name: '2', gender: 'm'})
                .end((err, res) => {
                    res.should.have.status(200);
                    res.body.length.should.equal(0);
                    return done();
                });
        });
    });
});