'use strict';

const restify = require('restify');
const session = require('./app/routes/session');
const util = require('./lib/util');
const mongoose = require('mongoose');
mongoose.Promise = global.Promise;

var server = restify.createServer();

util.connectDatabase(mongoose);
var db = mongoose.connection;
db.on('error', console.error.bind(console, 'connection error:'));
server.use(restify.bodyParser({ mapParams: true }));
server.post('/session', session.postSession);

server.listen(8081, function() {
    console.log('%s listening at %s', server.name, server.url);
});

module.exports = server;
