describe('embryo.sar', function () {

    describe('SarService', function () {
        var service;
        beforeEach(function () {
            var mockSubject = {
                getDetails: function () {
                    return {
                        userName: "foo"
                    };
                }
            };

            module('embryo.authentication.service', function ($provide) {
                $provide.value('Subject', mockSubject);
            });
            module('embryo.sar.service');
        });

        beforeEach(inject(function (SarService) {
            service = SarService;
        }));

        function createSarTestObject(service) {
            var searchObjectTypes = service.searchObjectTypes();
            return {
                sarNo: 1,
                type: embryo.sar.Operation.RapidResponse,
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
                searchObject: searchObjectTypes[0].id
            }
        }


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
            var formatLatitude = embryo.geo.formatLatitude;
            var formatLongitude = embryo.geo.formatLongitude;

            var input = createSarTestObject(service);

            //var sarOperation = null;
            var result = service.createSarOperation(input);

            expect(result).toBeDefined();
            expect(result.output).toBeDefined();
            var output = result.output;

            // ASSERT DATUM
            expect(formatLongitude(output.datum.lon)).toBe("050 52.939W");
            expect(formatLatitude(output.datum.lat)).toBe("61 03.328N");

            expect(output.rdv.direction).toBeCloseTo(45.780030, 4);
            expect(output.rdv.distance).toBeCloseTo(4.775445, 4);
            expect(output.rdv.speed).toBeCloseTo(4.775445, 4);
            expect(output.radius).toBeCloseTo(2.532634, 4);

            expect(formatLatitude(output.searchArea.A.lat)).toBe("61 06.905N");
            expect(formatLongitude(output.searchArea.A.lon)).toBe("050 52.842W");

            expect(formatLatitude(output.searchArea.B.lat)).toBe("61 03.278N");
            expect(formatLongitude(output.searchArea.B.lon)).toBe("050 45.541W");

            expect(formatLatitude(output.searchArea.C.lat)).toBe("60 59.748N");
            expect(formatLongitude(output.searchArea.C.lon)).toBe("050 53.043W");

            expect(formatLatitude(output.searchArea.D.lat)).toBe("61 03.375N");
            expect(formatLongitude(output.searchArea.D.lon)).toBe("051 00.331W");
            expect(output.searchArea.size).toBeCloseTo(2.5326335063948107 * 2.5326335063948107 * 4, 4);
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

            var input = {
                sarNo: 1,
                type: embryo.sar.Operation.RapidResponse,
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
                searchObject: searchObjectTypes[0].id
            }

            //var sarOperation = null;
            var result = service.createSarOperation(input);

            expect(result).toBeDefined();
            expect(result.output).toBeDefined();
            var output = result.output;

            // ASSERT DATUM
            expect(formatLatitude(output.datum.lat)).toBe("61 04.854N");
            expect(formatLongitude(output.datum.lon)).toBe("050 51.794W");

            expect(output.rdv.direction).toBeCloseTo(35.372815, 4);
            expect(output.rdv.distance).toBeCloseTo(3.914134, 4);
            expect(output.rdv.speed).toBeCloseTo(7.828269, 4);
            expect(output.radius).toBeCloseTo(1.374240, 4);

            expect(formatLatitude(output.searchArea.A.lat)).toBe("61 06.769N");
            expect(formatLongitude(output.searchArea.A.lon)).toBe("050 52.467W");

            expect(formatLatitude(output.searchArea.B.lat)).toBe("61 05.179N");
            expect(formatLongitude(output.searchArea.B.lon)).toBe("050 47.833W");

            expect(formatLatitude(output.searchArea.C.lat)).toBe("61 02.939N");
            expect(formatLongitude(output.searchArea.C.lon)).toBe("050 51.124W");

            expect(formatLatitude(output.searchArea.D.lat)).toBe("61 04.529N");
            expect(formatLongitude(output.searchArea.D.lon)).toBe("050 55.753W");
            expect(output.searchArea.size).toBeCloseTo(1.3742403439070814 * 1.3742403439070814 * 4, 4);
        });

        function executeWithTryCatch(service, input) {
            try {
                service.createSarOperation(input);
            } catch (Error) {
                return Error;
            }
            return null;
        }

        function assertErrorContent(err, fieldName) {
            expect(err).toBeDefined();
            expect(err.message).toBeDefined();
            expect(err.message.indexOf(fieldName) >= 0).toBe(true);
        }

        it('Error thrown if lastKnownPosition.ts has no value', function () {
            var input = createSarTestObject(service);
            input.lastKnownPosition.ts = null;

            var err = executeWithTryCatch(service, input)
            assertErrorContent(err, "ts");
        });

        it('Error thrown if lastKnownPosition.lon has no value', function () {
            var input = createSarTestObject(service);
            input.lastKnownPosition.lon = null;

            var err = executeWithTryCatch(service, input)
            assertErrorContent(err, "lon");
        });

        it('Error thrown if lastKnownPosition.lat has no value', function () {
            var input = createSarTestObject(service);
            input.lastKnownPosition.lat = null;

            var err = executeWithTryCatch(service, input)
            assertErrorContent(err, "lat");
        });

        it('Error thrown if startTs has no value ', function () {
            var input = createSarTestObject(service);
            input.startTs = null;

            var err = executeWithTryCatch(service, input)
            assertErrorContent(err, "startTs");
        });

        it('Error thrown if surfaceDriftPoint.ts has no value ', function () {
            var input = createSarTestObject(service);
            input.surfaceDriftPoints[0].ts = null;

            var err = executeWithTryCatch(service, input)
            assertErrorContent(err, "ts");
        });

        it('Error thrown if surfaceDriftPoint.twcSpeed has no value ', function () {
            var input = createSarTestObject(service);
            input.surfaceDriftPoints[0].twcSpeed = null;

            var err = executeWithTryCatch(service, input)
            assertErrorContent(err, "twcSpeed");
        });

        it('Error thrown if surfaceDriftPoint.twcDirection has no value ', function () {
            var input = createSarTestObject(service);
            input.surfaceDriftPoints[0].twcDirection = null;

            var err = executeWithTryCatch(service, input)
            assertErrorContent(err, "twcDirection");
        });

        it('Error thrown if surfaceDriftPoint.leewaySpeed has no value ', function () {
            var input = createSarTestObject(service);
            input.surfaceDriftPoints[0].leewaySpeed = null;

            var err = executeWithTryCatch(service, input)
            assertErrorContent(err, "leewaySpeed");
        });

        it('Error thrown if surfaceDriftPoint.leewayDirection has no value ', function () {
            var input = createSarTestObject(service);
            input.surfaceDriftPoints[0].leewayDirection = null;

            var err = executeWithTryCatch(service, input)
            assertErrorContent(err, "leewayDirection");
        });

        it('Error thrown if xError has no value ', function () {
            var input = createSarTestObject(service);
            input.xError = null;

            var err = executeWithTryCatch(service, input)
            assertErrorContent(err, "xError");
        });

        it('Error thrown if yError has no value ', function () {
            var input = createSarTestObject(service);
            input.yError = null;

            var err = executeWithTryCatch(service, input)
            assertErrorContent(err, "yError");
        });

        it('Error thrown if safetyFactor has no value ', function () {
            var input = createSarTestObject(service);
            input.safetyFactor = null;

            var err = executeWithTryCatch(service, input)
            assertErrorContent(err, "safetyFactor");
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

        it('create datum point SAR operation with one surface drift point', function () {
            var searchObjectTypes = service.searchObjectTypes();
            var formatLatitude = embryo.geo.formatLatitude;
            var formatLongitude = embryo.geo.formatLongitude;

            var input = {
                sarNo: 1,
                type: embryo.sar.Operation.DatumPoint,
                lastKnownPosition: {
                    ts: Date.now() - 60 * 60 * 1000,
                    lat: 61,
                    lon: -51
                },
                startTs: Date.now(),
                surfaceDriftPoints: [{
                    ts: Date.now() - 60 * 60 * 1000,
                    twcSpeed: 5,
                    twcDirection: 45,
                    leewaySpeed: 15,
                    leewayDirection: 30
                }],
                xError: 1.0,
                yError: 0.1,
                safetyFactor: 1.0,
                searchObject: searchObjectTypes[0].id
            }

            //var sarOperation = null;
            var result = service.createSarOperation(input);

            expect(result).toBeDefined();
            expect(result.output).toBeDefined();
            var output = result.output;

            // ASSERT DATUM
            expect(formatLatitude(output.downWind.datum.lat)).toBe("61 03.328N");
            expect(formatLongitude(output.downWind.datum.lon)).toBe("050 52.939W");
            expect(output.downWind.rdv.direction).toBeCloseTo(45.780030, 4);
            expect(output.downWind.rdv.distance).toBeCloseTo(4.7754450, 4);
            expect(output.downWind.rdv.speed).toBeCloseTo(4.775445, 4);
            expect(output.downWind.radius).toBeCloseTo(2.532633, 4);

            expect(formatLatitude(output.max.datum.lat)).toBe("61 03.413N");
            expect(formatLongitude(output.max.datum.lon)).toBe("050 53.115W");
            expect(output.max.rdv.direction).toBeCloseTo(44.331598, 4);
            expect(output.max.rdv.distance).toBeCloseTo(4.7752103, 4);
            expect(output.max.rdv.speed).toBeCloseTo(4.7752103, 4);
            expect(output.max.radius).toBeCloseTo(2.5325631, 4);

            expect(formatLatitude(output.min.datum.lat)).toBe("61 03.297N");
            expect(formatLongitude(output.min.datum.lon)).toBe("050 52.699W");
            expect(output.min.rdv.direction).toBeCloseTo(47.008245, 4);
            expect(output.min.rdv.distance).toBeCloseTo(4.8383743, 4);
            expect(output.min.rdv.speed).toBeCloseTo(4.8383743, 4);
            expect(output.min.radius).toBeCloseTo(2.5515123, 4);

            /*
             expect(formatLatitude(sarOperation.searchArea.A.lat)).toBe("60 59.801N");
             expect(formatLongitude(sarOperation.searchArea.A.lon)).toBe("050 50.788W");

             expect(formatLatitude(sarOperation.searchArea.B.lat)).toBe("61 02.460N");
             expect(formatLongitude(sarOperation.searchArea.B.lon)).toBe("051 00.292W");

             expect(formatLatitude(sarOperation.searchArea.C.lat)).toBe("61 06.878N");
             expect(formatLongitude(sarOperation.searchArea.C.lon)).toBe("050 55.017W");

             expect(formatLatitude(sarOperation.searchArea.D.lat)).toBe("61 04.229N");
             expect(formatLongitude(sarOperation.searchArea.D.lon)).toBe("050 45.497W");
             expect(sarOperation.searchArea.size).toBeCloseTo(1.3742403439070814 * 1.3742403439070814 * 4, 4);
             */
        });




    });

});