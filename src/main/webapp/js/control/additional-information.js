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
        return vesselDetails.additionalInformation.historicalTrack;
    },
    show: function (vessel, vesselDetails) {
        var that = this;
        embryo.vessel.service.historicalTrack(vessel.mmsi, function(error, data) {
            if (data) {
                that.layer.draw(data);
                that.layer.zoomToExtent();
            } else {
                console.log("unhandled error", error);
            }
        })
    },
    hide: function (vessel, vesselDetails) {
        this.layer.clear();
    }
}

embryo.additionalInformation.nearestShips = {
    title: "Nearest Vessels",
    showAt: [ "YourShip", "SelectedShip" ],
    layer: new NearestVesselsLayer(),
    init: function (map, group) {
        addLayerToMap(group, this.layer, map)
    },
    available: function (vessel, vesselDetails) {
        return vesselDetails.ais;
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
        return vesselDetails.ais && vesselDetails.ais.sog > 0;
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
        return vesselDetails.additionalInformation.routeId;
    },
    show: function (vessel, vesselDetails) {
        var that = this;
        embryo.route.service.getRoute(vesselDetails.additionalInformation.routeId, function(data) {
            that.layer.draw(data);
            that.layer.zoomToExtent();
        });
    }
}

embryo.additionalInformation.metoc = {
    title: "METOC",
    showAt: [ "YourShip" ],
    layer: new MetocLayer(),
    available: function (vessel, vesselDetails) {
        return vesselDetails.additionalInformation.routeId;
    },
    show: function (vessel, vesselDetails) {
        var that = this;
        embryo.metoc.service.getMetoc(vesselDetails.additionalInformation.routeId, function(metoc) {
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
    $.each(embryo.controllers, function (k, v) {
        v.hide();
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

embryo.ready(function() {
    $(".embryo-close-panel").click(function(e) {
        e.preventDefault();
        clearAdditionalInformation();
    });
});
