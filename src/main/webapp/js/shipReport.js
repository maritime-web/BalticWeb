/*
 * Dependencies:
 * 
 * aisview.js
 * aisviewUI.js
 * ....
 */

"use strict";

embryo.greenPos = {};
embryo.greenPos.Ctrl = function($scope, ShipService, GreenPos, VoyageService) {

	$scope.report = {
		reportType : "SP",
	};

	ShipService.getYourShip(function(yourShip) {
		$scope.report.shipMmsi = yourShip.mmsi;
		$scope.report.shipCallSign = yourShip.callSign;
		$scope.report.shipName = yourShip.name;
		$scope.report.shipMaritimeId = yourShip.maritimeId;
	});

	VoyageService.getYourActive(function(voyage) {
		console.log(voyage);
		$scope.report.destination = voyage.berthName;
		$scope.report.etaOfArrival = voyage.arrival;
		$scope.report.personsOnBoard = voyage.personsOnBoard;
	});

	// can speed and course be preset with ais data? 
	
	$scope.isShip = function() {
		return true;
	};

	$scope.sendReport = function() {
		console.log('trying to send report');
		GreenPos.save($scope.report, function() {
			$scope.message="GreenPos report successfully submitted";
			console.log("GreenPos successfully submitted");
		 });
	};

	$scope.chosenType = function(types) {
		return jQuery.inArray($scope.report.reportType, types) > -1;
	};
	
	$scope.cancel = function(){
		
	};

	$scope.clear = function(){
		
	};
};

embryo.ShipService = {
	getYourShipRemote : function(onSuccess) {
		$.getJSON('rest/ship/yourship', function(data) {
			console.log(data);
			onSuccess(data.ship);
		});
	}
};

var angularApp = angular.module('embryo', [ 'ngResource' ]);
// , 'ui.bootstrap'

angularApp.factory('ShipRestService', function($resource) {
	var defaultParams = {};
	var actions = {
		getYourShip : {
			params : {
				action : 'yourship'
			},
			method : 'GET',
			isArray : false,
		}
	};
	return $resource('rest/ship/:action/:id', defaultParams, actions);
});

angularApp.factory('ShipService', function(ShipRestService) {
	return {
		getYourShip : function(onSuccess) {
			var shipStr = sessionStorage.getItem('yourShip');
			if (!shipStr) {
				var ship = ShipRestService.getYourShip(function() {
					var shipStr = JSON.stringify(ship);
					sessionStorage.setItem('yourShip', shipStr);
					onSuccess(ship);
				});
			} else {
				var yourShip = JSON.parse(shipStr);
				onSuccess(yourShip);
			}
		}
	};
});

angularApp.factory('VoyageRestService', function($resource) {
	var defaultParams = {};
	var actions = {
		getActive : {
			params : {
				action : 'active'
			},
			method : 'GET',
			isArray : false,
		}
	};
	return $resource('rest/voyage/:action/:id', defaultParams, actions);
});

angularApp.factory('VoyageService', function(VoyageRestService, ShipService) {
	return {
		getYourActive : function(onSuccess) {
			var voyageStr = sessionStorage.getItem('activeVoyage');
			if (!voyageStr) {
				ShipService.getYourShip(function(yourShip){
					console.log(yourShip.maritimeId);
					var voyage = VoyageRestService.getActive({id:yourShip.maritimeId}, function() {
						var voyageStr = JSON.stringify(voyage);
						sessionStorage.setItem('activeVoyage', voyageStr);
						onSuccess(voyage);
					});
				});				
			} else {
				var voyage = JSON.parse(voyageStr);
				onSuccess(voyage);
			}
		}
	};
});

angularApp.factory('GreenPos', function($resource) {
	var defaultParams = {};

	var actions = {
		activate : {
			params : {
				action : 'activate'
			},
			method : 'PUT',
			isArray : false,
		}

	};

	return $resource('rest/greenpos/:action/:id', defaultParams, actions);
});

angularApp.config([ '$routeProvider', function($routeProvider) {
	$routeProvider.when('/test', {
		templateUrl : 'partials/testPartial.html',
		controller : embryo.greenPos.Ctrl
	}).when('/report', {
		templateUrl : 'partials/shipReport.html',
		controller : embryo.greenPos.Ctrl
	}).when('/report/:mmsi', {
		templateUrl : 'partials/shipReport.html',
		controller : embryo.greenPos.Ctrl
	}).otherwise({
		redirectTo : '/report'
	});
} ]);