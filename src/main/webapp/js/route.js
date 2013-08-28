/*
 * Dependencies:
 * 
 * aisview.js
 * aisviewUI.js
 * ....
 */

"use strict";

embryo.routeUpload = {};
embryo.routeUpload.Ctrl = function($scope, $element) {

	$scope.voyage = {
		id : null,
		name : null,
		isPreselected : false,
	};

	// TODO Find out how to reset prefetch/typeahead upon new ship
	var mmsi = '220443000';

	$($element).find('.ngTypeahead').bind(
			"typeahead:autocompleted typeahead:selected",
			embryo.routeUpload.selected);

	var vUrl = 'rest/voyage/typeahead/' + mmsi;
	$scope.voyageData = {
		name : 'voyages_21_' + mmsi,
		prefetch : {
			url : vUrl,
			ttl : 60000
		// 1 minut
		},
		remote : vUrl
	};

	$scope.close = function() {
		$('#routeUpload').find('.modal').modal('hide');

		if ($scope.onclose) {
			$scope.onclose({
				route : $scope.uploadedroute
			});
			// reset such that it can not be invoked twice
			$scope.onclose = null;
		}

		$scope.clear();
	};

	/**
	 * options object
	 * 
	 * @param options.onclose
	 *            function to be called when route upload closes
	 * @param options.preSelectedVoyage.name
	 *            Name of voyage for which to upload a route (new or modified)
	 * @param options.preSelectedVoyage.id
	 *            Id of voyage for which to upload a route (new or modified)
	 */
	$scope.open = function(options) {
		$scope.clear();
		if (options && options.preSelectedVoyage) {
			$scope.voyage.isPreselected = true;

			if (options.preSelectedVoyage.id) {
				$scope.voyage.id = options.preSelectedVoyage.id;
				$('#routeUpload').find('form').find('input[name="voyageId"]')
						.val(options.preSelectedVoyage.id);
			}
			if (options.preSelectedVoyage.name) {
				$scope.voyage.name = options.preSelectedVoyage.name;
				$('#routeUpload').find('#voyageName').typeahead('setQuery',
						options.preSelectedVoyage.name);
			}
		} else {
			$scope.voyage.isPreselected = false;
			$scope.voyage.id = null;
			$scope.voyage.name = null;
		}

		if (options && options.onclose) {
			$scope.onclose = options.onclose;
		}
		$('#routeUpload').find('.modal').modal('show');
	};

	$scope.clear = function() {
		var scope = $('#routeUpload').find('form').scope();

		scope.queue = [];
		scope.message = null;

		$('#routeUpload').find('form').find('input[name="voyageId"]').val('');
		$('#routeUpload').find('#voyageName').typeahead('setQuery', '');

		$scope.voyage.isPreselected = false;
		$scope.voyage.id = null;
		$scope.voyage.name = null;
	};

};

embryo.routeUpload.selected = function(event, datum) {
	$(event.target).parents('form').find('input[name="voyageId"]')
			.val(datum.id);
};

$('#routeUpload').find('form').bind('fileuploadsubmit', function(e, data) {
	var inputs = data.form.find('[name="voyageId"]');
	if (inputs.filter('[required][value=""]').first().focus().length) {
		return false;
	}
	data.formData = inputs.serializeArray();
});

embryo.routeUpload.uploadActive = function() {
	$('#routeUpload').scope().open();
};

var angularApp = angular.module('embryo', [ 'ngResource',
		'siyfion.ngTypeahead', 'blueimp.fileupload' ]);
// , 'ui.bootstrap'

var isOnGitHub = false;

var url = 'rest/routeUpload/single/';

angularApp.config([
		'$httpProvider',
		'fileUploadProvider',
		function($httpProvider, fileUploadProvider) {
			delete $httpProvider.defaults.headers.common['X-Requested-With'];
			fileUploadProvider.defaults.redirect = window.location.href
					.replace(/\/[^\/]*$/, '/cors/result.html?%s');
			if (isOnGitHub) {
				// Demo settings:
				angular.extend(fileUploadProvider.defaults, {
					// Enable image
					// resizing, except for
					// Android and Opera,
					// which actually
					// support image
					// resizing, but fail to
					// send Blob objects via
					// XHR requests:
					disableImageResize : /Android(?!.*Chrome)|Opera/
							.test(window.navigator.userAgent),
					maxFileSize : 5000000,
					acceptFileTypes : /(\.|\/)(gif|jpe?g|png)$/i
				});
			}
		} ]);

angularApp.controller('DemoFileUploadController', [ '$scope', '$http',
		'$filter', '$window', function($scope, $http) {
			$scope.options = {
				url : url,
				done : function(e, data) {
					$.each(data.result.files, function(index, file) {
						$scope.message = "Uploaded route";
						$scope.$apply(function() {
							$scope.$parent.uploadedroute = {
								id : file.routeId
							};
						});
					});
				}
			};
			if (!isOnGitHub) {
				$scope.loadingFiles = true;
				$http.get(url).then(function(response) {
					$scope.loadingFiles = false;
					$scope.queue = response.data.files || [];
				}, function() {
					$scope.loadingFiles = false;
				});
			}
		} ]);

angularApp.controller('FileDestroyController', [ '$scope', '$http',
		function($scope, $http) {
			var file = $scope.file, state;
			if (file.url) {
				file.$state = function() {
					return state;
				};
				file.$destroy = function() {
					state = 'pending';
					return $http({
						url : file.deleteUrl,
						method : file.deleteType
					}).then(function() {
						state = 'resolved';
						$scope.clear(file);
					}, function() {
						state = 'rejected';
					});
				};
			} else if (!file.$cancel && !file._index) {
				file.$cancel = function() {
					$scope.clear(file);
				};
			}
		} ]);

angularApp.factory('Route', function($resource) {
	var defaultParams = {};

	var actions = {
		getActive : {
			params : {
				action : 'active'
			},
			method : 'GET',
			isArray : false,
		},
		activate : {
			params : {
				action : 'activate'
			},
			method : 'PUT',
			isArray : false,
		}

	};

	return $resource('rest/route/:action/:id', defaultParams, actions);
});

angularApp.factory('RouteService', function(Route) {
	var route = new Route();
	var active = new Route();
	return {
		editRoute : function(r) {
			route = r;
		},
		newRoute : function() {
			route = new Route();
		},
		getRoute : function() {
			return route;
		},
		getActive : function() {
			return active;
		},
		setActive : function(r) {
			active = r;
		}
	};
});

embryo.routeModal = {};
embryo.routeModal.Ctrl = function($scope, Route, RouteService) {
	$scope.getRoute = function() {
		return RouteService.getRoute();
	};

	$scope.save = function() {
		
		console.log('save');
		// validate?
		Route.save(RouteService.getRoute(), function() {
			$scope.message = "Saved route '" + $scope.getRoute().name + "'";
			// Route not fetch from server, which might be a good idea.
			embryo.route.redrawIfVisible(RouteService.getRoute());
		});
	};
	$scope.close = function() {
		$scope.clear();
		$('#routeEdit').parents('.modal').modal('hide');

		if ($scope.onclose) {
			$scope.onclose({
				route : RouteService.getRoute()
			});
			$scope.onclose = null;
		}
	};

	$scope.open = function(options) {
		if (options && options.onclose) {
			$scope.onclose = options.onclose;
		}
		$('#routeEdit').parents('.modal').modal('show');
	};

	$scope.edit = function(routeId) {
		$scope.route = Route.get(routeId);
		$scope.open();
	};

	$scope.openYourActive = function() {
		var route = Route.getActive(function(route) {
			if (!route.name) {
				// no active route
				// should some status code reading be done?
				$scope.alertMessage = "No route has been activated";
			}
			RouteService.editRoute(route);
		});

		$scope.open();
	};

	$scope.saveable = function() {
//		if($scope.routeEditForm.$invalid){
//			return false;
//		}
		
		if (!($scope.getRoute().waypoints && $scope.getRoute().waypoints.length >= 2)) {
			return false;
		}

		return true;
	};

	$scope.newRoute = function() {
		RouteService.newRoute();
		$scope.open();
	};

	$scope.clear = function() {
		$scope.alertMessage = null;
		$scope.message = null;
	};
};

embryo.routeModal.editActive = function() {
	$('#routeEditModal').scope().openYourActive();
};

embryo.modal = {};
embryo.modal.close = function(id, action) {
	$("#" + id).modal('hide');

	if (action) {
		action();
	}

	if (embryo.routeUpload.onclose) {
		embryo.routeUpload.onclose();
	}

};


$(document).ready(function() {
	embryo.route.initLayer();
});

embryo.route = {};
embryo.route.fetch = function(id, draw) {
	$.getJSON('rest/route/' + id, function(route) {
		console.log(route);
		draw(route);
	});
};
embryo.route.fetchAndDraw = function(id) {
	return function() {
		embryo.route.fetch(id, embryo.route.draw);
	};
};

embryo.route.redrawIfVisible = function(route) {
	var toBeRemoved = [];
	var active;

	for ( var index in embryo.route.layer.features) {
		var feature = embryo.route.layer.features[index];
		if (feature.data.route.id === route.id) {
			active = feature.attributes.active;
			toBeRemoved.push(feature);
		}
	}

	embryo.route.layer.removeFeatures(toBeRemoved);
	embryo.route.draw(route, active);
};

embryo.route.drawTests = function() {
	embryo.route.fetch('231', embryo.route.draw);
	embryo.route.fetch('235', embryo.route.draw);
};

embryo.route.initLayer = function() {
	
	console.log('embryo.route.initLayer');
	
	// Create vector layer for routes

	// Find a better color code. How to convert sRGB to HTML codes?
	var defTemplate = OpenLayers.Util.applyDefaults({
		strokeWidth : pastTrackWidth,
		strokeDashstyle : 'dash',
		strokeColor : "${getColor}", // using context.getColor(feature)
		fillColor : "${getColor}" // using context.getColor(feature)
	}, OpenLayers.Feature.Vector.style["default"]);

	var context = {
		getColor : function(feature) {
			return feature.attributes.active ? 'red ' : '#D5672D';
		}
	};

	var defaultStyle = new OpenLayers.Style(defTemplate, {
		context : context
	});

	var select = OpenLayers.Util.applyDefaults({},
			OpenLayers.Feature.Vector.style.select);
	var selectStyle = new OpenLayers.Style(select);

	var temporary = OpenLayers.Util.applyDefaults({},
			OpenLayers.Feature.Vector.style.temporary);
	var temporaryStyle = new OpenLayers.Style(temporary);

	embryo.route.layer = new OpenLayers.Layer.Vector("routeLayer", {
		styleMap : new OpenLayers.StyleMap({
			'default' : defaultStyle,
			'select' : selectStyle,
			'temporary' : temporaryStyle
		})
	});

	embryo.mapPanel.map.addLayer(embryo.route.layer);
	embryo.mapPanel.add2SelectFeatureCtrl(embryo.route.layer);

	// testing event functionality
	function report(event) {
		console.log(event.type, event.feature ? event.feature.id
				: event.components);
	}

	
	// This is how one reacts on select events in the route layer
	embryo.route.layer.events.on({
		"beforefeaturemodified" : report,
		"featuremodified" : report,
		"afterfeaturemodified" : report,
		"vertexmodified" : report,
		"sketchmodified" : report,
		"sketchstarted" : report,
		"sketchcomplete" : report,
		"featureselected" : report,
		"featureunselected" : report
	});

	// embryo.route.addModifyControl();
	

	embryo.route.drawActiveRoute();
	
	var menuItems = [ {
		text : 'Edit Route',
		shown4FeatureType : 'route',
		choose : function(scope, feature) {
			var injector = angular.element(document).injector();
			var service = injector.get('RouteService');
			service.editRoute(feature.data.route);
			$('#routeEdit').parents('.modal').modal('show');
		}
	}, {
		text : 'New Route',
		choose : function(scope, feature) {
			$('#routeEditModal').scope().newRoute();
		}
	}, {
		text : 'Upload Active Route',
		choose : function(scope, feature) {
			$('#routeUpload').scope().open();
		}
	}, {
		text : 'Clear Drawn Routes',
		shown : function(feature) {
			return embryo.route.layer.features.length > 1;
		},
		choose : function(scope, feature) {
			embryo.route.layer.removeAllFeatures();
			embryo.route.drawActiveRoute();
		}
	} ];

	embryo.contextMenu.addMenuItems(menuItems);
};

embryo.route.drawActiveRoute = function() {
	var injector = angular.element(document).injector();
	var Route = injector.get('Route');
	var RouteService = injector.get('RouteService');

	if (!RouteService.getActive().name) {
		var activeRoute = Route.getActive(function() {

			console.log(activeRoute);
			RouteService.setActive(activeRoute);

			if (RouteService.getActive().name) {
				console.log('drawing active route: '
						+ RouteService.getActive().name);
				embryo.route.draw(RouteService.getActive(), true);
			} else {
				console.log('no active route to draw');
				console.log(RouteService.getActive());
			}
		});
	} else {
		console.log('drawing active route: ' + RouteService.getActive().name);
		embryo.route.draw(RouteService.getActive(), true);
	}
};

embryo.route.draw = function(route, active) {
	// Remove old tracks
	// routeLayer.removeAllFeatures();

	if (!active) {
		active = false;
	}

	// Draw tracks
	if (route && route.waypoints) {
		var firstPoint = true;
		var currentPoint;
		var previousPoint = null;

		var points = [];
		var lines = [];

		for ( var index in route.waypoints) {
			currentPoint = embryo.route.createPoint(route.waypoints[index]);

			points.push(embryo.route.createWaypointFeature(currentPoint));
			if (!firstPoint) {
				lines
						.push(embryo.route.createLine(previousPoint,
								currentPoint));
			}
			firstPoint = false;
			previousPoint = currentPoint;
		}

		var multiLine = new OpenLayers.Geometry.MultiLineString(lines);

		embryo.route.layer.addFeatures(new OpenLayers.Feature.Vector(multiLine,
				{
					featureType : 'route',
					active : active,
					route : route
				}));
		// embryo.route.layer.addFeatures(points);

		// Draw features
		embryo.route.layer.refresh();
	}
};

embryo.route.addModifyControl = function() {
	// var controls = {
	// point: new OpenLayers.Control.DrawFeature(embryo.route.layer,
	// OpenLayers.Handler.Point),
	// line: new OpenLayers.Control.DrawFeature(embryo.route.layer,
	// OpenLayers.Handler.Path)
	// };
	//        
	// for(var key in controls) {
	// embryo.mapPanel.map.addControl(controls[key]);
	// }

	embryo.route.modCtrl = new OpenLayers.Control.ModifyFeature(
			embryo.route.layer, {
				createVertices : true,
				mode : OpenLayers.Control.ModifyFeature.RESHAPE
			});
	embryo.mapPanel.map.addControl(embryo.route.modCtrl);
	embryo.route.modCtrl.activate();

	embryo.route.layer.events.on({
		"beforefeaturemodified" : function(feature) {
			console.log('beforefeaturemodified' + feature);
		},
		'featureselected' : function(feature) {
			console.log('featureselected' + feature);
		},
		'featureunselected' : function(feature) {
			console.log('featureunselected' + feature);
		}
	});

};

embryo.route.createWaypointFeature = function(point) {
	var style_green = {
		strokeColor : "#00FF00",
		// strokeColor: "#ee9900",
		strokeWidth : 6,
		pointRadius : 6,
	};

	return new OpenLayers.Feature.Vector(OpenLayers.Geometry.Polygon
			.createRegularPolygon(point, 20.0, 30, 0.0), null, style_green);
};

embryo.route.createLine = function(firstPoint, secondPoint) {
	var points = new Array(firstPoint, secondPoint);
	var line = new OpenLayers.Geometry.LineString(points);
	return line;
	// var lineFeature = new OpenLayers.Feature.Vector(line);
	// return lineFeature;
};

embryo.route.createPoint = function(waypoint) {
	return new OpenLayers.Geometry.Point(waypoint.longitude, waypoint.latitude)
			.transform(new OpenLayers.Projection("EPSG:4326"),
					embryo.mapPanel.map.getProjectionObject());
};
