var maritimeweb = {};

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

maritimeweb.groupLayers = new function () {
    return new ol.layer.Group({
        'title': 'Base maps',
        layers: [
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
            }),
            new ol.layer.Tile({
                title: 'OSM',
                type: 'base',
                visible: true,
                source: new ol.source.OSM()
            }),
            //uyyy

            new ol.layer.Tile({
                title: 'wind_vector_5',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_vector/5/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_vector_27',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_vector/27/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_stream_5',
                visible: true,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_stream/5/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_stream_27',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_stream/27/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'gust_5',
                visible: false,
                opacity: 0.3,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'gust/5/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'gust_27',
                visible: false,
                opacity: 0.3,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'gust/27/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'surface_pressure_5',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'surface_pressure/5/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'surface_pressure_27',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'surface_pressure/27/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'air_temperature_5',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'air_temperature/5/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'air_temperature_27',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'air_temperature/27/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'precipitation_5',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'precipitation/5/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'precipitation_27',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'precipitation/27/{z}/{x}/{y}.png'
                })
            }),

            new ol.layer.Tile({
                title: 'significant_wave_height_27',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'significant_wave_height/27/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'wind_barb_27',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'wind_barb/27/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL100_wind_barb_27',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL100_wind_barb/27/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL200_wind_barb_27',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL200_wind_barb/27/{z}/{x}/{y}.png'
                })
            }),
            new ol.layer.Tile({
                title: 'FL300_wind_barb_19',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/demo/' + 'FL300_wind_barb/19/{z}/{x}/{y}.png'
                })
            })


            //xxxx
        ]
    });

};
maritimeweb.groupOverlays = new function () {
    var group =  new ol.layer.Group({
        title: 'Overlays',
        layers: [
            new ol.layer.Tile({
                title: 'Countries',
                source: new ol.source.TileWMS({
                    url: 'http://demo.opengeo.org/geoserver/wms',
                    params: {'LAYERS': 'ne:ne_10m_admin_1_states_provinces_lines_shp'},
                    serverType: 'geoserver'
                })
            }),
            new ol.layer.Tile({
                title: 'Weather !',
                source: new ol.source.TileWMS({
                    url: 'http://weather.openportguide.de/tiles/actual/wind_stream/5/{z}/{x}/{y}.png'
                    //url: 'http://weather.openportguide.de/tiles/actual/air_temperature/wind_stream/5/{z}/{x}/{y}.png'
                }),
                visible: false
            }),
                new ol.layer.Tile({
                    title: 'Temperature !',
                    source: new ol.source.TileWMS({
                        url: 'http://weather.openportguide.de/tiles/actual/wind_stream/5/{z}/{x}/{y}.png'
                        //url: 'http://weather.openportguide.de/tiles/actual/air_temperature/wind_stream/5/{z}/{x}/{y}.png'
                    }),
                    visible: false
                })


        ]
    });
    return group;
};

    var zoomslider = new ol.control.ZoomSlider();
    var seaScaleLine = new ol.control.ScaleLine({
        units: 'nautical', // 'degrees', 'imperial', 'nautical', 'metric', 'us'
        minWidth: 80
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
            //layer_default_osm
            //,
            //maritimeweb.groupLayers
            //,
            //maritimeweb.groupOverlays
            /* new ol.layer.Tile({
             source: new ol.source.XYZ({
             url: 'http://t1.openseamap.org/seamark/{z}/{x}/{y}.png',
             crossOrigin: true
             })
             })*/
            maritimeweb.groupLayers,
            maritimeweb.groupOverlays
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
