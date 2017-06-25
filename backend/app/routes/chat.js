/* eslint-disable */
//@Robert
// When you are done just remove eslint-disable
// This file blocked merge of api branch

'use strict';

const restify = require('restify');
const Chat = require('../models/chat');
const Message = require('../models/message');
const User = require('../models/user');

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

    if(request.body.message.message = ''){
        response.status(422).send({error: 'You must not send empty messages.'});
        return next();
    }

    const chat = new Chat({
        participants: [request.authentication._id, request.body.recipients]
    });

    chat.save((err, chat) => {
        if(err) {
            response.send({error: err});
            return next(err);
        }

        const msg = new Message({
            chatId: chat._id,
            message: request.body.message,
            sender: request.authentication._id
        });

        msg.save((err, new_msg) => {
            if(err){
                response.send({error: err});
                return next(err);
            }

            return response.status(200).json({
                message: 'New Chat',
                chatId: chat._id
            });
        });
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
function replyMessage(request, response, next){
    const reply = new Message({
        chatId: request.params.chatId,
        message: request.body.message,
        sender: request.authentication._id
    });

    reply.save((err, new_reply) => {
        if(err){
            response.send({error: err});
            return next(err);
        }

        return res.status(200).json({
            message: 'replied to message'
        });
    })
}

/**
 * Retrieves all conversations from the user
 *
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function getConversations(request, response, next){
    Chat.find({
        participants: request.authentication._id
    })
        .select('_id')
        .exec((err, chats) => {
        if(err) {
            response.send({ error: err });
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
                if(err){
                    response.send({ error: err });
                    return next(err);
                }
                allChats.push(message);
                if(allChats.length === chats.length){
                    return response.status(200).json({
                        chats: allChats
                    });
                }
                });
        });
        });
};

/**
 * Retrieves whole conversation history of a chat
 *
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
function getConversation(request, response, next){
    Message.find({
        chatId: request.params.chatId
    })
        .select('date message sender')
        .sort('-date')
        .populate({
            path: 'sender',
            select: 'displayName'
        })
        .exec((err, message) => {
        if(err){
            response.send({error: err });
            return next(err);
        }

        return response.status(200).json({
            chat: messages
        });
        });
};

module.exports = {
    newChat,
    replyMessage,
    getConversation,
    getConversations
};