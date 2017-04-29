// test/string.js

var expect = require('chai').expect;

describe('Math', function() {
    describe('#max', function() {
        it('returns the biggest number from the arguments', function() {
            var max = Math.max(1,2,3,4,5,6,7,8,9,0);
            expect(max).to.equal(9);
        });
    });
});