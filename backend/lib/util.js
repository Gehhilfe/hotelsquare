const config = require('config');

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

exports.databaseURI = databaseURI;

exports.connectDatabase = function(mongoose) {
    if(mongoose.connection.db) {
        return Promise.resolve(mongoose);
    } else {
        return mongoose.connect(databaseURI());
    }
};

exports.bootstrap = bootstrap;