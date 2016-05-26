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
            var mapMaxZoomLevel = 9;


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
                var polygon =  ol.geom.Polygon.fromExtent(extent);
                return this.olGeometryToWkt(polygon);
            };
            
            
            /** ************************ **/
            /** GeoJSON Functionality    **/
            /** ************************ **/

            /** Serializes the coordinates of a geometry */
            this.serializeCoordinates = function (g, coords, props, index, includeCoord) {
                var that = this;
                props = props || {};
                index = index || 0;
                var bufferFeature = props['parentFeatureId'];
                if (g) {
                    if (g instanceof Array) {
                        if (g.length >= 2 && $.isNumeric(g[0])) {
                            if (includeCoord && !bufferFeature) {
                                coords.push({
                                    lon: g[0],
                                    lat: g[1],
                                    name: props['name:' + index + ':en']
                                });
                            }
                            index++;
                        } else {
                            for (var x = 0; x < g.length; x++) {
                                index = that.serializeCoordinates(g[x], coords, props, index, includeCoord);
                            }
                        }
                    } else if (g.type == 'FeatureCollection') {
                        for (var x = 0; g.features && x < g.features.length; x++) {
                            index = that.serializeCoordinates(g.features[x], coords, props, index, includeCoord);
                        }
                    } else if (g.type == 'Feature') {
                        index = that.serializeCoordinates(g.geometry, coords, g.properties, index, includeCoord);
                    } else if (g.type == 'GeometryCollection') {
                        for (var x = 0; g.geometries && x < g.geometries.length; x++) {
                            index = that.serializeCoordinates(g.geometries[x], coords, props, index, includeCoord);
                        }
                    } else if (g.type == 'MultiPolygon') {
                        for (var p = 0; p < g.coordinates.length; p++) {
                            // For polygons, do not include coordinates for interior rings
                            for (var x = 0; x < g.coordinates[p].length; x++) {
                                index = that.serializeCoordinates(g.coordinates[p][x], coords, props, index, x == 0);
                            }
                        }
                    } else if (g.type == 'Polygon') {
                        // For polygons, do not include coordinates for interior rings
                        for (var x = 0; x < g.coordinates.length; x++) {
                            index = that.serializeCoordinates(g.coordinates[x], coords, props, index, x == 0);
                        }
                    } else if (g.type) {
                        index = that.serializeCoordinates(g.coordinates, coords, props, index, includeCoord);
                    }
                }
                return index;
            };

            /** Converts a GeoJSON geometry to an OL geometry **/
            this.gjToOlGeometry = function (g) {
                return geoJsonFormat.readGeometry(g, {
                    dataProjection: proj4326,
                    featureProjection: projMercator
                });
            };


            /** Converts a GeoJSON feature to an OL feature **/
            this.gjToOlFeature = function (feature) {
                return geoJsonFormat.readFeature(feature, {
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


            /** Creates a group of standard weather layers **/
            this.createStdWeatherLayerGroup = function () {

                var openPortGuideAttributions = [
                    new ol.Attribution({
                        html: '<div class="panel panel-default">' +
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

                return new ol.layer.Group({
                    title: 'Weather Forecasts',
                    layers: [
                        new ol.layer.Tile({
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
                        })
                    ]
                });
            };

            /** Creates a group of standard miscellaneous layers **/
            this.createStdMiscLayerGroup = function () {

                var openseaMapAttributions = [
                    new ol.Attribution({
                        html: '<div class="panel panel-default">' +
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
                    title: 'Misc.',
                    layers: [
                       // layerGeoJSONmsi,
                        new ol.layer.Tile({
                            title: 'Seamark - OpenSeaMap.org',
                            visible: false,
                            source: new ol.source.XYZ({
                                url: 'http://t1.openseamap.org/seamark/{z}/{x}/{y}.png',
                                attributions: openseaMapAttributions
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
            };

        }]);


