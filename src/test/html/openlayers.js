var map, layer;

function init() {
    var map = new OpenLayers.Map( 'map' );
    var layer = new OpenLayers.Layer.WMS(
        "OpenLayers WMS",
        "http://vmap0.tiles.osgeo.org/wms/vmap0",
        { layers: 'basic' }
    );
    map.addLayer(layer);

    var overlay = new OpenLayers.Layer.Vector("overlay");

    var points = [
        new OpenLayers.Geometry.Point(-60, 60),
        new OpenLayers.Geometry.Point(-30, 60),
        new OpenLayers.Geometry.Point(-30, 30),
        new OpenLayers.Geometry.Point(-60, 30)
    ];

    var ring = new OpenLayers.Geometry.LinearRing(points);

    var polygon = new OpenLayers.Geometry.Polygon([ ring ]);

    var feature = new OpenLayers.Feature.Vector(polygon);

    overlay.addFeatures([ feature ]);

    map.addLayer(overlay);

    map.zoomToMaxExtent();

    map.zoomIn();
}
