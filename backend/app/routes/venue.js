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
    const buffer = await sharp(request.files.avatar.path)
        .resize(200, 200)
        .toFormat('jpeg')
        .toBuffer();

    minioClient.bucketExists(request.body.venueid, function(err) {
        if(err){
            if(err.code == 'NoSuchBucket'){
                minioClient.makeBucket(request.body.venueid, 'eu-west-1', function (err) {
                    if(err) {
                        response.send(500, "new bucket could not be created");
                        return next();
                    }
                    let no = 0;
                    const imagename = 'venue_' + request.body.venueid + no.toString() + '.jpeg';
                    minioClient.putObject(request.body.venueid, imagename, buffer, 'image/jpeg', (err, etag) => {
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
                    })
                })
            }
            if(err == null) {
                let itemsinbucket = minioClient.listObjects(request.body.venueid, '', true).length();
                const imagename = 'venue_' + request.body.venueid + itemsinbucket.toString() + '.jpeg';
                minioClient.putObject(request.body.venueid, imagename, buffer, 'image/jpeg', (err, etag) => {
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
                })
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
    minioClient.bucketExists(request.body.venueid, function(err) {
        if(err) {
            if (err.code === 'NoSuchBucket') {
                response.send('404', 'Bucket for ' + request.body.venueid + ' not found');
                return next();
            }
            if(err === null){
                VenueImages.findOne({ imagename: request.body.imagename}, function(err, obj) {
                    if(obj.name === request.authentication.name) {
                        minioClient.removeObject(request.body.venueid, request.body.imagename, (err) => {
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
 * Retrieves stored images for a venue
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
/**function getImages(request, response, next) {
    // When no name provided return error
    if (request.params.venueid === undefined){
        response.send(404, 'you must request a valid venueid');
        return next();
    }

    let allimageurls = [];
    let objectsstream = minioClient.listObjects(request.body.venueid, '', true);
    objectsstream.on('data', function(obj) {
        let publicurl = minioClient.protocol + '//' + minioClient.host + ':' + minioClient.port + '/' + request.body.venueid + '/' + obj.name;
        assets.pust(publicurl);
    });
    objectsstream.on('error'), function(e){
        response.send(500, ' error requesting minio image list for venue ' + request.body.venueid);
        return next();
    }
    objectsstream.on('end', function (e) {

    })
    minioClient.getObject(config.minio.bucket, 'venue_' + request.params.venuename + '.jpeg', (err, buffer) => {
        if (buffer.length()<1) {
            response.send(404, 'No image in database');
            return next();
        }

        if (err) {
            response.send(404, '');
        } else {
            response.setHeader('Content-Type', 'image/jpeg');
            buffer.pipe(response);
        }
        return next();
    });
}*/

/**
 * Imports/Updates a google result into our database
 * @param {Object} entry result entry
 * @returns {Promise.<void>} entry in out database
 */
async function importGoogleResult(entry) {
    //try {
    //     const exisiting = await Venue.findOne({reference: entry.reference});
    //}
    //catch (err) {
    //    console.log(err);
    //}

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

module.exports = {
    queryVenue, queryAllVenues, searchVenuesInDB
};

