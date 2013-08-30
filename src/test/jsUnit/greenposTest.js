describe('GreenPos Controller', function() {

	describe('embryo.greenPos.Ctrl', function() {

		it('Visibility must change depending on report type', function() {

			var shipService = {
				getYourShip : function() {
					return {
						mmsi : "2200",
						callSign : "CallSign",
						name : "shipName",
						maritimeId : "myId"
					};
				}
			};

			var voyageService = {
				getYourActive : function(callback) {
					callback({
						berthName : "Nuuk",
						arrival : "22-10-13 10:10",
						personsOnBoard : 12
					});
				}
			};

			var scope = {
				$on : function(eventType, callback) {

				},
				$watch : function(expr, callback){
					
				}
			};
			var ctrl = new embryo.greenPos.Ctrl(scope, shipService,
					voyageService);

			expect(scope.isVisible("destination")).toBe(true);
			expect(scope.isVisible("etaOfArrival")).toBe(true);
			expect(scope.isVisible("deviation")).toBe(false);

			scope.report.reportType = "FR";
			expect(scope.isVisible("destination")).toBe(false);
			expect(scope.isVisible("personsOnBoard")).toBe(false);
			expect(scope.isVisible("weather")).toBe(true);

			scope.report.reportType = "DR";
			expect(scope.isVisible("destination")).toBe(false);
			expect(scope.isVisible("iceInformation")).toBe(false);
			expect(scope.isVisible("deviation")).toBe(true);
		});
	});

});