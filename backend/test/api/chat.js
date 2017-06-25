'use strict';
const config = require('config');
const mongoose = require('mongoose');
const chai = require('chai');
const chaiHttp = require('chai-http');
const server = require('../../server');
const Util = require('../../lib/util');
const Chat = require('../../app/models/chat');
const Message = require('../../app/models/message');
const User = require('../../app/models/user');
const jsonwt = require('jsonwebtoken');
const expect = chai.expect;
chai.should();
chai.use(chaiHttp);
chai.use(require('chai-things'));

const request = require('supertest');

describe('Chat', () => {

    let u;
    let other;
    let third;
    let token;
    let otherToken;
    let thirdToken;
    let chat;
    let otherchat;
    let message;
    let othermessage;

    beforeEach((done) => {
        Util.connectDatabase(mongoose).then(function () {
            User.remove({}).then(() => {
                User.create({name: 'peter', email: 'peter1@cool.de', password: 'peter99', gender: 'm'}).then((user) => {
                    u = user;
                    token = jsonwt.sign(u.toJSON(), config.jwt.secret, config.jwt.options);
                    User.create({name: 'peter2', email: 'peter2@cool.de', password: 'peter99', gender: 'f'}).then((user) => {
                        other = user;
                        otherToken = jsonwt.sign(other.toJSON(), config.jwt.secret, config.jwt.options);
                        User.create({name: 'peter3', email: 'peter3@cool.de', password: 'peter99', gender: 'unspecified'}).then((user) => {
                            third = user;
                            thirdToken = jsonwt.sign(third.toJSON(), config.jwt.secret, config.jwt.options);
                            return done();
                        });
                    });
                });
            });
        });
    });

    beforeEach((done) => {
        Util.connectDatabase(mongoose).then(function () {
            Chat.remove({}).then(() => {
                Chat.create({participants: [u, other]}).then((c) => {
                    chat = c;
                    Message.create({sender: u, message: 'first chat', date: Date.now(), chatId: chat._id}).then((msg) => {
                        message = msg;
                        Chat.create({participants: [u, other, third]}).then((c) => {
                            otherchat = c;
                            Message.create({sender: other, message: 'second chat', date: Date.now(), chatId: otherchat._id}).then((msg) => {
                                othermessage = msg;
                                return done();
                            });
                        });
                    });
                });
            });
        });
    });

    describe('GET chat', () => {
        it('should retrieve the respective chat history of the passed id', (done) => {
            request(server)
                .get('/chat/with/' + chat._id)
                .set('x-auth', token)
                .end((err, res) => {
                    res.should.have.status(200);
                    res.body.should.be.a('array');
                    res.body.length.should.be.eql(1);
                    res.body[0].sender.displayName.should.be.equal('peter');
                    res.body[0].message.should.be.equal('first chat');
                    return done();
                });
        });

        it('should respond with 404 if no chat is available', (done) => {
            request(server)
                .get('/chat/with/' + mongoose.Types.ObjectId())
                .set('x-auth', token)
                .end((err, res) => {
                    res.should.have.status(404);
                    return done();
                });
        });

    });

    describe('POST chat', () => {
        it('should start a chat with an initial message', (done) => {
            const chatdata = {
                message: 'test message'
            };
            request(server)
                .post('/chat/' + other._id)
                .set('x-auth', token)
                .send(chatdata)
                .end((err, res) => {
                    res.should.have.status(200);
                    res.body.should.have.property('message', 'New Chat');
                    res.body.should.have.property('chatId')
                    return done();
                });
        });

        it('should respond with error message if no participant set', (done) => {
            const chatdata = {
                message: 'test message'
            };
            request(server)
                .post('/chat/' + [])
                .set('x-auth', token)
                .send(chatdata)
                .end((err, res) => {
                    res.should.have.status(422);
                    return done();
                });
        });

        it('should respond with error message if no message set', (done) => {
            const chatdata = {
                message: ''
            };
            request(server)
                .post('/chat/' + [other._id, third._id])
                .set('x-auth', token)
                .send(chatdata)
                .end((err, res) => {
                    res.should.have.status(422);
                    return done();
                });
        });

    });

    describe('GET chat/all', () => {
        it('should return the chat between u and other', (done) => {
            request(server)
                .get('/chat/all')
                .set('x-auth', token)
                .end((err, res) => {
                    res.should.have.status(200);
                    console.log(res.body);
                    res.body.chats[0].length.should.be.eql(1);
                    res.body.chats.length.should.be.eql(2);
                    return done();
                });
        });
    });

    describe('POST chat/reply', () => {
        it('should reply to a message', (done) => {
            const chatdata = {
                chatId: chat._id,
                message: 'test reply'
            };
            request(server)
                .post('/chat/reply/' + chat._id)
                .set('x-auth', token)
                .send(chatdata)
                .end((err, res) => {
                console.log(res.body);
                    res.should.have.status(200);
                    res.body.should.have.property('message', 'replied to message');
                    return done();
                });
        });

    });
});