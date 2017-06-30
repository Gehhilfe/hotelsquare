

exports = module.exports = function(io) {
    io.on('connection', (socket) => {
        socket.on('enter', (chat) => {
            socket.join(chat);
        });

        socket.on('leave', (chat) => {
            socket.leave(chat);
        });

        socket.on('new msg', (chat) => {
            io.sockets.in(chat).emit('refresh message', chat);
        });

        socket.on('disconnect', () => {

        });
    });
};
