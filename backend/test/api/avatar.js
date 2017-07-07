'use strict';

const mongoose = require('mongoose');
const chai = require('chai');
const server = require('../../server');
const Util = require('../../lib/util');
const jsonwt = require('jsonwebtoken');
const config = require('config');

const User = require('../../app/models/user');
const Image = require('../../app/models/image');

chai.should();

const expect = chai.expect;

const request = require('supertest');
const imageGenerator = require('js-image-generator');
const tempWrite = require('temp-write');
const fs = require('fs');
const path = require('path');

const testHelpers = require('../../lib/test_helpers');

const mochaAsync = (fn) => {
    return (done) => {
        fn.call().then(done, (err) => {
            return done(err);
        });
    };
};

describe('User Avatar', () => {

    let user, token;

    beforeEach(mochaAsync(async () => {
        mongoose.Promise = global.Promise;

        await Util.connectDatabase(mongoose);
        await Promise.all([
            Image.remove({}),
            User.remove({})
        ]);

        user = await User.create({name: 'peter', email: 'peter1@cool.de', password: 'peter99'});
        token = jsonwt.sign(user.toJSON(), config.jwt.secret, config.jwt.options);
    }));

    describe('/profile/avatar', () => {

        let image;
        let imagePath;

        before((done) => {
            imageGenerator.generateImage(300, 600, 80, (err, i) => {
                expect(err).to.be.null;
                image = i;
                imagePath = tempWrite.sync(image.data, 'avatar.jpeg');
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
                .attach('image', imagePath)
                .end((err, res) => {
                    expect(err).to.be.null;
                    console.log(res.body);
                    res.status.should.be.equal(200);
                    return done();
                });
        }).timeout(10000);

        it('should delete the stored image', (done) => {
            request(server)
                .post('/profile/avatar')
                .set('x-auth', token)
                .attach('image', imagePath)
                .end((err) => {
                    expect(err).to.be.null;
                    request(server)
                        .del('/profile/avatar')
                        .set('x-auth', token)
                        .end((err, res) => {
                            expect(res.body.avatar).to.be.undefined;
                            return done();
                        });
                });
        });
    });
});