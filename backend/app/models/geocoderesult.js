'use strict';

const mongoose = require('mongoose');
const Schema = mongoose.Schema;

// ---------------------------------------------------------------------------------------------------------------------
// Schema
// ---------------------------------------------------------------------------------------------------------------------

const GeocodeResultSchema = new Schema({
    querytime: {type: Date, default: Date.now},
    keyword: {
        type: String,
        index: true
    },
    result: Object
});

GeocodeResultSchema.pre('save', function (next) {
    const self = this;

    if (self.isModified('keyword'))
        self.keyword = self.keyword.toLowerCase();
        
    return next();
});

// ---------------------------------------------------------------------------------------------------------------------
// Class
// ---------------------------------------------------------------------------------------------------------------------

class GeocodeResultClass {

    static findByKeyword(keyword) {
        const query = this.findOne({ keyword: keyword.toLowerCase()  });
        return query;
    }

}

GeocodeResultSchema.loadClass(GeocodeResultClass);
module.exports = mongoose.model('GeocodeResult', GeocodeResultSchema);