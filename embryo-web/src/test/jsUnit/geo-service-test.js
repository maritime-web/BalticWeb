describe('geo', function () {

    describe('Converter', function () {

        /**
         * This unit test has been produced to ensure the same result as when calculating rapid response SAR operations in the EPD project.
         * The unit test was first written in Java in the EPD project, just making assertion values fit what was actually calculated, and then
         * there after ported to JavaScript. This way it is ensured that the JavaScript SAR calculations at least behaves the same as the Java
         * version did at the time of the SAR operations was implemented.
         *
         * Produced SAR unit test:
         */
        it('nmToMeters(5) should return 9260', function () {
            var result = embryo.geo.Converter.nmToMeters(5);
            expect(result).toBe(9260);
        });
    });

    describe('geo.Position', function () {
        it('use constructor', function () {
            var startPos = new embryo.geo.Position(-51.0, 61.0);
            expect(startPos).not.toBe(null);
            expect(startPos).toBeDefined();
            expect(startPos.lon).toEqual(-51);
            expect(startPos.lat).toEqual(61);
        });

        /**
         * This unit test has been produced to ensure the same result as when calculating rapid response SAR operations in the EPD project.
         * The unit test was first written in Java in the EPD project, just making assertion values fit what was actually calculated, and then
         * there after ported to JavaScript. This way it is ensured that the JavaScript SAR calculations at least behaves the same as the Java
         * version did at the time of the SAR operations was implemented.
         *
         * Produced SAR unit test: https://github.com/dma-enav/EPD/blob/master/epd-common/src/test/java/dk/dma/epd/common/util/CalculatorTest.java
         * as testFindPosition()
         */
        it('transformPosition(RL, 9260)', function () {
            var startPos = new embryo.geo.Position(-51.0, 61.0);
            var distanceInMeters = embryo.geo.Converter.nmToMeters(5);
            var heading = 45;

            var pos = startPos.transformPosition(heading, distanceInMeters);

            expect(pos).toBeDefined();
            expect(embryo.geo.formatLongitude(pos.lon)).toBe("050 52.699W");
            expect(embryo.geo.formatLatitude(pos.lat)).toBe("61 03.530N");
        });

        /**
         * This unit test has been produced to ensure the same result as when calculating rapid response SAR operations in the EPD project.
         * The unit test was first written in Java in the EPD project, just making assertion values fit what was actually calculated, and then
         * there after ported to JavaScript. This way it is ensured that the JavaScript SAR calculations at least behaves the same as the Java
         * version did at the time of the SAR operations was implemented.
         *
         * Produced SAR unit test: https://github.com/dma-enav/EPD/blob/master/epd-common/src/test/java/dk/dma/epd/common/util/CalculatorTest.java
         * as testRange()
         */
        it('(61 00.000N,050 52.699W).distanceTo((61 03.530N,050 52.699W),RL should return 5', function () {
            var startPos = new embryo.geo.Position(-51.0, 61.0);

            var lat = embryo.position.parseLatitude("61 03.530N");
            var lon = embryo.position.parseLongitude("050 52.699W");
            var end = new embryo.geo.Position(lon, lat);
            var result = startPos.distanceTo(end, embryo.geo.Heading.RL);

            expect(result).toBe(5.000017243489917);
        });

    });


    describe('Ellipsoid.SPHERE', function () {
        it('calculateEndingGlobalCoordinates to work', function () {
            var sphereEllipsoid = embryo.geo.Ellipsoid.SPHERE;
            var startPos = new embryo.geo.Position(-51.0, 61.0);
            var distanceInMeters = 5000;
            var heading = 45;

            var result = sphereEllipsoid.calculateEndingGlobalCoordinates(startPos, heading, distanceInMeters);

            expect(result).not.toBe(null);
            expect(result).toBeDefined();
            expect(result.lon).toBeGreaterThan(-51);
            expect(result.lat).toBeGreaterThan(61);
        });

    });

});