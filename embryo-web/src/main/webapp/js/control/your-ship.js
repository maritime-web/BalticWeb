$(function() {
    var yourShip;

    // var yourShipRouteLayer = RouteLayerSingleton.getInstance();
    var yourShipRouteLayer = new RouteLayer();
    addLayerToMap("vessel", yourShipRouteLayer, embryo.map);

    var updateInformationFn;

    function updateBox(error, vesselOverview, vesselDetails) {
        if (!error) {
            yourShip = vesselOverview;
            embryo.vessel.setMarkedVessel(vesselOverview.mmsi);

            $("a[href=#vcpYourShip]").html("Your Vessel - " + vesselOverview.name);
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
            } else {
                yourShipRouteLayer.clear();
            }

            updateInformationFn = updateInformation(vesselOverview, vesselDetails);
            updateInformationFn();
        }
    }

    function updateInformation(vesselOverview, vesselDetails) {
        return function() {
            var informations;
            informations = [ "ArcticWeb Reporting" ];

            var vesselInformation = embryo.vessel.information.getVesselInformation();
            vesselInformation.sort(function(object1, object2) {
                if (object1.title === "Vessel Information")
                    return -1;
                if (object2.title === "Vessel Information")
                    return 1;
                if (object1.title === "Schedule")
                    return -1;
                if (object2.title === "Schedule")
                    return 1;
                return 0;
            });

            for ( var i in embryo.vessel.information.getVesselInformation()) {
                informations.push(vesselInformation[i]);
            }

            informations.push("Additional Information");
            informations.push(embryo.additionalInformation.historicalTrack);
            informations.push(embryo.additionalInformation.nearestShips);
            informations.push(embryo.additionalInformation.distanceCircles);

            embryo.vessel.actions.setup("#yourVesselActions", informations, vesselOverview, vesselDetails);
        };
    }

    embryo.authenticated(function() {
        if (embryo.authentication.shipMmsi) {
            embryo.vessel.service.subscribe(embryo.authentication.shipMmsi, updateBox);

            embryo.vesselInformationAddedEvent(function(e) {
                if(typeof updateInformationFn === "function"){
                    updateInformationFn();
                }
            });
        } else {
            $("#vcpYourShip").parent().remove();
            $("#zoomToYourShip").remove();
        }
    });

    $("#zoomToYourShip").click(function() {
        embryo.vessel.goToVesselLocation(embryo.vessel.lookupVessel(yourShip.mmsi));
    });
});
