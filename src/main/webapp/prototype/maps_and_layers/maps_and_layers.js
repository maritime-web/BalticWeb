
angular.module('maritimeweb.maps_and_layers',[]).service('balticWebMap', function() {

        /*
         get the current bounding box in Bottom left  Top right format.
         */
        this.clientBBOX = function () {

            var bounds = this.map.getView().calculateExtent(this.map.getSize());
            var extent = ol.proj.transformExtent(bounds, 'EPSG:3857', 'EPSG:4326');
            var l = Math.floor(extent[0] * 100) / 100;
            var b = Math.floor(extent[1] * 100) / 100;
            var r = Math.ceil(extent[2] * 100) / 100;
            var t = Math.ceil(extent[3] * 100) / 100;
            var search_bbox = b + "|" + l + "|" + t + "|" + r + "";
            return search_bbox;
        };
        this.testFunction = function () {
            console.log("testing in namespace")
        };
        /*
         Pan to current position
         */
        this.panToPosition = function (postion) {
            var pan = ol.animation.pan({
                duration: 2000,
                source: /** @type {ol.Coordinate} */ (this.map.getView().getCenter())
            });
            this.map.beforeRender(pan);
            this.map.getView().setCenter(ol.proj.fromLonLat(postion));
            //maritimeweb.map.getView().setZoom(10);
        };

        this.isLayerVisible = function (nameOfLayer, layerGroup) {
            // Check om xxx-layer eksempelvis vessel-layer er aktiveret.
            var layersInGroup = layerGroup.getLayers().getArray();
            for (var i = 0, l; i < layersInGroup.length; i++) {
                l = layersInGroup[i];
                if ((l.get('name') === nameOfLayer) && l.get('visible')) {
                     return true;
                }
            }
            return false;
        };





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


    this.groupBaseMaps = new function () {
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
    //maritimeweb.endpoint = "https://arcticweb-alpha.e-navigation.net/";
    this.endpoint = "/";
    this.baseUrl = "/";
    this.defaultTimeout = 6000;


    this.groupAtons = new function () {
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
                    title: 'MSI Warnings - DMA*',
                    source: new ol.source.TileWMS({
                        url: 'http://demo.opengeo.org/geoserver/wms',
                        params: {'LAYERS': 'ne:ne_10m_admin_1_states_provinces_lines_shp'},
                        serverType: 'geoserver'
                    }),
                    visible: false
                })
                ,
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

    //this.layerVessels = new function () {
    //    return new ol.layer.Vector({
    //        name: "vesselVectorLayer",
    //        title: "Vessels - AIS data dynamic",
    //        visible: true
    //    });
    //};

    //layerVessels = new ol.layer.Vector({
    //        name: "vesselVectorLayer",
    //        title: "Vessels - AIS data dynamic",
    //        visible: true
    //    });


    this.groupVessels = new function () {
        var group = new ol.layer.Group({
            title: 'Vessels',
            layers: [
                //layerVessels,
                new ol.layer.Tile({
                    title: 'AIS - Helcom - High-bandwith *',
                    visible: false,
                    source: new ol.source.XYZ({
                        url: 'http://t1.openseamap.org/seamark/{z}/{x}/{y}.png',
                        attributions: openseaMapAttributions//,
                        //crossOrigin: 'null'
                    })
                }),
                new ol.layer.Tile({
                    title: 'AIS Helcom - low-bandwith *',
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


    this.groupWeather = new function () {
        var group = new ol.layer.Group({
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
                    title: 'Significant Wave Height - openportguide.org',
                    source: new ol.source.XYZ({
                        url: 'http://www.openportguide.org//tiles/actual/significant_wave_height/5/{z}/{x}/{y}.png'
                    }),
                    visible: false
                })
            ]
        });
        return group;
    };


    this.EPSG4326 = function () {
        return new ol.Projection("EPSG:4326")
    };
    this.EPSG900913 = function () {
        return new ol.Projection("EPSG:900913")
    };

    var center = new function () {
        return [22.0, 59.0];
    }
    console.log("loading OL3 map");


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

    var mousePosition = new ol.control.MousePosition({
        coordinateFormat: ol.coordinate.createStringXY(3), //ol.coordinate.toStringHDMS(coord, 1)
        projection: 'EPSG:4326',
        //target: 'mouseposition',
        target: document.getElementById('mouse-position'),
        className: 'mouse-position',
        undefinedHTML: '&nbsp;'
    });
    var balticExtent = ol.proj.transformExtent([9, 53, 31, 66], 'EPSG:4326', 'EPSG:3857');


    // Add Layers to map-------------------------------------------------------------------------------------------------------
    var layer_default_osm = new ol.layer.Tile({
        source: new ol.source.OSM({layer: 'sat'}),
        preload: Infinity
    });



        var overviewMap = new ol.control.OverviewMap({
            collapsed: false,
            layers: [
                new ol.layer.Tile({
                    source: new ol.source.OSM({
                        layer: 'sat'
                        //'url': '//{a-c}.tile.opencyclemap.org/cycle/{z}/{x}/{y}.png'
                    })
                })
            ],
            collapseLabel: '-',
            label: '+'

        });

        this.map = new ol.Map({
            controls: ol.control.defaults().extend([

                overviewMap,
                seaScaleLine,
                // metricScaleLine,
                zoomslider,
                new ol.control.FullScreen(),
                mousePosition,
                new ol.control.LayerSwitcher()
            ]),
            target: 'map',
            layers: [
                this.groupBaseMaps,
                this.groupWeather,
                this.groupAtons,
                this.groupVessels
            ],
            view: new ol.View({
                center: ol.proj.fromLonLat(center),
                zoom: 7,
                minZoom: 6,
                extent: balticExtent
            })
        });



    console.log("loaded OL3 map center = " + map);

});
/*
 maritimeweb.iconFeature = function() {
 return new ol.Feature({
 geometry: new ol.geom.Point(ol.proj.transform([18.0704, 57.678], 'EPSG:4326', 'EPSG:900913')),
 name: 'Speed vessel',
 speed: 40,
 course: 350
 });
 };

 maritimeweb.iconFeature1  = function() {
 return new ol.Feature({
 geometry: new ol.geom.Point(ol.proj.transform([18.1234, 55.678], 'EPSG:4326', 'EPSG:900913')),
 name: 'Large Vessel',
 speed: 30,
 course: 20
 });
 };*/