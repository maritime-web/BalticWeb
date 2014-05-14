embryo.vessel = {};

embryo.eventbus.VesselSelectedEvent = function(id) {
    var event = jQuery.Event("VesselSelectedEvent");
    event.vesselId = id;
    return event;
};

embryo.eventbus.VesselUnselectedEvent = function() {
    var event = jQuery.Event("VesselUnselectedEvent");
    return event;
};

embryo.eventbus.registerShorthand(embryo.eventbus.VesselSelectedEvent, "vesselSelected");
embryo.eventbus.registerShorthand(embryo.eventbus.VesselUnselectedEvent, "vesselUnselected");

$(function() {
    var vessels = null;
    var vesselLayer = new VesselLayer();

    addLayerToMap("vessel", vesselLayer, embryo.map);

    embryo.vessel.lookupVessel = function(id) {
        for (var i in vessels) {
            if (vessels[i].mmsi == id) return vessels[i];
        }
        return null;
    };

    embryo.vessel.allVessels = function() {
        return vessels;
    };

    embryo.vessel.goToVesselLocation = function (vessel) {
        if (vessel.type != null) embryo.map.setCenter(vessel.x, vessel.y, 8);
    };

    embryo.vessel.selectVessel = function (vessel) {
        if (vessel.type) {
            vesselLayer.select(vessel.mmsi);
        } else {
            vesselLayer.select(null);
            embryo.eventbus.fireEvent(embryo.eventbus.VesselSelectedEvent(vessel.mmsi));
        }
    };

    embryo.vessel.setMarkedVessel = function(markedVesselId) {
        vesselLayer.markedVesselId = markedVesselId;
        vesselLayer.draw(vessels);
    };

    var selectedId = null;

    vesselLayer.select(function (id) {
        if (selectedId != id && selectedId != null) embryo.eventbus.fireEvent(embryo.eventbus.VesselUnselectedEvent());
        if (id) embryo.eventbus.fireEvent(embryo.eventbus.VesselSelectedEvent(id));
        else embryo.eventbus.fireEvent(embryo.eventbus.VesselUnselectedEvent());
        selectedId = id;
    });

    function loadVesselList() {
        var messageId = embryo.messagePanel.show( { text: "Loading vessels ..." });

        embryo.vessel.service.list(function(data) {
            vessels = data;
            embryo.messagePanel.replace(messageId, { text: vessels.length + " vessels loaded.", type: "success" });
            vesselLayer.draw(vessels);
        }, function(errorMsg, status){
            embryo.messagePanel.replace(messageId, { text: errorMsg, type: "error" });
        });
    }

    embryo.mapInitialized(function() {
        setInterval(loadVesselList, embryo.loadFrequence);
        loadVesselList();
    });

    embryo.authenticated(function() {
        
        if (!$.inArray("Reporting", embryo.authentication.permissions)) {
            $("#vcpGreenposList").parent().remove();
        }

        function fixAccordionSize() {
            $("#vesselControlPanel .e-accordion-inner").css("overflow", "auto");
            $("#vesselControlPanel .e-accordion-inner").css("max-height", Math.max(100, $(window).height() - 233) + "px");
        }

        $(window).resize(fixAccordionSize);

        setTimeout(fixAccordionSize, 100);
    });
});

embryo.ready(function() {
    function fixReportingPanelSize() {
        $(".reportingPanel").css("overflow", "auto");
        $(".reportingPanel").css("max-height", Math.max(100, $("#map").height() - 100)+"px");
    }

    $(window).resize(fixReportingPanelSize);

    setTimeout(fixReportingPanelSize, 100);
});
