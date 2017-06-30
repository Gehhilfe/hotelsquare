'use strict';
const os = require('os');
const config = require('config');
const restify = require('restify');
const session = require('./app/routes/session');
const user = require('./app/routes/user');
const venue = require('./app/routes/venue');
const chat = require('./app/routes/chat');
const chatsocket = require('./app/routes/chatsocket');
const util = require('./lib/util');
const mongoose = require('mongoose');
const auth = require('./app/middleware/filter/authentication');
const bunyan = require('bunyan');
const restifyBunyanLogger = require('restify-bunyan-logger');
const fs = require('fs');

mongoose.Promise = global.Promise;

const server = restify.createServer();

const io = require('socket.io').listen(server);
chatsocket(io);

util.connectDatabase(mongoose).then(() => {
    //Bootstrap database
    if (process.env.NODE_ENV !== 'production') {
        const User = require('./app/models/user');

        if (config.bootstrap) {
            if (config.bootstrap.User) {
                User.remove({}).then(() => {
                    util.bootstrap(User, config.bootstrap.User);
                });
            }
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
let bunyanLogger;
if(config.logstash) {
    streams = [{
        type: 'raw',
        stream: require('bunyan-logstash').createStream(config.logstash)
    }];
    bunyanLogger = bunyan.createLogger({
        name: 'hotel-square',
        level: ((process.env.HOTEL_QUIET)?bunyan.FATAL + 1 : bunyan.INFO),
        streams: streams
    });
} else {
    bunyanLogger = bunyan.createLogger({
        name: 'hotel-square',
        level: ((process.env.HOTEL_QUIET)?bunyan.FATAL + 1 : bunyan.INFO)
    });
}

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

if(process.env.NODE_ENV !== 'production') {
    server.use(restify.CORS({

        // Defaults to ['*'].
        origins: ['*']

    }));

    server.opts(/.*/, function (req, res, next) {
        res.header('Access-Control-Allow-Origin', '*');
        res.header('Access-Control-Allow-Methods', req.header('Access-Control-Request-Method'));
        res.header('Access-Control-Allow-Headers', req.header('Access-Control-Request-Headers'));
        res.send(200);
        return next();
    });

    bunyanLogger.info('Using CORS');
}

// Session
server.post('session', session.postSession);

// User
server.get('user', auth, user.profile);
server.post('users', auth, user.search);

server.get('user/:name', user.profile);
server.get('user/:name/avatar', auth, user.getAvatar);

server.post('user', user.register, (request, response, next) => {
    io.sockets.emit('new user', 'hello');
});
server.post('user/:name/friend_requests', auth, user.sendFriendRequest);

server.put('user', auth, user.updateUser);

server.del('user', auth, user.deleteUser);

server.get('profile', auth, user.profile);
server.get('profile/avatar', auth, user.getAvatar);

server.del('profile/friends/:name', auth, user.removeFriend);

server.post('profile/avatar', auth, user.uploadAvatar);

server.put('profile/friend_requests/:name', auth, user.confirmFriendRequest);

server.del('profile/avatar', auth, user.deleteAvatar);

//Chat
server.post('chat/:recipients', auth, chat.newChat);

server.post('chat/reply/:chatId', auth, chat.replyMessage);

server.get('chat/with/:chatId', auth, chat.getConversation);

server.get('chat/all', auth, chat.getConversations);

//Venue
server.post('venues/query', venue.queryVenue);

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
    bunyanLogger.info('%s listening at %s', server.name, server.url);
});

module.exports = server;