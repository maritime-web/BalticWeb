/*
 * Dependencies:
 * 
 * aisview.js
 * aisviewUI.js
 * ....
 */

(function() {
    "use strict";

    var module = angular.module('embryo.routeEdit', [ 'embryo.voyageService', 'embryo.routeService', 'ui.bootstrap',
            'ui.bootstrap.datetimepicker' ]);

    function setDefault(context, field, defaultValue) {
        if (!context[field]) {
            context[field] = defaultValue;
        }
    }

    embryo.RouteEditCtrl = function($scope, RouteService, VoyageService, VesselService) {
        function initRouteMeta(route, meta) {
            setDefault(route, "etaDep", meta.etdep);
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
                });
            } else {
                $scope.route = {};
                initRouteMeta($scope.route, $scope.scheduleData);
            }
        }

        embryo.controllers.editroute = {
            notitle : "not shown in left panel",
            show : function(context) {
                clearAdditionalInformation();
                $scope.mmsi = context.mmsi;

                if (context.fromVoyage.route && context.fromVoyage.route.id) {
                    $scope.routeId = context.fromVoyage.route.id;
                }

                $scope.scheduleData = {
                    voyageId : context.fromVoyage.maritimeId,
                    dep : context.fromVoyage.berthName,
                    etdep : context.fromVoyage.departure,
                    des : context.toVoyage.berthName,
                    etdes : context.toVoyage.arrival
                };
                $scope.reset();
                $("#routeEditPanel").css("display", "block");
            },
            hide : function() {
                $("#routeEditPanel").css("display", "hide");
            }
        };

        $scope.save = function() {
            $scope.message = null;
            RouteService.save($scope.route, $scope.scheduleData.voyageId, function() {
                $scope.message = "Saved route '" + $scope.route.name + "'";

                // TODO replace this with a thrown event
                // embryo.route.redrawIfVisible(RouteService.getRoute());
            });
        };

        $scope.saveAndActivate = function() {
            RouteService.saveAndActivate($scope.route, $scope.scheduleData.voyageId, function() {
                $scope.message = "Saved and activated route '" + $scope.route.name + "'";
                VesselService
                .updateVesselDetailParameter($scope.mmsi, "additionalInformation.routeId", $scope.route.id);

                // TODO replace this with a thrown event
                // embryo.route.redrawIfVisible(RouteService.getRoute());
            });
        };

        $scope.saveable = function() {
            if ($scope.routeEditForm.$invalid) {
                return false;
            }

            if (!$scope.route) {
                return false;
            }

            if (!($scope.route.wps && $scope.route.wps.length >= 2)) {
                return false;
            }

            return true;
        };

        $scope.reset = function() {
            $scope.alertMessage = null;
            $scope.message = null;
            initRoute();
        };
    };
}());
