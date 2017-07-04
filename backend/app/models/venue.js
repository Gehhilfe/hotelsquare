'use strict';

const mongoose = require('mongoose');
const Schema = mongoose.Schema;

// ---------------------------------------------------------------------------------------------------------------------
// Schema
// ---------------------------------------------------------------------------------------------------------------------

const VenueSchema = new Schema({
    name: String,
    place_id: String,
    reference: String,
    types: [String],
    location: {
        'type': { type: String, default: 'Point' },
        coordinates: { type: [Number], default: [0, 0] }
    },
    comments: [{
        author: {
            type: Schema.Types.ObjectId,
            ref: 'User'
        },
        text: String,
        likes: Number,
        dislikes: Number,
        date: {
            type: Date,
            default: Date.now()
        },
        isimage: {
            type: Boolean,
            default: false
        },
        imagenames: String
    }]
});



VenueSchema.index({location: '2dsphere'});

// ---------------------------------------------------------------------------------------------------------------------
// Class
// ---------------------------------------------------------------------------------------------------------------------

class VenueClass {

}

VenueSchema.loadClass(VenueClass);
module.exports = mongoose.model('Venue', VenueSchema);