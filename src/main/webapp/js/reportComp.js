(function() {
    "use strict";

    var module = angular.module('embryo.reportComp', [ 'embryo.shipService', 'embryo.voyageService',
            'embryo.routeService', 'embryo.greenposService' ]);

    module.controller('ReportCompCtrl', function($scope, ShipService, VoyageService, RouteService, GreenposService) {
        function updateData() {
            ShipService.getYourShip(function(ship) {
                $scope.ship = ship;

                VoyageService.getCurrentPlan(ship.mmsi, function(plan) {
                    $scope.voyagePlan = plan;
                });

                GreenposService.getLatestReport(ship.maritimeId, function(latestReport) {
                    $scope.greenpos = latestReport;
                });

                RouteService.getActive(ship.mmsi, function(route) {
                    $scope.route = route;
                });
            });


        }

        updateData();

        $scope.$on("yourshipDataUpdated", function() {
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
                    if (newShip[property]) {
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

            console.log(newPlan);

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

        $scope.$watch('greenpos', function(lastGreenpos, old) {
            if (!lastGreenpos) {
                $scope.greenposTxt = 'DUE NOW';
                $scope.greenposLabel = 'label-warning';
                return;
            }

            $scope.greenposTxt = 'OK';
            $scope.greenposLabel = 'label-success';
        }, true);
    });

}());
