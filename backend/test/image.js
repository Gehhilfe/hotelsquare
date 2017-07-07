'use strict';

const _ = require('lodash');
const mongoose = require('mongoose');
const User = require('../app/models/user');
const Image = require('../app/models/image');
const Util = require('../lib/util');
const config = require('config');
const minio = require('minio');
const minioClient = new minio.Client({
    endPoint: 'stimi.ovh',
    port: 9000,
    secure: false,
    accessKey: config.minio.key,
    secretKey: config.minio.secret
});

const chai = require('chai');
chai.should();
chai.use(require('chai-date'));
const expect = chai.expect;

const imageGenerator = require('js-image-generator');
const tempWrite = require('temp-write');
const fs = require('fs');
const path = require('path');

const mochaAsync = (fn) => {
    return (done) => {
        fn.call().then(done, (err) => {
            return done(err);
        });
    };
};

describe('image', () => {

    beforeEach(mochaAsync(async () => {
        mongoose.Promise = global.Promise;

        await Util.connectDatabase(mongoose);
        await Promise.all([
            Image.remove({}),
            User.remove({})
        ]);
    }));


    describe('property', () => {
        describe('uuid', () => {
            it('should be initialized with a uuid', () => {
                const img = new Image();
                img.uuid.should.not.be.false;
            });

            it('should be unique for different images', () => {
                const a = new Image();
                const b = new Image();
                a.uuid.should.not.be.equal(b.uuid);
                a.uuid.should.be.equal(a.uuid);
            });
        });

        describe('created_at', () => {
            it('should be initialized with now date', () => {
                const img = new Image();
                img.created_at.should.be.today;
            });
        });
    });

    describe('static functions', () => {

        let image, imagePath;

        before((done) => {
            imageGenerator.generateImage(1920, 1080, 80, (err, i) => {
                expect(err).to.be.null;
                image;
                image = i;
                imagePath = tempWrite.sync(image.data, 'image.jpeg');
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

        it('should upload and remove image', mochaAsync(async () => {
            const img = await Image.upload(imagePath);
            img.should.not.be.null;
            // Test if images are on cloud storage
            const testObj = (name) => {
                return new Promise((resolve, reject) => {
                    minioClient.statObject(config.minio.bucket, name, (err) => {
                        if (err)
                            reject(err);
                        else
                            resolve();
                    });
                });
            };

            const testFailObj = (name) => {
                return new Promise((resolve, reject) => {
                    minioClient.statObject(config.minio.bucket, name, (err) => {
                        if (err)
                            resolve(err);
                        else
                            reject();
                    });
                });
            };

            await Promise.all(
                _.map(img.filenames(), (e) => {
                    testObj(e);
                })
            );

            await Image.destroy(img);

            await Promise.all(
                _.map(img.filenames(), (e) => {
                    testFailObj(e);
                })
            );

        })).timeout(10000);
    });
});