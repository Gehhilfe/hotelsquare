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
    if(!request.params.recipients)
    {
        response.send(422, { error: 'You must at least have one recipient for the message.' });
        return next();
    }

    if(request.body.message === ''){
        response.send(422, { error: 'You must not send empty messages.' });
        return next();
    }

    //request.params.recipients.forEach((userid) => {
    //    User.count({ _id: userid }, (err, count) => {
    //        if(count < 1){
    //            response.send(422, { error: 'recipient not known' });
    //            return next();
    //        }
    //    })
    //})

    const chat = new Chat({
        participants: [request.authentication._id, request.params.recipients]
    });

    chat.save((err, chat) => {
        if(err) {
            response.send({ error: err });
            return next();
        }

        const msg = new Message({
            chatId: chat._id,
            message: request.body.message,
            sender: request.authentication._id
        });

        msg.save((err, new_msg) => {
            if(err){
                response.send({error: err});
                return next();
            }

            return response.send(200, {
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
        sender: request.authentication._id,
        date: Date.now()
    });

    reply.save((err, new_reply) => {
        if(err){
            response.send({error: err});
            return next();
        }

        return response.send(200, {
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
                    return response.send(200, { chats: allChats });
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
        .exec((err, messages) => {
            if (err) {
                response.send({error: err});
                return next();
            }
            if(!messages.length){
                return response.send(404, { message: 'no chat found'});
            }
            return response.send(200, messages);
        });
};

module.exports = {
    newChat,
    replyMessage,
    getConversation,
    getConversations
};