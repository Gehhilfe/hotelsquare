'use strict';

/**
 * Created by gehhi on 02.05.2017.
 */

const jwt = require('jsonwebtoken');
const config = require('config');
const mongoose = require('mongoose');

require('../models/user');
const User = mongoose.model('User');

/*
 * POST /session
 * Creates a new session with given login details
 */
function postSession(req, res, next) {
    User.login(req.body).then((u) => {
        var token = jwt.sign(u.toJSON(), config.jwt.secret, config.jwt.options);
        res.json({token: token});
        return next();
    }, () => {
        res.status(401);
        res.json({error: 'Login credentials wrong!'});
        return next();
    });
}

module.exports = { postSession };