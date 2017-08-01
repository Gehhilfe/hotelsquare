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
const Image = require('../../app/models/image');
const jsonwt = require('jsonwebtoken');
chai.should();
chai.use(chaiHttp);
chai.use(require('chai-things'));

const request = require('supertest');

const mochaAsync = (fn) => {
    return (done) => {
        fn.call().then(done, (err) => {
            return done(err);
        });
    };
};


describe('Chat', () => {

    let u;
    let other;
    let third;
    let token;
    let chat;
    let otherchat;

    beforeEach(mochaAsync(async () => {
        mongoose.Promise = global.Promise;

        await Util.connectDatabase(mongoose);
        await Promise.all([
            User.remove({}),
            Chat.remove({}),
            Image.remove({}),
            Message.remove({})
        ]);

        const users = await Promise.all([
            User.create({name: 'peter', email: 'peter1@cool.de', password: 'peter99', gender: 'm'}),
            User.create({name: 'peter2', email: 'peter2@cool.de', password: 'peter99', gender: 'f'}),
            User.create({name: 'peter3', email: 'peter3@cool.de', password: 'peter99', gender: 'unspecified'})
        ]);

        u = users[0];
        u.avatar = await Image.create({});
        u = await u.save();
        token = jsonwt.sign(u.toJSON(), config.jwt.secret, config.jwt.options);
        other = users[0];
        third = users[0];

        chat = await Chat.create({participants: [u, other]});
        otherchat = await Chat.create({participants: [u, other, third]});
        const msgA = await Message.create({sender: u, message: 'first chat', date: Date.now(), chatId: chat._id});
        chat.addMessage(msgA);
        const msgB = await Message.create({sender: other, message: 'second chat', date: Date.now(), chatId: otherchat._id});
        otherchat.addMessage(msgB);
        await Promise.all([
            chat.save(),
            otherchat.save()
        ]);
    }));

    describe('GET chat', () => {
        it('should retrieve the respective chat history of the passed id', (done) => {
            request(server)
                .get('/chats/' + chat._id)
                .set('x-auth', token)
                .end((err, res) => {
                    res.should.have.status(200);
                    res.body.should.be.a('object');
                    res.body.messages.should.be.a('array');
                    res.body.participants.should.be.a('array');
                    res.body.participants[0].should.be.a('object');
                    res.body.participants[0].avatar.should.be.a('object')
                    res.body.messages[0].should.be.a('object');
                    res.body.messages.length.should.be.eql(1);
                    res.body.messages[0].message.should.be.equal('first chat');
                    return done();
                });
        });

        it('should respond with 404 if no chat is available', (done) => {
            request(server)
                .get('/chats/' + mongoose.Types.ObjectId())
                .set('x-auth', token)
                .end((err, res) => {
                    res.should.have.status(400);
                    return done();
                });
        });

    });

    describe('POST chat', () => {
        it('should start a chat with an initial message', (done) => {
            const chatdata = {
                recipients: [
                    other._id
                ],
                message: 'test message'
            };
            request(server)
                .post('/chats')
                .set('x-auth', token)
                .send(chatdata)
                .end((err, res) => {
                    res.should.have.status(200);
                    res.body.should.be.a('object');
                    res.body.messages.should.be.a('array');
                    res.body.messages[0].should.be.a('object');
                    return done();
                });
        });

        it('should respond with error message if no participant set', (done) => {
            const chatdata = {
                message: 'test message'
            };
            request(server)
                .post('/chats')
                .set('x-auth', token)
                .send(chatdata)
                .end((err, res) => {
                    res.should.have.status(400);
                    return done();
                });
        });

        it('should respond with error message if no message set', (done) => {
            const chatdata = {
                recipients: [other._id, third._id],
                message: ''
            };
            request(server)
                .post('/chats')
                .set('x-auth', token)
                .send(chatdata)
                .end((err, res) => {
                    res.should.have.status(400);
                    return done();
                });
        });

    });

    describe('GET chat', () => {
        it('should return the chat between u and other', (done) => {
            request(server)
                .get('/chats')
                .set('x-auth', token)
                .end((err, res) => {
                    res.should.have.status(200);
                    res.body.length.should.be.eql(2);
                    res.body[0].messages.length.should.be.eql(1);
                    return done();
                });
        });
    });

    describe('POST chat/:chatid/messages', () => {
        it('should reply to a message', (done) => {
            const chatdata = {
                chatId: chat._id,
                message: 'test reply'
            };
            request(server)
                .post('/chats/' + chat._id + '/messages')
                .set('x-auth', token)
                .send(chatdata)
                .end((err, res) => {
                    res.should.have.status(200);
                    return done();
                });
        });

    });
});