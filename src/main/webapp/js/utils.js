function setCookie(c_name, value, exdays) {
    var exdate = new Date();
    exdate.setDate(exdate.getDate() + exdays);
    var c_value = escape(value) + ((exdays == null) ? "" : "; expires=" + exdate.toUTCString());
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
    $(".collapse", $(id).parents(".panel-group")).data("collapse", null);
    
    function openSelected(){
        if (!$(id).hasClass("in")) {
            $("a[href=#" + $(id).attr("id") + "]").click();
        }
        
        if (!$(id).hasClass("in")) {
            setTimeout(openSelected, 10);
        }
    }
    
    openSelected();
}

function closeCollapse(id) {
    $(".collapse", $(id).parents(".panel-group")).data("collapse", null);
    if ($(id).hasClass("in")) {
        $("a[href=#" + $(id).attr("id") + "]").click();
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

    return (hours / 1000.0).toFixed(3).substring(2) + " " + lonStr + ns;
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

    return (hours / 100.0).toFixed(2).substring(2) + " " + latStr + ns;
}

function formatLonLat(lonlat) {
    return formatLatitude(lonlat.lat) + " " + formatLongitude(lonlat.lon);
}

function formatNauticalMile(km) {
    var result = (parseFloat(km) / 1.852);
    if (result > 25)
        return result.toFixed(0) + " NM";
    return result.toFixed(1) + " NM";
}

function formatCourse(course) {
    if(!course && course != 0){
        return course;
    }
    
    if(course < 0){
        return course;
    }
    if(course < 10){
        return "00" + course;
    }
    if(course < 100){
        return "0" + course;
    }
    return course.toString();
}


function formatDate(timeInMillis) {

	if (timeInMillis == null){
		return "-";
	}
    
	var date = new Date(timeInMillis);
    
	return date.getUTCFullYear() + "-" + ("" + (101 + date.getUTCMonth())).slice(1, 3) + "-" + ("" + (100 + date.getUTCDate())).slice(1, 3);
}

function formatTime(timeInMillis) {
    
	if (timeInMillis == null) {
		return "-";
	}
    
    var date = new Date(timeInMillis);
    
    return formatDate(timeInMillis) + " " + date.getUTCHours() + ":" + ("" + (100 + date.getUTCMinutes())).slice(1, 3);
}

function formatHour(hour) {
    return Math.floor(hour) + ":" + (0.6 * (hour - Math.floor(hour))).toFixed(2).substring(2);
}

function formatSize(size) {
    if (size < 1024 * 1024)
        return Math.round(size / 1024) + " KB";
    return (Math.round(size / 1024 / 1024 * 10) / 10) + " MB";
}

function adjustDateForUTC(millis) {
    return millis + (new Date().getTimezoneOffset() * 60000);
}

function adjustDateForLocal(millis) {
    return millis - (new Date().getTimezoneOffset() * 60000);
}

function arrayToHttpParams(arr, paramName) {
    if(!(arr && paramName)) {
        return '';
    }
    var result = paramName + '=' + arr[0];
    for(var i = 1; i < arr.length; i++) {
        result += '&' + paramName + '=' + arr[i];
    }
    return result;
}

function knots2Ms(knots){
    return knots * 1.852 / 3.6;
}

function ms2Knots(ms){
    return ms * 3.6 / 1.852;
}

function assertObjectFieldValue(object, fieldName) {
    var value = object[fieldName];
    if (!value && value != 0) {
        throw new Error(fieldName + " has invalid value " + value)
    }
}

function assertValue(value, msg) {
    if (!value && value != 0) {
        throw new Error(msg ? msg + " has invalid value " + value : value + " is not a valid value")
    }
}


var browser = {
    isIE : function(){
        var myNav = navigator.userAgent.toLowerCase();
        return myNav.indexOf('msie') != -1;
    },
    ieVersion : function() {
        var index = null, version = 999; // we assume a sane browser
        var myNav = navigator.userAgent.toLowerCase();

        var parts = myNav.split(";");
        
        for(index in parts){
            if(parts[index].indexOf("msie") >= 0){
                return parseFloat(parts[index].split("msie")[1]);
            }
        }
        return version;
    },
    chromeVersion : function() {
        var index = null, version = 999; // we assume a sane browser
        var myNav = navigator.userAgent.toLowerCase();
        var parts = myNav.split(" ");
        
        for(index in parts){
            if(parts[index].indexOf("chrome") >= 0){
                return parts[index].split("chrome/")[1];
            }
        }
        return version;
    },
    isChrome : function(){
        var myNav = navigator.userAgent.toLowerCase();
        return myNav.indexOf('chrome') != -1;
    }
};