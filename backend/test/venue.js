'use strict';

const mongoose = require('mongoose');
const Venue = require('../app/models/venue');
const Util = require('../lib/util');

const chai = require('chai');
chai.should();

describe('venue', () => {
    let aVenue, bVenue;

    beforeEach(async () => {
        mongoose.Promise = global.Promise;

        await Util.connectDatabase(mongoose);
        await Venue.remove({});

        aVenue = await Venue.create({
            name: 'aVenue',
            location: {
                type: 'Point',
                coordinates: [5, 5]
            }
        });

        bVenue = await Venue.create({
            name: 'bVenue',
            location: {
                type: 'Point',
                coordinates: [-5, -5]
            }
        });
    });

    describe('location', () => {
        it('should find veneu a', (done) => {
            Venue.geoNear(aVenue.location.coordinates, { maxDistance: 0.01, spherical: true }).then((v) => {
                v[0].obj.name.should.be.equal(aVenue.name);
                return done();
            });
        });

        it('should find veneu b', (done) => {
            Venue.geoNear(bVenue.location.coordinates, { maxDistance: 0.01, spherical: true }).then((v) => {
                v[0].obj.name.should.be.equal(bVenue.name);
                return done();
            });
        });

        it('should find both with same distance', (done) => {
            Venue.geoNear([0, 0], { maxDistance: 5, spherical: true }).then((v) => {
                v[0].dis.should.be.equal(v[1].dis);
                return done();
            });
        });
    });
});