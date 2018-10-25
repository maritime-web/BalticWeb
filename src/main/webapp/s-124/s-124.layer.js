(function () {
/**
 * Defines the main S-124 message layer. navigational warnings
 *
 */
angular.module('maritimeweb.s-124')

    /**
     * Supports drawing a list of messages or a single message on a map layer
     */
    .directive('mapS124Layer', ['$rootScope', '$timeout', 'MapService', 'S124Service',
        function ($rootScope, $timeout, MapService, S124Service) {
            return {
                restrict: 'E',
                require: '^olMap',
                template: "",
                scope: {
                    name:           '@',

                    // Specify the "message" attribute for showing the geometry of a single message
                    message:        '=?',

                    // Specify the "messageList" and "services" attributes for showing and loading messages within map bounds
                    services:       '=?',
                    messageList:    '=?',

                    fitExtent:      '@',
                    maxZoom:        '@'
                },
                link: function(scope, element, attrs, ctrl) {
                    var olScope = ctrl.getOpenlayersScope();
                    var nwLayer;
                    var boundaryLayer;
                    var nwBoundaryLayer;
                    var maxZoom = scope.maxZoom ? parseInt(scope.maxZoom) : 12;

                    scope.showDetails = scope.message !== undefined;
                    scope.generalMessages = []; // Messages with no geometry
                    scope.services = scope.services || [];

                    olScope.getMap().then(function(map) {

                        // Clean up when the layer is destroyed
                        scope.$on('$destroy', function() {
                            if (angular.isDefined(nwBoundaryLayer)) {
                                map.removeLayer(nwBoundaryLayer);
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
                                src: '/img/nwnm/nw.png'
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


                        var boundaryStyle = new ol.style.Style({
                                stroke: new ol.style.Stroke({
                                    color: 'rgba(255, 215, 0, 0.5)',
                                    width: 1
                                }),
                                fill: new ol.style.Fill({
                                    color: 'rgba(255, 215, 0, 0.05)'
                                })
                            });



                        // Construct the boundary layers
                        boundaryLayer = new ol.layer.Vector({
                            title: 'Navigational Warnings',
                            zIndex: 11,
                            source: new ol.source.Vector({
                                features: new ol.Collection(),
                                wrapX: false
                            }),
                            style: [ boundaryStyle ]
                        });
                        boundaryLayer.setZIndex(11);
                        boundaryLayer.setVisible(true);


                        // Construct the NW layers
                        nwLayer = new ol.layer.Vector({
                            name: 'Navigational Warnings',
                            title: 'Navigational Warnings',
                            source: new ol.source.Vector({
                                features: new ol.Collection(),
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
                        nwLayer.setZIndex(11);
                        nwLayer.setVisible(true);

                        /***************************/
                        /** Message List Handling **/
                        /***************************/


                        /** Updates the service boundary layer **/
                        scope.updateServiceBoundaryLayer = function () {

                            boundaryLayer.getSource().clear();

                            if (scope.services && scope.services.length > 0) {
                                angular.forEach(scope.services, function (service) {
                                    if (service.selected && service.boundary) {
                                        try {
                                            var olFeature = MapService.wktToOlFeature(service.boundary);
                                            boundaryLayer.getSource().addFeature(olFeature);
                                        } catch (error) {
                                            console.error("Error creating boundary for " + service.boundary);
                                        }
                                    }
                                });
                            }
                        };


                        /** Returns the list of GeoJson features of all message parts **/
                        scope.getMessagePartFeatures = function (message) {
                            var g = [];
                            if (message && message.navigationalWarningFeaturePart && message.navigationalWarningFeaturePart.length > 0) {
                                angular.forEach(message.navigationalWarningFeaturePart, function (part) {
                                    if (part.geometries && part.geometries.length > 0) {
                                        g.push.apply(g, part.geometries);
                                    }
                                });
                            }
                            return g;
                        };


                        /** Adds a single message to the layer **/
                        scope.addMessageToLayer = function (message) {
                            if (message.navigationalWarningFeaturePart) {
                                var features = scope.getMessagePartFeatures(message);
                                angular.forEach(features, function (gjFeature) {
                                    try {
                                        var olFeature = MapService.gjToOlFeature(gjFeature);
                                        olFeature.set('message', message);
                                        nwLayer.getSource().addFeature(olFeature);
                                    } catch (err) {
                                    }
                                });
                            } else {
                                scope.generalMessages.push(message);
                            }
                        };


                        /** Updates the message and boundary layers from the current NW-NM service selection **/
                        scope.updateMessageListLayers = function () {

                            nwLayer.getSource().clear();
                            scope.generalMessages.length = 0;

                            if (scope.messageList && scope.messageList.length > 0) {
                                angular.forEach(scope.messageList, scope.addMessageToLayer);
                            }

                        };


                        /** Returns the list of messages for the given pixel **/
                        scope.getMessagesForPixel = function (pixel) {
                            var messageIds = {};
                            var messages = [];
                            map.forEachFeatureAtPixel(pixel, function(feature, layer) {
                                var msg = feature.get('message');
                                if (layer === nwLayer && msg && messageIds[msg.id] === undefined) {
                                    messages.push(msg);
                                    messageIds[msg.id] = msg.id;
                                }
                            });
                            return messages;
                        };


                        /***************************/
                        /** Map creation          **/
                        /***************************/

                        // Construct NW layer
                        nwBoundaryLayer = new ol.layer.Group({
                            title: scope.name || 'NW',
                            zIndex: 11,
                            layers: [ boundaryLayer, nwLayer ]
                        });
                        nwBoundaryLayer.setZIndex(11);
                        nwBoundaryLayer.setVisible(true);

                        map.addLayer(nwBoundaryLayer);


                        if (scope.showDetails) {

                            scope.addMessageToLayer(scope.message);

                        } else {

                            // Listen for changes that should cause updates to the layers
                            scope.$watch("services", scope.updateServiceBoundaryLayer, true);
                            scope.$watch("messageList", scope.updateMessageListLayers, true);
                            nwBoundaryLayer.on('change:visible', scope.updateMessageListLayers);


                            map.on('click', function(evt) {
                                var messages = scope.getMessagesForPixel(map.getEventPixel(evt.originalEvent));
                                if (messages.length >= 1) {
                                    S124Service.showMessageInfo(messages[0]);
                                }
                            });
                        }

                        if (scope.fitExtent === 'true') {
                            var fitExtent = false;
                            var extent = ol.extent.createEmpty();
                            if (nwLayer.getSource().getFeatures().length > 0) {
                                ol.extent.extend(extent, nwLayer.getSource().getExtent());
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
})();
