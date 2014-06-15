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
        this.layers.lines.addFeatures(embryo.adt.createRing(vessel.x, vessel.y, vessel.msog * 3 * 1.852, 3,
                vessel.mmsi, 'circle'), {
            id : vessel.mmsi,
            type : 'circle'
        });

        if (vessel.msog) {
            var labelFeature = new OpenLayers.Feature.Vector(embryo.map.createPoint(vessel.x, vessel.y));
            // (parseFloat(vessel.x) + parseFloat(v.vessel.x)) / 2,
            // (parseFloat(vessel.y) + parseFloat(v.vessel.y)) / 2
            labelFeature.attributes = {
                id : vessel.mmsi,
                type : 'circle',
                label : "Based on maximum recorded SOG: " + vessel.msog + " kn",
                labelXOffset : 140,
                labelYOffset : -15
            }
            this.layers.labels.addFeatures([ labelFeature ]);
        }
    };

    this.drawNearestVessels = function(vessel, allVessels) {
        var vessels = [];

        $.each(allVessels, function(k, v) {
            if (v.mmsi != vessel.mmsi) {
                if (v.msog) {
                    distance = embryo.adt.measureDistanceGc(vessel.x, vessel.y, v.x, v.y);
                    var o = {
                        distance : distance,
                        timeInMinutes : (distance / (v.msog * 1.852) / 60),
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

        // function getVesselsWithYOffsets() {
        // var yCoords = [];
        // for ( var i = 0; i < 5; i++) {
        //                
        // embryo.map.internalMap.getViewPortPxFromLonLat();
        //                
        // yCoords.push({
        // y : vessels[i].vessel.y,
        // vessel : vessels[i].vessel
        // });
        // }
        // yCoords.sort(function(coord1, coord2){
        // return coord1.y - coord2.y;
        // });
        //            
        //
        // for ( var i = 1; i < 5; i++) {
        // var diff = yCoords[i-1].y - yCoords[i].y;
        // if(diff <= 16){
        // // for ( var j = i; j < 5; j++) {
        // // yCoords[j].y += 15;
        // // }
        // }
        // }
        // }
        //        
        // var vesselsWithYOffsets = getVesselsWithYOffsets();

        for ( var i = 0; i < 5; i++) {
            var v = vessels[i];

            this.layers.lines.addFeatures([ new OpenLayers.Feature.Vector(new OpenLayers.Geometry.LineString([
                    embryo.map.createPoint(vessel.x, vessel.y), embryo.map.createPoint(v.vessel.x, v.vessel.y) ]), {
                id : vessel.mmsi,
                type : 'nearest'

            }) ]);

            var labelFeature = new OpenLayers.Feature.Vector(embryo.map.createPoint(v.vessel.x, v.vessel.y));
            labelFeature.attributes = {
                id : vessel.mmsi,
                type : 'nearest',
                labelXOffset : 0,
                labelYOffset : -15,
                label : v.vessel.name
                        + ": "
                        + formatNauticalMile(v.distance)
                        + (v.vessel.msog == Infinity ? "" : ", " + formatHour(v.distance / (v.vessel.msog * 1.852))
                                + " hours, Max SOG " + v.vessel.msog + " kn")
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
