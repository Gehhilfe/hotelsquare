'use strict';

const jwt = require('jsonwebtoken');
const config = require('config');

/**
 * Filters all requests for authentication information.
 * If a requests matches a route and no valid auth information is provided a 403 is returned.
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
                response.authentication = decoded;
                return next();
            }
        });
    } else {
        response.status(403);
        response.json({});
        return next(new Error('Authentication missing'));
    }
};