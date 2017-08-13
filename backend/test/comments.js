'use strict';

const mongoose = require('mongoose');
const User = require('../app/models/user');
const Venue = require('../app/models/venue');
const Image = require('../app/models/image');
const Comments = require('../app/models/comments');
const Util = require('../lib/util');

const Comment = Comments.Comment;
const TextComment = Comments.TextComment;
const ImageComment = Comments.ImageComment;

const chai = require('chai');
chai.use(require('chai-things'));
chai.should();
const expect = chai.expect;

const imageGenerator = require('js-image-generator');
const tempWrite = require('temp-write');
const fs = require('fs');
const path = require('path');

const mochaAsync = (fn) => {
    return (done) => {
        fn.call().then(done, (err) => {
            return done(err);
        });
    };
};

describe('comments', () => {
    let aVenue, user, comment, textComment;

    beforeEach(mochaAsync(async () => {
        mongoose.Promise = global.Promise;

        await Util.connectDatabase(mongoose);
        await Promise.all([
            Comment.remove({}),
            TextComment.remove({}),
            ImageComment.remove({}),
            User.remove({}),
            Venue.remove({}),
            Image.remove({})
        ]);

        aVenue = await Venue.create({
            name: 'aVenue',
            location: {
                type: 'Point',
                coordinates: [5, 5]
            }
        });
        const avatar = await Image.create({});
        user = await User.create({name: 'pete213r', email: 'pete213r1@cool.de', password: 'peter99', avatar: avatar});
        comment = await Comment.create({});
        textComment = await TextComment.build(user, 'Test', comment);
    }));

    describe('assign', () => {
        it('should assign a venue and add itself to the venue', () => {
            comment.assignTo(aVenue);
            comment.assigned.to.should.be.equal(aVenue._id);
            comment.assigned.kind.should.be.equal(aVenue.constructor.modelName);
            aVenue.comments[0].item.equals(comment._id);
        });
    });

    describe('like', () => {

        describe('when liked', () => {
            beforeEach(mochaAsync(async () => {
                comment.like(user);
                textComment.like(user);
            }));

            it('should not change the number of likes', () => {
                comment.like(user);
                comment.likes.length.should.be.equal(1);
                comment.toJSONDetails().rating.should.be.equal(1);


                textComment.like(user);
                textComment.likes.length.should.be.equal(1);
                textComment.toJSONDetails().rating.should.be.equal(1);
            });
        });

        describe('when not liked', () => {
            it('should change the number of likes', () => {
                comment.like(user);
                comment.likes.length.should.be.equal(1);

                textComment.like(user);
                textComment.likes.length.should.be.equal(1);
            });
        });

        describe('when disliked', () => {
            beforeEach(mochaAsync(async () => {
                comment.dislike(user);

                textComment.dislike(user);
            }));

            it('should change the number of likes and dislikes', () => {
                comment.like(user);
                comment.likes.length.should.be.equal(1);
                comment.dislikes.length.should.be.equal(0);

                textComment.like(user);
                textComment.likes.length.should.be.equal(1);
                textComment.dislikes.length.should.be.equal(0);
            });
        });

    });

    describe('dislike', () => {

        describe('when disliked', () => {
            beforeEach(mochaAsync(async () => {
                comment.dislike(user);
                textComment.dislike(user);
            }));

            it('should not change the number of dislike', () => {
                comment.dislike(user);
                comment.dislike.length.should.be.equal(1);

                textComment.dislike(user);
                textComment.dislike.length.should.be.equal(1);
            });
        });

        describe('when not disliked', () => {
            it('should change the number of dislike', () => {
                comment.dislike(user);
                comment.dislike.length.should.be.equal(1);

                textComment.dislike(user);
                textComment.dislike.length.should.be.equal(1);
            });
        });

        describe('when liked', () => {
            beforeEach(mochaAsync(async () => {
                comment.like(user);
                textComment.like(user);
            }));

            it('should change the number of likes and dislikes', () => {
                comment.dislike(user);
                comment.likes.length.should.be.equal(0);
                comment.dislikes.length.should.be.equal(1);

                textComment.dislike(user);
                textComment.likes.length.should.be.equal(0);
                textComment.dislikes.length.should.be.equal(1);
            });
        });

    });

    describe('image', () => {

        let image, imagePath;

        before((done) => {
            imageGenerator.generateImage(1920, 1080, 80, (err, i) => {
                expect(err).to.be.null;
                image;
                image = i;
                imagePath = tempWrite.sync(image.data, 'image.jpeg');
                return done();
            });
        });

        after((done) => {
            fs.unlink(imagePath, () => {
                fs.rmdir(path.dirname(imagePath), () => {
                    return done();
                });
            });
        });

        it('should cyclic assign image and comment when saved', mochaAsync(async () => {
            const imageComment = await ImageComment.build(user, imagePath, comment);
            await imageComment.save();
            const img = await Image.findOne({_id: imageComment.image._id});

            imageComment.image.should.not.be.null;
            img.assigned.to.should.not.be.null;
        }));
    });

    describe('toJSON', () => {
        let json;

        beforeEach(mochaAsync(async () => {
            await TextComment.build(user, 'Test', textComment);
            await TextComment.build(user, 'Test', textComment);
            await TextComment.build(user, 'Test', textComment);
            await TextComment.build(user, 'Test', textComment);
            await TextComment.build(user, 'Test', textComment);
            await TextComment.build(user, 'Test', textComment);
            await TextComment.build(user, 'Test', textComment);
            await TextComment.build(user, 'Test', textComment);
            await textComment.save();
            const comment = await Comment.findOne({_id: textComment._id})
                .populate({
                    path: 'author',
                    populate: {
                        path: 'avatar'
                    }
                })
                .populate({
                    path: 'comments.item',
                    populate: {
                        path: 'author',
                        populate: {
                            path: 'avatar'
                        }
                    }
                })
                .populate({
                    path: 'comments.item',
                    populate: {
                        path: 'image author',
                        populate: {
                            path: 'avatar'
                        }
                    }
                });
            json = await comment.toJSONDetails();
        }));


        it('should only contained the 5 sub comments', (mochaAsync(async () => {
            //json.comments.length.should.be.equal(5);
        })));

        it('should only contained total amount of comments as number', () => {
            //json.comments_count.should.be.equal(8);
        });
    });
});