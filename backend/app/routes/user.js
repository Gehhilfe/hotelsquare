const mongoose = require('mongoose');
require('../models/user');
const User = mongoose.model('User');

/**
 * registers a new user with the given profile information.
 *
 * @function postUser
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function postUser(request, response, next) {

    const user = User(request.params);
    user.validate().then(() => {
        user.save();
        response.status(200);
        response.json(user);
        return next();
    }, () => {
        response.status(400);
        response.json({error: 'new user could not be created'});
        return next();
    });
}

/**
 * Deletes the current authenticated user
 *
 * @param {IncommingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function deleteUser(request, response, next) {
    response.json();
    return next();
}

/**
 * Uploads a avatar image for the current authenticated user and stores into
 * minio cloud storage.
 *
 * @param {IncommingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function uploadAvatar(request, response, next) {
    response.json();
    return next();
}

module.exports = {postUser, deleteUser, uploadAvatar};