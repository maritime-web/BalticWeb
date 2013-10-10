(function() {
    "use strict";

    var module = angular.module('embryo.reportComp', [ 'embryo.shipService', 'embryo.voyageService',
            'embryo.routeService', 'embryo.greenposService' ]);

    module.controller('ReportCompCtrl', function($scope, $timeout, ShipService, VoyageService, RouteService,
            GreenposService) {
        
        function updateData() {
            ShipService.getYourShip(function(ship) {
                $scope.ship = ship;

                VoyageService.getCurrentPlan(ship.mmsi, function(plan) {
                    $scope.voyagePlan = plan;
                });

                GreenposService.getLatestReport(ship.maritimeId, function(latestReport) {
                    $scope.greenpos = latestReport;
                });

                RouteService.getYourActive(ship.mmsi, function(route) {
                    $scope.route = route;
                    $scope.active = !route ? "/active" : "" ;
                });
            });
        }
        
        embryo.authenticated(function() {
            $scope.$apply(function() {
                updateData();
            });
        });

        $scope.$on("yourshipDataUpdated", function() {
            console.log('yourshipDataUpdated');
            
            updateData();
        });

        $scope.$watch('ship', function(newShip, oldShip) {
            if (!newShip) {
                $scope.shipTxt = 'UNKNOWN';
                $scope.shipLabel = '';
                return;
            }

            if (newShip) {
                var property, values = [];
                var propertyNames = Object.keys(newShip);
                for (property in newShip) {
                    if (newShip[property] || newShip[property] == false) {
                        values.push(newShip[property]);
                    }
                }

                if (propertyNames.length !== values.length) {
                    $scope.shipTxt = 'INVALID';
                    $scope.shipLabel = 'label-warning';
                    return;
                }
            }

            $scope.shipTxt = 'OK';
            $scope.shipLabel = 'label-success';
        }, true);

        $scope.$watch('voyagePlan', function(newPlan, old) {
            if (!newPlan || Object.keys(newPlan).length === 0) {
                $scope.voyageTxt = 'MISSING';
                $scope.voyageLabel = 'label-warning';
                return;
            }

            if (!newPlan.voyages || newPlan.voyages.length === 0) {
                $scope.voyageTxt = 'INVALID';
                $scope.voyageLabel = 'label-warning';
                return;
            }

            $scope.voyageTxt = 'OK';
            $scope.voyageLabel = 'label-success';
        }, true);

        $scope.$watch('route', function(newRoute, old) {
            if (!newRoute) {
                $scope.routeTxt = 'MISSING';
                $scope.routeLabel = '';
                $scope.routeEditTxt = null;
                return;
            }

            $scope.routeTxt = 'ACTIVE';
            $scope.routeEditTxt = 'deactivate';
            $scope.routeLabel = 'label-success';
        }, true);

        function getPeriod(dateLong) {
            var date = new Date(dateLong);
            if (date.getUTCHours() >= 0 && date.getUTCHours() < 6) {
                return {
                    from : Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), 0, 0),
                    to : Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), 6, 0),
                };
            } else if (date.getUTCHours() >= 6 && date.getUTCHours() < 12) {
                return {
                    from : Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), 6, 0),
                    to : Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), 12, 0),
                };
            }
            if (date.getUTCHours() >= 12 && date.getUTCHours() < 18) {
                return {
                    from : Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), 12, 0),
                    to : Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), 18, 0),
                };
            }
            if (date.getUTCHours() >= 18 && date.getUTCHours() <= 23) {
                return {
                    from : Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), 18, 0),
                    to : Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate() + 1, 0, 0),
                };
            }
        }

        function evalGreenpos(greenpos) {
            if (!greenpos || !greenpos.ts) {
                $scope.greenposTxt = 'DUE NOW';
                $scope.greenposLabel = 'label-warning blink';
                return;
            }

            
            if (greenpos.reportType === 'FR') {
                $scope.greenposTxt = 'OK';
                $scope.greenposLabel = 'label-success';
            }

            var now = Date.now();
            var period = getPeriod(now);

            // Allow for reports to be performed 15 minutes before reporting
            // hour.
            // if last report performed more than 15 minutes before reporting
            // period then perform new report
            if (greenpos.ts < (period.from - 900000) && now < (period.from + 1800000)) {
                $scope.greenposTxt = 'DUE NOW';
                $scope.greenposLabel = 'label-warning blink';
                return;
            }

            // if last report not performed more than ½ later than reporting
            // hour, then highlight.
            if (greenpos.ts < (period.from - 900000) && now >= (period.from + 1800000)) {
                $scope.greenposTxt = 'DUE NOW';
                $scope.greenposLabel = 'label-important blink';
                return;
            }

            $scope.greenposTxt = 'OK';
            $scope.greenposLabel = 'label-success';
        };

        $scope.$watch('greenpos', function(lastGreenpos, old) {
            evalGreenpos(lastGreenpos);
        }, true);

        function timedEvaluation() {
            evalGreenpos($scope.greenpos);
            $timeout(timedEvaluation, 15000);
        }

        $timeout(timedEvaluation, 15000);
    });

}());
