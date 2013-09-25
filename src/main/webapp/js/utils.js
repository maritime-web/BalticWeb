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

