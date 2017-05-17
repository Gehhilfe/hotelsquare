'use strict';

const config = require('config');
const restify = require('restify');
const session = require('./app/routes/session');
const util = require('./lib/util');
const mongoose = require('mongoose');
mongoose.Promise = global.Promise;

const server = restify.createServer();

util.connectDatabase(mongoose).then(() => {
    //Bootstrap database
    if(process.env.NODE_ENV !== 'production') {
        const User = require('./app/models/user');

        if(config.bootstrap) {
            if(config.bootstrap.User)
                util.bootstrap(User, config.bootstrap.User);
        }
    }
});


const db = mongoose.connection;
db.on('error', console.error.bind(console, 'connection error:'));
server.use(restify.bodyParser({ mapParams: true }));

server.pre(function (request, response, next) {
    console.log(request.rawHeaders);
    next();
});

server.post('/session', session.postSession);

server.listen(8081, function() {
    console.log('%s listening at %s', server.name, server.url);
});

module.exports = server;
