function InshoreIceReportLayer() {

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
            return this.rule.evaluate(cluster.cluster[0]) && this.rule.evaluate(feature)
                    && superProto.shouldCluster.apply(this, arguments);
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
                return feature.cluster ? feature.cluster.length + " Inshore report locations"
                        : feature.attributes.iceDescription.Number + ": " + feature.attributes.iceDescription.Placename;
            },
            display : function(feature) {
                if (!feature.cluster) {
                    return feature.attributes.iceDescription.hasReport ? "yes" : "none";
                }
                return "yes";
            }
        };

        this.layers.inshore = new OpenLayers.Layer.Vector("Inshore", {
            styleMap : new OpenLayers.StyleMap({
                "default" : new OpenLayers.Style({
                    externalGraphic : "img/inshoreIceReport.png",
                    graphicOpacity : "${transparency}",
                    graphicWidth : '${size}',
                    graphicHeight : '${size}',
                    graphicYOffset : "${offset}",
                    graphicXOffset : "${offset}",
                    fontColor : "#000000",
                    fontSize : "10px",
                    fontFamily : "Courier New, monospace",
                    label : "${zoomDependentDescription}",
                    fontOpacity : "${transparency}",
                    fontWeight : "bold",
                    labelOutlineWidth : 0,
                    labelYOffset : -20,
                    display : "${display}",

                }, {
                    context : this.context
                }),
                "select" : new OpenLayers.Style({
                    externalGraphic : "img/inshoreIceReport.png",
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
                    label : "${description}",
                    fontWeight : "bold",
                    labelOutlineWidth : 0,
                    labelYOffset : -20,
                    display : "${display}",
                }, {
                    context : this.context
                })
            }),
            strategies : [ new OpenLayers.Strategy.RuleCluster({
                distance : 25,
                threshold : 2,
                rule : new OpenLayers.Rule({
                    filter : new OpenLayers.Filter.Comparison({
                        type : OpenLayers.Filter.Comparison.EQUAL_TO,
                        property : "hasReport",
                        value : true
                    })
                })
            }) ]
        });

        this.selectableLayers = [ this.layers.inshore ];
        this.selectableAttribute = "number";
    };

    this.draw = function(shapes) {
        var that = this;

        function drawInshoreIceReport(fragments) {
            var features = [];
            for ( var f in fragments) {
                var desc = fragments[f].description;
                var feature = new OpenLayers.Feature.Vector(embryo.map.createPoint(desc.Longitude.replace(",", "."),
                        desc.Latitude.replace(",", ".")), {
                    iceDescription : desc,
                    number : desc.Number,
                    hasReport : desc.hasReport
                });
                features.push(feature);
            }
            that.layers.inshore.addFeatures(features);
            that.layers.inshore.refresh();
        }

        that.clear();
        
        for ( var l in shapes) {
            var shape = shapes[l];
            var ice = shape.fragments;
            fragments = ice.slice(0);
            drawInshoreIceReport(fragments);
        }
    };

    this.selectReport = function(number) {
    };
}

InshoreIceReportLayer.prototype = new EmbryoLayer();
