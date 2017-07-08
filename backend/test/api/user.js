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

    let peter;
    let peter2;
    let peterToken;
    let peter2Token;


    beforeEach((done) => {
        Util.connectDatabase(mongoose).then(function () {
            User.remove({}).then(() => {
                User.create({name: 'peter111', email: 'peter123@cool.de', password: 'peter99', gender: 'm'}).then((user) => {
                    peter = user;
                    peterToken = jsonwt.sign(peter.toJSON(), config.jwt.secret, config.jwt.options);
                    User.create({name: 'peter1112', email: 'peter1223@cool.de', password: 'peter99', gender: 'f'}).then((user) => {
                        peter2 = user;
                        peter2Token = jsonwt.sign(peter2.toJSON(), config.jwt.secret, config.jwt.options);
                        User.create({name: 'peter1113', email: 'peter12223@cool.de', password: 'peter99'}).then(() => {
                            return done();
                        });
                    });
                });
            });
        });
    });

    describe('GET user', () => {
        it('should retrieve user information when name given', (done) => {
            request(server)
                .get('/users/'+peter.name)
                .end((err, res) => {
                    res.should.have.status(200);
                    res.body.should.be.a('object');
                    res.body.name.should.be.equal(peter.name);
                    expect(res.body.email).to.be.undefined;
                    return done();
                });
        });

        it('should respond with 404 if user is unkown', (done) => {
            request(server)
                .get('/users/unkown')
                .end((err, res) => {
                    res.should.have.status(404);
                    return done();
                });
        });

        it('should retrieve own user information when authenticated and no name given', (done) => {
            request(server)
                .get('/profile')
                .set('x-auth', peterToken)
                .end((err, res) => {
                    res.should.have.status(200);
                    res.body.should.be.a('object');
                    res.body.name.should.be.equal(peter.name);
                    return done();
                });
        });


    });

    describe('PUT user', () => {
        it('should change the gender', (done) => {
            const before_updated_at = peter.updated_at;

            request(server)
                .put('/users')
                .set('x-auth', peterToken)
                .send({gender: 'm'})
                .end((err, res) => {
                    res.should.have.status(200);
                    res.body.should.be.a('object');
                    res.body.name.should.be.equal(peter.name);
                    res.body.gender.should.be.equal('m');
                    res.body.updated_at.should.not.equal(before_updated_at);
                    return done();
                });
        });
        
        it('should change the location', (done) => {
            const before_location = peter.location.coordinates;
            
            request(server)
                .put('/users')
                .set('x-auth', peterToken)
                .send({location:{ coordinates: [1.0, 1.0]}})
                .end((err, res) => {
                    res.should.have.status(200);
                    res.body.should.be.a('object');
                    res.body.name.should.be.equal(peter.name);
                    res.body.gender.should.be.equal('m');
                    res.body.location.coordinates.should.not.equal(before_location);
                    return done();
                });
        });

        it('should change the password', (done) => {
            const before_password = peter.password;

            request(server)
                .put('/users')
                .set('x-auth', peterToken)
                .send({password: 'leetpassword'})
                .end((err, res) => {
                    res.should.have.status(200);
                    return User.findById(peter._id).then((found) => {
                        found.password.should.not.equal(before_password);
                        return done();
                    });
                });
        });

        it('should set error when gender not valid', (done) => {
            const before_password = peter.password;

            request(server)
                .put('/users')
                .set('x-auth', peterToken)
                .send({gender: 's'})
                .end((err, res) => {
                    res.should.have.status(400);
                    res.body.errors.some((e) => e.field === 'gender').should.be.true;
                    return User.findById(peter._id).then((found) => {
                        found.password.should.equal(before_password);
                        return done();
                    });
                });
        });

        it('should set error when password not valid', (done) => {
            const before_password = peter.password;

            request(server)
                .put('/users')
                .set('x-auth', peterToken)
                .send({password: 'short'})
                .end((err, res) => {
                    res.should.have.status(400);
                    res.body.errors.some((e) => e.field === 'password').should.be.true;
                    return User.findById(peter._id).then((found) => {
                        found.password.should.equal(before_password);
                        return done();
                    });
                });
        });

        it('should not change created_at when nothing changed', (done) => {
            const before_updated_at = peter.updated_at;

            request(server)
                .put('/users')
                .set('x-auth', peterToken)
                .send({})
                .end((err, res) => {
                    res.should.have.status(200);
                    res.body.should.be.a('object');
                    res.body.name.should.be.equal(peter.name);
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
                .post('/users')
                .send(registrationData)
                .end((err, res) => {
                    res.should.have.status(200);
                    return done();
                });
        });

        it('should not register user with same name', (done) => {
            const registrationData = {
                name: peter.name,
                email: 'mail@online.de',
                password: 'secret'
            };
            request(server)
                .post('/users')
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
                .post('/users')
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
                .post('/users')
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
                .post('/users')
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
                .post('/users')
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
                .delete('/users')
                .set('x-auth', peterToken)
                .end((err, res) => {
                    res.should.have.status(200);
                    User.findById(peter._doc._id, (error, user) => {
                        expect(user).to.be.null;
                        return done();
                    });
                });
        });

    });

    describe('DELETE profile/friend/:name', () => {
        describe('when a friend', () => {
            beforeEach((done) => {
                peter.addFriend(peter2);
                peter2.addFriend(peter);
                Promise.all([
                    peter.save(),
                    peter2.save()
                ]).then(() => done());
            });

            it('should be possible to remove a friend', (done) => {
                request(server)
                    .del('/profile/friends/'+peter2.name)
                    .set('x-auth', peterToken)
                    .end((err, res) => {
                        res.should.have.status(200);
                        Promise.all([
                            User.findById(peter._id),
                            User.findById(peter2._id)
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
                    .del('/profile/friends/'+peter2.name)
                    .set('x-auth', peterToken)
                    .end((err, res) => {
                        res.should.have.status(200);
                        Promise.all([
                            User.findById(peter._id),
                            User.findById(peter2._id)
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
                .post('/users/'+peter2.name+'/friend_requests')
                .set('x-auth', peterToken)
                .end((err, res) => {
                    res.should.have.status(200);
                    User.findOne({name: peter2.name}).then((other) => {
                        other.friend_requests.should.not.be.empty;
                        return done();
                    });
                });
        });

        it('should result in error if a non existing friend request is tried to accept', (done) => {
            request(server)
                .put('/profile/friend_requests/'+peter.name)
                .set('x-auth', peterToken)
                .end((err, res) => {
                    res.should.have.status(400);
                    return done();
                });
        });

        describe('when a request already exists', () => {

            beforeEach((done) => {
                peter2.addFriendRequest(peter);
                peter2.save().then(() => {
                    return done();
                });
            });

            it('should not add another request', (done) => {
                request(server)
                    .post('/users/'+peter2.name+'/friend_requests')
                    .set('x-auth', peterToken)
                    .end((err, res) => {
                        res.should.have.status(400);
                        User.findOne({name: peter2.name}).then((u) => {
                            u.friend_requests.length.should.be.equal(1);
                            return done();
                        });
                    });
            });

            it('should be able to accept', (done) => {
                request(server)
                    .put('/profile/friend_requests/'+peter.name)
                    .set('x-auth', peter2Token)
                    .send({accept: true})
                    .end((err, res) => {
                        res.should.have.status(200);
                        Promise.all([
                            User.findById(peter._id),
                            User.findById(peter2._id)
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
                    .put('/profile/friend_requests/'+peter.name)
                    .set('x-auth', peter2Token)
                    .send({accept: false})
                    .end((err, res) => {
                        res.should.have.status(200);
                        Promise.all([
                            User.findById(peter._id),
                            User.findById(peter2._id)
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
                peter.addFriend(peter2);
                peter2.addFriend(peter);
                Promise.all([
                    peter.save(),
                    peter2.save()
                ]).then(() => done());
            });

            it('should result in error', (done) => {
                request(server)
                    .post('/users/'+peter2.name+'/friend_requests')
                    .set('x-auth', peterToken)
                    .end((err, res) => {
                        res.should.have.status(400);
                        User.findOne({name: peter2.name}).then((other) => {
                            other.friend_requests.should.be.empty;
                            return done();
                        });
                    });
            });
        });
    });

    describe('search', () => {
        it('should not include themselfs', (done) => {
            request(server)
                .post('/searches/users')
                .set('x-auth', peterToken)
                .send({ name: 'pet'})
                .end((err, res) => {
                    res.should.have.status(200);
                    res.body.should.all.not.contain.item.with.property('name', 'peter');
                    return done();
                });
        });

        it('should find both users', (done) => {
            request(server)
                .post('/searches/users')
                .set('x-auth', peterToken)
                .send({ name: 'pet'})
                .end((err, res) => {
                    res.should.have.status(200);
                    res.body.length.should.equal(2);
                    return done();
                });
        });

        it('should find only peter2', (done) => {
            request(server)
                .post('/searches/users')
                .set('x-auth', peterToken)
                .send({ name: '12'})
                .end((err, res) => {
                    res.should.have.status(200);
                    res.body.length.should.equal(1);
                    return done();
                });
        });

        it('should filter by gender male', (done) => {
            request(server)
                .post('/searches/users')
                .set('x-auth', peterToken)
                .send({ name: 'p', gender: 'f'})
                .end((err, res) => {
                    res.should.have.status(200);
                    res.body.length.should.equal(1);
                    return done();
                });
        });

        it('should filter by gender female', (done) => {
            request(server)
                .post('/searches/users')
                .set('x-auth', peterToken)
                .send({ name: '2', gender: 'm'})
                .end((err, res) => {
                    res.should.have.status(200);
                    res.body.length.should.equal(0);
                    return done();
                });
        });

        describe('results', () => {

            let results;

            beforeEach((done) => {
                request(server)
                    .post('/searches/users')
                    .set('x-auth', peterToken)
                    .send({ name: 'pet'})
                    .end((err, res) => {
                        results = res.body;
                        return done();
                    });
            });

            it('should contain name', () => {
                results.should.all.contain.item.with.property('name');
            });

            it('should contain displayName', () => {
                results.should.all.contain.item.with.property('displayName');
            });

            it('should contain type', () => {
                results.should.all.contain.item.with.property('type', 'user');
            });

            it('should not contain email', () => {
                results.should.not.contain.an.item.with.property('email');
            });

            it('should not contain password', () => {
                results.should.not.contain.an.item.with.property('password');
            });

            it('should contain friends as number', () => {
                results[0].friends.should.be.a('number');
            });

            it('should not contain friend_requests', () => {
                results.should.not.contain.an.item.with.property('friend_requests');
            });
        });
    });
});