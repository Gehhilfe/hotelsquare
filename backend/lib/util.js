const _ = require('lodash');
const config = require('config');
const User = require('./../app/models/user');
const Chat = require('./../app/models/chat');
const Message = require('./../app/models/message');
const bunyan = require('bunyan');
const restifyBunyanLogger = require('restify-bunyan-logger');
const restify = require('restify');

const databaseURI = function () {
    return 'mongodb://' + config.get('db.host') + '/' + config.get('db.name');
};

/**
 * Bootstrap a mongoose model with a given entities
 * @param {Object} model  mongoose model
 * @param {Array} data  entities
 * @returns {Promise} Promise of all entities are created
 */
function bootstrap(model, data) {
    if (data === undefined || data === null || !(data instanceof Array))
        return Promise.reject('No data provided');
    const promises = [];
    data.forEach((e) => {
        promises.push(model.create(e));
    });
    return new Promise((resolve, reject) => {
        Promise.all(promises).then(resolve).catch(reject);
    });
}

/**
 * Boostrap friend connections for user model
 * @param {Array} data Connection between user with name a and b
 * @returns {Promise}  Promise of all connection are created
 */
function bootstrapFriends(data) {
    if (data === undefined || data === null || !(data instanceof Array))
        return Promise.reject('No data provided');
    return new Promise((resolve, reject) => {
        Promise.all(_.map(data, async (e) => {
            const a = await User.findOne({displayName: e.a});
            const b = await User.findOne({displayName: e.b});
            User.connectFriends(a, b);
            await a.save();
            await b.save();
        })).then(resolve).catch(reject);
    });
}

/**
 * Creates predefined chat instances including messages
 * @param {Object} data bootstrap information
 * @returns {Promise.<void>} Promise of all chats created
 */
async function bootstrapChat(data) {
    await Promise.all(_.each(data, async (it) => {
        const participants = await User.find({'displayName': {$in: it.participants}});
        const chat = await Chat.create({
            participants: participants
        });
        const msgs = await Promise.all(_.map(it.messages, (it) => Message.create({
            sender: _.find(participants, ['displayName', it.from]),
            message: it.message,
            chatId: chat
        })));
        _.each(msgs, (it) => chat.addMessage(it));
        await chat.save();
    }));
}

/**
 * Boostrap friend requests for user model
 * @param {Array} data Friend request entry.from to entry.to
 * @returns {Promise}  Promise of all connection are created
 */
function bootstrapFriendRequets(data) {
    if (data === undefined || data === null || !(data instanceof Array))
        return Promise.reject('No data provided');
    return new Promise((resolve, reject) => {
        Promise.all(_.map(data, async (e) => {
            const from = await User.findOne({displayName: e.from});
            const to = await User.findOne({displayName: e.to});
            to.addFriendRequest(from);
            await to.save();
        })).then(resolve).catch(reject);
    });
}

/**
 * Initialize database
 * @returns {Promise.<void>} none
 */
async function initDatabase() {
//Bootstrap database
// Disabled for life demo
    /*   if (process.env.NODE_ENV !== 'production') {
           const User = require('./../app/models/user');
           const Venue = require('./../app/models/venue');
           const Message = require('./../app/models/message');
           const SearchRequest = require('./../app/models/searchrequest');
           const GeocodeResult = require('./../app/models/geocoderesult');
           const Chat = require('./../app/models/chat');

           await Promise.all([
               Venue.remove({}),
               Message.remove({}),
               SearchRequest.remove({}),
               GeocodeResult.remove({}),
               Chat.remove({}),
               Message.remove({})
           ]);

           if (config.bootstrap) {
               if (config.bootstrap.User) {
                   await User.remove({});
                   await bootstrap(User, config.bootstrap.User);
               }
               if (config.bootstrap.UserFriend) {
                   await bootstrapFriends(config.bootstrap.UserFriend);
               }
               if (config.bootstrap.UserFriendRequest) {
                   await bootstrapFriendRequets(config.bootstrap.UserFriendRequest);
               }
               if(config.bootstrap.Chat) {
                   await bootstrapChat(config.bootstrap.Chat);
               }
           }
       }*/
}

/**
 * Initialize bunyan logger
 * @param {Object} server Restify server
 * @returns {undefined}
 */
function initLogging(server) {
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

            if (req.method !== 'GET') {
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

    return bunyanLogger;
}

/**
 * Initialize CORS headers
 * @param {Object} server Restify server
 * @returns {undefined}
 */
function initCORS(server) {
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
    }
}

exports.databaseURI = databaseURI;
exports.connectDatabase = function (mongoose) {
    if (mongoose.connection.db) {
        return Promise.resolve(mongoose);
    } else {
        return mongoose.connect(databaseURI(), {
            useMongoClient: true
        });
    }
};
exports.bootstrap = bootstrap;
exports.initDatabase = initDatabase;
exports.initLogger = initLogging;
exports.initCORS = initCORS;