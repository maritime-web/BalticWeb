// Vessel information + tracks for individual vessels
// Listens for vessel selected events.

embryo.vesselInformation = {
    mmsi : null,    
        
    renderYourShipShortTable: function (data) {
        var html = "";

        var egenskaber = {
            "MMSI": data.mmsi,
            "Callsign": data.callsign,
            "Country": data.country,
            "Destination": data.destination,
            "Nav status": data.navStatus,
            "ETA": data.eta
        }

        $.each(egenskaber, function(k,v) {
            if (v != null) html += "<tr><th>"+k+"</th><td>"+v+"</td></tr>";
        });

        return html;
    },
    renderSelectedShipShortTable: function (data) {
        var html = "";
        
        var egenskaber = {
            "MMSI": data.mmsi,
            "Class": data["class"],
            "Callsign": data.callsign,
            "Cargo": data.cargo,
            "Country": data.country,
            "SOG": data.sog,
            "COG": data.cog,
            "Destination": data.destination,
            "Nav status": data.navStatus,
            "ETA": data.eta
        }
        
        $.each(egenskaber, function(k,v) {
            if (v != null) html += "<tr><th>"+k+"</th><td>"+v+"</td></tr>";
        });

        return html;
    },
    showAesDialog: function (data) {
        embryo.vesselInformation.mmsi = data.mmsi;
        
        var html = "";

        var link = "http://www.marinetraffic.com/ais/shipdetails.aspx?mmsi="+data.mmsi;

        var egenskaber = {
            "MMSI": data.mmsi,
            "Class": data["class"],
            "Name": data.name,
            "Callsign": data.callsign,
            "Lat": data.lat,
            "Lon": data.lon,
            "IMO": data.imo,
            "Source": data.source,
            "Type": data.type,
            "Cargo": data.cargo,
            "Country": data.country,
            "SOG": data.sog,
            "COG": data.cog,
            "Heading": data.heading,
            "Draught": data.draught,
            "ROT": data.rot,
            "Width": data.width,
            "Length": data.length,
            "Destination": data.destination,
            "Nav status": data.navStatus,
            "ETA": data.eta,
            "Pos acc": data.posAcc,
            "Last report": data.lastReport,
            "More information": "<a href='"+link+"' target='new_window'>"+link+"</a>"
        }
        
        $.each(egenskaber, function(k,v) {
            if (v != null) html += "<tr><th>"+k+"</th><td>"+v+"</td></tr>";
        });

        $("#aesModal h2").html("AIS Information - " + data.name);
        $("#aesModal table").html(html);
        $("#aesModal").modal("show");
    }
}

$(function() {
    var shipSelected = false;

    function showVesselInformation(data) {
        openCollapse("#vcpSelectedShip");
        $("a[href=#vcpSelectedShip]").html("Selected Ship - "+data.name);
        $("#selectedAesInformation table").html(embryo.vesselInformation.renderSelectedShipShortTable(data));
        $("#selectedAesInformationLink").off("click");
        $("#selectedAesInformationLink").on("click", function(e) {
            e.preventDefault();
            embryo.vesselInformation.showAesDialog(data);
        });
    }
    

    embryo.vesselSelected(function(e) {
        shipSelected = true;

        $("a[href=#vcpSelectedShip]").html("Selected Ship - loading data");
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

                showVesselInformation(data);

                var vessel = embryo.vessel.lookupVessel(e.vesselId);

                setupAdditionalInformationTable("#selectedShipAdditionalInformation", vessel, data, "SelectedShip");
            } else {
                embryo.messagePanel.show({ text: "Server returned error code: " + error.status + " loading vessel data.", type: "error" });
            }
        });
    });

    embryo.vesselUnselected(function() {
        shipSelected = false;
        closeCollapse("#vcpSelectedShip");
        $("a[href=#vcpSelectedShip]").html("Selected Ship");
        clearAdditionalInformation();
    });

    embryo.groupChanged(function(e) {
        if (e.groupId == "vessel") {
            $("#vesselControlPanel").css("display", "block");
            $("#vesselControlPanel .collapse").data("collapse", null)
            openCollapse("#vesselControlPanel .accordion-body:first");
        } else {
            $("#vesselControlPanel").css("display", "none");
            clearAdditionalInformation();
        }
    });

    initAdditionalInformation(embryo.map, "vessel");
});
