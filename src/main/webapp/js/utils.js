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
    return new OpenLayers.LonLat(lon, lat).transform(
        new OpenLayers.Projection("EPSG:4326"),
        embryo.mapPanel.map.getProjectionObject()
    );
}

function setLayerOpacityById(id, value) {
    var layers = embryo.mapPanel.map.getLayersByName(id);
    
    for (var k in layers) {
        layers[k].setOpacity(value);
    }
}

