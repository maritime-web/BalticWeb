/*
 * Dependencies:
 * 
 * aisview.js
 * aisviewUI.js
 * ....
 */

(function() {
	"use strict";

	embryo.angular.factory('RouteService', function($http, SessionStorageService, LocalStorageService) {

		var active = 'route_active';

		var routeKey = function(routeId) {
			return 'route_' + routeId;
		};

		return {
			getActive : function(callback) {
				var remoteCall = function(onSuccess) {
					$http.get('rest/route/active', {
						responseType : 'json'
					}).success(onSuccess);
				};

				// active route is maintained in SessionStorage as two key value
				// pairs:
				// 'route_active' -> someRouteId
				// 'route_someRouteId' -> Route (stringified JS object)
				SessionStorageService.getItem(active, function(routeId) {
					if (routeId) {
						SessionStorageService.getItem(routeKey(routeId), callback, remoteCall);
					} else {
						var setActiveCallback = function(route) {
							SessionStorageService.setActive(active, route.id);
							callback(route);
						};
						SessionStorageService.getItem(routeKey(routeId), setActiveCallback, remoteCall);
					}
				});
			},
			activate : function(routeId, callback) {
				$http.get('rest/route/activate/' + routeId, {
					responseType : 'json'
				}).success(function() {
					SessionStorageService.setItem(active, routeId);
					callback();
				});
			},
			getRoute : function(routeId, callback) {
				// should routes be cached?
				var remoteCall = function(onSuccess) {
					$http.get('rest/route/' + routeId, {
						responseType : 'json'
					}).success(onSuccess);
				};

				SessionStorageService.getItem(routeKey(routeId), callback, remoteCall);
			},
			save : function(route, callback) {
				$http.put('rest/route', route, {
					responseType : 'json'
				}).success(function() {
					SessionStorageService.setItem(routeKey(route.id), route);
					callback();
				});
			}
		};
	});

	embryo.RouteEditCtrl = function($scope, $routeParams, RouteService) {
		console.log($routeParams);
		console.log("hej");

		var initRoute = function() {
			if ($routeParams.routeId) {
				RouteService.getRoute($routeParams.routeId, function(route) {
					$scope.route = route;

					console.log(route);
				});
			} else {
				$scope.route = {};
			}
		};

		initRoute();

		$scope.save = function() {
			// validate?
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
