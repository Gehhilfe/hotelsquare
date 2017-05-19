'use strict';

const mongoose = require('mongoose');
const chai = require('chai');
const chaiHttp = require('chai-http');
const server = require('../../server');
const util = require('../../lib/util');
chai.should();
chai.use(chaiHttp);


describe('User', () => {
    beforeEach((done) => {
        util.connectDatabase(mongoose);
        return done();
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

        it('should delete user if authenticated', (done) => {
            chai.request(server)
                .delete('/user')
                .set('x-auth', 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ')
                .end((err, res) => {
                    res.should.have.status(200);
                    return done();
                });
        });

        it('should fail when token is not signed correct', (done) => {
            chai.request(server)
                .delete('/user')
                .set('x-auth', 'eyJhbGciOI1NiIsInRcCI6IkpXVCJ9.eyQzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ')
                .end((err, res) => {
                    res.should.have.status(403);
                    return done();
                });
        });

        it('should fail when header is missing', (done) => {
            chai.request(server)
                .delete('/user')
                .end((err, res) => {
                    res.should.have.status(403);
                    return done();
                });
        });

    });
});