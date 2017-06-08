'use strict';

const mongoose = require('mongoose');
const Schema = mongoose.Schema;

// ---------------------------------------------------------------------------------------------------------------------
// Schema
// ---------------------------------------------------------------------------------------------------------------------

const VenueSchema = new Schema({
    name: String,
    location: {
        'type': { type: String, default: 'Point' },
        coordinates: { type: [Number], default: [0, 0] }
    }
});


VenueSchema.index({location: '2dsphere'});

// ---------------------------------------------------------------------------------------------------------------------
// Class
// ---------------------------------------------------------------------------------------------------------------------

class VenueClass {

}

VenueSchema.loadClass(VenueClass);
module.exports = mongoose.model('Venue', VenueSchema);