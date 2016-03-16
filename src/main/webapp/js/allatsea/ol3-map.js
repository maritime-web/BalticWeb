var maritimeweb = maritimeweb || {};

    maritimeweb.center = new function () {
        return [22.0, 59.0];
    }
    console.log("loading OL3 map");

var thunderforestAttributions = [
    new ol.Attribution({
        html: 'Tiles &copy; <a href="http://www.thunderforest.com/">Thunderforest</a>'
    }),
    ol.source.OSM.ATTRIBUTION
];

var openseaMapAttributions = [
    new ol.Attribution({
        html: '<a href="http://www.openseamap.org/">www.openseamap.org</a> - <a rel="license" href="http://creativecommons.org/licenses/by-sa/2.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-sa/2.0/80x15.png" /></a><br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-sa/2.0/">Creative Commons Attribution-ShareAlike 2.0 Generic License</a> - '
    }),
    ol.source.OSM.ATTRIBUTION
];

maritimeweb.groupBaseMaps = new function () {
    return new ol.layer.Group({
        'title': 'Base maps',
        layers: [
            new ol.layer.Tile({
                title: 'OpenStreetMap',
                type: 'base',
                visible: true,
                source: new ol.source.OSM()
            }),
            new ol.layer.Tile({
                title: 'Stamen - Water color',
                type: 'base',
                visible: false,
                source: new ol.source.Stamen({
                    layer: 'watercolor'
                })
            }),
            new ol.layer.Tile({
                title: 'Stamen - Toner',
                type: 'base',
                visible: false,
                source: new ol.source.Stamen({
                    layer: 'toner'
                })
            }),
            new ol.layer.Tile({
                title: 'MapQuest - OSM',
                type: 'base',
                visible: false,
                source: new ol.source.MapQuest({
                    layer: 'osm'
                })
            }),
            new ol.layer.Tile({
                title: 'MapQuest - Satellite',
                type: 'base',
                visible: false,
                source: new ol.source.MapQuest({
                    layer: 'sat'
                })
            }),
            new ol.layer.Tile({
                title: 'MapQuest - Hybrid',
                type: 'base',
                visible: false,
                source: new ol.source.MapQuest({
                    layer: 'hyb'
                })
            }),
            new ol.layer.Tile({
                title: 'Thunderforest - OpenCycleMap',
                type: 'base',
                visible: false,
                source: new ol.source.OSM({
                    url: 'http://{a-c}.tile.thunderforest.com/cycle/{z}/{x}/{y}.png',
                    attributions: thunderforestAttributions
                })
            }),
            new ol.layer.Tile({
                title: 'Thunderforest - Outdoors',
                type: 'base',
                visible: false,
                source: new ol.source.OSM({
                    url: 'http://{a-c}.tile.thunderforest.com/outdoors/{z}/{x}/{y}.png',
                    attributions: thunderforestAttributions
                })
            }),
            new ol.layer.Tile({
                title: 'Thunderforest - Landscape',
                type: 'base',
                visible: false,
                source: new ol.source.OSM({
                    url: 'http://{a-c}.tile.thunderforest.com/landscape/{z}/{x}/{y}.png',
                    attributions: thunderforestAttributions
                })
            }),
            new ol.layer.Tile({
                title: 'Thunderforest - Transport',
                type: 'base',
                visible: false,
                source: new ol.source.OSM({
                    url: 'http://{a-c}.tile.thunderforest.com/transport/{z}/{x}/{y}.png',
                    attributions: thunderforestAttributions
                })
            }),
            new ol.layer.Tile({
                title: 'Thunderforest - Transport Dark',
                type: 'base',
                visible: false,
                source: new ol.source.OSM({
                    url: 'http://{a-c}.tile.thunderforest.com/transport-dark/{z}/{x}/{y}.png',
                    attributions: thunderforestAttributions
                })
            })
        ]
    });

};

maritimeweb.groupAtons = new function () {
    var group =  new ol.layer.Group({
        title: 'Atons',
        layers: [

            new ol.layer.Tile({
                title: 'Seamark - OpenSeaMap.org',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://t1.openseamap.org/seamark/{z}/{x}/{y}.png',
                    attributions: openseaMapAttributions//,
                    //crossOrigin: 'null'
                })
            }),
            new ol.layer.Tile({
                title: 'Countries',
                source: new ol.source.TileWMS({
                    url: 'http://demo.opengeo.org/geoserver/wms',
                    params: {'LAYERS': 'ne:ne_10m_admin_1_states_provinces_lines_shp'},
                    serverType: 'geoserver'
                }),
                visible: false
            })
        ]
    });
    return group;
};


maritimeweb.groupOverlays = new function () {
    var group =  new ol.layer.Group({
        title: 'Weather Forecasts',
        layers: [
            new ol.layer.Tile({
                title: 'Gust  - openportguide.de',
                opacity: 0.4,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/tiles/actual/gust/5/{z}/{x}/{y}.png'
                    //url: 'http://weather.openportguide.de/tiles/actual/air_temperature/wind_stream/5/{z}/{x}/{y}.png'
                }),
                visible: false
            }),
            new ol.layer.Tile({
                title: 'Wind - openportguide.de',
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/tiles/actual/wind_stream/5/{z}/{x}/{y}.png'
                }),
                visible: false
            }),
                new ol.layer.Tile({
                    title: 'Air temperature - openportguide.de',
                    source: new ol.source.XYZ({
                        //url: 'http://weather.openportguide.de/tiles/actual/wind_stream/5/{z}/{x}/{y}.png'
                        url: 'http://weather.openportguide.de/tiles/actual/air_temperature/5/{z}/{x}/{y}.png'
                    }),
                    visible: false
                }),
            new ol.layer.Tile({
                title: 'significant_wave_height - openportguide.org',
                source: new ol.source.XYZ({
                    url: 'http://www.openportguide.org//tiles/actual/significant_wave_height/5/{z}/{x}/{y}.png'
                }),
                visible: false
            })



        ]
    });
    return group;
};

    var zoomslider = new ol.control.ZoomSlider();
    var seaScaleLine = new ol.control.ScaleLine({
        className: 'ol-scale-line',
        units: 'nautical', // 'degrees', 'imperial', 'nautical', 'metric', 'us'
        minWidth: 80,
//        target: 'scaleline',
        target: document.getElementById('scale-line')
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
        //target: 'mouseposition',
        target: document.getElementById('mouse-position'),
        className: 'mouse-position',
        undefinedHTML: '&nbsp;'
    });
    var balticExtent = ol.proj.transformExtent([9, 53, 31, 66], 'EPSG:4326', 'EPSG:3857');
    var map = new ol.Map({
        controls: ol.control.defaults().extend([
            new ol.control.OverviewMap({collapsed: false}),
            seaScaleLine,
            // metricScaleLine,
            zoomslider,
            new ol.control.FullScreen(),
            mousePosition

        ]),
        target: 'map',
        layers: [
            maritimeweb.groupBaseMaps,
            maritimeweb.groupOverlays,
            maritimeweb.groupAtons,

        ],
        view: new ol.View({
            center: ol.proj.fromLonLat(maritimeweb.center),
            zoom: 7,
            minZoom: 6,
            extent: balticExtent
        })
    });
    map.addControl(new ol.control.LayerSwitcher());

    console.log("loaded OL3 map center = " + map);
