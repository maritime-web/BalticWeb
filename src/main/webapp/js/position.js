"use strict";

embryo.geographic = {};

embryo.geographic.parseLatitude = function() {
	if (arguments.length == 1) {
		if(this.isNumber(arguments[0])){
			return parseFloat(arguments[0]);
		}		
		
		// TODO validate latitude format
		var parts = this.splitFormattedPos(arguments[0]);
		return this.parseLatitude(parts[0], parts[1], parts[2]);
	}

	if (arguments.length != 3) {
		return null;
	}

	// TODO validate latitude format
	var h = parseInt(arguments[0]);
	var m = parseFloat(arguments[1]);
	var ns = arguments[2].trim();

	// not necessary if format has been validated
	// if (h == null || m == null || ns == null) {
	// // throw new FormatException();
	// }
	// if (!ns.equals("N") && !ns.equals("S")) {
	// throw new FormatException();
	// }
	var lat = h + m / 60.0;
	if (ns === "S") {
		lat *= -1;
	}
	return lat;
};

embryo.geographic.isNumber = function(n) {
	return !isNaN(parseFloat(n)) && isFinite(n);
};

embryo.geographic.parseLongitude = function() {
	if (arguments.length == 1) {
		if(this.isNumber(arguments[0])){
			return parseFloat(arguments[0]);
		}		

		// TODO validate longitude format
		var parts = this.splitFormattedPos(arguments[0]);
		return this.parseLongitude(parts[0], parts[1], parts[2]);
	}

	// TODO validate longitude format
	if (arguments.length != 3) {
		return null;
	}

	var h = parseInt(arguments[0]);
	var m = parseFloat(arguments[1]);
	var ew = arguments[2].trim();

	// not necessary if format has been validated
	// if (h === NaN || m === NaN || ew == null) {
	// return null;
	//        	
	// throw "format exception for values";
	// }
	// if (!ew.equals("E") && !ew.equals("W")) {
	// throw new FormatException();
	// }
	var lon = h + m / 60.0;
	if (ew === "W") {
		lon *= -1;
	}
	return lon;
};

embryo.geographic.splitFormattedPos = function(posStr) {
	if (posStr.length < 4) {
		throw new FormatException();
	}
	var parts = [];
	parts[2] = posStr.substring(posStr.length - 1);
	posStr = posStr.substring(0, posStr.length - 1);
	var posParts = posStr.split(" ");
	if (posParts.length != 2) {
		throw new FormatException();
	}
	parts[0] = posParts[0];
	parts[1] = posParts[1];
	return parts;
};