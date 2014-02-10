embryo.additionalInformation = {}

embryo.additionalInformation.historicalTrack = {
    doShow: false,
    title : "Historical Track",
    layer : new HistoricalTrackLayer(),
    init : function(map, group) {
        addLayerToMap(group, this.layer, map)
    },
    available : function(vessel, vesselDetails) {
        return vesselDetails.additionalInformation.historicalTrack;
    },
    show : function(vessel, vesselDetails) {
        var that = this;
        this.doShow = true;
        embryo.vessel.service.historicalTrack(vessel.mmsi, function(error, data) {
            if (data && that.doShow) {
                that.layer.draw(data);
                that.layer.zoomToExtent();
            } else {
                embryo.logger.log("unhandled error", error);
            }
        })
    },
    hide : function() {
        this.doShow = false;
        this.layer.clear();
    }
}

embryo.additionalInformation.nearestShips = {
    title : "Nearest Vessels",
    layer : null,
    init : function(map, group) {
        this.layer = DistanceLayerSingleton.getInstance();
    },
    available : function(vessel, vesselDetails) {
        return vesselDetails.ais != null;
    },
    show : function(vessel, vesselDetails) {
        this.layer.drawNearestVessels(vessel, vesselDetails, embryo.vessel.allVessels());
        this.layer.zoomToExtent();
    },
    hide : function() {
        this.layer.clear();
    }
}

embryo.additionalInformation.distanceCircles = {
    title : "3-6-9 hour distance circle based on SOG",
    layer : null,
    init : function(map, group) {
        this.layer = DistanceLayerSingleton.getInstance();
    },
    available : function(vessel, vesselDetails) {
        return vessel.msog > 0;
    },
    show : function(vessel, vesselDetails) {
        this.layer.drawDistanceCircles(vessel, vesselDetails);
        this.layer.zoomToExtent();
    },
    hide : function() {
        this.layer.clear();
    }
}

embryo.additionalInformation.route = {
    title : "Route",
    layer : null,
    //new RouteLayer(),
    init : function(map, group) {
        this.layer = RouteLayerSingleton.getInstance();
//        addLayerToMap(group, this.layer, map)
    },
    available : function(vessel, vesselDetails) {
        return vesselDetails.additionalInformation.routeId != null;
    },
    show : function(vessel, vesselDetails) {
        var that = this;
        embryo.route.service.getRoute(vesselDetails.additionalInformation.routeId, function(data) {
            var routeType = embryo.route.service.getRouteType(vesselDetails.mmsi, vesselDetails.additionalInformation.routeId); 
            that.layer.draw(data , routeType);
            that.layer.zoomToExtent();
        });
    },
    hide : function() {
        this.layer.clear();
    }
}

//embryo.additionalInformation.metoc = {
//    title : "METOC on Route",
//    layer : new MetocLayer(),
//    init : function(map, group) {
//        addLayerToMap(group, this.layer, map)
//    },
//    available : function(vessel, vesselDetails) {
//        return vesselDetails.additionalInformation.routeId != null;
//    },
//    show : function(vessel, vesselDetails) {
//        var that = this;
//        embryo.metoc.service.getMetoc(vesselDetails.additionalInformation.routeId, function(metoc) {
//            that.layer.draw(metoc);
//            that.layer.zoomToExtent();
//        });
//    },
//    hide: function() {
//        this.layer.clear();
//    }
//}

embryo.mapInitialized(function() {
    $.each(embryo.additionalInformation, function(k, v) {
        if (v.init) v.init(embryo.map, "vessel");
    });
});
