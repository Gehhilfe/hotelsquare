'use strict';

const mongoose = require('mongoose');
const Schema = mongoose.Schema;

// ---------------------------------------------------------------------------------------------------------------------
// Schema
// ---------------------------------------------------------------------------------------------------------------------

const ChatSchema = new Schema({
    participants: [{
        type: Schema.Types.ObjectId,
        ref: 'User'
    }],
    messages: [{
        type: Schema.Types.ObjectId,
        ref: 'Message'
    }]
});

// ---------------------------------------------------------------------------------------------------------------------
// Class
// ---------------------------------------------------------------------------------------------------------------------


class ChatClass {

    addMessage(msg) {
        this.messages.push(msg);
    }

}

ChatSchema.loadClass(ChatClass);
module.exports = mongoose.model('Chat', ChatSchema);