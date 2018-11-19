(function () {

    angular.module('maritimeweb.near-miss')

    /**
     * Supports illustrating a near miss situation between two vessels
     */
    .directive('mapNearMissLayer', ['$rootScope', '$timeout', 'MapService', 'moment',
        function ($rootScope, $timeout, MapService, moment) {
            return {
                restrict: 'E',
                require: '^olMap',
                template: "",
                scope: {
                    name:           '@',
                    ownVesselMmsi:  '=?',
                    vesselStates:   '=?',

                    fitExtent:      '@',
                    maxZoom:        '@'
                },
                link: function(scope, element, attrs, ctrl) {
                    var olScope = ctrl.getOpenlayersScope();
                    var nearMissLayer;
                    var maxZoom = scope.maxZoom ? parseInt(scope.maxZoom) : 12;
                    var ownVesselStates = scope.vesselStates.filter(ownFilter);
                    var ownVesselLastState = ownVesselStates[ownVesselStates.length - 1];

                    var otherVesselStates = {};
                    scope.vesselStates.filter(otherFilter).forEach(function (state) {
                        var mmsi = state.mmsi;
                        if (!otherVesselStates[mmsi]) {
                            otherVesselStates[mmsi] = [];
                        }
                        otherVesselStates[mmsi].push(state);
                    });


                    function ownFilter(state) {
                        return scope.ownVesselMmsi === state.mmsi;
                    }
                    function otherFilter(state) {
                        return scope.ownVesselMmsi !== state.mmsi;
                    }


                    olScope.getMap().then(function(map) {

                        // Clean up when the layer is destroyed
                        scope.$on('$destroy', function() {
                            if (angular.isDefined(nearMissLayer)) {
                                map.removeLayer(nearMissLayer);
                            }
                        });

                        function createVesselStyle(alpha) {
                            return new ol.style.Style({
                                fill: new ol.style.Fill({ color: 'rgba(255, 0, 255, ' + alpha +')' }),
                                stroke: new ol.style.Stroke({ color: '#8B008B', width: 1 })
                            });
                        }

                        function createOtherVesselStyle(alpha) {
                            return new ol.style.Style({
                                fill: new ol.style.Fill({ color: 'rgba(191, 127, 63, ' + alpha + ')' }),
                                stroke: new ol.style.Stroke({ color: '#8B008B', width: 1 })
                            });
                        }

                        var safetyZoneStyle = new ol.style.Style({
                            fill: new ol.style.Fill({ color: 'rgba(0,255,0, 0.2)' }),
                            stroke: new ol.style.Stroke({ color: '#FF0000', width: 1 })
                        });

                        var routeStyle = new ol.style.Style({
                            stroke: new ol.style.Stroke({
                                color: '#FF0000',
                                width: 2,
                                lineDash: [5, 5, 0, 5]
                            })
                        });

                        var otherRouteStyle = new ol.style.Style({
                            stroke: new ol.style.Stroke({
                                color: '#3FBF7F',
                                width: 2,
                                lineDash: [5, 5, 0, 5]
                            })
                        });


                        // Construct the Near miss layer
                        nearMissLayer = new ol.layer.Vector({
                            name: 'Near miss',
                            title: 'Near miss',
                            source: new ol.source.Vector({
                                features: new ol.Collection(),
                                wrapX: false
                            })
                        });
                        nearMissLayer.setZIndex(11);
                        nearMissLayer.setVisible(true);


                        function createRouteFeature(options) {
                            var vesselStates = options.vesselStates;
                            var style = options.style;
                            var lonLats = vesselStates.map(function (state) {
                                return [state.position.lon, state.position.lat];
                            });
                            var ownRoute = MapService.createLineString(lonLats);
                            console.log(ownRoute.getExtent());

                            var ownRouteFeature = new ol.Feature({
                                geometry: ownRoute
                            });
                            ownRouteFeature.setStyle(style);

                            nearMissLayer.getSource().addFeature(ownRouteFeature);
                        }

                        function createVesselFeature(options) {
                            var vesselState = options.state;
                            var style = options.style;

                            function toLonLat(p) {
                                return [p.geometry.coordinates[0], p.geometry.coordinates[1]];
                            }

                            var dim = vesselState.dimensions;
                            var pos = vesselState.position;
                            var halfBeam = (dim.beam / 2) / 1000;
                            var halfLoa = (dim.loa / 2) / 1000;
                            //turf calc
                            // var options = {units: 'kilometers'};

                            var posPoint = turf.point([pos.lon, pos.lat]);
                            var dist = halfBeam;
                            var bearing = 90;
                            var p1 = turf.destination(posPoint, dist, bearing);

                            dist = halfLoa;
                            bearing = 180;
                            var p2 = turf.destination(p1, dist, bearing);

                            dist = 2*halfBeam;
                            bearing = -90;
                            var p3 = turf.destination(p2, dist, bearing);

                            dist = halfLoa;
                            bearing = 0;
                            var p4 = turf.destination(p3, dist, bearing);

                            dist = halfLoa;
                            bearing = 0;
                            var p5 = turf.destination(posPoint, dist, bearing);

                            var lonLats = [toLonLat(p1), toLonLat(p2), toLonLat(p3), toLonLat(p4), toLonLat(p5)];

                            //OpenLayer
                            var poly = MapService.createPolygon(lonLats);

                            var anchor = MapService.fromLonLat([pos.lon, pos.lat]);
                            var radianHdg = (-vesselState.hdg) * (Math.PI / 180);
                            poly.rotate(radianHdg, anchor);

                            var vesselFeature = new ol.Feature({
                                geometry: poly
                            });

                            vesselFeature.setStyle(style);

                            nearMissLayer.getSource().addFeature(vesselFeature);
                        }

                        function createOwnSafetyZone(options) {
                            var vesselState = options.state;
                            var cog = vesselState.cog;
                            var a = vesselState.safetyZone.a;
                            var b = vesselState.safetyZone.b;

                            var c = [vesselState.safetyZone.centerPosition.lon, vesselState.safetyZone.centerPosition.lat];
                            var elipseLonLatsPos = [];
                            var elipseLonLatsNeg = [];
                            var stepCount = 64;
                            var step = 2*a/stepCount;
                            var metersPrDeg = 111139;
                            var translateX = c[0];
                            var translateY = c[1];

                            for (var i = 0; i <= stepCount; i++) {
                                var x = (-a + i * step);
                                var yPos = (b/a * Math.sqrt(a*a - x*x));
                                var yNeg = -yPos;
                                x = x/metersPrDeg + translateX;
                                yPos = yPos / metersPrDeg + translateY;
                                yNeg = yNeg / metersPrDeg + translateY;
                                elipseLonLatsPos.push([x, yPos]);
                                elipseLonLatsNeg.push([x, yNeg]);
                            }

                            var elipseLonLats = elipseLonLatsPos.concat(elipseLonLatsNeg.reverse());

                            var poly = MapService.createPolygon(elipseLonLats);
                            poly.rotate(-Math.PI/2 + (-cog) * ((Math.PI / 180)), MapService.fromLonLat(c));

                            var safetyZoneFeature = new ol.Feature({
                                geometry: poly
                            });

                            safetyZoneFeature.setStyle(safetyZoneStyle);

                            nearMissLayer.getSource().addFeature(safetyZoneFeature);

                        }

                        function createStartNearMiss(options) {
                            var state = options.state;
                            var point = MapService.createPoint([state.position.lon, state.position.lat]);

                            var nearMissStartFeature = new ol.Feature({
                                geometry: point
                            });

                            nearMissStartFeature.setStyle(new ol.style.Style(/** @type {olx.style.StyleOptions}*/{
                                image: new ol.style.Circle({
                                    radius: 8,
                                    snapToPixel: false,
                                    fill: new ol.style.Fill({
                                        color: 'red'
                                    }),
                                    stroke: new ol.style.Stroke({
                                        color: 'black',
                                        width: 1
                                    })
                                }),
                                text: new ol.style.Text({
                                    textAlign: 'center',
                                    font: 'bold 12px Arial',
                                    text: state.time,
                                    fill: new ol.style.Fill({color: 'red'}),
                                    stroke: new ol.style.Stroke({color: 'white', width: 3}),
                                    offsetX: 0,
                                    offsetY: 20,
                                    rotation: 0
                                })
                            }));
                            nearMissLayer.getSource().addFeature(nearMissStartFeature);
                        }

                        function createStopNearMiss(options) {
                            var state = options.state;
                            var point = MapService.createPoint([state.position.lon, state.position.lat]);

                            var nearMissStartFeature = new ol.Feature({
                                geometry: point
                            });

                            nearMissStartFeature.setStyle(new ol.style.Style(/** @type {olx.style.StyleOptions}*/{
                                image: new ol.style.Circle({
                                    radius: 8,
                                    snapToPixel: false,
                                    fill: new ol.style.Fill({
                                        color: 'green'
                                    }),
                                    stroke: new ol.style.Stroke({
                                        color: 'black',
                                        width: 1
                                    })
                                }),
                                text: new ol.style.Text({
                                    textAlign: 'start',
                                    font: 'bold 12px Arial',
                                    text: state.time,
                                    fill: new ol.style.Fill({color: 'green'}),
                                    stroke: new ol.style.Stroke({color: 'white', width: 3}),
                                    offsetX: 0,
                                    offsetY: 12,
                                    rotation: Math.PI/12
                                })
                            }));
                            nearMissLayer.getSource().addFeature(nearMissStartFeature);

                        }

                        createRouteFeature({vesselStates: ownVesselStates, style: routeStyle});
                        createVesselFeature({state: ownVesselLastState, style: createVesselStyle(0.2)});
                        createOwnSafetyZone({state: ownVesselLastState});

                        function getOwnStateCloseTo(state) {
                            var candidate = ownVesselStates[0];
                            ownVesselStates.forEach(function (ownState) {
                                var time = moment(state.time).valueOf();
                                var ownTime = moment(ownState.time).valueOf();
                                var candidateTime = moment(candidate.time).valueOf();

                                if (Math.abs(ownTime-time) < Math.abs(candidateTime-time)) {
                                    candidate = ownState;
                                }
                            });
                            return candidate;
                        }

                        angular.forEach(otherVesselStates, function (otherStates) {
                            createRouteFeature({vesselStates: otherStates, style: otherRouteStyle});
                            createVesselFeature({state: otherStates[otherStates.length - 1], style: createOtherVesselStyle(0.2)});
                            var nm = false;
                            otherStates.forEach(function (state) {
                                if (nm !== state.nearMissFlag) {
                                    var ownState = getOwnStateCloseTo(state);
                                    if (state.nearMissFlag) {
                                        createStartNearMiss({state: state});
                                        createStartNearMiss({state: ownState});
                                    } else {
                                        createStopNearMiss({state: state});
                                        createStopNearMiss({state: ownState});
                                    }
                                    // createVesselFeature({state: state, style: createOtherVesselStyle(0.1)});
                                    // createVesselFeature({state: ownState, style: createVesselStyle(0.1)});
                                    // createOwnSafetyZone({state: ownState});

                                    nm = state.nearMissFlag;
                                }
                            });
                        });

                        /***************************/
                        /** Map creation          **/
                        /***************************/

                        map.addLayer(nearMissLayer);

                        if (scope.fitExtent === 'true') {
                            var fitExtent = false;
                            var extent = ol.extent.createEmpty();
                            if (nearMissLayer.getSource().getFeatures().length > 0) {
                                ol.extent.extend(extent, nearMissLayer.getSource().getExtent());
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
