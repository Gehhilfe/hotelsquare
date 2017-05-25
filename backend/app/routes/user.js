
const User = require('../models/user');
const minio = require('minio');
const config = require('config');
const sharp = require('sharp');

const minioClient = new minio.Client({
    endPoint: 'stimi.ovh',
    port: 9000,
    secure: false,
    accessKey: config.minio.key,
    secretKey: config.minio.secret
});


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
    User.findByIdAndRemove(request.authentication._id, (error, res) => {
        if(error){
            console.log(error);
            response.status(500);
            response.json(error);
            return next();
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
        .resize(200,200)
        .toFormat('jpeg')
        .toBuffer()
        .then((buffer) => {
            minioClient.putObject(config.minio.bucket, 'avatar_'+request.authentication.name+'.jpeg', buffer, 'image/jpeg', (err, etag) => {
                if(err) {
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
    minioClient.removeObject(config.minio.bucket, 'avatar_'+request.authentication.name+'.jpeg', (err) => {
        if(err) {
            response.send('404', 'Avatar for '+request.authentication.name+' not found');
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
    if(request.params.name === undefined)
        request.params.name = request.authentication.name;

    minioClient.getObject(config.minio.bucket, 'avatar_'+request.params.name+'.jpeg', (err, buffer) => {
        if (err) {
            response.send(404, '');
        } else {
            response.setHeader('Content-Type', 'image/jpeg');
            buffer.pipe(response);
        }
        return next();
    });
}

module.exports = {postUser, deleteUser, uploadAvatar, getAvatar, deleteAvatar};