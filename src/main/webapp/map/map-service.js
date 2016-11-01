/**
 * Map services.
 */
angular.module('maritimeweb.map')

/**
 * The language service is used for changing language, etc.
 */
    .service('MapService', [
        function () {
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
                        })
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

        }]);


