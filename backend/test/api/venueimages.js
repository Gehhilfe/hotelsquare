'use strict';

const mongoose = require('mongoose');
const chai = require('chai');
const server = require('../../server');
const util = require('../../lib/util');
chai.should();

const expect = chai.expect;

const request = require('supertest');
const imageGenerator = require('js-image-generator');
const tempWrite = require('temp-write');
const fs = require('fs');
const path = require('path');

const testHelpers = require('../../lib/test_helpers');

describe('Venue images', () => {

    const token = testHelpers.createToken({
        _id: '0815d3241212',
        name: 'testUser',
        email: 'test@test.de'
    });

    beforeEach((done) => {
        util.connectDatabase(mongoose);
        return done();
    });

/**    describe('/venue/images', () => {

        let image;
        let imagePath;

        before((done) => {
            imageGenerator.generateImage(300, 600, 80, (err, i) => {
                expect(err).to.be.null;
                image = i;
                imagePath = tempWrite.sync(image.data, 'venueimage.jpeg');
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
                .post('/venue/images')
                .set('x-auth', token)
                .set('venueid', 'krone')
                .attach('venueimage', imagePath)
                .end((err, res) => {
                    expect(err).to.be.null;
                    res.status.should.be.equal(200);
                    return done();
                });
        });

        it('should redirect to stored image', (done) => {
            request(server)
                .get('/venue/images')
                .set('x-auth', token)
                .set('venueid', 'krone')
                .end((err, res) => {
                    expect(err).to.be.null;
                    res.status.should.be.equal(302);
                    return done();
                });
        });

        it('should respond with error when image not found', (done) => {
            request(server)
                .get('/venue/images')
                .set('x-auth', token)
                .set('venueid', 'krone')
                .end((err, res) => {
                    expect(err).to.be.null;
                    res.status.should.be.not.equal(200);
                    return done();
                });
        });

        it('should delete the stored image', (done) => {
            request(server)
                .post('/venue/images')
                .set('x-auth', token)
                .set('venueid', 'krone')
                .attach('venueimage', imagePath)
                .end((err) => {
                    expect(err).to.be.null;
                    request(server)
                        .del('/venue/venueimage')
                        .set('x-auth', token)
                        .end((err) => {
                            expect(err).to.be.null;
                            request(server)
                                .get('/venue/venueimage')
                                .set('x-auth', token)
                                .end((err, res) => {
                                    expect(err).to.be.null;
                                    res.status.should.be.equal(404);
                                    return done();
                                });
                        });
                });
        });
    });*/
});