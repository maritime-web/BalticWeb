$(function() {
    var yourShip;

    var yourShipRouteLayer = new RouteLayer("#D5672D");

    addLayerToMap("vessel", yourShipRouteLayer, embryo.map);

    function updateBox(error, vesselOverview, vesselDetails) {
        // console.log("updateBox", error, vesselOverview, vesselDetails);
        if (!error) {
            yourShip = vesselOverview;
            embryo.vessel.setMarkedVessel(vesselOverview.mmsi);

            $("a[href=#vcpYourShip]").html("Your Vessel - "+vesselOverview.name);
            $("#yourShipAesInformation table").html(embryo.vesselInformation.renderYourShipShortTable(vesselDetails));
            $("#yourShipAesInformationLink").off("click");
            $("#yourShipAesInformationLink").on("click", function(e) {
                e.preventDefault();
                embryo.vesselInformation.showAesDialog(vesselDetails);
            });
            if (vesselDetails.additionalInformation.routeId) {
                embryo.route.service.getRoute(vesselDetails.additionalInformation.routeId, function(data) {
                    // console.log("Drawing router layer for your vessel", data);
                    yourShipRouteLayer.draw(data);
                });
            }
            embryo.vessel.actions.setup("#yourVesselActions", embryo.vessel.actions.yourVessel, vesselOverview, vesselDetails);
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
        embryo.vessel.goToVesselLocation(embryo.vessel.lookupVessel(yourShip.mmsi));
    });
});
