// Vessel information + tracks for individual vessels
// Listens for vessel selected events.

embryo.vesselInformation = {
    renderYourShipShortTable: function (data) {
        if (!data.ais) return "<span class='label label-important'>AIS UNAVAILABLE</span>";
        var html = "";

        var egenskaber = {
            "MMSI": data.ais.mmsi,
            "Callsign": data.ais.callsign,
            "Country": data.ais.country,
            "Destination": data.ais.destination,
            "Nav status": data.ais.navStatus,
            "ETA": data.ais.eta
        }

        $.each(egenskaber, function(k,v) {
            if (v != null) html += "<tr><th>"+k+"</th><td>"+v+"</td></tr>";
        });

        return html;
    },
    renderSelectedShipShortTable: function (data) {
        if (!data.ais) return "<span class='label label-important'>AIS UNAVAILABLE</span>";
        var html = "";
        
        var egenskaber = {
            "MMSI": data.ais.mmsi,
            "Class": data.ais["class"],
            "Callsign": data.ais.callsign,
            "Cargo": data.ais.cargo,
            "Country": data.ais.country,
            "SOG": data.ais.sog,
            "COG": data.ais.cog,
            "Destination": data.ais.destination,
            "Nav status": data.ais.navStatus,
            "ETA": data.ais.eta
        }
        
        $.each(egenskaber, function(k,v) {
            if (v != null) html += "<tr><th>"+k+"</th><td>"+v+"</td></tr>";
        });

        return html;
    },
    showAesDialog: function (data) {
        if (!data.ais) return;

        var html = "";

        var link = "http://www.marinetraffic.com/ais/shipdetails.aspx?mmsi="+data.ais.mmsi;

        var egenskaber = {
            "MMSI": data.ais.mmsi,
            "Class": data.ais["class"],
            "Name": data.ais.name,
            "Callsign": data.ais.callsign,
            "Lat": data.ais.lat,
            "Lon": data.ais.lon,
            "IMO": data.ais.imo,
            "Source": data.ais.source,
            "Type": data.ais.type,
            "Cargo": data.ais.cargo,
            "Country": data.ais.country,
            "SOG": data.ais.sog,
            "COG": data.ais.cog,
            "Heading": data.ais.heading,
            "Draught": data.ais.draught,
            "ROT": data.ais.rot,
            "Width": data.ais.width,
            "Length": data.ais.length,
            "Destination": data.ais.destination,
            "Nav status": data.ais.navStatus,
            "ETA": data.ais.eta,
            "Pos acc": data.ais.posAcc,
            "Last report": data.ais.lastReport,
            "More information": "<a href='"+link+"' target='new_window'>"+link+"</a>"
        }
        
        $.each(egenskaber, function(k,v) {
            if (v != null) html += "<tr><th>"+k+"</th><td>"+v+"</td></tr>";
        });

        $("#aesModal h2").html("AIS Information - " + data.ais.name);
        $("#aesModal table").html(html);
        $("#aesModal").modal("show");
    }
}

$(function() {
    var shipSelected = false;

    function showVesselInformation(vesselOverview, vesselDetails) {
        openCollapse("#vcpSelectedShip");
        $("a[href=#vcpSelectedShip]").html("Selected Vessel - "+vesselOverview.name);
        $("#selectedAesInformation table").html(embryo.vesselInformation.renderSelectedShipShortTable(vesselDetails));
        $("#selectedAesInformationLink").off("click");
        $("#selectedAesInformationLink").on("click", function(e) {
            e.preventDefault();
            embryo.vesselInformation.showAesDialog(vesselDetails);
        });
    }
    
    embryo.vesselSelected(function(e) {
        shipSelected = true;

        $("a[href=#vcpSelectedShip]").html("Selected Vessel - loading data");
        // var messageId = embryo.messagePanel.show( { text: "Loading vessel data ..." })

        function setupAdditionalInformation(id, click) {
            $(id+" a").off("click");
            if (click != null) {
                $(id+" a").css("display", "block");
                $(id+" span").addClass("label-success");
                $(id+" span").html("AVAILABLE");
                $(id+" a").attr("href", "#");
                $(id+" a").on("click", click);
            } else {
                $(id+" a").css("display", "none");
                $(id+" span").removeClass("label-success");
                $(id+" span").html("NOT AVAILABLE");
            }
        }

        embryo.vessel.service.details(e.vesselId, function(error, data) {
            if (data) {
                if (shipSelected == false) return;

                var vessel = embryo.vessel.lookupVessel(e.vesselId);

                showVesselInformation(vessel, data);

                embryo.vessel.actions.setup("#selectedVesselActions", embryo.vessel.actions.selectedVessel, vessel, data);
            } else {
                embryo.messagePanel.show({ text: "Server returned error code: " + error.status + " loading vessel data.", type: "error" });
            }
        });
    });

    embryo.vesselUnselected(function() {
        shipSelected = false;
        closeCollapse("#vcpSelectedShip");
        $("a[href=#vcpSelectedShip]").html("Selected Vessel");
        embryo.vessel.actions.hide();
    });

    embryo.groupChanged(function(e) {
        if (e.groupId == "vessel") {
            $("#vesselControlPanel").css("display", "block");
            $("#vesselControlPanel .collapse").data("collapse", null)
            openCollapse("#vesselControlPanel .accordion-body:first");
        } else {
            $("#vesselControlPanel").css("display", "none");
            embryo.vessel.actions.hide();
        }
    });
});
