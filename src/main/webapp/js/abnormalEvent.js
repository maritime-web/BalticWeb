/**
 * Abnormal event object
 *
 * @param from
 *			Top left LonLat
 * @param to
 *			Top right LonLat
 * @param count
 *			The number of vessels in the cluster
 * @param locations
 *			A list of vessel locations in the cluster.
 *			This list is empty if count is large.
 * @returns vessel object
 */
function AbnormalEvent(
		eventType, 
		description,
		location, 
		date,
		vesselMMSI,
		vesselType,
		vesselLength,
		involvedVessels) {

	this.eventType = eventType;
	this.description = description;
	this.location = location;
	this.date = date;
	this.vesselMMSI = vesselMMSI;
	this.vesselType = vesselType;
	this.vesselLength = vesselLength;
	this.involvedVessels = involvedVessels;
	
}
