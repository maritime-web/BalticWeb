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

    var scheduleModule = angular.module('embryo.schedule', [ 'embryo.scheduleService', 'embryo.routeService',
            'siyfion.typeahead' ]);

    embryo.ScheduleCtrl = function($scope, VesselService, ScheduleService, RouteService) {
        var schedule;
        var loadSchedule = function() {
            if ($scope.mmsi) {
                ScheduleService.getYourSchedule($scope.mmsi, function(ss) {
                    schedule = ss;
                    $scope.voyages = schedule.voyages.slice();
                    $scope.voyages.push({});
                });
            }
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
                loadSchedule();
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
                routeId : $scope.voyages[index].route ? $scope.voyages[index].route.id : null,
                voyageId : $scope.voyages[index].maritimeId,
                dep : $scope.voyages[index].berthName,
                etdep : $scope.voyages[index].departure,
            };
            if (index < $scope.voyages.length - 1) {
                context.des = $scope.voyages[index + 1].berthName;
                context.etdes = $scope.voyages[index + 1].arrival;
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
            loadSchedule();
        };
        $scope.save = function() {
            var index;
            // remove last empty element
            schedule.voyages = $scope.voyages.slice(0, $scope.voyages.length - 1);

            for (index in schedule.voyages) {
                delete schedule.voyages[index].route;
            }

            ScheduleService.save(schedule, function() {
                $scope.message = "Schedule saved successfully";
                loadSchedule();
            });
        };
    };

    embryo.ScheduleViewCtrl = function($scope, ScheduleService, RouteService) {
        var schedule;
        var loadSchedule = function() {
            if ($scope.mmsi) {
                ScheduleService.getSchedule($scope.mmsi, function(ss) {
                    schedule = ss;
                    $scope.voyages = schedule.voyages.slice();
                });
            }
        };
        
        $scope.layer = new RouteLayer("#D5672F");
        addLayerToMap("vessel", $scope.layer, embryo.map);

        embryo.controllers.scheduleview = {
//            title : "Schedule View",
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
            init : function(map, group) {
            },
            show : function(context) {
                $scope.mmsi = context.vesselDetails.mmsi;
                $scope.activeRouteId = context.vesselDetails.additionalInformation.routeId;
                loadSchedule();
                $scope.$apply(function() {
                });
                $("#scheduleViewPanel").css("display", "block");
            },
            hide : function() {
                $("#scheduleViewPanel").css("display", "none");
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
        
        $scope.viewRoute = function(voyage, $event) {
            $event.preventDefault();
            RouteService.getRoute(voyage.route.id, function(route){
                $scope.layer.draw(route);
                $scope.layer.zoomToExtent();

            });
        };
    };

}());
