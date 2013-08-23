// TODO: All JavaScript should be placed in appropriate namespaces according to their components etc.
var embryo = {};

// Global variables

var vessels = [];
var clusters = [];
var clustersDrawn = 0;
var topLeftPixel;
var botRightPixel;
var filterQuery = {};
var showVesselName = false;
var savedTracks;
var savedTimeStamps;
var loadSavedFeatures = false;
var timeOfLastLoad = 0;
var selectSearchedVessel = false;
var selectMarkedVessel = false;
var searchedVessel;
// var selectedVessel;
var selectedVesselInView = false;
var selectedFeature;
var markedVessel;
var searchResults = [];
var lastRequestId = 0;
var lastZoomLevel;
var lastLoadArea;

embryo.selectedVessel = null;

// embryo.eventbus is necessary to construct a completely loose coupling between
// all components in the application.
// The implementation is based on jQuery, and as such embryo.eventbus is just a
// wrapper around jQuery enabling object oriented like code
embryo.eventbus = {};
embryo.eventbus.registerHandler = function(eventType, handler) {
	var type = eventType().type;
	console.log("registerHandler for eventType : " + type);
	$(document).on(type, handler);
};

embryo.eventbus.fireEvent = function(event) {
	$(document).trigger(event);
};

embryo.eventbus.VesselSelectedEvent = function(id) {
	var event = jQuery.Event("VesselSelectedEvent");
	event.vesselId = id;
	return event;
};

embryo.eventbus.HighLightEvent = function(feature) {
	var event = jQuery.Event("HighLightEvent");
	event.feature = feature;
	return event;
};

embryo.eventbus.UnHighLightEvent = function(feature) {
	var event = jQuery.Event("UnHighLightEvent");
	event.feature = feature;
	return event;
};

/**
 * Initialization fired when DOM has loaded (but before images, stylesheets, etc
 * has been fetched
 */
$(document).ready(function() {
	includePanels();
	includeTools();
	// Load cookies
	loadView();

	setupDatePickers();

	embryo.exitAbnormal.init();
	embryo.feed.init();
	embryo.vessel.details.init();
	embryo.filtering.init();

	embryo.initAngular();

	// TODO projection should be configured else where
	embryo.mapPanel.init2('EPSG:900913');

});

/**
 * Initialize the map. This method is fired on window load (configured by
 * wicket)
 */
embryo.mapPanel = {

	init2 : function(defaultProjection) {

		this.map = new OpenLayers.Map({
			div : "map",
			projection : defaultProjection,
			fractionalZoom : false
		});

		// Create hover control - indie vessels
		var hoverControl = new OpenLayers.Control.SelectFeature([], {
			id : 'HoverCtrl',
			hover : true,
			highlightOnly : true,
			eventListeners : {
				// OpenLayers does not have a general support for
				// featurehighlighted and featureunhighlighted events like
				// layer.events.on({featureselected:function(event){}})
				// Build our own using embryo.eventbus
				featurehighlighted : function(event) {
					embryo.eventbus.fireEvent(embryo.eventbus
							.HighLightEvent(event.feature));
				},
				featureunhighlighted : function(feature) {
					embryo.eventbus.fireEvent(embryo.eventbus
							.UnHighLightEvent(feature.feature));
				}
			}
		});

		// Add select controller to map and activate
		embryo.mapPanel.map.addControl(hoverControl);
		hoverControl.activate();

		// Select feature configuration should be moved into contextmenu section
		// It must be done after all layers have been initialized

		// Create one select control for all layers. This the only way to enable
		// selection of features in different active layers being shown at the
		// same
		// time. Also see below link.
		// http://gis.stackexchange.com/questions/13886/how-to-select-multiple-features-from-multiple-layers-in-openlayers
		// and http://openlayers.org/dev/examples/select-feature-multilayer.html

		// Reacting on select / unselect is performed like:
		// someLayer.events.on({
		// "featureselected" : function(e){ ... },
		// "featureunselected" : function(e){ ... }
		// });

		// add select control, registered for no layers
		// Layers will be registered on control through usage of
		// addSelectableLayer
		var selectControl = new OpenLayers.Control.SelectFeature([], {
			clickout : true,
			toggle : true,
			id : 'ClickCtrl'
		});

		this.map.addControl(selectControl);
		selectControl.activate();

		// Add OpenStreetMap Layer
		var osm = new OpenLayers.Layer.OSM("OSM",
				"http://a.tile.openstreetmap.org/${z}/${x}/${y}.png", {
					'layers' : 'basic',
					'isBaseLayer' : true
				});
		this.map.addLayer(osm);

		// this.addLayerInitializer(embryo.route.initLayer);

		embryo.mapPanel.initLayers();
		// this.map.addControl(new OpenLayers.Control.LayerSwitcher());

		var center = transformPosition(initialLon, initialLat);
		this.map.setCenter(center, initialZoom);
		lastZoomLevel = this.map.zoom;

	},

	addLayer2Ctrl : function(layer, property, testObject) {
		var controls = this.map.getControlsBy(property, testObject);
		// FIXME write proper error handling
		if (controls.length != 1) {
			console.log('Error. Expected exactly 1 map control with '
					+ property + '. Found ' + controls.length);
		}
		var layers = controls[0].layers;
		layers.push(layer);
		controls[0].setLayer(layers);

	},

	add2SelectFeatureCtrl : function(layer) {
		// TODO make this method a part of OpenLayers.Map object på extending
		// the prototype
		// this.addLayer2Ctrl('OpenLayers.Control.SelectFeature', layer);
		this.addLayer2Ctrl(layer, 'id', {
			test : function(id) {
				return 'ClickCtrl' === id;
			}
		});
	},

	add2HoverFeatureCtrl : function(layer) {
		this.addLayer2Ctrl(layer, 'id', {
			test : function(id) {
				return 'HoverCtrl' === id;
			}
		});
	},

	init : function(projection) {
		this.load();
	},

	load : function() {
		setupUI();

		// Load new vessels with an interval
		setInterval("loadVesselsIfTime()", loadCheckingFrequence);

		if (includeEventFeed) {
			setInterval("loadBehaviors()", loadBehaviorsFrequence);
			loadBehaviors();
		}

		parseFilterQuery();

		// Load vessels
		loadVessels();

		// Context menu should be initialized on Window onload. This will
		// increase
		// likelyhood that all other features have been loaded
		// Even better then load context menu as the very last thing or ?
		embryo.contextMenu.init();
	},
    hoveringHandlers: []
};


/**
 * Sets up the panels, event listeners and selection controllers.
 */
function setupUI() {
	// Set zoom panel positon
	$(".olControlZoom").css('left', zoomPanelPositionLeft);
	$(".olControlZoom").css('top', zoomPanelPositionTop);

	// Set loading panel positon
	var x = $(document).width() / 2 - $("#loadingPanel").width() / 2;
	$("#loadingPanel").css('left', x);

	//
	embryo.mapPanel.map.events.includeXY = true;

	// console.log('hoverControlVessels');
	// // Create hover control - vessels
	// hoverControlVessels = new OpenLayers.Control.SelectFeature(
	// embryo.vessel.vesselLayer, {
	// hover : true,
	// highlightOnly : true,
	// eventListeners : {
	// featurehighlighted : showName,
	// featureunhighlighted : hideName
	// }
	//
	// });
	//
	// console.log('hoverControlIndieVessels');
	// // Create hover control - indie vessels
	// hoverControlIndieVessels = new OpenLayers.Control.SelectFeature(
	// indieVesselLayer, {
	// hover : true,
	// highlightOnly : true,
	// eventListeners : {
	// featurehighlighted : showName,
	// featureunhighlighted : hideName
	// }
	// });

	// Add select controller to map and activate
	// embryo.mapPanel.map.addControl(hoverControlVessels);
	// embryo.mapPanel.map.addControl(hoverControlIndieVessels);
	// hoverControlVessels.activate();
	// hoverControlIndieVessels.activate();

	// Register listeners

	embryo.eventbus.registerHandler(embryo.eventbus.HighLightEvent, function(e) {
    	var lonlatCenter = e.feature.geometry.getBounds().getCenterLonLat();
    	var pixelTopLeft = new OpenLayers.Pixel(0, 0);
    	var lonlatTopLeft = embryo.mapPanel.map.getLonLatFromPixel(pixelTopLeft);
    	pixelTopLeft = embryo.mapPanel.map.getPixelFromLonLat(lonlatTopLeft);
        var pixel = embryo.mapPanel.map.getPixelFromLonLat(lonlatCenter);
		var x = pixel.x - pixelTopLeft.x;
		var y = pixel.y - pixelTopLeft.y;

        var html;

        for (var i in embryo.mapPanel.hoveringHandlers) {
            var html1 = embryo.mapPanel.hoveringHandlers[i](e);
            if (html1 != null) html = html1;
        }

        if (html != null) {
            $("#hoveringBox").css("top", y + "px");
        	$("#hoveringBox").css("left", x + "px");
            $("#hoveringBox").html(html);
            $("#hoveringBox").css("display", "block");
        } else {
            $("#hoveringBox").css("display", "none");
        }
	});


	embryo.mapPanel.map.events.register("movestart", map, function() {
		$("#hoveringBox").css('display', 'none');
	});

	embryo.mapPanel.map.events.register("moveend", map, function() {
		saveViewCookie();
		if (loadAfterMove()) {
			setTimeToLoad(loadDelay);
			loadVesselsIfTime();
		}
		// else {
		// updateVesselsInView();
		// }

		lastZoomLevel = embryo.mapPanel.map.zoom;
	});

	// Close empty panels
	setInterval("closeEmptyPanels()", 1000);

}

embryo.initAngular = function() {
	angular.bootstrap($('#contextMenuApp'));
	angular.bootstrap($('#routeEditModalApp'));
};

embryo.mapPanel.layerInitializers = [];

embryo.mapPanel.addLayerInitializer = function(layerInitializer) {
	this.layerInitializers.push(layerInitializer);
};

embryo.mapPanel.initLayers = function() {
	for ( var x in this.layerInitializers) {
		this.layerInitializers[x]();
	}
};

/**
 * Loads and draws the vessel clusters.
 */
function loadVesselClusters() {

	saveViewPort();

	// Find cluster size
	var size = 10;
	for (i in clusterSizes) {
		if (embryo.mapPanel.map.zoom >= clusterSizes[i].zoom) {
			size = clusterSizes[i].size;
			// break;
		}
	}

	// Generate data
	var data = filterQuery;
	data.clusterLimit = clusterLimit;
	data.clusterSize = size;
	lastRequestId++;
	data.requestId = lastRequestId;
	if (!loadViewportOnly) {
		delete data.topLon;
		delete data.topLat;
		delete data.botLon;
		delete data.botLat;
	}

	$.getJSON(clusterUrl, data, function(result) {

		if (result.requestId != lastRequestId)
			return;

		// Update vessel counter
		$("#vesselsTotal").html(result.vesselsInWorld);

		// Load vessel clusters
		var JSONClusters = result.clusters;

		for (clusterId in JSONClusters) {

			// Create vessel based on JSON data
			var JSONCluster = JSONClusters[clusterId];
			var from = transformPosition(JSONCluster.from.longitude,
					JSONCluster.from.latitude);
			var to = transformPosition(JSONCluster.to.longitude,
					JSONCluster.to.latitude);
			var count = JSONCluster.count;
			var density = JSONCluster.density;
			var vessels = JSONCluster.vessels.vessels;

			var cluster = new Cluster(from, to, count, density, vessels);
			clusters.push(cluster);

		}

		// Draw clusters
		drawClusters();

		// Hide Loading panel
		$("#loadingPanel").css('visibility', 'hidden');

	});

}

/**
 * Draws the vessel clusters.
 */
function drawClusters() {

	// Reset
	clusterTextLayer.removeAllFeatures();
	clusterLayer.removeAllFeatures();
	var selectionFeatures = [];
	selectedVesselInView = false;
	var vesselsInView = 0;

	// Remove old features except selected feature
	var arr = indieVesselLayer.features.slice();
	var idx = arr.indexOf(selectedFeature);
	if (idx != -1)
		arr.splice(idx, 1);
	indieVesselLayer.destroyFeatures(arr);
	indieVesselLayer.renderer.clear();

	// Draw
	for (id in clusters) {
		if (clusters[id].count > clusterLimit) {

			drawCluster(clusters[id]);

			vesselsInView += clusters[id].count;

		} else {

			drawIndieVessels(clusters[id]);

			vesselsInView += clusters[id].count;

		}

	}

	// Set vessel in focus if selected
	vesselInFocus(embryo.selectedVessel, selectedFeature);

	// Draw selected vessel
	if (embryo.selectedVessel) {
		drawIndieVessel(embryo.selectedVessel);
	}

	// Draw selection
	addSelectionFeature();
	selectionLayer.redraw();
	indieVesselLayer.redraw();
	drawPastTrack(null);

	// Update number of vessels
	$("#vesselsView").html("" + vesselsInView);

}

/**
 * Draws a vessel cluster.
 */
function drawCluster(cluster) {

	// Create polygon
	var points = [
			new OpenLayers.Geometry.Point(cluster.from.lon, cluster.from.lat),
			new OpenLayers.Geometry.Point(cluster.to.lon, cluster.from.lat),
			new OpenLayers.Geometry.Point(cluster.to.lon, cluster.to.lat),
			new OpenLayers.Geometry.Point(cluster.from.lon, cluster.to.lat) ];
	var ring = new OpenLayers.Geometry.LinearRing(points);
	var polygon = new OpenLayers.Geometry.Polygon([ ring ]);

	// Create feature
	var feature = new OpenLayers.Feature.Vector(polygon, {
		from : cluster.from,
		to : cluster.to,
		fill : findClusterColor(cluster)
	});
	clusterLayer.addFeatures([ feature ]);

	// Draw text
	var textLon = cluster.from.lon + (cluster.to.lon - cluster.from.lon) / 2;
	var textLat = cluster.from.lat + (cluster.to.lat - cluster.from.lat) / 2;
	var textPos = new OpenLayers.Geometry.Point(textLon, textLat);
	var textFeature = new OpenLayers.Feature.Vector(textPos, {
		count : cluster.count,
		fontSize : clusterFontSize
	});
	clusterTextLayer.addFeatures([ textFeature ]);

}

/**
 * Draws the individual vessels in a cluster.
 */
function drawIndieVessels(cluster) {

	if (showIndividualVessels) {

		$.each(clusters[id].vessels, function(i, n) {

			drawIndieVessel(n);

		});

	}

}

/**
 * Finds the color of a cluster based on either density or count.
 */
function findClusterColor(cluster) {

	if (baseColorsOn == "density") {

		for ( var i = clusterColors.length - 1; i >= 0; i--) {
			if (cluster.density >= clusterColors[i].densityLimit) {
				return clusterColors[i].color;
			}
		}

	} else if (baseColorsOn == "count") {

		for ( var i = clusterColors.length - 1; i >= 0; i--) {
			if (cluster.count >= clusterColors[i].countLimit) {
				return clusterColors[i].color;
			}
		}

	}

	return "#000000";
}

/**
 * 
 * Saves the viewport to the filter query object.
 */
function saveViewPort() {

	// Get points from viewport
	var viewportWidth = $(embryo.mapPanel.map.getViewport()).width();
	var viewportHeight = $(embryo.mapPanel.map.getViewport()).height();
	topLeftPixel = new OpenLayers.Pixel(viewportWidth * 0.00,
			viewportHeight * 0.00);
	botRightPixel = new OpenLayers.Pixel(viewportWidth * 1.00,
			viewportHeight * 1.00);

	var top = embryo.mapPanel.map.getLonLatFromPixel(topLeftPixel).transform(
			embryo.mapPanel.map.getProjectionObject(), // from Spherical
			// Mercator Projection
			new OpenLayers.Projection("EPSG:4326") // to WGS 1984
	);
	var bot = embryo.mapPanel.map.getLonLatFromPixel(botRightPixel).transform(
			embryo.mapPanel.map.getProjectionObject(), // from Spherical
			// Mercator Projection
			new OpenLayers.Projection("EPSG:4326") // to WGS 1984
	);

	filterQuery.topLon = top.lon;
	filterQuery.topLat = top.lat;
	filterQuery.botLon = bot.lon;
	filterQuery.botLat = bot.lat;

}

function getSpecificLoadArea() {

	var loadArea = {};

	// Get center from viewport
	var viewportWidth = $(embryo.mapPanel.map.getViewport()).width();
	var viewportHeight = $(embryo.mapPanel.map.getViewport()).height();
	var centerPixel = new OpenLayers.Pixel(viewportWidth * 0.50,
			viewportHeight * 0.50);
	var center = embryo.mapPanel.map.getLonLatFromPixel(centerPixel).transform(
			embryo.mapPanel.map.getProjectionObject(), // from Spherical
			// Mercator Projection
			new OpenLayers.Projection("EPSG:4326") // to WGS 1984
	);

	// Create area
	var topLeft = {};
	topLeft.lon = center.lon - fixedLoadAreaSize / 2;
	topLeft.lat = center.lat + fixedLoadAreaSize / 2;
	var botRight = {};
	botRight.lon = center.lon + fixedLoadAreaSize / 2;
	botRight.lat = center.lat - fixedLoadAreaSize / 2;

	// Correct area wrapping
	if (topLeft.lon > 180) {
		topLeft.lon = topLeft.lon - 180 * 2;
	} else if (topLeft.lon < -180) {
		topLeft.lon = topLeft.lon + 180 * 2;
	}

	if (botRight.lon > 180) {
		botRight.lon = botRight.lon - 180 * 2;
	} else if (topLeft.lon < -180) {
		botRight.lon = botRight.lon + 180 * 2;
	}

	if (topLeft.lat > 90) {
		topLeft.lat = 90;
	} else if (topLeft.lat < -90) {
		topLeft.lat = -90;
	}

	if (botRight.lat > 90) {
		botRight.lat = 90;
	} else if (topLeft.lat < -90) {
		botRight.lat = -90;
	}

	loadArea.top = topLeft;
	loadArea.bot = botRight;

	return loadArea;

}

/**
 * Moves the focus to a vessel. The zoom level is specified in the settings.js
 * file.
 */
function goToVessel(vessel) {

	var center = new OpenLayers.LonLat(vessel.lon, vessel.lat).transform(
			new OpenLayers.Projection("EPSG:4326"), embryo.mapPanel.map
					.getProjectionObject());

	embryo.selectedVessel = vessel;

	embryo.mapPanel.map.setCenter(center, focusZoom);

	setTimeToLoad(400);

}

/**
 * Moves the location to a vessel. The zoom level is specified in the
 * settings.js file.
 */
function goToVesselLocation(vessel) {

	var center = new OpenLayers.LonLat(vessel.lon, vessel.lat).transform(
			new OpenLayers.Projection("EPSG:4326"), embryo.mapPanel.map
					.getProjectionObject());

	embryo.mapPanel.map.setCenter(center, focusZoom);

}

/**
 * Moves the focus to a vessel in the search results. The zoom level is
 * specified in the settings.js file.
 */
function goToSearchedVessel(key) {

	var vessel = searchResults[key];

	var center = new OpenLayers.LonLat(vessel.lon, vessel.lat).transform(
			new OpenLayers.Projection("EPSG:4326"), embryo.mapPanel.map
					.getProjectionObject());

	searchedVessel = vessel;
	selectSearchedVessel = true;

	embryo.mapPanel.map.setCenter(center, focusZoom);

	setTimeToLoad(400);

}

/**
 * Moves the focus to a specific location and zoom level. The zoom level is
 * specified in the settings.js file.
 */
function goToLocation(longitude, latitude) {

	// Hide tools
	$("#abnormalPanel").css('visibility', 'hidden');
	$("#lightBoxEffect").css('visibility', 'hidden');
	$("#flash").css('visibility', 'hidden');
	abnormalOpen = false;

	var center = new OpenLayers.LonLat(longitude, latitude).transform(
			new OpenLayers.Projection("EPSG:4326"), embryo.mapPanel.map
					.getProjectionObject());

	embryo.mapPanel.map.setCenter(center, focusZoom);

}

/**
 * Converts a string in 12hr format to 24h format.
 * 
 * @param time
 *            a string in the following format: "12:36:26 PM"
 */
function to24hClock(time) {

	// Parse data
	var hour = parseInt(time.split(":")[0]);
	var min = parseInt(time.split(":")[1]);
	var sec = parseInt(time.split(":")[2]);
	var ampm = time.split(" ")[1];

	// AM?
	if (ampm == "PM") {
		hour += 12;
		if (hour == 24) {
			hour = 12;
		}
	} else if (hour == 12) {
		hour = 0;
	}

	// Insert zeroes
	hour = hour += "";
	min = min += "";
	sec = sec += "";
	if (hour.length == 1) {
		hour = "0" + hour;
	}
	if (min.length == 1) {
		min = "0" + min;
	}
	if (sec.length == 1) {
		sec = "0" + sec;
	}

	return hour + ":" + min + ":" + sec;

}

/**
 * Transforms a position to a position that can be used by OpenLayers. The
 * transformation uses OpenLayers.Projection("EPSG:4326").
 * 
 * @param lon
 *            The longitude of the position to transform
 * @param lat
 *            The latitude of the position to transform
 * @returns The transformed position as a OpenLayers.LonLat instance.
 */
function transformPosition(lon, lat) {
	return new OpenLayers.LonLat(lon, lat).transform(new OpenLayers.Projection(
			"EPSG:4326"), // transform from WGS 1984
	embryo.mapPanel.map.getProjectionObject() // to Spherical Mercator
	// Projection
	);
}

/**
 * Resets the filterQuery object and adds a filter preset.
 * 
 * @param presetSelect
 *            A string in the following form: "key = value"
 * 
 */
function useFilterPreset(presetSelect) {

	resetFilterQuery();

	filter = presetSelect.options[presetSelect.selectedIndex].value;
	if (filter != "") {
		var expr = 'filterQuery.' + filter.split("=")[0] + ' = "'
				+ filter.split("=")[1] + '"';
		eval(expr);
	}

	parseFilterQuery();
	filterChanged();
}

/**
 * Resets the filterQuery.
 */
function resetFilterQuery() {
	delete filterQuery.country;
	delete filterQuery.sourceCountry;
	delete filterQuery.sourceType;
	delete filterQuery.sourceRegion;
	delete filterQuery.sourceBS;
	delete filterQuery.sourceSystem;
	delete filterQuery.vesselClass;
}

/**
 * Clears the values in the filter panel.
 */
function clearFilters() {
	$("#country").val("");
	$("#soruceCountry").val("");
	$("#sourceType").val("");
	$("#sourceRegion").val("");
	$("#sourceBS").val("");
	$("#sourceSystem").val("");
	$("#vesselClass").val("");
}

/**
 * Sets the values in the filter panel equal to the values of the filterQuery
 * object.
 */
function parseFilterQuery() {
	clearFilters();
	$("#country").val(filterQuery.country);
	$("#soruceCountry").val(filterQuery.sourceCountry);
	$("#sourceType").val(filterQuery.sourceType);
	$("#sourceRegion").val(filterQuery.sourceRegion);
	$("#sourceBS").val(filterQuery.sourceBS);
	$("#sourceSystem").val(filterQuery.sourceSystem);
	$("#vesselClass").val(filterQuery.vesselClass);
}

/**
 * Applys the values of the filter panel to the filterQuery object.
 */
function applyFilter() {
	resetFilterQuery();

	filterQuery.country = $("#country").val();
	filterQuery.sourceCountry = $("#soruceCountry").val();
	filterQuery.sourceType = $("#sourceType").val();
	filterQuery.sourceRegion = $("#sourceRegion").val();
	filterQuery.sourceBS = $("#sourceBS").val();
	filterQuery.sourceSystem = $("#sourceSystem").val();
	filterQuery.vesselClass = $("#vesselClass").val();

	filterChanged();
}

/**
 * Method for refreshing when filtering is changed.
 */
function filterChanged() {
	// Save query cookie
	var f = JSON.stringify(filterQuery);
	setCookie("dma-ais-query", f, 30);

	loadVessels();
}

/**
 * Method for saving the current view into a cookie.
 */
function saveViewCookie() {
	var center = embryo.mapPanel.map.getCenter();
	setCookie("dma-ais-zoom", embryo.mapPanel.map.zoom, 30);
	var lonlat = new OpenLayers.LonLat(embryo.mapPanel.map.center.lon,
			embryo.mapPanel.map.center.lat).transform(embryo.mapPanel.map
			.getProjectionObject(), // from Spherical Mercator Projection
	new OpenLayers.Projection("EPSG:4326") // to WGS 1984
	);
	setCookie("dma-ais-lat", lonlat.lat, 30);
	setCookie("dma-ais-lon", lonlat.lon, 30);
}

/**
 * Get settings from cookies
 */
function loadView() {

	var zoom = getCookie("dma-ais-zoom");
	var lat = getCookie("dma-ais-lat");
	var lon = getCookie("dma-ais-lon");
	var q = getCookie("dma-ais-query");
	if (zoom) {
		initialZoom = parseInt(zoom);
	}
	if (lat && lon) {
		initialLat = parseFloat(lat);
		initialLon = parseFloat(lon);
	}
	if (q) {
		eval("filterQuery = " + q + ";");
	}
}

/**
 * Method for setting a cookie.
 */
function setCookie(c_name, value, exdays) {
	var exdate = new Date();
	exdate.setDate(exdate.getDate() + exdays);
	var c_value = escape(value)
			+ ((exdays == null) ? "" : "; expires=" + exdate.toUTCString());
	document.cookie = c_name + "=" + c_value;
}

/**
 * Method for getting a cookie.
 */
function getCookie(c_name) {
	var i, x, y, ARRcookies = document.cookie.split(";");
	for (i = 0; i < ARRcookies.length; i++) {
		x = ARRcookies[i].substr(0, ARRcookies[i].indexOf("="));
		y = ARRcookies[i].substr(ARRcookies[i].indexOf("=") + 1);
		x = x.replace(/^\s+|\s+$/g, "");
		if (x == c_name) {
			return unescape(y);
		}
	}
}
