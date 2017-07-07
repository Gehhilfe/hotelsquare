'use strict';

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
    utc_offset: Number,
    website: String,
    rating_google: Number,
    comments: [{
        author: {
            type: Schema.Types.ObjectId,
            ref: 'User'
        },
        text: String,
        likes: {
            type: Number,
            default: 0
        },
        dislikes: {
            type: Number,
            default: 0
        },
        date: {
            type: Date,
            default: Date.now()
        },
        isimage: {
            type: Boolean,
            default: false
        },
        imagename: {
            type: String,
            default: ''
        }
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
            types: this.types
        };
    }
}

VenueSchema.loadClass(VenueClass);
module.exports = mongoose.model('Venue', VenueSchema);