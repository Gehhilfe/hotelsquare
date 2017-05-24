'use strict';

const mock = require('mock-require');
const chai = require('chai');
const expect = chai.expect;

let failVerify = false;
let decoded = {
    test: 'a'
};

mock('jsonwebtoken', {
    verify: (token, secret, cb) => {
        if(failVerify)
            return cb(new Error('Wrong signature'));
        else
            return cb(null, true);
    } 
});

const auth = mock.reRequire('../../../app/middleware/filter/authentication');

describe('authentication filter', () => {
    let request;
    let response;
    beforeEach((done) => {
        request = {
            headers: {
                'x-auth': 'token'
            },
            authentication: undefined
        };
        
        response = {
            status: (code) => {
                
            },
            json: (body) => {
                
            }
        }
        return done();
    });
    
    
     it('should add decode to request.authentication with valid signature', (done) => {
        failVerify = false;
        auth(request, response, () => {
            expect(request.authentication).to.not.be.undefined;
            return done();
        });
     });
     
     it('should not add decode to request.authentication with invalid signature', (done) => {
        failVerify = true;
        auth(request, response, () => {
            expect(request.authentication).to.be.undefined;
            return done();
        });
     });
     
     it('should not add decode to request.authentication without header', (done) => {
        failVerify = false;
        request = {
            headers: {
                'x-auth': undefined  
            },
            authentication: undefined
        };
        auth(request, response, () => {
            expect(request.authentication).to.be.undefined;
            return done();
        });
     });
});