'use strict';

const _ = require('lodash');
const restify = require('restify');
const Comment = require('../models/comment');
const Image = require('../models/image');
const Venue = require('../models/venue');
const User = require('../models/user');

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
 * adds a comment to a venue
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function addComment(request, response, next) {
    const user = await User.findOne({_id: request.authentication._id});
    if(request.body.venueID){
        const venue = await Venue.findOne({_id: request.body.venueID});
        if(venue){
            const newcomment = {'kind': 'VenueComment', 'venue': venue, 'author': user, 'text': request.body.text, 'likes': 0, 'dislikes': 0, 'date': Date.now()};
            const c = await Comment.create(newcomment);
            if(c) {
                venue.comments.push(c);
                await venue.save();
                response.json(c);
                return next();
            }
            response.send(500, 'comment could not be created');
            return next();
        }
        response.send(404, 'venue not found');
        return next();
    }
    if(request.body.imageID){
        const image = await Image.findOne({_id: request.body.imageID});
        if(image){
            const newcomment = {'kind': 'ImageComment', 'image': image, 'author': user, 'text': request.body.text, 'likes': 0, 'dislikes': 0, 'date': Date.now()};
            const c = await Comment.create(newcomment);
            if(c) {
                image.comments.push(c);
                await image.save();
                response.json(c);
                return next();
            }
            response.send(500, 'comment could not be created');
            return next();
        }
        response.send(404, 'image not found');
        return next();
    }
    if(request.body.textID){
        const comment = await Comment.findOne({_id: request.body.textID});
        if(comment){
            const newcomment = {'kind': 'TextComment', 'comment': comment, 'author': user, 'text': request.body.text, 'likes': 0, 'dislikes': 0, 'date': Date.now()};
            const c = await Comment.create(newcomment);
            if(c) {
                comment.comments.push(c);
                await comment.save();
                response.json(c);
                return next();
            }
            response.send(500, 'comment could not be created');
            return next();
        }
        response.send(404, 'comment not found');
        return next();
    }
    response.send(500, 'not defined comment category');
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
async function getComment(request, response, next){
    const comment = await Comment.findOne({_id: request.params.id});
    if(comment){
        response.json(comment);
        return next();
    }
    response.send(404, 'comment not found');
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
        const comment = await Comment.findOne({_id: request.params.id});
        if(comment){
            if(comment.author._id.equals(user._id)){
                switch(comment.kind){
                case 'ImageComment':
                    await Image.update({_id: comment.image._id}, {$pull: {'comments': {_id: comment._id}}});
                    break;
                case 'TextComment':
                    await Comment.update({_id: comment.image._id}, {$pull: {'comments': {_id: comment._id}}});
                    break;
                case 'VenueComment':
                    await Venue.update({_id: comment.image._id}, {$pull: {'comments': {_id: comment._id}}});
                    break;
                }
                //await Comment.findOne({_id: request.params._id}).remove();
                await comment.remove();
                response.send(200, 'comment deleted');
                return next();
            }
            response.send(500, 'comment not found');
            return next();
        }
        response.send(404, 'comment not found');
    }
    response.send(404, 'user not known');
    return next();
}

module.exports = {
    like, dislike, addComment, getComment, delComment
};

