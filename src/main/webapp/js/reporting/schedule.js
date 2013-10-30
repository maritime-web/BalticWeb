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

    var scheduleModule = angular.module('embryo.schedule', [ 'embryo.voyageService', 'embryo.routeService',
            'siyfion.ngTypeahead' ]);

    embryo.ScheduleCtrl = function($scope, $rootScope, $routeParams, VoyageService, RouteService, $location) {
        var schedule;

        var loadVoyage = function() {
            if ($routeParams.voyage === 'current') {
                VoyageService.getSchedule($scope.mmsi, function(s) {
                    schedule = s;
                    $scope.voyages = schedule.voyages.slice();
                    $scope.voyages.push({});

                    console.log(schedule);
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
            if (!voyage || !voyage.route || !voyage.route.id) {
                return false;
            }

            if (!$scope.activeRoute) {
                return false;
            }

            return $scope.activeRoute.id === voyage.route.id;
        };

        $scope.uploadLink = function(voyage) {
            // if (!voyage.maritimeId) {
            // voyage.maritimeId = Math.uuid(17);
            // }

            return '#/routeUpload/' + $scope.mmsi + '/voyage/' + voyage.maritimeId;
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

        $scope.editRoute = function(voyage) {
            if (voyage && voyage.route && voyage.route.id) {
                $location.path('/routeEdit/' + $scope.mmsi + '/' + voyage.route.id);
            } else {
                $location.path('/routeEdit/' + $scope.mmsi + '/' + voyage.maritimeId);
            }
        };

        $scope.uploadRoute = function(voyage) {
            $location.path('/routeUpload/' + $scope.mmsi + '/voyage/' + voyage.maritimeId);
        };

        $scope.activate = function(voyage, $event) {
            $event.preventDefault();

            RouteService.setActiveRoute(voyage.route.id, true, function() {
                loadActiveRoute();
            });
        };
        $scope.deactivate = function(voyage, $event) {
            $event.preventDefault();

            RouteService.setActiveRoute(voyage.route.id, false, function() {
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
            var index;
            // remove last empty element
            schedule.voyages = $scope.voyages.slice(0, $scope.voyages.length - 1);

            for (index in schedule.voyages) {
                delete schedule.voyages[index].route;
            }

            VoyageService.saveScedule(schedule, function() {
                $rootScope.$broadcast('yourshipDataUpdated');
                $scope.message = "Schedule saved successfully";
                loadVoyage();
                loadActiveRoute();
            });
        };
    };
}());
