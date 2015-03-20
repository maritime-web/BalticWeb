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
                xError: 1,
                yError: 0.1,
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
            expect(sarOperation.radius).toBeCloseTo(2.532634, 6);

            expect(formatLatitude(sarOperation.searchArea.A.lat)).toBe("61 06.905N");
            expect(formatLongitude(sarOperation.searchArea.A.lon)).toBe("050 52.842W");

            expect(formatLatitude(sarOperation.searchArea.B.lat)).toBe("61 03.278N");
            expect(formatLongitude(sarOperation.searchArea.B.lon)).toBe("050 45.541W");

            expect(formatLatitude(sarOperation.searchArea.C.lat)).toBe("60 59.748N");
            expect(formatLongitude(sarOperation.searchArea.C.lon)).toBe("050 53.043W");

            expect(formatLatitude(sarOperation.searchArea.D.lat)).toBe("61 03.375N");
            expect(formatLongitude(sarOperation.searchArea.D.lon)).toBe("051 00.331W");
            expect(sarOperation.searchArea.totalSize).toBeCloseTo(2.5326335063948107 * 2.5326335063948107 * 4, 6);
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
                xError: 0.1,
                yError: 0.1,
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
            expect(sarOperation.radius).toBeCloseTo(1.374240, 6);

            expect(formatLatitude(sarOperation.searchArea.A.lat)).toBe("61 06.769N");
            expect(formatLongitude(sarOperation.searchArea.A.lon)).toBe("050 52.467W");

            expect(formatLatitude(sarOperation.searchArea.B.lat)).toBe("61 05.179N");
            expect(formatLongitude(sarOperation.searchArea.B.lon)).toBe("050 47.833W");

            expect(formatLatitude(sarOperation.searchArea.C.lat)).toBe("61 02.939N");
            expect(formatLongitude(sarOperation.searchArea.C.lon)).toBe("050 51.124W");

            expect(formatLatitude(sarOperation.searchArea.D.lat)).toBe("61 04.529N");
            expect(formatLongitude(sarOperation.searchArea.D.lon)).toBe("050 55.753W");
            expect(sarOperation.searchArea.totalSize).toBeCloseTo(1.3742403439070814 * 1.3742403439070814 * 4, 6);
        });

    });

});