'use strict';

const mongoose = require('mongoose');
const GeocodeResult = require('../app/models/geocoderesult');
const Util = require('../lib/util');

const chai = require('chai');
chai.should();


const mochaAsync = (fn) => {
    return (done) => {
        fn.call().then(done, (err) => {
            return done(err);
        });
    };
};

describe('geocoderesult', () => {

    beforeEach(mochaAsync(async () => {
        mongoose.Promise = global.Promise;

        await Util.connectDatabase(mongoose);
        await GeocodeResult.remove({});
    }));

    describe('keyword', () => {
        it('should be lowercase when saved', mochaAsync(async () => {
            const result = await GeocodeResult.create({
                keyword: 'UPPERCASE'
            });
            result.keyword.should.be.equal('UPPERCASE'.toLowerCase());
        }));
    });
});