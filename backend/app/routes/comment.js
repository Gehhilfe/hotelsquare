'use strict';

const _ = require('lodash');
const restify = require('restify');
const Comments = require('../models/comments');

const Comment = Comments.Comment;
const TextComment = Comments.TextComment;
const ImageComment = Comments.ImageComment;

const Image = require('../models/image');
const Venue = require('../models/venue');
const User = require('../models/user');


/**
 * Add like for authenticated user
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function like(request, response, next) {
    const comment = await Comment.findOne({_id: request.params.id});
    comment.like(request.authentication);
    await comment.save();
    response.send(comment);
    return next();
}


/**
 * Add dislike for authenticated user
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function dislike(request, response, next) {
    const comment = await Comment.findOne({_id: request.params.id});
    comment.dislike(request.authentication);
    await comment.save();
    response.send(comment);
    return next();
}

/**
 * Creates closure handler for creating text comments by adding these to a model
 * @param {Object} model Model to add comments to e.g. User, Venue, Image ...
 * @returns {function(*, *, *)} handler for text comment creation
 */
function textComment(model) {
    return async (request, response, next) => {
        const o = await model.findOne({_id: request.params.id});
        const textComment = await TextComment.build(request.authentication, request.body.text, o);
        await o.save();
        response.send(await textComment.toJSONDetails());
        return next();
    };
}

/**
 * Creates closure handler for creating image comments by adding these to a model
 * @param {Object} model Model to add comments to e.g. User, Venue, Image ...
 * @returns {function(*, *, *)} handler for text comment creation
 */
function imageComment(model) {
    return async (request, response, next) => {
        const o = await model.findOne({_id: request.params.id});
        const imageComment = await ImageComment.build(request.authentication, request.files.image.path, o);
        await o.save();
        response.send(await imageComment.toJSONDetails());
        return next();
    };
}

/**
 * Creates closure handler for retrieving comments from a given model
 * @param {Object} model Model to add comments to e.g. User, Venue, Image ...
 * @returns {function(*, *, *)} handler for text comment creation
 */
function getComments(model) {
    return async (request, response, next) => {
        let page = request.params.page;
        if(!page)
            page = 0;
        const o = await model.findOne({_id: request.params.id}).populate('comments').limit(10).skip(page.params.page);
        response.send(_.map(o.comments, (e) => e.toJSON()));
        return next();
    };
}

module.exports = {
    like,
    dislike,
    textComment,
    imageComment,
    getComments
};