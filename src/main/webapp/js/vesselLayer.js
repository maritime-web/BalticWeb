// this.addLayerInitializer(embryo.vessel.initLayers);

$(document).ready(function() {
	embryo.vessel.initLayers();
});
/**
 * Adds all the layers that will contain graphic.
 */
embryo.vessel = {};
embryo.vessel.initLayers = function() {

	// Get renderer
	var renderer = OpenLayers.Util.getParameters(window.location.href).renderer;
	renderer = (renderer) ? [ renderer ]
			: OpenLayers.Layer.Vector.prototype.renderers;
	// renderer = ["Canvas", "SVG", "VML"];

	// Create vector layer with a stylemap for vessels
	this.vesselLayer = new OpenLayers.Layer.Vector("Vessels", {
		styleMap : new OpenLayers.StyleMap({
			"default" : {
				externalGraphic : "${image}",
				graphicWidth : "${imageWidth}",
				graphicHeight : "${imageHeight}",
				graphicYOffset : "${imageYOffset}",
				graphicXOffset : "${imageXOffset}",
				rotation : "${angle}"
			},
			"select" : {
				cursor : "crosshair",
				externalGraphic : "${image}"
			}
		}),
		renderers : renderer
	});

	embryo.mapPanel.map.addLayer(this.vesselLayer);
	embryo.mapPanel.add2SelectFeatureCtrl(this.vesselLayer);
	embryo.mapPanel.add2HoverFeatureCtrl(this.vesselLayer);


	// Create vector layer with a stylemap for the selection image
	markerLayer = new OpenLayers.Layer.Vector("Markers", {
		styleMap : new OpenLayers.StyleMap({
			"default" : {
				externalGraphic : "${image}",
				graphicWidth : "${imageWidth}",
				graphicHeight : "${imageHeight}",
				graphicYOffset : "${imageYOffset}",
				graphicXOffset : "${imageXOffset}",
				rotation : "${angle}"
			},
			"select" : {
				cursor : "crosshair",
				externalGraphic : "${image}"
			}
		}),
		renderers : renderer
	});

	embryo.mapPanel.map.addLayer(markerLayer);

	// Create vector layer with a stylemap for the selection image
	selectionLayer = new OpenLayers.Layer.Vector("Selection", {
		styleMap : new OpenLayers.StyleMap({
			"default" : {
				externalGraphic : "${image}",
				graphicWidth : "${imageWidth}",
				graphicHeight : "${imageHeight}",
				graphicYOffset : "${imageYOffset}",
				graphicXOffset : "${imageXOffset}",
				rotation : "${angle}"
			},
			"select" : {
				cursor : "crosshair",
				externalGraphic : "${image}"
			}
		}),
		renderers : renderer
	});

	// Create vector layer for past tracks
	tracksLayer = new OpenLayers.Layer.Vector("trackLayer", {
		styleMap : new OpenLayers.StyleMap({
			'default' : {
				strokeColor : pastTrackColor,
				strokeOpacity : pastTrackOpacity,
				strokeWidth : pastTrackWidth
			}
		})
	});

	// Create vector layer for time stamps
	timeStampsLayer = new OpenLayers.Layer.Vector("timeStampsLayer", {
		styleMap : new OpenLayers.StyleMap({
			'default' : {
				label : "${timeStamp}",
				fontColor : timeStampColor,
				fontSize : timeStampFontSize,
				fontFamily : timeStampFontFamily,
				fontWeight : timeStampFontWeight,
				labelAlign : "${align}",
				labelXOffset : "${xOffset}",
				labelYOffset : "${yOffset}",
				labelOutlineColor : timeStamtOutlineColor,
				labelOutlineWidth : 5,
				labelOutline : 1
			}
		})
	});

	// Create cluster layer
	clusterLayer = new OpenLayers.Layer.Vector("Clusters", {
		styleMap : new OpenLayers.StyleMap({
			'default' : {
				fillColor : "${fill}",
				fillOpacity : clusterFillOpacity,
				strokeColor : clusterStrokeColor,
				strokeOpacity : clusterStrokeOpacity,
				strokeWidth : clusterStrokeWidth
			}
		})
	});

	embryo.mapPanel.map.addLayer(clusterLayer);

	// Create cluster text layer
	clusterTextLayer = new OpenLayers.Layer.Vector("Cluster text", {
		styleMap : new OpenLayers.StyleMap({
			'default' : {
				label : "${count}",
				fontColor : clusterFontColor,
				fontSize : "${fontSize}",
				fontWeight : clusterFontWeight,
				fontFamily : clusterFontFamily,
				labelAlign : "c"
			}
		})
	});

	embryo.mapPanel.map.addLayer(clusterTextLayer);

	// Create layer for individual vessels in cluster
	indieVesselLayer = new OpenLayers.Layer.Vector("Points", {
		styleMap : new OpenLayers.StyleMap({
			"default" : {
				pointRadius : indieVesselRadius,
				fillColor : indieVesselColor,
				strokeColor : indieVesselStrokeColor,
				strokeWidth : indieVesselStrokeWidth,
				graphicZIndex : 1
			},
			"select" : {
				pointRadius : indieVesselRadius * 3,
				fillColor : indieVesselColor,
				strokeColor : indieVesselStrokeColor,
				strokeWidth : indieVesselStrokeWidth,
				graphicZIndex : 1
			}
		})
	});

	embryo.mapPanel.map.addLayer(indieVesselLayer);
	embryo.mapPanel.add2SelectFeatureCtrl(indieVesselLayer);
	embryo.mapPanel.add2HoverFeatureCtrl(indieVesselLayer);
	
	embryo.mapPanel.map.addLayer(selectionLayer);
	embryo.mapPanel.map.addLayer(tracksLayer);
	embryo.mapPanel.map.addControl(new OpenLayers.Control.DrawFeature(
			tracksLayer, OpenLayers.Handler.Path));
	embryo.mapPanel.map.addLayer(timeStampsLayer);
	
	function onSelect(event) {
		var feature = event.feature;
		if (embryo.selectedVessel
				&& embryo.selectedVessel.id == feature.attributes.vessel.id) {
			selectedFeature = null;
			embryo.selectedVessel = null;
			detailsReadyToClose = true;
			// $("#hoveringBox").css('display', 'none');
			redrawSelection();
			selectControlVessels.unselectAll();
		} else {
			selectedFeature = feature;
			embryo.selectedVessel = feature.attributes.vessel;
			embryo.eventbus.fireEvent(embryo.eventbus
					.VesselSelectedEvent(feature.attributes.id));
			// embryo.vesselDetailsPanel.update(feature.attributes.id);

			// $("#hoveringBox").css('display', 'none');
			// selectControlVessels.select(feature);
			redrawSelection();
		}
	};
	
	function onUnselect(event) {
		var feature = event.feature;
		selectedFeature = null;
		embryo.selectedVessel = null;
		detailsReadyToClose = true;
		tracksLayer.removeAllFeatures();
		timeStampsLayer.removeAllFeatures();
		redrawSelection();
		//selectControlVessels.unselectAll();
	}

	this.vesselLayer.events.on({
		featureselected : onSelect,
		featureunselected : onUnselect
	});

	indieVesselLayer.events.on({
		"featureselected" : onSelect,
		"featureunselected" : onUnselect
	});
	

    embryo.mapPanel.hoveringHandlers.push(function(e) {
		if (e.feature.attributes.vessel) {
		    var id = "name_"+e.feature.attributes.id;
			$.getJSON(detailsUrl, {
				past_track : '1',
				id : e.feature.attributes.id
			}, function(result) {
				$("#"+id).html(
				    "<div class='whiteOpacity shadowsIE vesselNameBox'>"+result.name+"</div>"
				);
			});
			return "<div id="+id+"></div>";
		} else {
		    return null;
        }
    });

};


/**
 * Loads vessels if time since last update is higher than loadFrequence.
 */
function loadVesselsIfTime() {

	var timeSinceLastLoad = new Date().getTime() - timeOfLastLoad;

	if (timeOfLastLoad == 0 || timeSinceLastLoad >= loadFrequence) {
		loadVessels();
	}

}

/**
 * Loads vessels in the specified amount of time.
 */
function setTimeToLoad(ms) {

	var timeSinceLastLoad = new Date().getTime() - timeOfLastLoad;

	timeOfLastLoad -= (loadFrequence - timeSinceLastLoad);
	timeOfLastLoad += ms;

}

/**
 * Loads the vessels using JSON. If the zoom level is higher than or equal to
 * the minimum zoom level it adds each vessel as a vessel instance to the list
 * of vessels. The vessels will be drawn when the JSON is received. If the zoom
 * level is lower than the minumum zoom level, it draws the vesselclusters
 * instead.
 */
function loadVessels() {

	// Reset list of vessels
	vessels = [];
	clusters = [];

	if (embryo.mapPanel.map.zoom >= vesselZoomLevel || loadAllVessels) {

		loadVesselList();

		clusterLayer.setVisibility(false);
		clusterTextLayer.setVisibility(false);
		indieVesselLayer.setVisibility(false);

		embryo.vessel.vesselLayer.setVisibility(true);

		//selectControlVessels.activate();
		// selectControlIndieVessels.deactivate();

	} else {

		if (includeClustering) {

			loadVesselClusters();

			clusterLayer.setVisibility(true);
			clusterTextLayer.setVisibility(true);
			indieVesselLayer.setVisibility(true);

		}

		embryo.vessel.vesselLayer.setVisibility(false);
		//selectControlVessels.deactivate();
		// selectControlIndieVessels.activate();

	}

	// Set time of load
	timeOfLastLoad = new Date().getTime();

}

/**
 * Loads and draws all vessels in the view.
 */
function loadVesselList() {
	saveViewPort();

	// Generate data
	var data = filterQuery;
	lastRequestId++;
	data.requestId = lastRequestId;
	if (!loadViewportOnly || loadAllVessels) {
		delete data.topLon;
		delete data.topLat;
		delete data.botLon;
		delete data.botLat;
	}
	if (loadFixedAreaSize && !loadAllVessels) {
		lastLoadArea = getSpecificLoadArea();
		data.topLon = lastLoadArea.top.lon;
		data.topLat = lastLoadArea.top.lat;
		data.botLon = lastLoadArea.bot.lon;
		data.botLat = lastLoadArea.bot.lat;
	}

    var messageId = embryo.messagePanel.show( { text: "Loading vessels ..." })

    $.ajax({
        url: listUrl,
        data: data,
        success: function (result) {
        	if (result.requestId != lastRequestId)
        	    return;

        	// Update vessel counter
        	$("#vesselsTotal").html(result.vesselsInWorld);

        	embryo.messagePanel.replace(messageId, { text: result.vesselsInWorld + " vessels loaded.", type: "success" })

        	// Load new vessels
        	var JSONVessels = result.vesselList.vessels;

        	for (vesselId in JSONVessels) {
        	    // Create vessel based on JSON data
        	    var vesselJSON = JSONVessels[vesselId];
        	    var vessel = new Vessel(vesselId, vesselJSON, 1);

        	    if (embryo.selectedVessel && vesselId == embryo.selectedVessel.id
        		&& !selectSearchedVessel) {
        		// Update selected vessel
        		embryo.selectedVessel = vessel;
        	    } else if (selectSearchedVessel && searchedVessel
        		       && vesselId == searchedVessel.id) {
        		// Update selected vessel
        		embryo.selectedVessel = vessel;
        		vessels.push(vessel);
        	    }

        	    vessels.push(vessel);

        	}

        	// Draw vessels
        	drawVessels();

        	selectSearchedVessel = false;

        },
        error: function(data) {
            embryo.messagePanel.replace(messageId, { text: "Server returned error code: " + data.status + " loading vessels.", type: "error" });
            console.log("Server returned error code: " + data.status + " loading vessels.");
        }
    });
}
/**
 * Draws an individual vessel.
 */
function drawIndieVessel(vessel) {

	// Add feature
	var loc = transformPosition(vessel.lon, vessel.lat);
	var geom = new OpenLayers.Geometry.Point(loc.lon, loc.lat);
	var attr = {
		id : vessel.id,
		type : "indie",
		vessel : vessel,
		angle : vessel.degree
	};

	if (embryo.selectedVessel && vessel.id == embryo.selectedVessel.id
			&& selectedFeature.attributes.type == "indie") {

		selectedFeature.attributes = attr;
		selectedFeature.geometry = geom;

	} else {

		feature = new OpenLayers.Feature.Vector(geom, attr);
		indieVesselLayer.addFeatures([ feature ]);

	}

}

/**
 * Draws all known vessels using vector points styled to show images. Vessels
 * are drawn based on their color, angle and whether they are moored on not.
 */
function drawVessels() {
	var vesselFeatures = [];
	var selectionFeatures = [];
	selectedVesselInView = false;

	// Update number of vessels
	$("#vesselsView").html("" + vessels.length);

	// Iterate through vessels where value refers to each vessel.
	$.each(vessels, function(key, value) {

		var attr = {
			id : value.id,
			angle : value.degree - 90,
			opacity : 1,
			image : "img/" + value.image,
			imageWidth : value.imageWidth,
			imageHeight : value.imageHeight,
			imageYOffset : value.imageYOffset,
			imageXOffset : value.imageXOffset,
			type : "vessel",
			vessel : value
		}

		var geom = new OpenLayers.Geometry.Point(value.lon, value.lat)
				.transform(new OpenLayers.Projection("EPSG:4326"), // transform
				// from WGS
				// 1984
				embryo.mapPanel.map.getProjectionObject() // to Spherical
				// Mercator
				// Projection
				);

		if (embryo.selectedVessel && selectedFeature
				&& value.id == embryo.selectedVessel.id
				&& selectedFeature.attributes.type == "vessel") {

			selectedFeature.attributes = attr;
			selectedFeature.geometry = geom;

		} else {

			// Use styled vector points
			var feature = new OpenLayers.Feature.Vector(geom, attr);

			vesselFeatures.push(feature);

			// Select searched vessel?
			if (selectSearchedVessel && searchedVessel
					&& searchedVessel.id == value.id) {
				selectedFeature = feature;
			}

			// Select selected vessel?
			if (embryo.selectedVessel && embryo.selectedVessel.id == value.id
					&& !selectedFeature) {
				selectedFeature = feature;
			}

			// Update marked vessel
			if (markedVessel && markedVessel.id == value.id) {
				markedVessel = value;
			}

		}

	});

	// Draw marker
	if (markedVessel) {
		redrawMarker();
	}

	// Set vessel in focus if selected
	vesselInFocus(embryo.selectedVessel, selectedFeature);

	// Remove old features except selected feature
	var arr = embryo.vessel.vesselLayer.features.slice();
	var idx = arr.indexOf(selectedFeature);
	if (idx != -1)
		arr.splice(idx, 1);
	embryo.vessel.vesselLayer.addFeatures(vesselFeatures);
	embryo.vessel.vesselLayer.destroyFeatures(arr);

	// Redraw
	addSelectionFeature();
	embryo.vessel.vesselLayer.renderer.clear();
	embryo.vessel.vesselLayer.redraw();
	selectionLayer.redraw();
	drawPastTrack(null);

}

/**
 * Sets a vessel in focus if it is selected.
 */
function vesselInFocus(vessel, feature) {

	if (embryo.selectedVessel && feature
			&& vessel.id == embryo.selectedVessel.id) {

		selectedVesselInView = true;

		// Update selected vessel
		embryo.selectedVessel = vessel;
		selectedFeature = feature;

		// Update vessel details
		embryo.eventbus.fireEvent(embryo.eventbus
				.VesselSelectedEvent(feature.attributes.id));

		// embryo.vesselDetailsPanel.update(feature.attributes.id);

	}

}

/**
 * Adds the selection feature if a feature is selected.
 */
function addSelectionFeature() {

	// Add selection
	if (selectedFeature && selectedVesselInView) {
		var selectionFeature = new OpenLayers.Feature.Vector(
				new OpenLayers.Geometry.Point(selectedFeature.geometry.x,
						selectedFeature.geometry.y), {
					id : -1,
					angle : selectedFeature.attributes.angle - 90,
					opacity : 1,
					image : "img/selection.png",
					imageWidth : 32,
					imageHeight : 32,
					imageYOffset : -16,
					imageXOffset : -16,
					type : "selection"
				});

		selectionLayer.removeAllFeatures();
		selectionLayer.addFeatures([ selectionFeature ]);

	}

}

/**
 * Redraws all features in vessel layer and selection layer. Features are
 * vessels.
 */
function redrawSelection() {
	var selectionFeature;
	var selectionFeatures = [];
	drawPastTrack(null);

	// Set search result in focus
	if (selectedFeature) {
		selectionFeature = new OpenLayers.Feature.Vector(
				new OpenLayers.Geometry.Point(selectedFeature.geometry.x,
						selectedFeature.geometry.y), {
					id : -1,
					angle : selectedFeature.attributes.angle - 90,
					opacity : 1,
					image : "img/selection.png",
					imageWidth : 32,
					imageHeight : 32,
					imageYOffset : -16,
					imageXOffset : -16,
					type : "selection"
				});

		selectionFeatures.push(selectionFeature);
		selectedVesselInView = true;

		// embryo.eventbus.fireEvent(embryo.eventbus.VesselSelectedEvent(selectedFeature.attributes.id));

		// embryo.vesselDetailsPanel.update(selectedFeature.attributes.id);

	}

	selectionLayer.removeAllFeatures();
	selectionLayer.addFeatures(selectionFeatures);
	selectionLayer.redraw();
}

function redrawMarker() {

	var loc = transformPosition(markedVessel.lon, markedVessel.lat);
	var geom = new OpenLayers.Geometry.Point(loc.lon, loc.lat);

	var markerFeature = new OpenLayers.Feature.Vector(
			new OpenLayers.Geometry.Point(geom.x, geom.y), {
				id : -1,
				angle : 0,
				opacity : 1,
				image : "img/green_marker.png",
				imageWidth : 32,
				imageHeight : 32,
				imageYOffset : -16,
				imageXOffset : -16,
				type : "marker"
			});

	markerLayer.removeAllFeatures();
	markerLayer.addFeatures(markerFeature);
	markerLayer.redraw();

}

/**
 * Draws the past track. If tracks are null, it will simply remove all tracks
 * and draw nothing.
 * 
 * @param tracks
 *            Array of tracks
 */
function drawPastTrack(tracks) {

	// Remove old tracks
	tracksLayer.removeAllFeatures();
	timeStampsLayer.removeAllFeatures();

	// Get time stamp distance
	var CL = false;
	var tracksBetweenTimeStamps;
	if (embryo.mapPanel.map.zoom >= vesselZoomLevel) {
		tracksBetweenTimeStamps = tracksBetweenTimeStampsVL;
	} else {
		tracksBetweenTimeStamps = tracksBetweenTimeStampsCL;
		CL = true;
	}

	// Draw tracks
	if (selectedVesselInView && tracks && includePastTracks) {
		var lastLon;
		var lastLat;
		var firstPoint = true;
		var untilTimeStamp = 0;

		for (track in tracks) {
			var currentTrack = tracks[track];
			if (!firstPoint) {
				// Insert line
				var points = new Array(new OpenLayers.Geometry.Point(lastLon,
						lastLat)
						.transform(new OpenLayers.Projection("EPSG:4326"),
								embryo.mapPanel.map.getProjectionObject()),
						new OpenLayers.Geometry.Point(currentTrack.lon,
								currentTrack.lat).transform(
								new OpenLayers.Projection("EPSG:4326"),
								embryo.mapPanel.map.getProjectionObject()));

				var line = new OpenLayers.Geometry.LineString(points);
				var lineFeature = new OpenLayers.Feature.Vector(line);
				tracksLayer.addFeatures([ lineFeature ]);

				// Insert timeStamp?
				if (untilTimeStamp == 0
						&& parseInt(track) + tracksBetweenTimeStamps < tracks.length
						&& includeTimeStamps && (includeTimeStampsOnCL || !CL)) {

					var timeStampPos = points[0];
					var timeStampFeature = new OpenLayers.Feature.Vector(
							timeStampPos);

					// Remove date from time
					var time = (new Date(currentTrack.time)).toTimeString();

					// Change to 24h clock
					time = to24hClock(time);

					timeStampFeature.attributes = {
						timeStamp : time
					};
					timeStampsLayer.addFeatures([ timeStampFeature ]);

					untilTimeStamp = tracksBetweenTimeStamps;

				} else {
					untilTimeStamp--;
				}
			}
			lastLon = currentTrack.lon;
			lastLat = currentTrack.lat;
			firstPoint = false;
		}

		// Draw features
		tracksLayer.refresh();
		timeStampsLayer.refresh();
	}
}
