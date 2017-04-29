require('../models/User');

const mongoose = require('mongoose');
const expect = require('chai').expect;
const config = require('config');
const User = mongoose.model('User');

describe('user', function() {


    var validUser;

    beforeEach(function(done) {
        validUser = new User({
            name: 'Test',
            email: 'test@test.de',
            password: 'password'
        });

        if (mongoose.connection.db) return done();
        mongoose.promise = global.promise;
        mongoose.connect('mongodb://' + config.get('db.host') + '/' + config.get('db.name')).then(() => User.remove({}, done));
    });

    it('can be saved', function(done) {
        var u = new User({
            name: 'Test',
            email: 'test@test.de',
            password: 'password'
        });
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
    });
});