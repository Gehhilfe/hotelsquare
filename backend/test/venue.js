'use strict';

const mongoose = require('mongoose');
const User = require('../app/models/user');
const Venue = require('../app/models/venue');
const Util = require('../lib/util');

const chai = require('chai');
chai.use(require('chai-things'));
chai.should();

const mochaAsync = (fn) => {
    return (done) => {
        fn.call().then(done, (err) => {
            return done(err);
        });
    };
};

describe('venue', () => {
    let aVenue, bVenue;
    let user;

    beforeEach(mochaAsync(async () => {
        mongoose.Promise = global.Promise;

        await Util.connectDatabase(mongoose);
        await Promise.all([
            Venue.remove({}),
            User.remove({})
        ]);

        aVenue = await Venue.create({
            name: 'aVenue',
            utc_offset: 120,
            location: {
                type: 'Point',
                coordinates: [5, 5]
            },
            opening_hours: {
                periods: [{
                    close: {
                        day: 0,
                        time: '1204'
                    },
                    open: {
                        day: 0,
                        time: '0645'
                    }
                }, {
                    close: {
                        day: 1,
                        time: '0204'
                    },
                    open: {
                        day: 0,
                        time: '2245'
                    }
                }]
            }
        });

        bVenue = await Venue.create({
            name: 'bVenue',
            location: {
                type: 'Point',
                coordinates: [-5, -5]
            }
        });

        user = await User.create({name: 'peter', email: 'peter1@cool.de', password: 'peter99'});
    }));

    /**describe('opening times', () => {
        it('should be closed at day 0 at 1300', () => {
            const date = new Date(2017, 6, 9, 13, 0, 0, 0);
            aVenue.isOpen(date).should.be.false;
        });
        it('should be open at day 0 at 0815', () => {
            const date = new Date(2017, 6, 9, 8, 15, 0, 0);
            aVenue.isOpen(date).should.be.true;
        });
        it('should be open at day 1 at 0045', () => {
            const date = new Date(2017, 6, 10, 0, 45, 0, 0);
            aVenue.isOpen(date).should.be.true;
        });
        it('should be closed at day 0 at 0300', () => {
            const date = new Date(2017, 6, 9, 3, 0, 0, 0);
            aVenue.isOpen(date).should.be.false;
        });
        it('should be closed at day 1 at 1803', () => {
            const date = new Date(2017, 6, 9, 18, 3, 0, 0);
            aVenue.isOpen(date).should.be.false;
        });
        it('should be open at day 0 at 2345', () => {
            const date = new Date(2017, 6, 9, 23, 45, 0, 0);
            aVenue.isOpen(date).should.be.true;
        });
    });*/

    describe('location', () => {
        it('should find veneu a', (done) => {
            Venue.geoNear(aVenue.location.coordinates, {maxDistance: 0.01, spherical: true}).then((v) => {
                v[0].obj.name.should.be.equal(aVenue.name);
                return done();
            });
        });

        it('should find veneu b', (done) => {
            Venue.geoNear(bVenue.location.coordinates, {maxDistance: 0.01, spherical: true}).then((v) => {
                v[0].obj.name.should.be.equal(bVenue.name);
                return done();
            });
        });

        it('should find both with same distance', (done) => {
            Venue.geoNear([0, 0], {maxDistance: 5, spherical: true}).then((v) => {
                v[0].dis.should.be.equal(v[1].dis);
                return done();
            });
        });
    });

    describe('check_in', () => {
        it('should add user', () => {
            aVenue.checkIn(user);
            aVenue.check_ins.should.contain.a.thing.with.property('user', user);
        });

        it('should add user only once', () => {
            aVenue.checkIn(user);
            aVenue.checkIn(user);
            aVenue.check_ins.length.should.be.equal(1);
        });
    });
});