require('../models/User');

const mongoose = require('mongoose');
const expect = require('chai').expect;
const Util = require('../lib/Util');
const User = mongoose.model('User');

describe('user', function() {


    var validUser;

    beforeEach(function(done) {
        mongoose.Promise = global.Promise;
        
        validUser = new User({
            name: 'Test',
            email: 'test@test.de',
            password: 'password'
        });

        if (mongoose.connection.db) return done();
        mongoose.connect(Util.databaseURI()).then(() => User.remove({}, done));
    });

    it('can be saved', function(done) {
        var u = new User(validUser);
        u.save(done);
    });

    describe('name', function() {
        it('cant be empty', function(done) {
            validUser.name = '';
            var u = new User(validUser);

            u.validate(function(err) {
                expect(err).to.not.be.null;
                expect(err.errors.name).to.exist;
                done();
            });
        });

        it('with 3 characters is invalid', function(done) {
            validUser.name = 'a'.repeat(3);
            var u = new User(validUser);

            u.validate(function(err) {
                expect(err).to.not.be.null;
                expect(err.errors.name).to.exist;
                return done();
            });
        });

        it('with 4 characters is valid', function(done) {
            validUser.name = 'a'.repeat(4);
            var u = new User(validUser);

            u.validate(function(err) {
                expect(err).to.be.null;
                return done();
            });
        });
    });

    describe('email', function() {
        it('cant be empty', function(done) {
            validUser.email = '';
            var u = new User(validUser);
            u.validate(function(err) {
                expect(err).to.not.be.null;
                expect(err.errors.email).to.exist;
                return done();
            });
        });

        it('must be a valid email', function(done) {
            validUser.email = 'test.test.de';
            var u = new User(validUser);
            u.validate(function(err) {
                expect(err).to.not.be.null;
                expect(err.errors.email).to.exist;
                validUser.email = 'test@test.de';
                var u = new User(validUser);
                u.validate(function(err) {
                    expect(err).to.be.null;
                    return done();
                });
            });
        });
    });

    describe('password', function () {
        it('should be hashed when saved', function(done) {
            var u = new User(validUser);
            u.save(function() {
                expect(u.password).to.be.not.equal(validUser.password);
                return done();
            });
        });
    });
});