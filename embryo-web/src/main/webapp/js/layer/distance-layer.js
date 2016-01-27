function DistanceLayer() {
    this.init = function() {
        this.layers.lines = new OpenLayers.Layer.Vector("Vessel - Distance Layer - Line", {
            styleMap : new OpenLayers.StyleMap({
                "default" : new OpenLayers.Style({
                    fillColor : "#f80",
                    fillOpacity : 0.1,
                    strokeWidth : 2,
                    strokeColor : "#f80",
                    strokeOpacity : 0.7
                })
            })
        });

        this.layers.labels = new OpenLayers.Layer.Vector("Vessel - Distance Layer - Labels", {
            styleMap : new OpenLayers.StyleMap({
                "default" : new OpenLayers.Style({
                    label : "${label}",
                    fontColor : "black",
                    fontSize : "11px",
                    fontFamily : embryo.defaultFontFamily,
                    fontWeight : "normal",
                    labelAlign : "cm",
                    labelXOffset : "${labelXOffset}",
                    labelYOffset : "${labelYOffset}",
                    labelOutlineColor : "#fff",
                    labelOutlineWidth : 2,
                    labelOutline : 1
                })
            })
        });
    };

    this.containsDistanceCircle = function(vessel) {
        function featureFilter(feature) {
            return feature.attributes.id === vessel.mmsi && feature.attributes.type === 'circle';
        }
        return this.containsFeature(featureFilter, this.layers.lines);
    };

    this.containsNearestVessel = function(vessel) {
        function featureFilter(feature) {
            return feature.attributes.id === vessel.mmsi && feature.attributes.type === 'nearest';
        }
        return this.containsFeature(featureFilter, this.layers.lines);
    };

    this.removeDistanceCircle = function(vessel) {
        function featureFilter(feature) {
            return feature.attributes.id === vessel.mmsi && feature.attributes.type === 'circle';
        }
        return this.hideFeatures(featureFilter);
    };

    this.removeNearestVessel = function(vessel) {
        function featureFilter(feature) {
            return feature.attributes.id === vessel.mmsi && feature.attributes.type === 'nearest';
        }
        return this.hideFeatures(featureFilter);
    };

    this.drawDistanceCircles = function(vessel) {
        var attributes = {
            id: vessel.mmsi,
            type: 'circle'
        }
        this.layers.lines.addFeatures(embryo.adt.createRing(vessel.x, vessel.y, embryo.getMaxSpeed(vessel) * 3 * 1.852, 3, attributes), {
            id : vessel.mmsi,
            type : 'circle'
        });

        if (embryo.getMaxSpeed(vessel)) {
            var labelFeature = new OpenLayers.Feature.Vector(embryo.map.createPoint(vessel.x, vessel.y));
            // (parseFloat(vessel.x) + parseFloat(v.vessel.x)) / 2,
            // (parseFloat(vessel.y) + parseFloat(v.vessel.y)) / 2
            
            var maxSpeedLabel;
            if(vessel.awsog) {
            	maxSpeedLabel = "Based on ArcticWeb Max Speed: " + embryo.getMaxSpeed(vessel) + " kn";
            } else if (vessel.ssog) {
            	maxSpeedLabel = "Based on Service Speed: " + embryo.getMaxSpeed(vessel) + " kn";
            } else if (vessel.sog) {
                maxSpeedLabel = "Based on SOG: " + embryo.getMaxSpeed(vessel) + " kn";
            } else {
            	maxSpeedLabel = "No speed found."; 
            }
            	
            labelFeature.attributes = {
                id : vessel.mmsi,
                type : 'circle',
                label : maxSpeedLabel,
                labelXOffset : 140,
                labelYOffset : -15
            }
            this.layers.labels.addFeatures([ labelFeature ]);
        }
        
    };

    this.drawNearestVessels = function(selectedVessel, allVessels) {
        var vessels = [];

        $.each(allVessels, function(k, v) {
            if (v.mmsi != selectedVessel.mmsi) {
                if (embryo.getMaxSpeed(v) > 0.0) {
                    distance = embryo.adt.measureDistanceGc(selectedVessel.x, selectedVessel.y, v.x, v.y);
                    var o = {
                        distance : distance,
                        timeInMinutes : (distance / (embryo.getMaxSpeed(v) * 1.852) / 60),
                        vessel : v
                    }
                    if (o.distance > 0) {
                        vessels.push(o);
                    }
                }
            }
        });

        vessels.sort(function(a, b) {
            if (a.timeInMinutes == Infinity && b.timeInMinutes == Infinity) {
                return 0;
            }
            if (a.timeInMinutes == Infinity && b.timeInMinutes != Infinity) {
                return 100;
            }
            if (a.timeInMinutes != Infinity && b.timeInMinutes == Infinity) {
                return -100;
            }

            return a.timeInMinutes - b.timeInMinutes;
        });

        var getVesselPresentationName = function (toVessel) {
            var vessel = toVessel.vessel;

            var maxSpeed = embryo.getMaxSpeed(vessel);
            var maxSpeedLabel;
            if (vessel.awsog) {
                maxSpeedLabel = "AW Max Speed: " + maxSpeed + " kn";
            } else if (vessel.ssog) {
                maxSpeedLabel = "Service Speed: " + maxSpeed + " kn";
            } else if (vessel.sog) {
                maxSpeedLabel = "SOG: " + maxSpeed + " kn";
            }

            var name = vessel.name != undefined && vessel.name != null ? vessel.name : vessel.mmsi;

            var eta = "";
            if (maxSpeed != Infinity) {
                eta = ", " + formatHour(toVessel.distance / (maxSpeed * 1.852)) + " hours, " + maxSpeedLabel;
            }

            return name + ": " + formatNauticalMile(toVessel.distance) + eta;
        };

        for ( var i = 0; i < 5; i++) {
            var toVessel = vessels[i];

            this.layers.lines.addFeatures([ new OpenLayers.Feature.Vector(new OpenLayers.Geometry.LineString([
                    embryo.map.createPoint(selectedVessel.x, selectedVessel.y), embryo.map.createPoint(toVessel.vessel.x, toVessel.vessel.y) ]), {
                id : selectedVessel.mmsi,
                type : 'nearest'

            }) ]);

            var labelFeature = new OpenLayers.Feature.Vector(embryo.map.createPoint(toVessel.vessel.x, toVessel.vessel.y));

            labelFeature.attributes = {
                id : selectedVessel.mmsi,
                type : 'nearest',
                labelXOffset : 0,
                labelYOffset : -15,
                label : getVesselPresentationName(toVessel)
            };
            this.layers.labels.addFeatures([ labelFeature ]);
        }
    };
}

DistanceLayer.prototype = new EmbryoLayer();

/*
 * Can be used to create only one distance layer instance and reuse this as
 */
var DistanceLayerSingleton = {
    instance : null,
    getInstance : function() {
        if (this.instance == null) {
            this.instance = new DistanceLayer();
            addLayerToMap("vessel", this.instance, embryo.map);
        }
        return this.instance;
    }
}
