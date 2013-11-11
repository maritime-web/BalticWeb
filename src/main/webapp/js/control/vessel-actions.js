embryo.vessel.actions = {
    activeItem: null,
    activeItemRow: null,
    hide: function() {
        if (this.activeItem && this.activeItem.hide) this.activeItem.hide();
        if (this.activeItemRow) this.activeItemRow.removeClass("alert");
        this.activeItem = null;
        this.activeRow = null;
        $(".reportingPanel").css("display", "none");
    },
    markActiveItem: function() {
        if (this.activeItemRow) this.activeItemRow.addClass("alert");
    },
    setup: function(id, items, vesselOverview, vesselDetails) {
        var html = "";
        var inTable = false;
        for (var i in items) {
            var item = items[i];
            if (item) {
                switch (typeof(item)) {
                case "string":
                    if (inTable) {
                        inTable = false;
                        html += "</table>"
                    }
                    html += "<h4>" + item + "</h4>";
                    break;
                case "object":
                    if (!inTable) {
                        inTable = true;
                        html += "<table class='table table-condensed'>"
                    }

                    html += "<tr><th>"+item.title+"</th>";

                    var available = false;

                    if (item.available) {
                        available = item.available(vesselOverview, vesselDetails);
                    }

                    switch (available) {
                        case false:
                            html += "<td><span class='label'>NOT AVAILABLE</span></td><td></td>"
                            break;
                        case true:
                            html += "<td><span class='label label-success'>AVAILABLE</span></td><td><a href=# aid="+i+">view</a></td>"
                            break;
                        case "NOT OK":
                        case "INACTIVE":
                            html += "<td><span class='label'>"+available+"</span></td><td></td>"
                            break;
                        default:
                            html += "<td><span class='label label-success'>"+available+"</span></td><td><a href=# aid="+i+">view</a></td>"
                            break;
                    }

                    html += "</tr>";
                    break;
                }
            }
        }
        if (inTable) {
            html += "</table>"
        }
        $(id).html(html);
        this.markActiveItem();
        var that = this;
        $("a", id).click(function(e) {
            e.preventDefault();
            that.hide();
            that.activeItemRow = $(this).parents("tr");
            that.activeItem = items[$(this).attr("aid")];
            that.markActiveItem();
            that.activeItem.show(vesselOverview, vesselDetails);
        })
    }
}

embryo.ready(function() {
    $(".embryo-close-panel").click(function(e) {
        e.preventDefault();
        embryo.vessel.actions.hide();
    });
});

embryo.authenticated(function() {
    embryo.vessel.actions.yourVessel = [
        "ArcticWeb Reporting",
        embryo.controllers.vesselInformationEdit,
        embryo.controllers.schedule,
        embryo.controllers.greenpos,
        "Additional Information",
        embryo.additionalInformation.historicalTrack,
        embryo.additionalInformation.nearestShips,
        embryo.additionalInformation.distanceCircles,
        embryo.additionalInformation.metoc
    ];

    embryo.vessel.actions.selectedVessel = [
        "ArcticWeb Reporting",
        embryo.controllers.vesselInformationView,
        embryo.controllers.scheduleView,
        embryo.additionalInformation.route,
        (embryo.authentication.permissions.indexOf("GreenposList") >= 0) ? embryo.controllers.greenposListView : null,
        "Additional Information",
        embryo.additionalInformation.historicalTrack,
        embryo.additionalInformation.nearestShips,
        embryo.additionalInformation.distanceCircles
    ];
});
