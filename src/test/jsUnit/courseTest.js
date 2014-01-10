describe('Course', function() {

    describe('course', function() {
        var $compile, scope;

        it('formatCourse adds two leading zeros if 0 <= course < 10', function() {
            var course = formatCourse(0);
            expect(course).toBe("000");

            course = formatCourse(9);
            expect(course).toBe("009");
        });

        it('formatCourse adds one leading zero if course >= 10 and course < 100', function() {
            var course = formatCourse(10);
            expect(course).toBe("010");

            course = formatCourse(99);
            expect(course).toBe("099");
        });

        it('formatCourse returns the same value as a string if course >= 100', function() {
            var course = formatCourse(100);
            expect(course).toBe("100");

            course = formatCourse(360);
            expect(course).toBe("360");
        });

        it('formatCourse of null returns null', function() {
            var course = formatCourse(null);
            expect(course).toBe(null);
        });

        it('formatCourse of undefined returns undefined', function() {
            var course = formatCourse(undefined);
            expect(course).toBe(undefined);
        });

        it('course.parse handles null, undefined and empty string', function() {
            var course = embryo.course.parse(null);
            expect(course).toBe(null);

            course = embryo.course.parse("");
            expect(course).toBe(null);

            course = embryo.course.parse(undefined);
            expect(course).toBe(undefined);
        });

        it('course.parse removes leading zeros.', function() {
            var course = embryo.course.parse("000");
            expect(course).toBe(0);

            course = embryo.course.parse("010");
            expect(course).toBe(10);

            course = embryo.course.parse("355");
            expect(course).toBe(355);
        });

    });

});