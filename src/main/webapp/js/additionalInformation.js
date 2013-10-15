embryo.additionalInformation = {
}

embryo.additionalInformation.historicalTrack = {
    title: "Historical Track",
    showAt: [ "YourShip", "SelectedShip" ],
    layer: new HistoricalTrackLayer(),
    init: function (map, group) {
        addLayerToMap(group, this.layer, map)
    },
    available: function (vessel, vesselDetails) {
        return vesselDetails.pastTrack && vesselDetails.pastTrack.points.length > 5;
    },
    show: function (vessel, vesselDetails) {
        this.layer.draw(vesselDetails.pastTrack.points);
        this.layer.zoomToExtent();
    },
    hide: function (vessel, vesselDetails) {
        this.layer.clear();
    }
}

embryo.additionalInformation.nearestShips = {
    title: "Nearest Ships",
    showAt: [ "YourShip", "SelectedShip" ],
    layer: new NearestVesselsLayer(),
    init: function (map, group) {
        addLayerToMap(group, this.layer, map)
    },
    available: function (vessel, vesselDetails) {
        return true;
    },
    show: function (vessel, vesselDetails) {
        this.layer.draw(vessel, vesselDetails, embryo.vessel.allVessels());
        this.layer.zoomToExtent();
    },
    hide: function (vessel, vesselDetails) {
        this.layer.clear();
    }
}

embryo.additionalInformation.distanceCircles = {
    title: "3-6-9 hour distance circle based on SOG",
    showAt: [ "YourShip", "SelectedShip" ],
    layer: new DistanceCirclesLayer(),
    available: function (vessel, vesselDetails) {
        return vesselDetails.sog > 0;
    },
    show: function (vessel, vesselDetails) {
        this.layer.draw(vessel, vesselDetails);
        this.layer.zoomToExtent();
    }
}

embryo.additionalInformation.route = {
    title: "Route",
    showAt: [ "SelectedShip" ],
    layer: new RouteLayer("#D5672D"),
    available: function (vessel, vesselDetails) {
        console.log("Checking route")
        return vesselDetails.route != null;
    },
    show: function (vessel, vesselDetails) {
        this.layer.draw(vesselDetails.route);
        this.layer.zoomToExtent();
    }
}

embryo.additionalInformation.metoc = {
    title: "METOC",
    showAt: [ "YourShip" ],
    layer: new MetocLayer("#D5672D"),
    available: function (vessel, vesselDetails) {
        console.log("checking metoc", vessel, vesselDetails)
        return vesselDetails.route != null;
    },
    show: function (vessel, vesselDetails) {
        var that = this;
        embryo.metocService.getMetoc(vesselDetails.route.id, function(metoc) {
            console.log(metoc);
            that.layer.draw(metoc);
            that.layer.zoomToExtent();
        });
    }
}

function initAdditionalInformation(map, group) {
    $.each(embryo.additionalInformation, function (k, v) {
        addLayerToMap(group, v.layer, map)
    });
}

function clearAdditionalInformation() {
    $.each(embryo.additionalInformation, function (k, v) {
        v.layer.clear();
    });
    $(".additional-information tr").removeClass("alert");
}

function setupAdditionalInformationTable(id, vessel, vesselDetails, page) {
    var html = "";
    $.each(embryo.additionalInformation, function (k, v) {
        if (v.showAt.indexOf(page) >= 0) {
            var available = v.available(vessel, vesselDetails);
            html += "<tr><th>"+v.title+"</th>";

            if (available) {
                html += "<td><span class='label label-success'>AVAILABLE</span></td><td><a href=# aid="+k+">view</a></td>"
            } else {
                html += "<td><span class='label'>NOT AVAILABLE</span></td><td></td>"
            }

            html += "</tr>"
        }
    });

    $(id).html(html);

    $("a", $(id)).click(function (e) {
        e.preventDefault();
        clearAdditionalInformation();
        embryo.additionalInformation[$(this).attr("aid")].show(vessel, vesselDetails);
        $(this).parents("tr").addClass("alert");
    })
}
