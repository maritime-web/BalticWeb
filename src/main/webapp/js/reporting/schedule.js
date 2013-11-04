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
            'siyfion.typeahead' ]);

    embryo.ScheduleCtrl = function($scope, $rootScope, VesselService, VoyageService, RouteService, $location) {
        var schedule;
        var loadVoyage = function() {
            VoyageService.getSchedule($scope.mmsi, function(ss) {
                schedule = ss;
                $scope.voyages = schedule.voyages.slice();
                $scope.voyages.push({});
            });
        };

        embryo.controllers.schedule = {
            title : "Schedule",
            status : function(vesselOverview, vesselDetails) {
                var status = {
                    message : "INACTIVE",
                }

                if (vesselDetails.additionalInformation.routeId) {
                    status.message = "ACTIVE";
                    status.code = "success";
                }

                return status;
            },
            show : function(context) {
                $scope.mmsi = context.vesselDetails.mmsi;
                $scope.activeRouteId = context.vesselDetails.additionalInformation.routeId;
                loadVoyage();
                $scope.$apply(function() {
                });
                $("#schedulePanel").css("display", "block");
            },
            hide : function() {
                $("#schedulePanel").css("display", "none");
                $scope.reset();
            }
        };

        $scope.options = {
            "Yes" : true,
            "No" : false
        };

        $scope.berths = {
            name : 'embryo_berths2',
            prefetch : {
                url : berthUrl,
                // 1 time
                ttl : 3600000
            },
            remote : berthUrl
        };

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

            return $scope.activeRouteId === voyage.route.id;
        };

        $scope.editRoute = function(index) {
            var context = {
                mmsi : $scope.mmsi,
                fromVoyage : $scope.voyages[index]
            };
            if (index < $scope.voyages.length - 1) {
                context.toVoyage = $scope.voyages[index + 1];
            }
            embryo.controllers.editroute.show(context);
        };

        $scope.uploadRoute = function(voyage) {
            embryo.controllers.uploadroute.show({
                mmsi : $scope.mmsi,
                voyageId : voyage.maritimeId
            });
        };

        $scope.activate = function(voyage) {
            RouteService.setActiveRoute(voyage.route.id, true, function() {
                VesselService
                        .updateVesselDetailParameter($scope.mmsi, "additionalInformation.routeId", voyage.route.id);
                $scope.activeRouteId = voyage.route.id;
            });
        };
        $scope.deactivate = function(voyage) {
            RouteService.setActiveRoute(voyage.route.id, false, function() {
                VesselService.updateVesselDetailParameter($scope.mmsi, "additionalInformation.routeId", "");
                $scope.activeRouteId = null;
            });
        };

        $scope.reset = function() {
            $scope.message = null;
            $scope.alertMessage = null;
            loadVoyage();
        };
        $scope.save = function() {
            var index;
            // remove last empty element
            schedule.voyages = $scope.voyages.slice(0, $scope.voyages.length - 1);

            for (index in schedule.voyages) {
                delete schedule.voyages[index].route;
            }

            VoyageService.save(schedule, function() {
                $rootScope.$broadcast('yourshipDataUpdated');
                $scope.message = "Schedule saved successfully";
                loadVoyage();
            });
        };
    };

}());
