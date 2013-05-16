/**
 * Vessel object
 * 
 * @param vesselId vessels unique id
 * @param vessel JSON vessel data
 * @param markerScale
 *            Scale of the marker
 * @returns vessel object
 */
function Vessel(vesselId, vessel, markerScale) {
	this.id = vesselId;
	this.lat = vessel[1];
	this.lon = vessel[2];
	
	// Set color and vessel type
	this.color = vessel[4];
	switch(vessel[4]){
		case "0" : this.colorName = "blue"; break;
		case "1" : this.colorName = "gray"; break;
		case "2" : this.colorName = "green"; break;
		case "3" : this.colorName = "orange"; break;
		case "4" : this.colorName = "purple"; break;
		case "5" : this.colorName = "red"; break;
		case "6" : this.colorName = "turquoise"; break;
		case "7" : this.colorName = "yellow"; break;
		default : this.colorName = "unknown";
	}

	// Moored or not
	if (vessel[5] == 1) {
		this.moored = true;
	}
	
	// Set image properties
	if (this.moored){
		this.image = "vessel_" + this.colorName + "_moored.png";
		this.imageWidth = 12;
		this.imageHeight = 12;
	} else {
		this.image = "vessel_" + this.colorName + ".png";
		this.imageWidth = 20;
		this.imageHeight = 10;
	}
	this.imageYOffset = -this.imageHeight/2;
	this.imageXOffset = -this.imageWidth/2;
	
	this.degree = vessel[0];

	// Not anonymous
	if (vessel.length > 6){
		this.mmsi = vessel[6];
		this.vesselName = vessel[7];
		this.imo = vessel[9];
	}

}
