module.exports = function (mongoose) {
    var Schema = mongoose.Schema,
        util = require('util'),
        User;

    User = new Schema({
        name: {
            type: String,
            required: true
        },
        password: String
    });
    return mongoose.model('User', User);
};