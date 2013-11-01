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

    embryo.RouteEditCtrl = function($scope, RouteService, VoyageService) {
        function initRoute() {
            if ($scope.routeId) {
                RouteService.getRoute($scope.routeId, function(route) {
                    $scope.route = route;
                    $scope.date = new Date(route.etaDeparture);
                });
            } else {
                $scope.route = {};
            }
        }

        embryo.controllers.editroute = {
            notitle : "not shown in left panel",
            show : function(context) {
                clearAdditionalInformation();
                $scope.mmsi = context.mmsi;
                $scope.routeId = context.routeId;
                $scope.reset();
                $("#routeEditPanel").css("display", "block");
            },
            hide : function() {
                $("#routeEditPanel").css("display", "hide");
            }
        };

        $scope.save = function() {
            $scope.message = null;

            RouteService.save($scope.route, function() {
                $scope.message = "Saved route '" + $scope.route.name + "'";
                // Route not fetch from server, which might be a good idea.

                // TODO replace this with a thrown event
                // embryo.route.redrawIfVisible(RouteService.getRoute());
            });
        };

        $scope.saveable = function() {
            if ($scope.routeEditForm.$invalid) {
                return false;
            }

            if (!($scope.route.waypoints && $scope.route.waypoints.length >= 2)) {
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
