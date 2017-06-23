'use strict';

const restify = require('restify');
const sio = require('socket.io');
const Chat = require('../models/chat');

/**
 * Starts new Chat
 *
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function newChat(request, response, next){
    if(request.body.participants.length < 1)
    {
        response.status(422).send({error: 'You must at least have one recipient for the message.'});
        return next();
    }

    if(request.body.messages.length < 1){
        response.status(422).send({error: 'You must send at least one message.'});
        return next();
    }

    if(request.body.messages[0].message = ''){
        response.status(422).send({error: 'You must not send empty messages.'});
        return next();
    }

    const chat = new Chat({
        sender: request.authentication._id,
        messages: request.body.messages,
        participants: request.body.participants,
        is_group_message: request.body.is_group_message
    });

    chat.save((err, chat) => {
        if(err) {
            response.send({error: err});
            return next(err);
        }

        return response.status(200).json({message: 'Chat created', chat._id});
    });
}

/**
 * Replies to a message in a chat
 *
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function replyMessage(request, response, next){
    const chat = await Chat.findOne({request.authentication._id);

}

const results = await Promise.all([
    User.findOne({name: request.params.name, 'friend_requests.sender': { $in: [request.authentication._id]}}),
    User.findOne({name: request.params.name, friends: { $in: [request.authentication._id]}})
]);
if (results[0] === null && results[1] == null) {
    const res = await User.findOne({name: request.params.name});
    res.addFriendRequest(request.authentication);
    await res.save();
    response.json(res);
    return next();
}
return response.send(400, {error: 'Could not send friend request'});

/**
 * Retrieves all conversations from the user
 *
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function getConversations(request, response, next){
    const chats = await Chat.find({ participants: request.authentication._id })
        .select('_id')
        .exec((err, chats) => {
        if(err){
            response.send({error: err});
            return next(err);
        }
            return reponse.status(200).json({})
        })


)
}

/**
 * Retrieves whole conversation history of a chat
 *
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function getConversation(){

}

/**
 * Confirms or declines a friend request
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function confirmFriendRequest(request, response, next) {
    // Find both users
    const results = await Promise.all([
        User.findOne({name: request.authentication.name}),
        User.findOne({name: request.params.name})
    ]);

    const receiver = results[0];
    const sender = results[1];

    // Check if a friend requests exists
    const friendRequest = receiver.friend_requests.find(((e) => {
        return e.sender.equals(sender._id);
    }));

    if (friendRequest === undefined) {
        response.send(400, {error: 'No friend request existing'});
        return next();
    }

    // Remove friend request
    receiver.removeFriendRequest(friendRequest);

    if (request.body.accept) {
        // Request accepted
        User.connectFriends(sender, receiver);
        await Promise.all([
            sender.save(),
            receiver.save()
        ]);
        response.json({message: 'Friend request accepted'});
        return next();
    } else {
        // Request declined
        await receiver.save();
        response.json({message: 'Friend request declined'});
        return next();
    }
}

module.exports = {
    newChat,
    replyMessage,
    getConversation,
    getConversations
};