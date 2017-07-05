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

    let aVenue, bVenue;
    let u;
    let token;

    /**beforeEach((done) => {

        Util.connectDatabase(mongoose).then(function () {
            Venue.remove({}).then(() => {
                User.remove({}).then(() => {
                    User.create({name: 'peter', email: 'peter1@cool.de', password: 'peter99'}).then((user) =>{
                        u = user;
                        token = jsonwt.sign(u.toJSON(), config.jwt.secret, config.jwt.options);

                        Venue.create({
                            name: 'aVenue',
                            place_id: 'a',
                            location: {
                                type: 'Point',
                                coordinates: [5, 5]
                            }
                        }).then((avenue) => {
                            aVenue = avenue;
                            Venue.create({
                                name: 'bVenue',
                                place_id: 'b',
                                location: {
                                    type: 'Point',
                                    coordinates: [-5, -5]
                                }
                            }).then((bvenue) => {
                                bVenue = bvenue;
                                return done();
                            });
                        });
                    });
                });
            });
        });
    });*/

    beforeEach(mochaAsync(async() => {
        mongoose.Promise = global.Promise;

        await Util.connectDatabase(mongoose);
        await Venue.remove({});
        await User.remove({});

        const user = await User.create({name: 'peter', email: 'peter1@cool.de', password: 'peter99'});
        u = user;
        token = jsonwt.sign(u.toJSON(), config.jwt.secret, config.jwt.options);

        const avenue = await Venue.create({
            name: 'aVenue',
            place_id: 'a',
            location: {
                type: 'Point',
                coordinates: [5, 5]
            }
        });
        aVenue = avenue;
        const bvenue = Venue.create({
            name: 'bVenue',
            place_id: 'b',
            location: {
                type: 'Point',
                coordinates: [-5, -5]
            }
        });
        bVenue = bvenue;
    }));

    describe('POST comments to venue', () => {
        it('should add a comment to aVenue', (done) => {
            request(server)
                .post('/venues/comment')
                .set('x-auth', token)
                .send({
                    venueid: 'a',
                    comment: 'this is a comment'
                })
                .end((err, res) => {
                    res.should.have.status(200);
                    return done();
                });
        });

        it('should add another comment to aVenue', (done) => {
            request(server)
                .post('/venues/comment')
                .set('x-auth', token)
                .send({
                    venueid: 'a',
                    comment: 'this is a second comment'
                })
                .end((err, res) => {
                    res.should.have.status(200);
                    return done();
                });
        });

        it('should add a comment to bVenue', (done) => {
            request(server)
                .post('/venues/comment')
                .set('x-auth', token)
                .send({
                    venueid: 'b',
                    comment: 'this is a comment'
                })
                .end((err, res) => {
                    res.should.have.status(200);
                    return done();
                });
        });
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