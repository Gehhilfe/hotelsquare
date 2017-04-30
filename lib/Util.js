const config = require('config');

exports.databaseURI = function() {
    return 'mongodb://' + config.get('db.host') + '/' + config.get('db.name');
};