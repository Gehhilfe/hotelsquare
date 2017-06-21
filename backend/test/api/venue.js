'use strict';
//const config = require('config');
//const mongoose = require('mongoose');
const chai = require('chai');
const chaiHttp = require('chai-http');
const server = require('../../server');
//const expect = chai.expect;
const request = require('supertest');
chai.should();
chai.use(chaiHttp);

describe('google api query', () => {
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

        return done();
    });
});