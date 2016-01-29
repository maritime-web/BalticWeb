(function() {
    "use strict";

    var module = angular.module('embryo.routeEdit', [ 'embryo.routeService', 'embryo.datepicker', 'embryo.position',
            'embryo.controller.reporting' ]);

    function setDefault(context, field, defaultValue) {
        if (!context[field]) {
            context[field] = defaultValue;
        }
    }

    embryo.RouteEditCtrl = function($scope, RouteService, VesselService, ScheduleService, VesselInformation) {
        function initRouteMeta(route, meta) {
            //setDefault(route, "etaDep", $scope.scheduleData.etdep);
            route.etaDep = $scope.scheduleData.etdep;
            route.formatEtaDep = function() {
                return formatTime(route.etaDep);
            };
            setDefault($scope, "etaDes", meta.etdes);
            setDefault(route, "dep", meta.dep);
            setDefault(route, "des", meta.des);
        }

        function initRoute() {
            if ($scope.routeId) {
                RouteService.getRoute($scope.routeId, function(route) {
                    $scope.route = route;
                    initRouteMeta($scope.route, $scope.scheduleData);
                    $scope.date = new Date(route.etaDeparture);

                    $scope.waypoints = route.wps.slice();
                    $scope.waypoints.push({});
                });
            } else {
                $scope.route = {};
                $scope.waypoints = [ {} ];
                initRouteMeta($scope.route, $scope.scheduleData);
            }
        }

        $scope.provider = {
            doShow : false,
            title : "Route edit",
            show : function(context) {
                this.doShow = true;
                $scope.mmsi = context.mmsi;
                $scope.routeId = context.routeId;
                $scope.vesselDetails = context.vesselDetails;

                $scope.date = new Date();

                $scope.scheduleData = {
                    voyageId : context.voyageId,
                    dep : context.dep,
                    etdep : context.etdep,
                    des : context.des,
                    etdes : context.etdes
                };
                $scope.reset();
            },
            close : function() {
                this.doShow = false;
            }
        };

        embryo.controllers.editroute = $scope.provider;
        VesselInformation.addInformationProvider($scope.provider);

        $scope.close = function($event) {
            $event.preventDefault();
            $scope.provider.close();
        };

        $scope.getLastWaypoint = function() {
            if (!$scope.waypoints) {
                return null;
            }
            return $scope.waypoints[$scope.waypoints.length - 1];
        };

        $scope.$watch($scope.getLastWaypoint, function(newValue, oldValue) {
            // add extra empty voyage on initialization
            if (newValue && Object.keys(newValue).length > 0 && Object.keys(oldValue).length === 0) {
                $scope.waypoints.push({});
            }
        }, true);

        $scope.activeVoyage = function() {
            return $scope.vesselDetails.additionalInformation.routeId == $scope.routeId;
        };

        $scope.remove = function(index) {
            $scope.waypoints.splice(index, 1);
        };

        $scope.add = function(index) {
            $scope.waypoints.splice(index + 1, 0, {});
        };

        $scope.save = function() {
            $scope.message = null;
            $scope.route.wps = $scope.waypoints.slice(0, $scope.waypoints.length - 1);
            RouteService.save($scope.route, $scope.scheduleData.voyageId, function() {
                $scope.message = "Saved route '" + $scope.route.name + "'";
                ScheduleService.clearYourSchedule();
            }, function(error) {
                $scope.alertMessages = error;
            });
        };

        $scope.saveAndActivate = function() {
            $scope.message = null;
            $scope.route.wps = $scope.waypoints.slice(0, $scope.waypoints.length - 1);
            RouteService.saveAndActivate($scope.route, $scope.scheduleData.voyageId, function() {
                $scope.message = "Saved and activated route '" + $scope.route.name + "'";
                VesselService
                        .updateVesselDetailParameter($scope.mmsi, "additionalInformation.routeId", $scope.route.id);
                ScheduleService.clearYourSchedule();
            }, function(error) {
                $scope.alertMessages = error;
            });
        };

        $scope.reset = function() {
            $scope.alertMessages = null;
            $scope.message = null;
            initRoute();
        };
    };

    module.controller("RouteEditFormCtrl", [ '$scope', 'VesselInformation', function($scope, VesselInformation) {
        $scope.saveable = function() {
            if ($scope.routeEditForm.$invalid) {
                return false;
            }

            if (!$scope.route) {
                return false;
            }

            if (!($scope.waypoints && $scope.waypoints.length >= 3)) {
                return false;
            }

            return true;
        };

    } ]);

}());
