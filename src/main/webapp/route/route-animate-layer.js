/**
 * Defines a route layer
 */

angular.module('maritimeweb.route')

/** Service for displaying a route on a map **/

    .directive('routeanimate', ['$rootScope', '$timeout', 'MapService', 'VesselService', 'growl',
        function ($rootScope, $timeout, MapService, VesselService, growl) {
            return {
                restrict: 'E',
                require: '^olMap',
                template: "<div class='hidden-xs hidden-sm message-details-route col-md-3 col-lg-3 ng-cloak'>" +
                "<btn ng-if='!animating' class='btn btn-success' id='start-animation' ng-click='startAnimation()'> <i class='fa fa-play' aria-hidden='true'></i> </btn> " +
                "<btn ng-if='animating' class='btn btn-danger' id='start-animation' ng-click='stopAnimation()' tooltip='stop animation' data-toggle='tooltip'  " +
                " data-placement='right' title='stop animation'> <i class='fa fa-stop' aria-hidden='true'></i> </btn><br>" +
                "<label for='speed'>" + " Animation speed:&nbsp;" + "<input id='speed' type='range' min='1' max='999' step='10' value='10'> " + "</label><br>" +
                "<span class='label label-primary'>{{activeRouteTSTimeAgo}}</span><br>" +
                "<span class='label label-primary'>{{activeRouteTS}}</span><br>" +
                "<span class='label label-primary'>{{activeRoutePoint | lonlat:{ decimals : 3, pp: true} }}</span><br>" +
                "<span class='label label-primary'>{{activeRouteCOG}}</span> COG<br>" +
                "<span class='label label-primary'>{{activeRouteSOG}}</span> knots SOG<br>" +
                "</div>"
                ,
                scope: {
                    name: '@',
                    autoplay: '=?',
                    points: '=?',
                    feat: '=?'
                },
                link: function (scope, element, attrs, ctrl) {
                    console.log("got route" + scope.points.length +
                        " feat=" + scope.feat.length +
                        "autoplay=" + scope.autoplay
                     //   " scope.points=" + JSON.stringify(scope.points)
                    );
                    var olScope = ctrl.getOpenlayersScope();
                    var routeLayers;


                    olScope.getMap().then(function (map) {

                        var vectorSource = new ol.source.Vector({
                            features: []
                        });


                        var routeFeatureLayer = new ol.layer.Vector({
                            name: "routeVectorLayer",
                            title: "route",
                            source: vectorSource,
                            visible: true
                        });

                        routeFeatureLayer.getSource().addFeatures(scope.feat);
                        map.getView().setCenter(routeFeatureLayer.getSource().getFeatures()[0].getGeometry().getCoordinates());

                        // Clean up when the layer is destroyed
                        scope.$on('$destroy', function () {
                            if (angular.isDefined(routeLayers)) {
                                map.removeLayer(routeLayers);
                            }
                        });
                        var lineRoute = new ol.geom.LineString(scope.points);
                        var routeCoords = lineRoute.getCoordinates();
                        var routeLength = routeCoords.length;
                        //console.log("lineRoute: " + lineRoute + " routeCoords: " + routeCoords + " length: " + routeLength);

                        var routeFeature = new ol.Feature({
                            type: 'dashed-route',
                            geometry: lineRoute
                        });

                        var startMarker = scope.feat[0];
                        var endMarker = scope.feat[scope.feat.length - 1];

                        var styles = {
                            'dashed-route': new ol.style.Style({
                                stroke: new ol.style.Stroke({
                                    //lineDash: [10, 20, 0, 20],
                                    lineDash: [5, 10, 0, 10],
                                    lineJoin: 'miter',
                                    width: 4,
                                    color: [255, 0, 0, 0.8]
                                })
                            }),
                            'route': new ol.style.Style({
                                stroke: new ol.style.Stroke({
                                    width: 6, color: [237, 212, 0, 0.8]
                                })
                            }),
                            'icon': new ol.style.Style({
                                image: new ol.style.Icon({
                                    anchor: [0.5, 1],
                                    src: 'img/geolocation_marker.png'
                                })
                            }),
                            'geoMarker': new ol.style.Style({
                                image: new ol.style.Icon({
                                    anchor: [0.5, 1],
                                    src: 'img/geolocation_marker.png'
                                })
                            })
                        };

                        scope.animating = false;
                        var speed, now, orgIndexVal;
                        var speedInput = document.getElementById('speed');
                        // var startButton = document.getElementById('start-animation');

                        var animationLayer = new ol.layer.Vector({
                            source: new ol.source.Vector({
                                features: [routeFeature, startMarker, endMarker]
                            }),
                            style: function (feature) {
                                // hide geoMarker if animation is active
                                if (scope.animating && feature.get('type') === 'geoMarker') {
                                    return null;
                                }
                                return styles[feature.get('type')];
                            }
                        });

                        var index = 0;

                        var moveFeature = function (event) {
                            var vectorContext = event.vectorContext;
                            var frameState = event.frameState;

                            if (scope.animating) {
                                var elapsedTime = frameState.time - now;
                                // here the trick to increase speed is to jump some indexes
                                // on lineString coordinates
                                var index = Math.round(speed * elapsedTime / 1000);

                                if (index >= routeLength) {
                                    index = 0; // rewind, and loop
                                    scope.stopAnimation(true);
                                }

                                var feature = scope.feat[index];
                                vectorContext.drawFeature(feature, feature.getStyle());
                                scope.activeRoutePoint = feature.get('position');
                                scope.activeRouteTS = feature.get('ts');
                                scope.activeRouteTSTimeAgo = feature.get('tsTimeAgo');
                                scope.activeRouteCOG = feature.get('cog');
                                scope.activeRouteSOG = feature.get('sog');
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

                        var extent = animationLayer.getSource().getExtent();
                        map.getView().fit(extent, map.getSize());  // automatically zoom and pan the map to fit my features
                        routeLayers = new ol.layer.Group({
                            title: 'Route',
                            layers: [animationLayer],
                            visible: true
                        });
                        map.addLayer(routeLayers);

                        if (scope.autoplay) {
                            scope.startAnimation();
                        }
                    });
                }
            }
        }]);
 