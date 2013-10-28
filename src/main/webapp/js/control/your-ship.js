$(function() {
    var yourShip;

    var yourShipRouteLayer = new RouteLayer("#2a6237");

    addLayerToMap("vessel", yourShipRouteLayer, embryo.map);

    function updateBox(error, vesselOverview, vesselDetails) {
        if (!error) {
            yourShip = vesselOverview;
            embryo.vessel.setMarkedVessel(vesselOverview.id);

            $("a[href=#vcpYourShip]").html("Your Ship - "+vesselDetails.name);
            $("#yourShipAesInformation table").html(embryo.vesselInformation.renderYourShipShortTable(vesselDetails));
            $("#yourShipAesInformationLink").off("click");
            $("#yourShipAesInformationLink").on("click", function(e) {
                e.preventDefault();
                embryo.vesselInformation.showAesDialog(vesselDetails);
            });
            setupAdditionalInformationTable("#yourShipAdditionalInformation", vesselOverview, vesselDetails, "YourShip");
            if (vesselDetails.route) yourShipRouteLayer.draw(vesselDetails.route);
        }
    }

    embryo.authenticated(function() {
        if (embryo.authentication.shipMmsi) {
            embryo.vessel.service.subscribe(embryo.authentication.shipMmsi, updateBox);
        } else {
            $("#vcpYourShip").parent().remove();
            $("#zoomToYourShip").remove();
        }
    });

    $("#zoomToYourShip").click(function() {
        embryo.vessel.goToVesselLocation(embryo.vessel.lookupVessel(yourShip.id));
    });
});
