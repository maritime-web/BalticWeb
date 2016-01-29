describe('Distance Tools', function() {

    describe('Nearest Vessel ', function() {
        it('Status Unavailable if no vessels has msog value', function() {
            embryo.vessel = {};
            embryo.vessel.allVessels = function() {
                return [ {
                    mmsi : 12345678
                }, {
                    mmsi : 0987654
                }, {
                    mmsi : 76859403,
                    ssog : 0
                } ];
            };

            var result = embryo.additionalInformation.nearestShips.available(null,null);
            expect(result).toBeFalsy();
        });

        it('Status Available if at least one vessel has msog value greater than zero', function() {
            embryo.vessel = {};
            embryo.vessel.allVessels = function() {
                return [ {
                    mmsi : 12345678,
                    ssog : 1
                }, {
                    mmsi : 0987654
                }, {
                    mmsi : 76859403
                } ];
            };

            var result = embryo.additionalInformation.nearestShips.available(null,null);
            expect(result).toBeTruthy();
        });
});

});