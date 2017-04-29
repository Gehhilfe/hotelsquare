
require('../models/User');

const mongoose = require('mongoose');
const expect = require('chai').expect;
const config = require('config');
const User = mongoose.model('User');

describe('user', function () {
    
    beforeEach(function(done) {
        if(mongoose.connection.db)  return done();
        
        mongoose.connect('mongodb://'+config.get('db.host')+'/'+config.get('db.name')).then(() => User.remove({},done));
    });
    
    it('can be saved', function(done) {
       var u = new User({
            name: 'Test',
            email: 'test@test.de',
            password: 'password'
       });
       u.save(done);
    });
    
    describe('name', function () {
        it('should be invalid if name is empty', function (done) {
            var u = new User();

            u.validate(function (err) {
                expect(err).to.not.be.null;
                expect(err.errors.name).to.exist;
                done();
            });
        });
    });
});