'use strict';

const mongoose = require('mongoose');
const Schema = mongoose.Schema;

// ---------------------------------------------------------------------------------------------------------------------
// Schema
// ---------------------------------------------------------------------------------------------------------------------

const SearchRequestSchema = new Schema({
    location: {
        'type': {type: String, default: 'Point'},
        coordinates: {type: [Number], default: [0, 0]}
    },
    querytime: {type: Date, default: Date.now},
    keyword: String
});


SearchRequestSchema.index({location: '2dsphere'});

// ---------------------------------------------------------------------------------------------------------------------
// Class
// ---------------------------------------------------------------------------------------------------------------------

class SearchRequestClass {

    static findClosestLocation(coords, keyword, radius) {
        const query = this.find({ keyword: new RegExp(keyword, 'i')  });
        return query.where('location').near({
            center: {
                type: 'Point',
                coordinates: coords
            },
            maxDistance: radius
        });
    }

}

SearchRequestSchema.loadClass(SearchRequestClass);
module.exports = mongoose.model('SearchRequest', SearchRequestSchema);