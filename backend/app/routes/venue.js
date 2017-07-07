'use strict';

const _ = require('lodash');
const restify = require('restify');
const Venue = require('../models/venue');
const googleapilib = require('googleplaces');
const NodeGeocoder = require('node-geocoder');
const config = require('config');
const SearchRequest = require('../models/searchrequest');
const GeocodeResult = require('../models/geocoderesult');
const minio = require('minio');
const VenueImages = require('../models/venueimages');
const User = require('../models/user');
const sharp = require('sharp');

const bucket_name_venues = 'venue_images';

const minioClient = new minio.Client({
    endPoint: 'stimi.ovh',
    port: 9000,
    secure: false,
    accessKey: config.minio.key,
    secretKey: config.minio.secret
});

/**
 * Get details for a given venue id
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function getVenue(request, response, next) {
    const venue = await Venue.findOne({_id: request.params.id});

    // check if details are loaded
    if(!venue.details_loaded) {
        await venue.loadDetails();
        await venue.save();
    }

    response.send(venue);
    return next();
}

/**
 * Uploads an image for a venue to the database
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function putImage(request, response, next) {
}

/**
 * Deletes a stored image from the database in case the requesting user is the creator of the image
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function delImage(request, response, next) {
}

/**
 * Retrieves a list with all image names of a venue to be queried later on
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function getImageNames(request, response, next) {
}

/**
 * Retrieves stored images for a venue
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function getImage(request, response, next) {
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

    return Venue.create({
        name: entry.name,
        place_id: entry.place_id,
        reference: entry.reference,
        types: entry.types,
        rating_google: entry.rating,
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
            throw new restify.errors.BadRequestError({
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
    let page = 0;
    if (request.params.page)
        page = request.params.page;

    if (!radius) {
        radius = 5000;
    } else {
        radius = Math.min(5000, Math.max(1000, radius));
    }
    const keyword = request.body.keyword;

    if(keyword.length < 3) {
        return next(new restify.errors.BadRequestError({
            field: 'keyword',
            message: 'The search keyword needs to be at least 3 characters'
        }));
    }

    if (locationName) {
        // Check if result cached
        const result = await getLocationForName(locationName);

        location = {
            type: 'Point',
            coordinates: [result[0].longitude, result[0].latitude]
        };

        locationName = result[0].formattedAddress;
    }


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

    //search in our database for query
    let venues = await searchVenuesInDB(location, keyword, radius, page, 10);
    venues = _.map(venues, (v) => v.toJSONSearchResult());
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
 * @param {Number} page result page
 * @param {Number} limit  number of results on page
 * @returns {Promise.<*>} result
 */
function searchVenuesInDB(location, keyword = '', radius = 5000, page = 0, limit = 20) {
    const query = Venue.find({
        $or: [
            {name: new RegExp(keyword, 'i')},
            {types: new RegExp(keyword, 'i')}
        ]
    }).limit(limit).skip(page * limit);
    return query.where('location').near({
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
 * adds a like to a comment of a venue
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function like(request, response, next) {
    const venue = await Venue.findOne({place_id: request.body.venueid});
    if (venue) {
        const comment = await venue.comments.find({text: request.body.comment});
        comment.likes += 1;
        venue.comments = comment;
        await venue.save();
        response.json({message: 'likes: ' + comment.likes});
        return next();
    }
    response.send(404, 'venue could not be found');
    return next();
}

/**
 * adds a dislike to a comment of a venue
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function dislike(request, response, next) {
    Venue.findOne({place_id: request.body.venueid}, (err, obj) => {
        if (err) {
            response.send(404, 'venue could not be found');
            return next();
        }
        const comment = obj.comments.find({text: request.body.comment});
        comment.dislikes += 1;
        obj.comments = comment;
        obj.save();
        response.json({message: 'dislikes: ' + comment.dislikes});
        return next();
    });
}

/**
 * adds a comment to a venue
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function addComment(request, response, next) {
    const venue = await Venue.findOne({_id: request.params.id});
    const user = await User.findOne({_id: request.authentication._id});
    const comment = {'author': user, 'text': request.body.comment, 'likes': 0, 'dislikes': 0, 'date': Date.now()};
    venue.comments.push(comment);
    await venue.save();
    response.send(venue.commments);
    return next();
}

/**
 * gets comments of a venue
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function getComments(request, response, next) {
    Venue.findOne({place_id: request.body.venueid}, (err, obj) => {
        if (err) {
            response.send(404, 'venue could not be found in database');
            return next();
        }
        response.json(obj.comments);
        return next();
    });
}

/**
 * deletes comment of a venue if the requesting person is the author
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function delComment(request, response, next) {
    Venue.findOne({place_id: request.body.venueid}, (err, venue) => {
        if (err) {
            response.send(404, 'venue could not be found in database');
            return next();
        }
        User.findOne({_id: request.authentication._id}, (err, obj) => {
            if (err) {
                response.send(404, 'user could not be found in database');
                return next();
            }
            const comment = obj.comments.find({text: request.body.comment});
            if (comment.author._id.equals(obj._id)) {
                obj.comments.pull({'text': request.body.comment});
                obj.save();
            }
            response.send(200, 'comment deleted');
            return next();
        });
    });
}

module.exports = {
    queryVenue,
    queryAllVenuesFromGoogle,
    searchVenuesInDB,
    getImage,
    getImageNames,
    delImage,
    putImage,
    like,
    dislike,
    addComment,
    getComments,
    delComment,
    getVenue
};

