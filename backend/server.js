'use strict';
const os = require('os');
const restify = require('restify');
const errors = require('restify-errors');
const session = require('./app/routes/session');
const user = require('./app/routes/user');
const image = require('./app/routes/image');
const venue = require('./app/routes/venue');
const comment = require('./app/routes/comment');
const chat = require('./app/routes/chat');
const friend = require('./app/routes/friend');

const chatsocket = require('./app/routes/chatsocket');
const util = require('./lib/util');
const mongoose = require('mongoose');
const auth = require('./app/middleware/filter/authentication');
const fs = require('fs');


const Venue = require('./app/models/venue');
const Comments = require('./app/models/comments');
const Comment = Comments.Comment;

mongoose.Promise = global.Promise;

const server = restify.createServer();

const io = require('socket.io').listen(server);
chatsocket(io);

util.connectDatabase(mongoose).then(util.initDatabase);

server.use(restify.plugins.bodyParser({
    maxBodySize: 1024 * 1024,
    mapParams: true,
    mapFiles: true,
    overrideParams: false,
    keepExtensions: true,
    uploadDir: os.tmpdir(),
    multiples: true,
    hash: 'sha1'
}));

const bunyanLogger = util.initLogger(server);

/**
 * Handle promise rejection of route for async programming
 * @param {function(*,*,*)} func route handler
 * @returns {function(*=, *=, *=)} closure handling possible error
 */
function handlePromiseReject(func) {
    return (req, res, next) => {
        try {
            func(req, res, next);
        }catch(err) {
            return next(new errors.InternalServerError(err));
        }
    };
}

// Session
server.post('sessions', handlePromiseReject(session.postSession));

// User
server.get('users', auth, handlePromiseReject(user.profile));
server.get('users/:name', handlePromiseReject(user.profile));
server.get('users/id/:id', handlePromiseReject(user.profileByID));

server.post('users', handlePromiseReject(user.register));
server.post('users/:name/friend_requests', auth, handlePromiseReject(user.sendFriendRequest));
server.put('users', auth, handlePromiseReject(user.updateUser));
server.del('users', auth, handlePromiseReject(user.deleteUser));

server.get('profile', auth, handlePromiseReject(user.profile));

server.post('profile/avatar', auth, handlePromiseReject(user.uploadAvatar));
server.del('profile/avatar', auth, handlePromiseReject(user.deleteAvatar));

server.get('profile/friends', auth, handlePromiseReject(friend.getFriends));
server.get('profile/friends/:page', auth, handlePromiseReject(friend.getFriends));
server.del('profile/friends/:name', auth, handlePromiseReject(user.removeFriend));
server.put('profile/friend_requests/:name', auth, handlePromiseReject(user.confirmFriendRequest));

// Chat
server.get('chats/:chatId', auth, handlePromiseReject(chat.getConversation));
server.get('chats/:chatId/:page', auth, handlePromiseReject(chat.getConversation));
server.post('chats', auth, handlePromiseReject(chat.newChat));
server.post('chats/:chatId/messages', auth, handlePromiseReject(chat.replyMessage));
server.get('chats', auth, handlePromiseReject(chat.getConversations));

// Venue
server.get('venues/:id', handlePromiseReject(venue.getVenue));
server.put('venues/:id/checkin', auth, handlePromiseReject(venue.checkin));
server.get('venues/:id/comments', handlePromiseReject(comment.getComments(Venue)));
server.get('venues/:id/comments/:page', handlePromiseReject(comment.getComments(Venue)));

server.post('venues/:id/comments/text', auth, handlePromiseReject(comment.textComment(Venue)));
server.post('venues/:id/comments/image', auth, handlePromiseReject(comment.imageComment(Venue)));
// Comment

server.put('comments/:id/like', auth, handlePromiseReject(comment.like));
server.put('comments/:id/dislike', auth, handlePromiseReject(comment.dislike));

server.get('comments/:id/comments', handlePromiseReject(comment.getComments(Comment)));
server.get('comments/:id/comments/:page', handlePromiseReject(comment.getComments(Comment)));

server.post('comments/:id/comments/text', auth, handlePromiseReject(comment.textComment(Comment)));
server.post('comments/:id/comments/image', auth, handlePromiseReject(comment.imageComment(Comment)));
// server.del('comments/:id', auth, comment.delComment);
// server.post('comments', auth, comment.addComment);
// server.get('comments/:id', comment.getComment);

// Images
server.get('images/:id/:size/image.jpeg', image.getStat, restify.plugins.conditionalRequest(), image.getData);

// Search

// User
server.post('searches/users', auth, handlePromiseReject(user.search));

// Venue
server.post('searches/venues', handlePromiseReject(venue.queryVenue));
server.post('searches/venues/:page', handlePromiseReject(venue.queryVenue));

//Friends
server.post('searches/nearbyfriends', auth, handlePromiseReject(friend.getNearByFriends));

// Delete downloads
server.on('after', (request) => {
    if (request.files) {
        const key = Object.keys(request.files);
        key.forEach((k) => {
            fs.unlink(request.files[k].path, () => {
            });
        });
    }
});

server.listen(8081, function () {
    bunyanLogger.info('%s listening at %s', server.name, server.url);
});

module.exports = server;