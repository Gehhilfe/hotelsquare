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

const Image = require('./app/models/image');

util.connectDatabase(mongoose).then(async () => {
    //Bootstrap database
    if (process.env.NODE_ENV !== 'production') {
        const User = require('./app/models/user');
        const Venue = require('./app/models/venue');
        const Message = require('./app/models/message');
        const SearchRequest = require('./app/models/searchrequest');
        const GeocodeResult = require('./app/models/geocoderesult');

        await Promise.all([
            Venue.remove({}),
            Message.remove({}),
            SearchRequest.remove({}),
            GeocodeResult.remove({})
        ]);

        if (config.bootstrap) {
            if (config.bootstrap.User) {
                await User.remove({});
                await util.bootstrap(User, config.bootstrap.User);
            }
            if(config.bootstrap.UserFriend) {
                await util.bootstrapFriends(config.bootstrap.UserFriend);
            }
            if(config.bootstrap.UserFriendRequest) {
                await util.bootstrapFriendRequets(config.bootstrap.UserFriendRequest);
            }
        }
    }
});

server.use(restify.bodyParser({
    maxBodySize: 1024 * 1024,
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
if (config.logstash) {
    streams = [{
        type: 'raw',
        stream: require('bunyan-logstash').createStream(config.logstash)
    }];
    bunyanLogger = bunyan.createLogger({
        name: 'hotel-square',
        level: ((process.env.HOTEL_QUIET) ? bunyan.FATAL + 1 : bunyan.INFO),
        streams: streams
    });
} else {
    bunyanLogger = bunyan.createLogger({
        name: 'hotel-square',
        level: ((process.env.HOTEL_QUIET) ? bunyan.FATAL + 1 : bunyan.INFO)
    });
}

server.on('after', restifyBunyanLogger({
    skip: function (req) {
        return req.method === 'OPTIONS';
    },
    custom: function (req, res, route, err, log) {
        
        if(req.method !== 'GET') {
            log.req.body = req.body;
        }

        // This will not work when using gzip.
        log.res.length = res.get('Content-Length');

        log.err = err;

        // Don't forget to return!
        return log;
    },
    logger: bunyanLogger
}));

if (process.env.NODE_ENV !== 'production') {
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
server.post('sessions', session.postSession);

// User
server.get('users', auth, user.profile);

server.get('users/:name', user.profile);

server.post('users', user.register, (request, response, next) => {
    io.sockets.emit('new user', 'hello');
    return next();
});
server.post('users/:name/friend_requests', auth, user.sendFriendRequest);

server.put('users', auth, user.updateUser);
server.del('users', auth, user.deleteUser);

server.get('profile', auth, user.profile);

server.post('profile/avatar', auth, user.uploadAvatar);
server.del('profile/avatar', auth, user.deleteAvatar);

server.del('profile/friends/:name', auth, user.removeFriend);
server.put('profile/friend_requests/:name', auth, user.confirmFriendRequest);

// Image
server.post('images', async (request, response, next) => {
    const img = await Image.upload(request.files.image.path);
    response.json(img);
    return next();
});

// Chat
server.get('chats/:chatId', auth, chat.getConversation);
server.post('chats', auth, chat.newChat);
server.post('chats/:chatId/messages', auth, chat.replyMessage);
server.get('chats', auth, chat.getConversations);

// Venue
server.get('venues/:id', venue.getVenue);
server.post('venues/images', auth, venue.putImage);
server.get('venues/images', auth, venue.getImage);
server.del('venues/images', auth, venue.delImage);
server.get('venues/imagenames', auth, venue.getImageNames);

server.post('venues/:id/comments', auth, venue.addComment);
server.get('venues/comments', venue.getComments);
server.del('venues/comment', auth, venue.delComment);
server.post('venues/like', auth, venue.like);
server.post('venues/dislike', auth, venue.dislike);

// Search

// User
server.post('searches/users', auth, user.search);
// Venue
server.post('searches/venues', venue.queryVenue);
server.post('searches/venues/:page', venue.queryVenue);

// Delete downloads
server.on('after', (request) => {
    if (request.files) {
        const key = Object.keys(request.files);
        key.forEach((k) => {
            fs.unlink(request.files[k].path, () => {
            });
        });
    }
});

server.listen(8081, function () {
    bunyanLogger.info('%s listening at %s', server.name, server.url);
});

module.exports = server;