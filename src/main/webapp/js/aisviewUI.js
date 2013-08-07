// TODO: All JavaScript should be placed in appropriate namespaces according to their components etc.
// var embryo = {}; defined in aisview.js, which is imported first

// Panel variables
var detailsOpen = false;
var filteringOpen = false;
var feedOpen = false;
var abnormalOpen = false;
var detailsReadyToClose = false;
var tools = [];

// Layers
var vesselLayer;
var selectionLayer;
var markerLayer;
var tracksLayer;
var timeStampsLayer;
var clusterLayer;
var clusterTextLayer;
var indieVesselLayer;

function includePanels() {
	if (includeFilteringPanel) {
		$("#filteringPanel").css('visibility', 'visible');
	} else {
		$("#filteringPanel").remove();
	}

	if (includeDetailsPanel) {
		$("#detailsPanel").css('visibility', 'visible');
	} else {
		$("#detailsPanel").remove();
	}

	if (includeFeedPanel) {
		$("#feedPanel").css('visibility', 'visible');
	} else {
		$("#feedPanel").remove();
	}

	if (!includeAbnormalBehaviorPanel) {
		$("#abnormalPanel").remove();
		$("#lightBoxEffect").remove();
	} else {
		$("#eventFromDate").datepicker();
		$("#eventToDate").datepicker();
	}

	if (!includeZoomPanel) {
		$(".olControlZoom").remove();
	}

	if (loadFixedAreaSize) {
		$("#vesselsView").remove();
		$("#vesselsViewText").remove();
	}

	if (!includeAdvancedTools) {
		$("#toolbox").remove();
		$("#toolboxHeader").remove();
	}

}

function includeTools() {

	if (includeAbnormalBehaviorTool) {

		var tool = new Tool("abnormalPanel", "abnormal");
		tools.push(tool);
		addToToolbox(tool);
	}

}

function addToToolbox(tool) {

	$("#toolbox").append(toolToHTML(tool));

}

function openPanel(panelId) {

	if (!abnormalOpen) {
		var n = "#" + panelId + "";
		$("#" + panelId).css('visibility', 'visible');
		$("#lightBoxEffect").css('visibility', 'visible');
		$("#flash").css('visibility', 'visible');
		abnormalOpen = true;
	}

}

/**
 * Adds all the layers that will contain graphic.
 */
function addLayers() {

	// Get renderer
	var renderer = OpenLayers.Util.getParameters(window.location.href).renderer;
	renderer = (renderer) ? [ renderer ]
			: OpenLayers.Layer.Vector.prototype.renderers;
	// renderer = ["Canvas", "SVG", "VML"];

	// Create vector layer with a stylemap for vessels
	vesselLayer = new OpenLayers.Layer.Vector("Vessels", {
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

	embryo.mapPanel.map.addLayer(vesselLayer);

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
	embryo.mapPanel.map.addLayer(selectionLayer);
	embryo.mapPanel.map.addLayer(tracksLayer);
	embryo.mapPanel.map.addControl(new OpenLayers.Control.DrawFeature(
			tracksLayer, OpenLayers.Handler.Path));
	embryo.mapPanel.map.addLayer(timeStampsLayer);

	// Add OpenStreetMap Layer
	var osm = new OpenLayers.Layer.OSM("OSM",
			"http://a.tile.openstreetmap.org/${z}/${x}/${y}.png", {
				'layers' : 'basic',
				'isBaseLayer' : true
			});

	// Add OpenStreetMap Layer
	embryo.mapPanel.map.addLayer(osm);

	embryo.route.initLayer();

	// Add KMS Layer
	// addKMSLayer();

}

/**
 * Sets up the panels, event listeners and selection controllers.
 */
function setupUI() {

	setupDatePickers();

	// Set zoom panel positon
	$(".olControlZoom").css('left', zoomPanelPositionLeft);
	$(".olControlZoom").css('top', zoomPanelPositionTop);

	// Set loading panel positon
	var x = $(document).width() / 2 - $("#loadingPanel").width() / 2;
	$("#loadingPanel").css('left', x);

	//
	embryo.mapPanel.map.events.includeXY = true;

	// Create functions for hovering a vessel
	var showName = function(e) {
		var lonlatCenter = e.feature.geometry.getBounds().getCenterLonLat();
		if (e.feature.attributes.vessel) {
			$.getJSON(detailsUrl, {
				past_track : '1',
				id : e.feature.attributes.id
			}, function(result) {
				var pixelTopLeft = new OpenLayers.Pixel(0, 0);
				var lonlatTopLeft = embryo.mapPanel.map
						.getLonLatFromPixel(pixelTopLeft);
				pixelTopLeft = embryo.mapPanel.map
						.getPixelFromLonLat(lonlatTopLeft);

				var pixel = embryo.mapPanel.map
						.getPixelFromLonLat(lonlatCenter);

				var x = pixel.x - pixelTopLeft.x;
				var y = pixel.y - pixelTopLeft.y;

				$("#vesselNameBox").html(result.name);
				$("#vesselNameBox").css('visibility', 'visible');
				$("#vesselNameBox").css('top', (y - 26) + 'px');
				$("#vesselNameBox").css('left', x + 'px');
			});
		}
	};

	var hideName = function(e) {
		// $("#vesselNameBox").css('visibility', 'hidden');
	};

	vesselLayer.events.on({
		"featurehighlighted" : function(e) {
			alert(e);
		}
	});

	// Create hover control - vessels
	hoverControlVessels = new OpenLayers.Control.SelectFeature(vesselLayer, {
		hover : true,
		highlightOnly : true,
		eventListeners : {
			featurehighlighted : showName,
			featureunhighlighted : hideName
		}
	});

	// Create hover control - indie vessels
	hoverControlIndieVessels = new OpenLayers.Control.SelectFeature(
			indieVesselLayer, {
				hover : true,
				highlightOnly : true,
				eventListeners : {
					featurehighlighted : showName,
					featureunhighlighted : hideName
				}
			});

	// Create select control - vessels
	selectControlVessels = new OpenLayers.Control.SelectFeature(
			vesselLayer,
			{
				clickout : true,
				toggle : true,
				onSelect : function(feature) {
					if (embryo.selectedVessel
							&& embryo.selectedVessel.id == feature.attributes.vessel.id) {
						selectedFeature = null;
						embryo.selectedVessel = null;
						detailsReadyToClose = true;
						$("#vesselNameBox").css('visibility', 'hidden');
						redrawSelection();
						selectControlVessels.unselectAll();
					} else {
						selectedFeature = feature;
						embryo.selectedVessel = feature.attributes.vessel;
						embryo.eventbus.fireEvent(embryo.eventbus
								.VesselSelectedEvent(feature.attributes.id));
						// embryo.vesselDetailsPanel.update(feature.attributes.id);

						$("#vesselNameBox").css('visibility', 'hidden');
						// selectControlVessels.select(feature);
						redrawSelection();
					}
				},
				onUnselect : function(feature) {
					selectedFeature = null;
					embryo.selectedVessel = null;
					detailsReadyToClose = true;
					tracksLayer.removeAllFeatures();
					timeStampsLayer.removeAllFeatures();
					redrawSelection();
					selectControlVessels.unselectAll();
				}
			});

	// Create select control - indie vessels
	selectControlIndieVessels = new OpenLayers.Control.SelectFeature(
			indieVesselLayer,
			{
				clickout : true,
				toggle : true,
				onSelect : function(feature) {
					if (embryo.selectedVessel
							&& embryo.selectedVessel.id == feature.attributes.vessel.id) {
						selectedFeature = null;
						embryo.selectedVessel = null;
						detailsReadyToClose = true;
						$("#vesselNameBox").css('visibility', 'hidden');
						redrawSelection();
						selectControlIndieVessels.unselectAll();
					} else {
						selectedFeature = feature;
						embryo.selectedVessel = feature.attributes.vessel;

						embryo.eventbus.fireEvent(embryo.eventbus
								.VesselSelectedEvent(feature.attributes.id));
						// embryo.vesselDetailsPanel.update(feature.attributes.id);

						$("#vesselNameBox").css('visibility', 'hidden');
						redrawSelection();
					}
				},
				onUnselect : function(feature) {
					selectedFeature = null;
					embryo.selectedVessel = null;
					detailsReadyToClose = true;
					tracksLayer.removeAllFeatures();
					timeStampsLayer.removeAllFeatures();
					redrawSelection();
					selectControlIndieVessels.unselectAll();
				}
			});

	// Add select controller to map and activate
	embryo.mapPanel.map.addControl(hoverControlVessels);
	embryo.mapPanel.map.addControl(hoverControlIndieVessels);
	embryo.mapPanel.map.addControl(selectControlVessels);
	embryo.mapPanel.map.addControl(selectControlIndieVessels);
	hoverControlVessels.activate();
	hoverControlIndieVessels.activate();
	selectControlVessels.activate();
	selectControlIndieVessels.activate();

	// Register listeners
	embryo.mapPanel.map.events.register("movestart", map, function() {

	});
	embryo.mapPanel.map.events.register("moveend", map, function() {

		saveViewCookie();
		$("#vesselNameBox").css('visibility', 'hidden');

		if (loadAfterMove()) {
			setTimeToLoad(loadDelay);
			loadVesselsIfTime();
		}
		// else {
		// updateVesselsInView();
		// }

		lastZoomLevel = embryo.mapPanel.map.zoom;
	});

	// Set click events on vessel details panel
	$("#detailsHeader").click(function() {
		if (detailsOpen) {
			$("#detailsContainer").slideUp({
				complete : function() {
					$("#detailsHeader").html("Vessel details");
					detailsOpen = false;
					$("#detailsPanel").removeClass("arrowUp");
					$("#detailsPanel").addClass("arrowDown");
					checkForPanelOverflow();
				}
			});
		} else if ($("#detailsContainer").html() != "") {
			$("#detailsHeader").append("<hr class='tight'>");
			$("#detailsContainer").slideDown({
				complete : function() {
					detailsOpen = true;
					$("#detailsPanel").removeClass("arrowDown");
					$("#detailsPanel").addClass("arrowUp");
					checkForPanelOverflow();
				}
			});
		}
	});

	// embryo.legendsPanel.init();

	// Set click events on filtering panel
	$("#filteringHeader").click(function() {
		if (filteringOpen) {
			$("#filteringContainer").slideUp({
				complete : function() {
					filteringOpen = false;
					$("#filteringHeader").html("Filtering");
					$("#filteringPanel").removeClass("arrowUp");
					$("#filteringPanel").addClass("arrowDown");
					checkForPanelOverflow();
				}
			});
		} else {
			$("#filteringContainer").css('display', 'none');
			$("#filteringContainer").html($("#filtering").html());
			$("#filteringHeader").html("Filtering<br /><hr class='tight'>");
			$("#filteringContainer").slideDown({
				complete : function() {
					filteringOpen = true;
					$("#filteringPanel").removeClass("arrowDown");
					$("#filteringPanel").addClass("arrowUp");
					checkForPanelOverflow();
					parseFilterQuery();
				}
			});
		}
	});

	// Set click events on feed panel
	$("#feedHeader").click(
			function() {
				if (feedOpen) {
					$("#feedContainer").slideUp({
						complete : function() {
							feedOpen = false;
							$("#feedContainer").html("");
							$("#feedHeader").html("Abnormal behaviors");
							$("#feedPanel").removeClass("arrowUpWide");
							$("#feedPanel").addClass("arrowDown");
							$("#feedPanel").css('width', '220px');
						}
					});
				} else {
					$("#feedContainer").css('display', 'none');
					$("#feedContainer").html($("#feedContent").html());
					$("#feedHeader").html(
							"Abnormal behaviors - Last " + feedLifeTime
									+ " minutes<br /><hr class='tight'>");
					$("#feedPanel").removeClass("arrowDown");
					$("#feedPanel").css('width', feedPanelExpandedWidth);
					$("#feedPanel").addClass("arrowUpWide");
					$("#feedContainer").slideDown({
						complete : function() {
							feedOpen = true;
							loadBehaviors();
						}
					});
				}
			});

	// Set click events on feed panel
	$("#exitAbnormal").click(function() {
		$("#abnormalPanel").css('visibility', 'hidden');
		$("#lightBoxEffect").css('visibility', 'hidden');
		$("#flash").css('visibility', 'hidden');
		abnormalOpen = false;
	});

	// Close empty panels
	setInterval("closeEmptyPanels()", 1000);

}

embryo.leftPanel = {};
embryo.leftPanel.toggleArrows = function() {
	$(this).toggleClass('arrowDown arrowUp');
};

// På en eller måde skal der lægges noget checkForPanelOverflow ind i leftPanel
// Dvs hvis panelet flyder ud af bunden skal nogen af delpanelerne enten lukkes
// eller have tilføjet en scroller
// checkForPanelOverflow();

embryo.leftPanel.init = function() {
	$('.collapsablePanel .accordion-heading').click(this.toggleArrows);
};

embryo.legendsPanel = {};
// FIXME embryo.legendsPanel.open bruges af checkForPanelOverflow som pt. ikke
// kaldes
// Fjern embryo.legendsPanel.open, når checkForPanelOverflow fjernes/skrives om.
embryo.legendsPanel.open = false;
embryo.legendsPanel.init = function() {

};

embryo.searchPanel = {};
// FIXME embryo.searchPanel.searchOpen bruges af checkForPanelOverflow som pt.
// ikke kaldes
// Fjern embryo.searchPanel.searchOpen, når checkForPanelOverflow
// fjernes/skrives om.
embryo.searchPanel.searchOpen = false;
embryo.searchPanel.init = function() {

	// some search fun

	// Set click events on search panel
	// $("#searchHeader").click(function() {
	// if (embryo.searchPanel.searchOpen) {
	// } else {
	// $("#searchContainer").slideDown({
	// complete : function() {

	// //FIXME should this be removed?
	// parseFilterQuery();
	// }
	// });
	// }
	// });

	// Search when search field is changed
	setInterval("embryo.searchPanel.checkForSearch()", 200);
};

embryo.searchPanel.lastSearch = "";
embryo.searchPanel.checkForSearch = function() {
	var val = $("#searchField").val();
	if (val != this.lastSearch) {
		this.lastSearch = val;
		this.search(val);
	}
};

/**
 * Searches for the vessel described in the search field.
 */
embryo.searchPanel.search = function() {
	// Read search field
	var arg = $("#searchField").val();
	$("#searchResultsTop").empty();
	$("#searchResultsContainer").empty();

	if (arg.length > 0) {

		// Show loader
		$("#searchLoad").css('visibility', 'visible');

		// Load search results
		$.getJSON(searchUrl, {
			argument : arg
		}, function(result) {
			var s = "s";

			// Show search results
			$("#searchResults").css('visibility', 'visible');

			// Search results
			searchResults = [];

			// Get vessels
			for (vesselId in result.vessels) {
				var vesselJSON = result.vessels[vesselId];
				var vessel = new Vessel(vesselId, vesselJSON, 1);
				searchResults.push(vessel);
			}

			// Add search result to list
			if (searchResults.length <= searchResultsLimit
					&& searchResults.length != 0) {
				if (searchResults.length == 1) {
					s = "";
				}
				// embryo.selectedVessel = searchResults[0];

				$("#searchResultsTop").html(
						"<div class='information'>Search results: </div>");
				$.each(searchResults, function(key, value) {

					searchResults.push(value);

					$("#searchResultsContainer").append(
							searchResultToHTML(value, key));

				});

			}

			$("#searchMatch").html(
					result.vesselCount + " vessel" + s + " match.");

			// Hide loader
			$("#searchLoad").css('visibility', 'hidden');

		});
	} else {
		searchResults = [];
		drawVessels();

		// Hide results
		$("#searchMatch").html('');
		$("#searchResults").css('visibility', 'hidden');
	}

}

function setupDatePickers() {

	$("#eventFromDate").datepicker();
	$("#eventToDate").datepicker();

	// Today
	var today = new Date();
	var dd = today.getDate();
	var mm = today.getMonth() + 1; // January is 0!
	var minutes = today.getMinutes();
	var hours = today.getHours();
	var yyyy = today.getFullYear();
	today = mm + '/' + dd + '/' + yyyy;

	// Yesterday
	var yesterday = new Date();
	yesterday.setDate(yesterday.getDate() - 1);
	dd = yesterday.getDate();
	mm = yesterday.getMonth() + 1; // January is 0!
	yyyy = yesterday.getFullYear();

	yesterday = mm + '/' + dd + '/' + yyyy;

	$("#eventFromDate").datepicker("setDate", yesterday);
	$("#eventToDate").datepicker("setDate", today);

	// Set time
	minutes = round5(minutes);

	if (minutes == 60) {
		if (hours != 23) {
			hours++;
			minutes = 0;
		}
	}

	$("#eventFromHour").val(hours);
	$("#eventToHour").val(hours);
	$("#eventFromMin").val(minutes);
	$("#eventToMin").val(minutes);

}

function round5(x) {
	x += 2.5;
	return (x % 5) >= 2.5 ? parseInt(x / 5) * 5 + 5 : parseInt(x / 5) * 5;
}

function loadAfterMove() {

	// Are all vessels loaded?
	if (loadAllVessels) {
		return false;
	}

	// If cluster zoom level
	if (embryo.mapPanel.map.zoom < vesselZoomLevel) {

		return true;

	} else if (embryo.mapPanel.map.zoom >= vesselZoomLevel
			&& lastZoomLevel < vesselZoomLevel) {

		return true;

	} else {

		// If zoom in
		if (embryo.mapPanel.map.zoom > lastZoomLevel) {

			return false;

		} else if (!loadFixedAreaSize || outOfLastLoadArea()) {

			return true;

		}

	}

	return false;

}

function outOfLastLoadArea() {

	// No vessel load
	if (lastLoadArea == undefined) {

		return true;

	}

	saveViewPort();

	// Longitude overflow
	if (filterQuery.topLon < lastLoadArea.top.lon
			|| filterQuery.botLon > lastLoadArea.bot.lon) {

		return true;

	}

	// Latitude overflow
	if (filterQuery.topLat > lastLoadArea.top.lat
			|| filterQuery.botLat < lastLoadArea.bot.lat) {

		return true;

	}

	return false;

}

function updateVesselsInView() {

	var vesselsInView = 0;

	// Iterate through vessels where value refers to each vessel.
	$.each(vessels, function(key, value) {

		// Ignore vessels outside viewport
		if (vesselInsideViewPort(value)) {
			vesselsInView++;
		}
	}

	);

	$("#vesselsView").html(vesselsInView);

}

/**
 * Sets up the panels, event listeners and selection controller.
 */
function closeEmptyPanels() {
	if (detailsReadyToClose) {
		$("#detailsContainer").slideUp({
			complete : function() {
				$("#detailsHeader").html("Vessel details");
				detailsOpen = false;
				$("#detailsPanel").removeClass("arrowUp");
				$("#detailsPanel").addClass("arrowDown");
				checkForPanelOverflow();
				$("#detailsContainer").html("");
				detailsReadyToClose = false;

			}
		});
	}
}

/**
 * Check if a panel overflows the window height. The height of the panels will
 * correct to fit.
 */
function checkForPanelOverflow() {
	var h = $(window).height();
	var lh = 370; // The height used by legends
	var vdh = 496; // The height of the vessel details
	var fih = 380; // The height of the filtering
	var sh = 92; // The height of the search panel

	if (embryo.searchPanel.searchOpen) {
		sh = 340;
	} else {
		sh = 27;
	}
	h -= sh;

	if (embryo.legendsPanel.open && detailsOpen && filteringOpen) {
		$("#detailsContainer").height(Math.min(vdh, (h - lh) / 2));
		$("#detailsContainer").css("overflow-y", "scroll");
		$("#filteringContainer").height(Math.min(fih, (h - lh) / 2));
		$("#filteringContainer").css("overflow-y", "scroll");
	} else if (embryo.legendsPanel.open && detailsOpen && !filteringOpen) {
		$("#detailsContainer").height(Math.min(vdh, h - lh));
		$("#detailsContainer").css("overflow-y", "scroll");
	} else if (embryo.legendsPanel.open && !detailsOpen && filteringOpen) {
		$("#filteringContainer").height(Math.min(fih, h - lh));
		$("#filteringContainer").css("overflow-y", "scroll");
	} else if (!embryo.legendsPanel.open && detailsOpen && filteringOpen) {
		$("#detailsContainer").height(Math.min(vdh, h / 2 - 70));
		$("#detailsContainer").css("overflow-y", "scroll");
		$("#filteringContainer").height(Math.min(fih, h / 2 - 70));
		$("#filteringContainer").css("overflow-y", "scroll");
	} else if (!embryo.legendsPanel.open && !detailsOpen && filteringOpen) {
		$("#filteringContainer").height(Math.min(fih, h - 60));
		$("#filteringContainer").css("overflow-y", "scroll");
	} else if (!embryo.legendsPanel.open && detailsOpen && !filteringOpen) {
		$("#detailsContainer").height(Math.min(vdh, h - 110));
		$("#detailsContainer").css("overflow-y", "scroll");
	} else {
		$("#filteringContainer").height('');
		$("#filteringContainer").css("overflow-y", "auto");
		$("#detailsContainer").height('');
		$("#detailsContainer").css("overflow-y", "auto");
	}
}

/**
 * Updates the vessel details panel to show informaiton of a specific vessel.
 * 
 * @param feature
 *            The feature of the vessel
 */

embryo.vesselDetailsPanel = {};
embryo.vesselDetailsPanel.init = function() {
	embryo.eventbus.registerHandler(embryo.eventbus.VesselSelectedEvent,
			embryo.vesselDetailsPanel.onVesselSelected);
};

embryo.vesselDetailsPanel.onVesselSelected = function(event) {
	var vesselId = event.vesselId;

	console.log("Fetching data for vessel with id: " + vesselId);

	// Get details from server
	$.getJSON(detailsUrl, {
		past_track : '1',
		id : vesselId
	}, embryo.vesselDetailsPanel.onAjaxResponse);
};

embryo.vesselDetailsPanel.onAjaxResponse = function(result) {
	// Load and draw tracks
	var tracks = result.pastTrack.points;
	drawPastTrack(tracks);

	// Load details
	// $("#detailsContainer").html("");
	$("#vesselDetails").hide();

	$("#vd_mmsi").html(result.mmsi);
	$("#vd_class").html(result.vesselClass);
	$("#vd_name").html(result.name);
	$("#vd_callsign").html(result.callsign);
	$("#vd_lat").html(result.lat);
	$("#vd_lon").html(result.lon);
	$("#vd_imo").html(result.imoNo);
	$("#vd_source").html(result.sourceType);
	$("#vd_type").html(result.vesselType);
	$("#vd_cargo").html(result.cargo);
	$("#vd_country").html(result.country);
	$("#vd_sog").html(result.sog + ' kn');
	$("#vd_cog").html(result.cog + ' &deg;');
	$("#vd_heading").html(result.heading + ' &deg;');
	$("#vd_draught").html(result.draught + ' m');
	$("#vd_rot").html(result.rot + ' &deg;/min');
	$("#vd_width").html(result.width + ' m');
	$("#vd_length").html(result.length + ' m');
	$("#vd_destination").html(result.destination);
	$("#vd_navStatus").html(result.navStatus);
	$("#vd_eta").html(result.eta);
	$("#vd_posAcc").html(result.posAcc);
	if (result.lastReceived != "undefined") {
		var lastParts = result.lastReceived.split(":");
		var lastReceivedStr = '';
		if (lastParts.length > 2) {
			lastReceivedStr = lastParts[0] + " h " + lastParts[1] + " m "
					+ lastParts[2] + " s";
		} else {
			lastReceivedStr = lastParts[0] + " m " + lastParts[1] + " s";
		}
		lastReceivedStr += " ago";
		$("#vd_lastReport").html(lastReceivedStr);
	} else {
		$("#vd_lastReport").html("undefined");
	}

	$("#vd_link").html(
			'<a href="http://www.marinetraffic.com/ais/shipdetails.aspx?mmsi='
					+ result.mmsi + '" target="_blank">Target info</a>');

	// Append details to vessel details panel
	// $("#detailsContainer").append($("#vesselDetails").html());

	$("#vesselDetails").show();

	detailsReadyToClose = false;

	// Open vessel details
	if (embryo.vesselDetailsPanel.onShown) {
		embryo.vesselDetailsPanel.onShown();
	}
};
embryo.vesselDetailsPanel.onShown = function(result) {
	var collapsable = $('#vesselDetails').closest('.collapse');
	if (!collapsable.hasClass('in')) {
		collapsable.collapse('show');
	}
};

embryo.statusPanel = {};
embryo.statusPanel.init = function(projection) {
	// Update mouse location when moved

	embryo.mapPanel.map.events.register("mousemove", embryo.mapPanel.map,
			function(e) {
				var position = embryo.mapPanel.map.events.getMousePosition(e);
				var pixel = new OpenLayers.Pixel(position.x, position.y);
				var lonLat = embryo.mapPanel.map.getLonLatFromPixel(pixel)
						.transform(embryo.mapPanel.map.getProjectionObject(), // from
						// Spherical
						// Mercator
						// Projection
						new OpenLayers.Projection(projection) // to WGS 1984
						);
				$("#location").html(
						lonLat.lat.toFixed(4) + ", " + lonLat.lon.toFixed(4));
			});
};

embryo.voyagePlanForm = {};
embryo.voyagePlanForm.copyEmptyRow = function(event) {
	var $row = $(event.target).closest('tr');

	// create new row by copy and modify before insertion into document
	var $newRow = $row.clone(true);
	var columnIndex = $row.find('input').index(event.target);
	$newRow.find('input').eq(columnIndex).val("");
	$row.after($newRow);

	// enableRow must be called after copying new row
	embryo.voyagePlanForm.enableRow($row);

	// if user typed into berth field, then give field focus and trigger
	// drowdown
	if ($(event.target).is('.typeahead-textfield')) {
		$(event.target).focus();
		$(event.target).prev('.tt-hint').trigger('focused');
	}
};
embryo.voyagePlanForm.registerHandlers = function($rows) {
	var formObject = this;

	$rows.each(function() {
		$(this).find('input.typeahead-textfield').bind(
				"typeahead:autocompleted typeahead:selected",
				formObject.onBerthSelected($(this)));

		$(this).find('input.lat input.lon').change(formObject.lonLanChanged);
		$(this).find('button').click(formObject.onDelete);
	});
};

embryo.voyagePlanForm.onBerthSelected = function($row) {
	return function(event, datum) {
		$row.find('input.lat').val(datum.latitude);
		$row.find('input.lon').val(datum.longitude);
	};
};

embryo.voyagePlanForm.onDelete = function(event) {
	event.preventDefault();
	event.stopPropagation();
	$rowToDelete = $(event.target).closest('tr');
	$rowToDelete.next().find("input:first").focus();
	$rowToDelete.remove();
};

embryo.voyagePlanForm.lonLanChanged = function(event) {
	var $lonLan = $(event.target);
	var $inputs = $lonLan.closest('tr').find('input');

	$lan = $inputs.eq(1);
	$lon = $inputs.eq(2);
	if (($lan.val() != null && $lan.val().length > 0)
			|| ($lon.val() != null && $lon.val().length > 0)) {
		$inputs.eq(0).prop('disabled', true);
	} else {
		$inputs.eq(0).removeProp('disabled');
	}
};
embryo.voyagePlanForm.deleteRowIfEmpty = function(event) {
	var $row = $(event.target).closest('tr');
	if (!$row.find('input[type="text"]').is(function() {
		// return true if value is present
		return this.value != null && this.value.length > 0;
	})) {
		// if no values are present then delete row
		$row.remove();
	}
};

embryo.voyagePlanForm.enableRow = function($row) {
	embryo.typeahead.create($row.find('input.typeahead-textfield')[0]);

	$row.find('button').show();
	$row.removeClass('emptyRow');
	$row.find('input, button').unbind('keydown',
			embryo.voyagePlanForm.copyEmptyRow);

	embryo.voyagePlanForm.registerHandlers($row);
};

embryo.voyagePlanForm.prepareRequest = function(containerSelector) {
	var $modalBody = $(containerSelector);
	var $rows = $modalBody.find('tbody tr');
	$modalBody.find('input[name="voyageCount"]').val($rows.length);

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
embryo.voyagePlanForm.init = function(containerSelector) {
	// TODO remove when DynamicListView is introduced for voyagePlanForm
	$(containerSelector).find('tr:last-child').addClass('emptyRow').find(
			'button').hide();

	$(containerSelector).find('.emptyRow input[type="text"]').keydown(
			embryo.voyagePlanForm.copyEmptyRow);

	$rows = $(containerSelector).find('.table tr:not(.emptyRow)');
	embryo.voyagePlanForm.registerHandlers($rows);

	// Initialize typeahead for all input fields not being empty row
	embryo.typeahead.create(containerSelector
			+ ' tr:not(.emptyRow) input.typeahead-textfield');

	$(containerSelector).find(containerSelector).closest(
			'button[type="submit"]')
			.click(embryo.voyagePlanForm.prepareRequest);

	// TODO if berth not typed in, but longitude and lattitude is typed in, then
	// make it impossible to type in berth (until longitude and lattitude are
	// again deleted)
};

embryo.typeahead = {};

embryo.typeahead.init = function(inputSelector, jsonUrl) {

	// Initialize existing typeahead fields
	embryo.typeahead.create(inputSelector);
};

// Initialize create function, which can be used both when initializing new
// rows and during this first initialization
embryo.typeahead.create = function(selector) {
	$(selector).each(function() {
		var jsonUrl = $(this).attr('data-json');

		// ttl value should be set higher (see doc)
		$(this).typeahead({
			name : 'berths',
			prefetch : {
				url : jsonUrl,
				ttl : 30000
			},
			remote : {
				url : jsonUrl
			}
		});
	});

};

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
