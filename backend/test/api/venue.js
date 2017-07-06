'use strict';
const config = require('config');
const mongoose = require('mongoose');
const Venue = require('../../app/models/venue');
const SearchRequest = require('../../app/models/searchrequest');
const Util = require('../../lib/util');
const chai = require('chai');
const chaiHttp = require('chai-http');
const server = require('../../server');
const User = require('../../app/models/user');
const jsonwt = require('jsonwebtoken');
const expect = chai.expect;
const request = require('supertest');
chai.should();
chai.use(chaiHttp);
chai.use(require('chai-things'));

const mochaAsync = (fn) => {
    return (done) => {
        fn.call().then(done, (err) => {
            return done(err);
        });
    };
};

describe('comment api query', () => {

    let aVenue;
    let u;
    let token;

    before(mochaAsync(async () => {
        mongoose.Promise = global.Promise;

        await Util.connectDatabase(mongoose);
        await Venue.remove({});
        await User.remove({});

        u = await User.create({name: 'peter', email: 'peter1@cool.de', password: 'peter99'});
        token = jsonwt.sign(u.toJSON(), config.jwt.secret, config.jwt.options);

        aVenue = await Venue.create({
            name: 'aVenue',
            place_id: 'a',
            location: {
                type: 'Point',
                coordinates: [5, 5]
            }
        });
    }));

    describe('POST comments to venue', () => {
        it('should add a comment to aVenue', (mochaAsync(async () => {
            const res = await request(server)
                .post('/venues/' + aVenue._id + '/comments')
                .set('x-auth', token)
                .send({
                    comment: 'this is a comment'
                });
            res.should.have.status(200);
            const v = await Venue.findOne({_id: aVenue._id});
            v.comments.length.should.be.equal(1);
        })));

        it('should add another comment to aVenue', (mochaAsync(async () => {
            const res = await request(server)
                .post('/venues/' + aVenue._id + '/comments')
                .set('x-auth', token)
                .send({
                    comment: 'this is another comment'
                });
            res.should.have.status(200);
            const v = await Venue.findOne({_id: aVenue._id});
            v.comments.length.should.be.equal(2);
        })));

        it('should return 400 because auth is missing', (mochaAsync(async () => {
            const res = await request(server)
                .post('/venues/' + aVenue._id + '/comments')
                .send({
                    comment: 'this is another comment'
                });
            res.should.have.property('status',403);
            const v = await Venue.findOne({_id: aVenue._id});
            v.comments.length.should.be.equal(2);
        })));

    });

    describe('GET comments from venue', () => {
        it('should get all comments from aVenue', (mochaAsync(async () => {
            const res = await request(server)
                .get('/venues/' + aVenue._id + '/comments')
                .send({
                    venueid: 'a'
                });
            res.should.have.status(200);
            const v = await Venue.findOne({_id: aVenue._id});
            v.comments.length.should.be.equal(2);
            res.body.should.be.a('array');
            res.body[0].text.should.be.equal('this is a comment');
            res.body[1].text.should.be.equal('this is another comment');
            res.body.length.should.be.equal(2);
        })));
    });

    describe('DEL a comment from venue', () => {
        it('should delete a comment from aVenue', (mochaAsync(async () => {
            const res = await request(server)
                .del('/venues/' + aVenue._id + '/comment')
                .set('x-auth', token)
                .send({
                    venueid: 'a',
                    comment: 'this is a comment'
                });
            res.should.have.status(200);
            const v = await Venue.findOne({_id: aVenue._id});
            v.comments.length.should.be.equal(1);
            res.body.should.be.a('object');
            res.body.venue.comments.should.be.equal(['this is another comment']);
        })));

        it('should not delete a comment from aVenue because user is not authenticated', (mochaAsync(async () => {
            const res = await request(server)
                .del('/venues/' + aVenue._id + '/comment')
                .send({
                    venueid: 'a',
                    comment: 'this is a comment'
                });
            res.should.have.property('status', 403);
            const v = await Venue.findOne({_id: aVenue._id});
            v.comments.length.should.be.equal(1);
        })));
    });
});

describe('google api query', () => {

    before(async () => {
        mongoose.Promise = global.Promise;

        await Util.connectDatabase(mongoose);
        await Venue.remove({});
        await SearchRequest.remove({});
    });


    it('should return some places', (done) => {
        request(server)
            .post('/searches/venues')
            .send({
                location: {
                    type: 'Point',
                    coordinates: [-74.0059, 40.7127]
                },
                keyword: 'bar',
                radius: 1000
            })
            .end((err, res) => {
                res.should.have.status(200);
                return done();
            });
    });

    it('should return krone in darmstadt for Krone', (done) => {
        request(server)
            .post('/searches/venues')
            .send({
                locationName: 'Hügelstraße, Darmstadt',
                keyword: 'Krone',
                radius: 5000
            })
            .end((err, res) => {
                res.should.have.status(200);
                res.body.results.should.contain.a.thing.with.property('name', 'Goldene Krone');
                return done();
            });
    });

    it('should return krone in darmstadt when searching for bar', (done) => {
        request(server)
            .post('/searches/venues')
            .send({
                locationName: 'Schustergasse 18, 64283 Darmstadt',
                keyword: 'bar',
                radius: 5000
            })
            .end((err, res) => {
                res.should.have.status(200);
                res.body.results.should.contain.a.thing.with.property('name', 'Goldene Krone');
                return done();
            });
    });

    it('should return hobbit in darmstadt', (done) => {
        request(server)
            .post('/searches/venues')
            .send({
                locationName: 'Kantplatz, Darmstadt',
                keyword: 'Hobbit',
                radius: 5000
            })
            .end((err, res) => {
                res.should.have.status(200);
                res.body.results.should.contain.a.thing.with.property('name', 'Hobbit');
                return done();
            });
    });
});