function SeaForecastLayer() {

    this.init = function () {
        var that = this;

        this.zoomLevels = [ 3, 4, 5];

        this.context = {
            fillColor: function (feature) {
                //#ff0000
                //#f2dede
                return feature.attributes.district.warnings ? "#ff0000" : "transparent";
            },
            transparency: function () {
                return that.active ? 0.5 : 0.25;
            },
            fontSize: function () {
                if (that.zoomLevel <= 1) {
                    return "8px";
                }
                if (that.zoomLevel == 2) {
                    return "10px";
                }
                return "12px";
            },
            label: function (feature) {
                if (that.active) {
                    return "" + feature.attributes.id + "\n" + feature.attributes.name;
                }
                return "";
            },
            display: function (feature) {
                return feature.attributes.district.warnings || that.active ? "yes" : "none";
            }

        };

        this.layers.forecasts = new OpenLayers.Layer.Vector("DistrictForecasts", {
            styleMap: new OpenLayers.StyleMap({
                "default": new OpenLayers.Style({
                    fillColor: "${fillColor}",
                    fillOpacity: "${transparency}",
                    strokeWidth: "1",
                    strokeColor: "#000000",
                    strokeOpacity: "0.2",
                    fontColor: "#000000",
                    fontSize: "${fontSize}",
                    fontFamily: "Courier New, monospace",
                    label: "${label}",
                    fontOpacity: "${transparency}",
                    fontWeight: "bold",
                    display: "${display}"
                }, {
                    context: this.context
                }),
                "temporary": new OpenLayers.Style({
                    fillColor: "${fillColor}",
                    fillOpacity: "${transparency}",
                    strokeWidth: "1",
                    strokeColor: "#000000",
                    strokeOpacity: "0.7"
                }, {
                    context: this.context
                }),
                "select": new OpenLayers.Style({
                    fillColor: "${fillColor}",
                    fillOpacity: "${transparency}",
                    strokeWidth: "1",
                    strokeColor: "#000",
                    strokeOpacity: "1"
                }, {
                    context: this.context
                })
            })
        });

        this.selectableLayers = [this.layers.forecasts];
        this.selectableAttribute = "district";
    };

    this.draw = function (shapes, callback) {

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
                name: fragment.description.name,
                id: fragment.description.Id,
                district: fragment.district
            });
            that.layers.forecasts.addFeatures([ feature ]);
            that.layers.forecasts.refresh();
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

        that.layers.forecasts.removeAllFeatures();
        that.layers.forecasts.refresh();

        for (var l in shapes) {
            var shape = shapes[l];
            drawFragments(shape, shape.fragments.slice(0));
        }
    };
}

SeaForecastLayer.prototype = new EmbryoLayer();
