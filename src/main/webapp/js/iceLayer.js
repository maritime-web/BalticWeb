function createIceLayer() {
    function setLayerVisibility(id, value) {
        var layers = embryo.mapPanel.map.getLayersByName(id);

        for (var k in layers) {
            layers[k].setVisibility(value);
        }
    }

    function removeLayerById(id) {
        var layers = embryo.mapPanel.map.getLayersByName(id);

        for (var k in layers) {
            embryo.mapPanel.map.removeLayer(layers[k]);
        }
    }

    function colorByDescription(description) {
        if (description.POLY_TYPE == 'W') return "#0000ff";

        if (description.CT > 80) return "#ff0000";
        if (description.CT > 60) return "#e57425";
        if (description.CT > 40) return "#ffc05e";
        if (description.CT > 20) return "#fdfc39";

        return "#90fba4";
    }

    function setupLayers(ice) {
        removeLayerById("Ice");
        removeLayerById("Water");

        var iceLayer = new OpenLayers.Layer.Vector("Ice");
        var waterLayer = new OpenLayers.Layer.Vector("Water");

        for (var i in ice) {
            var polygons = ice[i].polygons;

            var rings = [];

            for (var k in polygons) {
                var polygon = polygons[k];

                var points = [];

                for (var j in polygon) {
                    var point = polygon[j];
                    var pointMapped = new OpenLayers.Geometry.Point(point.x, point.y);

                    pointMapped.transform(new OpenLayers.Projection("EPSG:4326"), embryo.mapPanel.map.getProjectionObject());

                    points.push(pointMapped);
                }

                rings.push(new OpenLayers.Geometry.LinearRing(points));
            }


            var feature = new OpenLayers.Feature.Vector(
                new OpenLayers.Geometry.Polygon(rings),
                null, {
                    fillColor: colorByDescription(ice[i].description),
                    fillOpacity: 0.4,
                    strokeWidth: 1,
                    strokeColor: "#000000",
                    strokeOpacity: 0.2
                }
            );

            if (ice[i].description.POLY_TYPE == 'I')
                iceLayer.addFeatures([ feature ], {
                    featureType : 'ice',
                    active : true
                });
            else
                waterLayer.addFeatures([ feature ]);
        }

        embryo.mapPanel.map.addLayer(iceLayer);
        embryo.mapPanel.map.addLayer(waterLayer);
        embryo.mapPanel.add2SelectFeatureCtrl(iceLayer);

        console.log("Ice and water layers addded.")
    }

    function requestShapefile(name) {
        console.log("Requesting " + name + " data ...");

        $.ajax({
            url: "http://localhost:8080/arcticweb/rest/shapefile/" + name,
            data: { },
            success: function(data) {
                var totalPolygons = 0;
                var totalPoints = 0;
                for (var i in data) {
                    totalPolygons += data[i].polygons.length;
                    for (var j in data[i].polygons)
                        totalPoints += data[i].polygons[j].length;
                }
                console.log(totalPolygons + " polygons. "+totalPoints+" points returned.");
                setupLayers(data);
            },
            error: function(data) {
                console.log("Server returned error code: " + data.status+ " for shapefile "+name);
            }
        });
    }

    requestShapefile("201304100920_CapeFarewell_RIC,201308141200_Greenland_WA,201308132150_Qaanaaq_RIC,201308070805_NorthEast_RIC");
};

$(document).ready(createIceLayer);
