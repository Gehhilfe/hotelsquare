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

const handleValidation = (next, func) => {
    func().catch((error) => {
        switch (error.name) {
        case 'MongoError':
            if(error.errmsg.includes('name')) {
                return next(new ValidationError({
                    name: {
                        message: 'Name is already taken'
                    }}));
            }
            if(error.errmsg.includes('email')) {
                return next(new ValidationError({
                    email: {
                        message: 'Email is already used'
                    }}));
            }
            break;

        case 'ValidationError':
            return next(new ValidationError(error.errors));
        }
    });
};


async function search(request, response, next) {
    let query = User.find({ name: new RegExp(request.body.name, 'i')  });
    if(request.body.gender)
        query = query.where('gender').equals(request.body.gender);
    let result = await query;
    result =  result.map( (user) => {
        const obj = user.toJSONPublic();
        obj.type = 'user';
        return obj;
    });
    response.send(result);
    return next();
}

/**
 * Retrieves user profile
 *
 * @function profile
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function profile(request, response, next) {
    let selfRequest = false;

    // When no name provided use authenticated user
    if (request.params.name === undefined) {
        request.params.name = request.authentication.name;
        selfRequest = true;
    }

    let user = await User.findOne({name: request.params.name}).populate('friend_requests.sender');
    if (user === null)
        return next(new restify.errors.NotFoundError());
    if (!selfRequest) {
        // Remove sensitive information
        user = user.toJSONPublic();
    }
    response.send(user);
    return next();
}

/**
 * Retrieves user profile information
 *
 * @function updateUser
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function updateUser(request, response, next) {
    handleValidation(next, async () => {
        const user = await User.findOne({_id: request.authentication._id});
        user.update(request.body);
        await user.save();

        response.send(user);
        return next();
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
async function register(request, response, next) {
    handleValidation(next, async () => {
        const user = await User.create(request.params);
        response.json(user);
        return next();
    });
}

/**
 * Deletes the current authenticated user
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function deleteUser(request, response, next) {
    const res = await User.findByIdAndRemove(request.authentication._id);
    response.json(res);
    return next();
}

/**
 * Uploads a avatar image for the current authenticated user and stores into
 * minio cloud storage.
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function uploadAvatar(request, response, next) {
    //Convert to jpeg
    const buffer = await sharp(request.files.avatar.path)
        .resize(200, 200)
        .toFormat('jpeg')
        .toBuffer();

    minioClient.putObject(config.minio.bucket, 'avatar_' + request.authentication.name + '.jpeg', buffer, 'image/jpeg', (err, etag) => {
        if (err) {
            response.send(500, err);
            return next();
        } else {
            response.json(etag);
            return next();
        }
    });
}

/**
 * Deletes a stored avater image for the authenticated user.
 *
 * @param {IncomingMessage} request request
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
 * @param {IncomingMessage} request request
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
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function sendFriendRequest(request, response, next) {
    const results = await Promise.all([
        User.findOne({name: request.params.name, 'friend_requests.sender': { $in: [request.authentication._id]}}),
        User.findOne({name: request.params.name, friends: { $in: [request.authentication._id]}})
    ]);
    if (results[0] === null && results[1] == null) {
        const res = await User.findOne({name: request.params.name});
        res.addFriendRequest(request.authentication);
        await res.save();
        response.json(res);
        return next();
    }
    return response.send(400, {error: 'Could not send friend request'});
}

/**
 * Removes a friend from the authenticated user
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function removeFriend(request, response, next) {
    // Find both users
    const results = await Promise.all([
        User.findOne({name: request.authentication.name}),
        User.findOne({name: request.params.name})
    ]);

    const receiver = results[0];
    const sender = results[1];

    sender.removeFriend(receiver);
    receiver.removeFriend(sender);

    await Promise.all([
        sender.save(),
        receiver.save()
    ]);
    response.json({message: 'Friend removed'});
    return next();
}

/**
 * Confirms or declines a friend request
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function confirmFriendRequest(request, response, next) {
    // Find both users
    const results = await Promise.all([
        User.findOne({name: request.authentication.name}),
        User.findOne({name: request.params.name})
    ]);

    const receiver = results[0];
    const sender = results[1];

    // Check if a friend requests exists
    const friendRequest = receiver.friend_requests.find(((e) => {
        return e.sender.equals(sender._id);
    }));

    if (friendRequest === undefined) {
        response.send(400, {error: 'No friend request existing'});
        return next();
    }

    // Remove friend request
    receiver.removeFriendRequest(friendRequest);

    if (request.body.accept) {
        // Request accepted
        User.connectFriends(sender, receiver);
        await Promise.all([
            sender.save(),
            receiver.save()
        ]);
        response.json({message: 'Friend request accepted'});
        return next();
    } else {
        // Request declined
        await receiver.save();
        response.json({message: 'Friend request declined'});
        return next();
    }
}

module.exports = {
    register,
    deleteUser,
    uploadAvatar,
    getAvatar,
    deleteAvatar,
    profile,
    sendFriendRequest,
    confirmFriendRequest,
    updateUser,
    removeFriend,
    search
};