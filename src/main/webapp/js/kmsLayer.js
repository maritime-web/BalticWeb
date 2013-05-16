
/**
 * Add the KMS layer to the map.
 * This function does not create a bounding box at the moment, 
 * which it should.
*/
function addKMSLayer(){
    // Add KMS wms layer
	var proj = map.projection;
	var zfactor = Math.pow(2, map.zoom);
	
	// Get bounding box
	/*
	var viewportWidth = $(map.getViewport()).width();
	var viewportHeight = $(map.getViewport()).height();
	var topLeftPixel = new OpenLayers.Pixel(50,50);
	var botRightPixel = new OpenLayers.Pixel(viewportWidth,viewportHeight);
	var top = map.getLonLatFromPixel(topLeftPixel);
	var bot = map.getLonLatFromPixel(botRightPixel);
	
	//corrections for the slight shift of the SLP (mapserver)
        var deltaX = 0.0013;
        var deltaY = 0.00058;
					
	var bbox = (top.lon) + ','
		+ bot.lat + ','
		+ bot.lon + ','
		+ top.lat;
	*/
	
	// WMS URL
	//http://kortforsyningen.kms.dk/soe_enc_primar? - Small map
	var url ='http://kortforsyningen.kms.dk/?servicename=soe_enc&'
	url += '&REQUEST=GetMap'; //WMS operation
	url += '&SERVICE=WMS'; //WMS service
	url += '&VERSION=1.1.1'; //WMS version
	url += '&LAYERS=cells'; //WMS layers ,coverage:coverage.1,coverage:coverage.2,coverage:coverage.L
	url += '&FORMAT=image/gif'; //WMS format
	//url += '&BGCOLOR=0xFFFFFF';
	url += '&TRANSPARENT=true';
	url += '&SRS=EPSG:4326'; //set WGS84
	//url += '&BBOX=' + bbox; // set bounding box
	url += '&WIDTH=' + map.tileSize.w; //tile size in map
	url += '&HEIGHT=' + map.tileSize.h;
	
	
	//http://kortforsyningen.kms.dk/soe_enc_primar?ignoreillegallayers=TRUE&transparent=TRUE&login=StatSofart&password=114karls&VERSION=1.1.1&REQUEST=GetMap&SRS=EPSG:4326&LAYERS=cells&STYLES=style-id-246&FORMAT=image/gif&service=WMS
	
	if(map.zoom>3){
		url += '&STYLES=style-id-245';
	}else{
		url += '&STYLES=style-id-200';
	}
	
	url += '&login=StatSofart&password=114karls';
	 
    var wms = new OpenLayers.Layer.WMS(
		"KMS",
		url,
		{
			'layers':'basic',
			//'maxExtent': new OpenLayers.Bounds(bot.lon, bot.lat, top.lon, top.lat),
			//'maxResolution': "auto",
			'isBaseLayer': false
		} 
	);

    //add WMS layer
    map.addLayer(wms);

}