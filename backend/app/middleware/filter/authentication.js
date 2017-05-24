'use strict';

const jwt = require('jsonwebtoken');
const config = require('config');

/**
 * Checks if a request is authenticated and drops non authenticated connections with status 403.
 *
 * @param {Object} request http request
 * @param {Object} response http response
 * @param {Function} next next handler
 * @returns {undefined}
 */
module.exports = function (request, response, next) {

    if (request.headers['x-auth']) {
        const token = request.headers['x-auth'];
        jwt.verify(token, config.jwt.secret, (err, decoded) => {
            if (err) {
                response.status(403);
                response.json({});
                return next(new Error(err));
            } else {
                request.authentication = decoded;
                return next();
            }
        });
    } else {
        response.status(403);
        response.json({});
        return next(new Error('Authentication missing'));
    }
};