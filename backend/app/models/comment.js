'use strict';

const mongoose = require('mongoose');
const Schema = mongoose.Schema;

// ---------------------------------------------------------------------------------------------------------------------
// Schema
// ---------------------------------------------------------------------------------------------------------------------

const CommentSchema = new Schema({
    author: {
        type: Schema.Types.ObjectId,
        ref: 'User',
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
    }
});

const ImageCommentSchema = CommentSchema.extend({
    image: {
        type: Schema.Types.ObjectId,
        ref: 'Image'
    }
});

const CommentCommentSchema = CommentSchema.extend({
    comment: {
        type: Schema.Types.ObjectId,
        ref: 'Comment'
    }
});

const VenueCommentSchema = CommentSchema.extend({
    venue: {
        type: Schema.Types.ObjectId,
        ref: 'Venue'
    }
});

// ---------------------------------------------------------------------------------------------------------------------
// Class
// ---------------------------------------------------------------------------------------------------------------------

class CommentClass {

}

class CommentCommentClass {

}

class ImageCommentClass {

}

class VenueCommentClass {

}

CommentSchema.loadClass(CommentClass);
CommentCommentSchema.loadClass(CommentCommentClass);
ImageCommentSchema.loadClass(ImageCommentClass);
VenueCommentSchema.loadClass(VenueCommentClass);
module.exports = mongoose.model('Comment', CommentSchema);
module.exports = mongoose.model('CommentComment', CommentCommentSchema);
module.exports = mongoose.model('ImageComment', ImageCommentSchema);
module.exports = mongoose.model('VenueComment', VenueCommentSchema);