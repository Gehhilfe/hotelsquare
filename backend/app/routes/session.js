'use strict';

const jwt = require('jsonwebtoken');
const config = require('config');
const mongoose = require('mongoose');

require('../models/user');
const User = mongoose.model('User');


/**
 * Creates a new session with the given login credentials.
 *
 * @function postSession
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function postSession(request, response, next) {
    User.login(request.params).then((u) => {
        const token = jwt.sign(u.toJSON(), config.jwt.secret, config.jwt.options);
        response.json({token: token});
        return next();
    }, () => {
        response.status(401);
        response.json({error: 'Login credentials wrong!'});
        return next();
    });
}

module.exports = { postSession };