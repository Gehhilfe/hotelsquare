'use strict';

const _ = require('lodash');
const restify = require('restify');
const mongoose = require('mongoose');
const Schema = mongoose.Schema;
const googleapilib = require('googleplaces');
const config = require('config');

const foursquare = require('node-foursquare')(config.foursquare);

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
    price: {
        type: Number,
        default: 0
    },
    foursquare_id: String,
    opening_hours: {
        periods: [{
            close: [{
                day: Number,
                time: String
            }],
            open: [{
                day: Number,
                time: String
            }]
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
        },
        created_at: Date
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

    _foursquareMatchSearch() {
        const self = this;
        return new Promise((resolve, reject) => {
            foursquare.Venues.search(self.location.coordinates[1], self.location.coordinates[0], null, {
                intent: 'match',
                query: self.name
            }, null, (err, res) => {
                if (err)
                    return reject(err);
                return resolve(res);
            });
        });
    }

    _foursquareDetails() {
        const self = this;
        return new Promise((resolve, reject) => {
            foursquare.Venues.getVenue(self.foursquare_id, null, (err, res) => {
                if(err)
                    return reject(err);
                return resolve(res.venue);
            });
        });
    }

    async _getFoursquareDetails() {
        const self = this;
        const searchResult = await this._foursquareMatchSearch();
        if (!searchResult.venues || searchResult.venues.length == 0)
            return;
        const v = searchResult.venues[0];
        this.foursquare_id = v.id;
        const details = await this._foursquareDetails();
        if(details.tags)
            this.types = _.uniq(_.concat(this.types, details.tags));
        if(details.price)
            this.price = details.price.tier;
        if(details.photos && details.photos.count !== 0) {
            await Promise.all(_.map(details.photos.groups[0].items, async (it) => {
                const buffer = await this._loadPhotoFoursquare(it);
                const img = await Image.upload(buffer, null, self);
                self.images.push(img);
            }));
        }
    }

    /**
     * Return open venues
     *
     * @param {Date} datenow current date
     * @returns {bool} flag if venue is open
     */
    isOpen(datenow) {
        if (!this.opening_hours || !this.opening_hours.periods)
            return null;
        const opening_periods = this.opening_hours.periods;
        if (!datenow) {
            datenow = new Date();
        }

        //flag if venue is open
        let isOpen = false;

        //open and close to minutes from day 0 time 0000 -> periods in ints festlegen (independend of day) -> check if inside
        //periods in minutes from day 0 time 0000 where it is open
        const opentimes = [];
        const closetimes = [];

        for (let i = 0, len = opening_periods.length; i < len; i++) {
            const day = opening_periods[i].open.length > 0 ? opening_periods[i].open[0].day : opening_periods[i].close[0].day;
            const days_from_zero_in_minutes = day * 24 * 60;
            for (let j = 0, len = opening_periods[i].open.length; j < len; j++) {
                const opentime = parseInt(opening_periods[i].open[j].time, 10);
                const opentime_in_minutes = Math.floor(opentime / 100) * 60 + opentime % 100;
                const totaltime_since_day_zero_open = days_from_zero_in_minutes + opentime_in_minutes;
                opentimes.push(totaltime_since_day_zero_open);
            }
            for (let j = 0, len = opening_periods[i].close.length; j < len; j++) {
                const closetime = parseInt(opening_periods[i].close[j].time, 10);
                const closetime_in_minutes = Math.floor(closetime / 100) * 60 + closetime % 100;
                const totaltime_since_day_zero_close = days_from_zero_in_minutes + closetime_in_minutes;
                closetimes.push(totaltime_since_day_zero_close);
            }
        }

        //current time in minutes from day zero
        const currentDayAtVenue = datenow.getUTCDay();
        const currentTimeAtVenueInMinutes = currentDayAtVenue * 24 * 60 + this.utc_offset + datenow.getUTCHours() * 60 + datenow.getUTCMinutes() + this.utc_offset % 60;

        //for current time find closest opening time prior to now
        let last_open_time = opentimes[0];
        let max_open_so_far;
        let index = 0;
        for (let i = 1, len = opentimes.length; i < len; i++) {
            max_open_so_far = opentimes[i];
            if (currentTimeAtVenueInMinutes < max_open_so_far) {
                break;
            }
            index++;
            last_open_time = max_open_so_far;
        }

        //find closest closing time to last_open_time
        let closest_close;
        if (closetimes.length > index) {
            closest_close = closetimes[index];
            if (currentTimeAtVenueInMinutes > last_open_time && currentTimeAtVenueInMinutes < closest_close) {
                isOpen = true;
            }
        } else {
            isOpen = true;
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

    _loadPhotoFoursquare(photo) {
        const client = restify.createClient({
            url: photo.prefix + 'original' + photo.suffix
        });
        return new Promise((resolve, reject) => {
            client.get('', (err, req) => {
                if (err)
                    return reject(err);

                req.on('result', function (err, res) {
                    if (err)
                        return reject(err);

                    let body = '';
                    let buffer = Buffer.alloc(0);
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
        await this._getFoursquareDetails();
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
        if (comment.constructor.modelName === 'ImageComment') {
            this.images.push(comment.image);
        }
        if (_.indexOf(this.comments, comment._id) === -1) {
            this.comments.push({
                item: comment,
                kind: comment.constructor.modelName,
                created_at: Date.now()
            });
            this.comments = _.reverse(_.sortBy(this.comments, 'created_at'));
        }
    }

    toJSONSearchResult() {
        return {
            _id: this._id,
            name: this.name,
            location: this.location,
            types: this.types,
            images: this.images,
            check_ins_count: _.reduce(this.check_ins, (res, val) => res += val.count, 0),
            is_open: this.isOpen(),
            rating: this.rating_google,
            formatted_address: this.formatted_address,
            price: this.price
        };
    }

    toJSONDetails() {

        return {
            _id: this._id,
            name: this.name,
            location: this.location,
            types: this.types,
            images: this.images,
            last_check_ins: _.take(_.sortBy(this.check_ins, 'last'), 5),
            top_check_ins: _.take(_.sortBy(this.check_ins, 'count'), 5),
            check_ins_count: _.reduce(this.check_ins, (res, val) => res += val.count, 0),
            opening_hours: this.opening_hours,
            is_open: this.isOpen(),
            website: this.website,
            phone_number: this.phone_number,
            vicinity: this.vicinity,
            rating: this.rating_google,
            formatted_address: this.formatted_address,
            utc_offset: this.utc_offset,
            price: this.price
        };
    }
}

VenueSchema.loadClass(VenueClass);
module.exports = mongoose.model('Venue', VenueSchema);