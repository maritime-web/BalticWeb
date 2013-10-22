$(function() {
    var yourShip;

    var yourShipRouteLayer = new RouteLayer("#2a6237");

    addLayerToMap("vessel", yourShipRouteLayer, embryo.map);

    function setup() {
        function downloadShipDetails(id) {
            embryo.vessel.service.details(id, function(error, data) {
                if (data) {
                    $("a[href=#vcpYourShip]").html("Your Ship - "+data.name);
                    $("#yourShipAesInformation table").html(embryo.vesselInformation.renderYourShipShortTable(data));
                    $("#yourShipAesInformationLink").off("click");
                    $("#yourShipAesInformationLink").on("click", function(e) {
                        e.preventDefault();
                        embryo.vesselInformation.showAesDialog(data);
                    });
                    setupAdditionalInformationTable("#yourShipAdditionalInformation", yourShip, data, "YourShip");
                    if (data.route) yourShipRouteLayer.draw(data.route);
                } else {
                    embryo.messagePanel.show({ text: "Server returned error code: " + error.status + " getting vessel details.", type: "error" });
                }
            })
        }

        embryo.vessel.service.search(embryo.authentication.shipMmsi, function(error, data) {
            if (data) {
                $.each(data, function(k, v) {
                    yourShip = new Vessel(k, v);
                })

                embryo.vessel.setMarkedVessel(yourShip.id);

                downloadShipDetails(yourShip.id);
            } else {
                embryo.messagePanel.show({ text: "Server returned error code: " + error.status + " searching vessels.", type: "error" });
            }
        });
    }
    
    embryo.authenticated(function() {
        setup();
        setInterval(setup, embryo.loadFrequence);
    });

    $("#zoomToYourShip").click(function() {
        embryo.vessel.goToVesselLocation(embryo.vessel.lookupVessel(yourShip.id));
    });
});
