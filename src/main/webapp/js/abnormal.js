var eventListUrl = "/abnormal/json/list";
var events = [];

$(document).ready(function() {

	setup();
	
});

function setup(){

	if (includeAbnormalBehaviorTool && abnormalOpen){
		//setInterval('loadEvents()', 20000);
		loadEvents()
		setInterval('checkArguments()', 1000);
	}
	
}

function loadEvents(){

	checkArguments();

	if (warnings == ""){

		var data = getEventArguments();

		$.getJSON(eventListUrl, data, 
			function (result) {

				events = [];

				// Load new vessels
				var JSONEvents = result;
		
				for (eventId in JSONEvents) {
					// Create vessel based on JSON data
					var eventJSON = JSONEvents[eventId];
					var event = new AbnormalEvent(
							eventJSON.type, 
							eventJSON.description,
							eventJSON.location, 
							eventJSON.date,
							eventJSON.vessel,
							eventJSON.shipType,
							eventJSON.shipLength,
							eventJSON.involvedVessels
						);
				
					events.push(event);
				}

				drawEvents();
			}
		);
	}
}

function drawEvents(){

	// Clear event list
	$("#eventTable").empty('');

	// Header
	$("#eventTable").html(

			"<tr>" +
				"<th>Event type</th>" +
				"<th>Description</th>" +
				"<th>Location</th>" +
				"<th>Date</th>" +
				"<th>Vessel MMSI</th>" +
				"<th>Vessel length</th>" +
				"<th>Vessel type</th>" +
				"<th>Involved vessels</th>" +	
			"</tr>"
			
		);

	// Rows
	$.each(events, function(key, value) { 

			$("#eventTable").append(eventToTableRow(value));
		}
		
	);

	// NumberOfEvents
	$("#flash").html(events.length + " events found!");
	$("#flash").css('visibility', 'visible');
	$("#flash").css('background-color', '#77dd77');
	
}

function eventToTableRow(event){

	var str = 	"<tr>" +
						"<td>" + event.eventType + "</td>" +
						"<td>" + event.description + "</td>" +
						"<td>" + event.location.latitude.toFixed(3) + ", " + event.location.longitude.toFixed(3) + "</td>" +
						"<td>" + event.date + "</td>" +
						"<td>" + event.vesselMMSI + "</td>" +
						"<td>" + event.vesselLength + "</td>" +
						"<td>" + event.vesselType + "</td>" + 
						"<td>";

	// Add involved vessels
	if (event.involvedVessels != undefined){
		$.each(event.involvedVessels, function(key, value) { 

				str += value;

			}
		);
	}
	str += "</td><tr>";

	return str;

}

function getEventArguments(){

	// Get values
	var eventTypeCOG = $('#eventTypeCOG').attr('checked');
	var eventTypeSOG = $('#eventTypeSOG').attr('checked');
	var eventTypeSSC = $('#eventTypeSSC').attr('checked');
	var eventTypeCE = $('#eventTypeCE').attr('checked');
	var eventMMSI = $("#eventMMSI").val();
	var eventFromDate = $("#eventFromDate").val();
	var eventFromHour = $("#eventFromHour").val();
	var eventFromMin = $("#eventFromMin").val();
	var eventToDate = $("#eventToDate").val();
	var eventToHour = $("#eventToHour").val();
	var eventToMin = $("#eventToMin").val();

	var data = {};

	// Parse event types
	var eventTypes = [];
	if (eventTypeCOG){
		eventTypes.push("COG");
	}
	if (eventTypeSOG){
		eventTypes.push("SOG");
	}
	if (eventTypeSSC){
		eventTypes.push("SuddenSpeedChange");
	}
	if (eventTypeCE){
		eventTypes.push("CloseEncounter");
	}
	eventTypesStr = "";
	for(var i = 0; i < eventTypes.length; i++){
		eventTypesStr += eventTypes[i];
		if (i != eventTypes.length - 1){
			eventTypesStr += ",";
		}
	}
	data.eventTypes = eventTypesStr;

	// Parse MMSI
	if (eventMMSI != ""){
		data.mmsi = eventMMSI;
	}

	// Parse from date and time
	if (eventFromDate != "" 
			&& eventFromHour != ""  
			&& eventFromMin != ""){

		var dateFrom = parseDateAndTime(eventFromDate, eventFromHour, eventFromMin);
			
		data.timeMin = dateFrom.getTime();
		
	}

	// Parse to date and time
	if (eventToDate != "" 
			&& eventToHour != ""  
			&& eventToMin != ""){
			
		var dateTo = parseDateAndTime(eventToDate, eventToHour, eventToMin);
			
		data.timeMax = dateTo.getTime();
	}

	return data;
}


function parseDateAndTime(date, hour, min){

	var month = date.split("/")[0];
	month = parseInt(month) - 1;
	var day = date.split("/")[1];
	day = parseInt(day);
	var year = date.split("/")[2];
	year = parseInt(year);

	var myDate = new Date(year, month, day, hour, min, 0, 0);

	return myDate;

}

function checkArguments(){

	warnings = "";

	// Get values
	var eventTypeCOG = $('#eventTypeCOG').attr('checked');
	var eventTypeSOG = $('#eventTypeSOG').attr('checked');
	var eventTypeSSC = $('#eventTypeSSC').attr('checked');
	var eventTypeCE = $('#eventTypeCE').attr('checked');
	var eventMMSI = $("#eventMMSI").val();
	var eventFromDate = $("#eventFromDate").val();
	var eventFromHour = $("#eventFromHour").val();
	var eventFromMin = $("#eventFromMin").val();
	var eventToDate = $("#eventToDate").val();
	var eventToHour = $("#eventToHour").val();
	var eventToMin = $("#eventToMin").val();

	// Parse from date and time
	if (eventFromDate != "" 
			&& eventFromHour != ""  
			&& eventFromMin != ""
			&& eventToDate != ""
			&& eventToHour != ""  
			&& eventToMin != ""){

		var dateFrom = parseDateAndTime(eventFromDate, eventFromHour, eventFromMin);

		var dateTo = parseDateAndTime(eventToDate, eventToHour, eventToMin);

		// Compare dates and times
		if (dateFrom >= dateTo){
			warnings += '"From" must be before "To" <br>';
		}
		
	}
	
	if (warnings == ""){
		//$("#flash").css('visibility', 'hidden');
	} else {
		$("#flash").html(warnings);
		$("#flash").css('visibility', 'visible');
		$("#flash").css('background-color', '#dd7777');
	}

}











