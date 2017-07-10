'use strict';

const mongoose = require('mongoose');
const Schema = mongoose.Schema;

// ---------------------------------------------------------------------------------------------------------------------
// Schema
// ---------------------------------------------------------------------------------------------------------------------

const options = {discriminatorKey: 'kind'};

const CommentSchema = new Schema({
    author: {
        type: Schema.Types.ObjectId,
        ref: 'User'
    },
    text: String,
    likes: {
        type: Number,
        default: 0
    },
    dislikes: {
        type: Number,
        default: 0
    },
    date: {
        type: Date,
        default: Date.now()
    },
    comments: [{
        type: Schema.Types.ObjectId,
        ref: 'TextComment'
    }]
}, options);

// ---------------------------------------------------------------------------------------------------------------------
// Class
// ---------------------------------------------------------------------------------------------------------------------

class CommentClass {

}

CommentSchema.loadClass(CommentClass);

const comment = mongoose.model('Comment', CommentSchema);

const TextCommentSchema = comment.discriminator('TextComment',
    new mongoose.Schema({
        comment: {
            type: Schema.Types.ObjectId, ref: 'TextComment'}
    }));

const ImageCommentSchema = comment.discriminator('ImageComment',
    new mongoose.Schema({image: {type: Schema.Types.ObjectId, ref: 'Image'}}));

const VenueCommentSchema = comment.discriminator('VenueComment',
    new mongoose.Schema({venue: {type: Schema.Types.ObjectId, ref: 'Venue'}}));


module.exports = comment;