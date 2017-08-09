'use strict';

const mongoose = require('mongoose');
const Schema = mongoose.Schema;


// ---------------------------------------------------------------------------------------------------------------------
// Schema
// ---------------------------------------------------------------------------------------------------------------------
const CompletionSchema = new Schema({
    input: String,
    result: Schema.Types.Mixed
});

module.exports = mongoose.model('Completion', CompletionSchema);