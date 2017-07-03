'use strict';
//const config = require('config');
const mongoose = require('mongoose');
const Venue = require('../../app/models/venue');
const SearchRequest = require('../../app/models/searchrequest');
const Util = require('../../lib/util');
const chai = require('chai');
const chaiHttp = require('chai-http');
const server = require('../../server');
//const expect = chai.expect;
const request = require('supertest');
chai.should();
chai.use(chaiHttp);
chai.use(require('chai-things'));

describe('google api query', () => {

    before(async () => {
        mongoose.Promise = global.Promise;

        await Util.connectDatabase(mongoose);
        await Venue.remove({});
        await SearchRequest.remove({});
    });


    it('should return some places', (done) => {
        request(server)
            .post('/venues/query')
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
            .post('/venues/query')
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
            .post('/venues/query')
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
            .post('/venues/query')
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