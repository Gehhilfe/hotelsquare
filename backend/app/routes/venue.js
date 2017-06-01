'use strict';

const restify = require('restify');
const Venue = require('../models/venue');
const googleapilib = require('googleplaces');
const config = require('config');

/**
 * queries for venues
 *
 * @function query
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function queryVenue(request, response, next) {
    let api = googleapilib(config.googleapi.GOOGLE_PLACES_API_KEY, config.googleapi.GOOGLE_PLACES_OUTPUT_FORMAT);

    let params = {
        location: request.body.location,
        keyword: request.body.keyword
    };

    try {
        api.nearBySearch(params, function (error, res) {
            if (error) {
                response.send(500, err);
                return next();
            } else {
                response.json(res);
                return next();
            }
        });
    } catch (err) {
        console.log(err);
    }
}

module.exports = {
    queryVenue
};

