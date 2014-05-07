// Vessel information + tracks for individual vessels
// Listens for vessel selected events.

embryo.vesselInformation = {
    renderYourShipShortTable: function (data) {
        if (!data.ais) return "<span class='label label-important'>AIS UNAVAILABLE</span>";
        var html = "";

        var egenskaber = {
            "MMSI": data.ais.mmsi,
            "Call Sign": data.ais.callsign,
            "Country": data.ais.country,
            "Destination": data.ais.destination,
            "Nav Status": data.ais.navStatus,
            "ETA": data.ais.eta
        };

        $.each(egenskaber, function(k,v) {
            if (v != null && v != "") html += "<tr><th>"+k+"</th><td>"+v+"</td></tr>";
        });

        return html;
    },
    renderSelectedShipShortTable: function (data) {
        if (!data.ais) return "<span class='label label-important'>AIS UNAVAILABLE</span>";
        var html = "";
        
        var egenskaber = {
            "MMSI": data.ais.mmsi,
            "Class": data.ais["class"],
            "Call Sign": data.ais.callsign,
            "Vessel Type": data.ais.vesselType,
            "Cargo": data.ais.cargo != "N/A" && data.ais.cargo != "Undefined" ? data.ais.cargo : null,
            "Country": data.ais.country,
            "SOG": data.ais.sog,
            "COG": data.ais.cog,
            "Destination": data.ais.destination,
            "Nav Status": data.ais.navStatus,
            "ETA": data.ais.eta
        };
        
        $.each(egenskaber, function(k,v) {
            if (v != null && v != "") html += "<tr><th>"+k+"</th><td>"+v+"</td></tr>";
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
            "Call Sign": data.ais.callsign,
            "Vessel Type": data.ais.vesselType,
            "Cargo": data.ais.cargo != "N/A" && data.ais.cargo != "Undefined" ? data.ais.cargo : null,
            "Lat": data.ais.lat,
            "Lon": data.ais.lon,
            "IMO": data.ais.imo,
            "Source": data.ais.source,
            "Type": data.ais.type,
            "Country": data.ais.country,
            "SOG": data.ais.sog,
            "COG": data.ais.cog,
            "Heading": data.ais.heading,
            "Draught": data.ais.draught,
            "ROT": data.ais.rot,
            "Width": data.ais.width,
            "Length": data.ais.length,
            "Destination": data.ais.destination,
            "Nav Status": data.ais.navStatus,
            "ETA": data.ais.eta,
            "Position Accuracy": data.ais.posAcc,
            "Last Report": data.ais.lastReport,
            "More Information": "<a href='"+link+"' target='new_window'>"+link+"</a>"
        };
        
        $.each(egenskaber, function(k,v) {
            if (v != null && v != "") html += "<tr><th>"+k+"</th><td>"+v+"</td></tr>";
        });

        $("#aesModal h2").html("AIS Information - " + data.ais.name);
        $("#aesModal table").html(html);
        $("#aesModal").modal("show");
    }
};

$(function() {
    var shipSelected = false;

    function showVesselInformation(vesselOverview, vesselDetails) {
        openCollapse("#vcpSelectedShip");
        $("a[href=#vcpSelectedShip]").html("Selected Vessel - "+vesselOverview.name);
        $("#selectedAesInformation table").html(embryo.vesselInformation.renderSelectedShipShortTable(vesselDetails));
        $("#selectedAesInformationLink").off("click");
        $("#selectedAesInformationLink").on("click", function(e) {
            e.preventDefault();
            embryo.controllers.ais.open(vesselDetails.ais);
        });
    }
    
    embryo.vesselSelected(function(e) {
        embryo.vessel.actions.hide();

        shipSelected = true;

        $("a[href=#vcpSelectedShip]").html("Selected Vessel - loading data");

        embryo.vessel.service.details(e.vesselId, function(data) {
            if (shipSelected == false) return;
            var vessel = embryo.vessel.lookupVessel(e.vesselId);
            showVesselInformation(vessel, data);
            embryo.vessel.actions.setup("#selectedVesselActions", embryo.vessel.actions.selectedVessel, vessel, data);
        }, function(errorMsg, status){
            embryo.messagePanel.show({ text: errorMsg, type: "error" });
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
            $("#vesselControlPanel .collapse").data("collapse", null);
            openCollapse("#vesselControlPanel .e-accordion-body:first");
        } else {
            $("#vesselControlPanel").css("display", "none");
            embryo.vessel.actions.hide();
        }
    });
});
