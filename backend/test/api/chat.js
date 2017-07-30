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
            Chat.remove({})
        ]);

        const users = await Promise.all([
            User.create({name: 'peter', email: 'peter1@cool.de', password: 'peter99', gender: 'm'}),
            User.create({name: 'peter2', email: 'peter2@cool.de', password: 'peter99', gender: 'f'}),
            User.create({name: 'peter3', email: 'peter3@cool.de', password: 'peter99', gender: 'unspecified'})
        ]);

        u = users[0];
        token = jsonwt.sign(u.toJSON(), config.jwt.secret, config.jwt.options);
        other = users[0];
        third = users[0];

        chat = await Chat.create({participants: [u, other]});
        otherchat = await Chat.create({participants: [u, other, third]});
        await Message.create({sender: u, message: 'first chat', date: Date.now(), chatId: chat._id});
        await Message.create({sender: other, message: 'second chat', date: Date.now(), chatId: otherchat._id});
    }));

    describe('GET chat', () => {
        it('should retrieve the respective chat history of the passed id', (done) => {
            request(server)
                .get('/chats/' + chat._id)
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
                .get('/chats/' + mongoose.Types.ObjectId())
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
                    res.body.chats[0].length.should.be.eql(1);
                    res.body.chats.length.should.be.eql(2);
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
                    res.body.should.have.property('message', 'replied to message');
                    return done();
                });
        });

    });
});