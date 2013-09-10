$(function() {
    var yourShip;
    
    embryo.authenticated(function() {
        function downloadShipDetails(id) {
            $.ajax({
                url: embryo.baseUrl+detailsUrl,
                data: { 
                    id : id,
                    past_track: 0
                },
                success: function (result) {
                    $("a[href=#vcpYourShip]").html("Your Ship - "+result.name);
                    $("#yourShipAesInformation table").html(embryo.vesselInformation.renderShortTable(result));
                    $("#yourShipAesInformationLink").off("click");
                    $("#yourShipAesInformationLink").on("click", function() {
                        embryo.vesselInformation.showAesDialog(result);
                    });
                }
            });
            
            updateReportStatus();
        }
        

    	function updateReportStatus() {
    		var injector = angular.element(document).injector();

    		$("#shipInfo").attr('href', 'index.html#/ship/');
    		$("#greenposReport").attr('href', 'index.html#/report');

    		var VoyageService = injector.get('VoyageService');
    		var RouteService = injector.get('RouteService');
    		var ShipService = injector.get('ShipService');

    		ShipService.getYourShip(function(ship) {
    			$("#voyagePlan").attr('href', 'index.html#/voyagePlan/' + ship.mmsi + '/current');
    			
    			RouteService.getActive(ship.mmsi, function(route) {
    				$("#routeEdit").attr('href', 'index.html#/routeEdit/' + ship.mmsi + "/" + route.id);
    			});

    			VoyageService.getYourActive(function(voyage) {
    				$("#routeUpload").attr('href', 'index.html#/routeUpload/' + ship.mmsi + "/" + voyage.maritimeId);
    			});
    		});

    	}

        $.getJSON(embryo.baseUrl + searchUrl, { argument: embryo.authentication.shipMmsi }, function (result) {
	    var searchResults = [];

	    for (var vesselId in result.vessels) {
	        var vesselJSON = result.vessels[vesselId];
	        var vessel = new Vessel(vesselId, vesselJSON, 1);
		searchResults.push(vessel);
	    }
            
	    if (searchResults.length <= searchResultsLimit && searchResults.length != 0){
	        if (searchResults.length == 1){
		    yourShip = searchResults[0];
	        }
	    }
	    
	    embryo.vessel.markedVessel = yourShip;

            downloadShipDetails(yourShip.id);
        });
    });

    $("#zoomToYourShip").click(function() {
        embryo.vessel.goToVesselLocation(yourShip);
    });
});
