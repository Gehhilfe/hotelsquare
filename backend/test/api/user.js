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


describe('User', () => {

    let u;
    let token;

    before((done) => {
        Util.connectDatabase(mongoose).then(function () {
            User.remove({}).then(() => {
                User.create({name: 'peter', email: 'peter123@cool.de', password: 'peter99'}).then((user) => {
                    u = user;
                    token = jsonwt.sign(u.toJSON(), config.jwt.secret, config.jwt.options);
                    return done();
                });
            });
        });
    });

    describe('GET user', () => {
        it('should retrieve user information when name given', (done) => {
            chai.request(server)
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
            chai.request(server)
                .get('/user/unkown')
                .end((err, res) => {
                    res.should.have.status(404);
                    return done();
                });
        });

        it('should retrieve own user information when authenticated and no name given', (done) => {
            chai.request(server)
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
            chai.request(server)
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
            chai.request(server)
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
            chai.request(server)
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
            chai.request(server)
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
            chai.request(server)
                .post('/user')
                .send(registrationData)
                .end((err, res) => {
                    res.should.have.status(400);
                    return done();
                });
        });


    });

    describe('/DELETE user', () => {

        it('should delete user if authenticated', (done) => {
            chai.request(server)
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
});