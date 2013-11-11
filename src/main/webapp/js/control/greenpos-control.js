(function() {
    "use strict";

    var greenposTypes = {
        "SP" : "Sailing Plan",
        "FR" : "Final",
        "PR" : "Position",
        "DR" : "Deviation"
    };

    function loadGreenposReports() {
        var html = "";
        embryo.greenpos.service.getLatest(function(greenposList) {
            $.each(greenposList, function(index, greenpos) {
                html += "<tr>";
                html += "<td>" + greenpos.name + "</td><td>" + greenposTypes[greenpos.type] + "</td>";
                html += "<td>" + formatTime(greenpos.ts) + "</td>";
                html += "<td><a id='" + greenpos.mmsi + "' href=#>view vessel</a></td>";
                html += "</tr>";
            });

            var tbody = $("#greenposTableBody");
            tbody.html(html);

            tbody.find("a").click(function(e) {
                e.preventDefault();
                var vessel = embryo.vessel.lookupVessel($(this).attr("id"));
                if (vessel) {
                    embryo.vessel.goToVesselLocation(vessel);
                    embryo.vessel.selectVessel(vessel);
                }
                
                $(this).parents("tbody").find("tr").removeClass("alert");
                $(this).parents("tr").addClass("alert");
            });

            embryo.vesselUnselected(function() {
                $("tr", "#greenposTableBody").removeClass("alert");
            })
        });
    }

    embryo.authenticated(function() {
        if (embryo.authentication.permissions.indexOf("GreenposList") >= 0) {
            loadGreenposReports();
        }
    });
}());
