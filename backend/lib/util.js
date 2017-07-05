const _ = require('lodash');
const config = require('config');
const User = require('./../app/models/user');

const databaseURI = function() {
    return 'mongodb://' + config.get('db.host') + '/' + config.get('db.name');
};

/**
 * Bootstrap a mongoose model with a given entities
 * @param {Object} model  mongoose model
 * @param {Array} data  entities
 * @returns {Promise} Promise of all entities are created
 */
function bootstrap(model, data) {
    if(data === undefined || data === null || !(data instanceof Array))
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
    if(data === undefined || data === null || !(data instanceof Array))
        return Promise.reject('No data provided');
    return new Promise((resolve, reject) => {
        Promise.all(_.map(data, async (e) => {
            const a = await User.findOne({displayName: e.a });
            const b = await User.findOne({displayName: e.b });
            User.connectFriends(a, b);
            await a.save();
            await b.save();
        })).then(resolve).catch(reject);
    });
}

/**
 * Boostrap friend requests for user model
 * @param {Array} data Friend request entry.from to entry.to
 * @returns {Promise}  Promise of all connection are created
 */
function bootstrapFriendRequets(data) {
    if(data === undefined || data === null || !(data instanceof Array))
        return Promise.reject('No data provided');
    return new Promise((resolve, reject) => {
        Promise.all(_.map(data, async (e) => {
            const from = await User.findOne({displayName: e.from });
            const to = await User.findOne({displayName: e.to });
            to.addFriendRequest(from);
            await to.save();
        })).then(resolve).catch(reject);
    });
}

exports.bootstrapFriends = bootstrapFriends;
exports.bootstrapFriendRequets = bootstrapFriendRequets;
exports.databaseURI = databaseURI;

exports.connectDatabase = function(mongoose) {
    if(mongoose.connection.db) {
        return Promise.resolve(mongoose);
    } else {
        return mongoose.connect(databaseURI());
    }
};

exports.bootstrap = bootstrap;