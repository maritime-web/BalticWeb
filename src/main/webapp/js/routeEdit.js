/*
 * Dependencies:
 * 
 * aisview.js
 * aisviewUI.js
 * ....
 */

(function() {
	"use strict";

	var module = angular.module('embryo.routeEdit',['embryo.voyageService', 'embryo.routeService', 'ui.bootstrap']);

	embryo.RouteEditCtrl = function($scope, $routeParams, RouteService, VoyageService) {
		if ($routeParams.mmsi) {
			VoyageService.getVoyages($routeParams.mmsi, function(voyages) {
				$scope.voyages = voyages;
			});
		}

		var initRoute = function() {
			if ($routeParams.routeId) {
				RouteService.getRoute($routeParams.routeId, function(route) {
					$scope.route = route;
				});
			} else {
				$scope.route = {};
			}
		};

		initRoute();

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
