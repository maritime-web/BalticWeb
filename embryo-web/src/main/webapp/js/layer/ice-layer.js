function IceLayer() {

    this.init = function () {
        var that = this;

        this.context = {
            sizes: {
                'S': 0.8,
                'M': 1,
                'L': 1.2,
                'VL': 1.4
            },
            transparency: function () {
                return that.active ? 0.5 : 0.25;
            },
            icebergSize: function (size) {
                return this.sizes[size];
            },
            imageWidth: function (feature) {
                if (feature.attributes.iceDescription.type === 'iceberg') {
                    return that.context.icebergSize(feature.attributes.iceDescription.Size_Catg) * 5;
                }
                return 0;
            },
            imageHeight: function (feature) {
                if (feature.attributes.iceDescription.type === 'iceberg') {
                    return that.context.icebergSize(feature.attributes.iceDescription.Size_Catg) * 6;
                }
                return 0;
            },
            description: function (feature) {
                return feature.attributes.description;
            }
        };

        this.layers.ice = new OpenLayers.Layer.Vector("IceChart", {
            styleMap: new OpenLayers.StyleMap({
                "default": new OpenLayers.Style({
                    fillColor: "${fillColor}",
                    fillOpacity: "${transparency}",
                    strokeWidth: "1",
                    strokeColor: "#000000",
                    strokeOpacity: "0.2",
                    fontColor: "#000000",
                    fontSize: "12px",
                    fontFamily: "Courier New, monospace",
                    label: "${description}",
                    fontOpacity: "${transparency}",
                    fontWeight: "bold"
                }, {
                    context: this.context
                }),
                "select": new OpenLayers.Style({
                    fillColor: "${fillColor}",
                    fillOpacity: "${transparency}",
                    strokeWidth: "1",
                    strokeColor: "#000",
                    strokeOpacity: "1",

                    externalGraphic: '${graphic}',
                    graphicWidth: '${imageWidth}',
                    graphicHeight: '${imageHeight}',
                    graphicOpacity: "${transparency}",
                }, {
                    context: this.context
                })
            })
        });

        this.layers.iceberg = new OpenLayers.Layer.Vector("Iceberg", {
            styleMap: new OpenLayers.StyleMap({
                "default": new OpenLayers.Style({
                    externalGraphic: 'img/iceberg.png',
                    graphicWidth: '${imageWidth}',
                    graphicHeight: '${imageHeight}',
                    graphicOpacity: "${transparency}",
                    strokeWidth: "1",
                    strokeColor: "#000000",
                    strokeOpacity: "0.2"
                }, {
                    context: this.context
                }),
                "select": new OpenLayers.Style({
                    externalGraphic: 'img/iceberg.png',
                    graphicWidth: '${imageWidth}',
                    graphicHeight: '${imageHeight}',
                    graphicOpacity: "${transparency}",
                    strokeWidth: "1",
                    strokeColor: "#000",
                    strokeOpacity: "1",
                    opacity: 1,
                    cursor: "crosshair",
                    backgroundGraphic: "img/selection.png",
                    backgroundXOffset: -16,
                    backgroundYOffset: -16,
                    backgroundHeight: 32,
                    backgroundWidth: 32,
                    backgroundRotation: 0,
                }, {
                    context: this.context
                })
            })
            /*
             * , strategies: [ new OpenLayers.Strategy.Cluster({ distance: 10,
             * threshold: 3 }) ]
             */
        });

        this.selectableLayers = [ this.layers.ice, this.layers.iceberg ];
        this.selectableAttribute = "iceDescription";
    };

    this.draw = function (chartType, shapes, callback) {
        function colorByDescription(description) {

            if (description.CT == 92 && parseInt(description.FA) == 8) {
                return "#979797";
            } else if (description.CT == 79 || description.CT > 80)
                return "#ff0000";
            if (description.CT == 57 || description.CT > 60)
                return "#ff7c06";
            if (description.CT == 24 || description.CT > 30)
                return "#ffff00";
            if (description.CT >= 10)
                return "#8effa0";
            return "#96C7FF";
        }

        var waterCount = 0;

        var that = this;

        function drawFragment(shape, fragment) {
            var rings = [];
            var polygons = fragment.polygons;

            for (var k in polygons) {
                var polygon = polygons[k];

                var points = [];
                for (var j in polygon) {
                    var p = polygon[j];

                    if (j >= 1) {
                        var diff = Math.abs(polygon[j - 1].x - p.x);
                        // if (diff > 350 && !(Math.abs(polygon[j-1].x) ==
                        // Math.abs(p.x) && polygon[j-1].y == p.y)) {
                        if (diff > 350) {
                            if (p.x < polygon[j - 1].x) {
                                p.x += 360;
                            } else {
                                p.x -= 360;
                            }
                        }
                    }

                    points.push(embryo.map.createPoint(p.x, p.y));
                }
                rings.push(new OpenLayers.Geometry.LinearRing(points));
            }

            var feature = new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Polygon(rings), {
                fillOpacity: function () {
                    return 0.4 * groupOpacity;
                },
                fillColor: colorByDescription(fragment.description),
                iceDescription: $.extend(fragment.description, {
                    information: shape.information,
                    type: 'iceChart'
                }),
                description: ""
            });
            if (fragment.description.POLY_TYPE == 'I') {
                feature.attributes.description = "";
            } else if (fragment.description.POLY_TYPE == 'W') {
                feature.attributes.description = waterCount == 0 ? shape.description.id : "";
                // modify description to make sure we show it is open water
                feature.attributes.iceDescription.CT = "1";
                waterCount++;
            }
            that.layers.ice.addFeatures([ feature ]);
            // that.layers.ice.strategies[0].deactivate();
            // that.layers.iceberg.removeAllFeatures();
            that.layers.ice.refresh();
        }

        function drawFragments(shape, fragments) {
            if (fragments.length > 0) {
                var fragment = fragments.pop();

                drawFragment(shape, fragment);

                window.setTimeout(function () {
                    drawFragments(shape, fragments);
                }, 20);
            } else {
                if (callback) {
                    callback();
                }
            }
        }

        function drawPoints() {
            for (var f in fragments) {
                var desc = fragments[f].description;
                var feature = new OpenLayers.Feature.Vector(embryo.map.createPoint(desc.Long, desc.Lat), {
                    iceDescription: $.extend(desc, {
                        information: shape.information,
                        type: 'iceberg'
                    })
                });
                that.layers.iceberg.addFeatures([ feature ]);
            }
            // that.layers.ice.strategies[0].activate();
            that.layers.iceberg.refresh();
            callback();
        }

        if (chartType == 'iceberg') {
            that.layers.iceberg.removeAllFeatures();
        } else {
            that.layers.ice.removeAllFeatures();
        }

        for (var l in shapes) {
            var shape = shapes[l];
            var ice = shape.fragments;

            fragments = ice.slice(0);
            if (chartType == 'iceberg') {
                drawPoints(fragments);
            } else {
                drawFragments(shape, fragments);
            }
        }
    };

    this.clear = function (chartType) {
        if (chartType) {
            if (chartType == 'iceberg') {
                this.layers.iceberg.removeAllFeatures();
            } else {
                this.layers.ice.removeAllFeatures();
            }
        } else {
            this.prototype.clear();
        }
    };
}

IceLayer.prototype = new EmbryoLayer();
