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
        required: [true, 'Please fill a name'],
        minlength: [4, 'Name needs to have at least 5 characters'],
        unique: true,
        match: [/^[a-zA-Z][a-zA-Z0-9-_]*$/, 'Only a-z,A-Z,0-9,-,_ characters are allowed as name']
    },
    email: {
        type: String,
        required: true,
        match: [/^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/, 'Please fill a valid email address']
    },
    password: {
        type: String,
        required: true,
        minlength: 6
    },
    friends: [{
        name: String
    }],
    friendRequests: [{
        name: String,
        created_at: {
            type: Date,
            default: Date.now
        }
    }]
});

UserSchema.pre('save', function (next) {
    const self = this;

    if (!self.isModified('password'))
        return next();


    bcrypt.hash(self.password, SALT_WORK_FACTOR).then((hash) => {
        self.password = hash;
        return next();
    }, (err) => {
        return next(new Error(err));
    });
});

// ---------------------------------------------------------------------------------------------------------------------
// Statics
// ---------------------------------------------------------------------------------------------------------------------

UserSchema.statics.login = function (name, password) {
    const self = this;
    if(password === undefined) {
        password = name.password;
        name = name.name;
    }
    return new Promise(function (resolve, reject) {
        self.findOne({$or: [{name: name}, {email: name}]}).then(function (res) {
            const foundUser = res;
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
    const self = this;
    return bcrypt.compare(candidatePassword, self.password, null);
};

UserSchema.methods.toJSON = function () {
    const obj = this.toObject();
    delete obj.password;
    return obj;
};

UserSchema.methods.toJSONPublic = function () {
    const obj = this.toObject();
    delete obj.password;
    delete obj.email;
    delete obj.friends;
    delete obj.friendRequests;
    return obj;
};

module.exports = mongoose.model('User', UserSchema);