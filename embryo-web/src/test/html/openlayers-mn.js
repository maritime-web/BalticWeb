function init() {
    var map = new OpenLayers.Map('map');
    var world = new OpenLayers.Layer.WMS(
        "OpenLayers WMS",
        "http://localhost:8000", {
            layers: 'world',
            isBaseLayer: true
        }
    );
    map.addLayer(world);
    map.zoomToMaxExtent();
}