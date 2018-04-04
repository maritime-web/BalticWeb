/**
 * Defines a route layer
 */

angular.module('maritimeweb.route')

/** Service for displaying a route on a map **/

    .directive('routertz', ['$rootScope', '$timeout', 'MapService', 'VesselService', 'growl', '$log', '$window',
        function ($rootScope, $timeout, MapService, VesselService, growl, $log, $window) {
            return {
                restrict: 'E',
                require: '^olMap',
                template:
                '<div id="waypoint-route-info" class="ng-cloak"></div>' +
                    '<div id="waypoint-popup" class="ol-popup">' +
                    '<a href="#" id="waypoint-popup-closer" class="ol-popup-closer"></a>' +
                    '<h3 class="popover-title">{{waypoint.name}}</h3>' +
                    '<div id="waypoint-popover-content" class="popover-content">' +
                        '<p>Waypoint number: {{waypoint.id}}</p>' +
                        '<p>Position: {{ toLonLat(waypoint.lon, waypoint.lat) | lonlat:{ decimals : 3, pp: true} }} </p>' +
                        '<p>Eta: {{waypoint.eta}}</p>' +
                        '<p>Time: {{waypoint.etatimeago}}</p>' +
                        /*'<p>Radius: {{waypoint.radius}}</p>' +
                        '<p>Speed: {{waypoint.speed}}</p>' +
                        '<p>Speed Min: {{waypoint.speedmin}}</p>' +
                        '<p>Speed Max: {{waypoint.speedmax}}</p>' +
                        '<p>Geometry Type: {{waypoint.geometrytype}}</p>' +
                        '<p>Portside XTD: {{waypoint.portsidextd}}</p>' +
                        '<p>Starboard XTD: {{waypoint.starboardxtd}}</p>' +*/
                    '</div>' +
                '</div>' +
                "<div class='hidden-xs hidden-sm message-details-route col-md-3 col-lg-3 ng-cloak'>" +
                "<btn ng-if='!animating' class='btn btn-success' id='start-animation' ng-click='startAnimation()'> <i class='fa fa-play' aria-hidden='true'></i> </btn> " +
                "<btn ng-if='animating' class='btn btn-danger' id='start-animation' ng-click='stopAnimation()' tooltip='stop animation' data-toggle='tooltip'  " +
                " data-placement='right' title='stop animation'> <i class='fa fa-stop' aria-hidden='true'></i> </btn><br>" +
                "<label for='speed'>" + " Animation speed:&nbsp;" + "<input id='speed' type='range' min='1' max='50' step='1' value='10'> " + "</label><br>" +
                "<span class='label label-primary' ng-if='activeRouteName'>{{activeRouteName}}</span><br>" +
                "<span class='label label-primary'>{{activeRouteTS}}</span><br>" +
                "<span class='label label-primary'>{{activeRouteTSetaTimeAgo}}</span><br>" +
                "<span class='label label-primary'>{{activeRoutePoint | lonlat:{ decimals : 3, pp: true} }}</span><br>" +
                "<span class='label label-primary'>{{activeRouteSpeed }}</span><br>" +

                "</div>"
                ,
                scope: {
                    name: '@',
                    autoplay: '=?',
                    points: '=?',
                    features: '=?',
                    animatedfeatures: '=?'

                },
                link: function (scope, element, attrs, ctrl) {
                    $log.info("scope.showgraphSidebar" + scope.showgraphSidebar);
                    $log.info("$rootScope.showgraphSidebar" + $rootScope.showgraphSidebar);
                    $rootScope.showgraphSidebar = false; // disable sidebar.

                    $log.log("route RTZ got route points=" +  scope.points.length +
                        " feat=" + scope.features.length +
                        "autoplay=" + scope.autoplay
                    );

                    var  animatedMarkerStyle = new ol.style.Style({
                        image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
                            anchor: [0.5, 0.5],
                            anchorXUnits: 'fraction',
                            anchorYUnits: 'fraction',
                            opacity: 0.85,
                            rotation: 0,
                            rotateWithView: false,
                            src: 'img/vessel_green_moored.png'
                        }))
                    });

                    var markerStyle = new ol.style.Style({
                        image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
                            anchor: [0.5, 0.5],
                            anchorXUnits: 'fraction',
                            anchorYUnits: 'fraction',
                            opacity: 0.85,
                            rotation:  0,
                            rotateWithView: false,
                            src: 'img/vessel_green.png'
                        }))
                    });

                    animatedMarkerStyle.getImage().load();
                    markerStyle.getImage().load();

                    var olScope = ctrl.getOpenlayersScope();

                    var styles = {
                        'route': new ol.style.Style({
                            stroke: new ol.style.Stroke({
                                width: 6, color: [237, 212, 0, 0.8]
                            })
                        }),
                        'icon': new ol.style.Style({
                            image: new ol.style.Icon({
                                anchor: [0.5, 1],
                                src: 'data/icon.png'
                            })
                        }),
                        'geoMarker': new ol.style.Style({
                            image: new ol.style.Circle({
                                radius: 7,
                                snapToPixel: false,
                                fill: new ol.style.Fill({color: 'black'}),
                                stroke: new ol.style.Stroke({
                                    color: 'white', width: 2
                                })
                            })
                        }),
                        endStyle: new ol.style.Style({
                            image: new ol.style.Circle({
                                radius: 6,
                                stroke: new ol.style.Stroke({
                                    color: 'darkred',
                                    width: 2
                                }),
                                fill: new ol.style.Fill({
                                    color: [255, 0, 0, 0.5]
                                })
                            }),
                            text: new ol.style.Text({
                                text: 'END', // attribute code
                                font: 'bold 14 Verdana',
                                offsetY: 20,
                                stroke: new ol.style.Stroke({color: "white", width: 5})
                            })
                        }),
                        startStyle: new ol.style.Style({
                            image: new ol.style.Circle({
                                radius: 6,
                                stroke: new ol.style.Stroke({
                                    color: 'darkred',
                                    width: 2
                                }),
                                fill: new ol.style.Fill({
                                    color: [255, 0, 0, 0.5]
                                })
                            }),
                            text: new ol.style.Text({
                                text: 'START', // attribute code
                                font: 'bold 14 Verdana',
                                offsetY: 20,
                                stroke: new ol.style.Stroke({color: "white", width: 5})
                                //rotation: 45
                            })

                        })
                    };

                    var routeLayers;

                    var animationLayer = new ol.layer.Vector({
                        source: new ol.source.Vector({
                            features: []
                        }),
                        updateWhileAnimating: true,
                        style: function (feature) {
                        // hide geoMarker if animation is active
                        if (scope.animating && feature.get('type') === 'geoMarker') {
                            return null;
                        }
                        return styles[feature.get('type')];
                    }
                    });                 var pathLayer = new ol.layer.Vector({
                        source: new ol.source.Vector({
                            features: []
                        }),
                        style: new ol.style.Style({
                            stroke: new ol.style.Stroke({
                                //lineDash: [10, 20, 0, 20],
                                lineDash: [5, 10, 0, 10],
                                lineJoin: 'miter',
                                width: 2,
                                color: [255, 0, 0, 0.8]
                            })
                        })
                    });

                    var vectorSource = new ol.source.Vector({
                        features: []
                    });

                    var routeFeatureLayer = new ol.layer.Vector({
                        name: "routeVectorLayer",
                        title: "route",
                        source: vectorSource,
                        visible: true
                    });

                    routeLayers = new ol.layer.Group({
                        title: 'Route',
                        layers: [pathLayer, animationLayer, routeFeatureLayer],
                        visible: true
                    });

                    var pathLayer = new ol.layer.Vector({
                        source: new ol.source.Vector({
                            features: []
                        }),
                        style: new ol.style.Style({
                            stroke: new ol.style.Stroke({
                                //lineDash: [10, 20, 0, 20],
                                lineDash: [5, 10, 0, 10],
                                lineJoin: 'miter',
                                width: 2,
                                color: [255, 0, 0, 0.8]
                            })
                        })
                    });

                    var vectorSource = new ol.source.Vector({
                        features: []
                    });

                    var routeFeatureLayer = new ol.layer.Vector({
                        name: "routeVectorLayer",
                        title: "route",
                        source: vectorSource,
                        visible: true
                    });

                    routeLayers = new ol.layer.Group({
                        title: 'Route',
                        layers: [pathLayer, animationLayer, routeFeatureLayer],
                        visible: true
                    });

                    /**
                     * Elements that make up the popup.
                     */
                    var container = document.getElementById('waypoint-popup');
                    var content = document.getElementById('waypoint-popup-content');
                    var closer = document.getElementById('waypoint-popup-closer');


                    /**
                     * Create an overlay to anchor the popup to the map.
                     */
                    var overlay = new ol.Overlay(/** @type {olx.OverlayOptions} */ ({
                        element: container,
                        autoPan: true,
                        autoPanAnimation: {
                            duration: 250
                        }
                    }));


                    scope.populatePopupWaypoint = function (feature) {
                        scope.waypoint = {};
                        scope.waypoint.id = feature.get('id');
                        scope.waypoint.name = feature.get('wayname');
                        scope.waypoint.lon = feature.get('lon');
                        scope.waypoint.lat = feature.get('lat');
                        scope.waypoint.radius = feature.get('radius');
                        scope.waypoint.eta = feature.get('eta');
                        scope.waypoint.etatimeago = feature.get('etatimeago');
                        scope.waypoint.speed = feature.get('speed');
                        scope.waypoint.speedmin = feature.get('speedmin');
                        scope.waypoint.speedmax = feature.get('speedmax');
                        scope.waypoint.geometrytype = feature.get('geometrytype');
                        scope.waypoint.portsidextd = feature.get('portsidextd');
                        scope.waypoint.starboardxtd = feature.get('starboardxtd');
                        $log.debug("#" + feature);
                    };
                    olScope.getMap().then(function (map) {

                        routeFeatureLayer.getSource().addFeatures(scope.features);
                        map.getView().setCenter(routeFeatureLayer.getSource().getFeatures()[0].getGeometry().getCoordinates());
                        // Clean up when the layer is destroyed
                        scope.$on('$destroy', function () {
                            if (angular.isDefined(routeLayers)) {
                                map.removeLayer(routeLayers);
                            }
                        });

                        var routeFeature = new ol.Feature({
                            type: 'route',
                            geometry: new ol.geom.LineString(scope.points)
                        });

                        var startMarker = scope.features[0];
                        var endMarker = scope.features[scope.features.length - 1];

                        scope.animating = false;

                        /**
                         * Animation of the route.
                         */

                        var index = 0;
                        var renders = 0;
                        var speed, now, orgIndexVal;
                        var speedInput = document.getElementById('speed');
                        var routeLength = scope.animatedfeatures.length;

                        var moveFeature = function (event) {
                            var vectorContext = event.vectorContext;
                            var frameState = event.frameState;


                            if (scope.animating) {
                                var elapsedTime = frameState.time - now;
                                //$log.log("elapsedTime:" + elapsedTime + " renders" + renders++);
                                 index = Math.round(speed * elapsedTime / 1000);

                                if (index >= scope.animatedfeatures.length) {
                                    index = 0; // rewind, and loop
                                    renders = 0;
                                    scope.stopAnimation(true);
                                }

                                var feature = scope.animatedfeatures[index];
                                var retrievedStyle = feature.getStyle();
                                retrievedStyle.getImage().load();

                                scope.activeRoutePoint = feature.get('position');
                                scope.activeRouteName = feature.get('name');
                                scope.activeRouteSpeed = feature.get('speed');
                                scope.activeRouteTS = feature.get('eta');
                                scope.activeRouteTSetaTimeAgo = feature.get('etatimeago');
                                vectorContext.drawFeature(feature, retrievedStyle);
                             }

                            map.render();      // tell OL3 to continue the postcompose animation
                        };

                        scope.startAnimation = function () {
                            if (scope.animating) {
                                scope.stopAnimation(false);
                            } else {
                                scope.animating = true;
                                now = new Date().getTime();
                                speed = speedInput.value;
                                map.on('postcompose', moveFeature);
                                map.render();
                            }
                        };

                        if (scope.autoplay) {
                            $timeout(function(){scope.startAnimation()}, 5000);
                        }
                        /**
                         * @param {boolean} ended end of animation.
                         */
                        scope.stopAnimation = function (ended) {
                            scope.animating = false;
                        };

                        /**
                         * Clickable waypoints pop-up content
                         */

                        var elm = document.getElementById('waypoint-route-info');

                        var popup = new ol.Overlay({
                            element: elm,
                            positioning: 'bottom-center',
                            stopEvent: false
                        });
                        map.addOverlay(popup);



                        /**
                         * Add a click handler to hide the popup.
                         * @return {boolean} Don't follow the href.
                         */
                        closer.onclick = function () {
                            overlay.setPosition(undefined);
                            closer.blur();
                            return false;
                        };

                        map.addOverlay(overlay);

                        /**
                         * Add a click handler to the map to render the popup.
                         */
                        map.on('singleclick', function (evt) {

                                var feature = map.forEachFeatureAtPixel(evt.pixel, function (feature, layer) {
                                    return feature;
                                }, null, function (layer) {
                                    return layer === routeFeatureLayer;
                                });

                                if (feature) {

                                    var coordinate = evt.coordinate;
                                    $rootScope.activeWayPoint = feature.get('id');
                                    $rootScope.$apply();

                                    // well, the directive watches the rootscopes directive so we don't have to do anymore...
                                    // scope.populatePopupWaypoint(feature);
                                    //overlay.setPosition(coordinate);
                                } else {
                                    overlay.setPosition(undefined);
                                    closer.blur();
                                }
                        });

                        map.addLayer(routeLayers);
                    });

                    /** Returns the lat-lon attributesas json-object */
                    scope.toLonLat = function (long, lati) {
                        return {lon: long, lat: lati};
                    };

                    // while watch if a new active waypoint has been selected via the chart or the table. If so, we pop the popup for that Openlayer Feature.
                    $rootScope.$watch("activeWayPoint", function(newValue, oldValue) {
                        if (newValue)
                            olScope.getMap().then(function (map) {

                                var activeFeature = routeFeatureLayer.getSource().getFeatureById(newValue);
                                $log.debug("ActiveFeature  ID=" + activeFeature.getId());
                                scope.stopAnimation();

                                if (activeFeature) {
                                    var coordinate = activeFeature.getGeometry().getCoordinates();
                                    scope.populatePopupWaypoint(activeFeature);
                                    overlay.setPosition(coordinate);
                                } else {
                                    overlay.setPosition(undefined);
                                    closer.blur();
                                }
                                $window.scrollTo(0, 0);
                            });
                    }, true);

                    // watch if a new RTZ route has been uploaded by the end user.
                    scope.$watch("points", function(newValue, oldValue) {
                        if (newValue)
                            olScope.getMap().then(function (map) {
                                animationLayer.getSource().clear();
                                pathLayer.getSource().clear();
                                routeFeatureLayer.getSource().clear();

                                var routeFeature = new ol.Feature({
                                    type: 'route',
                                    geometry: new ol.geom.LineString(scope.points)
                                });

                                var startMarker = scope.features[0];
                                var endMarker = scope.features[scope.features.length - 1];

                                startMarker.setStyle(styles['startStyle']);
                                endMarker.setStyle(styles['endStyle']);


                                //animationLayer.getSource().addFeatures(scope.animatedfeatures);
                                pathLayer.getSource().addFeature(routeFeature);
                                routeFeatureLayer.getSource().addFeatures(scope.features);
                                routeFeatureLayer.getSource().addFeature(startMarker);
                                routeFeatureLayer.getSource().addFeature(endMarker);

                                var extent = routeFeatureLayer.getSource().getExtent();
                                map.getView().fit(extent, map.getSize());  // automatically zoom and pan the map to fit my features
                            });
                    }, true);
                }
            }
        }])

    .directive('optimizedroutertz', ['$rootScope', '$timeout', 'MapService', 'VesselService', 'growl', '$log', '$window',
        function ($rootScope, $timeout, MapService, VesselService, growl, $log, $window) {
            return {
                restrict: 'E',
                require: '^olMap',
                template:
                '<div id="waypoint-optimized-route-info" class="ng-cloak"></div>' +
                '<div id="optimized-waypoint-popup" class="ol-popup">' +
                '<a href="#" id="optimized-waypoint-popup-closer" class="ol-popup-closer"></a>' +
                '<h3 class="popover-title">{{waypoint.name}}</h3>' +
                '<div id="optimized-waypoint-popover-content" class="popover-content">' +
                '<p>Optimized Route</p>' +
                '<p>Waypoint number: {{waypoint.id}}</p>' +
                '<p>Position: {{ toLonLat(waypoint.lon, waypoint.lat) | lonlat:{ decimals : 3, pp: true} }} </p>' +
                '<p>Eta: {{waypoint.eta}}</p>' +
                '<p>Time: {{waypoint.etatimeago}}</p>' +
                /*'<p>Radius: {{waypoint.radius}}</p>' +
                 '<p>Speed: {{waypoint.speed}}</p>' +
                 '<p>Speed Min: {{waypoint.speedmin}}</p>' +
                 '<p>Speed Max: {{waypoint.speedmax}}</p>' +
                 '<p>Geometry Type: {{waypoint.geometrytype}}</p>' +
                 '<p>Portside XTD: {{waypoint.portsidextd}}</p>' +
                 '<p>Starboard XTD: {{waypoint.starboardxtd}}</p>' +*/
                '</div>' +
                '</div>' +
                "<div ng-if='animatedfeatures.length>0' class='hidden-xs hidden-sm message-details-optimized-route col-md-3 col-lg-3 ng-cloak'>" +
                "<h3>Optimized route</h3> " +
                "<btn ng-if='!animating' class='btn btn-success' id='start-animation' ng-click='startAnimation()'> <i class='fa fa-play' aria-hidden='true'></i> </btn> " +
                "<btn ng-if='animating' class='btn btn-danger' id='start-animation' ng-click='stopAnimation()' tooltip='stop animation' data-toggle='tooltip'  " +
                " data-placement='right' title='stop animation'> <i class='fa fa-stop' aria-hidden='true'></i> </btn><br>" +
                "<label for='speed'>" + " Animation speed:&nbsp;" + "<input id='speed' type='range' min='1' max='50' step='1' value='10'> " + "</label><br>" +
                "<span class='label label-primary' ng-if='activeRouteName'>{{activeRouteName}}</span><br>" +
                "<span class='label label-primary'>{{activeRouteTS}}</span><br>" +
                "<span class='label label-primary'>{{activeRouteTSetaTimeAgo}}</span><br>" +
                "<span class='label label-primary'>{{activeRoutePoint | lonlat:{ decimals : 3, pp: true} }}</span><br>" +
                "<span class='label label-primary'>{{activeRouteSpeed }}</span><br>" +

                "</div>"
                ,
                scope: {
                    name: '@',
                    autoplay: '=?',
                    points: '=?',
                    features: '=?',
                    animatedfeatures: '=?'

                },
                link: function (scope, element, attrs, ctrl) {
                    $log.info("scope.showgraphSidebar" + scope.showgraphSidebar);
                    $log.info("$rootScope.showgraphSidebar" + $rootScope.showgraphSidebar);
                    $rootScope.showgraphSidebar = false; // disable sidebar.

                    $log.log("route RTZ got route points=" +  scope.points.length +
                        " feat=" + scope.features.length +
                        "autoplay=" + scope.autoplay
                    );

                    // We need to create this object in order to satisfy OpenLayers
                    var  animatedMarkerStyle = new ol.style.Style({
                        image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
                            anchor: [0.5, 0.5],
                            anchorXUnits: 'fraction',
                            anchorYUnits: 'fraction',
                            opacity: 0.85,
                            rotation: 0,
                            rotateWithView: false,
                            src: 'img/vessel_orange_moored.png'
                        }))
                    });

                    var markerStyle = new ol.style.Style({
                        image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
                            anchor: [0.5, 0.5],
                            anchorXUnits: 'fraction',
                            anchorYUnits: 'fraction',
                            opacity: 0.85,
                            rotation:  0,
                            rotateWithView: false,
                            src: 'img/vessel_orange.png'
                        }))
                    });

                    animatedMarkerStyle.getImage().load();
                    markerStyle.getImage().load();

                    var olScope = ctrl.getOpenlayersScope();

                    var styles = {
                        'route': new ol.style.Style({
                            stroke: new ol.style.Stroke({
                                width: 6, color: [255, 165, 0, 0.8]
                            })
                        }),
                        'icon': new ol.style.Style({
                            image: new ol.style.Icon({
                                anchor: [0.5, 1],
                                src: 'data/icon.png'
                            })
                        }),
                        'geoMarker': new ol.style.Style({
                            image: new ol.style.Circle({
                                radius: 7,
                                snapToPixel: false,
                                fill: new ol.style.Fill({color: 'black'}),
                                stroke: new ol.style.Stroke({
                                    color: 'white', width: 2
                                })
                            })
                        }),
                        endStyle: new ol.style.Style({
                            image: new ol.style.Circle({
                                radius: 6,
                                stroke: new ol.style.Stroke({
                                    color: [255, 165, 0, 0.2],
                                    width: 1
                                }),
                                fill: new ol.style.Fill({
                                    color: [255, 140, 0, 0.1]
                                })
                            }),
                            text: new ol.style.Text({
                                text: 'END', // attribute code
                                font: 'bold 14 Verdana',
                                offsetY: 20,
                                stroke: new ol.style.Stroke({color: "white", width: 5})
                            })
                        }),
                        startStyle: new ol.style.Style({
                            image: new ol.style.Circle({
                                radius: 6,
                                stroke: new ol.style.Stroke({
                                    color:  [255, 165, 0, 0.1],
                                    width: 1
                                }),
                                fill: new ol.style.Fill({
                                    color: [255, 140, 0, 0.5]
                                })
                            }),
                            text: new ol.style.Text({
                                text: 'START', // attribute code
                                font: 'bold 14 Verdana',
                                offsetY: 20,
                                stroke: new ol.style.Stroke({color: "white", width: 5})
                                //rotation: 45
                            })

                        })
                    };

                    var routeLayers;

                    var animationLayer = new ol.layer.Vector({
                        source: new ol.source.Vector({
                            features: []
                        }),
                        updateWhileAnimating: true,
                        style: function (feature) {
                            // hide geoMarker if animation is active
                            if (scope.animating && feature.get('type') === 'geoMarker') {
                                return null;
                            }
                            return styles[feature.get('type')];
                        }
                    });
                    var pathLayer = new ol.layer.Vector({
                        source: new ol.source.Vector({
                            features: []
                        }),
                        style: new ol.style.Style({
                            stroke: new ol.style.Stroke({
                                //lineDash: [10, 20, 0, 20],
                                lineDash: [5, 10, 0, 10],
                                lineJoin: 'miter',
                                width: 2,
                                color: [255, 140, 0, 0.8]
                            })
                        })
                    });

                    var vectorSource = new ol.source.Vector({
                        features: []
                    });

                    var routeFeatureLayer = new ol.layer.Vector({
                        name: "routeVectorLayer",
                        title: "route",
                        source: vectorSource,
                        visible: true
                    });

                    routeLayers = new ol.layer.Group({
                        title: 'Route',
                        layers: [pathLayer, animationLayer, routeFeatureLayer],
                        visible: true
                    });

                    var pathLayer = new ol.layer.Vector({
                        source: new ol.source.Vector({
                            features: []
                        }),
                        style: new ol.style.Style({
                            stroke: new ol.style.Stroke({
                                //lineDash: [10, 20, 0, 20],
                                lineDash: [5, 10, 0, 10],
                                lineJoin: 'miter',
                                width: 2,
                                color: [255, 140, 0, 0.8]
                            })
                        })
                    });

                    var vectorSource = new ol.source.Vector({
                        features: []
                    });

                    var routeFeatureLayer = new ol.layer.Vector({
                        name: "routeVectorLayer",
                        title: "route",
                        source: vectorSource,
                        visible: true
                    });

                    routeLayers = new ol.layer.Group({
                        title: 'Route',
                        layers: [pathLayer, animationLayer, routeFeatureLayer],
                        visible: true
                    });

                    /**
                     * Elements that make up the popup.
                     */
                    var container = document.getElementById('optimized-waypoint-popup');
                    var content = document.getElementById('optimized-waypoint-popup-content');
                    var closer = document.getElementById('optimized-waypoint-popup-closer');


                    /**
                     * Create an overlay to anchor the popup to the map.
                     */
                    var overlay = new ol.Overlay(/** @type {olx.OverlayOptions} */ ({
                        element: container,
                        autoPan: true,
                        autoPanAnimation: {
                            duration: 250
                        }
                    }));


                    scope.populatePopupWaypoint = function (feature) {
                        scope.waypoint = {};
                        scope.waypoint.id = feature.get('id');
                        scope.waypoint.name = feature.get('wayname');
                        scope.waypoint.lon = feature.get('lon');
                        scope.waypoint.lat = feature.get('lat');
                        scope.waypoint.radius = feature.get('radius');
                        scope.waypoint.eta = feature.get('eta');
                        scope.waypoint.etatimeago = feature.get('etatimeago');
                        scope.waypoint.speed = feature.get('speed');
                        scope.waypoint.speedmin = feature.get('speedmin');
                        scope.waypoint.speedmax = feature.get('speedmax');
                        scope.waypoint.geometrytype = feature.get('geometrytype');
                        scope.waypoint.portsidextd = feature.get('portsidextd');
                        scope.waypoint.starboardxtd = feature.get('starboardxtd');
                        $log.debug("#" + feature);
                    };

                    olScope.getMap().then(function (map) {

                        routeFeatureLayer.getSource().addFeatures(scope.features);
                       // map.getView().setCenter(routeFeatureLayer.getSource().getFeatures()[0].getGeometry().getCoordinates());
                        // Clean up when the layer is destroyed
                        scope.$on('$destroy', function () {
                            if (angular.isDefined(routeLayers)) {
                                map.removeLayer(routeLayers);
                            }
                        });

                        var routeFeature = new ol.Feature({
                            type: 'route',
                            geometry: new ol.geom.LineString(scope.points)
                        });

                        var startMarker = scope.features[0];
                        var endMarker = scope.features[scope.features.length - 1];

                        scope.animating = false;

                        /**
                         * Animation of the route.
                         */

                        var index = 0;
                        var renders = 0;
                        var speed, now, orgIndexVal;
                        var speedInput = document.getElementById('speed');
                        var routeLength = scope.animatedfeatures.length;


                        // all styling related to interactions and editing
                        var overlayInteractionStyle = (function() {
                            /* jshint -W069 */
                            var styles = {};
                            styles['Polygon'] = [
                                new ol.style.Style({
                                    fill: new ol.style.Fill({
                                        color: [255, 255, 255, 0.5]
                                    })
                                }),
                                new ol.style.Style({
                                    stroke: new ol.style.Stroke({
                                        color: [255, 255, 255, 1],
                                        width: 5
                                    })
                                }),
                                new ol.style.Style({
                                    stroke: new ol.style.Stroke({
                                        color: [255, 140, 0, 1],
                                        width: 3
                                    })
                                })
                            ];
                            styles['MultiPolygon'] = styles['Polygon'];

                            styles['LineString'] = [
                                new ol.style.Style({
                                    stroke: new ol.style.Stroke({
                                        color: [255, 255, 255, 1],
                                        width: 5
                                    })
                                }),
                                new ol.style.Style({
                                    stroke: new ol.style.Stroke({
                                        color: [255, 140, 0, 1],
                                        width: 3
                                    })
                                })
                            ];
                            styles['MultiLineString'] = styles['LineString'];

                            styles['Point'] = [
                                new ol.style.Style({
                                    image: new ol.style.Circle({
                                        radius: 7,
                                        fill: new ol.style.Fill({
                                            color: [255, 140, 0, 1]
                                        }),
                                        stroke: new ol.style.Stroke({
                                            color: [255, 255, 255, 0.75],
                                            width: 1.5
                                        })
                                    }),
                                    zIndex: 100000
                                })
                            ];
                            styles['MultiPoint'] = styles['Point'];

                            styles['GeometryCollection'] = styles['Polygon'].concat(styles['Point']);

                            return function(feature, resolution) {
                                return styles[feature.getGeometry().getType()];
                            };
                            /* jshint +W069 */
                        })();


                        var moveFeature = function (event) {
                            // TODO in the case of the map has been edited we need to re-create the animation features.
                            var vectorContext = event.vectorContext;
                            var frameState = event.frameState;


                            if (scope.animating) {
                                var elapsedTime = frameState.time - now;
                                //$log.log("elapsedTime:" + elapsedTime + " renders" + renders++);
                                index = Math.round(speed * elapsedTime / 1000);

                                if (index >= scope.animatedfeatures.length) {
                                    index = 0; // rewind, and loop
                                    renders = 0;
                                    scope.stopAnimation(true);
                                }

                                var feature = scope.animatedfeatures[index];
                                var retrievedStyle = feature.getStyle();
                                retrievedStyle.getImage().load();

                                scope.activeRoutePoint = feature.get('position');
                                scope.activeRouteName = feature.get('name');
                                scope.activeRouteSpeed = feature.get('speed');
                                scope.activeRouteTS = feature.get('eta');
                                scope.activeRouteTSetaTimeAgo = feature.get('etatimeago');
                                vectorContext.drawFeature(feature, retrievedStyle);
                            }

                            map.render();      // tell OL3 to continue the postcompose animation
                        };

                        scope.startAnimation = function () {
                            if (scope.animating) {
                                scope.stopAnimation(false);
                            } else {
                                scope.animating = true;
                                now = new Date().getTime();
                                speed = speedInput.value;
                                map.on('postcompose', moveFeature);
                                map.render();
                            }
                        };

                        if (scope.autoplay) {
                            $timeout(function(){scope.startAnimation()}, 5000);
                        }
                        /**
                         * @param {boolean} ended end of animation.
                         */
                        scope.stopAnimation = function (ended) {
                            scope.animating = false;
                        };

                        /**
                         * Clickable waypoints pop-up content
                         */

                        var elm = document.getElementById('optimized-waypoint-route-info');

                        var popup = new ol.Overlay({
                            element: elm,
                            positioning: 'bottom-center',
                            stopEvent: false
                        });
                        map.addOverlay(popup);



                        /**
                         * Add a click handler to hide the popup.
                         * @return {boolean} Don't follow the href.
                         */
                        closer.onclick = function () {
                            overlay.setPosition(undefined);
                            closer.blur();
                            return false;
                        };

                        map.addOverlay(overlay);

                        /**
                         * Add a click handler to the map to render the popup.
                         */
     /*                   map.on('singleclick', function (evt) {

                            var feature = map.forEachFeatureAtPixel(evt.pixel, function (feature, layer) {
                                return feature;
                            }, null, function (layer) {
                                return layer === routeFeatureLayer;
                            });

                            if (feature) {

                                var coordinate = evt.coordinate;
                                $rootScope.activeWayPoint = feature.get('id');
                                $rootScope.$apply();

                                // well, the directive watches the rootscopes directive so we don't have to do anymore...
                                // scope.populatePopupWaypoint(feature);
                                //overlay.setPosition(coordinate);
                            } else {
                                overlay.setPosition(undefined);
                                closer.blur();
                            }
                        });*/

                        var select = new ol.interaction.Select({
                            style: overlayInteractionStyle
                        });

                        var modify = new ol.interaction.Modify({
                            features: select.getFeatures(),
                            style: overlayInteractionStyle
                        });

                        map.addLayer(routeLayers);
                        //map.getInteractions().extend([select, modify]);
                      //  map.getInteractions().extend([select]);
                    });

                    /** Returns the lat-lon attributesas json-object */
                    scope.toLonLat = function (long, lati) {
                        return {lon: long, lat: lati};
                    };


                    // watch if a new RTZ route has been uploaded by the end user.
                    scope.$watch("points", function(newValue, oldValue) {
                        if (newValue && newValue.length>0)
                            olScope.getMap().then(function (map) {
                                growl.info("Route has been optimized. Optimized route is orange");
                                $log.info("Route has been optimized. Optimized route is orange");
                                animationLayer.getSource().clear();
                                pathLayer.getSource().clear();
                                routeFeatureLayer.getSource().clear();

                                var routeFeature = new ol.Feature({
                                    type: 'route',
                                    geometry: new ol.geom.LineString(scope.points)
                                });

                                var startMarker = scope.features[0];
                                var endMarker = scope.features[scope.features.length - 1];

                                startMarker.setStyle(styles['startStyle']);
                                endMarker.setStyle(styles['endStyle']);


                                //animationLayer.getSource().addFeatures(scope.animatedfeatures);
                                pathLayer.getSource().addFeature(routeFeature);
                                routeFeatureLayer.getSource().addFeatures(scope.features);
                                routeFeatureLayer.getSource().addFeature(startMarker);
                                routeFeatureLayer.getSource().addFeature(endMarker);

                                var extent = routeFeatureLayer.getSource().getExtent();
                                map.getView().fit(extent, map.getSize());  // automatically zoom and pan the map to fit my features
                            });
                    }, true);
                }
            }
        }])
    /**
     * very simple route in a map. The key is that it doesn't have an animation.
     */
    .directive('simpleRtzRoute', ['MapService', '$rootScope', '$log', '$window', '$http', 'growl', function (MapService, $rootScope, $log, $window, $http, growl) {
        return {
            restrict: 'E',
            replace: true,
            require: '^olMap',
            template:
            '<div>' +
            '<div id="smp-waypoint-route-info" class="ng-cloak"></div>' +
            '<div id="smp-waypoint-popup" class="ol-popup">' +
            '<a href="#" id="smp-waypoint-popup-closer" class="ol-popup-closer"></a>' +
            '<h3 class="popover-title">{{waypoint.name}}</h3>' +
            '<div id="smp-waypoint-popover-content" class="popover-content">' +
            '<p>Waypoint number: {{waypoint.id}}</p>' +
            '<p>Position: {{ waypoint.lon}} - {{waypoint.lat}} </p>' +
            '<p>Eta: {{waypoint.eta}}</p>' +
            '<p>Time: {{waypoint.etatimeago}}</p>' +
            /*'<p>Radius: {{waypoint.radius}}</p>' +
             '<p>Speed: {{waypoint.speed}}</p>' +
             '<p>Speed Min: {{waypoint.speedmin}}</p>' +
             '<p>Speed Max: {{waypoint.speedmax}}</p>' +
             '<p>Geometry Type: {{waypoint.geometrytype}}</p>' +
             '<p>Portside XTD: {{waypoint.portsidextd}}</p>' +
             '<p>Starboard XTD: {{waypoint.starboardxtd}}</p>' +*/
            '</div>' +
            '</div>' +
            '</div>'
            ,
            scope: {
                name: '@',
                    points: '=?',
                    features: '=?'
            },
            link: function(scope, element, attrs, ctrl) {
                // $log.info("The simple rtz route got features: ");// + $rootScope.route_oLfeatures.length + " and oLpoints:" + $rootScope.route_oLpoints.length );
                var olScope = ctrl.getOpenlayersScope();

                scope.populatePopupWaypoint = function (feature) {
                    scope.waypoint = {};
                    scope.waypoint.id = feature.get('id');
                    scope.waypoint.name = feature.get('wayname');
                    scope.waypoint.lon = feature.get('lon');
                    scope.waypoint.lat = feature.get('lat');
                    scope.waypoint.radius = feature.get('radius');
                    scope.waypoint.eta = feature.get('eta');
                    scope.waypoint.etatimeago = feature.get('etatimeago');
                    scope.waypoint.speed = feature.get('speed');
                    scope.waypoint.speedmin = feature.get('speedmin');
                    scope.waypoint.speedmax = feature.get('speedmax');
                    scope.waypoint.geometrytype = feature.get('geometrytype');
                    scope.waypoint.portsidextd = feature.get('portsidextd');
                    scope.waypoint.starboardxtd = feature.get('starboardxtd');
                    $log.debug("#" + feature.get('id'));
                };

                olScope.getMap().then(function(map) {
                    var  animatedMarkerStyle = new ol.style.Style({
                        image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
                            anchor: [0.5, 0.5],
                            anchorXUnits: 'fraction',
                            anchorYUnits: 'fraction',
                            opacity: 0.85,
                            rotation: 0,
                            rotateWithView: false,
                            src: 'img/vessel_green_moored.png'
                        }))
                    });

                    //WEATHER ON ROUTE
                    scope.wor_enabled = false;
                    //on change
                    scope.$watch(function () { return window.localStorage['wor_enabled']; },function(newVal,oldVal){
                        if(newVal && (newVal+"")!="" && (newVal+"") != (oldVal+"")){
                            if((newVal+"")=="true"){
                                scope.wor_enabled = true;
                                growl.info("Getting Weather On Route");
                                routeFeatureLayer.getSource().clear(); //route markers
                                $rootScope.wor_route_oLfeatures = $rootScope.route_oLfeatures; //start with fresh route
                                routeFeatureLayer.getSource().addFeatures($rootScope.wor_route_oLfeatures); //route markers
                                scope.getWeatherDataForWaypoint(1); //get weather on route but skip the starting point
                            }else{
                                scope.wor_enabled = false;
                                growl.info("Removing Weather On Route");
                                setTimeout(function(){scope.getRouteFromLocalstorage()}, 200); //race condition
                                setTimeout(function(){routeFeatureLayer.getSource().clear();}, 500); //race condition
                                setTimeout(function(){ routeFeatureLayer.getSource().addFeatures($rootScope.route_oLfeatures);}, 1000); //race condition
                            }
                            console.log("WOR?",scope.wor_enabled);
                        }
                    });
                    //on load:
                    var lcl_wor = window.localStorage['wor_enabled'];
                    if(lcl_wor && (lcl_wor+"")=="true"){
                        scope.wor_enabled = true;
                    }else{
                        scope.wor_enabled = false;
                    }





                    var markerStyle = new ol.style.Style({
                        image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
                            anchor: [0.5, 0.5],
                            anchorXUnits: 'fraction',
                            anchorYUnits: 'fraction',
                            opacity: 0.85,
                            rotation:  0,
                            rotateWithView: false,
                            src: 'img/vessel_green.png'
                        }))
                    });

                    markerStyle.getImage().load();

                    var olScope = ctrl.getOpenlayersScope();

                    var styles = {
                        'route': new ol.style.Style({
                            stroke: new ol.style.Stroke({
                                width: 6, color: [237, 212, 0, 0.8]
                            })
                        }),
                        'icon': new ol.style.Style({
                            image: new ol.style.Icon({
                                anchor: [0.5, 1],
                                src: 'data/icon.png'
                            })
                        }),
                        'geoMarker': new ol.style.Style({
                            image: new ol.style.Circle({
                                radius: 7,
                                snapToPixel: false,
                                fill: new ol.style.Fill({color: 'black'}),
                                stroke: new ol.style.Stroke({
                                    color: 'white', width: 2
                                })
                            })
                        }),
                        endStyle: new ol.style.Style({
                            image: new ol.style.Circle({
                                radius: 6,
                                stroke: new ol.style.Stroke({
                                    color: 'darkred',
                                    width: 2
                                }),
                                fill: new ol.style.Fill({
                                    color: [255, 0, 0, 0.5]
                                })
                            }),
                            text: new ol.style.Text({
                                text: 'END', // attribute code
                                font: 'bold 14 Verdana',
                                offsetY: 20,
                                stroke: new ol.style.Stroke({color: "white", width: 5})
                            })
                        }),
                        startStyle: new ol.style.Style({
                            image: new ol.style.Circle({
                                radius: 6,
                                stroke: new ol.style.Stroke({
                                    color: 'darkred',
                                    width: 2
                                }),
                                fill: new ol.style.Fill({
                                    color: [255, 0, 0, 0.5]
                                })
                            }),
                            text: new ol.style.Text({
                                text: 'START', // attribute code
                                font: 'bold 14 Verdana',
                                offsetY: 20,
                                stroke: new ol.style.Stroke({color: "white", width: 5})
                                //rotation: 45
                            })
                        }),
                        worstyle: new ol.style.Style({
                            image: new ol.style.Circle({
                                radius: 6,
                                stroke: new ol.style.Stroke({
                                    color: 'white',
                                    width: 2
                                }),
                                fill: new ol.style.Fill({
                                    color: [255, 0, 0, 0.5]
                                })
                            }),
                            text: new ol.style.Text({
                                text: 'wor', // attribute code
                                font: 'bold 14 Verdana',
                                offsetY: 20,
                                stroke: new ol.style.Stroke({color: "white", width: 5})
                                //rotation: 45
                            })
                        })
                    };

                    // convert degrees to radians
                    scope.degToRad = function(deg) {
                        return deg * Math.PI * 2 / 360;
                    };
                    scope.calcSinCosFromAngle = function(xy, angle, radius) { //requires ('x' or 'y'), angle in degrees and radius in px.
                        var SinCos;
                        if (xy == 'x') SinCos = radius * Math.cos(angle); // Calculate the x position of the element.
                        if (xy == 'y') SinCos = radius * Math.sin(angle); // Calculate the y position of the element.
                        return SinCos;
                    };
                    // scope.retStyle = function(){
                    //     var bob = new ol.style.Style({
                    //         image: new ol.style.Circle({
                    //             radius: 6,
                    //             stroke: new ol.style.Stroke({
                    //                 color: 'white',
                    //                 width: 2
                    //             }),
                    //             fill: new ol.style.Fill({
                    //                 color: [255, 0, 0, 0.5]
                    //             })
                    //         }),
                    //         text: new ol.style.Text({
                    //             text: 'wor', // attribute code
                    //             font: 'bold 14 Verdana',
                    //             offsetY: 20,
                    //             stroke: new ol.style.Stroke({color: "white", width: 5})
                    //             //rotation: 45
                    //         })
                    //     })
                    //     return bob;
                    // };




                    scope.retWORMWaveStyle = function (scale, wavedir, waveheight, markertext) {
                        if (!scale) scale = 1;
                        if (!wavedir) wavedir = 180;
                        wavedir += 45; //offset for icon
                        var useimage = (waveheight == "") ? 'img/WeatherOnRoute//WOR_backdropcircle_nowave.png' : 'img/WeatherOnRoute/WOR_backdropcircle.png';
                        if (markertext == "nodata") useimage = "img/WeatherOnRoute/WOR_nodata.png";


                        if (!waveheight || waveheight==0) waveheight = "";
                        var radOff = 0.47; //text offset in radians for current and wave indicator
                        var WORMWaveStyle = new ol.style.Style({
                            zIndex: 50,
                            image: new ol.style.Icon({
                                opacity: 0.75,
                                rotation: scope.degToRad(wavedir), //wavepointer is pointing lowerright
                                anchor: [(0.5), (0.5)],
                                anchorXUnits: 'fraction',
                                anchorYUnits: 'fraction',
                                src: 'img/WeatherOnRoute/WOR_backdropcircle.png',
                                scale: (0.5 * scale)
                            }),
                                text: new ol.style.Text({
                                    font: '12px helvetica,sans-serif',
                                    text: ('' + Math.round( waveheight * 10 ) / 10),
                                    offsetX: scope.calcSinCosFromAngle('x', scope.degToRad(wavedir) + radOff, (44 * scale)),
                                    offsetY: scope.calcSinCosFromAngle('y', scope.degToRad(wavedir) + radOff, (44 * scale)),
                                    scale: (1 * scale),
                                    fill: new ol.style.Fill({
                                        color: '#000'
                                    }),
                                    stroke: new ol.style.Stroke({
                                        color: '#fff',
                                        width: 1
                                    })
                                })
                        });
                        return WORMWaveStyle;
                    };

                    scope.retWORMWindStyle = function (scale, winddir, windstr, markertext, wavedir) { //windstr is m/s - wavedir is needed to make offset greater if pointing south so text doesnt overlap.
                        if (!scale) scale = 1;
                        var waypointtextoffset = 46;
                        if (!winddir) winddir = 180; //default north
                        (!windstr) ? windstr = 1 : windstr * 1.9438444924574; // make 1 knot if nothing, or meter/sec to knots.
                        var markerImageNamePath = "img/wind/";


                        //Determine wind marker image to display
                        if (windstr < 1.9){
                            markerImageNamePath += 'mark000.png';
                        } else if (windstr >= 2 && windstr < 7.5) {
                            markerImageNamePath += 'mark005.png';
                        } else if (windstr >= 7.5 && windstr < 12.5) {
                            markerImageNamePath += 'mark010.png';
                        } else if (windstr >= 12.5 && windstr < 17.5) {
                            markerImageNamePath += 'mark015.png';
                        } else if (windstr >= 17.5 && windstr < 22.5) {
                            markerImageNamePath += 'mark020.png';
                        } else if (windstr >= 22.5 && windstr < 27.5) {
                            markerImageNamePath += 'mark025.png';
                        } else if (windstr >= 27.5 && windstr < 32.5) {
                            markerImageNamePath += 'mark030.png';
                        } else if (windstr >= 32.5 && windstr < 37.5) {
                            markerImageNamePath += 'mark035.png';
                        } else if (windstr >= 37.5 && windstr < 42.5) {
                            markerImageNamePath += 'mark040.png';
                        } else if (windstr >= 42.5 && windstr < 47.5) {
                            markerImageNamePath += 'mark045.png';
                        } else if (windstr >= 47.5 && windstr < 52.5) {
                            markerImageNamePath += 'mark050.png';
                        } else if (windstr >= 52.5 && windstr < 57.5) {
                            markerImageNamePath += 'mark055.png';
                        } else if (windstr >= 57.5 && windstr < 62.5) {
                            markerImageNamePath += 'mark060.png';
                        } else if (windstr >= 62.5 && windstr < 67.5) {
                            markerImageNamePath += 'mark065.png';
                        } else if (windstr >= 67.5 && windstr < 72.5) {
                            markerImageNamePath += 'mark070.png';
                        } else if (windstr >= 72.5 && windstr < 77.5) {
                            markerImageNamePath += 'mark075.png';
                        } else if (windstr >= 77.5 && windstr < 82.5) {
                            markerImageNamePath += 'mark080.png';
                        } else if (windstr >= 82.5 && windstr < 87.5) {
                            markerImageNamePath += 'mark085.png';
                        } else if (windstr >= 87.5 && windstr < 92.5) {
                            markerImageNamePath += 'mark090.png';
                        } else if (windstr >= 92.5 && windstr < 97.5) {
                            markerImageNamePath += 'mark095.png';
                        } else if (windstr >= 97.5) {
                            markerImageNamePath += 'mark100.png';
                        }

                        //move the text a bit lower if the wavearrow points down
                        if ((wavedir < 55 && wavedir > 0) || (wavedir > 305)) {
                            waypointtextoffset = 56;
                        }

                        var useimage = markerImageNamePath;
                        if (markertext == "nodata") {
                            useimage = "img/emptyimage.png";
                            markertext = "";
                        }


                        var WORMWindStyle = new ol.style.Style({
                            zIndex: 52,
                            image: new ol.style.Icon(({
                                opacity: 1,
                                rotation: scope.degToRad(winddir), //windpointer is straight is pointing straight down
                                anchor: [(0.52), (0.25)],
                                anchorXUnits: 'fraction',
                                anchorYUnits: 'fraction',
                                src: useimage, //needs path and windstr to paint correct arrow
                                scale: (0.80 * scale)
                            })),
                            text: new ol.style.Text({
                                font: 'bold 12px helvetica,sans-serif',
                                text: "" + markertext,
                                offsetX: 0,
                                offsetY: waypointtextoffset * scale,
                                scale: (1 * scale),
                                fill: new ol.style.Fill({
                                    color: '#000'
                                }),
                                stroke: new ol.style.Stroke({
                                    color: '#fff',
                                    width: 1
                                })
                            })
                        });
                        return WORMWindStyle;
                    };

                    scope.retWORMCurrentStyle = function (scale, currdir, currstr, markertext) {
                        if (!scale) scale = 1;
                        if (!currdir) currdir = 180;
                        currdir += 45; //offset for icon
                        (!currstr) ? currstr = "" : currstr * 1.9438444924574; // make "" if nothing, or meter/sec to knots.
                        var useimage = 'img/WOR_innercircle.png';
                        if (markertext == "nodata") useimage = "img/emptyimage.png";

                        if (!currstr || currstr == 0) currstr = "";
                        var radOff = 0.25; //text offset in radians for current and wave indicator
                        var WORMCurrentStyle = new ol.style.Style({
                            zIndex: 51,
                            image: new ol.style.Icon({
                                opacity: (currstr!="")?1:0,
                                rotation: scope.degToRad(currdir), //currentpointer is pointing lowerright
                                anchor: [0.5, 0.5],
                                anchorXUnits: 'fraction',
                                anchorYUnits: 'fraction',
                                src: useimage, //needs path
                                scale: (0.5 * scale)
                            }),
                            text: new ol.style.Text({
                                font: '10px helvetica,sans-serif',
                                text: ('' + (Math.round( currstr * 10 ) / 10)),
                                offsetX: scope.calcSinCosFromAngle('x', scope.degToRad(currdir) + radOff, (18 * scale)),
                                offsetY: scope.calcSinCosFromAngle('y', scope.degToRad(currdir) + radOff, (18 * scale)),
                                scale: (1 * scale),
                                fill: new ol.style.Fill({
                                    color: '#000'
                                }),
                                stroke: new ol.style.Stroke({
                                    color: '#fff',
                                    width: 1
                                })
                            })
                        });
                        return WORMCurrentStyle;
                    };

                    // scope.retStyle = function(){
                    //     var scale = 1;
                    //     // var useimage = (waveheight == "") ? 'img/WeatherOnRoute//WOR_backdropcircle_nowave.png' : 'img/WeatherOnRoute/WOR_backdropcircle.png';
                    //     var useimage = 'img/WeatherOnRoute/WOR_backdropcircle.png';
                    //     // if (markertext == "nodata") useimage = "img/WeatherOnRoute/WOR_nodata.png";
                    //     // var  animatedMarkerStyle = new ol.style.Style({
                    //     //     image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
                    //     //         anchor: [0.5, 0.5],
                    //     //         anchorXUnits: 'fraction',
                    //     //         anchorYUnits: 'fraction',
                    //     //         opacity: 0.85,
                    //     //         rotation: 0,
                    //     //         rotateWithView: false,
                    //     //         src: 'img/vessel_orange_moored.png'
                    //     //     }))
                    //     // });
                    //
                    //     var bob = new ol.style.Style({
                    //         //     image: new ol.style.Icon({
                    //         //         opacity: 0.75,
                    //         //         rotation: degToRad(wavedir), //wavepointer is pointing lowerright
                    //         //         anchor: [(0.5), (0.5)],
                    //         //         anchorXUnits: 'fraction',
                    //         //         anchorYUnits: 'fraction',
                    //         //         src: useimage,
                    //         //         scale: (0.5 * scale)
                    //         //     }),
                    //         image: new ol.style.Icon({
                    //             opacity: 0.75,
                    //             rotation: scope.degToRad(180), //wavepointer is pointing lowerright
                    //             anchor: [(0.5), (0.5)],
                    //             anchorXUnits: 'fraction',
                    //             anchorYUnits: 'fraction',
                    //             src: useimage,
                    //             scale: (0.5 * scale)
                    //         }),
                    //         text: new ol.style.Text({
                    //             text: 'word', // attribute code
                    //             font: 'bold 14 Verdana',
                    //             offsetY: 20,
                    //             stroke: new ol.style.Stroke({color: "white", width: 5})
                    //             //rotation: 45
                    //         })
                    //     });
                    //     return bob;
                    // };


                    var routeLayers;
                    var pathLayer; //With or without weather

                        pathLayer = new ol.layer.Vector({
                            source: new ol.source.Vector({
                                features: []
                            }),
                            style: new ol.style.Style({
                                stroke: new ol.style.Stroke({
                                    lineJoin: 'miter',
                                    width: 3,
                                    color: [255, 0, 0, 0.8]
                                })
                            })
                        });
                    // }



                    var vectorSource = new ol.source.Vector({
                        features: []
                    });

                    var routeFeatureLayer = new ol.layer.Vector({
                        name: "routeVectorLayer",
                        title: "route",
                        source: vectorSource,
                        style:  new ol.style.Style({
                            image: new ol.style.Circle({
                                radius: 3,
                                stroke: new ol.style.Stroke({
                                    color: 'red',
                                    width: 3
                                }),
                                fill: new ol.style.Fill({
                                    color: [255, 0, 0, 0.5]
                                })
                            })
                        }),
                        visible: true
                    });


                    //ROUTE
                    routeLayers = new ol.layer.Group({
                        title: 'Route',
                        layers: [pathLayer, routeFeatureLayer],
                        visible: true
                    });


                    pathLayer.getSource().clear();
                    routeFeatureLayer.getSource().clear();

                    scope.getRouteFromLocalstorage = function(){
                        if($window.localStorage.getItem('route_oLfeaturesGeoJson')){
                            var geoJSONFormat = new ol.format.GeoJSON();
                            var olFeaturesGeojson = $window.localStorage.getItem('route_oLfeaturesGeoJson');
                            var olPointsJSON = $window.localStorage.getItem('route_oLpoints');
                            $rootScope.route_oLfeatures = geoJSONFormat.readFeatures(olFeaturesGeojson);
                            $rootScope.wor_route_oLfeatures = geoJSONFormat.readFeatures(olFeaturesGeojson);
                            $rootScope.route_oLpoints = JSON.parse(olPointsJSON);
                        }
                    };
                    scope.getRouteFromLocalstorage();

                    if($rootScope.route_oLfeatures ){
                        $log.info("Loading features! ");

                        var routeFeature;
                        routeFeature = new ol.Feature({
                            type: 'route',
                            geometry: new ol.geom.LineString($rootScope.route_oLpoints)
                        });

                        scope.worData = []; //array of weatherdata as long as route_oLfeatures.length (number of waypoints
                        scope.getWeatherDataForWaypoint = function (pointnumber) { //type= 'application/text' or json
                            if(scope.wor_enabled) {
                                //draw a little square along the route and get some weather data from it
                                var coords = ol.proj.transform($rootScope.wor_route_oLfeatures[pointnumber].getGeometry().getCoordinates(), 'EPSG:3857', 'EPSG:4326');
                                var time1 = moment(moment().add(1,'days')).utc().format("YYYY-MM-DDThh:mm:ss");
                                time1 = time1 + ".000+0100";
                                var time2 = moment(moment().add(1,'days').add(10,'minutes')).utc().format("YYYY-MM-DDThh:mm:ss");
                                time2 = time2 + ".000+0100";
                                var req2 = { "mssi": 999999999,
                                    "datatypes":["current","wave","wind"],
                                    "dt":15,
                                    "waypoints":[
                                        {
                                            "eta":time1,
                                            "heading":"GC",
                                            "lat":coords[1],
                                            "lon":coords[0]},
                                        {
                                            "eta":time2,
                                            "heading":"GC",
                                            "lat":(parseFloat(coords[1]) + 0.1), //add a bit to coords
                                            "lon":(parseFloat(coords[0]) + 0.1)}
                                    ]
                                };
                                var reqx = encodeURI(JSON.stringify(req2)).replace(/[+]/g, '%2B');
                                var reqxjson = {data:(reqx + "")};

                                if($rootScope.wor_route_oLfeatures.length > 1){ //must be a barely valid route
                                    $http({
                                        url: 'rest/weatherforwarding', //needs a forward to circumvent http -> https blocking.
                                        method: "POST", //POST, GET etc.
                                        headers: {'Content-Type': 'application/json'},
                                        data: reqxjson
                                    })
                                        .then(function (data) {
                                            var parsedData = JSON.parse(JSON.stringify(data));
                                            //data is in, now find the 6 data types: wave (height & direction), wind (strength & direction), current (strength & direction)
                                            var tmpwd = {error:true,winddir:null,windspd:null,wavedir:null,wavehgt:null,currdir:null,currspd:null};
                                            try{ //can fail for so many reasons
                                                if(parsedData.error != 0){ //no point if fail
                                                    var pd = parsedData.data.metocForecast.forecasts;
                                                    for(var i=0;i!=pd.length;i++){ //prepare the data types
                                                        if(pd[i]["wind-dir"] && tmpwd.winddir == null) tmpwd.winddir = pd[i]["wind-dir"].forecast;
                                                        if(pd[i]["wind-speed"] && tmpwd.windspd == null) tmpwd.windspd = pd[i]["wind-speed"].forecast;

                                                        if(pd[i]["current-dir"] && tmpwd.currdir == null) tmpwd.currdir = pd[i]["current-dir"].forecast;
                                                        if(pd[i]["current-speed"] && tmpwd.currspd == null) tmpwd.currspd = pd[i]["current-speed"].forecast;

                                                        if(pd[i]["wave-dir"] && tmpwd.wavedir == null) tmpwd.wavedir = pd[i]["wave-dir"].forecast;
                                                        if(pd[i]["wave-height"] && tmpwd.wavehgt == null) tmpwd.wavehgt = pd[i]["wave-height"].forecast;
                                                    }
                                                    tmpwd.error = false;
                                                    scope.worData.push(tmpwd); // add one to the array
                                                }

                                            }catch(objectReturnedError){
                                                scope.worData.push({error:true}); //only error
                                                growl.error("An error occurred while trying to parse weather data");
                                            }
                                                // $rootScope.wor_route_oLfeatures[pointnumber].setStyle(styles('wormstyle'));
                                                $rootScope.wor_route_oLfeatures[pointnumber].setStyle([scope.retWORMWaveStyle(1, tmpwd.wavedir, tmpwd.wavehgt, "aa"),scope.retWORMWindStyle(1, tmpwd.winddir, tmpwd.windspd, "", tmpwd.wavedir),scope.retWORMCurrentStyle(1,tmpwd.currdir,tmpwd.currspd,"")]);

                                            //create new point content from waypoint
                                            if(pointnumber && pointnumber < $rootScope.wor_route_oLfeatures.length -2){ //good to go
                                                pointnumber = pointnumber + 1;
                                                scope.getWeatherDataForWaypoint(pointnumber);
                                            }else{
                                                growl.success("Weather On Route is loaded.");
                                                console.log("scope.worData:",scope.worData);
                                            }
                                        },
                                        function (data) { // error
                                            console.log(data);
                                            growl.error("An error occurred while trying to get weather data!");
                                        });
                                }

                            }
                        };
                        if(scope.wor_enabled) scope.getWeatherDataForWaypoint(1); //get weather on route but skip the starting point

                        //RTZ
                        var startMarker = $rootScope.route_oLfeatures[0];
                        var endMarker = $rootScope.route_oLfeatures[$rootScope.route_oLfeatures.length - 1];

                        startMarker.setStyle(styles['startStyle']);
                        endMarker.setStyle(styles['endStyle']);

                        /**
                         * Clickable waypoints pop-up content
                         */

                        var elm = document.getElementById('smp-waypoint-route-info');

                        /**
                         * Elements that make up the popup.
                         */
                        var container = document.getElementById('smp-waypoint-popup');
                        var content = document.getElementById('smp-waypoint-popup-content');
                        var closer = document.getElementById('smp-waypoint-popup-closer');

                        /**
                         * Create an overlay to anchor the popup to the map.
                         */
                        var overlay = new ol.Overlay(/** @type {olx.OverlayOptions} */ ({
                            element: container,
                            autoPan: true,
                            autoPanAnimation: {
                                duration: 250
                            }
                        }));

                        var popup = new ol.Overlay({
                            element: elm,
                            positioning: 'bottom-center',
                            stopEvent: false
                        });
                        map.addOverlay(popup);



                        /**
                         * Add a click handler to hide the popup.
                         * @return {boolean} Don't follow the href.
                         */
                        closer.onclick = function () {
                            overlay.setPosition(undefined);
                            closer.blur();
                            return false;
                        };

                        map.addOverlay(overlay);

                        /**
                         * Add a click handler to the map to render the popup for the waypoint marker
                         */
                        map.on('singleclick', function (evt) {

                            var feature = map.forEachFeatureAtPixel(evt.pixel, function (feature, layer) {
                                    if(feature.get('id')){ // fixed bug were we would get a popup marker everywhere
                                        return feature;
                                    }
                                    return false;

                            }, 0, function (layer) {
                                return layer === routeFeatureLayer;
                            });

                            if (feature) {

                                var coordinate = evt.coordinate;
                                //$rootScope.activeWayPoint = feature.get('id');
                                //$rootScope.$apply();
                                 scope.populatePopupWaypoint(feature);
                                overlay.setPosition(coordinate);
                                scope.$apply();
                            } else {
                                overlay.setPosition(undefined);
                                closer.blur();
                            }
                        });



                        scope.updateRouteWORMFunction = function(routemarkernumber) { //create route weather marker when data is available.
                            //
                            // var winddirection = route.scheduleElement[routemarkernumber].winddirection;
                            // var windspeed = route.scheduleElement[routemarkernumber].windspeed;
                            // var currentdirection = route.scheduleElement[routemarkernumber].currentdirection;
                            // var currentspeed = route.scheduleElement[routemarkernumber].currentspeed;
                            // var wavedirection = route.scheduleElement[routemarkernumber].wavedirection;
                            // var waveheight = route.scheduleElement[routemarkernumber].waveheight;
                            //
                            // var timehours = (route.scheduleElement[routemarkernumber].eta.split("T")[1]).split(".")[0]; //get hours & minutes from 2017-04-19T11:00:01.000Z
                            // timehours = timehours.substring(0,timehours.length-3);
                            //
                            // var markertext = retDayFromRTZ(route.scheduleElement[routemarkernumber].eta) + "\n" + timehours + " UTC";
                            // if (!control_displaymarkertext) markertext = "";
                            // //adds a new clickmarker with updated info
                            // mapSource.addFeatures(generateWORM('ROUTEWEATHERMARKER', 'routeweathermarker_' + routemarkernumber, route.waypoints[routemarkernumber].lon, route.waypoints[routemarkernumber].lat, control_scale, winddirection, windspeed, currentdirection, currentspeed, wavedirection, waveheight, markertext));

                        };

                        scope.cleanWeatherMarkersOverlapping = function () {
                            console.log("cleaning overlapping WOR markers on zoom");
                            // function hideshowmarkerswithindistance(showhidedistance) {
                            //     var totalDistance = 0;
                            //     for (var i = 0; i != route.scheduleElement.length - 1; i++) {//loop through all waypoints, remove any that are closer than (distance)
                            //         for (var y = i; y != route.scheduleElement.length - 1; y++) {
                            //             totalDistance = parseFloat(route.scheduleElement[i].nextwaypointdistance) + parseFloat(route.scheduleElement[i + 1].nextwaypointdistance); //add up distances
                            //             if (showhidedistance > totalDistance) {
                            //                 try { //remove the marker
                            //                     mapSource.removeFeature(mapSource.getFeatureById("routeweathermarker_" + (i) + "_wavemarker"));
                            //                     mapSource.removeFeature(mapSource.getFeatureById("routeweathermarker_" + (i) + "_currentmarker"));
                            //                     mapSource.removeFeature(mapSource.getFeatureById("routeweathermarker_" + (i) + "_windmarker"));
                            //                 } catch (ExceptionNoFeature) { }
                            //                 route.scheduleElement[i].zoomdisplay = mapZoomLevel; //save at which zoomlevel to show this marker again
                            //             }
                            //         }
                            //         //add markers that are within acceptable distance again on zoom change
                            //         if (route.scheduleElement[i].zoomdisplay > 0 && mapZoomLevel > (route.scheduleElement[i].zoomdisplay)) {
                            //             updateRouteWORMFunction(i);
                            //             route.scheduleElement[i].zoomdisplay = 0; //reset marker zoom behaviour
                            //         }
                            //     }
                            // }
                            // var showhidedistance = 0;
                            // switch (mapZoomLevel) {
                            //
                            //     case 11:
                            //         showhidedistance = 0.9;
                            //         break;
                            //     case 10:
                            //         showhidedistance = 1.8;
                            //         break;
                            //     case 9:
                            //         showhidedistance = 3.5;
                            //         break;
                            //     case 8:
                            //         showhidedistance = 6;
                            //         break;
                            //     case 7:
                            //         showhidedistance = 11;
                            //         break;
                            //     case 6:
                            //         showhidedistance = 22;
                            //         break;
                            //     case 5:
                            //         showhidedistance = 33;
                            //         break;
                            //     case 4:
                            //         showhidedistance = 66;
                            //         break;
                            // }
                            // hideshowmarkerswithindistance(showhidedistance);
                        };


                        //animationLayer.getSource().addFeatures(scope.animatedfeatures);
                            pathLayer.getSource().addFeature(routeFeature); //routeline
                            if(scope.wor_enabled){
                                routeFeatureLayer.getSource().addFeatures($rootScope.wor_route_oLfeatures); //route markers
                            }else {
                                routeFeatureLayer.getSource().addFeatures($rootScope.route_oLfeatures); //route markers
                            }
                            routeFeatureLayer.getSource().addFeature(startMarker);
                            routeFeatureLayer.getSource().addFeature(endMarker);
                            routeFeatureLayer.setZIndex(13);
                            pathLayer.setZIndex(13);
                            map.addLayer(routeLayers);


                        var extent = routeFeatureLayer.getSource().getExtent();
                        map.getView().fit(extent, map.getSize());  // automatically zoom and pan the map to fit my features
                    }

                });
            }
        };
    }]);
 