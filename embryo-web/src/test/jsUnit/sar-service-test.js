describe('embryo.sar', function () {

    describe('SarService', function () {
        var service;
        beforeEach(function () {
            module('embryo.sar.service');
        });

        beforeEach(inject(function (SarService) {
            service = SarService;
        }));


        /**
         * This unit test has been produced to ensure the same result as when calculating rapid response SAR operations in the EPD project.
         * The unit test was first written in Java in the EPD project, just making assertion values fit what was actually calculated, and then
         * there after ported to JavaScript. This way it is ensured that the JavaScript SAR calculations at least behaves the same as the Java
         * version did at the time of the SAR operations was implemented.
         *
         * Produced SAR unit test: https://github.com/dma-enav/EPD/blob/master/epd-common/src/test/java/dk/dma/epd/common/prototype/model/voct/SarOperationTest.java
         * as testRapidResponseWithOneSurfarceDriftPoint()
         */
        it('create rapid response SAR operation with one surface drift point', function () {
            var searchObjectTypes = service.searchObjectTypes();
            var formatLatitude = embryo.geo.formatLatitude;
            var formatLongitude = embryo.geo.formatLongitude;

            var data = {
                sarNo: 1,
                type: embryo.sar.types.RapidResponse,
                lastKnownPosition: {
                    ts: Date.now() - 60 * 60 * 1000,
                    lon: -51,
                    lat: 61
                },
                startTs: Date.now(),
                surfaceDriftPoints: [{
                    ts: Date.now() - 60 * 60 * 1000,
                    twcSpeed: 5,
                    twcDirection: 45,
                    leewaySpeed: 15,
                    leewayDirection: 30
                }],
                initPosErr: 1,
                sruErr: 0.1,
                safetyFactor: 1,
                searchObject: searchObjectTypes[0]
            }

            //var sarOperation = null;
            var sarOperation = service.createSarOperation(data);

            expect(sarOperation).toBeDefined();

            // ASSERT DATUM
            expect(formatLongitude(sarOperation.datum.lon)).toBe("050 52.939W");
            expect(formatLatitude(sarOperation.datum.lat)).toBe("61 03.328N");

            expect(sarOperation.rdvDirection).toBeCloseTo(45.780030,6);
            expect(sarOperation.rdvDistance).toBeCloseTo(4.775445, 6);
            expect(sarOperation.rdvSpeed).toBeCloseTo(4.775445, 6);
            expect(sarOperation.radius).toBeCloseTo(1.511634, 6);

            expect(formatLatitude(sarOperation.searchArea.A.lat)).toBe("61 05.464N");
            expect(formatLongitude(sarOperation.searchArea.A.lon)).toBe("050 52.880W");

            expect(formatLatitude(sarOperation.searchArea.B.lat)).toBe("61 03.299N");
            expect(formatLongitude(sarOperation.searchArea.B.lon)).toBe("050 48.524W");

            expect(formatLatitude(sarOperation.searchArea.C.lat)).toBe("61 01.191N");
            expect(formatLongitude(sarOperation.searchArea.C.lon)).toBe("050 53.001W");

            expect(formatLatitude(sarOperation.searchArea.D.lat)).toBe("61 03.357N");
            expect(formatLongitude(sarOperation.searchArea.D.lon)).toBe("050 57.352W");
            expect(sarOperation.searchArea.totalSize).toBeCloseTo(1.5116335063948105 * 1.5116335063948105 * 4, 6);
        });


        /**
         * This unit test has been produced to ensure the same result as when calculating rapid response SAR operations in the EPD project.
         * The unit test was first written in Java in the EPD project, just making assertion values fit what was actually calculated, and then
         * there after ported to JavaScript. This way it is ensured that the JavaScript SAR calculations at least behaves the same as the Java
         * version did at the time of the SAR operations was implemented.
         *
         * Produced SAR unit test: https://github.com/dma-enav/EPD/blob/master/epd-common/src/test/java/dk/dma/epd/common/prototype/model/voct/SarOperationTest.java
         * as testRapidResponseWithTwoSurfarceDriftPoint()
         */

        it('create rapid response SAR operation with two surface drift points', function () {
            var searchObjectTypes = service.searchObjectTypes();
            var formatLatitude = embryo.geo.formatLatitude;
            var formatLongitude = embryo.geo.formatLongitude;

            var data = {
                sarNo: 1,
                type: embryo.sar.types.RapidResponse,
                lastKnownPosition: {
                    ts: Date.now() - 60 * 60 * 1000,
                    lon: -51,
                    lat: 61
                },
                startTs: Date.now(),
                surfaceDriftPoints: [{
                    ts: Date.now() - 60 * 60 * 1000,
                    twcSpeed: 5,
                    twcDirection: 45,
                    leewaySpeed: 15,
                    leewayDirection: 30
                }, {
                    ts: Date.now() - 30 * 60 * 1000,
                    twcSpeed: 8,
                    twcDirection: 35,
                    leewaySpeed: 10,
                    leewayDirection: 20
                }],
                initPosErr: 1,
                sruErr: 0.1,
                safetyFactor: 1,
                searchObject: searchObjectTypes[0]
            }

            //var sarOperation = null;
            var sarOperation = service.createSarOperation(data);

            expect(sarOperation).toBeDefined();

            // ASSERT DATUM
            expect(formatLatitude(sarOperation.datum.lat)).toBe("61 04.854N");
            expect(formatLongitude(sarOperation.datum.lon)).toBe("050 51.794W");

            expect(sarOperation.rdvDirection).toBeCloseTo(35.372815, 6);
            expect(sarOperation.rdvDistance).toBeCloseTo(3.914134, 6);
            expect(sarOperation.rdvSpeed).toBeCloseTo(7.828269, 6);
            expect(sarOperation.radius).toBeCloseTo(1.253240, 6);

            expect(formatLatitude(sarOperation.searchArea.A.lat)).toBe("61 06.600N");
            expect(formatLongitude(sarOperation.searchArea.A.lon)).toBe("050 52.408W");

            expect(formatLatitude(sarOperation.searchArea.B.lat)).toBe("61 05.150N");
            expect(formatLongitude(sarOperation.searchArea.B.lon)).toBe("050 48.182W");

            expect(formatLatitude(sarOperation.searchArea.C.lat)).toBe("61 03.108N");
            expect(formatLongitude(sarOperation.searchArea.C.lon)).toBe("050 51.183W");

            expect(formatLatitude(sarOperation.searchArea.D.lat)).toBe("61 04.558N");
            expect(formatLongitude(sarOperation.searchArea.D.lon)).toBe("050 55.404W");
            expect(sarOperation.searchArea.totalSize).toBeCloseTo(1.2532403439070814 * 1.2532403439070814 * 4, 6);
        });

    });

});