/*
 * Dependencies:
 * 
 * aisview.js
 * aisviewUI.js
 * position.js
 * ....
 */

(function() {
	"use strict";

	var serviceModule = angular.module('embryo.shipService',['embryo.storageServices', 'ngResource']);

	serviceModule.factory('AisRestService', function($resource) {
		var defaultParams = {};
		var actions = {
			findVesselsByMmsi : {
				params : {
					action : 'vessel_search'
				},
				method : 'GET',
				isArray : false,
			}
		};
		return $resource('json_proxy/:action?argument=:mmsi', defaultParams, actions);
	});

	
	serviceModule.factory('ShipService', function($http, SessionStorageService, LocalStorageService) {
		return {
			getYourShip : function(callback) {

				var remoteCall = function(onSuccess) {
					$http.get('rest/ship/yourship', {
						responseType : 'json'
					}).success(onSuccess);
				};

				SessionStorageService.getItem('yourShip', callback, remoteCall);
			},
			getShipTypes : function(callback) {
				var remoteCall = function(onSuccess) {
					$http.get('rest/ship/shiptypes', {
						responseType : 'json'
					}).success(onSuccess);
				};

				LocalStorageService.getItem('shipTypes', callback, remoteCall);
			},
			save : function(ship, callback) {
				$http.put('rest/ship', ship, {
					responseType : 'json'
				}).success(function(maritimeId) {
					if (maritimeId) {
						ship.maritimeId = maritimeId;
						SessionStorageService.setItem('yourShip', ship);
					}
					callback();
				});
			}
		};
	});
}());
