'use strict';

const mock = require('mock-require');
const bcrypt = require('bcrypt');
let failBcrypt = false;

mock('bcrypt', {
    hash: function (password, salt_work) {
        if (failBcrypt)
            return Promise.reject('Cant hash password!');
        else
            return bcrypt.hash(password, salt_work);
    },
    compare: bcrypt.compare
});

// Need to reload User model with mocked bcrypt
const mongoose = require('mongoose');
mongoose.models = {};
mongoose.modelSchemas = {};
const User = mock.reRequire('../app/models/user');

const chai = require('chai');

const expect = chai.expect;

const chaiAsPromised = require('chai-as-promised');
chai.use(chaiAsPromised);
chai.use(require('chai-date'));
chai.should();

const Util = require('../lib/util');

describe('user', function () {


    let validUser;
    let aUser;
    let bUser;

    beforeEach(function (done) {
        failBcrypt = false;
        mongoose.Promise = global.Promise;

        validUser = {
            name: 'Test',
            email: 'test@test.de',
            password: 'password'
        };

        const otherUser = {
            name: 'OtherTest',
            email: 'test@test.de',
            password: 'password'
        };

        Util.connectDatabase(mongoose).then(() => {
            User.remove({}).then(() => {
                User.create(validUser).then((u) => {
                    aUser = u;
                    User.create(otherUser).then((u) => {
                        bUser = u;
                        return done();
                    });
                });
            });
        });
    });

    describe('#name', () => {
        it('cant be empty', function (done) {
            validUser.name = '';
            const u = new User(validUser);

            u.validate(function (err) {
                expect(err).to.not.be.null;
                expect(err.errors.name).to.exist;
                done();
            });
        });

        it('with 3 characters is invalid', function (done) {
            validUser.name = 'a'.repeat(3);
            const u = new User(validUser);

            u.validate(function (err) {
                expect(err).to.not.be.null;
                expect(err.errors.name).to.exist;
                return done();
            });
        });

        it('with 4 characters is valid', function (done) {
            validUser.name = 'a'.repeat(4);
            const u = new User(validUser);

            u.validate(function (err) {
                expect(err).to.be.null;
                return done();
            });
        });

        it('should be unqiue', function () {
            return expect(User.create(validUser)).to.be.eventually.rejected;
        });

        const format_tests = [
            {name: 'test', result: true},
            {name: 'test123', result: true},
            {name: 'test-123', result: true},
            {name: 'test#', result: false},
            {name: '1test', result: false},
            {name: 'a', result: false}
        ];

        it('should match the examples format results', (done) => {
            for (let i = 0; i < format_tests.length; i++) {
                const example = format_tests[i];
                validUser.name = example.name;
                const u = new User(validUser);
                u.validate(function (err) {
                    if (example.result) {
                        expect(err).to.be.null;
                    } else {
                        expect(err).to.not.be.null;
                        expect(err.errors.name).to.exist;
                    }
                    if (i === format_tests.length - 1)
                        return done();
                });
            }
        });
    });

    describe('#email', () => {
        it('cant be empty', function (done) {
            validUser.email = '';
            const u = new User(validUser);
            u.validate(function (err) {
                expect(err).to.not.be.null;
                expect(err.errors.email).to.exist;
                return done();
            });
        });

        it('must be a valid email', function (done) {
            validUser.email = 'test.test.de';
            const u = new User(validUser);
            u.validate(function (err) {
                expect(err).to.not.be.null;
                expect(err.errors.email).to.exist;
                validUser.email = 'test@test.de';
                const u = new User(validUser);
                u.validate(function (err) {
                    expect(err).to.be.null;
                    return done();
                });
            });
        });
    });

    describe('#password', () => {

        it('should have a minimum length of 6 characters', function (done) {
            const u = new User(validUser);
            u.password = '12345';

            u.validate(function (err) {
                expect(err).to.not.be.null;
                expect(err.errors.password).to.exist;
                u.password = '123456';
                u.validate(function (err) {
                    expect(err).to.be.null;
                    return done();
                });
            });
        });

        it('should stay valid when not changed', function (done) {
            aUser.name = 'Blubbbb';
            aUser.validate().then(done);
        });

        it('should be hashed when saved', function () {
            expect(aUser.password).to.be.not.equal(validUser.password);
        });

        it('should not change when already hashed and saved again', (done) => {
            aUser.save().then((res) => {
                const hashed_password = res.password;
                res.save().then((res) => {
                    expect(hashed_password).to.be.equal(res.password);
                    return done();
                });
            });
        });

        it('should not be converted into json', function () {
            const u = new User(validUser);
            expect(u.toJSON().password).to.be.undefined;
        });

        it('should not save object if password cant be hashed', () => {
            failBcrypt = true;
            const u = new User(validUser);
            return expect(u.save()).to.eventually.rejected;
        });
    });

    describe('#friendRequests', () => {
        it('should store name of sender', (done) => {
            aUser.friendRequests.push(bUser);
            expect(aUser.friendRequests[0].name).to.be.equal(bUser.name);
            console.log(aUser.friendRequests[0]);
            aUser.friendRequests[0].created_at.should.be.today;
            return done();
        });
    });

    describe('comparePassword', () => {
        it('compare should yield a true result with correct plain text password', function (done) {
            aUser.comparePassword(validUser.password).then(function (res) {
                expect(res).to.be.true;
                return done();
            });
        });

        it('compare should yield a false result with wrong plain text password', function (done) {
            aUser.comparePassword('haxor').then(function (res) {
                expect(res).to.be.false;
                return done();
            });
        });
    });

    describe('public json', () => {
        it('should not contain password', () => {
            const json = aUser.toJSONPublic();
            json.should.not.have.property('password');
        });

        it('should not contain email', () => {
            const json = aUser.toJSONPublic();
            json.should.not.have.property('email');
        });

        it('should not contain friends', () => {
            const json = aUser.toJSONPublic();
            json.should.not.have.property('friends');
        });

        it('should not contain friendRequests', () => {
            const json = aUser.toJSONPublic();
            json.should.not.have.property('friendRequests');
        });

        it('should contain name', () => {
            const json = aUser.toJSONPublic();
            json.should.have.property('name');
        });
    });

    describe('login', function () {
        beforeEach(function (done) {

            validUser = new User({
                name: 'Test',
                email: 'test@test.de',
                password: 'password'
            });
            User.remove({}, function () {
                validUser.save(done);
            });
        });

        it('should return the user with valid name and password', function () {
            return expect(User.login(validUser.name, 'password').then(function (u) {
                return Promise.resolve(u.equals(validUser));
            })).to.eventually.equal(true);
        });

        it('should return the user with valid emai and password', function () {
            return expect(User.login(validUser.email, 'password').then(function (u) {
                return Promise.resolve(u.equals(validUser));
            })).to.eventually.equal(true);
        });

        it('should reject with invalid password', function () {
            return expect(User.login(validUser.name, 'wrong')).to.eventually.rejected;
        });

        it('should reject with invalid name', function () {
            return expect(User.login('test', 'password')).to.eventually.rejected;
        });

        it('should work with a json object as first parameter', () => {
            return expect(User.login({name: 'test', password: 'password'})).to.eventually.resolved;
        });
    });
});