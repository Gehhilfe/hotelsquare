const Util = require('../lib/util');
const mongoose = require('mongoose');
const chai = require('chai');
const expect = chai.expect;


describe('util', function () {
    describe('connectDatabase', () => {
        it('should connect to a database', (done) => {
            Util.connectDatabase(mongoose).then(() => {
                expect(mongoose.connection.db).to.not.be.undefined;
                return done();
            });
        });

        it('should connect to a database only once', (done) => {
            Util.connectDatabase(mongoose).then(() => {
                var db = mongoose.connection.db;
                Util.connectDatabase(mongoose).then(() => {
                    expect(mongoose.connection.db).to.be.equal(db);
                    return done();
                });
            });
        });
    });
});