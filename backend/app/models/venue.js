'use strict';

const _ = require('lodash');
const mongoose = require('mongoose');
const Schema = mongoose.Schema;
const googleapilib = require('googleplaces');
const config = require('config');

// ---------------------------------------------------------------------------------------------------------------------
// Schema
// ---------------------------------------------------------------------------------------------------------------------

const VenueSchema = new Schema({
    name: String,
    place_id: String,
    reference: String,
    types: [String],
    location: {
        'type': {type: String, default: 'Point'},
        coordinates: {type: [Number], default: [0, 0]}
    },
    images: [{
        type: Schema.Types.ObjectId,
        ref: 'Image'
    }],
    details_loaded: {
        type: Boolean,
        default: false
    },
    opening_hours: {
        periods: [{
            close: {
                day: Number,
                time: String
            },
            open: {
                day: Number,
                time: String
            }
        }]
    },
    check_ins: [{
        user: {
            type: Schema.Types.ObjectId,
            ref: 'User'
        },
        count: {
            type: Number,
            default: 0
        },
        last: {
            type: Date,
            default: Date.now()
        }
    }],
    utc_offset: Number,
    website: String,
    rating_google: Number,
    comments: [{
        type: Schema.Types.ObjectId,
        ref: 'VenueComment'
    }]
});


VenueSchema.index({location: '2dsphere'});

// ---------------------------------------------------------------------------------------------------------------------
// Class
// ---------------------------------------------------------------------------------------------------------------------

class VenueClass {

    _getPlaceDetails(place_id) {
        const api = googleapilib(config.googleapi.GOOGLE_PLACES_API_KEY, config.googleapi.GOOGLE_PLACES_OUTPUT_FORMAT);
        return new Promise((resolve, reject) => {
            api.placeDetailsRequest({
                placeid: place_id
            }, (err, details) => {
                if (err)
                    reject(err);
                else
                    resolve(details);
            });
        });
    }

    /**
     * Check ins user
     *
     * @param {User} user to check in
     * @returns {undefined}
     */
    checkIn(user) {
        // Search for checkin
        let index = -1;
        let checkin = _.find(this.check_ins, (v, i) =>{
            if(v.user.equals(user._id)) {
                index = i;
                return true;
            } else
                return false;
        });

        // Create new checkin when non found
        if(!checkin) {
            checkin = {
                user: user,
                count: 0
            };
        }

        // Increment counter and reset last visit date
        checkin.count += 1;
        checkin.last = Date.now();

        // Update element in collection
        if(index === -1)
            this.check_ins.push(checkin);
        else
            this.check_ins[index] = checkin;

        return {
            user: user._id,
            count: checkin.count,
            last: checkin.last
        };
    }

    async loadDetails() {
        const details = await this._getPlaceDetails(this.place_id);
        this.opening_hours = details.result.opening_hours;
        this.utc_offest = details.result.utc_offset;
        this.details_loaded = true;
    }

    toJSONSearchResult() {
        return {
            _id: this._id,
            name: this.name,
            location: this.location,
            types: this.types,
            check_ins_count: _.reduce(this.check_ins, (res, val) => res += val.count, 0)
        };
    }

    toJSONDetails() {
        return {
            _id: this._id,
            name: this.name,
            location: this.location,
            types: this.types,
            last_check_ins: _.take(_.sortBy(this.check_ins, 'last'), 5),
            top_check_ins: _.take(_.sortBy(this.check_ins, 'count'), 5),
            check_ins_count: _.reduce(this.check_ins, (res, val) => res += val.count, 0),
            opening_hours: this.opening_hours
        };
    }
}

VenueSchema.loadClass(VenueClass);
module.exports = mongoose.model('Venue', VenueSchema);