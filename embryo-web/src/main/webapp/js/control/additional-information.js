embryo.additionalInformation = {};

embryo.additionalInformation.historicalTrack = {
    title : "Historical Track",
    layer : null,
    init : function(map, group) {
        this.layer = new HistoricalTrackLayer();
        addLayerToMap(group, this.layer, map);
    },
    available : function(vessel, vesselDetails) {
        return vesselDetails.additionalInformation.historicalTrack;
    },
    show : function(vessel, vesselDetails) {
        var that = this;

        var messageId = embryo.messagePanel.show({
            text : "Loading historical track"
        });

        embryo.vessel.service.historicalTrack(vessel.mmsi, function(track) {
            if (track) {
                that.layer.draw(track, vessel.mmsi);
                that.layer.zoomToExtent();
            }
            var points = !track ? 0 : track.length;
            embryo.messagePanel.replace(messageId, {
                text : "Loaded historical track with " + points + " points.",
                type : "success"
            });
        }, function(errorMsg, status) {
            embryo.messagePanel.replace(messageId, {
                text : errorMsg,
                type : "error"
            });
        });
    },
    shown : function(vessel, vesselDetails) {
        if (vessel) {
            return this.layer && this.layer.containsFeature(vessel.mmsi);
        }
        return this.layer && this.layer.containsFeatures();
    },
    hide : function(vessel, vesselDetails) {
        this.layer.hideFeatures(vessel.mmsi);
    },
    hideAll : function() {
        this.layer.clear();
    }
};

embryo.additionalInformation.nearestShips = {
    title : "Nearest Vessels",
    layer : null,
    init : function(map, group) {
        this.layer = DistanceLayerSingleton.getInstance();
    },
    available : function(vessel, vesselDetails) {
        var vessels = embryo.vessel.allVessels();
        for (index in vessels) {
            if (vessels[index].msog) {
                return true;
            }
        }
        return false;
    },
    show : function(vessel, vesselDetails) {
        this.layer.drawNearestVessels(vessel, embryo.vessel.allVessels());
        this.layer.zoomToExtent();
    },
    hide : function(vessel, vesselDetails) {
        this.layer.removeNearestVessel(vessel);
    },
    shown : function(vessel, vesselDetails) {
        if (vessel) {
            return this.layer && this.layer.containsNearestVessel(vessel);
        }
        return this.layer && this.layer.containsFeatures();
    },
    hideAll : function() {
        this.layer.clear();
    }
};

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
        this.layer.drawDistanceCircles(vessel);
        this.layer.zoomToExtent();
    },
    hide : function(vessel, vesselDetails) {
        this.layer.removeDistanceCircle(vessel);
    },
    shown : function(vessel, vesselDetails) {
        if (vessel) {
            return this.layer && this.layer.containsDistanceCircle(vessel);
        }
        return this.layer && this.layer.containsFeatures();
    },
    hideAll : function() {
        this.layer.clear();
    }
};

embryo.additionalInformation.route = {
    title : "Route",
    layer : null,
    // new RouteLayer(),
    init : function(map, group) {
        this.layer = RouteLayerSingleton.getInstance();
        // addLayerToMap(group, this.layer, map)
    },
    available : function(vessel, vesselDetails) {
        return vesselDetails.additionalInformation.routeId != null;
    },
    show : function(vessel, vesselDetails) {
        var that = this;
        embryo.route.service.getRoute(vesselDetails.additionalInformation.routeId, function(data) {
            var routeType = embryo.route.service.getRouteType(vesselDetails.mmsi,
                    vesselDetails.additionalInformation.routeId);
            that.layer.draw(data, routeType);
            that.layer.zoomToExtent();
        });
    },
    hide : function() {
        this.layer.clear();
    },
    shown : function(vessel, vesselDetails) {
        if (vesselDetails) {
            return vesselDetails.additionalInformation.routeId != null && this.layer
                    && this.layer.containsFeature(vesselDetails.additionalInformation.routeId);
        }
        return this.layer && this.layer.containsFeatures();
    },
    hideAll : function() {
        this.layer.clear();
    }
};

embryo.mapInitialized(function() {
    $.each(embryo.additionalInformation, function(k, v) {
        if (v.init)
            v.init(embryo.map, "vessel");
    });
});
