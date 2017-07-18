'use strict';

const _ = require('lodash');
const restify = require('restify');
const User = require('../models/user');


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
}

module.exports = {
    getFriends
};