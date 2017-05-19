const chalk = require('chalk');

/**
 * Logs a request to the console
 * @param {Object} request http request
 * @param {Object} response http response
 * @param {Function} next next handler
 * @returns {undefined}
 */
module.exports = function (request, response, next) {
    console.log(chalk.green(request.method) + ' - ' + request.url + ' - ' + request.headers['host']);
    return next();
};