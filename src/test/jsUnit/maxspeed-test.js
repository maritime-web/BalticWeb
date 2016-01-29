describe('Max Speed', function() {

    describe('Max Speed ', function() {
        it('If awsog, ssog or sog is not set we defaults to 0.0', function() {
            embryo.vessel = {
        		mmsi : 12345678
            };
            var result = embryo.getMaxSpeed(embryo.vessel);
            expect(result).toEqual(0.0);
        });
        
        it('If awsog is set use it.', function() {
            embryo.vessel = {
        		mmsi	: 12345678,
        		awsog	: 1.5
            };
            var result = embryo.getMaxSpeed(embryo.vessel);
            expect(result).toEqual(1.5);
        });
        
        it('If ssog is set use it.', function() {
            embryo.vessel = {
        		mmsi	: 12345678,
        		ssog	: 2.5
            };
            var result = embryo.getMaxSpeed(embryo.vessel);
            expect(result).toEqual(2.5);
        });
        
        it('If sog is set use it.', function() {
            embryo.vessel = {
        		mmsi	: 12345678,
        		sog		: 3.5
            };
            var result = embryo.getMaxSpeed(embryo.vessel);
            expect(result).toEqual(3.5);
        });
        
    });
});