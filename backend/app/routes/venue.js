'use strict';

const _ = require('lodash');
const restify = require('restify');
const Venue = require('../models/venue');
const googleapilib = require('googleplaces');
const config = require('config');
const SearchRequest = require('../models/searchrequests');

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
    const keyword = request.body.keyword;

    const closestSearch = await SearchRequest.findClosestLocation(location);

    if (!(closestSearch.length !== 0 && closestSearch[0].dis < 30000)) {
        //load all google results into database
        const googleResults = await queryAllVenues(location);
        await Promise.all(_.map(googleResults, importGoogleResult));
        console.log(googleResults);
    }

    //search in our database for query
    const venues = await searchVenuesInDB(location, keyword);

    response.send(venues);
    return next();
}

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
 * Retrieves venues inside search radius filtered by keyword
 *
 * @param {Number[]} location center point
 * @param {string} keyword keyword
 * @param {Number} radius search radius
 * @returns {Promise.<*>} result
 */
async function searchVenuesInDB(location, keyword = '', radius = 30000) {
    try {
        // TODO: Use regex search for keyword search
        return await Venue.geoNear(location, {maxDistance: radius, spherical: true});
        /*if (keyword !== '' && query.length > 0) {
         query = query.filter(function (venue) {
         return venue.name.contains(keyword);
         });
         }*/
        //const result = await query;

        //if (result.length === 0)
        //    return new restify.errors.NotFoundError();

        //return result;
    } catch (err) {
        console.log(err);
    }
}

/**
 * Lookups all venues nearby location
 *
 * @param {Number[]} location Center of lookup radius
 * @param {string} next_page_token token for next result page
 * @returns {Promise} Lookup result
 */
function queryAllVenues(location, next_page_token = '') {
    const api = googleapilib(config.googleapi.GOOGLE_PLACES_API_KEY, config.googleapi.GOOGLE_PLACES_OUTPUT_FORMAT);

    const params = {
        location: location,
        radius: 5000,
        language: 'en',
        types: [
            'jewelry_store',
            'laundry',
            'bakery',
            'liquor_store',
            'bar',
            'beauty_salon',
            'bicycle_store',
            'book_store',
            'meal_delivery',
            'meal_takeaway',
            'bowling_alley',
            'cafe',
            'movie_rental',
            'movie_theater',
            'night_club',
            'casino',
            'pet_store',
            'restaurant',
            'shopping_mall',
            'clothing_store',
            'convenience_store',
            'gym',
            'hair_care',
            'zoo'
        ]
    };

    if(!_.isEmpty(next_page_token)) {
        params.pagetoken = next_page_token;
    }

    return new Promise((resolve, reject) => {
        api.nearBySearch(params, function (error, res) {
            if (error) {
                reject(error);
            } else {
                if(res.next_page_token && !_.isElement(res.next_page_token)) {
                    queryAllVenues(location, res.next_page_token).then((recvResult) => {
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

