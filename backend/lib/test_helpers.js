'use strict';

const jwt = require('jsonwebtoken');
const config = require('config');


/**
 * Creates a signed jwt token with the given data
 * Should only be used for tests!
 * 
 * @param {Object} data JSON Object
 * @returns {String} Token as String
 */
function createToken(data) {
    return jwt.sign(data, config.jwt.secret);
}

module.exports = {createToken};