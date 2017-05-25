'use strict';

const restify = require('restify');
const User = require('../models/user');
const minio = require('minio');
const config = require('config');
const sharp = require('sharp');
const ValidationError = require('../errors/ValidationError');

const minioClient = new minio.Client({
    endPoint: 'stimi.ovh',
    port: 9000,
    secure: false,
    accessKey: config.minio.key,
    secretKey: config.minio.secret
});

/**
 * Retrieves user profile
 *
 * @function register
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function profile(request, response, next) {
    let selfRequest = false;

    // When no name provided use authenticated user
    if (request.params.name === undefined) {
        request.params.name = request.authentication.name;
        selfRequest = true;
    }

    User.findOne({name: request.params.name}).exec().then((user) => {
        if (user === null)
            return next(new restify.errors.NotFoundError());
        if (!selfRequest) {
            // Remove sensitive information
            user = user.toJSONPublic();
        }
        response.send(user);
        return next();
    }).catch((err) => {
        return next(err);
    });
}

/**
 * Registers a new user with the given profile information.
 *
 * @function register
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function register(request, response, next) {
    User.create(request.params).then((user) => {
        response.json(user);
        return next();
    }).catch((error) => {
        switch (error.name) {
        case 'MongoError':
            return next(new ValidationError({
                name: {
                    message: 'Name is already taken'
                }
            }));

        case 'ValidationError':
            return next(new ValidationError(error.errors));
        }
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
    User.findByIdAndRemove(request.authentication._id, (error, res) => {
        if (error) {
            return next(error);
        } else {
            response.json(res);
            return next();
        }
    });
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
    //Convert to jpeg
    sharp(request.files.avatar.path)
        .resize(200, 200)
        .toFormat('jpeg')
        .toBuffer()
        .then((buffer) => {
            minioClient.putObject(config.minio.bucket, 'avatar_' + request.authentication.name + '.jpeg', buffer, 'image/jpeg', (err, etag) => {
                if (err) {
                    response.send(500, err);
                    return next();
                } else {
                    response.json(etag);
                    return next();
                }
            });
        })
        .catch((err) => {
            response.status(500);
            response.json(err);
            return next();
        });
}

/**
 * Deletes a stored avater image for the authenticated user.
 *
 * @param {IncommingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function deleteAvatar(request, response, next) {
    minioClient.removeObject(config.minio.bucket, 'avatar_' + request.authentication.name + '.jpeg', (err) => {
        if (err) {
            response.send('404', 'Avatar for ' + request.authentication.name + ' not found');
        } else {
            response.send();
        }
        return next();
    });
}

/**
 * Retrieves a stored avater image for a user.
 *
 * @param {IncommingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function getAvatar(request, response, next) {
    // When no name provided use authenticated user
    if (request.params.name === undefined)
        request.params.name = request.authentication.name;

    minioClient.getObject(config.minio.bucket, 'avatar_' + request.params.name + '.jpeg', (err, buffer) => {
        if (err) {
            response.send(404, '');
        } else {
            response.setHeader('Content-Type', 'image/jpeg');
            buffer.pipe(response);
        }
        return next();
    });
}

/**
 * Sends a friend request from the authenticated user to name
 *
 * @param {IncommingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function sendFriendRequest(request, response, next) {
    User.findOne({'friendRequests.name': request.authentication.name}, {'friendRequests.$': 1})
        .then((found) => {
            if (found === null) {
                return User.updateOne({name: request.params.name}, {$push: {'friendRequests': request.authentication}})
                    .then((res) => {
                        response.json(res);
                        return next();
                    });
            } else {
                return response.send(400, {error: 'Friend request already existing'});
            }
        }).catch((err) => {
            return next(err);
        });
}

module.exports = {register, deleteUser, uploadAvatar, getAvatar, deleteAvatar, profile, sendFriendRequest};