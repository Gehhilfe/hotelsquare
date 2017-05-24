'use strict';
const config = require('config');
const mongoose = require('mongoose');
const chai = require('chai');
const chaiHttp = require('chai-http');
const server = require('../../server');
const util = require('../../lib/util');
const User = require('../../app/models/user');
const jsonwt = require('jsonwebtoken');
const expect = chai.expect;
chai.should();
chai.use(chaiHttp);


describe('User', () => {

    let u;

    before((done) => {
        util.connectDatabase(mongoose);
        User.create({name: 'peter', email: 'peter123@cool.de', password: 'peter99'}).then((param) => {
            u = param;
            return done();
        }).catch((error ) => {
            console.log(error);
            return done();
        });
    });

    describe('/POST user', () => {

        it('should register a new user with valid data', (done) => {
            const registrationData = {
                name: 'test',
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

        let token;
        before((done) => {
            token = jsonwt.sign(u.toJSON(), config.jwt.secret, config.jwt.options);
            return done();
        });

        it('should delete user if authenticated', () => {
            chai.request(server)
                .delete('/user')
                .set('x-auth', token)
                .end((err, res) => {
                    res.should.have.status(200);
                    User.findById(u._doc._id, (error, user) => {
                        return expect(user).to.be.null;
                    });
                });
        });

    });
});