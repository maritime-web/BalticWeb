$(function() {
    var yourShip;

//  var yourShipRouteLayer = RouteLayerSingleton.getInstance();
    var yourShipRouteLayer = new RouteLayer();
    addLayerToMap("vessel", yourShipRouteLayer, embryo.map);

    function updateBox(error, vesselOverview, vesselDetails) {
        if (!error) {
            yourShip = vesselOverview;
            embryo.vessel.setMarkedVessel(vesselOverview.mmsi);

            $("a[href=#vcpYourShip]").html("Your Vessel - "+vesselOverview.name);
            $("#yourShipAesInformation table").html(embryo.vesselInformation.renderYourShipShortTable(vesselDetails));
            $("#yourShipAesInformationLink").off("click");
            $("#yourShipAesInformationLink").on("click", function(e) {
                e.preventDefault();
                embryo.controllers.ais.open(vesselDetails.ais);
            });
            if (vesselDetails.additionalInformation.routeId) {
                embryo.route.service.getRoute(vesselDetails.additionalInformation.routeId, function(data) {
                    yourShipRouteLayer.draw(data, "active");
                });
            }else{
                yourShipRouteLayer.clear();
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
