$(function() {
    var yourShip;

    var yourShipRouteLayer = new RouteLayer("#2a6237");

    addLayerToMap("vessel", yourShipRouteLayer, embryo.map);

    function updateBox(error, vesselOverview, vesselDetails) {
        if (!error) {
            yourShip = vesselOverview;
            embryo.vessel.setMarkedVessel(vesselOverview.id);

            $("a[href=#vcpYourShip]").html("Your Vessel - "+vesselDetails.ais.name);
            $("#yourShipAesInformation table").html(embryo.vesselInformation.renderYourShipShortTable(vesselDetails));
            $("#yourShipAesInformationLink").off("click");
            $("#yourShipAesInformationLink").on("click", function(e) {
                e.preventDefault();
                embryo.vesselInformation.showAesDialog(vesselDetails);
            });
            setupAdditionalInformationTable("#yourShipAdditionalInformation", vesselOverview, vesselDetails, "YourShip");
            if (vesselDetails.additionalInformation.routeId) {
                embryo.route.service.getRoute(vesselDetails.additionalInformation.routeId, function(data) {
                    console.log("Drawing router layer for your vessel", data);
                    yourShipRouteLayer.draw(data);
                });
            }
            setupReporting("#yourShipReporting", vesselOverview, vesselDetails);
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
