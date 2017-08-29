'use strict';

const config = require('config');
const restify = require('restify');
const restify_errors = require('restify-errors');
const password_generate = require('generate-password');
const nodemailer = require('nodemailer');

const User = require('../models/user');
const Image = require('../models/image');

const ValidationError = require('../errors/ValidationError');

var {FB, FacebookApiException} = require('fb');

const handleValidation = (next, func) => {
    func().catch((error) => {
        switch (error.name) {
        case 'MongoError':
            if (error.errmsg.includes('name')) {
                return next(new ValidationError({
                    name: {
                        message: 'Name is already taken'
                    }
                }));
            }
            if (error.errmsg.includes('email')) {
                return next(new ValidationError({
                    email: {
                        message: 'Email is already used'
                    }
                }));
            }
            return next();

        case 'ValidationError':
            return next(new ValidationError(error.errors));
        }
    });
};

/**
 * Search for user by name and filters results by gender when gender is provided.
 *
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function search(request, response, next) {
    let query = User.find({ name: new RegExp(request.body.name, 'i'), _id: {$ne: request.authentication._id}, active: true, deleted: false  }).populate('avatar');
    if(request.body.gender)
        query = query.where('gender').equals(request.body.gender);
    let result = await query;
    result = result.map((user) => {
        const obj = user.toJSONPublic();
        obj.type = 'user';
        return obj;
    });
    response.send(result);
    return next();
}

/**
 * Retrieves user profile
 *
 * @function profile
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function profile(request, response, next) {
    let selfRequest = false;

    // When no name provided use authenticated user
    if (request.params.name === undefined) {
        request.params.name = request.authentication.name;
        selfRequest = true;
    }

    let user = await User.findOne({name: request.params.name, active: true, deleted: false}).populate('avatar').exec();
    if (user === null)
        return next(new restify_errors.NotFoundError());
    if (!selfRequest) {
        // Remove sensitive information
        user = user.toJSONPublic();
    }
    response.send(user);
    return next();
}

/**
 * Retrieves user profile by id
 *
 * @function profile
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function profileByID(request, response, next) {
    const user = await User.findOne({_id: request.params.id, active: true}).populate('avatar');
    response.send(user.toJSONPublic());
    return next();
}

/**
 * Retrieves user profile information
 *
 * @function updateUser
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function updateUser(request, response, next) {
    handleValidation(next, async () => {
        let user = await User.findOne({_id: request.authentication._id, active: true, deleted: false}).populate('avatar');
        user.update(request.body);
        user = await user.save();

        response.send(user);
        return next();
    });
}

/**
 * Registers a new user with the given profile information.
 *
 * @function register
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function register(request, response, next) {
    handleValidation(next, async () => {
        const user = await User.register(request.body.name, request.body.email, request.body.password);

        if(config.email) {
            const transporter = nodemailer.createTransport({
                host: config.email.server,
                port: 587,
                secure: false,
                requireTLS: true,
                auth: {
                    user: config.email.mail,
                    pass: config.email.password
                }
            });

            const mailOptions = {
                from: '"HOTELSQUARE Mailer" <'+config.email.mail+'>',
                to: user.email,
                subject: 'Welcome to HOTELSQUARE',
                text: 'Hello '+user.displayName+', have fun using HOTELSQUARE! But before you start please confirm your email address by clicking on this link https://dev.ip.stimi.ovh/emailConfirmation?key='+user.activation_key+'&id='+user._id.toString(),
                html: '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"\n' +
                '        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">\n' +
                '<html xmlns="http://www.w3.org/1999/xhtml">\n' +
                '<head>\n' +
                '    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>\n' +
                '    <title>E-Mail Template</title>\n' +
                '    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>\n' +
                '    <link href="https://fonts.googleapis.com/css?family=Pacifico" rel="stylesheet">\n' +
                '    <style>\n' +
                '        body {\n' +
                '            font-family: Arial;\n' +
                '            margin: 0;\n' +
                '            padding: 0;\n' +
                '            /*background-color: #fafafa;*/\n' +
                '        }\n' +
                '\n' +
                '        #logo {\n' +
                '            color: #2fb7e6;\n' +
                '            font-family: \'Pacifico\', cursive;\n' +
                '            font-size: 5em;\n' +
                '            text-align: center;\n' +
                '            padding-bottom: .5em;\n' +
                '        }\n' +
                '\n' +
                '        a {\n' +
                '            background-color: #2fb7e6;\n' +
                '            color: #fff;\n' +
                '            text-decoration: none;\n' +
                '            padding: 0 2rem;\n' +
                '            text-transform: uppercase;\n' +
                '            line-height: 36px;\n' +
                '            height: 36px;\n' +
                '            border-radius: 2px;\n' +
                '            display: inline-block;\n' +
                '        }\n' +
                '\n' +
                '        #contentTable {\n' +
                '            background-color: #fff;\n' +
                '            padding: 50px;\n' +
                '            margin: 50px;\n' +
                '            border: 1px solid #dddddd;\n' +
                '            border-bottom: 2px solid #dddddd;\n' +
                '        }\n' +
                '\n' +
                '        #trenner {\n' +
                '            height: 1px;\n' +
                '            line-height: 1px;\n' +
                '            width: 100%;\n' +
                '            background-color: #dddddd;\n' +
                '            margin: 40px 0;\n' +
                '        }\n' +
                '\n' +
                '        .input_label {\n' +
                '            color: #2fb7e6;\n' +
                '            font-size: .75rem;\n' +
                '            padding-top: 15px;\n' +
                '        }\n' +
                '\n' +
                '        .input_text {\n' +
                '            border-bottom: 1px solid #2fb7e6;\n' +
                '            padding: 5px 0;\n' +
                '        }\n' +
                '    </style>\n' +
                '</head>\n' +
                '<body style="">\n' +
                '<table border="0" cellpadding="0" cellspacing="0" width="100%" style="background-color: #fafafa;">\n' +
                '    <tr>\n' +
                '        <td style="width:50%;"></td>\n' +
                '        <td>\n' +
                '            <table id="contentTable" border="0" cellpadding="0" cellspacing="0">\n' +
                '                <tr>\n' +
                '                    <td>\n' +
                '                        <div id="logo">\n' +
                '                            <img width="250" src="http://stimi.ovh:9000/dev/tim_hotelsquare.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=stimi-storage%2F20170828%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20170828T171954Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=d14e6b450fcb536aa9f07cb0154406dd5db45ea900d0b9b351e52d953d80d962" alt="hotelsquare">\n' +
                '                        </div>\n' +
                '                    </td>\n' +
                '                </tr>\n' +
                '                <tr>\n' +
                '                    <td>\n' +
                '                        Hello '+user.displayName+',<br><br>\n' +
                '                        have fun using HOTELSQUARE!<br>\n' +
                '                        But before you start please confirm your email address by clicking on this link<br>\n' +
                '                    </td>\n' +
                '                </tr>\n' +
                '                <tr>\n' +
                '                    <td>\n' +
                '                        <div id="trenner"></div>\n' +
                '                    </td>\n' +
                '                </tr>\n' +
                '                <tr>\n' +
                '                    <td style="text-align:center;">\n' +
                '                        <a href="https://dev.ip.stimi.ovh/emailConfirmation?key='+user.activation_key+'&id='+user._id.toString()+'">Activate</a>\n' +
                '                    </td>\n' +
                '                </tr>\n' +
                '            </table>\n' +
                '        </td>\n' +
                '        <td style="width:50%;"></td>\n' +
                '    </tr>\n' +
                '</table>\n' +
                '</body>\n' +
                '</html>'
            };

            await transporter.sendMail(mailOptions);
        }
        response.json(user);
        return next();
    });
}

/**
 * Resets user password and sends it him per mail
 *
 * @function register
 * @param {Object} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function resetPassword(request, response, next) {
    if(!request.body.name && !request.body.email) {
        return next(new restify_errors.BadRequestError());
    }
    const user = await User.findOne({$or: [{name: request.body.name}, {displayName: request.body.name}], email: request.body.email});
    const password = password_generate.generate({
        length: 10,
        numbers: true
    });
    user.password = password;
    await user.save();
    if(config.email) {
        const transporter = nodemailer.createTransport({
            host: config.email.server,
            port: 587,
            secure: false,
            requireTLS: true,
            auth: {
                user: config.email.mail,
                pass: config.email.password
            }
        });

        const mailOptions = {
            from: '"HOTELSQUARE Mailer" <'+config.email.mail+'>',
            to: user.email,
            subject: 'HOTELSQUARE Passwordreset',
            text: 'Hello '+user.displayName+', your password has been changed to "'+password+'" without quotes. Change your password immediately!',
            html: '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"\n' +
            '        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">\n' +
            '<html xmlns="http://www.w3.org/1999/xhtml">\n' +
            '<head>\n' +
            '    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>\n' +
            '    <title>E-Mail Template</title>\n' +
            '    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>\n' +
            '    <link href="https://fonts.googleapis.com/css?family=Pacifico" rel="stylesheet">\n' +
            '    <style>\n' +
            '        body {\n' +
            '            font-family: Arial;\n' +
            '            margin: 0;\n' +
            '            padding: 0;\n' +
            '            /*background-color: #fafafa;*/\n' +
            '        }\n' +
            '\n' +
            '        #logo {\n' +
            '            color: #2fb7e6;\n' +
            '            font-family: \'Pacifico\', cursive;\n' +
            '            font-size: 5em;\n' +
            '            text-align: center;\n' +
            '            padding-bottom: .5em;\n' +
            '        }\n' +
            '\n' +
            '        a {\n' +
            '            background-color: #2fb7e6;\n' +
            '            color: #fff;\n' +
            '            text-decoration: none;\n' +
            '            padding: 0 2rem;\n' +
            '            text-transform: uppercase;\n' +
            '            line-height: 36px;\n' +
            '            height: 36px;\n' +
            '            border-radius: 2px;\n' +
            '            display: inline-block;\n' +
            '        }\n' +
            '\n' +
            '        #contentTable {\n' +
            '            background-color: #fff;\n' +
            '            padding: 50px;\n' +
            '            margin: 50px;\n' +
            '            border: 1px solid #dddddd;\n' +
            '            border-bottom: 2px solid #dddddd;\n' +
            '        }\n' +
            '\n' +
            '        #trenner {\n' +
            '            height: 1px;\n' +
            '            line-height: 1px;\n' +
            '            width: 100%;\n' +
            '            background-color: #dddddd;\n' +
            '            margin: 40px 0;\n' +
            '        }\n' +
            '\n' +
            '        .input_label {\n' +
            '            color: #2fb7e6;\n' +
            '            font-size: .75rem;\n' +
            '            padding-top: 15px;\n' +
            '        }\n' +
            '\n' +
            '        .input_text {\n' +
            '            border-bottom: 1px solid #2fb7e6;\n' +
            '            padding: 5px 0;\n' +
            '        }\n' +
            '    </style>\n' +
            '</head>\n' +
            '<body style="">\n' +
            '<table border="0" cellpadding="0" cellspacing="0" width="100%" style="background-color: #fafafa;">\n' +
            '    <tr>\n' +
            '        <td style="width:50%;"></td>\n' +
            '        <td>\n' +
            '            <table id="contentTable" border="0" cellpadding="0" cellspacing="0">\n' +
            '                <tr>\n' +
            '                    <td>\n' +
            '                        <div id="logo">\n' +
            '                            <img width="250" src="http://stimi.ovh:9000/dev/tim_hotelsquare.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=stimi-storage%2F20170828%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20170828T171954Z&X-Amz-Expires=604800&X-Amz-SignedHeaders=host&X-Amz-Signature=d14e6b450fcb536aa9f07cb0154406dd5db45ea900d0b9b351e52d953d80d962" alt="hotelsquare">\n' +
            '                        </div>\n' +
            '                    </td>\n' +
            '                </tr>\n' +
            '                <tr>\n' +
            '                    <td>\n' +
            '                        Hello '+user.displayName+',<br><br>\n' +
            '                        Change your password immediately!<br>\n' +
            '                    </td>\n' +
            '                </tr>\n' +
            '                <tr>\n' +
            '                    <td>\n' +
            '                        <div class="input_label">Password</div>\n' +
            '                        <div class="input_text">'+password+'</div>\n' +
            '                    </td>\n' +
            '                </tr>\n' +
            '            </table>\n' +
            '        </td>\n' +
            '        <td style="width:50%;"></td>\n' +
            '    </tr>\n' +
            '</table>\n' +
            '</body>\n' +
            '</html>'};

        await transporter.sendMail(mailOptions);
    }

    response.send(200, 'Password reseted');
    return next();
}

/**
 * Deletes the current authenticated user
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function deleteUser(request, response, next) {
    const user = await User.findOne({_id: request.authentication._id});
    await user.destory();
    await user.save();
    response.json(user);
    return next();
}

/**
 * Uploads a avatar image for the current authenticated user and stores into
 * minio cloud storage.
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function uploadAvatar(request, response, next) {
    let user = await User.findOne({name: request.authentication.name}).populate('avatar');
    if(user.avatar) {
        await Image.destroy(user.avatar);
    }
    user.avatar = await Image.upload(request.files.image.path, user, user);
    user = await user.save();
    response.json(user);
    return next();
}

/**
 * Deletes a stored avatar image for the authenticated user.
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function deleteAvatar(request, response, next) {
    let user = await User.findOne({name: request.authentication.name}).populate('avatar');
    if(user.avatar) {
        await Image.destroy(user.avatar);
    }
    user.avatar = undefined;
    user = await user.save();
    response.json(user);
    return next();
}

/**
 * Sends a friend request from the authenticated user to name
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function sendFriendRequest(request, response, next) {
    const results = await Promise.all([
        User.findOne({name: request.params.name, 'friend_requests.sender': {$in: [request.authentication._id]}}),
        User.findOne({name: request.params.name, friends: {$in: [request.authentication._id]}})
    ]);
    if (results[0] === null && results[1] == null) {
        const res = await User.findOne({name: request.params.name});
        res.addFriendRequest(request.authentication);
        await res.save();
        response.json(res);
        return next();
    }
    return response.send(400, {error: 'Could not send friend request'});
}

/**
 * Removes a friend from the authenticated user
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function removeFriend(request, response, next) {
    // Find both users
    const results = await Promise.all([
        User.findOne({name: request.authentication.name}),
        User.findOne({name: request.params.name})
    ]);

    const receiver = results[0];
    const sender = results[1];

    sender.removeFriend(receiver);
    receiver.removeFriend(sender);

    await Promise.all([
        sender.save(),
        receiver.save()
    ]);
    response.json({message: 'Friend removed'});
    return next();
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

/**
 * Register user with information provided from facebook api
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function facebookRegister(request, response, next) {
    FB.setAccessToken(request.params.token);
    FB.api('me', {fields: ['id', 'name', 'email', 'cover']}, async (res) => {
        const password = password_generate.generate({
            length: 10,
            numbers: true
        });
        const user = await User.register(res.name, res.email, password);
        if(config.email) {
            const transporter = nodemailer.createTransport({
                host: config.email.server,
                port: 587,
                secure: false,
                requireTLS: true,
                auth: {
                    user: config.email.mail,
                    pass: config.email.password
                }
            });

            const mailOptions = {
                from: '"HOTELSQUARE Mailer" <'+config.email.mail+'>',
                to: user.email,
                subject: 'HOTELSQUARE Passwordreset',
                text: 'Hello '+user.displayName+', your password has been changed to "'+password+'" without quotes. Change your password immediately!'
            };

            await transporter.sendMail(mailOptions);
        }

        response.send(await user.save());
        return next();
    });
}

/**
 * Handle email confirmation
 *
 * @param {IncomingMessage} request request
 * @param {Object} response response
 * @param {Function} next next handler
 * @returns {undefined}
 */
async function confirmEmail(request, response, next) {
    if(!request.params.id || !request.params.key) {
        return next(new restify_errors.BadRequestError('Missing id or key'));
    }

    const user = await User.findOne({_id: request.params.id, activation_key: request.params.key});
    if(!user) {
        return next(new restify_errors.BadRequestError('Wrong id or key'));
    }

    user.active = true;
    user.activation_key = '';
    await user.save();

    response.send('Hello '+user.displayName+' your email is succesfuly confirmed. Have fun using HOTEL-Square!');
    return next();
}

module.exports = {
    register,
    deleteUser,
    uploadAvatar,
    deleteAvatar,
    profile,
    profileByID,
    sendFriendRequest,
    confirmFriendRequest,
    updateUser,
    removeFriend,
    search,
    resetPassword,
    facebookRegister,
    confirmEmail
};