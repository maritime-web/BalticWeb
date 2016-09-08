/**
 * Defines a route layer
 */

angular.module('maritimeweb.route')

/** Service for displaying a route on a map **/

    .directive('routertz', ['$rootScope', '$timeout', 'MapService', 'VesselService', 'growl', '$log',
        function ($rootScope, $timeout, MapService, VesselService, growl, $log) {
            return {
                restrict: 'E',
                require: '^olMap',
                template:
                '<div id="route-info" class="ng-cloak"></div>' +
                    '<div id="popup" class="ol-popup">' +
                    '<a href="#" id="popup-closer" class="ol-popup-closer"></a>' +
                    '<h3 class="popover-title">{{waypoint.name}}</h3>' +
                    '<div class="popover-content">' +
                        '<p>Speed: {{waypoint.speed}}</p>' +
                        '<p>Eta: {{waypoint.eta}}</p>' +
                        '<p>Radius: {{waypoint.radius}}</p>' +
                        '<p>Number: {{waypoint.id}}</p>' +
                    '</div>' +
                '</div>',
                /*"<div class='hidden-xs hidden-sm message-details-route col-md-3 col-lg-3 ng-cloak'>" +
                "<btn ng-if='!animating' class='btn btn-success' id='start-animation' ng-click='startAnimation()'> <i class='fa fa-play' aria-hidden='true'></i> </btn> " +
                "<btn ng-if='animating' class='btn btn-danger' id='start-animation' ng-click='stopAnimation()' tooltip='stop animation' data-toggle='tooltip'  " +
                " data-placement='right' title='stop animation'> <i class='fa fa-stop' aria-hidden='true'></i> </btn><br>" +
                "<label for='speed'>" + " Animation speed:&nbsp;" + "<input id='speed' type='range' min='1' max='999' step='10' value='10'> " + "</label><br>" +
                "</div>"
                ,*/
                scope: {
                    name: '@',
                    autoplay: '=?',
                    points: '=?',
                    features: '=?'
                },
                link: function (scope, element, attrs, ctrl) {

                    console.log("route RTZ got route points=" +  scope.points.length +
                        " feat=" + scope.features.length +
                        "autoplay=" + scope.autoplay //+
                         //   " scope.points=" + JSON.stringify(scope.points)
                    );
                    var olScope = ctrl.getOpenlayersScope();
                    var routeLayers;

                    var animationLayer = new ol.layer.Vector({
                        source: new ol.source.Vector({
                            features: []
                        }),
                        style: new ol.style.Style({
                            stroke: new ol.style.Stroke({
                                //lineDash: [10, 20, 0, 20],
                                lineDash: [5, 10, 0, 10],
                                lineJoin: 'miter',
                                width: 4,
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
                        layers: [animationLayer, routeFeatureLayer],
                        visible: true
                    });




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
                         * Animation of the route. Maybe we can use this later.
                         */

                        var index = 0;

                        var moveFeature = function (event) {
                            var vectorContext = event.vectorContext;
                            var frameState = event.frameState;

                            if (scope.animating) {
                                var elapsedTime = frameState.time - now;
                                var index = Math.round(speed * elapsedTime / 1000);

                                if (index >= routeLength) {
                                    index = 0; // rewind, and loop
                                    scope.stopAnimation(true);
                                }

                                var feature = scope.features[index];
                                vectorContext.drawFeature(feature, feature.getStyle());
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

                        /**
                         * @param {boolean} ended end of animation.
                         */
                        scope.stopAnimation = function (ended) {
                            scope.animating = false;
                        };



                        /**
                         * Clickable waypoints pop-up content
                         */

                        var elm = document.getElementById('vessel-info');

                        var popup = new ol.Overlay({
                            element: elm,
                            positioning: 'bottom-center',
                            stopEvent: false
                        });
                        map.addOverlay(popup);

                        /**
                         * Elements that make up the popup.
                         */
                        var container = document.getElementById('popup');
                        var content = document.getElementById('popup-content');
                        var closer = document.getElementById('popup-closer');

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
                                    var geometry = feature.getGeometry();
                                    var coordinate = evt.coordinate;
                                    scope.waypoint = {};
                                    scope.waypoint.id = feature.get('id');
                                    scope.waypoint.name = feature.get('wayname');
                                    scope.waypoint.radius = feature.get('radius');
                                    scope.waypoint.eta = feature.get('eta');
                                    scope.waypoint.speed = feature.get('speed');
                                    overlay.setPosition(coordinate);
                                } else {
                                    overlay.setPosition(undefined);
                                    closer.blur();
                                }
                        });

                        if (scope.autoplay) {
                            scope.startAnimation();
                        }
                        map.addLayer(routeLayers);
                    });

                    // while watch if a new RTZ route has been uploaded
                    scope.$watch("points", function(newValue, oldValue) {
                        if (newValue)
                            olScope.getMap().then(function (map) {
                                animationLayer.getSource().clear();
                                routeFeatureLayer.getSource().clear();

                                var routeFeature = new ol.Feature({
                                    type: 'route',
                                    geometry: new ol.geom.LineString(scope.points)
                                });

                                var startMarker = scope.features[0];
                                var endMarker = scope.features[scope.features.length - 1];

                                animationLayer.getSource().addFeature(routeFeature);
                                routeFeatureLayer.getSource().addFeatures(scope.features);

                                $log.debug("retrieved the map animationLayer=" + animationLayer.getSource().getFeatures().length + " routeFeatureLayer=" + routeFeatureLayer.getSource().getFeatures().length);

                                var extent = routeFeatureLayer.getSource().getExtent();
                                map.getView().fit(extent, map.getSize());  // automatically zoom and pan the map to fit my features

                            });


                    }, true);
                }
            }
        }]);
 