describe('GreenPos Controller', function() {

    describe('embryo.GreenPosCtrl', function() {

        var $compile, scope, shipService, voyageService;

        beforeEach(inject(function($injector, $sniffer) {
            shipService = {
                getYourShip : function() {
                    return {
                        mmsi : "2200",
                        callSign : "CallSign",
                        name : "shipName",
                        maritimeId : "myId"
                    };
                }
            };

            voyageService = {
                getYourActive : function(callback) {
                    callback({
                        berthName : "Nuuk",
                        arrival : "22-10-13 10:10",
                        personsOnBoard : 12
                    });
                }
            };

            $compile = $injector.get('$compile');
            scope = $injector.get('$rootScope');

            changeInputValue = function(elm, value) {
                elm.val(value);
                browserTrigger(elm, $sniffer.hasEvent('input') ? 'input' : 'change');
            };
        }));

        it('Visibility must change depending on report type', function() {

            var scopeLocal = {
                $on : function(eventType, callback) {

                },
                $watch : function(expr, callback) {

                }
            };
            var ctrl = new embryo.GreenPosCtrl(scopeLocal, shipService, voyageService);

            expect(scopeLocal.isVisible("destination")).toBe(true);
            expect(scopeLocal.isVisible("etaOfArrival")).toBe(true);
            expect(scopeLocal.isVisible("deviation")).toBe(false);

            scopeLocal.report.reportType = "FR";
            expect(scopeLocal.isVisible("destination")).toBe(false);
            expect(scopeLocal.isVisible("personsOnBoard")).toBe(false);
            expect(scopeLocal.isVisible("weather")).toBe(true);

            scopeLocal.report.reportType = "DR";
            expect(scopeLocal.isVisible("destination")).toBe(false);
            expect(scopeLocal.isVisible("iceInformation")).toBe(false);
            expect(scopeLocal.isVisible("deviation")).toBe(true);
        });

        it('Loading ng-view content loads the map', function() {
            var ctrl = new embryo.GreenPosCtrl(scope, shipService, voyageService);
            scope.loadMap();

            // expect empty array when longitude and latitude has not been set
            expect(scope.pointLayer.features.length).toBe(0);

        });

        it('Point is shown on map', function() {
            var ctrl = new embryo.GreenPosCtrl(scope, shipService, voyageService);
            scope.loadMap();

            // expect empty array when longitude and latitude has not been set
            expect(scope.pointLayer.features.length).toBe(0);

            // set data to provoke change
            scope.$apply(function() {
                scope.report.longitude = -35.0;
                scope.report.latitude = 74.0;
            });

            expect(scope.pointLayer.features.length).toBe(1);
            var transformedPoint = scope.pointLayer.features[0].geometry.transform(scope.map.getProjectionObject(),
                    new OpenLayers.Projection(scope.projection));

            expect(transformedPoint.x).toBeCloseTo(-35.0);
            expect(transformedPoint.y).toBeCloseTo(74.0);
        });

    });

    describe('embryo.GreenposListCtrl', function() {
        beforeEach(inject(function($injector, $sniffer) {
            GreenposService = {
                findReports : function(params, callback) {
                    callback(null);
                }
            };

            $compile = $injector.get('$compile');
            scope = $injector.get('$rootScope');

            // var ctrl = $controller(embryo.RouteUploadCtrl, {$scope: scope});
            new embryo.GreenposListCtrl(scope, GreenposService);
        }));

        it('utc returns a UTC date', function() {
            // expect empty array when longitude and latitude has not been set
            var utcDate = scope.utc(1380208109000); 
            expect(utcDate).toBeDefined();
            expect(utcDate.toString()).toEqual('Thu Sep 26 2013 15:08:29 GMT+0200 (CEST)');
        });

        
    });

});