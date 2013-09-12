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
							$http.get('rest/route/active/220443000', {
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
								var setActiveCallback = function(route) {
									SessionStorageService.setItem(active, route.id);
									callback(route);
								};
								
								SessionStorageService.getItem(routeKey(routeId), setActiveCallback, remoteCall);
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
