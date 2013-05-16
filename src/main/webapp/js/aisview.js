// Global variables
var map;
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
var selectedVessel;
var selectedVesselInView = false;
var selectedFeature;
var markedVessel;
var searchResults = [];
var lastRequestId = 0;
var lastZoomLevel;
var lastLoadArea;

/**
 * Sets up the map by adding layers and overwriting 
 * the 'map' element in the HTML index file.
 * Vessels are loaded using JSON and drawn on the map.
 */
function setupMap(){

	includePanels();

	includeTools();
	
	// Load cookies
	loadView();

	// Create the map and overwrite cotent of the map element
	
	map = new OpenLayers.Map({
        div: "map",
        projection: "EPSG:900913",
        fractionalZoom: true
    });
    
	addLayers();

	var center = transformPosition(initialLon, initialLat);
	map.setCenter (center, initialZoom);
	lastZoomLevel = map.zoom;
	
	// Load new vessels with an interval
	setInterval("loadVesselsIfTime()", loadCheckingFrequence);

	if (includeEventFeed){
		setInterval("loadBehaviors()", loadBehaviorsFrequence);
		loadBehaviors();
	}
	
	setupUI();
	
	parseFilterQuery();

	// Load vessels
	loadVessels();
	
}

/**
 * Loads vessels if time since last update is higher than loadFrequence. 
 */
function loadVesselsIfTime(){

	var timeSinceLastLoad = new Date().getTime() - timeOfLastLoad;
	
	if (timeOfLastLoad == 0 || timeSinceLastLoad >= loadFrequence){
		loadVessels();
	}	
	
}

/**
 * Loads vessels in the specified amount of time.
 */
function setTimeToLoad(ms){

	var timeSinceLastLoad = new Date().getTime() - timeOfLastLoad;

	timeOfLastLoad -= (loadFrequence - timeSinceLastLoad);
	timeOfLastLoad += ms;

}


/**
 * Loads the vessels using JSON.
 * If the zoom level is higher than or equal to the 
 * minimum zoom level it adds each vessel as a vessel 
 * instance to the list of vessels.
 * The vessels will be drawn when the JSON is received.
 * If the zoom level is lower than the minumum zoom level,
 * it draws the vesselclusters instead.
 */
function loadVessels(){

	// Reset list of vessels
	vessels = [];
	clusters = [];

	if (map.zoom >= vesselZoomLevel || loadAllVessels){

		// Show Loading panel
		$("#loadingPanel").css('visibility', 'visible');

		loadVesselList();

		clusterLayer.setVisibility(false);
		clusterTextLayer.setVisibility(false);
		indieVesselLayer.setVisibility(false);

		vesselLayer.setVisibility(true);

		selectControlVessels.activate();
		selectControlIndieVessels.deactivate();

	} else {

		if (includeClustering){

			// Show Loading panel
			$("#loadingPanel").css('visibility', 'visible');
			
			loadVesselClusters();

			clusterLayer.setVisibility(true);
			clusterTextLayer.setVisibility(true);
			indieVesselLayer.setVisibility(true);

		}

		vesselLayer.setVisibility(false);
		selectControlVessels.deactivate();
		selectControlIndieVessels.activate();

	}

	// Set time of load
	timeOfLastLoad = new Date().getTime();

}

/**
 * Loads and draws all vessels in the view.
 */
function loadVesselList(){

	saveViewPort();

	// Generate data
	var data = filterQuery;
	lastRequestId++;
	data.requestId = lastRequestId;
	if (!loadViewportOnly || loadAllVessels){
		delete data.topLon; 
		delete data.topLat; 
		delete data.botLon; 
		delete data.botLat;
	}
	if (loadFixedAreaSize && !loadAllVessels){
		lastLoadArea = getSpecificLoadArea();
		data.topLon = lastLoadArea.top.lon; 
		data.topLat = lastLoadArea.top.lat; 
		data.botLon = lastLoadArea.bot.lon; 
		data.botLat = lastLoadArea.bot.lat;
	}

	$.getJSON(listUrl, data, 
		function (result) {

			if (result.requestId != lastRequestId) return;
			
			// Update vessel counter
			$("#vesselsTotal").html(result.vesselsInWorld);

			// Load new vessels
			var JSONVessels = result.vesselList.vessels;
		
			for (vesselId in JSONVessels) {
				// Create vessel based on JSON data
				var vesselJSON = JSONVessels[vesselId];
				var vessel = new Vessel(vesselId, vesselJSON, 1);
			
				if (selectedVessel && vesselId == selectedVessel.id && !selectSearchedVessel) {
					// Update selected vessel
					selectedVessel = vessel;
				} else if (selectSearchedVessel && searchedVessel && vesselId == searchedVessel.id) {
					// Update selected vessel
					selectedVessel = vessel;
					vessels.push(vessel);
				}

				vessels.push(vessel);
			
			}
		
			// Draw vessels
			drawVessels();

			selectSearchedVessel = false;

			// Hide Loading panel
			$("#loadingPanel").css('visibility', 'hidden');
		}
	);
}

/**
 * Loads and draws the vessel clusters.
 */
function loadVesselClusters(){

	saveViewPort();

	// Find cluster size
	var size = 10;
	for (i in clusterSizes){
		if (map.zoom >= clusterSizes[i].zoom){
			size = clusterSizes[i].size;
			//break;
		}
	}

	// Generate data
	var data = filterQuery;
	data.clusterLimit = clusterLimit;
	data.clusterSize = size;
	lastRequestId++;
	data.requestId = lastRequestId;
	if (!loadViewportOnly){
		delete data.topLon; 
		delete data.topLat; 
		delete data.botLon; 
		delete data.botLat;
	}
	
	$.getJSON(clusterUrl, data, 
		function (result) {

			if (result.requestId != lastRequestId) return;
			
			// Update vessel counter
			$("#vesselsTotal").html(result.vesselsInWorld);

			// Load vessel clusters
			var JSONClusters = result.clusters;

			for (clusterId in JSONClusters) {
			
				// Create vessel based on JSON data
				var JSONCluster = JSONClusters[clusterId];
				var from = transformPosition(JSONCluster.from.longitude, JSONCluster.from.latitude);
				var to = transformPosition(JSONCluster.to.longitude, JSONCluster.to.latitude);
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

		}
	);
	
}

/**
 * Draws the vessel clusters.
 */
function drawClusters(){

	// Reset
	clusterTextLayer.removeAllFeatures();
	clusterLayer.removeAllFeatures();
	var selectionFeatures = [];
	selectedVesselInView = false;
	var vesselsInView = 0;

	// Remove old features except selected feature
	var arr = indieVesselLayer.features.slice();
	var idx = arr.indexOf(selectedFeature);
	if(idx!=-1) arr.splice(idx, 1);
	indieVesselLayer.destroyFeatures(arr);
	indieVesselLayer.renderer.clear();

	// Draw
	for(id in clusters){
		if (clusters[id].count > clusterLimit){

			drawCluster(clusters[id]);

			vesselsInView += clusters[id].count;
			
		} else {

			drawIndieVessels(clusters[id]);

			vesselsInView += clusters[id].count;
			
		}

    }

    // Set vessel in focus if selected
	vesselInFocus(selectedVessel, selectedFeature);

    // Draw selected vessel
    if (selectedVessel){
		drawIndieVessel(selectedVessel);
    }

	// Draw selection
	addSelectionFeature();
	selectionLayer.redraw();
	indieVesselLayer.redraw();
	drawPastTrack(null);

    // Update number of vessels
	$("#vesselsView").html(""+vesselsInView);
    
}

/**
 * Draws a vessel cluster.
 */
function drawCluster(cluster){

	// Create polygon
	var points = [
		new OpenLayers.Geometry.Point(cluster.from.lon, cluster.from.lat),
		new OpenLayers.Geometry.Point(cluster.to.lon, cluster.from.lat),
		new OpenLayers.Geometry.Point(cluster.to.lon, cluster.to.lat),
		new OpenLayers.Geometry.Point(cluster.from.lon, cluster.to.lat)
	];
	var ring = new OpenLayers.Geometry.LinearRing(points);
	var polygon = new OpenLayers.Geometry.Polygon([ring]);

	// Create feature
	var feature = new OpenLayers.Feature.Vector(polygon,
	 	{	
			from: cluster.from,
			to: cluster.to,
			fill: findClusterColor(cluster)
		}
	);
	clusterLayer.addFeatures([feature]);

	// Draw text
	var textLon = cluster.from.lon + (cluster.to.lon - cluster.from.lon) / 2;
	var textLat = cluster.from.lat + (cluster.to.lat - cluster.from.lat) / 2;
	var textPos = new OpenLayers.Geometry.Point(textLon, textLat);
	var textFeature = new OpenLayers.Feature.Vector(textPos,
		{
			count: cluster.count,
			fontSize: clusterFontSize
		}
	);
	clusterTextLayer.addFeatures([textFeature]);

}

/**
 * Draws the individual vessels in a cluster.
 */
function drawIndieVessels(cluster){

	if (showIndividualVessels){

		$.each( clusters[id].vessels, function(i, n){

			drawIndieVessel(n);
			
		});

	}

}

/**
 * Draws an individual vessel.
 */
function drawIndieVessel(vessel){

	// Add feature
	var loc = transformPosition(vessel.lon, vessel.lat);
	var geom = new OpenLayers.Geometry.Point(loc.lon, loc.lat);
	var attr = {	
	 		id: vessel.id,
			type: "indie",
			vessel: vessel,
			angle: vessel.degree
		};

	if (selectedVessel && vessel.id == selectedVessel.id && selectedFeature.attributes.type == "indie"){

		selectedFeature.attributes = attr;
		selectedFeature.geometry = geom;

	} else {
	
		feature = new OpenLayers.Feature.Vector(geom, attr);
		indieVesselLayer.addFeatures([feature]);

	}

}

/**
 * Draws all known vessels using vector points styled to show images.
 * Vessels are drawn based on their color, angle and whether they are
 * moored on not.
 */
function drawVessels(){

	var vesselFeatures = [];
	var selectionFeatures = [];
	selectedVesselInView = false;

	// Update number of vessels
	$("#vesselsView").html(""+vessels.length);

	// Iterate through vessels where value refers to each vessel.
	$.each(vessels, function(key, value) { 

		var attr = {	
				id: value.id,
				angle: value.degree - 90, 
				opacity:1, 
				image:"img/" + value.image,
				imageWidth: value.imageWidth,
				imageHeight: value.imageHeight,
				imageYOffset: value.imageYOffset,
				imageXOffset: value.imageXOffset,
				type: "vessel",
				vessel: value
			}

		var geom = new OpenLayers.Geometry.Point( value.lon , value.lat ).transform(
					new OpenLayers.Projection("EPSG:4326"), // transform from WGS 1984
					map.getProjectionObject() // to Spherical Mercator Projection
				);

		if (selectedVessel && selectedFeature && value.id == selectedVessel.id && selectedFeature.attributes.type == "vessel"){

			selectedFeature.attributes = attr;
			selectedFeature.geometry = geom;

		} else {
		
			// Use styled vector points
			var feature = new OpenLayers.Feature.Vector(geom, attr);

			vesselFeatures.push(feature);

			// Select searched vessel?
			if (selectSearchedVessel && searchedVessel && searchedVessel.id == value.id){
				selectedFeature = feature;
			}
			
			// Select selected vessel?
			if (selectedVessel && selectedVessel.id == value.id && !selectedFeature){
				selectedFeature = feature;
			}
			
			// Update marked vessel
			if (markedVessel && markedVessel.id == value.id){
				markedVessel = value;
			}
			
		}
		
	});
	
	// Draw marker
	if (markedVessel){
		redrawMarker();
	}
	
	// Set vessel in focus if selected
	vesselInFocus(selectedVessel, selectedFeature);

	// Remove old features except selected feature
	var arr = vesselLayer.features.slice();
	var idx = arr.indexOf(selectedFeature);
	if(idx!=-1) arr.splice(idx, 1);
	vesselLayer.addFeatures(vesselFeatures);
	vesselLayer.destroyFeatures(arr);

	// Redraw
	addSelectionFeature();
	vesselLayer.renderer.clear();
	vesselLayer.redraw();
	selectionLayer.redraw();
	drawPastTrack(null);

}

/**
 * Sets a vessel in focus if it is selected.
 */
function vesselInFocus(vessel, feature){

	if (selectedVessel && feature && vessel.id == selectedVessel.id){

		selectedVesselInView = true;

		// Update selected vessel
		selectedVessel = vessel;
		selectedFeature = feature;

		// Update vessel details
		updateVesselDetails(feature.attributes.id);

	}

}

/**
 * Adds the selection feature if a feature is selected.
 */
function addSelectionFeature(){

	// Add selection
	if (selectedFeature && selectedVesselInView){
		var selectionFeature = new OpenLayers.Feature.Vector(
			new OpenLayers.Geometry.Point( selectedFeature.geometry.x , selectedFeature.geometry.y ),
		 	{	
				id: -1,
				angle: selectedFeature.attributes.angle - 90, 
				opacity:1, 
				image:"img/selection.png",
				imageWidth: 32,
				imageHeight: 32,
				imageYOffset: -16,
				imageXOffset: -16,
				type: "selection"
			}
		);
		
		selectionLayer.removeAllFeatures();
		selectionLayer.addFeatures([selectionFeature]);

	}

}

/**
 * Redraws all features in vessel layer and selection layer.
 * Features are vessels.
 */
function redrawSelection(){
	var selectionFeature;
	var selectionFeatures = [];
	drawPastTrack(null);

	// Set search result in focus
	if (selectedFeature){
		selectionFeature = new OpenLayers.Feature.Vector(
			new OpenLayers.Geometry.Point( selectedFeature.geometry.x , selectedFeature.geometry.y ),
		 	{	
				id: -1,
				angle: selectedFeature.attributes.angle - 90, 
				opacity:1, 
				image:"img/selection.png",
				imageWidth: 32,
				imageHeight: 32,
				imageYOffset: -16,
				imageXOffset: -16,
				type: "selection"
			}
		);
		
		selectionFeatures.push(selectionFeature);
		selectedVesselInView = true;
		updateVesselDetails(selectedFeature.attributes.id);

	}

	selectionLayer.removeAllFeatures();
	selectionLayer.addFeatures(selectionFeatures);
	selectionLayer.redraw();
}

function redrawMarker(){
	
	var loc = transformPosition(markedVessel.lon, markedVessel.lat);
	var geom = new OpenLayers.Geometry.Point(loc.lon, loc.lat);
	
	var markerFeature = new OpenLayers.Feature.Vector(
			new OpenLayers.Geometry.Point( geom.x , geom.y ),
		 	{	
				id: -1,
				angle: 0, 
				opacity:1, 
				image:"img/green_marker.png",
				imageWidth: 32,
				imageHeight: 32,
				imageYOffset: -16,
				imageXOffset: -16,
				type: "marker"
			}
		);
	
	markerLayer.removeAllFeatures();
	markerLayer.addFeatures(markerFeature);
	markerLayer.redraw();
	
}

/**
 * Draws the past track.
 * If tracks are null, it will simply remove all tracks and draw nothing.
 *
 * @param tracks 
 *		Array of tracks
 */
function drawPastTrack(tracks) {

	// Remove old tracks
	tracksLayer.removeAllFeatures();
	timeStampsLayer.removeAllFeatures();

	// Get time stamp distance
	var CL = false;
	var tracksBetweenTimeStamps;
	if (map.zoom >= vesselZoomLevel){
		tracksBetweenTimeStamps	= tracksBetweenTimeStampsVL;
	} else {
		tracksBetweenTimeStamps	= tracksBetweenTimeStampsCL;	
		CL = true;
	}

	// Draw tracks
	if (selectedVesselInView && tracks && includePastTracks){
		var lastLon;
		var lastLat;
		var firstPoint = true;
		var untilTimeStamp = 0;

		for(track in tracks) {
			var currentTrack = tracks[track];
			if (!firstPoint){
				// Insert line
				var points = new Array(
					new OpenLayers.Geometry.Point(lastLon, lastLat).transform(
							new OpenLayers.Projection("EPSG:4326"), 
							map.getProjectionObject()),
					new OpenLayers.Geometry.Point(currentTrack.lon, currentTrack.lat).transform(
							new OpenLayers.Projection("EPSG:4326"), 
							map.getProjectionObject())
				);
			
				var line = new OpenLayers.Geometry.LineString(points);
				var lineFeature = new OpenLayers.Feature.Vector(line);
				tracksLayer.addFeatures([lineFeature]);

				// Insert timeStamp?
				if (untilTimeStamp == 0 
						&& parseInt(track) + tracksBetweenTimeStamps < tracks.length
						&& includeTimeStamps
						&& (includeTimeStampsOnCL || !CL)){

					var timeStampPos = points[0];
					var timeStampFeature = new OpenLayers.Feature.Vector(timeStampPos);
					
					// Remove date from time
					var time = (new Date(currentTrack.time)).toTimeString();
					
					// Change to 24h clock
					time = to24hClock(time);
					
					timeStampFeature.attributes = {timeStamp: time};
					timeStampsLayer.addFeatures([timeStampFeature]);

					untilTimeStamp = tracksBetweenTimeStamps;

				} else {
					untilTimeStamp --;
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

/**
 * Finds the color of a cluster based on either density or count.
 */
function findClusterColor(cluster){

	if (baseColorsOn == "density"){ 
	
		for (var i = clusterColors.length - 1; i >= 0; i--){
			if (cluster.density >= clusterColors[i].densityLimit){
				return clusterColors[i].color;
			}
		}
		
	} else if (baseColorsOn == "count"){ 

		for (var i = clusterColors.length - 1; i >= 0; i--){
			if (cluster.count >= clusterColors[i].countLimit){
				return clusterColors[i].color;
			}
		}
		
	}
	
	return "#000000";
}

/**

 *	Saves the viewport to the filter query object.
 */
function saveViewPort(){

	// Get points from viewport
	var viewportWidth = $(map.getViewport()).width();
	var viewportHeight = $(map.getViewport()).height();
	topLeftPixel = new OpenLayers.Pixel(viewportWidth*0.00, viewportHeight*0.00);
	botRightPixel = new OpenLayers.Pixel(viewportWidth*1.00, viewportHeight*1.00);

	var top = map.getLonLatFromPixel(topLeftPixel).transform(
			map.getProjectionObject(), // from Spherical Mercator Projection
			new OpenLayers.Projection("EPSG:4326") // to WGS 1984
		);
	var bot = map.getLonLatFromPixel(botRightPixel).transform(
			map.getProjectionObject(), // from Spherical Mercator Projection
			new OpenLayers.Projection("EPSG:4326") // to WGS 1984
		);

	filterQuery.topLon = top.lon; 
	filterQuery.topLat = top.lat; 
	filterQuery.botLon = bot.lon; 
	filterQuery.botLat = bot.lat;
	
}

function getSpecificLoadArea(){

	var loadArea = {};
	
	// Get center from viewport
	var viewportWidth = $(map.getViewport()).width();
	var viewportHeight = $(map.getViewport()).height();
	var centerPixel = new OpenLayers.Pixel(viewportWidth*0.50, viewportHeight*0.50);
	var center = map.getLonLatFromPixel(centerPixel).transform(
			map.getProjectionObject(), // from Spherical Mercator Projection
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
	if (topLeft.lon > 180){
		topLeft.lon = topLeft.lon - 180*2;
	} else if (topLeft.lon < -180){
		topLeft.lon = topLeft.lon + 180*2;
	}
	
	if (botRight.lon > 180){
		botRight.lon = botRight.lon - 180*2;
	} else if (topLeft.lon < -180){
		botRight.lon = botRight.lon + 180*2;
	}

	if (topLeft.lat > 90){
		topLeft.lat = 90;
	} else if (topLeft.lat < -90){
		topLeft.lat = -90;
	}
	
	if (botRight.lat > 90){
		botRight.lat = 90;
	} else if (topLeft.lat < -90){
		botRight.lat = -90;
	}
	
	loadArea.top = topLeft;
	loadArea.bot = botRight;
	
	return loadArea;
	
}

/**
 * Moves the focus to a vessel. 
 * The zoom level is specified in the settings.js file.
 */
function goToVessel(vessel){

	var center = new OpenLayers.LonLat(vessel.lon, vessel.lat).transform(
			new OpenLayers.Projection("EPSG:4326"), 
			map.getProjectionObject()
		);

	selectedVessel = vessel;
		
	map.setCenter (center, focusZoom);

	setTimeToLoad(400);

}

/**
 * Moves the location to a vessel. 
 * The zoom level is specified in the settings.js file.
 */
function goToVesselLocation(vessel){

	var center = new OpenLayers.LonLat(vessel.lon, vessel.lat).transform(
			new OpenLayers.Projection("EPSG:4326"), 
			map.getProjectionObject()
		);
		
	map.setCenter (center, focusZoom);

}

/**
 * Moves the focus to a vessel in the search results. 
 * The zoom level is specified in the settings.js file.
 */
function goToSearchedVessel(key){

	var vessel = searchResults[key];

	var center = new OpenLayers.LonLat(vessel.lon, vessel.lat).transform(
			new OpenLayers.Projection("EPSG:4326"), 
			map.getProjectionObject()
		);

	searchedVessel = vessel;
	selectSearchedVessel = true;
		
	map.setCenter (center, focusZoom);

	setTimeToLoad(400);

}

/**
 * Moves the focus to a specific location and zoom level. 
 * The zoom level is specified in the settings.js file.
 */
function goToLocation(longitude, latitude){

	// Hide tools
	$("#abnormalPanel").css('visibility', 'hidden');
	$("#lightBoxEffect").css('visibility', 'hidden');
	$("#flash").css('visibility', 'hidden');
	abnormalOpen = false;

	var center = new OpenLayers.LonLat(longitude, latitude).transform(
			new OpenLayers.Projection("EPSG:4326"), 
			map.getProjectionObject()
		);
		
	map.setCenter (center, focusZoom);

}

/**
 * Converts a string in 12hr format to 24h format.
 * 		
 * @param time
 * 		a string in the following format: "12:36:26 PM"
 */
function to24hClock(time){
	
	// Parse data
	var hour = parseInt(time.split(":")[0]);
	var min = parseInt(time.split(":")[1]);
	var sec = parseInt(time.split(":")[2]);
	var ampm = time.split(" ")[1];
	
	// AM?
	if (ampm == "PM"){
		hour += 12;
		if (hour == 24){
			hour = 12;
		}
	} else if(hour == 12){
		hour = 0;
	}
	
	// Insert zeroes
	hour = hour += "";
	min = min += "";
	sec = sec += "";
	if (hour.length == 1){
		hour = "0" + hour;
	}
	if (min.length == 1){
		min = "0" + min;
	}
	if (sec.length == 1){
		sec = "0" + sec;
	}
	
	return hour + ":" + min + ":" + sec;
	
}

/**
 * Searches for the vessel described in the search field.
 */
function search(){
	// Read search field
	var arg = $("#searchField").val();
	$("#searchResultsTop").empty();
	$("#searchResultsContainer").empty();
	
	if (arg.length > 0){

		// Show loader
		$("#searchLoad").css('visibility', 'visible');

		// Load search results
		$.getJSON(searchUrl, { argument: arg }, function (result) {
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
				if (searchResults.length <= searchResultsLimit && searchResults.length != 0){
					if (searchResults.length == 1){
						s = "";
					}
					//selectedVessel = searchResults[0];
					
					$("#searchResultsTop").html("<div class='information'>Search results: </div>");
					$.each(searchResults, function(key, value) { 

							searchResults.push(value);

							$("#searchResultsContainer").append(searchResultToHTML(value, key));
							
						}
					);
					
				}

				$("#searchMatch").html(result.vesselCount + " vessel" + s + " match.");

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


/**
 * Transforms a position to a position that can be used 
 * by OpenLayers. The transformation uses 
 * OpenLayers.Projection("EPSG:4326").
 * 
 * @param lon
 *            The longitude of the position to transform
 * @param lat
 *            The latitude of the position to transform
 * @returns The transformed position as a OpenLayers.LonLat 
 * instance.
 */
function transformPosition(lon, lat){
	return new OpenLayers.LonLat( lon , lat )
		.transform(
			new OpenLayers.Projection("EPSG:4326"), // transform from WGS 1984
			map.getProjectionObject() // to Spherical Mercator Projection
		);
}


/**
 * Resets the filterQuery object and adds a filter preset.
 * 
 * @param presetSelect
	 	A string in the following form:
 * 		"key = value"
 *
 */
function useFilterPreset(presetSelect) {
	
	resetFilterQuery();

	filter = presetSelect.options[presetSelect.selectedIndex].value;
	if (filter != ""){
		var expr = 'filterQuery.' + filter.split("=")[0] + ' = "' + filter.split("=")[1] + '"';
		eval(expr);
	}
	
	parseFilterQuery();			
	filterChanged();
}


/**
 * Resets the filterQuery.
 */
function resetFilterQuery(){
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
 * Sets the values in the filter panel equal to 
 * the values of the filterQuery object.
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
 * Applys the values of the filter panel to the 
 * filterQuery object.
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
	var center = map.getCenter();
	setCookie("dma-ais-zoom", map.zoom, 30);
	var lonlat = new OpenLayers.LonLat(map.center.lon, map.center.lat).transform(
		map.getProjectionObject(), // from Spherical Mercator Projection
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
function setCookie(c_name,value,exdays) {
	var exdate=new Date();
	exdate.setDate(exdate.getDate() + exdays);
	var c_value=escape(value) + ((exdays==null) ? "" : "; expires="+exdate.toUTCString());
	document.cookie=c_name + "=" + c_value;
}


/**
 * Method for getting a cookie.
 */
function getCookie(c_name) {
	var i,x,y,ARRcookies=document.cookie.split(";");
	for (i=0;i<ARRcookies.length;i++) {
		x=ARRcookies[i].substr(0,ARRcookies[i].indexOf("="));
		y=ARRcookies[i].substr(ARRcookies[i].indexOf("=")+1);
		x=x.replace(/^\s+|\s+$/g,"");
		if (x==c_name) {
			return unescape(y);
		}
	}
}
