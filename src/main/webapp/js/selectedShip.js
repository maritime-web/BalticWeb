(function() {
    "use strict";

    console.log('before loading selected ship');

    var module = angular.module('embryo.selectedShip', [ 'embryo.shipService', 'embryo.voyageService',
            'embryo.routeService', 'embryo.metoc' ]);

    module.controller('SelectedShipCtrl', function($scope, ShipService, RouteService, MetocService) {
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

        $scope.$watch('routeLinkTxt', function(newValue, oldValue) {
            if (newValue == 'hide') {
                // I can now see a route
                $scope.metocTxt = 'NOT SHOWN';
                $scope.metocLabel = '';
                $scope.metocLinkTxt = 'view';
            } else {
                $scope.metocTxt = 'NOT AVAILABLE';
                $scope.metocLabel = '';
                $scope.metocLinkTxt = null;
                // route hidden visible
                if ($scope.metoc) {
                    embryo.metoc.remove($scope.metoc);
                }
            }
        });

        embryo.vesselSelected(function(e) {
            var vessel = embryo.vessel.lookupVessel(e.vesselId);

            $scope.$apply(function() {
                $scope.mmsi = vessel.mmsi;
            });
        });

        $scope.toggleShowMetoc = function() {
            if ($scope.metocLinkTxt === 'view') {
                MetocService.getMetoc($scope.route.id, function(metoc) {
                    $scope.metoc = metoc;
                    embryo.metoc.draw(metoc);
                });
                $scope.metocTxt = 'SHOWN';
                $scope.metocLabel = 'label-success';
                $scope.metocLinkTxt = 'hide';
            } else {
                $scope.metocTxt = 'NOT SNOWN';
                $scope.metocLabel = '';
                $scope.metocLinkTxt = 'view';
                embryo.metoc.remove($scope.metoc);
            }
        };

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
