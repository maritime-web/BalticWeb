/*
 * Dependencies:
 * 
 * aisview.js
 * aisviewUI.js
 * ....
 */

(function() {
	"use strict";

	angular.module('embryo.routeService', [ 'embryo.storageServices' ]).factory('RouteService',
			function($http, SessionStorageService, LocalStorageService) {

				var active = 'route_active';

				var routeKey = function(routeId) {
					return 'route_' + routeId;
				};

				return {
					getActive : function(mmsi, callback) {
						var remoteCall = function(onSuccess) {
							var url = 'rest/route/active/' + mmsi;
							$http.get(url, {
								responseType : 'json'
							}).success(onSuccess);
						};

						// active route is maintained in SessionStorage as two
						// key value
						// pairs:
						// 'route_active' -> someRouteId
						// 'route_someRouteId' -> Route (stringified JS object)
						SessionStorageService.getItem(active, function(routeId) {
							if (routeId) {
								SessionStorageService.getItem(routeKey(routeId), callback, remoteCall);
							} else {
								remoteCall(function(activeRoute){
									if (typeof activeRoute !== 'undefined') {
										SessionStorageService.setItem(active, activeRoute.id);
										SessionStorageService.setItem(routeKey(activeRoute.id), activeRoute);
									} else {
										SessionStorageService.removeItem(active);
									}
								});
							}
						});
					},
					setActiveRoute : function(routeId, activity, callback) {
						var activeRoute = {
							routeId : routeId,
							active : activity
						};

						$http.put('rest/route/activate/', activeRoute, {
							responseType : 'json'
						}).success(function() {
							if (activity) {
								SessionStorageService.setItem(active, routeId);
							} else {
								SessionStorageService.removeItem(active);
							}
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

}());
