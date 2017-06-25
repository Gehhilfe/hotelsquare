'use strict';

const mongoose = require('mongoose');
const Schema = mongoose.Schema;

// ---------------------------------------------------------------------------------------------------------------------
// Schema
// ---------------------------------------------------------------------------------------------------------------------

const MessageSchema = new Schema({
    sender: {
        type: Schema.Types.ObjectId,
        ref: 'User'
    },
    message: {
        type: String,
        required: true
    },
    date: {
        type: Date,
        default: Date.now()
    },
    chatId: {
        type: Schema.Types.ObjectId,
        required: true
    }
});

// ---------------------------------------------------------------------------------------------------------------------
// Class
// ---------------------------------------------------------------------------------------------------------------------


class MessageClass {

}

MessageSchema.loadClass(MessageClass);
module.exports = mongoose.model('Message', MessageSchema);