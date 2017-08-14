'use strict';

const _ = require('lodash');
const config = require('config');
const restify = require('restify');
const restify_errors = require('restify-errors');
const Venue = require('../models/venue');
const googleapilib = require('googleplaces');
const NodeGeocoder = require('node-geocoder');
const SearchRequest = require('../models/searchrequest');
const GeocodeResult = require('../models/geocoderesult');
const User = require('../models/user');

/**
 * Get details for a given venue id
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function getVenue(request, response, next) {
    const venue = await Venue.findOne({_id: request.params.id})
        .populate('images');

    // check if details are loaded
    if (!venue.details_loaded) {
        await venue.loadDetails();
        await venue.save();
    }

    response.send(venue.toJSONDetails());
    return next();
}


/**
 * Imports/Updates a google result into our database
 * @param {Object} entry result entry
 * @returns {Promise.<void>} entry in out database
 */
async function importGoogleResult(entry) {
    // Check if venues is already existing
    const existing = await Venue.find({place_id: entry.place_id});

    if (existing.length > 0)
        return;

    const photo_reference = (entry.photos && entry.photos.length > 0) ? entry.photos[0].photo_reference : '';

    return Venue.create({
        name: entry.name,
        place_id: entry.place_id,
        reference: entry.reference,
        types: entry.types,
        rating_google: entry.rating,
        photo_reference: photo_reference,
        location: {
            type: 'Point',
            coordinates: [entry.geometry.location.lng, entry.geometry.location.lat]
        }
    });
}

/**
 * Resolve a formal location name into location coordinates
 *
 * @param {String} locationName formal name of location e.g. TU Darmstadt
 * @returns {Promise.<*>} Geocoding result
 */
async function getLocationForName(locationName) {
    // Check if result cached
    let result = await GeocodeResult.findByKeyword(locationName);
    if (!result) {
        // Get result from google
        const geocoder = NodeGeocoder({
            provider: 'google',
            httpAdapter: 'https',
            apiKey: config.googleapi.GOOGLE_PLACES_API_KEY,
            formatter: null
        });
        result = await geocoder.geocode(locationName);

        if (!result || result.length === 0) {
            throw new restify_errors.BadRequestError({
                message: 'No valid location for location name found.'
            });
        }
        // Cache result
        GeocodeResult.create({
            keyword: locationName,
            result: result
        });
    } else {
        result = result.result;
    }
    return result;
}


/**
 * Search query for venues
 *
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function queryVenue(request, response, next) {
    let location = request.body.location;
    let locationName = request.body.locationName;
    let radius = request.body.radius;
    let price = 0;
    let only_open = false;
    let page = 0;
    if (request.params.page)
        page = request.params.page;

    if (request.body.only_open) {
        only_open = request.body.only_open;
    }

    if (request.body.price) {
        price = request.body.price;
    }

    if (!radius) {
        radius = 25000;
    } else {
        radius = Math.min(25000, Math.max(1000, radius));
    }
    let keyword = request.body.keyword;

    if (keyword.length < 3) {
        return next(new restify_errors.BadRequestError({
            field: 'keyword',
            message: 'The search keyword needs to be at least 3 characters'
        }));
    }

    let keywords = _.split(keyword, ' ');

    keywords = _.map(keywords, (it) => _.get(config.keywords, it.toLowerCase(), it.toLowerCase()));

    keyword = _.join(keywords, ' ');

    if (locationName) {
        // Resolve name into location
        const result = await getLocationForName(locationName);

        location = {
            type: 'Point',
            coordinates: [result[0].longitude, result[0].latitude]
        };

        locationName = result[0].formatted_address;
    }

    // search in our database for query
    let venues = await searchVenuesInDB(location, keyword, radius, price, page, 10);
    venues = _.map(venues, (v) => v.toJSONSearchResult());
    if (only_open) {
        venues = _.filter(venues, (v) => v.isOpen());
    }
    response.send({
        location: location,
        locationName: locationName,
        radius: radius,
        page: page,
        limit: 10,
        results: venues
    });
    return next();
}


/**
 * Retrieves venues inside search radius filtered by keyword
 *
 * @param {Number[]} location center point
 * @param {string} keyword keyword
 * @param {Number} radius search radius
 * @param {Number} price minimum price level 0=all 5=highest
 * @param {Number} page result page
 * @param {Number} limit  number of results on page
 * @returns {Promise.<*>} result
 */
async function searchVenuesInDB(location, keyword = '', radius = 5000, price = 0, page = 0, limit = 20) {
    const closestSearch = await SearchRequest.findClosestLocation(location, keyword, 5000);

    if (closestSearch.length === 0) {
        //load all google results into database
        const googleResults = await queryAllVenuesFromGoogle(location, keyword);
        await Promise.all(_.map(googleResults, importGoogleResult));
        await SearchRequest.create({
            location: location,
            keyword: keyword
        });
    }

    const query = Venue.find({
        $or: [
            {name: new RegExp(keyword, 'i')},
            {types: new RegExp(keyword, 'i')}
        ],
        price: {
            $gte: price
        }
    }).populate('images').limit(limit).skip(page * limit);
    return await query.where('location').near({
        center: location,
        maxDistance: radius
    });
}

/**
 * Lookups all venues nearby location
 *
 * @param {Number[]} location Center of lookup radius
 * @param {string} keyword Search Keyword
 * @param {string} next_page_token token for next result page
 * @returns {Promise} Lookup result
 */
function queryAllVenuesFromGoogle(location, keyword, next_page_token = '') {
    const api = googleapilib(config.googleapi.GOOGLE_PLACES_API_KEY, config.googleapi.GOOGLE_PLACES_OUTPUT_FORMAT);

    const params = {
        location: [
            location.coordinates[1],
            location.coordinates[0]
        ],
        keyword: keyword,
        rankby: 'distance',
        language: 'en'
    };

    if (!_.isEmpty(next_page_token)) {
        params.pagetoken = next_page_token;
    }

    return new Promise((resolve, reject) => {
        api.nearBySearch(params, function (error, res) {
            if (error) {
                reject(error);
            } else {
                if (res.next_page_token && !_.isElement(res.next_page_token)) {
                    queryAllVenuesFromGoogle(location, keyword, res.next_page_token).then((recvResult) => {
                        resolve(_.concat(recvResult, res.results));
                    });
                } else {
                    resolve(res.results);
                }
            }
        });
    });
}

/**
 * Checkins authenticated user into venue given per parameter id
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function checkin(request, response, next) {
    const venue = await Venue.findOne({_id: request.params.id});
    response.send(venue.checkIn(request.authentication));
    await venue.save();
    return next();
}

module.exports = {
    queryVenue,
    queryAllVenuesFromGoogle,
    searchVenuesInDB,
    getVenue,
    checkin
};

