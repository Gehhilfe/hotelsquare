const config = require('config');

var databaseURI = function() {
    return 'mongodb://' + config.get('db.host') + '/' + config.get('db.name');
};

exports.databaseURI = databaseURI;

exports.connectDatabase = function(mongoose) {
    if(mongoose.connection.db) {
        return Promise.resolve(mongoose);
    } else {
        return mongoose.connect(databaseURI());
    }
};