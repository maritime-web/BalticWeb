(function() {
    "use strict";

    var module = angular.module('embryo.selectedShip', [ 'embryo.shipService', 'embryo.voyageService',
            'embryo.routeService' ]);

    module.controller('SelectedShipCtrl', function($scope, ShipService, RouteService) {
        embryo.authenticated(function() {
            $scope.$apply(function() {
                ShipService.getYourShip(function(ship) {
                    $scope.yourMmsi = ship.mmsi;
                });
            });
        });

        function updateData() {
            // provide information while querying the server
            $scope.routeTxt = 'NOT AVAILABLE';
            $scope.routeLabel = '';
            $scope.routeLinkTxt = null;

            if ($scope.mmsi) {
                if ($scope.mmsi == $scope.yourMmsi) {
                    $scope.routeLabel = 'label-success';
                    $scope.routeTxt = 'ACTIVE';
                    $scope.routeLinkTxt = null;
                    return;
                }

                // query the server (or fetched cached data)
                RouteService.getActive($scope.mmsi, function(route) {
                    $scope.route = route;
                    if (route) {
                        $scope.routeLabel = 'label-success';
                        $scope.routeTxt = 'AVAILABLE';
                        $scope.routeLinkTxt = 'view';
                    } else {
                        $scope.routeTxt = 'NOT AVAILABLE';
                        $scope.routeLabel = '';
                        $scope.routeLinkTxt = null;
                    }
                });
            }
        }

        $scope.$watch('mmsi', function(newId, oldId) {
            updateData();
        });

        embryo.vesselSelected(function(e) {
            var vessel = embryo.vessel.lookupVessel(e.vesselId);

            $scope.$apply(function() {
                $scope.mmsi = vessel.mmsi;
            });
        });

        $scope.toggleShow = function() {
            if ($scope.routeLinkTxt === 'view') {
                embryo.route.draw($scope.route, false);
                $scope.routeLinkTxt = 'hide';
            } else {
                embryo.route.remove($scope.route);
                $scope.routeLinkTxt = 'view';
            }
        };
    });

}());
