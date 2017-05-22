
const User = require('../models/user');
/*
TODO Store upload into minio
const minio = require('minio');
const config = require('config');


const minioClient = new minio.Client({
    endPoint: 'stimi.ovh',
    port: 9000,
    secure: true,
    accessKey: config.minio.key,
    secretKey: config.minio.secret
});
*/

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
    //console.log(request.files);
    response.json({});
    return next();
}

/**
 * Retrieves a stored avater image for the authenticated user.
 *
 * @param {IncommingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function getAvatar(request, response, next) {
    response.setHeader('Content-Type', 'image/jpeg');
    response.send();
    return next();
}

module.exports = {postUser, deleteUser, uploadAvatar, getAvatar};