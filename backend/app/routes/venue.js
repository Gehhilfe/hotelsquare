'use strict';

//const restify = require('restify');
//const Venue = require('../models/venue');
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

    if(!(closestSearch.length !== 0 && closestSearch[0].dis < 30000)){
        //load all google results into database
    }

    //search in our database for query
    const venues = await searchVenuesInDB(location, keyword);

    response.send(result);
    return next();
}

async function searchVenuesInDB(location, keyword='', radius=30000) {
    let query = await Venue.find({ location: { $nearSphere: Venue.location, $maxDistance: radius, $spherical: yes } });
    if(keyword !== '' && query.length > 0){
        query = query.filter(function(venue){return venue.name.contains(keyword);});
    }
    let result = await query;

    if (result.length === 0)
        return next(new restify.errors.NotFoundError());

    return result;
}

function queryAllVenues(location){
    const api = googleapilib(config.googleapi.GOOGLE_PLACES_API_KEY, config.googleapi.GOOGLE_PLACES_OUTPUT_FORMAT);

    const params = {
        location: location,
        radius: 50000,
        language: en
    };

    api.nearBySearch(params, function (error, res) {
        if (error) {
            response.send(500, error);
            return next();
        } else {
            response.json(res);
            return next();
        }
    });
}

module.exports = {
    queryVenue
};

