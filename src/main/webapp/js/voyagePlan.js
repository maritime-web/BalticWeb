/*
 * Dependencies:
 * 
 * aisview.js
 * aisviewUI.js
 * route.js
 * ....
 */

(function() {
    "use strict";

    var berthUrl = embryo.baseUrl + 'rest/berth/search';

    var voyagePlanModule = angular.module('embryo.voyagePlan', [ 'embryo.voyageService', 'embryo.routeService',
            'siyfion.ngTypeahead' ]);

    embryo.VoyagePlanCtrl = function($scope, $routeParams, VoyageService, RouteService) {
        var voyagePlan;

        var loadVoyage = function() {
            if ($routeParams.voyage === 'current') {
                VoyageService.getCurrent($scope.mmsi, function(plan) {
                    voyagePlan = plan;
                    $scope.voyages = voyagePlan.voyages.slice();
                    $scope.voyages.push({});
                });
            }

        };
        var loadActiveRoute = function() {
            RouteService.getActive($scope.mmsi, function(route) {
                $scope.activeRoute = route;
            });
        };

        $scope.mmsi = $routeParams.mmsi;

        $scope.options = {
            "Yes" : true,
            "No" : false
        };

        $scope.berths = {
            name : 'embryo_berths',
            prefetch : {
                url : berthUrl,
                // 1 time
                ttl : 3600000
            },
            remote : berthUrl
        };

        loadVoyage();
        loadActiveRoute();

        $scope.getLastVoyage = function() {
            if (!$scope.voyages) {
                return null;
            }
            return $scope.voyages[$scope.voyages.length - 1];
        };

        $scope.$watch($scope.getLastVoyage, function(newValue, oldValue) {
            // add extra empty voyage on initialization
            if (newValue && Object.keys(newValue).length > 0 && Object.keys(oldValue).length === 0) {
                $scope.voyages.push({});
            }
        }, true);

        $scope.del = function(index) {
            $scope.voyages.splice(index, 1);
        };

        $scope.berthSelected = function(voyage, datum) {
            if (typeof datum !== 'undefined') {
                voyage.latitude = datum.latitude;
                voyage.longitude = datum.longitude;
            }
        };

        $scope.isActive = function(voyage) {
            if (!voyage || !voyage.routeId) {
                return false;
            }

            if (!$scope.activeRoute) {
                return false;
            }

            return $scope.activeRoute.id === voyage.routeId;
        };

        $scope.uploadLink = function(voyage) {
            // if (!voyage.maritimeId) {
            // voyage.maritimeId = Math.uuid(17);
            // }

            return '#/routeUpload/' + $scope.mmsi + '/' + voyage.maritimeId;
        };

        $scope.routeDisabled = function(voyage) {
            return typeof voyage.maritimeId === 'undefined';
        };

        $scope.editRouteLink = function(voyage) {
            if (voyage.routeId) {
                return '#/routeEdit/' + $scope.mmsi + '/' + voyage.routeId;
            }
            // if (!voyage.maritimeId) {
            // voyage.maritimeId = Math.uuid(17);
            // }
            return '#/routeNew/' + $scope.mmsi + '/' + voyage.maritimeId;
        };

        $scope.activate = function(voyage) {
            RouteService.setActiveRoute(voyage.routeId, true, function() {
                loadActiveRoute();
            });
        };
        $scope.deactivate = function(voyage) {
            RouteService.setActiveRoute(voyage.routeId, false, function() {
                loadActiveRoute();
            });
        };

        $scope.reset = function() {
            $scope.message = null;
            $scope.alertMessage = null;
            loadVoyage();
            loadActiveRoute();
        };
        $scope.save = function() {
            // remove last empty element
            voyagePlan.voyages = $scope.voyages.slice(0, $scope.voyages.length - 1);

            VoyageService.save(voyagePlan, function() {
                $scope.message = "Voyage plan saved successfully";
                loadVoyage();
                loadActiveRoute();
            });
        };
    };
}());
