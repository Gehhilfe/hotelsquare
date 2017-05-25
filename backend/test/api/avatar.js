'use strict';

const mongoose = require('mongoose');
const chai = require('chai');
const chaiHttp = require('chai-http');
const server = require('../../server');
const util = require('../../lib/util');
chai.should();
chai.use(chaiHttp);

const expect = chai.expect;

const request = require('supertest');
const imageGenerator = require('js-image-generator');
const tempWrite = require('temp-write');
const fs = require('fs');
const path = require('path');

const testHelpers = require('../../lib/test_helpers');

describe('User Avatar', () => {

    const token = testHelpers.createToken({
        _id: '0815d3241212',
        name: 'testUser',
        email: 'test@test.de'
    });

    beforeEach((done) => {
        util.connectDatabase(mongoose);
        return done();
    });

    describe('/profile/avatar', () => {

        let image;
        let imagePath;

        before((done) => {
            imageGenerator.generateImage(300, 600, 80, (err, i) => {
                expect(err).to.be.null;
                image = i;
                imagePath = tempWrite.sync(image.data, 'avatar.jpeg');
                console.log('Image Path: ' + imagePath);
                return done();
            });
        });

        after((done) => {
            fs.unlink(imagePath, () => {
                fs.rmdir(path.dirname(imagePath), () => {
                    return done();
                });
            });
        });

        it('should handle a uploaded image', (done) => {
            request(server)
                .post('/profile/avatar')
                .set('x-auth', token)
                .attach('avatar', imagePath)
                .end((err, res) => {
                    expect(err).to.be.null;
                    res.should.have.status(200);
                    return done();
                });
        });

        it('should retrieve stored image', (done) => {
            request(server)
                .get('/profile/avatar')
                .set('x-auth', token)
                .end((err, res) => {
                    expect(err).to.be.null;
                    res.should.have.status(200);
                    res.should.have.header('content-type', /jpeg/);
                    return done();
                });
        });

        it('should respond with 404 when image not found', (done) => {
            request(server)
                .get('/user/unknown/avatar')
                .set('x-auth', token)
                .end((err, res) => {
                    expect(err).to.be.null;
                    res.should.have.status(404);
                    return done();
                });
        });

        it('should delete the stored image', (done) => {
            request(server)
                .post('/profile/avatar')
                .set('x-auth', token)
                .attach('avatar', imagePath)
                .end((err) => {
                    expect(err).to.be.null;
                    request(server)
                        .del('/user/avatar')
                        .set('x-auth', token)
                        .end((err) => {
                            expect(err).to.be.null;
                            request(server)
                                .get('/user/avatar')
                                .set('x-auth', token)
                                .end((err, res) => {
                                    expect(err).to.be.null;
                                    res.should.have.status(404);
                                    return done();
                                });
                        });
                });
        });
    });
});