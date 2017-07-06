'use strict';

const _ = require('lodash');
const restify = require('restify');
const Comment = require('../models/comment');
const config = require('config');
const minio = require('minio');
const User = require('../models/user');
const sharp = require('sharp');

const minioClient = new minio.Client({
    endPoint: 'stimi.ovh',
    port: 9000,
    secure: false,
    accessKey: config.minio.key,
    secretKey: config.minio.secret
});

/**
 * adds a like to a comment
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function like(request, response, next){
    const comment = await Comment.findOne({_id: request.params.id});
    if(comment){
        comment.likes += 1;
        await comment.save();
        response.json({message: 'likes: ' + comment.likes});
        return next();
    }
    response.send(404, 'comment could not be found');
    return next();
}

/**
 * adds a dislike to a comment
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function dislike(request, response, next){
    const comment = await Comment.findOne({_id: request.params.id});
    if(comment){
        comment.dislikes += 1;
        comment.save();
        response.json({message: 'dislikes: ' + comment.dislikes});
        return next();
    }
    response.send(404, 'comment could not be found');
    return next();
}

/**
 * add a comment
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function addComment(request, response, next){
    const type = request.body.type;
    switch(type){
        case 'venue':
            break;
        case 'comment':
            break;
        case 'image':
            break;
        default:

            break;
    }
    const user = await User.findOne({_id: request.authentication._id});
    const comment = await Comment.create({
        author: user,
        text: request.body.comment,
        likes: 0,
        dislikes: 0,
        date: Date.now()
    });
    response.json(comment);
    return next();
}

/**
 * gets comments of a venue
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function getComments(request, response, next){
    const comments = await Venue.findOne({_id: request.params.id});
    response.json(venue.comments);
    return next();
}

/**
 * deletes comment of a venue if the requesting person is the author
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function delComment(request, response, next){
    const user = await User.findOne({_id: request.authentication._id});
    if(user){
        const comment = await Comment.find({'text': request.body.comment}); ///arrrrrrghhh
        if(comment){
            if(comment.author._id.equals(user._id)){
                await Comment.find({'text': request.body.comment}).remove();
            }
            response.send(200, 'comment deleted');
            return next();
        }
        response.send(404, 'comment not found');
    }
    response.send(404, 'user not known');
    return next();
}

module.exports = {
    queryVenue, queryAllVenues, searchVenuesInDB, getImage, getImageNames, delImage, putImage, like, dislike, addComment, getComments, delComment
};

