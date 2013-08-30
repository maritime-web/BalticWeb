/*
 * Dependencies:
 * 
 * aisview.js
 * aisviewUI.js
 * ....
 */

"use strict";

embryo.greenPos = {};
embryo.greenPos.Ctrl = function($scope, ShipService, VoyageService, GreenPos) {

	$scope.visibility = {
		"SP" : [ "destination", "etaOfArrival", "personsOnBoard", "course",
				"speed", "weather", "ice" ],
		"PR" : [ "course", "speed", "weather", "ice" ],
		"FR" : [ "weather", "ice" ],
		"DR" : [ "deviation" ]
	};

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
		$scope.report.destination = voyage.berthName;
		$scope.report.etaOfArrival = voyage.arrival;
		$scope.report.personsOnBoard = voyage.personsOnBoard;
	});

	$scope.$on('$viewContentLoaded', function(){
		var greenPos = embryo.greenPos;
		if (!greenPos.map) {
			// postpone map loading sligtly, to let the resize directive set the
			// sizes of the map container divs, before map loading. If not done, the
			// map is not loaded in correct size
			setTimeout(embryo.greenPos.loadMap, 100);
		}
	});

	$scope.isVisible = function(fieldName) {
		if (!$scope.report || !$scope.report.reportType) {
			return true;
		}
		var fields = $scope.visibility[$scope.report.reportType];

		return fields.indexOf(fieldName) > -1;
	};

	// can speed and course be preset with ais data?

	$scope.isShip = function() {
		return true;
	};

	$scope.sendReport = function() {
		console.log('trying to send report');
		GreenPos.save($scope.report, function() {
			$scope.message = "GreenPos report successfully submitted";
			console.log("GreenPos successfully submitted");
		});
	};

	$scope.cancel = function() {
		console.log($scope.greenPosForm.gpCourse.$error.required);
		console.log($scope.greenPosForm.gpCourse.$error);
	};

	$scope.clear = function() {

	};

	$scope.getLatLon = function() {
		return {
			lat : $scope.report.latitude,
			lon : $scope.report.longitude
		};
	};

	$scope.$watch($scope.getLatLon, function(newValue, oldValue) {
		if (newValue.lat && newValue.lon) {
			embryo.greenPos.drawPosition(newValue.lat, newValue.lon);
		}
	}, true);

};

embryo.greenPos.loadMap = function() {
	this.map = new OpenLayers.Map({
		div : "greenPosMap",
		projection : 'EPSG:900913',
		fractionalZoom : false
	});

	var osm = new OpenLayers.Layer.OSM("OSM",
			"http://a.tile.openstreetmap.org/${z}/${x}/${y}.png", {
				'layers' : 'basic',
				'isBaseLayer' : true
			});
	this.map.addLayer(osm);

	var renderer = OpenLayers.Util.getParameters(window.location.href).renderer;
	renderer = (renderer) ? [ renderer ]
			: OpenLayers.Layer.Vector.prototype.renderers;

	var defTemplate = OpenLayers.Util.applyDefaults({
		strokeWidth : 2,
		strokeColor : "blue", // using context.getColor(feature)
		fillColor : "blue", // using context.getColor(feature)
		graphicName : "x",
		pointRadius : 5
	}, OpenLayers.Feature.Vector.style["default"]);

	var pointLayer = new OpenLayers.Layer.Vector("pointLayer", {
		styleMap : new OpenLayers.StyleMap({
			'default' : defTemplate
		}),
		renderes : renderer
	});

	this.vesselLayer = new OpenLayers.Layer.Vector("staticLayer", {
		styleMap : new OpenLayers.StyleMap({
			"default" : {
				externalGraphic : "${image}",
				graphicWidth : "${imageWidth}",
				graphicHeight : "${imageHeight}",
				graphicYOffset : "${imageYOffset}",
				graphicXOffset : "${imageXOffset}",
				rotation : "${angle}",
				strokeDashstyle : 'dash',
				strokeColor : "red", // using context.getColor(feature)
				strokeWidth : 3
			// strokeOpacity
			},
			"select" : {
				cursor : "crosshair",
				externalGraphic : "${image}"
			}
		}),
		renderers : renderer
	});
	this.map.addLayer(this.vesselLayer);
	this.map.addLayer(pointLayer);

	embryo.greenPos.pointLayer = pointLayer;
	embryo.greenPos.map = this.map;
	
	var initialLat = 74.00;
	var initialLon = -40.0;
	var initialZoom = 3;

	var center = transformPosition(initialLon, initialLat, this.map);
	this.map.setCenter(center, initialZoom);
	this.lastZoomLevel = this.map.zoom;

};

embryo.greenPos.drawPosition = function(lat, lon) {
	if (embryo.greenPos.pointLayer) {
		var point = new OpenLayers.Geometry.Point(lon, lat).transform(
				new OpenLayers.Projection("EPSG:4326"), embryo.greenPos.map
						.getProjectionObject());

		var pointFeature = new OpenLayers.Feature.Vector(point);
		embryo.greenPos.pointLayer.removeAllFeatures();
		embryo.greenPos.pointLayer.addFeatures([ pointFeature ]);
		embryo.greenPos.pointLayer.refresh();
	}

};

function transformPosition(lon, lat, map) {
	return new OpenLayers.LonLat(lon, lat).transform(new OpenLayers.Projection(
			"EPSG:4326"), // transform from WGS 1984
	map.getProjectionObject() // to Spherical Mercator
	// Projection
	);
}

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
				ShipService.getYourShip(function(yourShip) {
					console.log(yourShip.maritimeId);
					var voyage = VoyageRestService.getActive({
						id : yourShip.maritimeId
					}, function() {
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

	// $locationProvider.html5Mode(true);
} ]);

angularApp.directive('msgRequired', function() {
	return {
		link : function(scope, element, attrs) {
			element.text('Value required');
			element.addClass('msg-invalid');

			attrs.$set('ngShow', attrs.msgRequired + '$error.required'
					&& greenPosForm.gpPersons + '.$dirty');

			// watch the expression, and update the UI on change.
			scope.$watch('greenPosForm.gpPersons', function(value, oldValue) {
				console.log(value);
				// console.log(value.$dirty);
				// console.log(value.$error.required);

				// if(value.$dirty && value.$error.required){
				// element.show();
				// }else{
				// element.hide();
				// }
			});
		}
	};

});

/*
 * Inspired by http://jsfiddle.net/zbjLh/2/
 */
angularApp.directive('resize', function($window) {
	return {
		restrict : 'A',
		link : function(scope, element, attrs) {

			var elemToMatch = $('#' + attrs.resize);
			scope.getElementDimensions = function() {
				return {
					'h' : elemToMatch.height(),
					'w' : elemToMatch.width(),
				};
			};
			scope.$watch(scope.getElementDimensions, function(newValue,
					oldValue) {

				scope.style = function() {
					return {
						'height' : (newValue.h) + 'px',
						'width' : (newValue.w) + 'px'
					};
				};
			}, true);

			var window = angular.element($window);
			window.bind('resize', function() {
				scope.$apply(function() {
				});
			});
		}
	};
});