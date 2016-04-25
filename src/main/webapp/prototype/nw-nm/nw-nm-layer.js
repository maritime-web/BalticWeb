/**
 * Defines the main NW-NM message layer
 *
 * TODO: Lifecycle management, convert to use backend data...
 */
angular.module('maritimeweb.nw-nm.layer',[])

    .service('NwNmService', ['$http',
        function($http) {

            /**
             * Returns the published NW-NM messages
             * @returns the published NW-NM messages
             */
            this.getPublishedNwNm = function () {
                // Possible parameters: "lang", "mainType" and "wkt". Refer to:
                // http://niord.e-navigation.net/api.html#!/message_list/search
                return $http.get('http://niord.e-navigation.net/rest/public/v1/messages?lang=en');
            };


        }])


    .service('NwNmLayer', ['$http', '$timeout', '$uibModal', 'NwNmService',
        function($http, $timeout, $uibModal, NwNmService) {

        var that = this;

        // Actual layer
        var nwLayer;
        var nmLayer;
        var nwnmLayer;

        // Message list
        var messageList = [];

        // Messages with no geometry
        var generalMessages = [];

        // Use for reading GeoJSON
        var projMercator = 'EPSG:3857';
        var proj4326 = 'EPSG:4326';
        var geoJsonFormat = new ol.format.GeoJSON();

        // Batch up loading of messages
        var loadTimer = undefined;


        /***************************/
        /** Utility methods       **/
        /***************************/

        /** Converts a GeoJSON feature to an OL feature **/
        this.gjToOlFeature = function (feature) {
            return geoJsonFormat.readFeature(feature, {
                dataProjection: proj4326,
                featureProjection: projMercator
            });
        };

        /** Returns the list of messages for the given pixel **/
        this.getMessagesForPixel = function (pixel) {
            var messageIds = {};
            var messages = [];
            this.map.forEachFeatureAtPixel(pixel, function(feature, layer) {
                var msg = feature.get('message');
                if ((layer == nwLayer || layer == nmLayer) && msg && messageIds[msg.id] === undefined) {
                    messages.push(msg);
                    messageIds[msg.id] = msg.id;
                }
            });
            return messages;
        };

        /***************************/
        /** Construct Layer       **/
        /***************************/

        var nwStyle = new ol.style.Style({
            fill: new ol.style.Fill({ color: 'rgba(255, 0, 255, 0.2)' }),
            stroke: new ol.style.Stroke({ color: '#8B008B', width: 1 }),
            image: new ol.style.Icon({
                anchor: [0.5, 0.5],
                scale: 0.3,
                src: '/img/nwnm/msi.png'
            })
        });

        var nmStyle = new ol.style.Style({
            fill: new ol.style.Fill({ color: 'rgba(255, 0, 255, 0.2)' }),
            stroke: new ol.style.Stroke({ color: '#8B008B', width: 1 }),
            image: new ol.style.Icon({
                anchor: [0.5, 0.5],
                scale: 0.3,
                src: '/img/nwnm/nm.png'
            })
        });

        var bufferedStyle = new ol.style.Style({
            fill: new ol.style.Fill({
                color: 'rgba(100, 50, 100, 0.2)'
            }),
            stroke: new ol.style.Stroke({
                color: 'rgba(100, 50, 100, 0.6)',
                width: 1
            })
        });

        // Construct the layer
        var nwFeatures = new ol.Collection();
        nwLayer = new ol.layer.Vector({
            title: 'Navigational Warnings',
            source: new ol.source.Vector({
                features: nwFeatures,
                wrapX: false
            }),
            style: function(feature) {
                var featureStyle;
                if (feature.get('parentFeatureId')) {
                    featureStyle = bufferedStyle;
                } else {
                    featureStyle = nwStyle;
                }
                return [ featureStyle ];
            }
        });
        nwLayer.setVisible(true);

        var nmFeatures = new ol.Collection();
        nmLayer = new ol.layer.Vector({
            title: 'Notices to Mariners',
            source: new ol.source.Vector({
                features: nmFeatures,
                wrapX: false
            }),
            style: function(feature) {
                var featureStyle;
                if (feature.get('parentFeatureId')) {
                    featureStyle = bufferedStyle;
                } else {
                    featureStyle = nmStyle;
                }
                return [ featureStyle ];
            }
        });
        nmLayer.setVisible(true);


        /***************************/
        /** Message List Handling **/
        /***************************/

        this.updateLayerFromMessageList = function (messages) {
            nwLayer.getSource().clear();
            nmLayer.getSource().clear();
            messageList = messages;
            generalMessages.length = 0;
            if (messageList && messageList.length > 0) {
                angular.forEach(messageList, function (message) {
                    if (message.geometry && message.geometry.features.length > 0) {

                        angular.forEach(message.geometry.features, function (gjFeature) {
                            var olFeature = that.gjToOlFeature(gjFeature);
                            olFeature.set('message', message);
                            if (message.mainType == 'NW') {
                                nwLayer.getSource().addFeature(olFeature);
                            } else {
                                nmLayer.getSource().addFeature(olFeature);
                            }
                        });
                    } else {
                        generalMessages.push(message);
                    }
                });
            }
        };


        // Reload the messages
        this.loadMessages = function () {
            loadTimer = undefined;

            // Load at most scope.maxAtonNo AtoN's
            NwNmService.getPublishedNwNm().success(that.updateLayerFromMessageList);
        };


        // When the map extent changes, reload the messages using a timer to batch up changes
        this.mapChanged = function () {
            if (nwnmLayer.getVisible()) {
                // Make sure we reload at most every half second
                if (loadTimer) {
                    $timeout.cancel(loadTimer);
                }
                loadTimer = $timeout(that.loadMessages, 500);
            }
        };


        nwnmLayer = new ol.layer.Group({
            title: 'NW-NM',
            layers: [ nwLayer, nmLayer ]
        });
        nwnmLayer.setVisible(true);

        // Listen for visibility changes of the layer
        nwnmLayer.on('change:visible', this.loadMessages);


        /***************************/
        /** Details dialog        **/
        /***************************/


        // Open the message details dialog
        this.showMessageInfo = function (message) {
            return $uibModal.open({
                controller: "MessageDialogCtrl",
                templateUrl: "/prototype/nw-nm/message-details-dialog.html",
                size: 'lg',
                resolve: {
                    message: function () {
                        return message;
                    }
                }
            });
        };


        /***************************/
        /** Map methods           **/
        /***************************/
        

        /** Attaches the layer of this service to the given map */
        this.addLayerToMap = function (map) {
            this.map = map;
            this.map.addLayer(nwnmLayer);
            this.map.on('moveend', this.mapChanged);

            // Show AtoN info dialog when an AtoN is clicked
            this.map.on('click', function(evt) {
                var messages = that.getMessagesForPixel(map.getEventPixel(evt.originalEvent));
                if (messages.length >= 1) {
                    that.showMessageInfo(messages[0]);
                }
            });
        };

    }])


    /*******************************************************************
     * Controller that handles displaying message details in a dialog
     *******************************************************************/
    .controller('MessageDialogCtrl', ['$scope', '$window', 'NwNmService', 'message',
        function ($scope, $window, NwNmService, message) {
            'use strict';

            $scope.warning = undefined;
            $scope.msg = message;

            /** Render the time of a message */
            $scope.renderTime = function () {
                // First check for a textual time description
                var time;
                var lang = 'en';
                var desc = $scope.msg.descs[0];
                if (desc && desc.time) {
                    time = desc.time;
                } else {
                    var from = moment($scope.msg.startDate);
                    time = from.locale(lang).format("lll");
                    if ($scope.msg.endDate) {
                        var to = moment($scope.msg.endDate);
                        var fromDate = from.locale(lang).format("ll");
                        var toDate = to.locale(lang).format("ll");
                        var toDateTime = to.locale(lang).format("lll");
                        if (fromDate == toDate) {
                            // Same dates
                            time += " - " + toDateTime.replace(toDate, '');
                        } else {
                            time += " - " + toDateTime;
                        }
                    }
                    time += ' ' + from.format('z');
                }
                return time;
            };


            $scope.coordinates = [];

            $scope.serializedCoordinates = function () {
                // Compute on-demand
                if ($scope.coordinates.length == 0 && $scope.msg.geometry) {
                    $scope.serializeCoordinates($scope.msg.geometry, $scope.coordinates, {}, 0, true);
                }
                return $scope.coordinates;
            };

            /** Serializes the coordinates of a GeoJSON geometry */
            $scope.serializeCoordinates = function (g, coords, props, index, includeCoord) {
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
                                index = $scope.serializeCoordinates(g[x], coords, props, index, includeCoord);
                            }
                        }
                    } else if (g.type == 'FeatureCollection') {
                        for (var x = 0; g.features && x < g.features.length; x++) {
                            index = $scope.serializeCoordinates(g.features[x], coords, props, index, includeCoord);
                        }
                    } else if (g.type == 'Feature') {
                        index = $scope.serializeCoordinates(g.geometry, coords, g.properties, index, includeCoord);
                    } else if (g.type == 'GeometryCollection') {
                        for (var x = 0; g.geometries && x < g.geometries.length; x++) {
                            index = $scope.serializeCoordinates(g.geometries[x], coords, props, index, includeCoord);
                        }
                    } else if (g.type == 'MultiPolygon') {
                        for (var p = 0; p < g.coordinates.length; p++) {
                            // For polygons, do not include coordinates for interior rings
                            for (var x = 0; x < g.coordinates[p].length; x++) {
                                index = $scope.serializeCoordinates(g.coordinates[p][x], coords, props, index, x == 0);
                            }
                        }
                    } else if (g.type == 'Polygon') {
                        // For polygons, do not include coordinates for interior rings
                        for (var x = 0; x < g.coordinates.length; x++) {
                            index = $scope.serializeCoordinates(g.coordinates[x], coords, props, index, x == 0);
                        }
                    } else if (g.type) {
                        index = $scope.serializeCoordinates(g.coordinates, coords, props, index, includeCoord);
                    }
                }
                return index;
            };


        }]);

