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

function setLayerOpacityById(id, value) {
    var layers = embryo.map.internalMap.getLayersByName(id);
    
    for (var k in layers) {
        layers[k].setOpacity(value);
    }
}

// de her to er lidt vilde, men altsaa noedvendige

function openCollapse(id) {
    if (!$(id).hasClass("in") && !$(id).hasClass("collapse-opening")) {
        $(id).addClass("collapse-opening");
        $("a[href=#"+$(id).attr("id")+"]").click();
        setTimeout(function() {
            $(id).removeClass("collapse-opening");
        }, 500);
    }
    $(id).removeClass("collapse-closing");
}

function closeCollapse(id) {
    if ($(id).hasClass("in") && !$(id).hasClass("collapse-opening")) {
        $(id).addClass("collapse-closing");
        setTimeout(function() { 
            if ($(id).hasClass("collapse-closing")) {
                $(id).removeClass("collapse-closing");
                //if ($(id).is(":visible")) {
                    $("a[href="+id+"]").click();
                //}
            }
        }, 500);
    }
}

function formatLongitude(longitude) {
    var ns = "E";
    if (longitude < 0) {
        ns = "W";
        longitude *= -1;
    }
    var hours = Math.floor(longitude);
    longitude -= hours;
    longitude *= 60;
    var lonStr = longitude.toFixed(3);
    while (lonStr.indexOf('.') < 2) {
        lonStr = "0" + lonStr;
    }

    return (hours/1000.0).toFixed(3).substring(2) + " " + lonStr + ns;
}

function formatLatitude(latitude) {
    var ns = "N";
    if (latitude < 0) {
        ns = "S";
        latitude *= -1;
    }
    var hours = Math.floor(latitude);
    latitude -= hours;
    latitude *= 60;
    var latStr = latitude.toFixed(3);
    while (latStr.indexOf('.') < 2) {
        latStr = "0" + latStr;
    }

    return (hours/100.0).toFixed(2).substring(2) + " " + latStr + ns;
}

function formatLonLat(lonlat) {
    return formatLatitude(lonlat.lat) + " " + formatLatitude(lonlat.lon);
}
