
var maritimeweb = {};

maritimeweb.center = new function(){
    return [22.0, 59.0];
}
console.log("loading OL3 map");

var zoomslider = new ol.control.ZoomSlider();
var seaScaleLine = new ol.control.ScaleLine({
    units: 'nautical', // 'degrees', 'imperial', 'nautical', 'metric', 'us'
    minWidth: 80,

});
var metricScaleLine = new ol.control.ScaleLine({
    units: 'metric', // 'degrees', 'imperial', 'nautical', 'metric', 'us'
    minWidth: 80

});

// Add Layers to map-------------------------------------------------------------------------------------------------------
layer_default_osm = new ol.layer.Tile({
    source: new ol.source.OSM({layer: 'sat'}),
    preload: Infinity
});



var mousePosition = new ol.control.MousePosition({
    coordinateFormat: ol.coordinate.createStringXY(4),
    projection: 'EPSG:4326',
    target: 'mouseposition',
    //target: document.getElementById('mouseposition'),
    undefinedHTML: '&nbsp;'
});
var balticExtent = ol.proj.transformExtent([9, 53, 31, 66], 'EPSG:4326', 'EPSG:3857');
var map = new ol.Map({
    controls: ol.control.defaults().extend([
        new ol.control.OverviewMap(),
        seaScaleLine,
        // metricScaleLine,
        zoomslider,
        new ol.control.FullScreen(),
        mousePosition

    ]),
    target: 'map',
    layers: [
        layer_default_osm//,
       /* new ol.layer.Tile({
            source: new ol.source.XYZ({
                url: 'http://t1.openseamap.org/seamark/{z}/{x}/{y}.png',
                crossOrigin: true
            })
        })*/
    ],
    view: new ol.View({
        center: ol.proj.fromLonLat(maritimeweb.center),
        zoom: 7,
        minZoom: 6,
        extent: balticExtent
    })
});
console.log("loaded OL3 map center = " + map);
