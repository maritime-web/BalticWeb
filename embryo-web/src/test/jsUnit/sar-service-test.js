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
         * Produced SAR unit test: xx
         */
        it('create rapid response SAR operation', function () {
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

            expect(sarOperation.rdvDirection).toBe(45.78003035557367);
            expect(sarOperation.rdvDistance).toBe(4.7754450213160355);
            expect(sarOperation.rdvSpeed).toBe(4.7754450213160355);
            expect(sarOperation.radius).toBe(1.5116335063948105);

            expect(formatLatitude(sarOperation.searchArea.A.lat)).toBe("61 05.464N");
            expect(formatLongitude(sarOperation.searchArea.A.lon)).toBe("050 52.880W");

            expect(formatLatitude(sarOperation.searchArea.B.lat)).toBe("61 03.299N");
            expect(formatLongitude(sarOperation.searchArea.B.lon)).toBe("050 48.524W");

            expect(formatLatitude(sarOperation.searchArea.C.lat)).toBe("61 01.191N");
            expect(formatLongitude(sarOperation.searchArea.C.lon)).toBe("050 53.001W");

            expect(formatLatitude(sarOperation.searchArea.D.lat)).toBe("61 03.357N");
            expect(formatLongitude(sarOperation.searchArea.D.lon)).toBe("050 57.352W");
            expect(sarOperation.searchArea.totalSize).toBe(1.5116335063948105 * 1.5116335063948105 * 4);


        });

    });

});