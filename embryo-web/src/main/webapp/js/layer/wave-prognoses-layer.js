function WavePrognosesLayer() {

    OpenLayers.Strategy.RuleCluster = OpenLayers.Class(OpenLayers.Strategy.Cluster, {
        /**
         * the rule to use for comparison
         */
        rule : null,
        /**
         * Method: shouldCluster Determine whether to include a feature in a
         * given cluster.
         * 
         * Parameters: cluster - {<OpenLayers.Feature.Vector>} A cluster.
         * feature - {<OpenLayers.Feature.Vector>} A feature.
         * 
         * Returns: {Boolean} The feature should be included in the cluster.
         */
        shouldCluster : function(cluster, feature) {
            var superProto = OpenLayers.Strategy.Cluster.prototype;
            return this.rule.evaluate(cluster.cluster[0]) && this.rule.evaluate(feature) && superProto.shouldCluster.apply(this, arguments);
        },
        CLASS_NAME : "OpenLayers.Strategy.RuleCluster"
    });

    this.init = function() {
        this.zoomLevels = [ 4, 6, 11 ];

        var that = this;

        this.context = {
            transparency : function() {
                return that.active ? 0.5 : 0.25;
            },
            size : function(feature) {
                return [ 16, 20, 24, 24 ][that.zoomLevel];
            },
            offset : function() {
                return -that.context.size() / 2;
            },
            zoomDependentDescription : function(feature) {
                if (that.zoomLevel < 3) {
                    return "";
                }
                return that.context.description(feature);
            },
            description : function(feature) {
                return feature.cluster ? feature.cluster.length + " Wave prognoses locations" : feature.attributes.iceDescription.Number + ": "
                        + feature.attributes.iceDescription.Placename;
            },
            display : function(feature) {
                return "yes";
            }
        };

        this.layers.waveprognoses = new OpenLayers.Layer.Vector("WavePrognoses", {
            styleMap : new OpenLayers.StyleMap({
                "default" : new OpenLayers.Style({
                    // externalGraphic : "img/inshoreIceReport.png",
                    graphicOpacity : "${transparency}",
                    graphicWidth : '${size}',
                    graphicHeight : '${size}',
                    graphicYOffset : "${offset}",
                    graphicXOffset : "${offset}",
                    fillColor : '${level}',
                    strokeWidth : 1,
                    strokeColor : '#333333',
                    strokeOpacity : 1,
                    fontColor : "#000000",
                    fontSize : "10px",
                    fontFamily : "Courier New, monospace",
                    // label : "${obs}",
                    fontOpacity : 1,
                    fontWeight : "bold",
                    labelOutlineWidth : 0,
                    labelYOffset : -20
                }, {
                    context : this.context
                }),
                "select" : new OpenLayers.Style({
                    // externalGraphic : "img/inshoreIceReport.png",
                    graphicOpacity : 1,
                    graphicWidth : 24,
                    graphicHeight : 24,
                    graphicXOffset : -12,
                    graphicYOffset : -12,
                    backgroundGraphic : "img/ring.png",
                    backgroundXOffset : -16,
                    backgroundYOffset : -16,
                    backgroundHeight : 32,
                    backgroundWidth : 32,
                    fontOpacity : 1,
                    fontColor : "#000",
                    fontSize : "10px",
                    fontFamily : "Courier New, monospace",
                    label : "${time} - ${obs}",
                    fontWeight : "bold",
                    labelOutlineWidth : 0,
                    labelYOffset : -20,
                    display : "${display}",
                }, {
                    context : this.context
                })
            })
        });

        this.selectableLayers = [ this.layers.waveprognoses ];
        this.selectableAttribute = "number";
    };

    this.getConcentrationLevel = function(obs) {
        if(obs < 0.7) {
            return '#669999';
        } else if(obs < 1.0) {
            return '#996666';
        } else if(obs < 1.3) {
            return '#cc3333';
        } else {
            return '#ff0000';
        }
    };
    
    this.drawFeature = function(lat, lon, obs, vars) {
        var half = 0.2;
        if (obs && obs[0]) {
            var level = this.getConcentrationLevel(obs[vars['Significant wave height']]);
            var points = new Array(embryo.map.createPoint(lon, lat + half), embryo.map.createPoint(lon + half, lat + (half * 0.2)), embryo.map.createPoint(
                    lon + (half * 0.2), lat + (half * 0.2)), embryo.map.createPoint(lon + (half * 0.2), lat - (half * 0.5)), embryo.map.createPoint(lon - (half * 0.2), lat - (half * 0.5)), embryo.map.createPoint(
                    lon - (half * 0.2), lat + (half * 0.2)), embryo.map.createPoint(lon - half, lat + (half * 0.2)));
            var linearRing = new OpenLayers.Geometry.LinearRing(points);
            linearRing.rotate(obs[vars['Wave direction']], embryo.map.createPoint(lon, lat));
            var feature = new OpenLayers.Feature.Vector(linearRing, {level: level});
            console.log('Sign. wave height: ' + obs[vars['Significant wave height']]);
            return feature;
        }
        return null;
    };


    this.draw = function(prognosis, time) {
        var that = this;
        that.clear();

        var vars = prognosis.variables;
        var lats = prognosis.metadata.lat;
        var lons = prognosis.metadata.lon;
        var features = [];

      
        for(var d in prognosis.data) {
            var data = prognosis.data[d];
            if(time == data.time) {
                var feature = this.drawFeature(lats[data.lat], lons[data.lon], data.obs, vars);
                if(feature) {
                    features.push(feature);
                }
            }
        }

        that.layers.waveprognoses.addFeatures(features);
        that.layers.waveprognoses.refresh();

    };

}

WavePrognosesLayer.prototype = new EmbryoLayer();
