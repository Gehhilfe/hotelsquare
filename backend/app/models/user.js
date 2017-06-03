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
    displayName: String,
    email: {
        type: String,
        required: true,
        unique: true,
        match: [/^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/, 'Please fill a valid email address']
    },
    password: {
        type: String,
        required: true,
        minlength: [6, 'Please fill a password with minimum length of 6 characters']
    },
    friends: [{
        type: Schema.Types.ObjectId,
        ref: 'User'
    }],
    friend_requests: [{
        sender: {
            type: Schema.Types.ObjectId,
            ref: 'User'
        },
        created_at: {
            type: Date,
            default: Date.now
        }
    }],
    gender: {
        type: String,
        enum: ['m', 'f', 'unspecified'],
        default: 'unspecified'
    },
    updated_at: {
        type: Date,
        default: Date.now()
    }
});

UserSchema.pre('save', function (next) {
    const self = this;

    if(self.isModified())
        self.updated_at = Date.now();

    if (self.isModified('name')) {
        self.displayName = self.name;
        self.name = self.name.toLowerCase();
    }

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
// Class
// ---------------------------------------------------------------------------------------------------------------------


class UserClass {

    static login(name, password) {
        const self = this;
        if (password === undefined) {
            password = name.password;
            name = name.name;
        }
        return new Promise(function (resolve, reject) {
            self.findOne({$or: [{displayName: name}, {email: name}, {name: name}]}).then(function (res) {
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
    }

    /**
     * Connects the friendship between two users
     *
     * @param {User} o1 One part of the friendship
     * @param {User} o2 Other part of the friendship
     * @returns {Array} Changed objects
     */
    static connectFriends(o1, o2) {
        o1.addFriend(o2);
        o2.addFriend(o1);
        return [o1, o2];
    }

    update(data) {
        const self = this;

        if(data.gender)
            self.gender = data.gender;

        if(data.password)
            self.password = data.password;
    }

    comparePassword(candidatePassword) {
        const self = this;
        return bcrypt.compare(candidatePassword, self.password, null);
    }

    addFriendRequest(sender) {
        const self = this;
        self.friend_requests.push({
            sender: sender
        });
    }

    addFriend(other) {
        const self = this;
        self.friends.push(other);
    }

    removeFriend(other) {
        const self = this;
        self.friends.pull(other);
    }

    removeFriendRequest(friendRequest) {
        const self = this;

        self.friend_requests.pull(friendRequest);

        return [self];
    }

    toJSON() {
        const obj = this.toObject();
        delete obj.password;
        return obj;
    }

    toJSONPublic() {
        return {
            _id: this._id,
            name: this.name
        };
    }
}

UserSchema.loadClass(UserClass);
module.exports = mongoose.model('User', UserSchema);