/**
 * Map services.
 */
angular.module('maritimeweb.map')

/**
 * The language service is used for changing language, etc.
 */
    .service('MapService', ['$http',
        function ($http) {
            'use strict';

            var that = this;
            var projMercator = 'EPSG:3857';
            var proj4326 = 'EPSG:4326';
            var geoJsonFormat = new ol.format.GeoJSON();
            var wktFormat = new ol.format.WKT();

            var mapDefaultLongitude = 22;
            var mapDefaultLatitude = 59;
            var mapDefaultZoomLevel = 7;


            /** Returns the data projection */
            this.dataProjection = function () {
                return proj4326;
            };


            /** Returns the feature projection */
            this.featureProjection = function () {
                return projMercator;
            };


            /** Rounds each value of the array to the given number of decimals */
            this.round = function (values, decimals) {
                for (var x = 0; values && x < values.length; x++) {
                    // NB: Prepending a '+' will convert from string to float
                    values[x] = +values[x].toFixed(decimals);
                }
                return values;
            };


            /** Returns the default center position of a map */
            this.defaultCenterLonLat = function () {
                return [mapDefaultLongitude, mapDefaultLatitude];
            };


            /** Returns the default zoom level of a map */
            this.defaultZoomLevel = function () {
                return mapDefaultZoomLevel;
            };


            /** Converts lon-lat array to xy array in mercator */
            this.fromLonLat = function (lonLat) {
                return lonLat ? ol.proj.fromLonLat(lonLat) : null;
            };


            /** Converts xy array in mercator to a lon-lat array */
            this.toLonLat = function (xy) {
                return xy ? ol.proj.transform(xy, projMercator, proj4326) : null;
            };



            /** Converts lon-lat extent array to xy extent array in mercator */
            this.fromLonLatExtent = function (lonLatExtent) {
                if (lonLatExtent && lonLatExtent.length == 4) {
                    var minPos = this.fromLonLat([lonLatExtent[0], lonLatExtent[1]]);
                    var maxPos = this.fromLonLat([lonLatExtent[2], lonLatExtent[3]]);
                    return [minPos[0], minPos[1], maxPos[0], maxPos[1]];
                }
                return null;
            };


            /** Converts xy extent array in mercator to a lon-lat extent array */
            this.toLonLatExtent = function (xyExtent) {
                if (xyExtent && xyExtent.length == 4) {
                    var minPos = this.toLonLat([xyExtent[0], xyExtent[1]]);
                    var maxPos = this.toLonLat([xyExtent[2], xyExtent[3]]);
                    return [minPos[0], minPos[1], maxPos[0], maxPos[1]];
                }
                return null;
            };


            /** Returns the center of the extent */
            this.getExtentCenter = function (extent) {
                var x = extent[0] + (extent[2] - extent[0]) / 2.0;
                var y = extent[1] + (extent[3] - extent[1]) / 2.0;
                return [x, y];
            };


            /** Return a lon-lat center from the xy geometry */
            this.toCenterLonLat = function (geometry) {
                return this.toLonLat(this.getExtentCenter(geometry.getExtent()));
            };


            /** ************************ **/
            /** WKT Functionality    **/
            /** ************************ **/

            /** Converts the given OL geometry to a WKT */
            this.olGeometryToWkt = function (g) {
                return wktFormat.writeGeometry(g, {
                    dataProjection: proj4326,
                    featureProjection: projMercator
                });
            };


            /** Converts the given extent to a WKT */
            this.extentToWkt = function (extent) {
                var polygon = ol.geom.Polygon.fromExtent(extent);
                return this.olGeometryToWkt(polygon);
            };


            /** Converts a GeoJSON feature to an OL feature **/
            this.gjToOlFeature = function (feature) {
                return geoJsonFormat.readFeature(feature, {
                    dataProjection: proj4326,
                    featureProjection: projMercator
                });
            };




            /** Converts a GeoJSON feature to an OL feature **/
            this.wktToOlFeature = function (feature) {
                return wktFormat.readFeature(feature, {
                    dataProjection: proj4326,
                    featureProjection: projMercator
                });
            };

            /** Converts a GeoJSON feature to an OL feature **/
            this.wktToOlGeomFeature = function (feature) {
                return wktFormat.readGeometry(feature, {
                    dataProjection: proj4326,
                    featureProjection: projMercator
                });
            };



            /** ************************ **/
            /** Standard map layers      **/
            /** ************************ **/

            /** Checks if the given layer is visible in the given layer group **/
            this.isLayerVisible = function (nameOfLayer, layerGroup) {
                var layersInGroup = layerGroup.getLayers().getArray();
                for (var i = 0, l; i < layersInGroup.length; i++) {
                    l = layersInGroup[i];
                    if ((l.get('name') === nameOfLayer) && l.get('visible')) {
                        return true;
                    }
                }
                return false;
            };

            this.customAjaxWMSLoader = function (tile, src) {
                $http.get(src, {responseType: 'arraybuffer'})
                    .then(function (response) {
                        var img = tile.getImage();
                        try {
                            var blob = new Blob([response.data], {type: 'image/png'});
                            img.src = (window.URL || window.webkitURL).createObjectURL(blob);
                            img.width = img.height = 256;
                        } catch (err) {
                            img.src = "/img/blank.png";
                            img.width = img.height = 256;
                        }
                    });
            };


            /** Creates a group of standard background layers **/
            this.createSuperSeaMapLayerGroup = function () {
                return new ol.layer.Group({
                    'title': 'Combined Nautical Charts ',
                    layers: [
                        new ol.layer.Tile({
                            title: 'OpenStreetMap',
                            type: 'base',
                            visible: false,
                            source: new ol.source.OSM()
                        }),

                        new ol.layer.Tile({
                            title: 'sjofartsverket.se - Sjokort',
                            visible: false,
                            source: new ol.source.TileWMS({
                                url: 'https://geokatalog.sjofartsverket.se/mapservice/wms.axd/Sjokort_TS',
                                minZoom: 10,
                                params: {
                                    'LAYERS': 'Sjokort',
                                    'TRANSPARENT': 'true'
                                }
                            })
                        }),
                        new ol.layer.Tile({
                            title: 'Danish Geodata Agency - Sea map',
                            visible: false,
                            source: new ol.source.TileWMS({
                                url: '/wms/',
                                params: {
                                    'LAYERS': 'cells',
                                    'TRANSPARENT': 'TRUE'
                                },
                                minZoom: 10,
                                tileLoadFunction: this.customAjaxWMSLoader
                            })
                        })
                    ]
                });
            };


            /** Creates a group of standard background layers **/
            this.createStdBgLayerGroup = function () {

                var thunderforestAttributions = [
                    new ol.Attribution({
                        html: '<span>Tiles &copy; <a href="http://www.thunderforest.com/">Thunderforest</a></span>'
                    }),
                    ol.source.OSM.ATTRIBUTION
                ];

                return new ol.layer.Group({
                    'title': 'Base maps',
                    layers: [
                        new ol.layer.Tile({
                            title: 'OpenStreetMap',
                            type: 'base',
                            visible: true,
                            source: new ol.source.OSM()
                        }),
/*                        new ol.layer.Tile({
                            title: 'Stamen - Water color',
                            type: 'base',
                            visible: false,
                            source: new ol.source.Stamen({
                                layer: 'watercolor'
                            })
                        }),*/
                        new ol.layer.Tile({
                            title: 'Stamen - Toner',
                            type: 'base',
                            visible: false,
                            source: new ol.source.Stamen({
                                layer: 'toner'
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
                            title: 'sjofartsverket.se - Sjokort',
                            visible: false,
                            source: new ol.source.TileWMS({
                                url: 'https://geokatalog.sjofartsverket.se/mapservice/wms.axd/Sjokort_TS',
                                params: {
                                    'LAYERS': 'Sjokort',
                                    'TRANSPARENT': 'true'
                                }
                            })
                        }),
                        new ol.layer.Tile({
                            title: 'Danish Geodata Agency - Sea map',
                            visible: false,
                            source: new ol.source.TileWMS({
                                url: '/wms/',
                                params: {
                                    'LAYERS': 'cells',
                                    'TRANSPARENT': 'TRUE'
                                },
                                tileLoadFunction: this.customAjaxWMSLoader
                            })
                        })
                        //,
                        //this.createWMSTileLayer()

                        /* new ol.layer.Tile({
                         title: 'Arcgisonline - Light Grey Base',
                         type: 'base',
                         visible: false,
                         source: new ol.source.OSM({
                         url: 'http://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Light_Gray_Base/MapServer/tile/{z}/{y}/{x}.png',
                         attributions: thunderforestAttributions
                         })
                         })*/
                    ]
                });
            };



            /** Creates a group of standard weather layers **/
            this.createStdWeatherLayerGroup = function () {

                var openPortGuideAttributions = [
                    new ol.Attribution({
                        html: '<div class="panel panel-info">' +
                        '<div class="panel-heading">Weather forecasts from  <a href="http://www.openportguide.de/">www.openportguide.de</a></div>' +
                        '<div class="panel-body">' +
                        '<span>Weather forecasts are from <a href="http://www.openportguide.de/">www.openportguide.de <img src="/img/OpenPortGuideLogo_32.png"/></a><br />' +
                        'The work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/4.0/">Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License</a>  ' +
                        '<a rel="license" href="http://creativecommons.org/licenses/by-nc-sa/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc-sa/4.0/88x31.png" /></a></span>' +
                        '</div>' +
                        '</div>'

                    }),
                    ol.source.OSM.ATTRIBUTION
                ];

                var nasaAttributions = [
                    new ol.Attribution({
                        html: '<div class="panel panel-info">' +
                        '<div class="panel-heading">NASA: Satellite image from NASA</div>' +
                        '<div class="panel-body">' +
                        '<span>We acknowledge the use of data products or imagery from the Land, Atmosphere Near real-time Capability for EOS (LANCE) system operated by the NASA/GSFC/Earth Science Data and Information System (ESDIS) with funding provided by NASA/HQ.</span>' +
                        '</div>' +
                        '</div>'

                    }),
                    ol.source.OSM.ATTRIBUTION
                ];

                return new ol.layer.Group({
                    title: 'Weather Forecasts',
                    layers: [
                        new ol.layer.Tile({
                            title: 'NASA: Latest - Aqua Satellite image',

                            source: new ol.source.XYZ({
                                urls:[
                                    'http://satellite.e-navigation.net:8080/BalticSea.latest.aqua.250m/{z}/{x}/{y}.png'
                                ],
                                attributions: nasaAttributions,
                                minZoom: 3,
                                maxZoom: 8,
                                tilePixelRatio: 1.000000
                            }),
                            visible: false

                        }),
                        new ol.layer.Tile({
                            title: 'NASA: Latest - Terra satellite image',

                            source: new ol.source.XYZ({
                                urls:[
                                    'http://satellite.e-navigation.net:8080/BalticSea.latest.terra.250m/{z}/{x}/{y}.png'
                                ],
                                attributions: nasaAttributions,
                                minZoom: 3,
                                maxZoom: 8,
                                tilePixelRatio: 1.000000
                            }),
                            visible: false

                        })

/*                        new ol.layer.Tile({
                            title: 'Surface Pressure  - openportguide.de',
                            source: new ol.source.XYZ({
                                url: 'http://weather.openportguide.de/tiles/actual/surface_pressure/5/{z}/{x}/{y}.png',
                                attributions: openPortGuideAttributions
                                //url: 'http://weather.openportguide.de/tiles/actual/air_temperature/wind_stream/5/{z}/{x}/{y}.png'
                            }),
                            visible: false
                        }),
                        new ol.layer.Tile({
                            title: 'Wind - openportguide.de',
                            source: new ol.source.XYZ({
                                url: 'http://weather.openportguide.de/tiles/actual/wind_stream/5/{z}/{x}/{y}.png',
                                attributions: openPortGuideAttributions
                            }),
                            visible: false
                        }),
                        new ol.layer.Tile({
                            title: 'Air temperature - openportguide.de',
                            source: new ol.source.XYZ({
                                url: 'http://weather.openportguide.de/tiles/actual/air_temperature/5/{z}/{x}/{y}.png',
                                attributions: openPortGuideAttributions
                            }),
                            visible: false
                        }),
                        new ol.layer.Tile({
                            title: 'Precipitation - openportguide.de',
                            source: new ol.source.XYZ({
                                //url: 'http://weather.openportguide.de/tiles/actual/wind_stream/5/{z}/{x}/{y}.png'
                                url: 'http://weather.openportguide.de/tiles/actual/precipitation/5/{z}/{x}/{y}.png',
                                attributions: openPortGuideAttributions
                            }),
                            visible: false
                        }),

                        new ol.layer.Tile({
                            title: 'Significant Wave Height - openportguide.org',
                            source: new ol.source.XYZ({
                                url: 'http://www.openportguide.org//tiles/actual/significant_wave_height/5/{z}/{x}/{y}.png',
                                attributions: openPortGuideAttributions
                            }),
                            visible: false
                        })*/

                        /*,
                        new ol.layer.Vector({
                            title: 'tidal - Barrenswatch',
                            source: new ol.source.Vector({
                                projection : 'EPSG:3857',
                                url: 'map/barrenswatch_no_salstraumen.json',
                            //url: 'https://www.barentswatch.no/api/v1/geodata/saltstraumen',
                                format: new ol.format.GeoJSON()
                            }),
                            visible: false

                        })*/
                    ]
                });
            };

            /** Creates a group of standard miscellaneous layers **/
            this.createStdMiscLayerGroup = function () {

                var openseaMapAttributions = [
                    new ol.Attribution({
                        html: '<div class="panel panel-info">' +
                        '<div class="panel-heading">SeaMark the OpenSeaMap.org layer</div>' +
                        '<div class="panel-body">' +
                        '<span>' +
                        'The OpenSeaMap.org layer and all its content is attributed to <a href="http://www.openseamap.org/">www.openseamap.org</a> ' +
                        '- <a rel="license" href="http://creativecommons.org/licenses/by-sa/2.0/">' +
                        '<img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-sa/2.0/80x15.png" /></a>' +
                        '<br />The OpenSeaMap.org layer is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-sa/2.0/">' +
                        'Creative Commons Attribution-ShareAlike 2.0 Generic License</a>' +
                        '</span>' +
                        '</div>'
                    })
                ];

                return new ol.layer.Group({
                    title: 'Navigation',
                    layers: [
                        // layerGeoJSONmsi,
                        new ol.layer.Tile({
                            title: 'Seamark - OpenSeaMap.org',
                            visible: false,
                            source: new ol.source.XYZ({
                                url: 'http://t1.openseamap.org/seamark/{z}/{x}/{y}.png',
                                attributions: openseaMapAttributions
                            })
                        })
                    ]
                });
            };


            /***************************/
            /** noGoLayer Layers      **/
            /***************************/
            this.createNoGoLayerGroup = function () {

                var noGoStyleRed = new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: 'rgba(255, 0, 10, 0.5)',
                        width: 1
                    }),
                    fill: new ol.style.Fill({
                        color: 'rgba(255, 0, 10, 0.10)'
                    })
                });
                var availableServiceStyle = new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: 'rgba(0, 255, 10, 0.8)',
                        width: 3
                    })
                });


                // Construct the boundary layers
                var boundaryLayer = new ol.layer.Vector({
                    title: 'Calculated NO GO AREA',
                    zIndex: 11,
                    source: new ol.source.Vector({
                        features: new ol.Collection(),
                        wrapX: false
                    }),
                    style: [noGoStyleRed]
                });

                var serviceAvailableLayer = new ol.layer.Vector({
                    title: 'Service Available - NO GO AREA',
                    zIndex: 11,
                    source: new ol.source.Vector({
                        features: new ol.Collection(),
                        wrapX: false
                    }),
                    style: [availableServiceStyle]
                });

                serviceAvailableLayer.setZIndex(12);
                serviceAvailableLayer.setVisible(true);
                serviceAvailableLayer.getSource().clear();


                boundaryLayer.setZIndex(11);
                boundaryLayer.setVisible(true);


                /***************************/
                /** Map creation          **/
                /***************************/

                // Construct No Go Layer Group layer
                var noGoGroupLayer = new ol.layer.Group({
                    title: 'No Go Service',
                    zIndex: 11,
                    layers: [boundaryLayer, serviceAvailableLayer]
                });
                noGoGroupLayer.setZIndex(11);
                noGoGroupLayer.setVisible(true);

                return noGoGroupLayer;
            }



            /***************************/
            /** noGoLayer Layers      **/
            /***************************/
            this.createMCLayerGroup = function () {

                var mcStylePurple = new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: 'rgba(180, 0, 180, 0.5)',
                        width: 1
                    }),
                    fill: new ol.style.Fill({
                        color: 'rgba(180, 0, 180, 0.10)'
                    })
                });
                var greenServiceStyle = new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: 'rgba(0, 255, 10, 0.8)',
                        width: 3
                    })
                });


                // Construct the boundary layers
                var boundaryLayer = new ol.layer.Vector({
                    id: 'mcboundary',
                    title: 'MaritimeCloud Service Instance AREA',
                    zIndex: 11,
                    source: new ol.source.Vector({
                        features: new ol.Collection(),
                        wrapX: false
                    }),
                    style: [mcStylePurple]
                });

       /*         var serviceAvailableLayer = new ol.layer.Vector({
                    id: 'serviceavailboundary',
                    title: 'Service Available - NO GO AREA',
                    zIndex: 11,
                    source: new ol.source.Vector({
                        features: new ol.Collection(),
                        wrapX: false
                    }),
                    style: [greenServiceStyle]
                });

                serviceAvailableLayer.setZIndex(12);
                serviceAvailableLayer.setVisible(true);
                serviceAvailableLayer.getSource().clear();*/


                boundaryLayer.setZIndex(11);
                boundaryLayer.setVisible(true);
                var wkt = 'MULTIPOLYGON (((9.624023437500002 54.838663612975125, 9.448242187500002 54.84498993218759, ' +
                    '9.382324218750002 54.807017138462555, 9.206542968750002 54.832336301970344, 8.6572265625 54.90819859298938, ' +
                    '8.536376953125 54.990221720048936, 8.382568359375002 55.065786886591724, 7.415771484375 55.19768334019969, ' +
                    '5.778808593749998 55.528630522571916, 5.44921875 55.24781504467555, 5.185546875 55.24155203565252, ' +
                    '4.757080078125 55.391592107033404, 4.229736328125 55.76421316483771, 3.3837890624999996 55.91227293006361, ' +
                    '3.2739257812499996 56.09042714399155, 7.8662109375 57.48040333923342, 8.887939453125 57.692405535264584, ' +
                    '9.404296875 57.99063188288076, 9.99755859375 58.269065573473284, 10.535888671875 58.14751859907358, ' +
                    '11.041259765625002 57.83305491291088, 12.15087890625 56.5231395643722, 12.10693359375 56.29825315291387, ' +
                    '12.384338378906248 56.20975914792473, 12.634277343749996 56.058235955596075, 12.664489746093746 56.015272531542365, ' +
                    '12.656249999999998 55.91996893509676, 12.711181640624998 55.82134464477079, 12.892456054687496 55.64659898563684, ' +
                    '12.878723144531248 55.60783270038269, 12.716674804687498 55.541064956111, 12.7056884765625 55.48819145580225, ' +
                    '12.617797851562498 55.41654360858007, 12.6397705078125 55.285372382493534, 12.7935791015625 55.15376626853558, ' +
                    '13.062744140624998 55.06893234377864, 13.1561279296875 55.01542594056298, 12.930908203124998 54.82917227452137, ' +
                    '12.7276611328125 54.76267040025496, 12.453002929687498 54.680183097099984, 12.117919921875 54.41573362292809, ' +
                    '11.942138671874996 54.36455818952146, 11.678466796874998 54.35815677227373, 11.321411132812498 54.56569261911193, ' +
                    '11.118164062499996 54.62933821655574, 10.925903320312498 54.63569730606386, 10.739135742187498 54.54339315407256, ' +
                    '10.623779296874998 54.54339315407256, 10.360107421874998 54.62933821655574, 10.184326171874998 54.77534585936445, ' +
                    '10.057983398437496 54.77534585936445, 9.876708984374998 54.8386636129751, 9.624023437500002 54.838663612975125)), ' +
                    '((14.0020751953125 54.95869417101662, 15.0457763671875 55.6930679264579, 16.5069580078125 55.363502833950776, ' +
                    '14.633789062500002 54.53383250794428, 14.414062499999998 54.65794628989232, 14.3975830078125 54.81334841741929, ' +
                    '14.161376953124998 54.81334841741929, 14.0020751953125 54.95869417101662)))';
                var olFeature = this.wktToOlFeature(wkt);
                boundaryLayer.getSource().addFeature(olFeature);


                /***************************/
                /** Map creation          **/
                /***************************/

                // Construct No Go Layer Group layer
                var mcSRGroupLayer = new ol.layer.Group({
                    title: 'MC Service Registry',
                    zIndex: 11,
                    layers: [boundaryLayer]
                });
                mcSRGroupLayer.setZIndex(11);
                mcSRGroupLayer.setVisible(true);

                return mcSRGroupLayer;
            }



        }]);


