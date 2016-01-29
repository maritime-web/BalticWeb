describe('Decimal', function() {

    describe('decimal', function() {
        it('decimal.parse handles null, undefined and empty string', function() {
            var course = embryo.decimal.parse(null);
            expect(course).toBe(null);

            course = embryo.decimal.parse("");
            expect(course).toBe(null);

            course = embryo.decimal.parse(undefined);
            expect(course).toBe(undefined);
        });

        it('decimal.parse replaces commas with dots.', function() {
            var course = embryo.decimal.parse("0,1");
            expect(course).toBe(0.1);

            course = embryo.decimal.parse("0,111,1");
            expect(course).toBe(0.111);
        });

    });

});