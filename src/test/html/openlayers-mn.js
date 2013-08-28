var map, layer;

function init() {
    var map = new OpenLayers.Map( 'map' );
    var ice = new OpenLayers.Layer.WMS(
        "OpenLayers WMS",
        "http://localhost:8000", {
            layers: 'ice',
            sphericalMercator: true,
            wrapDateLine: true,
            transitionEffect: "resize",
            transparent: true,
            isBaseLayer: false,
            opacity: 0.5
        }
    );
    var world = new OpenLayers.Layer.WMS(
        "OpenLayers WMS",
        "http://localhost:8000", {
            layers: 'world',
            isBaseLayer: true
        }
    );
    map.addLayer(world);
    map.addLayer(ice);

    map.zoomToMaxExtent();
}
