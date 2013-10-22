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
    var lastRequestId = 0;
    var vessels;
    var vesselLayer = new VesselLayer();

    addLayerToMap("vessel", vesselLayer, embryo.map)

    embryo.vessel.lookupVessel = function(id) {
        for (var i in vessels) {
            if (vessels[i].id == id) return vessels[i];
        }
        return null;
    }

    embryo.vessel.allVessels = function() {
        return vessels;
    }

    embryo.vessel.searchVessels = function(argument, callback) {
        if (argument == null || argument == "") return [];

        var result = [];

        $.each(embryo.vessel.allVessels(), function (k,v) {
            if (v.vesselName) {
                if ((v.vesselName.toLowerCase().indexOf(argument.toLowerCase()) == 0) || 
                    (v.vesselName.toLowerCase().indexOf(" "+argument.toLowerCase()) >= 0)) {
                    result.push(v);
                }
            }
        })

        callback(result);
    }

    embryo.vessel.goToVesselLocation = function (vessel) {
        embryo.map.setCenter(vessel.lon, vessel.lat, focusZoom);
    }

    embryo.vessel.selectVessel = function (vessel) {
        vesselLayer.select(vessel.id);
    }

    embryo.vessel.setMarkedVessel = function(markedVesselId) {
        vesselLayer.markedVesselId = markedVesselId;
        // vesselLayer.draw(vessels);
    }

    var selectedId = null;

    vesselLayer.select(function (id) {
        if (selectedId != id && selectedId != null) embryo.eventbus.fireEvent(embryo.eventbus.VesselUnselectedEvent());
        if (id) embryo.eventbus.fireEvent(embryo.eventbus.VesselSelectedEvent(id));
        else embryo.eventbus.fireEvent(embryo.eventbus.VesselUnselectedEvent());
        selectedId = id;
    })

    function loadVesselList() {
        var messageId = embryo.messagePanel.show( { text: "Loading vessels ..." })

        $.ajax({
            url: embryo.baseUrl + "json_proxy/vessel_list",
            data: {
                requestId: lastRequestId
            },
            success: function (result) {
                embryo.messagePanel.replace(messageId, { text: result.vesselsInWorld + " vessels loaded.", type: "success" })

                if (result.requestId != lastRequestId) return;

                vessels = [];

                for (var i in result.vesselList.vessels) {
                    vessels.push(new Vessel(i, result.vesselList.vessels[i]));
                }

                vesselLayer.draw(vessels);
            },
            error: function(data) {
                embryo.messagePanel.replace(messageId, { text: "Server returned error code: " + data.status + " loading vessels.", type: "error" });
                console.log("Server returned error code: " + data.status + " loading vessels.");
            }
        });
    }

    embryo.mapInitialized(function() {
        setInterval(loadVesselList, embryo.loadFrequence);
        loadVesselList();
    });

    embryo.ready(function() {
        function fixAccordionSize() {
            $("#vesselControlPanel .accordion-inner").css("overflow", "auto");
            $("#vesselControlPanel .accordion-inner").css("max-height", Math.max(100, $(window).height() - 300)+"px");
        }

        $(window).resize(fixAccordionSize);

        fixAccordionSize();
    });
})
