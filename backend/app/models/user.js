'use strict';

const mongoose = require('mongoose');
const restify = require('restify');
const restify_errors = require('restify-errors');
const Schema = mongoose.Schema;
const bcrypt = require('bcrypt');
const password_generate = require('generate-password');
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
        match: [/^[a-zA-Z][a-zA-Z0-9-_ ]*$/, 'Only a-z,A-Z,0-9,-,_ characters are allowed as name']
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
    },
    location: {
        'type': {type: String, default: 'Point'},
        coordinates: {type: [Number], default: [0, 0]}
    },
    avatar: {
        type: Schema.Types.ObjectId,
        ref: 'Image'
    },
    incognito: {
        type: Boolean,
        default: false
    },
    active: {
        type: Boolean,
        default: false
    },
    activation_key: String,
    age: Number,
    city: String
});

UserSchema.index({location: '2dsphere'});

UserSchema.pre('save', function (next) {
    const self = this;

    if (self.isModified())
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
            self.findOne({$or: [{displayName: name}, {email: name}, {name: name}], active: true}).then(function (res) {
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

    static async register(name, email, password) {
        const self = this;
        const user = await self.create({
            name: name,
            email: email,
            password: password,
            activation_key: password_generate.generate({
                length: 64,
                numbers: true,
                excludeSimilarCharacters: true
            })
        });
        return user;
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

        if (data.gender)
            self.gender = data.gender;

        if (data.password)
            self.password = data.password;

        if (data.location && data.location.coordinates)
            self.location.coordinates = data.location.coordinates;

        if (data.city)
            self.city = data.city;

        if (data.age)
            self.age = data.age;

        if (data.incognito)
            self.incognito = data.incognito;
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
        const obj = this.toObject({
            depopulate: true
        });
        delete obj.password;
        if (this.avatar) {
            if (this.populated('avatar') === undefined)
                throw new restify_errors.InternalServerError('User avatar not populated!');
            obj.avatar = this.avatar.toJSON();
        } else {
            delete obj.avatar;
        }
        return obj;
    }

    toJSONToken() {
        return {
            _id: this._id,
            name: this.name,
            displayName: this.displayName
        };
    }

    toJSONPublic() {
        const location = this.incognito ? null : this.location;
        let avatar = null;
        if (this.avatar) {
            if (this.populated('avatar') === undefined)
                throw new restify_errors.InternalServerError('User avatar not populated!');
            avatar = this.avatar.toJSON();
        }

        return {
            _id: this._id,
            name: this.name,
            displayName: this.displayName,
            friends_count: this.friends.length,
            avatar: avatar,
            city: this.city,
            age: this.age,
            gender: this.gender,
            location: location
        };
    }
}

UserSchema.loadClass(UserClass);
module.exports = mongoose.model('User', UserSchema);