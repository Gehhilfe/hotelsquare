const mongoose = require('mongoose');
require('../models/user');
const User = mongoose.model('User');

/**
 * registers a new user with the given profile information.
 *
 * @function postUser
 * @param {Object} req request
 * @param {Object} res response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function postUser(req, res, next) {

    const user = User(req.params);
    user.validate().then(() => {
        user.save();
        res.status(200);
        res.json(user);
        return next();
    }, () => {
        res.status(400);
        res.json({error: 'new user could not be created'});
        return next();
    });
}

/**
 * Deletes the current authenticated user
 *
 * @param {Object} request
 * @param {Object}response
 * @param {Function} next
 * @returns {undefined}
 */
function deleteUser(request, response, next) {
    response.json();
    return next();
}


module.exports = {postUser, deleteUser};