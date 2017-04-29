'use strict';


const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const UserSchema = new Schema({
    name: {
        type: String,
        required: true
    },
    email: String,
    hashed_password: String
});

UserSchema.methods = {

};

mongoose.model('User', UserSchema);