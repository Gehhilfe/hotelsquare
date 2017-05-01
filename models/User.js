'use strict';


const mongoose = require('mongoose');
const Schema = mongoose.Schema;
const bcrypt = require('bcrypt');
const SALT_WORK_FACTOR = 10;

// ---------------------------------------------------------------------------------------------------------------------
// Schema
// ---------------------------------------------------------------------------------------------------------------------

const UserSchema = new Schema({
    name: {
        type: String,
        required: true,
        minlength: 4
    },
    email: {
        type: String,
        required: true,
        match: [/^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/, 'Please fill a valid email address']
    },
    password: {
        type: String,
        validate: {
            validator: function (v) {
                var user = this;
                if (!user.isModified('password'))
                    return true;
                return v.length >= 6;
            }
        }
    }
});

UserSchema.pre('save', function (next) {
    var user = this;

    if (!user.isModified('password'))
        return next();


    bcrypt.hash(user.password, SALT_WORK_FACTOR).then(function (hash) {
        user.password = hash;
        return next();
    }, function (err) {
        return next(err);
    });
});

// ---------------------------------------------------------------------------------------------------------------------
// Statics
// ---------------------------------------------------------------------------------------------------------------------

UserSchema.statics.login = function (name, password) {
    var User = this;
    return new Promise(function (resolve, reject) {
        User.findOne({name: name}).then(function (res) {
            var foundUser = res;
            if (res === null)
                return reject();
            return foundUser.comparePassword(password).then(function (res) {
                if (res)
                    return resolve(foundUser);
                else
                    return reject();
            }, reject);
        }, reject);
    });
};

// ---------------------------------------------------------------------------------------------------------------------
// Methods
// ---------------------------------------------------------------------------------------------------------------------


UserSchema.methods.comparePassword = function (candidatePassword) {
    var user = this;
    return bcrypt.compare(candidatePassword, user.password, null);
};

mongoose.model('User', UserSchema);