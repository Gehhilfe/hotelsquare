'use strict';

const mongoose = require('mongoose');
const chai = require('chai');
const chaiHttp = require('chai-http');
const server = require('../../server');
const util = require('../../lib/util');
chai.should();
chai.use(chaiHttp);

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
    
    describe('POST /user/avater', () => {
        it('should require an authentication', (done) => {
            chai.request(server)
                .post('/user/avatar')
                .set('x-auth', token)
                .end((err, res) => {
                    res.should.have.status(200);
                    return done();
                });
        });
        
        it('should return 403 without authentication', (done) => {
            chai.request(server)
                .post('/user/avatar')
                .end((err, res) => {
                    res.should.have.status(403);
                    return done();
                });
        });
        
    });
});