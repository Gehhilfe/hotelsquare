'use strict';

const mongoose = require('mongoose');
const chai = require('chai');
const chaiHttp = require('chai-http');
const server = require('../../server');
const util = require('../../lib/util');
chai.should();
const expect = chai.expect;
chai.use(chaiHttp);

const jwt = require('jsonwebtoken');
const config = require('config');

require('../../app/models/user');
const User = mongoose.model('User');

describe('Session', () => {
    beforeEach((done) => {
        util.connectDatabase(mongoose);
        return done();
    });

    describe('/POST session', () => {
        beforeEach((done) => {
            User.remove({}).then(() => {
                User.create({
                    name: 'test',
                    password: 'secret',
                    email: 'test@test.de'
                }).then(() => {return done();});
            });
        });

        it('should return a new jwt with correct login details', (done) => {
            const loginDetails = {
                name: 'test',
                password: 'secret'
            };
            chai.request(server)
                .post('/session')
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
            chai.request(server)
                .post('/session')
                .send(loginDetails)
                .end((err, res) => {
                    res.should.have.status(401);
                    return done();
                });
        });
    });
});