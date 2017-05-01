const config = require('config');

var databaseURI = function() {
    return 'mongodb://' + config.get('db.host') + '/' + config.get('db.name');
};

exports.databaseURI = databaseURI;

exports.connectDatabase = function(mongoose) {
    return mongoose.connect(databaseURI());
}