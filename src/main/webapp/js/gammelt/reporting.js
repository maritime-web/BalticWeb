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
