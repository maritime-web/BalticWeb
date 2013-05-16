
function searchResultToHTML(vessel, key){

	var html = "<div class='event oldEvent' onClick='goToSearchedVessel(" + key + ")'>" +
					"<div class='panelText'>" +
						vessel.vesselName +
					"</div>";

	if (searchResultsShowPositon){
	html += 		"<div class='smallText'>" +
						vessel.lon + ", " + vessel.lat
					"</div>";

	}
	
	html +=		"</div>";

	return html;

}

function eventToHTML(event){

	var html = "<div class='event oldEvent' onClick='goToLocation(" + event.location.longitude + ", " + event.location.latitude + ")'>" +
					"<div class='panelText'>" +
						event.description +
					"</div>" +
				"</div>";

	return html;

}

function toolToHTML(tool){

	var panelName = '"' + tool.panel + '"'; 

	var html = "<div class='tool' onClick='openPanel(" + panelName + ")'>" +
					"<img src=" + tool.img + ">" +
				"</div>";

	return html;

}
