'use strict';

const restify = require('restify');
const util = require('util');

/**
 * Maps a mongoose validation error to a restify error
 * @param {Array} errors Error descriptions
 * @constructor
 */
function ValidationError(errors) {
    const message = [];
    for (const key in errors) {
        message.push({
            field: key,
            message: errors[key].message
        });
    }

    restify.RestError.call(this, {
        restCode: 'ValidationError',
        statusCode: 400,
        message: message,
        constructorOpt: ValidationError
    });
    this.name = 'ValidationError';
    this.body.errors = message;
}

util.inherits(ValidationError, restify.RestError);

module.exports = ValidationError;