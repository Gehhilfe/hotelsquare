'use strict';
//const config = require('config');
const mongoose = require('mongoose');
const Venue = require('../../app/models/venue');
const SearchRequest = require('../../app/models/searchrequests');
const Util = require('../../lib/util');
const chai = require('chai');
const chaiHttp = require('chai-http');
const server = require('../../server');
//const expect = chai.expect;
const request = require('supertest');
chai.should();
chai.use(chaiHttp);

describe('google api query', () => {

    beforeEach(async () => {
        mongoose.Promise = global.Promise;

        await Util.connectDatabase(mongoose);
        await Venue.remove({});
        await SearchRequest.remove({});
    });


    it('should return some places', (done) => {

        request(server)
            .post('/venues/query')
            .send({
                location: [40.7127, -74.0059],
                keyword: 'bar'
            })
            .end((err, res) => {
                res.should.have.status(200);
                console.log(JSON.stringify(res.body));
                return done();
            });
    });
});