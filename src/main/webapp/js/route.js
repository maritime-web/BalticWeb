
/*
 * Dependencies:
 * 
 * aisview.js
 * aisviewUI.js
 * ....
 */

embryo.routeUpload = {};
embryo.routeUpload.Ctrl = function($scope, $element) {
	
	//TODO Find out how to reset prefetch/typeahead upon new ship
	var mmsi = '220443000';

	// c
	$($element).find('.ngTypeahead').bind(
			"typeahead:autocompleted typeahead:selected", embryo.routeUpload.selected);

	
	var vUrl = 'rest/voyage/typeahead/' + mmsi;
	$scope.voyageData = {
		name : 'voyages_45_' + mmsi,
		prefetch : {
			url : vUrl,
			ttl : 18000000// 1/2 hour
		},
		remote : vUrl
	};
};
embryo.routeUpload.selected = function(event, datum){
	console.log(event);
	console.log(datum.id);
	$(event.target).parents('form').find('.voyageId').val(datum.id)
	console.log($('.voyageId'));
};

embryo.routeModal = {};
embryo.routeModal.modalIdSelector;
embryo.routeModal.prepareRequest = function(containerSelector) {

	console.log('prepareRequest');

	embryo.routeModal.modalIdSelector = containerSelector;

	var $modalBody = $(containerSelector);
	var $rows = $modalBody.find('tbody tr');
	$modalBody.find('input[name="routeCount"]').val($rows.length);

	var regex = new RegExp('\\d+', 'g');
	$rows.each(function(index, row) {
		$(row).find('input[name]').each(function(indeks, input) {
			var nameAttr = $(input).attr("name");
			var result = nameAttr.replace(regex, "" + index);
			$(input).attr("name", result);
		});
	});

	return false;
};

var angularApp = angular.module('embryo', ['ngResource', 'siyfion.ngTypeahead']);

angularApp.factory('Route', function($resource) {
	return $resource('rest/route/save', {
		save : {
			method : 'POST',
			headers : [ {
				'Content-Type' : 'application/json'
			}, {
				'Accept' : 'application/json'
			} ]
		}
	});
});

angularApp.factory('RouteService', function() {
	var route = {
		waypoints : []
	};
	return {
		editRoute : function(r) {
			route = r;
		},
		getRoute : function() {
			return route;
		}
	};
});

embryo.routeModal.Ctrl = function($scope, RouteService, Route) {
	$scope.route = RouteService.getRoute();

	$scope.getRoute = function() {
		return RouteService.getRoute();
	};

	$scope.save = function() {
		// validate?
		Route.save(RouteService.getRoute(), function() {
			console.log('Saved route' + $scope.route.name);
			console.log('Saved route' + $scope.route);

		});
	};
	$scope.close = function() {
		$('#routeEdit').parents('.modal').modal('hide');
	};
};

embryo.dynamicListView = {};
embryo.dynamicListView.init = function(listSelector, name, autoExpand) {
	$(listSelector).append('<input type="hidden" name="' + name + '"/>');

	if (autoExpand) {
		$(listSelector)
				.find('tr')
				.each(
						function() {
							$(this)
									.find('td:last-child')
									.append(
											'<td><button type="submit" class="btn btn-danger">Delete</button></td>');
						});

		$(containerSelector).find('tr:last-child').addClass('emptyRow').find(
				'button').hide();
	}

};

/*
 * embryo.dynamicListView.onDelete = function(event) { event.preventDefault();
 * event.stopPropagation(); $rowToDelete = $(event.target).closest('tr');
 * $rowToDelete.next().find("input:first").focus(); $rowToDelete.remove(); };
 * 
 * embryo.dynamicListView.copyEmptyRow = function(event) { var $row =
 * $(event.target).closest('tr'); // create new row by copy and modify before
 * insertion into document var $newRow = $row.clone(true); var columnIndex =
 * $row.find('input').index(event.target);
 * $newRow.find('input').eq(columnIndex).val(""); $row.after($newRow); //
 * enableRow must be called after copying new row
 * embryo.dynamicListView.enableRow($row); };
 * 
 * 
 * embryo.dynamicListView.enableRow = function($row) {
 * embryo.typeahead.create($row.find('input.typeahead-textfield')[0]);
 * 
 * $row.find('button').show(); $row.removeClass('emptyRow'); $row.find('input,
 * button').unbind('keydown', embryo.voyagePlanForm.copyEmptyRow);
 * 
 * //$(this).find('button').click(formObject.onDelete); //
 * embryo.voyagePlanForm.registerHandlers($row); };
 */

embryo.dynamicListView.prepareRequest = function(containerSelector) {
	var $modalBody = $(containerSelector);
	var $rows = $modalBody.find('tbody tr');
	$modalBody.find('input[name="listCount"]').val($rows.length);

	var regex = new RegExp('\\d+', 'g');
	$rows.each(function(index, row) {
		$(row).find('input[name]').each(function(indeks, input) {
			var nameAttr = $(input).attr("name");
			var result = nameAttr.replace(regex, "" + index);
			$(input).attr("name", result);
		});
	});

	return false;
};

embryo.modal = {};
embryo.modal.close = function(id, action) {
	$("#" + id).modal('hide');

	if (action) {
		action();
	}
};

embryo.route = {};
embryo.route.fetch = function(id, draw) {
	$.getJSON('rest/route/byId/' + id, function(route) {
		draw(route);
	});
};
embryo.route.fetchAndDraw = function(id) {
	return function() {
		alert('fetch and draw');
		embryo.route.fetch(id, embryo.route.draw);
	};
};
embryo.route.drawTests = function() {
	embryo.route.fetch('231', embryo.route.draw);
	embryo.route.fetch('235', embryo.route.draw);
};

embryo.route.initLayer = function() {
	// Create vector layer for routes

	var defaultStyle = OpenLayers.Util.applyDefaults({
		fillColor : pastTrackColor,
		strokeColor : pastTrackColor,
		strokeOpacity : pastTrackOpacity,
		strokeWidth : pastTrackWidth
	}, OpenLayers.Feature.Vector.style["default"]);

	var selectStyle = OpenLayers.Util.applyDefaults({},
			OpenLayers.Feature.Vector.style.select);

	var temporary = OpenLayers.Util.applyDefaults({},
			OpenLayers.Feature.Vector.style.temporary);

	embryo.route.layer = new OpenLayers.Layer.Vector("routeLayer", {
		styleMap : new OpenLayers.StyleMap({
			'default' : defaultStyle,
			'select' : selectStyle,
			'temporary' : temporary
		})
	});
	// embryo.route.layer = new OpenLayers.Layer.Vector("routeLayer");

	// TODO use Rules and Filters to set different colours on lines.

	embryo.mapPanel.map.addLayer(embryo.route.layer);

	// testing event functionality
	function report(event) {
		console.log(event.type, event.feature ? event.feature.id
				: event.components);
	}
	// move this to initialization of route layer
	embryo.route.layer.events.on({
		"beforefeaturemodified" : report,
		"featuremodified" : report,
		"afterfeaturemodified" : report,
		"vertexmodified" : report,
		"sketchmodified" : report,
		"sketchstarted" : report,
		"sketchcomplete" : report
	});

	var menuItems = [ {
		text : 'Edit Route',
		shown4FeatureType : 'route',
		choose : function(scope, feature) {
			var injector = angular.element(document).injector();
			var service = injector.get('RouteService');
			service.editRoute(feature.data.route);
			$('#routeEdit').parents('.modal').modal('show');
		}
	} ];

	embryo.contextMenu.addMenuItems(menuItems);
};

embryo.route.draw = function(route) {
	// Remove old tracks
	// routeLayer.removeAllFeatures();

	// Draw tracks
	if (route && route.waypoints) {
		var firstPoint = true;
		var currentPoint;
		var previousPoint = null;

		var points = [];
		var lines = [];

		for (index in route.waypoints) {
			currentPoint = embryo.route.createPoint(route.waypoints[index]);

			points.push(embryo.route.createWaypointFeature(currentPoint));
			if (!firstPoint) {
				lines
						.push(embryo.route.createLine(previousPoint,
								currentPoint));
				// embryo.route.drawRouteLeg(previousPoint, currentPoint);
			}
			firstPoint = false;
			previousPoint = currentPoint;
		}

		var multiLine = new OpenLayers.Geometry.MultiLineString(lines);

		embryo.route.layer.addFeatures(new OpenLayers.Feature.Vector(multiLine,
				{
					featureType : 'route',
					routeId : 'empty',
					route : route
				}));
		// embryo.route.layer.addFeatures(points);

		// Draw features

		// START FIXME: Find out how to move this code to initLayer function
		// embryo.route.addSelectFeature();
		// END FIXME: Find out how to move this code to initLayer function

		embryo.route.addModifyControl();

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

embryo.route.addSelectFeature = function() {
	embryo.route.select_feature_control = new OpenLayers.Control.SelectFeature(
			embryo.route.layer, {
				multiple : false,
				toggle : true,
				multipleKey : 'shiftKey'
			});

	embryo.mapPanel.map.addControl(embryo.route.select_feature_control);
	embryo.route.select_feature_control.activate();

	function selected_feature(event) {
		console.log(event);
	}
	;

	embryo.route.layer.events.register('featureselected', this,
			selected_feature);
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
