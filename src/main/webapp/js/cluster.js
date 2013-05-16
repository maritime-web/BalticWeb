/**
 * Cluster object
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
function Cluster(from, to, count, density, vessels) {
	this.from = from;
	this.to = to;
	this.count = count;
	this.density = density;

	var vesselList = [];
	$.each( vessels, function(i, n){

			n = new Vessel(i, n, 1);
			vesselList.push(n);
			
		}
	);

	this.vessels = vesselList;
	
}
