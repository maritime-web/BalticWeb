describe('Position', function() {

	describe('latitude and longitude', function() {
		var $compile, scope, shipService, voyageService;

		it('parseLatitude can parse the string 64 10.400N ', function() {
			var lat = embryo.position.parseLatitude('64 10.400N');
			expect(lat).toBeCloseTo(64.173);
		});

		it('parseLatitude can parse the number 74', function() {
			var lat = embryo.position.parseLatitude('74');
			expect(lat).toBeCloseTo(74);
		});

		it('parseLatitude can parse the number 74.5', function() {
			var lat = embryo.position.parseLatitude('74.5');
			expect(lat).toBeCloseTo(74.5);
		});

		it('parseLongitude can parse the string 051 43.500W', function() {
			var lon = embryo.position.parseLongitude('051 43.500W');
			expect(lon).toBeCloseTo(-51.725);
		});

		it('parseLongitude can parse the string 051 43.500E', function() {
			var lon = embryo.position.parseLongitude('051 43.500E');
			expect(lon).toBeCloseTo(51.725);
		});

		it('parseLongitude can parse the number -35', function() {
			var lon = embryo.position.parseLongitude('-35');
			expect(lon).toBeCloseTo(-35);
		});

		it('parseLongitude can parse the number -35.0', function() {
			var lon = embryo.position.parseLongitude('-35.0');
			expect(lon).toBeCloseTo(-35);
		});

	});

});