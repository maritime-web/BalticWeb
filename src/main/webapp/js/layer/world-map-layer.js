function WorldMapLayer() {
    this.init = function() {
        var that = this;

        this.layers.world = new OpenLayers.Layer.Vector("World", {
            styleMap: new OpenLayers.StyleMap({
                "default": new OpenLayers.Style({
                    fillColor: "${fillColor}",
                    fillOpacity: 0.5,
                    strokeWidth: 1,
                    strokeColor: "#000000",
                    strokeOpacity: 0.2,
                })
            })
        });
    }

    this.draw = function(shapes) {
        this.layers.world.removeAllFeatures();

        for (var l in shapes) {
            var shape = shapes[l];
            var ice = shape.fragments;
            for (var i in ice) {
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
                        fillColor: "#009051"
                    }
                );

                this.layers.world.addFeatures([ feature ]);
            }
        }
    }
}

WorldMapLayer.prototype = new EmbryoLayer();


embryo.authenticated(function() {
    switch (embryo.baseMap) {
        case "osm":
            break;
        default:
            var layer = new WorldMapLayer();
            addLayerToMap("world", layer, embryo.map);
            embryo.ice.service.shapes({
                ids: "static." + embryo.baseMap,
                exponent: 2,
                delta:true
            }, function(error, data) {
                if (data) {
                    layer.draw(data);
                } else {
                    console.log("unhandled error", error);
                }
            });
            break;
    }
});
