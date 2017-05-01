'use strict';

const mongoose = require('mongoose');
const Schema = mongoose.Schema;
const uuidV4 = require('uuid/v4');

// ---------------------------------------------------------------------------------------------------------------------
// Schema
// ---------------------------------------------------------------------------------------------------------------------

const AuthenticationTokenSchema = new Schema({
    owner: {
        type: Schema.ObjectId, 
        ref: 'User', 
        required: true
    },
    token: {
        type: String,
        default: function() {
            return uuidV4();
        },
        required: true,
        unique: true
    }
});

// ---------------------------------------------------------------------------------------------------------------------
// Statics
// ---------------------------------------------------------------------------------------------------------------------

// ---------------------------------------------------------------------------------------------------------------------
// Methods
// ---------------------------------------------------------------------------------------------------------------------

mongoose.model('AuthenticationToken', AuthenticationTokenSchema);