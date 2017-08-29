'use strict';
const _ = require('lodash');
const errors = require('restify-errors');
const Completion = require('../models/completion');
const googleapilib = require('googleplaces');
const config = require('config');

/**
 * AutoComplete input into result
 *
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function complete(request, response, next) {
    const api = googleapilib(config.googleapi.GOOGLE_PLACES_API_KEY, config.googleapi.GOOGLE_PLACES_OUTPUT_FORMAT);

    const query = request.body.toLowerCase();

    const parameters = {
        input: query,
        types: 'geocode'
    };

    const comp = await Completion.findOne({input: query});

    if(comp) {
        response.send(comp.result);
        return next();
    } else {

        api.placeAutocomplete(parameters, (err, res) => {
            if(err)
                return next(new errors.InternalServerError(err));

            Completion.create({
                input: query,
                result: res
            });

            response.send(res);
            return next();
        });
    }
}

module.exports = {
    complete
};