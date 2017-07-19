'use strict';

const _ = require('lodash');
const restify = require('restify');
const User = require('../models/user');

/**
 * Returns a bunch of 20 friends
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next
 * @returns {undefined}
 */
async function getFriends(request, response, next) {
    let page = 0;
    if (request.params.page)
        page = request.params.page;
    let user = await User.findOne({_id: request.authentication._id});
    const friends = user.friends.length;

    user = await user.populate({
        path: 'friends',
        options: {
            limit: 20,
            sort: {updated_at: -1},
            skip: page * 20
        }
    }).execPopulate();
    response.send({
        count: friends,
        friends: _.map(user.friends, (it) => it.toJSONPublic())
    });
    return next();
}

/**
 * returns all friends within 5km radius
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function}next next
 * @returns {undefined}
 */
async function getNearByFriends(request, response, next) {
    const radius = 5000;
    const user = await User.findOne({
        _id: request.authentication._id
    });

    const users = await User.find({
        _id : { $in : user.friends },
        incognito: false
    }).where('location').near({
        center: request.body,
        maxDistance: radius
    });
    response.send(_.map(user, (it) => it.toJSONPublic()));
    return next();
}

module.exports = {
    getFriends,
    getNearByFriends
};