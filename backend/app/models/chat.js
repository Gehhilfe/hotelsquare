'use strict';

const mongoose = require('mongoose');
const Schema = mongoose.Schema;

// ---------------------------------------------------------------------------------------------------------------------
// Schema
// ---------------------------------------------------------------------------------------------------------------------

const ChatSchema = new Schema({
    messages: [{
        type: String,
        date: {
            type: Date,
            default: Date.now()
        },
        meta: [{
            user: {
                type: Schema.Types.ObjectId,
                ref:'User'
            },
            read: Boolean,
            delivered: Boolean,
        }]
    }],
    sender: {
        type: Schema.Types.ObjectId,
        ref: 'User'
    },
    participants: [{
        user: {
            type: Schema.Types.ObjectId,
            ref: 'User'
        },
        read: Boolean,
        delivered: Boolean,

    }],
    is_group_message: {
        type: Boolean,
        default: false
    }
    //possible extensions: location where msg was sent, expiry date
});

// ---------------------------------------------------------------------------------------------------------------------
// Class
// ---------------------------------------------------------------------------------------------------------------------


class ChatClass {

}

ChatSchema.loadClass(ChatClass);
module.exports = mongoose.model('Chat', ChatSchema);