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

	var shipModule = angular.module('embryo.shipInformation',['embryo.shipService']);

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
