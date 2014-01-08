function DistanceLayer() {
    this.init = function () {
        this.layers.lines = new OpenLayers.Layer.Vector("Vessel - Distance Layer - Line", {
            styleMap: new OpenLayers.StyleMap({
                "default": new OpenLayers.Style({
                    fillColor: "#f80",
                    fillOpacity: 0.1,
                    strokeWidth: 2,
                    strokeColor: "#f80",
                    strokeOpacity: 0.7
                })
            })
        });

        this.layers.labels = new OpenLayers.Layer.Vector("Vessel - Distance Layer - Labels", {
            styleMap: new OpenLayers.StyleMap({
                "default": new OpenLayers.Style({
                    label : "${label}",
                    fontColor : "black",
                    fontSize : "11px",
                    fontFamily : embryo.defaultFontFamily,
                    fontWeight : "normal",
                    labelAlign : "cm",
                    labelXOffset : 0,
                    labelYOffset : -15,
                    labelOutlineColor : "#fff",
                    labelOutlineWidth : 2,
                    labelOutline : 1
                })
            })
        });
    };
    
    this.drawDistanceCircles = function (vessel, vesselDetails) {
        this.layers.lines.removeAllFeatures();
        this.layers.labels.removeAllFeatures();
        this.layers.lines.addFeatures(embryo.adt.createRing(vessel.x, vessel.y, vesselDetails.ais.sog * 3 * 1.852, 3));
    };


    this.drawNearestVessels = function (vessel, vesselDetails, allVessels) {
        this.layers.lines.removeAllFeatures();
        this.layers.labels.removeAllFeatures();

        var vessels = [];

        $.each(allVessels, function (k,v) {
            if (v.mmsi != vessel.mmsi) {
                var o = {
                    distance : embryo.adt.measureDistanceGc(vessel.x, vessel.y, v.x, v.y),
                    vessel : v
                }
                if (o.distance > 0) {
                    vessels.push(o);
                }
            }
        });

        vessels.sort(function(a, b) {
            return a.distance - b.distance;
        });

        for (var i = 0; i < 5; i++) {
            var v = vessels[i];

            this.layers.lines.addFeatures([new OpenLayers.Feature.Vector(new OpenLayers.Geometry.LineString([
                embryo.map.createPoint(vessel.x, vessel.y), embryo.map.createPoint(v.vessel.x, v.vessel.y)
            ]))]);

            var labelFeature = new OpenLayers.Feature.Vector(embryo.map.createPoint(
                v.vessel.x, v.vessel.y
                // (parseFloat(vessel.x) + parseFloat(v.vessel.x)) / 2, (parseFloat(vessel.y) + parseFloat(v.vessel.y)) / 2
            ));

            if (vesselDetails.ais.sog > 0) {
                labelFeature.attributes = {
                    label: formatNauticalMile(v.distance) + " " + formatHour(v.distance / (vesselDetails.ais.sog * 1.852))+ " hours"
                }
            } else {
                labelFeature.attributes = {
                    label: formatNauticalMile(v.distance)
                }
            }


            this.layers.labels.addFeatures([
                labelFeature
            ]);
        }
    };
}

DistanceLayer.prototype = new EmbryoLayer();

/*
 * Can be used to create only one distance layer instance and reuse this as  
 */
var DistanceLayerSingleton = {
    instance : null,
    getInstance : function(){
        if(this.instance == null){
            this.instance = new DistanceLayer();
            addLayerToMap("vessel", this.instance, embryo.map);
        }
        return this.instance;
    }
}
