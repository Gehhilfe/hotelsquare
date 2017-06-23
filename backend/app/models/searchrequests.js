'use strict';

const mongoose = require('mongoose');
const Schema = mongoose.Schema;

// ---------------------------------------------------------------------------------------------------------------------
// Schema
// ---------------------------------------------------------------------------------------------------------------------

const SearchRequestSchema = new Schema({
    location: {
        'type': { type: String, default: 'Point' },
        coordinates: { type: [Number], default: [0, 0] }
    },
    querytime: { type: Date, default: Date.now }
});



SearchRequestSchema.index({location: '2dsphere'});

// ---------------------------------------------------------------------------------------------------------------------
// Class
// ---------------------------------------------------------------------------------------------------------------------

class SearchRequestClass {

    static findClosestLocation(coords){
        return this.find({
            location: {
                $nearsphere: coords
            }
        })
        .limit(1);
    }

}

SearchRequestSchema.loadClass(SearchRequestClass);
module.exports = mongoose.model('SearchRequest', SearchRequestSchema);