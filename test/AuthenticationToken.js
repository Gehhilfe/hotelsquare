require('../models/User');
require('../models/AuthenticationToken');

const mongoose = require('mongoose');
const chai = require('chai');
const expect = chai.expect;
const chaiAsPromised = require('chai-as-promised');
chai.use(chaiAsPromised);

const Util = require('../lib/Util');
const User = mongoose.model('User');
const AuthToken = mongoose.model('AuthenticationToken');

describe('authenticationToken', function() {
    
    var validUser;
    var validToken;
    
    before(function(done) {
        mongoose.Promise = global.Promise;
        Util.connectDatabase(mongoose).then(done);
    });
    
    beforeEach(function(done){
        validUser = new User({
            name: 'Test',
            email: 'test@test.de',
            password: 'password'
        });
        
        User.remove({}).then(() => {
            validUser.save().then(() => {
                validToken = new AuthToken({
                    owner: validUser
                });
                validToken.save(done);
            });
        });
    });
    
    describe('#owner', function() {
        
        it('cant be empty', function(done) {
            validToken.owner = null;
            
            validToken.validate(function(err) {
                expect(err).to.not.be.null;
                return done();
            });
        });
    });
    
    describe('#token', function() {
        it('cant be empty', function(done) {
            validToken.token = null;
            
            validToken.validate(function(err) {
                expect(err).to.not.be.null;
                return done();
            });
        });
        
        it('should have a length of 36 characters', function() {
            expect(validToken.token.length).to.be.equal(36);
        });
        
        it('should be distinct after creation', function() {
            var anotherToken = new AuthToken({
                owner: validUser 
            });
            expect(anotherToken.token).to.not.be.equal(validToken.token);
        });
        
        it('should be unique', function() {
            var anotherToken = new AuthToken({
                owner: validUser,
                token: validToken.token
            });
            return expect(AuthToken.create(anotherToken)).to.eventually.rejected;
        });
    });
    
});