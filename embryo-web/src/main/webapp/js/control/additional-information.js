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
        	
            if (embryo.getMaxSpeed(vessels[index])) {
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
        return embryo.getMaxSpeed(vessel) > 0;
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

function routeFeatureFilter(feature){
    if(feature.attributes.featureType === "route"){
        return !feature.attributes.data.active || !feature.attributes.data.own;
    }
    return true;
}

embryo.additionalInformation.route = {
    title : "Route",
    layer : null,
    // new RouteLayer(),
    init : function(map, group) {
        this.layer = RouteLayerSingleton.getInstance();
    },
    available : function(vessel, vesselDetails) {
        return vesselDetails.additionalInformation.routeId != null;
    },
    show : function(vessel, vesselDetails) {
        var that = this;
        embryo.route.service.getRoute(vesselDetails.additionalInformation.routeId, function(route) {
            route.active = vesselDetails.additionalInformation.routeId == route.id;
            route.own = embryo.authentication.shipMmsi == vesselDetails.mmsi;
            that.layer.draw([ route ]);
            that.layer.zoomToExtent();
        });
    },
    hide : function() {
        if (this.routeId) {
            this.layer.hideFeatures(this.routeId);
        }
    },
    shown : function(vessel, vesselDetails) {
        if (vesselDetails) {
            this.routeId = vesselDetails.additionalInformation.routeId ? vesselDetails.additionalInformation.routeId
                    : null;
            return this.routeId != null && this.layer && this.layer.containsFeature(this.routeId);
        }
        
        return this.layer && this.layer.containsFeature(routeFeatureFilter);
    },
    hideAll : function() {
        this.layer.hideFeatures(routeFeatureFilter);
    }
};

embryo.postLayerInitialization(function() {
    $.each(embryo.additionalInformation, function(k, v) {
        if (v.init)
            v.init(embryo.map, "vessel");
    });
});
