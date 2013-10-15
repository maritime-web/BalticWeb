$(function() {
    var yourShip;

    function setup() {
        function downloadShipDetails(id) {
            $.ajax({
                url: embryo.baseUrl+"rest/vessel/details", // detailsUrl,
                data: {
                    id : id,
                    past_track: 0
                },
                success: function (result) {
                    $("a[href=#vcpYourShip]").html("Your Ship - "+result.name);
                    $("#yourShipAesInformation table").html(embryo.vesselInformation.renderYourShipShortTable(result));
                    $("#yourShipAesInformationLink").off("click");
                    $("#yourShipAesInformationLink").on("click", function(e) {
                        e.preventDefault();
                        embryo.vesselInformation.showAesDialog(result);
                    });
                    setupAdditionalInformationTable("#yourShipAdditionalInformation", yourShip, result, "YourShip");
                }
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

            embryo.vessel.setMarkedVessel(yourShip.id);

            downloadShipDetails(yourShip.id);
        });
    }
    
    embryo.authenticated(function() {
        setup();
        setInterval(setup, loadFrequence);
    });

    $("#zoomToYourShip").click(function() {
        embryo.vessel.goToVesselLocation(embryo.vessel.lookupVessel(yourShip.id));
    });
});
