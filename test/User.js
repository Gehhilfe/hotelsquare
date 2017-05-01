require('../models/User');

const mongoose = require('mongoose');
const chai = require('chai');
const expect = chai.expect;
const should = chai.should;
const chaiAsPromised = require("chai-as-promised");
chai.use(chaiAsPromised);

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
        mongoose.connect(Util.databaseURI()).then(function() { User.remove({}, done)});
    });

    it('can be saved', function(done) {
        var u = new User(validUser);
        u.save(done);
    });

    describe('#name', function() {
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

    describe('#email', function() {
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

    describe('#password', function () {

        it('should have a minimum length of 6 characters', function (done) {
            var u = new User(validUser);
            u.password = '12345';

            u.validate(function(err) {
                expect(err).to.not.be.null;
                expect(err.errors.password).to.exist;
                u.password = '123456';
                u.validate(function(err) {
                    expect(err).to.be.null;
                    return done();
                });
            });
        });

        it('should be hashed when saved', function(done) {
            var u = new User(validUser);
            u.save(function() {
                expect(u.password).to.be.not.equal(validUser.password);
                return done();
            });
        });

        it('compare should yield a true result with correct plain text password', function(done) {
            var u = new User(validUser);
            u.save().then(function() {
               u.comparePassword(validUser.password).then(function(res) {
                   expect(res).to.be.true;
                   return done();
               });
            });
        });

        it('compare should yield a false result with wrong plain text password', function(done) {
            var u = new User(validUser);
            u.save().then(function() {
                u.comparePassword('haxor').then(function(res) {
                    expect(res).to.be.false;
                    return done();
                });
            });
        });
    });

    describe('login', function() {
        beforeEach(function(done) {

            validUser = new User({
                name: 'Test',
                email: 'test@test.de',
                password: 'password'
            });
            User.remove({}, function() {
                validUser.save(done);
            });
        });

        it('should return the user with valid name and password', function() {
            return expect(User.login(validUser.name, 'password').then(function(u) {
                return Promise.resolve(u.equals(validUser))
            })).to.eventually.equal(true);
        });

        it('should reject with invalid password', function () {
            return expect(User.login(validUser.name, 'wrong')).to.eventually.rejected;
        });

        it('should reject with invalid name', function () {
            return expect(User.login('test', 'password')).to.eventually.rejected;
        });
    });
});