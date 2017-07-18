'use strict';

const _ = require('lodash');
const restify = require('restify');
const mongoose = require('mongoose');
const Schema = mongoose.Schema;
const googleapilib = require('googleplaces');
const config = require('config');

const Image = require('./image');

// ---------------------------------------------------------------------------------------------------------------------
// Schema
// ---------------------------------------------------------------------------------------------------------------------

const VenueSchema = new Schema({
    name: String,
    place_id: String,
    reference: String,
    photo_reference: String,
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
    vicinity: String,
    formattedAddress: String,
    phone_number: Number,
    icon_url: String,
    rating_google: Number,
    comments: [{
        kind: String,
        item: {
            type: Schema.Types.ObjectId,
            refPath: 'comments.kind'
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

    /**
     * Return open venues
     *
     * @param {Date} datenow current date
     * @returns {bool} flag if venue is open
     */
    isOpen(datenow){
        const opening_periods = this.opening_hours.periods;
        if(!datenow){
            datenow = new Date();
        }
        const currentDayAtVenue = datenow.getUTCDay();
        const currentTimeAtVenue = (datenow.getUTCHours() + Math.floor(this.utc_offset/60))*100 + datenow.getUTCMinutes() + this.utc_offset%60;
        let isOpen = false;

        for(let i = 0, len = opening_periods.length; i < len; i++){
            const opentime = parseInt(opening_periods[i].open.time, 10);
            if(currentDayAtVenue === opening_periods[i].open.day && currentTimeAtVenue > opentime){
                if(opening_periods[i].close){
                    if(currentDayAtVenue === opening_periods[i].close.day){
                        const closetime = parseInt(opening_periods[i].close.time, 10);
                        if(closetime > currentTimeAtVenue){
                            isOpen = true;
                        }
                    } else {
                        isOpen = true;
                    }
                } else {
                    isOpen = true;
                }
            }
        }
        return isOpen;
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
        let checkin = _.find(this.check_ins, (v, i) => {
            if (v.user.equals(user._id)) {
                index = i;
                return true;
            } else
                return false;
        });

        // Create new checkin when non found
        if (!checkin) {
            checkin = {
                user: user,
                count: 0
            };
        }

        // Increment counter and reset last visit date
        checkin.count += 1;
        checkin.last = Date.now();

        // Update element in collection
        if (index === -1)
            this.check_ins.push(checkin);
        else
            this.check_ins[index] = checkin;

        return {
            user: user._id,
            count: checkin.count,
            last: checkin.last
        };
    }

    _loadPhotoURLGoogle() {
        const client = restify.createClient({
            url: 'https://maps.googleapis.com'
        });
        return new Promise((resolve, reject) => {
            client.get('/maps/api/place/photo?maxheight=1600&photoreference='
                + this.photo_reference + '&key='
                + config.googleapi.GOOGLE_PLACES_API_KEY, function (err, req) {
                if (err)
                    return reject(err);

                req.on('result', function (err, res) {
                    if (err)
                        return reject(err);

                    let body = '';
                    let buffer = Buffer.alloc(0);
                    resolve(res.headers.location);
                    res.on('data', function (chunk) {
                        buffer = Buffer.concat([buffer, chunk]);
                        body += chunk;
                    });

                    res.on('end', function () {
                        resolve(buffer);
                    });

                    res.on('error', (e) => {
                        reject(e);
                    });
                });
            });
        });
    }

    async _loadPhotoGoogle() {
        const url = await this._loadPhotoURLGoogle();
        const client = restify.createClient({
            url: url
        });
        return new Promise((resolve, reject) => {
            client.get('', function (err, req) {
                if (err)
                    return reject(err);

                req.on('result', function (err, res) {
                    if (err)
                        return reject(err);

                    let buffer = Buffer.alloc(0);
                    res.on('data', function (chunk) {
                        buffer = Buffer.concat([buffer, chunk]);
                    });

                    res.on('end', function () {
                        resolve(buffer);
                    });

                    res.on('error', (e) => {
                        reject(e);
                    });
                });
            });
        });
    }

    async loadDetails() {
        const details = await this._getPlaceDetails(this.place_id);
        if (this.photo_reference !== '') {
            const buffer = await this._loadPhotoGoogle();
            const img = await Image.upload(buffer, null, this);
            this.images.push(img);
        }

        this.opening_hours = details.result.opening_hours;
        this.utc_offset = details.result.utc_offset;
        this.website = details.result.website;
        this.phone_number = details.result.phone_number;
        this.icon_url = details.result.icon;
        this.vicinity = details.result.vicinity;
        this.formatted_address = details.result.formatted_address;
        this.details_loaded = true;
    }

    addComment(comment) {
        if (!comment.assigned.to.equals(this._id))
            return;
        if (_.indexOf(this.comments, comment._id) === -1) {
            this.comments.push({
                item: comment,
                kind: comment.constructor.modelName
            });
        }
    }

    toJSONSearchResult() {
        const images = (this.images && this.images.length > 0) ? [{_id: _.first(this.images)}] : [];
        return {
            _id: this._id,
            name: this.name,
            location: this.location,
            types: this.types,
            images: images,
            check_ins_count: _.reduce(this.check_ins, (res, val) => res += val.count, 0),
            is_open: this.isOpen(),
            formatted_address: this.formatted_address
        };
    }

    toJSONDetails() {
        const images = (this.images && this.images.length > 0) ? _.map(this.images, (it) => {
            return {_id: it};
        }) : [];
        return {
            _id: this._id,
            name: this.name,
            location: this.location,
            types: this.types,
            images: images,
            last_check_ins: _.take(_.sortBy(this.check_ins, 'last'), 5),
            top_check_ins: _.take(_.sortBy(this.check_ins, 'count'), 5),
            check_ins_count: _.reduce(this.check_ins, (res, val) => res += val.count, 0),
            opening_hours: this.opening_hours,
            is_open: this.isOpen(),
            website: this.website,
            phone_number: this.phone_number,
            vicinity: this.vicinity,
            formatted_address: this.formatted_address,
            utc_offset: this.utc_offset
        };
    }
}

VenueSchema.loadClass(VenueClass);
module.exports = mongoose.model('Venue', VenueSchema);