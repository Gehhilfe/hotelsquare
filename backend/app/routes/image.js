'use strict';

const _ = require('lodash');

const restify = require('restify');
const Image = require('../models/image');

/**
 * Add stat information for cache able request
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function getStat(request, response, next) {
    const image = await Image.findOne({_id: request.params.id});
    const size = request.params.size;

    if (!_.inRange(size, 3))
        throw new restify.errors.BadRequestError({
            field: 'size',
            message: 'Size needs to be 0<=size<=2.'
        });

    const stat = await image.getStat(size);
    response.etag = stat.etag;
    response.header('etag', stat.etag);
    response.header('last-modified', stat.lastModified);
    response.header('content-type', 'image/jpeg');
    return next();
}

/**
 * Add actual data for cache able request
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function getData(request, response, next) {
    const image = await Image.findOne({_id: request.params.id});
    const size = request.params.size;

    if (!_.inRange(size, 3))
        throw new restify.errors.BadRequestError({
            field: 'size',
            message: 'Size needs to be 0<=size<=2.'
        });

    const data = await image.getObject(size);
    response.end(data);
    return next();
}


module.exports = {
    getStat, getData
};