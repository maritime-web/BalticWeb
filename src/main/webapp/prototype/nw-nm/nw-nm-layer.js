/**
 * Defines the main NW-NM message layer
 *
 * TODO: Lifecycle management, convert to use backend data...
 */
angular.module('maritimeweb.nw-nm')

    /** Service for accessing NW-NMs **/
    .service('NwNmService', ['$http', '$uibModal',
        function($http, $uibModal) {

            /**
             * Returns the published NW-NM messages
             */
            this.getPublishedNwNm = function (instanceIds, lang, wkt) {
                var params = lang ? 'lang=' + lang : 'lang=en';
                angular.forEach(instanceIds, function (instanceId) {
                    params += '&instanceId=' + encodeURIComponent(instanceId);
                });
                if (wkt) {
                    params += '&wkt=' + encodeURIComponent(wkt);
                }

                return $http.get('/rest/nw-nm/messages?' + params);
            };


            /**
             * Get NW-NM services
             * TODO: Add parameter for geographical extent
             */
            this.getNwNmServices = function () {
                return $http.get('/rest/service/lookup');
            };


            /** Open the message details dialog **/
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

            /** Returns the area heading for the given message, i.e. two root-most areas **/
            this.getAreaHeading = function (message) {
                if (message && message.areas && message.areas.length > 0) {
                    var area = message.areas[0];
                    while (area.parent && area.parent.parent) {
                        area = area.parent;
                    }
                    var heading = '';
                    if (area.parent) {
                        heading += area.parent.descs[0].name + ' - ';
                    }
                    heading += area.descs[0].name;
                    return heading;
                }
                return '';
            }
        }])



    /**
     * The map-nw-nm-layer directive supports drawing a list of messages or a single message on a map layer
     */
    .directive('mapNwNmLayer', ['$rootScope', '$timeout', 'MapService', 'NwNmService',
        function ($rootScope, $timeout, MapService, NwNmService) {
            return {
                restrict: 'E',
                require: '^olMap',
                scope: {
                    name:           '@',

                    // Specify the "message" attribute for showing the geometry of a single message
                    message:        '=?',

                    // Specify the "messageList" and "services" attributes for showing and loading messages within map bounds
                    services:       '=?',
                    messageList:    '=?',

                    language:       '@',
                    showGeneral:    '@',
                    fitExtent:      '@',
                    maxZoom:        '@'
                },
                link: function(scope, element, attrs, ctrl) {
                    var olScope = ctrl.getOpenlayersScope();
                    var nwLayer;
                    var nmLayer;
                    var boundsLayer;
                    var nwnmLayer;
                    var loadTimer;
                    var maxZoom = scope.maxZoom ? parseInt(scope.maxZoom) : 12;

                    scope.showDetails = scope.message !== undefined;
                    scope.generalMessages = []; // Messages with no geometry
                    scope.language = scope.language || 'en';
                    scope.services = scope.services || [];


                    olScope.getMap().then(function(map) {

                        // Clean up when the layer is destroyed
                        scope.$on('$destroy', function() {
                            if (angular.isDefined(nwnmLayer)) {
                                map.removeLayer(nwnmLayer);
                            }
                            if (angular.isDefined(loadTimer)) {
                                $timeout.cancel(loadTimer);
                            }
                        });

                        /***************************/
                        /** NW and NM Layers      **/
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


                        var stylesArea = [
                            /* We are using two different styles for the polygons:
                             *  - The first style is for the polygons themselves.
                             *  - The second style is to draw the vertices of the polygons.
                             *    In a custom `geometry` function the vertices of a polygon are
                             *    returned as `MultiPoint` geometry, which will be used to render
                             *    the style.
                             */
                            new ol.style.Style({
                                stroke: new ol.style.Stroke({
                                    color: 'blue',
                                    width: 3
                                }),
                                fill: new ol.style.Fill({
                                    color: 'rgba(0, 0, 255, 0.1)'
                                })
                            }),
                            new ol.style.Style({
                                image: new ol.style.Circle({
                                    radius: 5,
                                    fill: new ol.style.Fill({
                                        color: 'darkblue'
                                    })
                                }),
                                geometry: function(feature) {
                                    // return the coordinates of the first ring of the polygon
                                    var coordinates = feature.getGeometry().getCoordinates()[0];
                                    return new ol.geom.MultiPoint(coordinates);
                                }
                            })
                        ];

                        var projMercator = 'EPSG:3857';
                        var proj4326 = 'EPSG:4326';

                        var geojsonObject = {
                            'type': 'FeatureCollection',
                            'crs': {
                                'type': 'name',
                                'properties': {
                                    'name': projMercator
                                }
                            },
                            'features': [
                                {
                                    'type': 'Feature',
                                    'geometry': {
                                        'type': 'Polygon',
                                        'coordinates': [
                                            [
                                                [14.0020751953125, 54.95869417101662],
                                                [15.0457763671875, 55.6930679264579],
                                                [16.5069580078125, 55.363502833950776],
                                                [14.633789062500002, 54.53383250794428],
                                                [14.414062499999998, 54.65794628989232],
                                                [14.3975830078125, 54.81334841741929],
                                                [14.161376953124998, 54.81334841741929],
                                                [14.0020751953125, 54.95869417101662]
                                            ]
                                        ]
                                    }
                                }]
                        };
                        /*
                        var gmlObject = '<p2:LinearRing xmlns:p2="http://www.opengis.net/gml">' +
                              '<p2:pos>14.0020751953125 54.95869417101662</p2:pos>' +
                            '<p2:pos>15.0457763671875 55.6930679264579</p2:pos>' +
                        '<p2:pos>16.5069580078125 55.363502833950776</p2:pos>' +
                        '<p2:pos>14.633789062500002 54.53383250794428</p2:pos>' +
                        '<p2:pos>14.414062499999998 54.65794628989232</p2:pos>' +
                        '<p2:pos>14.3975830078125 54.81334841741929</p2:pos>' +
                        '<p2:pos>14.161376953124998 54.81334841741929</p2:pos>' +
                        '<p2:pos>14.0020751953125 54.95869417101662</p2:pos>' +
                        '</p2:LinearRing>' ;

                        var source = new ol.source.Vector({
                            features: (new ol.format.GML3()).readFeatures(gmlObject, {
                                dataProjection: proj4326,
                                featureProjection: projMercator
                            })
                        });*/

                        var source = new ol.source.Vector({
                            features: (new ol.format.GeoJSON()).readFeatures(geojsonObject, {
                                dataProjection: proj4326,
                                featureProjection: projMercator
                            })
                        });


                        var layerGeoJSONmsi = new ol.layer.Vector({
                            title: 'Navigational Warnings - boundaries',
                            source: source,
                            style: stylesArea
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


                        /** Updates the NW and NM layers from the given message list */
                        scope.updateLayerFromMessageList = function (messages) {
                            nwLayer.getSource().clear();
                            nmLayer.getSource().clear();


                            scope.messageList.length = 0;
                            scope.generalMessages.length = 0;
                            if (messages && messages.length > 0) {
                                scope.messageList.push.apply(scope.messageList, messages);
                                angular.forEach(scope.messageList, scope.addMessageToLayer);
                            }
                        };


                        /** Adds a single message to the layer **/
                        scope.addMessageToLayer = function (message) {
                            if (message.geometry && message.geometry.features.length > 0) {

                                angular.forEach(message.geometry.features, function (gjFeature) {
                                    var olFeature = MapService.gjToOlFeature(gjFeature);
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
                        };




                        /** Loads the messages from the server **/
                        scope.loadMessages = function () {
                            loadTimer = undefined;

                            // Determine the selected service instances
                            var instanceIds = [];
                            angular.forEach(scope.services, function (service) {
                                if (service.selected) {
                                    instanceIds.push(service.instanceId);
                                }
                            });

                            var extent = map.getView().calculateExtent(map.getSize());
                            var wkt = MapService.extentToWkt(extent);

                            // Load at most scope.maxAtonNo AtoN's
                            NwNmService
                                .getPublishedNwNm(instanceIds, scope.language, wkt)
                                .success(scope.updateLayerFromMessageList);
                        };


                        /** When the map extent changes, reload the messages using a timer to batch up changes **/
                        scope.mapChanged = function () {
                            // TODO: Check if either nwLayer or nmLayer is visible. Not working. Why?
                            if (nwnmLayer.getVisible()) {
                                // Make sure we reload at most every half second
                                if (loadTimer) {
                                    $timeout.cancel(loadTimer);
                                }
                                loadTimer = $timeout(scope.loadMessages, 500);
                            }
                        };

                        // Monitor changes to the service list and selection
                        if (!scope.showDetails) {
                            scope.$watch("services", scope.mapChanged, true);
                        }


                        /** Returns the list of messages for the given pixel **/
                        scope.getMessagesForPixel = function (pixel) {
                            var messageIds = {};
                            var messages = [];
                            map.forEachFeatureAtPixel(pixel, function(feature, layer) {
                                var msg = feature.get('message');
                                if ((layer == nwLayer || layer == nmLayer) && msg && messageIds[msg.id] === undefined) {
                                    messages.push(msg);
                                    messageIds[msg.id] = msg.id;
                                }
                            });
                            return messages;
                        };


                        /***************************/
                        /** Map creation          **/
                        /***************************/

                        // Construct NW-NM layer
                        nwnmLayer = new ol.layer.Group({
                            title: scope.name || 'NW-NM',
                            layers: [ layerGeoJSONmsi, nwLayer, nmLayer ]
                        });
                        nwnmLayer.setVisible(true);
                        map.addLayer(nwnmLayer);


                        if (scope.showDetails) {

                            scope.addMessageToLayer(scope.message);

                        } else {
                            // Listen for visibility changes of the layer
                            nwnmLayer.on('change:visible', scope.loadMessages);

                            map.on('moveend', scope.mapChanged);

                            map.on('click', function(evt) {
                                var messages = scope.getMessagesForPixel(map.getEventPixel(evt.originalEvent));
                                if (messages.length >= 1) {
                                    NwNmService.showMessageInfo(messages[0]);
                                }
                            });
                        }

                        if (scope.fitExtent == 'true') {
                            var fitExtent = false;
                            var extent = ol.extent.createEmpty();
                            if (nwLayer.getSource().getFeatures().length > 0) {
                                ol.extent.extend(extent, nwLayer.getSource().getExtent());
                                fitExtent = true;
                            }
                            if (nmLayer.getSource().getFeatures().length > 0) {
                                ol.extent.extend(extent, nmLayer.getSource().getExtent());
                                fitExtent = true;
                            }
                            if (fitExtent) {
                                map.getView().fit(extent, map.getSize(), {
                                    padding: [20, 20, 20, 20],
                                    maxZoom: maxZoom
                                });
                            }
                        }

                    });
                }
            };
        }])


    /*******************************************************************
     * Controller that handles displaying message details in a dialog
     *******************************************************************/
    .controller('MessageDialogCtrl', ['$scope', '$window', 'MapService', 'message',
        function ($scope, $window, MapService, message) {
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
                    MapService.serializeCoordinates($scope.msg.geometry, $scope.coordinates, {}, 0, true);
                }
                return $scope.coordinates;
            };
        }]);

