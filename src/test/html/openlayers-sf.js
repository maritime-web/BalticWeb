var map;

function setupBasicMapLayer() {
    var layer = new OpenLayers.Layer.WMS(
        "Basic Map",
        "http://vmap0.tiles.osgeo.org/wms/vmap0",
        { layers: 'basic' }
    );
    map.addLayer(layer);
}

function setLayerVisibility(id, value) {
    var layers = map.getLayersByName(id);

    for (var k in layers) {
        layers[k].setVisibility(value);
    }
}

function removeLayerById(id) {
    var layers = map.getLayersByName(id);

    for (var k in layers) {
        map.removeLayer(layers[k]);
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
                /*
                pointMapped.transform(
                    new OpenLayers.Projection("EPSG:4326"),
                    map.getProjectionObject()
                );
                */
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
            iceLayer.addFeatures([ feature ]);
        else
            waterLayer.addFeatures([ feature ]);
    }

    map.addLayer(iceLayer);
    map.addLayer(waterLayer);

    updateVisibility();
}

function requestShapefile(name) {
    $("#feedback").html("Requesting " + name + " data ...");

    var parameter = { };

    if ($("#resolution").val().trim() != "") {
        parameter.resolution = parseInt($("#resolution").val().trim());
    }

    $.ajax({
        url: "http://localhost:8080/arcticweb/rest/shapefile/" + name,
        data: parameter,
        success: function(data) {
            var totalPolygons = 0;
            var totalPoints = 0;
            for (var i in data) {
                totalPolygons += data[i].polygons.length;
                for (var j in data[i].polygons)
                    totalPoints += data[i].polygons[j].length;
            }
            $("#feedback").html(totalPolygons + " polygons. "+totalPoints+" points returned.");
            setupLayers(data);
        },
        error: function(data) {
            $("#feedback").html("Server returned error code: " + data.status);
        }
    });
}

function updateVisibility() {
    setLayerVisibility("Water", $("#waterVisibility").is(':checked'));
    setLayerVisibility("Ice", $("#iceVisibility").is(':checked'));
    setLayerVisibility("Basic Map", $("#mapVisibility").is(':checked'));
}

$(function() {
    map = new OpenLayers.Map('map');

    setupBasicMapLayer();

    map.zoomToMaxExtent();
    map.zoomIn();

    $("#fetchButton").click(function() {
        requestShapefile($("#fileName").val());
    });

    $("input:checkbox").click(updateVisibility);
});
