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
 * Uploads an image for a venue to the database
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function putImage(request, response, next) {
    //Convert to jpeg
    const buffer = await sharp(request.files.venueimage.path)
        .resize(200, 200)
        .toFormat('jpeg')
        .toBuffer();

    minioClient.bucketExists(bucket_name_venues, function(err) {
        if(err){
            if(err.code === 'NoSuchBucket'){
                minioClient.makeBucket(bucket_name_venues, 'eu-west-1', function (err) {
                    if(err) {
                        response.send(500, 'new bucket could not be created');
                        return next();
                    }
                    const no = 0;
                    const imagename = 'venue_' + request.body.venueid + no.toString() + '.jpeg';
                    minioClient.putObject(bucket_name_venues, imagename, buffer, 'image/jpeg', (err, etag) => {
                        if(err){
                            response.send(500, err);
                            return next();
                        } else {
                            const user = User.findOne({_id: request.authentication._id});
                            VenueImages.create({
                                venueid: request.body.venueid,
                                imagename: imagename,
                                user: user
                            });
                            response.json(etag);
                            return next();
                        }
                    });
                });
            }
            if(err === null) {
                const itemsinbucket = minioClient.listObjects(bucket_name_venues, '', true).length();
                const imagename = 'venue_' + request.body.venueid + itemsinbucket.toString() + '.jpeg';
                minioClient.putObject(bucket_name_venues, imagename, buffer, 'image/jpeg', (err, etag) => {
                    if (err) {
                        response.send(500, err);
                        return next();
                    } else {
                        User.findOne({_id: request.authentication._id}, function (err, obj) {
                            VenueImages.create({
                                venueid: request.body.venueid,
                                imagename: imagename,
                                user: obj
                            });
                            response.json(etag);
                            return next();
                        });
                    }
                });
            }
            response.send(500, err);
            return next();
        }
        response.send(500, err);
        return next();
    });
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
    minioClient.bucketExists(bucket_name_venues, function(err) {
        if(err) {
            if (err.code === 'NoSuchBucket') {
                response.send('404', 'Bucket for ' + request.body.venueid + ' not found');
                return next();
            }
            if(err === null){
                VenueImages.findOne({ imagename: request.body.imagename}, function(err, obj) {
                    if(obj.name === request.authentication.name) {
                        minioClient.removeObject(bucket_name_venues, request.body.imagename, (err) => {
                            if (err) {
                                response.send('404', 'Image for ' + request.body.imagename + ' not found');
                            } else {
                                response.send();
                            }
                            return next();
                        });
                    }
                    response.send('400', 'Image not in database');
                    return next();
                });
            }
        }
        response.send(500, err);
        return next();
    });
}

/**
 * Retrieves a list with all image names of a venue to be queried later on
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function getImageNames(request, response, next){
    // When no name provided return error
    if (request.params.venueid === undefined){
        response.send(404, 'you must request a valid venueid');
        return next();
    }

    const allimageurls = [];
    const objectsstream = minioClient.listObjects(bucket_name_venues, '', true);
    objectsstream.on('data', function(obj) {
        const objectname = obj.name;
        allimageurls.put(objectname);
    });
    objectsstream.on('error', function(e){
        response.send(500, ' error requesting minio image list for venue ' + bucket_name_venues);
        return next();
    });
    objectsstream.on('end', function (e) {
        response.json(allimageurls);
        return next();
    });
    response.send(500, 'undefined internal error requesting image names of bucket ' + bucket_name_venues);
    return next();
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
    // When no name provided return error
    if (request.params.imagename === undefined){
        response.send(404, 'you must request a valid venueid');
        return next();
    }

    minioClient.statObject(bucket_name_venues, request.params.imagename, (err, stat) => {
        if (err)
            return next(new restify.errors.NotFoundError());
        else
            minioClient.presignedGetObject(bucket_name_venues, request.params.imagename, 30 * 60, (err, url) => {
                if (err) {
                    return next(new restify.errors.NotFoundError());
                } else {
                    response.redirect(url, next);
                }
            });
    });
}



/**
 * Imports/Updates a google result into our database
 * @param {Object} entry result entry
 * @returns {Promise.<void>} entry in out database
 */
async function importGoogleResult(entry) {
    // Check if venues is already existing
    const existing = await Venue.find({place_id: entry.place_id});

    if(existing.length > 0)
        return;

    return await Venue.create({
        name: entry.name,
        place_id: entry.place_id,
        reference: entry.reference,
        types: entry.types,
        location: {
            type: 'Point',
            coordinates: [entry.geometry.location.lng, entry.geometry.location.lat]
        }
    });
}


/**
 * queries for venues
 *
 * @function query
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function queryVenue(request, response, next) {
    let location = request.body.location;
    let locationName = request.body.locationName;
    let radius = request.body.radius;
    if (!radius) {
        radius = 5000;
    } else {
        radius = Math.min(5000, Math.max(1000, radius));
    }
    const keyword = request.body.keyword;

    if (locationName) {
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

        location = {
            type: 'Point',
            coordinates: [result[0].longitude, result[0].latitude]
        };

        locationName = result[0].formattedAddress;
    }


    const closestSearch = await SearchRequest.findClosestLocation(location, keyword, 5000);

    if (closestSearch.length === 0) {
        //load all google results into database
        const googleResults = await queryAllVenues(location, keyword);
        await Promise.all(_.map(googleResults, importGoogleResult));
        SearchRequest.create({
            location: location,
            keyword: keyword
        });
    }

    //search in our database for query
    const venues = await searchVenuesInDB(location, keyword, radius);

    response.send({
        location: location,
        locationName: locationName,
        radius: radius,
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
 * @returns {Promise.<*>} result
 */
function searchVenuesInDB(location, keyword = '', radius = 5000) {
    const query = Venue.find({
        $or: [
            {name: new RegExp(keyword, 'i')},
            {types: new RegExp(keyword, 'i')}
        ]
    });
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
function queryAllVenues(location, keyword, next_page_token = '') {
    const api = googleapilib(config.googleapi.GOOGLE_PLACES_API_KEY, config.googleapi.GOOGLE_PLACES_OUTPUT_FORMAT);

    const params = {
        location: [
            location.coordinates[1],
            location.coordinates[0]
        ],
        keyword: keyword,
        radius: 5000,
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
                    queryAllVenues(location, keyword, res.next_page_token).then((recvResult) => {
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
function like(request, response, next){
    Venue.findOne({place_id: request.body.venueid}, (err, obj) => {
        if(err){
            response.send(404, 'venue could not be found');
            return next();
        }
        let comment = obj.comments.find({text: request.body.comment});
        comment.likes += 1;
        obj.comments = comment;
        obj.save();
        response.json({message: 'likes: ' + comment.likes});
        return next();
    });
}

/**
 * adds a dislike to a comment of a venue
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function dislike(request, response, next){
    Venue.findOne({place_id: request.body.venueid}, (err, obj) => {
        if(err){
            response.send(404, 'venue could not be found');
            return next();
        }
        let comment = obj.comments.find({text: request.body.comment});
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
function addComment(request, response, next){
    Venue.findOne({place_id: request.body.venueid}, (err, venue) => {
        if(err){
            response.send(404, 'venue could not be found in database');
            return next();
        }
        User.findOne({'user_id': request.authentication._id}, (err, obj) => {
            if(err){
                response.send(404, 'user could not be found in database');
                return next();
            }
            const comment = {'author': obj.name, 'text': request.body.comment, 'likes': 0, 'dislikes': 0, 'date': Date.now()};
            venue.comments.push(comment);
            venue.save();
            return next();
        });
    });
}

/**
 * gets comments of a venue
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function getComments(request, response, next){
    Venue.findOne({place_id: request.body.venueid}, (err, obj) => {
        if(err){
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
function delComment(request, response, next){
    Venue.findOne({place_id: request.body.venueid}, (err, venue) => {
        if(err){
            response.send(404, 'venue could not be found in database');
            return next();
        }
        User.findOne({'user_id': request.authentication._id}, (err, obj) => {
            if(err){
                response.send(404, 'user could not be found in database');
                return next();
            }
            let comment = obj.comments.find({text: request.body.comment});
            if(comment.author._id.equals(obj._id)){
                obj.comments.pull({'text': request.body.comment});
                obj.save();
            }
            response.send(200, 'comment deleted');
            return next();
        });
    });
}

module.exports = {
    queryVenue, queryAllVenues, searchVenuesInDB, getImage, getImageNames, delImage, putImage, like, dislike, addComment, getComments
};

