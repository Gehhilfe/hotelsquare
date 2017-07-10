'use strict';

const _ = require('lodash');

const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const Image = require('./image');

// ---------------------------------------------------------------------------------------------------------------------
// Schema
// ---------------------------------------------------------------------------------------------------------------------

const options = {discriminatorKey: 'kind'};

const CommentSchema = new Schema({
    assigned: {
        kind: String,
        to: {
            type: Schema.Types.ObjectId,
            refPath: 'assigned.kind'
        }
    },
    likes: [{
        type: Schema.Types.ObjectId,
        ref: 'User'
    }],
    dislikes: [{
        type: Schema.Types.ObjectId,
        ref: 'User'
    }],
    date: {
        type: Date,
        default: Date.now()
    },
    comments: [{
        type: Schema.Types.ObjectId,
        ref: 'Comment'
    }]
}, options);

// ---------------------------------------------------------------------------------------------------------------------
// Class
// ---------------------------------------------------------------------------------------------------------------------

class CommentClass {

    /**
     * Assign comment to an instance so basically everything can be commented
     *
     * @param {Object} instance Instance to assign this comment e.g. Venue, User, Image or other comments
     * @returns {undefined}
     */
    assignTo(instance) {
        this.assigned.to = instance;
        this.assigned.kind = instance.constructor.modelName;
        instance.addComment(this);
    }

    /**
     * Adds a comment o to this instance
     *
     * @param {Comment} o some comment
     * @returns {undefined}
     */
    addComment(o) {
        if (_.indexOf(this.comments, o._id) === -1) {
            this.comments.push(o);
        }
    }

    /**
     * Adds user to likes if possible
     *
     * @param {User} user User that likes this comment
     * @returns {undefined}
     */
    like(user) {
        if (_.indexOf(this.likes, user._id) === -1) {
            this.likes.push(user);
        }
        this.dislikes.pull(user);
    }

    /**
     * Adds user to dislikes if possible
     *
     * @param {User} user User that dislikes this comment
     * @returns {undefined}
     */
    dislike(user) {
        if (_.indexOf(this.dislikes, user._id) === -1) {
            this.dislikes.push(user);
        }
        this.likes.pull(user);
    }
}

class TextCommentClass {

    static async build(user, text, assigned_to) {
        const self = this;
        const cmt = new self();
        if (assigned_to) {
            cmt.assignTo(assigned_to);
        }
        cmt.text = text;
        return await cmt.save();
    }
}

class ImageCommentClass {

    static async build(user, path, assigned_to) {
        const self = this;
        const icmt = new self();
        if (assigned_to) {
            icmt.assignTo(assigned_to);
        }
        icmt.image = await Image.upload(path, user);
        await icmt.save();
        icmt.image.assignTo(icmt);
        await icmt.image.save();
        return icmt;
    }

}

CommentSchema.loadClass(CommentClass);

const Comment = mongoose.model('Comment', CommentSchema);


const textCommentSchema = new mongoose.Schema({
    text: String
}, options);
textCommentSchema.loadClass(TextCommentClass);

const TextComment = Comment.discriminator('TextComment', textCommentSchema);

const imageCommentSchema = new mongoose.Schema({
    image: {
        type: Schema.Types.ObjectId,
        ref: 'Image'
    }
}, options);
imageCommentSchema.loadClass(ImageCommentClass);

const ImageComment = Comment.discriminator('ImageComment', imageCommentSchema);


module.exports = {
    Comment,
    TextComment,
    ImageComment
};