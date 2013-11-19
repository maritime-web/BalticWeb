var map;

function init() {
    map = new OpenLayers.Map( 'map' );

    var osm = new OpenLayers.Layer.OSM(
        "OSM",
        [ "http://a.tile.openstreetmap.org/${z}/${x}/${y}.png",
        "http://b.tile.openstreetmap.org/${z}/${x}/${y}.png",
         "http://c.tile.openstreetmap.org/${z}/${x}/${y}.png" ], {
            'layers' : 'basic',
            'isBaseLayer' : true
        }
    );
    map.addLayer(osm);
    map.setBaseLayer(osm);

    function createPoint(longitude, latitude) {
        return new OpenLayers.Geometry.Point(longitude, latitude)
            .transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject());
    }

    function addOverlay(i) {
        var overlay = new OpenLayers.Layer.Vector("overlay"+i /* , { renderers: [ "Canvas" ]} */ );

        var points = [
            createPoint(-60, 60+i*2),
            createPoint(-30+i*6, 60-3*i),
            createPoint(-30, 30),
            createPoint(-60+i*9, 30+i)
        ];

        var ring = new OpenLayers.Geometry.LinearRing(points);

        var polygon = new OpenLayers.Geometry.Polygon([ ring ]);

        var feature = new OpenLayers.Feature.Vector(polygon);

        overlay.addFeatures([ feature ]);

        map.addLayer(overlay);
    }

    // Virker i fuldskærms chrome
    // for (var i = 0; i < 13; i++) addOverlay(i);

    // Virker ikke i fuldskærms chrome
    for (var i = 0; i < 14; i++) addOverlay(i);

    map.zoomToMaxExtent();

    map.zoomIn();
}
