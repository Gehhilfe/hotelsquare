'use strict';

const _ = require('lodash');
const restify = require('restify');
const Venue = require('../models/venue');
const googleapilib = require('googleplaces');
const config = require('config');
const SearchRequest = require('../models/searchrequest');

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
    const location = request.body.location;
    const locationName = request.body.locationName;
    const keyword = request.body.keyword;

    const closestSearch = await SearchRequest.findClosestLocation(location, keyword, 5000);

    if (closestSearch.length === 0) {
        //load all google results into database
        const googleResults = await queryAllVenues(location, keyword);
        await Promise.all(_.map(googleResults, importGoogleResult));
        SearchRequest.create({
            location: {
                type: 'Point',
                coordinates: location
            },
            keyword: keyword
        });
    }

    //search in our database for query
    const venues = await searchVenuesInDB(location, keyword);

    response.send(venues);
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
function searchVenuesInDB(location, keyword = '', radius = 30000) {
    const query = Venue.find({name: new RegExp(keyword, 'i')});
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

