'use strict';


const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const UserSchema = new Schema({
    name: String,
    email: String,
    hashed_password: String
});

UserSchema.methods = {
    
};

mongoose.model('User', UserSchema);