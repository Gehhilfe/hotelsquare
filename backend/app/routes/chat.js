'use strict';
const _ = require('lodash');
const errors = require('restify-errors');
const User = require('../models/user');
const Chat = require('../models/chat');
const Message = require('../models/message');

/**
 * Starts new Chat
 *
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function newChat(request, response, next) {
    if (!request.body.recipients) {
        return next(new errors.BadRequestError('You must at least have one recipient for the message.'));
    }

    if (request.body.message === '') {
        return next(new errors.BadRequestError('You must not send empty messages.'));
    }


    const recipients = _.map(await User.find({
        _id: { $in: request.body.recipients }
    }), (it) => it._id);

    if(recipients.length !== request.body.recipients.length) {
        return next(new errors.BadRequestError('Unknown recipient.'));
    }

    const chat = await Chat.create({
        participants: [request.authentication._id, recipients]
    });

    const msg = await Message.create({
        chatId: chat._id,
        message: request.body.message,
        sender: request.authentication._id
    });

    return response.send(200, {
        message: msg,
        chatId: chat._id
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
function replyMessage(request, response, next) {
    const reply = new Message({
        chatId: request.params.chatId,
        message: request.body.message,
        sender: request.authentication._id,
        date: Date.now()
    });

    reply.save((err) => {
        if (err) {
            response.send({error: err});
            return next();
        }

        return response.send(200, {
            message: 'replied to message'
        });
    });
}

/**
 * Retrieves all conversations from the user
 *
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function getConversations(request, response, next) {
    Chat.find({
        participants: request.authentication._id
    })
        .select('_id')
        .exec((err, chats) => {
            if (err) {
                response.send({error: err});
                return next(err);
            }

            const allChats = [];
            chats.forEach((chat) => {

                Message.find({
                    chatId: chat._id
                })
                    .sort('-date')
                    .limit(1)
                    .populate({
                        path: 'sender',
                        select: 'displayName'
                    })
                    .exec((err, message) => {

                        if (err) {
                            response.send({error: err});
                            return next(err);
                        }
                        allChats.push(message);
                        if (allChats.length === chats.length) {
                            return response.send(200, {chats: allChats});
                        }
                    });
            });
        });
}

/**
 * Retrieves whole conversation history of a chat
 *
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function getConversation(request, response, next) {
    Message.find({
        chatId: request.params.chatId
    })
        .select('date message sender')
        .sort('-date')
        .populate({
            path: 'sender',
            select: 'displayName'
        })
        .exec((err, messages) => {
            if (err) {
                response.send({error: err});
                return next();
            }
            if (!messages.length) {
                return response.send(404, {message: 'no chat found'});
            }
            return response.send(200, messages);
        });
}

module.exports = {
    newChat,
    replyMessage,
    getConversation,
    getConversations
};