function setCookie(c_name, value, exdays) {
    var exdate = new Date();
    exdate.setDate(exdate.getDate() + exdays);
    var c_value = escape(value)
	+ ((exdays == null) ? "" : "; expires=" + exdate.toUTCString());
    document.cookie = c_name + "=" + c_value;
}

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

function openCollapse(id) {
    $(".collapse", $(id).parents(".accordion")).data("collapse", null);
    setTimeout(function() {
        if (!$(id).hasClass("in")) {
            $("a[href=#"+$(id).attr("id")+"]").click();
        }
    }, 10)
}

function closeCollapse(id) {
    $(".collapse", $(id).parents(".accordion")).data("collapse", null);
    if ($(id).hasClass("in")) {
        $("a[href=#"+$(id).attr("id")+"]").click();
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
    return formatLatitude(lonlat.lat) + " " + formatLongitude(lonlat.lon);
}

function formatNauticalMile(km) {
    var result = (parseFloat(km) / 1.852);
    if (result > 25) return result.toFixed(0) + " NM";
    return result.toFixed(1) + " NM";
}

function formatDate(dato) {
    if (dato == null) return "-";
    var d = new Date(dato);
    return d.getFullYear()+"-"+(""+(101+d.getMonth())).slice(1,3)+"-"+(""+(100+d.getDate())).slice(1,3);
}

function formatTime(dato) {
    if (dato == null) return "-";
    var d = new Date(dato);
    return formatDate(dato) + " " + d.getHours()+":"+(""+(100+d.getMinutes())).slice(1,3);
}

function formatHour(hour) {
    return Math.floor(hour) + ":" + (0.6 * (hour - Math.floor(hour))).toFixed(2).substring(2);
}

function formatSize(size) {
    if (size < 1024*1024) return Math.round(size / 1024) + " KB";
    return (Math.round(size / 1024 / 1024 * 10) / 10) + " MB";
}

function parseLatitude(formattedString) {
    var parts = splitFormattedPos(formattedString);
    return parseLat(parts[0], parts[1], parts[2]);
}

function parseLongitude(formattedString) {
    var parts = splitFormattedPos(formattedString);
    return parseLon(parts[0], parts[1], parts[2]);
}

function splitFormattedPos(posStr) {
    if (posStr.length < 4) {
        throw "Format exception";
    }
    var parts = [];
    parts[2] = posStr.substring(posStr.length - 1);
    posStr = posStr.substring(0, posStr.length - 1);
    var posParts = posStr.split(" ");
    if (posParts.length != 2) {
        throw "Format exception";
    }
    parts[0] = posParts[0];
    parts[1] = posParts[1];

    return parts;
}

function parseString(str){
    str = str.trim();
    if (str == null || str.length == 0) {
        return null;
    }
    return str;
}

function parseLat(hours, minutes, northSouth) {
    var h = parseInt(hours);
    var m = parseFloat(minutes);
    var ns = parseString(northSouth);
    if (h == null || m == null || ns == null) {
        throw "Format exception";
    }
    if (!ns == "N" && !ns == "S") {
        throw "Format exception";
    }
    var lat = h + m / 60.0;
    if (ns == "S") {
        lat *= -1;
    }
    return lat;
}

function parseLon(hours, minutes, eastWest) {
    var h = parseInt(hours);
    var m = parseFloat(minutes);
    var ew = parseString(eastWest);
    if (h == null || m == null || ew == null) {
        throw "Format exception";
    }
    if (!(ew == "E") && !(ew == "W")) {
        throw "Format exception";
    }
    var lon = h + m / 60.0;
    if (ew == "W") {
        lon *= -1;
    }
    return lon;
}

