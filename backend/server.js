'use strict';

const config = require('config');
const restify = require('restify');
const session = require('./app/routes/session');
const user = require('./app/routes/user');
const util = require('./lib/util');
const mongoose = require('mongoose');
const auth = require('./app/middleware/filter/authentication');
const  restifyBunyanLogger = require('restify-bunyan-logger');

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


server.on('after', restifyBunyanLogger());
//server.pre(require('./app/middleware/log'));

// session
server.post('session', session.postSession);

// user
server.post('user', user.postUser);
server.del('user', auth, user.deleteUser);

server.listen(8081, function () {
    console.log('%s listening at %s', server.name, server.url);
});

module.exports = server;
