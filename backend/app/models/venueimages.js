'use strict';

const mongoose = require('mongoose');
const Schema = mongoose.Schema;

// ---------------------------------------------------------------------------------------------------------------------
// Schema
// ---------------------------------------------------------------------------------------------------------------------

const VenueImagesSchema = new Schema({
    venueid: String,
    imagename: String,
    user: {
        type: Schema.Types.ObjectId,
        ref: 'User'
    }
});

// ---------------------------------------------------------------------------------------------------------------------
// Class
// ---------------------------------------------------------------------------------------------------------------------

class VenueImagesClass {

}

VenueImagesSchema.loadClass(VenueImagesClass);
module.exports = mongoose.model('VenueImages', VenueImagesSchema);