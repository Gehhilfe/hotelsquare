'use strict';
const os = require('os');
const config = require('config');
const restify = require('restify');
const session = require('./app/routes/session');
const user = require('./app/routes/user');
const util = require('./lib/util');
const mongoose = require('mongoose');
const auth = require('./app/middleware/filter/authentication');
const bunyan = require('bunyan');
const restifyBunyanLogger = require('restify-bunyan-logger');
const fs = require('fs');


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

server.use(restify.bodyParser({
    maxBodySize: 1024*1024,
    mapParams: true,
    mapFiles: true,
    overrideParams: false,
    keepExtensions: true,
    uploadDir: os.tmpdir(),
    multiples: true,
    hash: 'sha1'
}));

let streams = undefined;

if(config.logstash) {
    streams = [{
        type: 'raw',
        stream: require('bunyan-logstash').createStream(config.logstash)
    }];
}

const bunyanLogger = bunyan.createLogger({
    name: 'hotel-square',
    level: ((process.env.HOTEL_QUIET)?bunyan.FATAL + 1 : bunyan.INFO),
    streams: streams
});

server.on('after', restifyBunyanLogger({
    skip: function(req) {
        return req.method === 'OPTIONS';
    },
    custom: function(req, res, route, err, log) {
        // This will not work when using gzip.
        log.res.length = res.get('Content-Length');

        log.err = err;

        // Don't forget to return!
        return log;
    },
    logger: bunyanLogger
}));

// Session
server.post('session', session.postSession);

// User
server.get('user', auth, user.profile);
server.post('users', auth, user.search);

server.get('user/:name', user.profile);
server.get('user/:name/avatar', auth, user.getAvatar);

server.post('user', user.register);
server.post('user/:name/friend_requests', auth, user.sendFriendRequest);

server.put('user', auth, user.updateUser);

server.del('user', auth, user.deleteUser);

server.get('profile', auth, user.profile);
server.get('profile/avatar', auth, user.getAvatar);

server.del('profile/friends/:name', auth, user.removeFriend);

server.post('profile/avatar', auth, user.uploadAvatar);

server.put('profile/friend_requests/:name', auth, user.confirmFriendRequest);

server.del('profile/avatar', auth, user.deleteAvatar);


// Delete downloads
server.on('after', (request) => {
    if(request.files) {
        const key = Object.keys(request.files);
        key.forEach((k) => {
            fs.unlink(request.files[k].path, () => {});
        });
    }
});

server.listen(8081, function () {
    server.log.info('%s listening at %s', server.name, server.url);
});

module.exports = server;