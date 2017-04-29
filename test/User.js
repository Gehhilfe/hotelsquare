/**
 * Created by gehhi on 29.04.2017.
 */


var mongoose = require('mongoose');
var expect = require('chai').expect;
var User = require('../models/User')(mongoose);

describe('user', function () {
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