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
            embryo.vessel.actions.hide();
            embryo.controllers.ais.show(vesselDetails.ais);
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
            embryo.vessel.actions.setup("#selectedVesselActions", embryo.vessel.actions.selectedVessel(), vessel, data);
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
        var groupId = e.groupId;
        var count = 0;

        function init(){
            if(count < 10 && ($('#vesselControlPanel').length == 0 || $('#vcpLegends').length == 0)){
                count++;
                setTimeout(init, 500);
                return;
            }
            
            if (groupId == "vessel") {
                $("#vesselControlPanel .controlPanel a").on('click', function(e) {
                    e.preventDefault();
                });
                if ($.inArray("Reporting", embryo.authentication.permissions) == -1) {
                    $("#vesselControlPanel #vcpGreenposList").parent().remove();
                }           
                
                $("#vesselControlPanel").css("display", "block");
                $("#vesselControlPanel .collapse").data("collapse", null);                
                openCollapse("#vesselControlPanel .e-accordion-body:first");
            } else {
                $("#vesselControlPanel").css("display", "none");
                embryo.vessel.actions.hide();
            }
        }
        
        init();
    });
});
