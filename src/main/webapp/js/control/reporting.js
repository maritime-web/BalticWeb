embryo.reporting = {

}

embryo.controllers = {}

embryo.reporting.vesselInformation = {
    title : "Vessel Information",
    status : function(vesselOverview, vesselDetails) {
        return "OK";
    },
    show : function(vesselOverview, vesselDetails) {
        $("#vesselInformationPanel").css("display", "block");
        $("#maxSpeed").focus();
    },
    hide : function() {
        $("#vesselInformationPanel").css("display", "none");
    }
}

embryo.reporting.schedule = {
    title : "Schedule",
    status : function(vesselOverview, vesselDetails) {
        return "OK";
    },
    show : function(vesselOverview, vesselDetails) {
        embryo.ScheduleCtrl.show(vesselDetails);
        $("#schedulePanel").css("display", "block");
    },
    hide : function() {
        $("#schedulePanel").css("display", "none");
    }
}

embryo.reporting.route = {
    title : "Active Route",
    status : function(vesselOverview, vesselDetails) {
        return "OK";
    },
    show : function(vesselOverview, vesselDetails) {
        $("#routeUploadPanel").css("display", "block");
    },
    hide : function() {
        $("#routeUploadPanel").css("display", "none");
    }
}

embryo.reporting.editRoute = {
    title : "Edit Route",
    status : function(vesselOverview, vesselDetails) {
        return "OK";
    },
    show : function(vesselOverview, vesselDetails) {
        $("#routeEditPanel").css("display", "block");
    },
    hide : function() {
        $("#routeEditPanel").css("display", "none");
    }
}

embryo.reporting.greenposReport = {
    title : "Greenpos Report",
    status : function(vesselOverview, vesselDetails) {
        return "OK";
    },
    show : function(vesselOverview, vesselDetails) {
        $("#greenposReportPanel").css("display", "block");
    },
    hide : function() {
        $("#greenposReportPanel").css("display", "none");
        embryo.controllers.greenpos.hide();
    }
}

function setupReporting(id, vessel, vesselDetails) {
    var html = "";
    $.each(embryo.controllers, function(k, v) {
        if (v.title) {
            html += "<tr><th>" + v.title + "</th>";
            var status = v.status(vessel, vesselDetails)
            var label = "";
            if (status.code) {
                label = "label-" + status.code;
            }

            html += "<td><span class='label " + label + "'>" + status.message + "</span></td>";
            html += "<td><a href=# aid=" + k + ">edit</a></td>";

            html += "</tr>";
        }

    });

    $(id).html(html);

    $("a", $(id)).click(function(e) {
        e.preventDefault();
        clearAdditionalInformation();
        embryo.controllers[$(this).attr("aid")].show({
            vesselOverview : vessel,
            vesselDetails : vesselDetails
        });
        $(this).parents("tr").addClass("alert");
    })
}
