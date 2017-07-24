'use strict';

const mongoose = require('mongoose');
const chai = require('chai');
const chaiHttp = require('chai-http');
const server = require('../../server');
const util = require('../../lib/util');
chai.should();
const expect = chai.expect;
chai.use(chaiHttp);
const request = require('supertest');


const jwt = require('jsonwebtoken');
const config = require('config');

const User = require('../../app/models/user');
const Image = require('../../app/models/image');

const mochaAsync = (fn) => {
    return (done) => {
        fn.call().then(done, (err) => {
            return done(err);
        });
    };
};

describe('Session', () => {
    beforeEach((done) => {
        util.connectDatabase(mongoose);
        return done();
    });

    describe('/POST session', () => {

        let validUser;

        beforeEach((done) => {
            User.remove({}).then(() => {
                User.create({
                    name: 'test',
                    password: 'secret',
                    email: 'test@test.de'
                }).then((u) => {
                    validUser = u;
                    return done();
                });
            });
        });

        describe('when user has avatar', () => {
            beforeEach(mochaAsync(async () => {
                const img = new Image();
                img.uploader = validUser;
                await img.save();
                validUser.avatar = img;
                await validUser.save();
            }));

            it('should return a new jwt with correct login details', (done) => {
                const loginDetails = {
                    name: 'test',
                    password: 'secret'
                };
                request(server)
                    .post('/sessions')
                    .send(loginDetails)
                    .end((err, res) => {
                        res.should.have.status(200);
                        res.body.should.be.a('object');
                        jwt.verify(res.body.token, config.jwt.secret, config.jwt.options, (err, decoded) => {
                            expect(decoded.name).to.be.equal(loginDetails.name);
                        });
                        return done();
                    });
            });
        });

        it('should return a new jwt with correct login details', (done) => {
            const loginDetails = {
                name: 'test',
                password: 'secret'
            };
            request(server)
                .post('/sessions')
                .send(loginDetails)
                .end((err, res) => {
                    res.should.have.status(200);
                    res.body.should.be.a('object');
                    jwt.verify(res.body.token, config.jwt.secret, config.jwt.options, (err, decoded) => {
                        expect(decoded.name).to.be.equal(loginDetails.name);
                    });
                    return done();
                });
        });

        it('should return a 401 unauthorized with wrong login details', (done) => {
            const loginDetails = {
                name: 'test',
                password: 'wrong'
            };
            request(server)
                .post('/sessions')
                .send(loginDetails)
                .end((err, res) => {
                    res.should.have.status(401);
                    return done();
                });
        });
    });
});