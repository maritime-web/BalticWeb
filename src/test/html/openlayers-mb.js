var map, layer;

function createMapBoxBaseLayer(id) {
    return new OpenLayers.Layer.XYZ(
        id,
        [
            "http://a.tiles.mapbox.com/v3/"+id+"/${z}/${x}/${y}.png",
            "http://b.tiles.mapbox.com/v3/"+id+"/${z}/${x}/${y}.png",
            "http://c.tiles.mapbox.com/v3/"+id+"/${z}/${x}/${y}.png",
            "http://d.tiles.mapbox.com/v3/"+id+"/${z}/${x}/${y}.png"
        ], {
            sphericalMercator: true,
            wrapDateLine: true,
            transitionEffect: "resize",
            buffer: 1,
            numZoomLevels: 17,
            isBaseLayer: true
        }
    );
}

function createMapBoxLayer(id) {
    return new OpenLayers.Layer.XYZ(
        id,
        [
            "http://a.tiles.mapbox.com/v3/"+id+"/${z}/${x}/${y}.png",
            "http://b.tiles.mapbox.com/v3/"+id+"/${z}/${x}/${y}.png",
            "http://c.tiles.mapbox.com/v3/"+id+"/${z}/${x}/${y}.png",
            "http://d.tiles.mapbox.com/v3/"+id+"/${z}/${x}/${y}.png"
        ], {
            sphericalMercator: true,
            wrapDateLine: true,
            transitionEffect: "resize",
            buffer: 1,
            numZoomLevels: 17,
            transparent: true,
            isBaseLayer: false,
            opacity: 0.3
        }
    );
}

function init() {
    var map = new OpenLayers.Map({
        div: "map",
        controls: [
            new OpenLayers.Control.Attribution(),
            new OpenLayers.Control.Navigation({
                dragPanOptions: {
                    enableKinetic: true
                }
            }),
            new OpenLayers.Control.Zoom(),
            new OpenLayers.Control.Permalink({anchor: true})
        ],
        center: [0, 0],
        zoom: 1
    });

    map.addLayer(createMapBoxBaseLayer("chvid.kort"));
    // map.addLayer(createMapBoxLayer("chvid.iskort-4"));

    map.zoomToMaxExtent();
}
