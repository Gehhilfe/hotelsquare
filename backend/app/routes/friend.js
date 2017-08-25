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
        populate: {
            path: 'avatar'
        },
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
 * Returns a bunch of 20 friendrequest
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next
 * @returns {undefined}
 */
async function getFriendRequests(request, response, next) {
    let page = 0;
    if (request.params.page)
        page = request.params.page;
    const user = await User.findOne({_id: request.authentication._id});
    if (user.friend_requests.length === 0) {
        response.send([]);
        return next();
    }
    const sorted = _.sortBy(user.friend_requests, 'created_at');
    const mapped = _.map(sorted, (it) => it.sender);
    const sliced = _.take(_.drop(mapped, page * 20), 20);
    const friendRequests = await User.find({_id: {$in: sliced}}).populate('avatar');
    response.send(_.map(friendRequests, (it) => it.toJSONPublic()));
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

    const location = (request.body) ? request.body : user.location;
    let filterSet = user.friends;

    if (request.params.only) {
        filterSet = _.intersection(filterSet, [request.params.only]);
    }

    const users = await User.find({
        _id: {$in: filterSet},
        incognito: false
    }).where('location').near({
        center: location,
        maxDistance: radius
    }).populate('avatar');
    response.send(_.map(users, (it) => it.toJSONPublic()));
    return next();
}

module.exports = {
    getFriends,
    getNearByFriends,
    getFriendRequests
};