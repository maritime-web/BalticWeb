/**
 * Commonly used functions for formatting and parsing map postions
 */


function formatDegree(deg, isLatitude, decimals, pp) {
    decimals = decimals || 3;
    pp = pp || false;

    var suffix = isLatitude ? "N" : "E";
    if (deg < 0) {
        suffix = isLatitude ? "S" : "W";
        deg *= -1;
    }
    var hours = Math.floor(deg);
    deg -= hours;
    deg *= 60;

    var decimalStr = [ '0', '0.0', '0.00', '0.000', '0.0000', '0.00000'];
    var minuteStr = numeral(deg).format(decimalStr[decimals]);
    while (minuteStr.length < 2 + 1 + decimals) {
        minuteStr = '0' + minuteStr;
    }

    // Check if we need to pretty-print
    var degSymbol = pp ? '\u00b0' : '';
    var minSymbol = pp ? '\'' : '';
    var hourStr = isLatitude
        ? (hours / 100.0).toFixed(2).substring(2)
        : (hours / 1000.0).toFixed(3).substring(2);
    return hourStr + degSymbol + " " + minuteStr + minSymbol + suffix;
}

function formatLongitude(longitude, decimals, pp) {
    return formatDegree(longitude, false, decimals, pp);
}

function formatLatitude(latitude, decimals, pp) {
    return formatDegree(latitude, true, decimals, pp);
}

function formatLatLon(lonlat, decimals, pp) {
    var divider = pp ? ' - ' : ' ';
    return formatLatitude(lonlat.lat, decimals, pp) + divider + formatLongitude(lonlat.lon, decimals, pp);
}

function parseLatLon(value) {
    if (!value) {
        throw "Format exception";
    }
    value = value.trim().toUpperCase();
    var index = value.indexOf("N");
    if (index == -1) {
        index = value.indexOf("S");
    }
    if (index == -1) {
        throw "Format exception";
    }
    return {
        lat: parseLatitude(value.substring(0, index + 1).trim()),
        lon: parseLongitude(value.substring(index + 2).trim())
    };

}

function cleanUpDegree(deg) {
    // Get rid of potential lat-lon divider ('-'), degree (°), minute (') and second symbols (")
    deg = deg.replace(/°|'|"|-|/g, '');
    return deg.trim();
}

function parseLatitude(value) {
    value = cleanUpDegree(value);
    if (value.trim().indexOf(" ") < 0) {
        var parsed = numeral().unformat(value);
        if (parsed == value) {
            return parsed;
        }
    }
    var parts = splitFormattedPos(value);
    return parseLat(parts[0], parts[1], parts[2]);
}

function parseLongitude(value) {
    value = cleanUpDegree(value);
    if (value.trim().indexOf(" ") < 0) {
        var parsed = numeral().unformat(value);
        if (parsed == value) {
            return parsed;
        }
    }
    var parts = splitFormattedPos(value);
    return parseLon(parts[0], parts[1], parts[2]);
}

function splitFormattedPos(posStr) {
    var parts = [];
    parts[2] = posStr.substring(posStr.length - 1);
    posStr = posStr.substring(0, posStr.length - 1);
    var posParts = posStr.trim().split(" ");
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
    var h = parseInt(hours, 10);
    var m = numeral().unformat(minutes);
    var ns = parseString(northSouth);
    if (h == null || m == null || ns == null) {
        throw "Format exception";
    }
    ns = ns.toUpperCase();
    if (!(ns == "N") && !(ns == "S")) {
        throw "Format exception";
    }
    var lat = h + m / 60.0;
    if (ns == "S") {
        lat *= -1;
    }
    return lat;
}

function parseLon(hours, minutes, eastWest) {
    var h = parseInt(hours, 10);
    var m = numeral().unformat(minutes);
    var ew = parseString(eastWest);
    if (h == null || m == null || ew == null) {
        throw "Format exception";
    }
    ew = ew.toUpperCase();
    if (!(ew == "E") && !(ew == "W")) {
        throw "Format exception";
    }
    var lon = h + m / 60.0;
    if (ew == "W") {
        lon *= -1;
    }
    return lon;
}

function positionDirective(directive, formatter1, parser) {
    function formatter(value) {
        if (value || value === 0) return formatter1(value);
        return null;
    }

    return {
        require : '^ngModel',
        restrict : 'A',
        link : function(scope, element, attr, ctrl) {
            ctrl.$formatters.unshift(function(modelValue) {
                if (!modelValue) {
                    return null;
                }
                return formatter(modelValue);
            });

            ctrl.$parsers.unshift(function(valueFromInput) {
                try {
                    var val = parser(valueFromInput);
                    ctrl.$setValidity(directive, true);
                    return val;
                } catch (e) {
                    ctrl.$setValidity(directive, false);
                    return undefined;
                }
            });

            element.bind('change', function(event) {
                if (!ctrl.$modelValue) {
                    ctrl.$viewValue = null;
                }
                ctrl.$viewValue = formatter(ctrl.$modelValue);
                ctrl.$render();
            });

        }
    };
}


