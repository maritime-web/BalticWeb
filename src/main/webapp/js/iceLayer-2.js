function IceLayer() {
    this.init = function() {
        var that = this;

        var iceContext = {
            transparency: function() {
                return that.active ? 0.5 : 0.25;
            }
        }

        this.layers.ice = new OpenLayers.Layer.Vector("Ice", {
            styleMap: new OpenLayers.StyleMap({
                "default": new OpenLayers.Style({
                    fillColor: "${fillColor}",
                    fillOpacity: "${transparency}",
                    strokeWidth: 1,
                    strokeColor: "#000000",
                    strokeOpacity: 0.2,
                }, { context: iceContext}),
                "temporary": new OpenLayers.Style({
                    fillColor: "${fillColor}",
                    fillOpacity: "${transparency}",
                    strokeWidth: 1,
                    strokeColor: "#000000",
                    strokeOpacity: 0.7,
                }, { context: iceContext}),
                "select": new OpenLayers.Style({
                    fillColor: "${fillColor}",
                    fillOpacity: "${transparency}",
                    strokeWidth: 1,
                    strokeColor: "#000",
                    strokeOpacity: 1
                }, { context: iceContext})
            })
        });

        var waterContext = {
            transparency: function() {
                return that.active ? 0.3 : 0.15;
            }
        }

        this.layers.water = new OpenLayers.Layer.Vector("Water", {
            styleMap: new OpenLayers.StyleMap({
                "default": new OpenLayers.Style({
                    fillColor: "#5599ff",
                    fillOpacity: "${transparency}",
                    strokeWidth: 0,
                    strokeColor: "#000000",
                    strokeOpacity: 0,
                    fontColor: "#000000",
                    fontSize: "12px",
                    fontFamily: "Courier New, monospace",
                    label: "${description}",
                    fontOpacity: "${transparency}",
                    fontWeight: "bold"
                }, { context: waterContext})
            })
        });

        this.selectableLayer = this.layers.ice;
        this.selectableAttribute = "iceDescription";
    }

    this.draw = function(shapes) {
        function colorByDescription(description) {
            if (description.CT > 80) return "#ff0000";
            if (description.CT > 60) return "#e57425";
            if (description.CT > 40) return "#ffc05e";
            if (description.CT > 20) return "#fdfc39";
            return "#90fba4";
        }

        this.layers.ice.removeAllFeatures();
        this.layers.water.removeAllFeatures();

        for (var l in shapes) {
            var shape = shapes[l];
            var ice = shape.fragments;
            for (var i in ice)
                if (ice[i].description.POLY_TYPE == 'I') {
                    var polygons = ice[i].polygons;

                    var rings = [];

                    for (var k in polygons) {
                        var polygon = polygons[k];

                        var points = [];

                        for (var j in polygon) {
                            var p = polygon[j];
                            points.push(embryo.map.createPoint(p.x, p.y));
                        }

                        rings.push(new OpenLayers.Geometry.LinearRing(points));
                    }

                    var feature = new OpenLayers.Feature.Vector(
                        new OpenLayers.Geometry.Polygon(rings), {
                            fillOpacity: function() { return 0.4 * groupOpacity; },
                            fillColor: colorByDescription(ice[i].description),
                            iceDescription: ice[i].description
                        }
                    );

                    feature.attributes.iceDescription = $.extend(ice[i].description, { source: shape.description.id });

                    this.layers.ice.addFeatures([ feature ]);
                }

            // Water

            var rings = [];

            for (var i in ice)
                if (ice[i].description.POLY_TYPE == 'W') {
                    var polygons = ice[i].polygons;

                    for (var k in polygons) {
                        var polygon = polygons[k];

                        var points = [];

                        for (var j in polygon) {
                            var p = polygon[j];
                            points.push(embryo.map.createPoint(p.x, p.y));
                        }

                        rings.push(new OpenLayers.Geometry.LinearRing(points));
                    }

                }

            var feature = new OpenLayers.Feature.Vector(
                new OpenLayers.Geometry.Polygon(rings), {
                    description: shape.description.id
                }
            );

            this.layers.water.addFeatures([ feature ]);
        }
    }
}

IceLayer.prototype = new EmbryoLayer();
