const mongoose = require('mongoose');
require('../models/user');
const User = mongoose.model('User');

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
    User.find({id:request.authentication._id}).remove((error, res) => {
        if(error){
            response.status(500);
            response.json(error);
            return next();
        } else {
            response.json(res);
            return next();
        }
    });
}


module.exports = {postUser, deleteUser};