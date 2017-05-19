'use strict';

const config = require('config');
const restify = require('restify');
const session = require('./app/routes/session');
const user = require('./app/routes/user');
const util = require('./lib/util');
const mongoose = require('mongoose');
const auth = require('./app/middleware/filter/authentication');

mongoose.Promise = global.Promise;

const server = restify.createServer();

util.connectDatabase(mongoose).then(() => {
    //Bootstrap database
    if (process.env.NODE_ENV !== 'production') {
        const User = require('./app/models/user');

        if (config.bootstrap) {
            if (config.bootstrap.User)
                util.bootstrap(User, config.bootstrap.User);
        }
    }
});


const db = mongoose.connection;
db.on('error', console.error.bind(console, 'connection error:'));
server.use(restify.bodyParser({mapParams: true}));

server.pre(require('./app/middleware/log'));

/*
server.pre(function (req, res, next) {
    console.log('filtering server request');

    const filters = [{route: /^\/user.*$/g, method: 'DELETE'}];
    let filters_passed = true;
    filters.forEach((e) => {
        if (req.url.match(e.route) !== null && req.method === e.method) {
            if (req.headers['x-auth'] === undefined) {
                res.status(403);
                res.json({error: 'authentication failed'});
                filters_passed = false;
            }

        }
    });
    if (filters_passed)
        return next();
});*/

server.post('/session', session.postSession);

server.post('/user', user.postUser);
server.del('/user', auth, user.deleteUser);

server.listen(8081, function () {
    console.log('%s listening at %s', server.name, server.url);
});

module.exports = server;
