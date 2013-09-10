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

	embryo.angular.factory('ShipService', function($http, SessionStorageService, LocalStorageService) {
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

	embryo.ShipInformationCtrl = function($scope, ShipService) {
		var loadData = function(){
			ShipService.getYourShip(function(ship) {
				$scope.ship = ship;
			});

			ShipService.getShipTypes(function(types) {
				$scope.types = types;
			});
		};
		
		$scope.$on('$viewContentLoaded', function(event) {
			loadData();
		});

		$scope.save = function() {
			ShipService.save($scope.ship, function() {
				$scope.message = "Ship information successfully submitted";
			});
		};

		$scope.reset = function() {
			// TODO find a way to hide these
			$scope.message = null;
			$scope.alertMessage = null;
			loadData();
		};
	};

}());
