'use strict';

const _ = require('lodash');
const restify = require('restify');
const restify_errors = require('restify-errors');
const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const Image = require('./image');

// ---------------------------------------------------------------------------------------------------------------------
// Schema
// ---------------------------------------------------------------------------------------------------------------------

const options = {discriminatorKey: 'kind'};

const CommentSchema = new Schema(
    {
        assigned: {
            kind: String,
            to: {
                type: Schema.Types.ObjectId,
                refPath: 'assigned.kind'
            }
        },
        author: {
            type: Schema.Types.ObjectId,
            ref: 'User'
        },
        likes: [{
            type: Schema.Types.ObjectId,
            ref: 'User'
        }],
        dislikes: [{
            type: Schema.Types.ObjectId,
            ref: 'User'
        }],
        rating: {
            type: Number,
            default: 0
        },
        created_at: {
            type: Date,
            default: Date.now()
        },
        comments: [{
            kind: String,
            item: {
                type: Schema.Types.ObjectId,
                refPath: 'comments.kind'
            },
            created_at: Date
        }]
    },
    options);

CommentSchema.pre('save', (next) => {
    const self = this;
    if (self.likes === undefined)
        self.likes = [];
    if (self.dislikes === undefined)
        self.dislikes = [];
    self.rating = self.likes.length - self.dislikes.length;
    return next();
});

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
        if (!o.assigned.to.equals(this._id))
            return;
        if (_.indexOf(this.comments, o._id) === -1) {
            this.comments.push({
                item: o,
                kind: o.constructor.modelName,
                created_at: Date.now()
            });
            this.comments = _.reverse(_.sortBy(this.comments, 'created_at'));
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

    async toJSONDetails() {
        return {
            _id: this._id,
            assigned: {
                to: this.assigned.to,
                kind: this.assigned.kind
            },
            author: this.author.toJSONPublic(),
            date: this.date,
            rating: this.rating,
            comments_count: this.comments.length,
            comments: _.map(_.take(this.comments, 5), e => {
                return e.item.toJSONDetails();
            })
        };
    }
}

class TextCommentClass {

    static async build(user, text, assigned_to) {
        const self = this;
        const cmt = new self();
        cmt.author = user;
        if (assigned_to) {
            cmt.assignTo(assigned_to);
        }
        cmt.text = text;
        return await cmt.save();
    }

    toJSONDetails() {
        if (this.author && this.populated('author') === undefined)
            throw new restify_errors.InternalServerError('Author not populated!');
        return {
            _id: this._id,
            assigned: {
                to: this.assigned.to,
                kind: this.assigned.kind
            },
            author: this.author.toJSONPublic(),
            kind: 'TextComment',
            date: this.date,
            rating: this.rating,
            text: this.text,
            comments_count: this.comments.length,
            comments: _.map(_.take(this.comments, 5), e => {
                return e.item.toJSONDetails();
            })
        };
    }
}

class ImageCommentClass {

    static async build(user, path, assigned_to) {
        const self = this;
        const icmt = new self();
        icmt.author = user;
        icmt.image = await Image.upload(path, user);
        if (assigned_to) {
            icmt.assignTo(assigned_to);
        }
        await icmt.save();
        icmt.image.assignTo(icmt);
        await icmt.image.save();
        return icmt;
    }

    toJSONDetails() {
        if (this.author && this.populated('author') === undefined)
            throw new restify_errors.InternalServerError('Author not populated!');
        if (this.image && this.populated('image') === undefined)
            throw new restify_errors.InternalServerError('Author not populated!');
        return {
            _id: this._id,
            assigned: {
                to: this.assigned.to,
                kind: this.assigned.kind
            },
            author: this.author.toJSONPublic(),
            kind: 'ImageComment',
            date: this.date,
            rating: this.rating,
            image: this.image,
            comments_count: this.comments.length,
            comments: _.map(_.take(this.comments, 5), e => {
                return e.item.toJSONDetails();
            })
        };
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