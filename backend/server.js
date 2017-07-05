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
                util.bootstrap(User, config.bootstrap.User);
                let peter = User.findOne({name: 'Peter'});
                let admin = User.findOne({name: 'Admin'});
                let janus = User.findOne({name: 'Janus'});
                let waldi = User.findOne({name: 'Waldi'});
                let rosamunde = User.findOne({name: 'Rosamunde'});
                let birte = User.findOne({name: 'Birte'});
                admin, peter = User.connectFriends(admin, peter);
                User.update(admin);
                User.update(peter);
                [admin, janus] = User.connectFriends(admin, janus);
                User.update(admin);
                User.update(janus);
                [admin, waldi] = User.connectFriends(admin, waldi);
                User.update(admin);
                User.update(waldi);
                [admin, rosamunde] = User.connectFriends(admin, rosamunde);
                User.update(admin);
                User.update(rosamunde);
                [admin, birte] = User.connectFriends(admin, birte);
                User.update(admin);
                User.update(birte);
                [admin, rosamunde] = User.connectFriends(peter, rosamunde);
                User.update(peter);
                User.update(rosamunde);
                [waldi, peter] = User.connectFriends(waldi, peter);
                User.update(waldi);
                User.update(peter);
                [waldi, rosamunde] = User.connectFriends(waldi, rosamunde);
                User.update(waldi);
                User.update(rosamunde);
                [waldi, birte] = User.connectFriends(waldi, birte);
                User.update(waldi);
                User.update(birte);
                [rosamunde, janus] = User.connectFriends(rosamunde, janus);
                User.update(rosamunde);
                User.update(janus);
                [rosamunde, birte] = User.connectFriends(rosamunde, birte);
                User.update(rosamunde);
                User.update(birte);
                [waldi, janus] = User.connectFriends(waldi, janus);
                User.update(waldi);
                User.update(janus);

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
server.post('session', session.postSession);

// User
server.get('users', auth, user.profile);

server.get('users/:name', user.profile);
server.get('users/:name/avatar', auth, user.getAvatar);

server.post('users', user.register, (request, response, next) => {
    io.sockets.emit('new user', 'hello');
});
server.post('users/:name/friend_requests', auth, user.sendFriendRequest);

server.put('users', auth, user.updateUser);
server.del('users', auth, user.deleteUser);

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


// Search

// User
server.post('searches/users', auth, user.search);
// Venue
server.post('searches/venues', venue.queryVenue);


server.post('venues/image', venue.putImage);
server.get('venues/images', venue.getImages);
server.del('venues/images', venue.delImage);

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