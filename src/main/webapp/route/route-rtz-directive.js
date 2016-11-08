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
    /**
     * very simple route in a map. The key is that it doesn't have an animation.
     */
    .directive('simpleRtzRoute', ['MapService', '$rootScope', '$log', function (MapService, $rootScope, $log) {
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
                $log.info("The simple rtz route got features: ");// + $rootScope.route_oLfeatures.length + " and oLpoints:" + $rootScope.route_oLpoints.length );
                var olScope         = ctrl.getOpenlayersScope();

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

                        })
                    };

                    var routeLayers;

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
                        layers: [pathLayer, routeFeatureLayer],
                        visible: true
                    });


                    pathLayer.getSource().clear();
                    routeFeatureLayer.getSource().clear();

                    if($rootScope.route_oLfeatures && $rootScope.route_oLfeatures.length > 0){
                        var routeFeature = new ol.Feature({
                            type: 'route',
                            geometry: new ol.geom.LineString($rootScope.route_oLpoints)
                        });

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


                        //animationLayer.getSource().addFeatures(scope.animatedfeatures);
                        pathLayer.getSource().addFeature(routeFeature);
                        routeFeatureLayer.getSource().addFeatures($rootScope.route_oLfeatures);
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
 