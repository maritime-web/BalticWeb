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

embryo.exitAbnormal = {};
embryo.exitAbnormal.init = function(){
	$("#exitAbnormal").click(function() {
		$("#abnormalPanel").css('visibility', 'hidden');
		$("#lightBoxEffect").css('visibility', 'hidden');
		$("#flash").css('visibility', 'hidden');
		abnormalOpen = false;
	});
};

embryo.feed = {};
embryo.feed.init = function() {
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
};

embryo.vessel.details = {};
embryo.vessel.details.init = function(){
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
};

embryo.filtering = {};
embryo.filtering.init = function() {
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
};

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
 * Sets up the panels, event listeners.
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
