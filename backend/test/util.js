'use strict';

const Util = require('../lib/util');
const mongoose = require('mongoose');
const chai = require('chai');
const expect = chai.expect;
const chaiAsPromised = require('chai-as-promised');
chai.use(chaiAsPromised);

const User = require('../app/models/user');

describe('util', function () {

    describe('bootstrap', () => {
        beforeEach((done) => {
            mongoose.Promise = global.Promise;

            if (mongoose.connection.db) return done();
            Util.connectDatabase(mongoose).then(function () {
                User.remove({}, () => {
                    User.ensureIndexes(done);
                });
            });
        });

        it('should reject on null', () => {
            return expect(Util.bootstrap(User, null)).eventually.rejected;
        });

        it('should reject on undefined', () => {
            return expect(Util.bootstrap(User, undefined)).eventually.rejected;
        });

        it('should reject on non array', () => {
            return expect(Util.bootstrap(User, {a: 'b'})).eventually.rejected;
        });

        it('should resolve', () => {
            return expect(Util.bootstrap(User, [
                {
                    name: 'testetstsetste',
                    email: 'test@tttttt.de',
                    password: 'admin1'
                }
            ])).eventually.fulfilled;
        });
    });

    describe('connectDatabase', () => {
        it('should connect to a database', (done) => {
            Util.connectDatabase(mongoose).then(() => {
                expect(mongoose.connection.db).to.not.be.undefined;
                return done();
            });
        });

        it('should connect to a database only once', (done) => {
            Util.connectDatabase(mongoose).then(() => {
                const db = mongoose.connection.db;
                Util.connectDatabase(mongoose).then(() => {
                    expect(mongoose.connection.db).to.be.equal(db);
                    return done();
                });
            });
        });
    });
});