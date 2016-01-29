function InshoreIceReportLayer() {  

    this.init = function () {
        this.zoomLevels = [ 4, 6, 11 ];

        var that = this;

        this.context = {
            transparency: function () {
                return that.active ? 0.5 : 0.25;
            },
            size: function () {
                return [ 16, 20, 24, 24 ][that.zoomLevel];
            },
            offset: function () {
                return -that.context.size() / 2;
            },
            zoomDependentDescription: function (feature) {
                if (that.zoomLevel < 3) {
                    return "";
                }
                return that.context.description(feature);
            },
            description: function (feature) {
                return feature.cluster ? feature.cluster.length + " Inshore report locations"
                    : feature.attributes.iceDescription.Number + ": " + feature.attributes.iceDescription.Placename;
            }
        };

        this.layers.inshore = new OpenLayers.Layer.Vector("Inshore", {
            styleMap: new OpenLayers.StyleMap({
                "default": new OpenLayers.Style({
                    externalGraphic: "img/inshoreIceReport.png",
                    graphicOpacity: "${transparency}",
                    graphicWidth: '${size}',
                    graphicHeight: '${size}',
                    graphicYOffset: "${offset}",
                    graphicXOffset: "${offset}",
                    fontColor: "#000000",
                    fontSize: "10px",
                    fontFamily: "Courier New, monospace",
                    label: "${zoomDependentDescription}",
                    fontOpacity: "${transparency}",
                    fontWeight: "bold",
                    labelOutlineWidth: 0,
                    labelYOffset: -20

                }, {
                    context: this.context
                }),
                "select": new OpenLayers.Style({
                    externalGraphic: "img/inshoreIceReport.png",
                    graphicOpacity: 1,
                    graphicWidth: 24,
                    graphicHeight: 24,
                    graphicXOffset: -12,
                    graphicYOffset: -12,
                    backgroundGraphic: "img/ring.png",
                    backgroundXOffset: -16,
                    backgroundYOffset: -16,
                    backgroundHeight: 32,
                    backgroundWidth: 32,
                    fontOpacity: 1,
                    fontColor: "#000",
                    fontSize: "10px",
                    fontFamily: "Courier New, monospace",
                    label: "${description}",
                    fontWeight: "bold",
                    labelOutlineWidth: 0,
                    labelYOffset: -20
                }, {
                    context: this.context
                })
            }),
            strategies: [ new OpenLayers.Strategy.Cluster({
                distance: 25,
                threshold: 2
            }) ]
        });

        this.selectableLayers = [ this.layers.inshore ];
        this.selectableAttribute = "number";
    };

    this.draw = function (shapes) {
        var that = this;

        function drawInshoreIceReport(fragments) {
            var features = [];
            for (var f in fragments) {
                var desc = fragments[f].description;
                var feature = new OpenLayers.Feature.Vector(embryo.map.createPoint(desc.Longitude.replace(",", "."),
                    desc.Latitude.replace(",", ".")), {
                    iceDescription: desc,
                    number: desc.Number,
                    hasReport: desc.hasReport
                });
                features.push(feature);
            }
            that.layers.inshore.addFeatures(features);
            that.layers.inshore.refresh();
        }

        that.clear();

        for (var l in shapes) {
            var shape = shapes[l];
            var ice = shape.fragments;
            fragments = ice.slice(0);
            drawInshoreIceReport(fragments);
        }
    };

    this.selectReport = function (number) {
    };
}

InshoreIceReportLayer.prototype = new EmbryoLayer();
