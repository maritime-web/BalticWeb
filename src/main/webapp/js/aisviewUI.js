// Panel variables
var detailsOpen = false;
var legendsOpen = false;
var filteringOpen = false;
var searchOpen = false;
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

function includePanels(){

	if (includeStatusPanel){
		$("#statusPanel").css('visibility', 'visible');
	} else {
		$("#statusPanel").remove();
	}

	if (includeLoadingPanel){
		$("#loadingPanel").css('visibility', 'hidden');
	} else {
		$("#loadingPanel").remove();
	}

	if (includeLegendsPanel){
		$("#legendsPanel").css('visibility', 'visible');
	} else {
		$("#legendsPanel").remove();
	}

	if (includeSearchPanel){
		$("#searchPanel").css('visibility', 'visible');
	} else {
		$("#searchPanel").remove();
	}

	if (includeFilteringPanel){
		$("#filteringPanel").css('visibility', 'visible');
	} else {
		$("#filteringPanel").remove();
	}

	if (includeDetailsPanel){
		$("#detailsPanel").css('visibility', 'visible');
	} else {
		$("#detailsPanel").remove();
	}

	if (includeFeedPanel){
		$("#feedPanel").css('visibility', 'visible');
	} else {
		$("#feedPanel").remove();
	}

	if (!includeAbnormalBehaviorPanel){
		$("#abnormalPanel").remove();
		$("#lightBoxEffect").remove();
	} else {
		$( "#eventFromDate" ).datepicker();
		$( "#eventToDate" ).datepicker();
	}

	if (!includeZoomPanel){
		$(".olControlZoom").remove();
	}

	if (loadFixedAreaSize){
		$("#vesselsView").remove();
		$("#vesselsViewText").remove();
	}

	if (!includeAdvancedTools){
		$("#toolbox").remove();
		$("#toolboxHeader").remove();
	}
	
}

function includeTools(){

	if (includeAbnormalBehaviorTool){

		var tool = new Tool("abnormalPanel", "abnormal");
		tools.push(tool);
		addToToolbox(tool);
	}

}

function addToToolbox(tool){

	$("#toolbox").append(toolToHTML(tool));

}

function openPanel(panelId){

	if (!abnormalOpen){
		var n = "#"+panelId+"";
		$("#"+panelId).css('visibility', 'visible');
		$("#lightBoxEffect").css('visibility', 'visible');
		$("#flash").css('visibility', 'visible');
		abnormalOpen = true;
	}

}


/**
 * Adds all the layers that will contain graphic.
 */
function addLayers(){

	// Get renderer
	var renderer = OpenLayers.Util.getParameters(window.location.href).renderer;
	renderer = (renderer) ? [renderer] : OpenLayers.Layer.Vector.prototype.renderers;
	// renderer = ["Canvas", "SVG", "VML"];

	// Create vector layer with a stylemap for vessels
	vesselLayer = new OpenLayers.Layer.Vector(
			"Vessels",
			{
				styleMap: new OpenLayers.StyleMap({
					"default": {
						externalGraphic: "${image}",
						graphicWidth: "${imageWidth}",
						graphicHeight: "${imageHeight}",
						graphicYOffset: "${imageYOffset}",
						graphicXOffset: "${imageXOffset}",
						rotation: "${angle}"
					},
					"select": {
						cursor: "crosshair",
						externalGraphic: "${image}"
					}
				}),
				renderers: renderer
			}
		);

	map.addLayer(vesselLayer);
	
	// Create vector layer with a stylemap for the selection image
	markerLayer = new OpenLayers.Layer.Vector(
			"Markers",
			{
				styleMap: new OpenLayers.StyleMap({
					"default": {
						externalGraphic: "${image}",
						graphicWidth: "${imageWidth}",
						graphicHeight: "${imageHeight}",
						graphicYOffset: "${imageYOffset}",
						graphicXOffset: "${imageXOffset}",
						rotation: "${angle}"
					},
					"select": {
						cursor: "crosshair",
						externalGraphic: "${image}"
					}
				}),
				renderers: renderer
			}
		);
	
	map.addLayer(markerLayer);

	// Create vector layer with a stylemap for the selection image
	selectionLayer = new OpenLayers.Layer.Vector(
			"Selection",
			{
				styleMap: new OpenLayers.StyleMap({
					"default": {
						externalGraphic: "${image}",
						graphicWidth: "${imageWidth}",
						graphicHeight: "${imageHeight}",
						graphicYOffset: "${imageYOffset}",
						graphicXOffset: "${imageXOffset}",
						rotation: "${angle}"
					},
					"select": {
						cursor: "crosshair",
						externalGraphic: "${image}"
					}
				}),
				renderers: renderer
			}
		);

	// Create vector layer for past tracks
	tracksLayer = new OpenLayers.Layer.Vector("trackLayer", {
        styleMap: new OpenLayers.StyleMap({'default':{
            strokeColor: pastTrackColor,
            strokeOpacity: pastTrackOpacity,
            strokeWidth: pastTrackWidth
        }})
    });

	// Create vector layer for time stamps
	timeStampsLayer = new OpenLayers.Layer.Vector("timeStampsLayer", {
        styleMap: new OpenLayers.StyleMap({'default':{
            label : "${timeStamp}",
			fontColor: timeStampColor,
			fontSize: timeStampFontSize,
			fontFamily: timeStampFontFamily,
			fontWeight: timeStampFontWeight,
			labelAlign: "${align}",
			labelXOffset: "${xOffset}",
			labelYOffset: "${yOffset}",
			labelOutlineColor: timeStamtOutlineColor,
			labelOutlineWidth: 5,
			labelOutline:1
        }})
    });

	// Create cluster layer
	clusterLayer = new OpenLayers.Layer.Vector( "Clusters", 
		{
		    styleMap: new OpenLayers.StyleMap({
		    'default':{
		        fillColor: "${fill}",
		        fillOpacity: clusterFillOpacity,
		        strokeColor: clusterStrokeColor,
		        strokeOpacity: clusterStrokeOpacity,
		        strokeWidth: clusterStrokeWidth
        	}
        })
    });
		
	map.addLayer(clusterLayer);

	// Create cluster text layer
	clusterTextLayer = new OpenLayers.Layer.Vector("Cluster text", 
		{
		    styleMap: new OpenLayers.StyleMap(
		    {
				'default':
				{
						label : "${count}",
						fontColor: clusterFontColor,
						fontSize: "${fontSize}",
						fontWeight: clusterFontWeight,
						fontFamily: clusterFontFamily,
						labelAlign: "c"
				}
			})
    	});

	map.addLayer(clusterTextLayer); 

	// Create layer for individual vessels in cluster 
	indieVesselLayer = new OpenLayers.Layer.Vector("Points", 
		{
		    styleMap: new OpenLayers.StyleMap({
		    "default": {
                pointRadius: indieVesselRadius,
                fillColor: indieVesselColor,
                strokeColor: indieVesselStrokeColor,
                strokeWidth: indieVesselStrokeWidth,
                graphicZIndex: 1
        	},
			"select": {
                pointRadius: indieVesselRadius * 3,
                fillColor: indieVesselColor,
                strokeColor: indieVesselStrokeColor,
                strokeWidth: indieVesselStrokeWidth,
                graphicZIndex: 1
        	}
        })
    });
    
	
    map.addLayer(indieVesselLayer); 
    map.addLayer(selectionLayer);
    map.addLayer(tracksLayer); 
    map.addControl(new OpenLayers.Control.DrawFeature(tracksLayer, OpenLayers.Handler.Path)); 
	map.addLayer(timeStampsLayer); 

	// Add OpenStreetMap Layer
	var osm = new OpenLayers.Layer.OSM(
		"OSM",
		"http://a.tile.openstreetmap.org/${z}/${x}/${y}.png",
		{
			'layers':'basic',
			'isBaseLayer': true
		} 
	);

	// Add OpenStreetMap Layer
	map.addLayer(osm);
	
	// Add KMS Layer
	//addKMSLayer();

}

/**
 * Sets up the panels, event listeners and selection controllers.
 */
function setupUI(){

	setupDatePickers();

	// Set zoom panel positon
	$(".olControlZoom").css('left', zoomPanelPositionLeft);
	$(".olControlZoom").css('top', zoomPanelPositionTop);

	// Set loading panel positon
	var x = $(document).width() / 2 - $("#loadingPanel").width() / 2;
	$("#loadingPanel").css('left', x);

	// Update mouse location when moved
	map.events.register("mousemove", map, function(e) { 
		var position = this.events.getMousePosition(e);
		pixel = new OpenLayers.Pixel(position.x, position.y);
		var lonLat = map.getLonLatFromPixel(pixel).transform(
			map.getProjectionObject(), // from Spherical Mercator Projection
			new OpenLayers.Projection("EPSG:4326") // to WGS 1984
		);
		$("#location").html(lonLat.lat.toFixed(4) + ", " + lonLat.lon.toFixed(4));
	});
	
	// Create functions for hovering a vessel
	var showName = function(e) {
		if (e.feature.attributes.vessel){
			$.getJSON(detailsUrl, {
				past_track: '1',
				id: e.feature.attributes.id
			}, function(result) {
				var pointVessel = new OpenLayers.Geometry.Point(e.feature.geometry.x, e.feature.geometry.y);
				var lonlatVessel = new OpenLayers.LonLat(pointVessel.x, pointVessel.y);
				var pixelVessel = map.getPixelFromLonLat(lonlatVessel);
				var pixelTopLeft = new OpenLayers.Pixel(0,0);
				var lonlatTopLeft = map.getLonLatFromPixel(pixelTopLeft)
				pixelTopLeft = map.getPixelFromLonLat(lonlatTopLeft);

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
    	$("#vesselNameBox").css('visibility', 'hidden');
    };

	// Create hover control - vessels
	hoverControlVessels = new OpenLayers.Control.SelectFeature(vesselLayer, 
		{	
			hover: true,
                highlightOnly: true,
                eventListeners: {
                   	featurehighlighted: showName,
                   	featureunhighlighted: hideName
                }
		}
	);

	// Create hover control - indie vessels
	hoverControlIndieVessels = new OpenLayers.Control.SelectFeature(indieVesselLayer, 
		{	
			hover: true,
                highlightOnly: true,
                eventListeners: {
                   	featurehighlighted: showName,
                   	featureunhighlighted: hideName
                }
		}
	);

	// Create select control - vessels
	selectControlVessels = new OpenLayers.Control.SelectFeature(vesselLayer, 
		{	
			clickout: true, 
			toggle: true,
			onSelect: function(feature) {
				if (selectedVessel && selectedVessel.id == feature.attributes.vessel.id){
					selectedFeature = null;
					selectedVessel = null;
					detailsReadyToClose = true;
					$("#vesselNameBox").css('visibility', 'hidden');
					redrawSelection();
					selectControlVessels.unselectAll();
				} else {
					selectedFeature = feature;
					selectedVessel = feature.attributes.vessel;
					updateVesselDetails(feature.attributes.id);
					$("#vesselNameBox").css('visibility', 'hidden');
					//selectControlVessels.select(feature);
					redrawSelection();
				}
			},
			onUnselect: function(feature) {
				selectedFeature = null;
				selectedVessel = null;
				detailsReadyToClose = true;
				tracksLayer.removeAllFeatures();
				timeStampsLayer.removeAllFeatures();
				redrawSelection();
				selectControlVessels.unselectAll();
			}
		}
	);

	// Create select control - indie vessels
	selectControlIndieVessels = new OpenLayers.Control.SelectFeature(indieVesselLayer, 
		{	
			clickout: true, 
			toggle: true,
			onSelect: function(feature) {
				if (selectedVessel && selectedVessel.id == feature.attributes.vessel.id){
					selectedFeature = null;
					selectedVessel = null;
					detailsReadyToClose = true;
					$("#vesselNameBox").css('visibility', 'hidden');
					redrawSelection();
					selectControlIndieVessels.unselectAll();
				} else {
					selectedFeature = feature;
					selectedVessel = feature.attributes.vessel;
					updateVesselDetails(feature.attributes.id);
					$("#vesselNameBox").css('visibility', 'hidden');
					redrawSelection();
				}
			},
			onUnselect: function(feature) {
				selectedFeature = null;
				selectedVessel = null;
				detailsReadyToClose = true;
				tracksLayer.removeAllFeatures();
				timeStampsLayer.removeAllFeatures();
				redrawSelection();
				selectControlIndieVessels.unselectAll();
			}
		}
	);
	
	// Add select controller to map and activate
	map.addControl(hoverControlVessels);
	map.addControl(hoverControlIndieVessels);
    map.addControl(selectControlVessels);
    map.addControl(selectControlIndieVessels);
	hoverControlVessels.activate();   
	hoverControlIndieVessels.activate();     
	selectControlVessels.activate();
	selectControlIndieVessels.activate();

	// Register listeners
	map.events.register("movestart", map, function() {
		
    });
	map.events.register("moveend", map, function() {
	
		saveViewCookie();
		$("#vesselNameBox").css('visibility', 'hidden');
		
		if (loadAfterMove()){
			setTimeToLoad(loadDelay);
	    	loadVesselsIfTime();
		} 
		//else {
			//updateVesselsInView();
		//}
		
		lastZoomLevel = map.zoom;
    });
	
	// Set click events on vessel details panel
	$("#detailsHeader").click(function() {
		if (detailsOpen){
			$("#detailsContainer").slideUp(
				{
					complete:function(){
						$("#detailsHeader").html("Vessel details");
						detailsOpen = false;
						$("#detailsPanel").removeClass("arrowUp");
						$("#detailsPanel").addClass("arrowDown");
						checkForPanelOverflow();
					}
				}
			);
		} else if($("#detailsContainer").html() != ""){
			$("#detailsHeader").append("<hr class='tight'>");
			$("#detailsContainer").slideDown(
				{
					complete:function(){
						detailsOpen = true;
						$("#detailsPanel").removeClass("arrowDown");
						$("#detailsPanel").addClass("arrowUp");
						checkForPanelOverflow();
					}
				}
			);
		}
	});
	
	// Set click events on legends panel
	$("#legendsHeader").click(function() {
		if (legendsOpen){
			$("#legendsContainer").slideUp(
				{
					complete:function(){
						legendsOpen = false;
						$("#legendsContainer").html("");
						$("#legendsHeader").html("Legends");
						$("#legendsPanel").removeClass("arrowUp");
						$("#legendsPanel").addClass("arrowDown");
						checkForPanelOverflow();
					}
				}
			);
		} else {
			$("#legendsContainer").css('display', 'none');
			$("#legendsHeader").html("Legends<br /><hr class='tight'>");
			$("#legendsContainer").html($("#legends").html());
			$("#legendsContainer").slideDown(
				{
					complete:function(){
						legendsOpen = true;
						$("#legendsPanel").removeClass("arrowDown");
						$("#legendsPanel").addClass("arrowUp");
						checkForPanelOverflow();
					}
				}
			);
		}
	});
	
	// Set click events on filtering panel
	$("#filteringHeader").click(function() {
		if (filteringOpen){
			$("#filteringContainer").slideUp(
				{
					complete:function(){
						filteringOpen = false;
						$("#filteringHeader").html("Filtering");
						$("#filteringPanel").removeClass("arrowUp");
						$("#filteringPanel").addClass("arrowDown");
						checkForPanelOverflow();
					}
				}
			);
		} else {
			$("#filteringContainer").css('display', 'none');
			$("#filteringContainer").html($("#filtering").html());
			$("#filteringHeader").html("Filtering<br /><hr class='tight'>");
			$("#filteringContainer").slideDown(
				{
					complete:function(){
						filteringOpen = true;
						$("#filteringPanel").removeClass("arrowDown");
						$("#filteringPanel").addClass("arrowUp");
						checkForPanelOverflow();
						parseFilterQuery();
					}
				}
			);
		}
	});

	// Set click events on search panel
	$("#searchHeader").click(function() {
		if (searchOpen){
			$("#searchContainer").slideUp(
				{
					complete:function(){
						searchOpen = false;
						$("#searchHeader").html("Search");
						$("#searchPanel").removeClass("arrowUp");
						$("#searchPanel").addClass("arrowDown");
						checkForPanelOverflow();
					}
				}
			);
		} else {
			$("#searchContainer").css('display', 'none');
			$("#searchContainer").html($("#search").html());
			$("#searchHeader").html("Search<br /><hr class='tight'>");
			$("#searchContainer").slideDown(
				{
					complete:function(){
						searchOpen = true;
						$("#searchPanel").removeClass("arrowDown");
						$("#searchPanel").addClass("arrowUp");
						checkForPanelOverflow();
						parseFilterQuery();
					}
				}
			);
		}
	});

	// Set click events on feed panel
	$("#feedHeader").click(function() {
		if (feedOpen){
			$("#feedContainer").slideUp(
				{
					complete:function(){
						feedOpen = false;
						$("#feedContainer").html("");
						$("#feedHeader").html("Abnormal behaviors");
						$("#feedPanel").removeClass("arrowUpWide");
						$("#feedPanel").addClass("arrowDown");
						$("#feedPanel").css('width', '220px');
					}
				}
			);
		} else {
			$("#feedContainer").css('display', 'none');
			$("#feedContainer").html($("#feedContent").html());
			$("#feedHeader").html("Abnormal behaviors - Last " + feedLifeTime + " minutes<br /><hr class='tight'>");
			$("#feedPanel").removeClass("arrowDown");
			$("#feedPanel").css('width', feedPanelExpandedWidth);
			$("#feedPanel").addClass("arrowUpWide");
			$("#feedContainer").slideDown(
				{
					complete:function(){
						feedOpen = true;
						loadBehaviors();
					}
				}
			);
		}
	});

	// Set click events on feed panel
	$("#exitAbnormal").click(function() {
		$("#abnormalPanel").css('visibility', 'hidden');
		$("#lightBoxEffect").css('visibility', 'hidden');
		$("#flash").css('visibility', 'hidden');
		abnormalOpen = false;
	});

	// Search when search field is changed
	setInterval("checkForSearch()", 200);
	
	// Close empty panels
	setInterval("closeEmptyPanels()", 1000);
	
}

function setupDatePickers(){

	$( "#eventFromDate" ).datepicker();
	$( "#eventToDate" ).datepicker();
	
	// Today
	var today = new Date();
	var dd = today.getDate();
	var mm = today.getMonth() + 1; //January is 0!
	var minutes = today.getMinutes();
	var hours = today.getHours();
	var yyyy = today.getFullYear();
	today = mm+'/'+dd+'/'+yyyy;
	
	// Yesterday
	var yesterday = new Date();
	yesterday.setDate(yesterday.getDate() - 1);
	dd = yesterday.getDate();
	mm = yesterday.getMonth() + 1; //January is 0!
	yyyy = yesterday.getFullYear();
	
	yesterday = mm+'/'+dd+'/'+yyyy;
	
	$( "#eventFromDate" ).datepicker( "setDate", yesterday );
	$( "#eventToDate" ).datepicker( "setDate", today );

	// Set time
	minutes = round5(minutes);

	if (minutes == 60){ 
		if (hours != 23){
			hours++;
			minutes = 0;
		}
	}
	
	$( "#eventFromHour" ).val(hours);
	$( "#eventToHour" ).val(hours);
	$( "#eventFromMin" ).val(minutes);
	$( "#eventToMin" ).val(minutes);

}

function round5(x){
	x += 2.5;
    return (x % 5) >= 2.5 ? parseInt(x / 5) * 5 + 5 : parseInt(x / 5) * 5;
}

function loadAfterMove(){

	// Are all vessels loaded?
	if (loadAllVessels){
		return false;
	}

	// If cluster zoom level
	if (map.zoom < vesselZoomLevel){
	
		return true;
		
	} else if (map.zoom >= vesselZoomLevel && lastZoomLevel < vesselZoomLevel){

		return true;

	} else {

		// If zoom in		
		if (map.zoom > lastZoomLevel){

			return false;

		} else if (!loadFixedAreaSize || outOfLastLoadArea()){

			return true;

		}
		
	}

	return false;

}

function outOfLastLoadArea(){

	// No vessel load
	if (lastLoadArea == undefined){

		return true;

	}

	saveViewPort();

	// Longitude overflow
	if (filterQuery.topLon < lastLoadArea.top.lon
		|| filterQuery.botLon > lastLoadArea.bot.lon){

		return true;
		
	}

	// Latitude overflow
	if (filterQuery.topLat > lastLoadArea.top.lat
		|| filterQuery.botLat < lastLoadArea.bot.lat){

		return true;
		
	}

	return false;

}

var lastSearch = "";

function checkForSearch(){
	var val = $("#searchField").val();
	if (val != lastSearch){
		lastSearch = val;
		search(val);
	}
}

function updateVesselsInView(){

	var vesselsInView = 0;

	// Iterate through vessels where value refers to each vessel.
	$.each(vessels, function(key, value) { 

			// Ignore vessels outside viewport
			if (vesselInsideViewPort(value)){
				vesselsInView++;
			}

		}

	);

	$("#vesselsView").html(vesselsInView);
	
}

/**
 * Sets up the panels, event listeners and selection controller.
 */
function closeEmptyPanels(){
	if (detailsReadyToClose){
		$("#detailsContainer").slideUp(
			{
				complete:function(){
					$("#detailsHeader").html("Vessel details");
					detailsOpen = false;
					$("#detailsPanel").removeClass("arrowUp");
					$("#detailsPanel").addClass("arrowDown");
					checkForPanelOverflow();
					$("#detailsContainer").html("");
					detailsReadyToClose = false;

				}
			}
		);
	}
}

/**
 * Check if a panel overflows the window height.
 * The height of the panels will correct to fit.
 */
function checkForPanelOverflow(){
	var h = $(window).height();
	var lh = 370;				// The height used by legends
	var vdh = 496;				// The height of the vessel details
	var fih = 380;				// The height of the filtering
	var sh = 92;				// The height of the search panel

	if (searchOpen){
		sh = 340;
	} else {
		sh = 27;
	}
	h -= sh;
	
	if (legendsOpen && detailsOpen && filteringOpen){
		$("#detailsContainer").height(Math.min(vdh, (h-lh)/2));
		$("#detailsContainer").css("overflow-y", "scroll");
		$("#filteringContainer").height(Math.min(fih, (h-lh)/2));
		$("#filteringContainer").css("overflow-y", "scroll");
	} else if (legendsOpen && detailsOpen && !filteringOpen){
		$("#detailsContainer").height(Math.min(vdh, h-lh));
		$("#detailsContainer").css("overflow-y", "scroll");
	} else if (legendsOpen && !detailsOpen && filteringOpen){
		$("#filteringContainer").height(Math.min(fih, h-lh));
		$("#filteringContainer").css("overflow-y", "scroll");
	} else if (!legendsOpen && detailsOpen && filteringOpen){
		$("#detailsContainer").height(Math.min(vdh, h/2 - 70));
		$("#detailsContainer").css("overflow-y", "scroll");
		$("#filteringContainer").height(Math.min(fih, h/2 - 70));
		$("#filteringContainer").css("overflow-y", "scroll");
	} else if (!legendsOpen && !detailsOpen && filteringOpen){
		$("#filteringContainer").height(Math.min(fih, h - 60));
		$("#filteringContainer").css("overflow-y", "scroll");
	} else if (!legendsOpen && detailsOpen && !filteringOpen){
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
 * Updates the vessel details panel to show informaiton 
 * of a specific vessel.
 *
 * @param feature
 *            The feature of the vessel
 */
function updateVesselDetails(vesselId){
	// Get details from server
	$.getJSON(detailsUrl, {
			past_track: '1',
			id: vesselId
		}, function(result) {			
			// Load and draw tracks
			var tracks = result.pastTrack.points;
			drawPastTrack(tracks);			

			// Load details
			$("#detailsContainer").html("");
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
			if (result.lastReceived != "undefined"){
				var lastParts = result.lastReceived.split(":");
				var lastReceivedStr = '';
				if (lastParts.length > 2) {
					lastReceivedStr = lastParts[0] + " h " + lastParts[1] + " m " + lastParts[2] + " s"; 
				} else {
					lastReceivedStr = lastParts[0] + " m " + lastParts[1] + " s";
				}
				lastReceivedStr += " ago";				
				$("#vd_lastReport").html(lastReceivedStr);
			} else {
				$("#vd_lastReport").html("undefined");
			}
			
			$("#vd_link").html('<a href="http://www.marinetraffic.com/ais/shipdetails.aspx?mmsi=' + result.mmsi + '" target="_blank">Target info</a>');
			
			// Append details to vessel details panel
			$("#detailsContainer").append($("#vesselDetails").html());
			detailsReadyToClose = false;

			// Open vessel detals
			if (!detailsOpen){
				$("#detailsContainer").css('display', 'none');
				$("#detailsHeader").html("Vessel details<br /><hr class='tight'>");
				$("#detailsContainer").slideDown(
					{
						complete:function(){
							detailsOpen = true;
							$("#detailsPanel").removeClass("arrowDown");
							$("#detailsPanel").addClass("arrowUp");
							checkForPanelOverflow();
							$("#detailsPanel").css('background-image', 'url("../img/arrowUp.png"');
						}
					}
				);
			}
		});
}

