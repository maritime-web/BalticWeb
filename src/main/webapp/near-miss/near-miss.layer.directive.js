(function () {

    angular.module('maritimeweb.near-miss')

    /**
     * Supports illustrating a near miss situation between two vessels
     */
    .directive('mapNearMissLayer', ['MapService', '$timeout',
        function (MapService, $timeout) {
            return {
                restrict: 'E',
                require: '^olMap',
                template: "",
                scope: {
                    name:           '@',
                    nearMissDataSet: '=?',

                    fitExtent:      '@',
                    maxZoom:        '@'
                },
                link: function(scope, element, attrs, ctrl) {
                    var olScope = ctrl.getOpenlayersScope();
                    var nearMissLayer;
                    var maxZoom = scope.maxZoom ? parseInt(scope.maxZoom) : 12;
                    var ownEventIdPrefix = "OWN_NEAR_MISS_EVENT";
                    var otherEventIdPrefix = "OTHER_NEAR_MISS_EVENT";

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
                                return state.getLonLat();
                            });
                            var route = MapService.createLineString(lonLats);

                            var routeFeature = new ol.Feature({
                                geometry: route
                            });
                            routeFeature.setStyle(style);

                            return routeFeature;
                        }

                        function createNearMissStyle() {
                            var style = new ol.style.Style({
                                stroke: new ol.style.Stroke({
                                    color: 'rgba(255, 165, 0, 0.8)',
                                    width: 4
                                })
                            });
                            return function (feature, resolution) {
                                var styles = [style];
                                if (scope.nearMissDataSet.zoomTarget) {
                                    var id = scope.nearMissDataSet.zoomTarget.id;
                                    var ownId = ownEventIdPrefix + id;
                                    var otherId = otherEventIdPrefix + id;
                                    if (feature.getId() === ownId || feature.getId() === otherId) {
                                        style.getStroke().setWidth(5);
                                        style.getStroke().setColor('rgba(255, 165, 0, 1.0)');
                                    }
                                }
                                return styles;
                            }
                        }
                        function createNeaMissRoutes(event) {
                            var ownRouteFeature = new ol.Feature({
                                geometry: MapService.createLineString(event.getOwnLonLats())
                            });
                            ownRouteFeature.setId(ownEventIdPrefix + event.id);
                            ownRouteFeature.setStyle(createNearMissStyle());
                            nearMissLayer.getSource().addFeature(ownRouteFeature);

                            var otherRouteFeature = new ol.Feature({
                                geometry: MapService.createLineString(event.getOtherLonLats())
                            });
                            otherRouteFeature.setId(otherEventIdPrefix + event.id);
                            otherRouteFeature.setStyle(createNearMissStyle());
                            nearMissLayer.getSource().addFeature(otherRouteFeature);
                        }

                        function createVesselFeature(options) {
                            var vesselState = options.state;
                            var style = options.style;

                            function toLonLat(p) {
                                return [p.geometry.coordinates[0], p.geometry.coordinates[1]];
                            }

                            var dim = vesselState.dimensions;
                            var halfBeam = (dim.beam / 2) / 1000;
                            var halfLoa = (dim.loa / 2) / 1000;
                            //turf calc
                            // var options = {units: 'kilometers'};

                            var posPoint = turf.point(vesselState.getLonLat());
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

                            //Intentionally added p5 twice to ensure correct rendering during animation
                            var lonLats = [toLonLat(p1), toLonLat(p2), toLonLat(p3), toLonLat(p4), toLonLat(p5), toLonLat(p5)];

                            //OpenLayer
                            var poly = MapService.createPolygon(lonLats);

                            var anchor = MapService.fromLonLat(vesselState.getLonLat());
                            poly.rotate(vesselState.getRadianHdg(), anchor);

                            var vesselFeature = new ol.Feature({
                                geometry: poly
                            });

                            vesselFeature.setStyle(style);

                            return vesselFeature;
                        }

                        function createOwnSafetyZone(options) {
                            var vesselState = options.state;
                            var cog = vesselState.cog;
                            var c = vesselState.getSafetyZoneCenterPosition();

                            var poly = MapService.createPolygon(vesselState.getPointsForSafetyZone());
                            poly.rotate(-Math.PI/2 + (-cog) * ((Math.PI / 180)), MapService.fromLonLat(c));

                            var safetyZoneFeature = new ol.Feature({
                                geometry: poly
                            });

                            safetyZoneFeature.setStyle(safetyZoneStyle);

                            return safetyZoneFeature;
                        }

                        function createStartNearMiss(options) {
                            var state = options.state;
                            var point = MapService.createPoint(state.getLonLat());

                            var nearMissStartFeature = new ol.Feature({
                                geometry: point
                            });

                            var style = new ol.style.Style(/** @type {olx.style.StyleOptions}*/{
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
                                    text: state.time.format('YYYY-MM-DD HH:mm:ss'),
                                    fill: new ol.style.Fill({color: 'red'}),
                                    stroke: new ol.style.Stroke({color: 'white', width: 3}),
                                    offsetX: 0,
                                    offsetY: 20,
                                    rotation: 0
                                })
                            });
                            nearMissStartFeature.setStyle(getStartStopStyle(style, state));
                            nearMissLayer.getSource().addFeature(nearMissStartFeature);
                        }

                        function createStopNearMiss(options) {
                            var state = options.state;
                            var point = MapService.createPoint(state.getLonLat());

                            var nearMissStartFeature = new ol.Feature({
                                geometry: point
                            });

                            var style = new ol.style.Style(/** @type {olx.style.StyleOptions}*/{
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
                                    text: state.time.format('YYYY-MM-DD HH:mm:ss'),
                                    fill: new ol.style.Fill({color: 'green'}),
                                    stroke: new ol.style.Stroke({color: 'white', width: 3}),
                                    offsetX: 0,
                                    offsetY: 12,
                                    rotation: Math.PI / 12
                                })
                            });

                            nearMissStartFeature.setStyle(getStartStopStyle(style, state));
                            nearMissLayer.getSource().addFeature(nearMissStartFeature);

                        }

                        function getStartStopStyle(initialStyle, state) {
                            return function (feature, resolution) {
                                var styles = [initialStyle];
                                if (resolution > 40) {
                                    initialStyle.getText().setText("");
                                    initialStyle.getImage().setRadius(2);
                                    initialStyle.getImage().setOpacity(0.3);
                                } else {
                                    initialStyle.getText().setText(state.time.format('YYYY-MM-DD HH:mm:ss'));
                                    initialStyle.getImage().setRadius(4);
                                    initialStyle.getImage().setOpacity(1.0);
                                }
                                return styles;

                            }
                        }

                        function updateDataSet() {
                            if (scope.nearMissDataSet) {
                                nearMissLayer.getSource().clear();

                                nearMissLayer.getSource().addFeature(createRouteFeature({vesselStates: scope.nearMissDataSet.ownStates, style: routeStyle}));
                                var ownVesselLastState = scope.nearMissDataSet.ownStates[scope.nearMissDataSet.ownStates.length - 1];
                                nearMissLayer.getSource().addFeature(createVesselFeature({state: ownVesselLastState, style: createVesselStyle(0.2)}));
                                nearMissLayer.getSource().addFeature(createOwnSafetyZone({state: ownVesselLastState}));

                                scope.nearMissDataSet.nearMissEvents.forEach(function (event) {
                                    createNeaMissRoutes(event)
                                });

                                angular.forEach(scope.nearMissDataSet.otherStates, function (otherStates) {
                                    nearMissLayer.getSource().addFeature(createRouteFeature({vesselStates: otherStates, style: otherRouteStyle}));
                                    nearMissLayer.getSource().addFeature(createVesselFeature({
                                        state: otherStates[otherStates.length - 1],
                                        style: createOtherVesselStyle(0.2)
                                    }));
                                });

                                if (scope.nearMissDataSet.zoomTarget) {
                                    var id = scope.nearMissDataSet.zoomTarget.id;
                                    var ownId = ownEventIdPrefix + id;
                                    var otherId = otherEventIdPrefix + id;
                                    fitExtent([
                                        nearMissLayer.getSource().getFeatureById(ownId),
                                        nearMissLayer.getSource().getFeatureById(otherId)
                                    ]);
                                } else {
                                    fitExtent(nearMissLayer.getSource().getFeatures());
                                }

                                if (scope.nearMissDataSet.animateTarget) {
                                    var nm = scope.nearMissDataSet.animateTarget;
                                    var ownStates = nm.ownStates;
                                    var otherStates = nm.otherStates;
                                    var ownIndex = 0;
                                    var otherIndex = 0;

                                    var duration = 5000;
                                    var coolOfFrames = 100;
                                    var start = new Date().getTime();

                                    // console.log(ownStates.length);
                                    // console.log(otherStates.length);

                                    map.on('postcompose', moveAlong);
                                    map.render();

                                    //animation
                                    function moveAlong(event) {
                                        var vectorContext = event.vectorContext;
                                        var frameState = event.frameState;
                                        var elapsed = frameState.time - start;
                                        var elapsedRatio = elapsed / duration;

                                        if (elapsedRatio > 1) {
                                            if (coolOfFrames > 0) {
                                                elapsedRatio = 1;
                                                coolOfFrames--;
                                            } else {
                                                map.un('postcompose', moveAlong);
                                                return;
                                            }
                                        }

                                        ownIndex = Math.round((ownStates.length - 1) * elapsedRatio);
                                        otherIndex =  Math.round((otherStates.length - 1) *elapsedRatio);

                                        console.log('ownIndex: ' + ownIndex + ' otherIndex: ' + otherIndex + ' elapsed: ' + elapsed + ' elapsedRatio: ' + elapsedRatio);

                                        if (ownIndex < ownStates.length) {
                                            var own = ownStates[ownIndex];
                                            var ownVessel = createVesselFeature({state: own, style: createVesselStyle(1.0)});
                                            var safetyZone = createOwnSafetyZone({state: own});
                                            vectorContext.drawFeature(ownVessel, ownVessel.getStyle());
                                            vectorContext.drawFeature(safetyZone, safetyZone.getStyle());
                                        }

                                        if (otherIndex < otherStates.length) {
                                            var other = otherStates[otherIndex];
                                            var otherVessel = createVesselFeature({
                                                state: other,
                                                style: createOtherVesselStyle(1.0)
                                            });
                                            vectorContext.drawFeature(otherVessel, otherVessel.getStyle());
                                        }

                                        map.render();
                                    }
                                }
                            }
                        }

                        //Redraw when a new data set is loaded
                        scope.$watch('nearMissDataSet', updateDataSet, true);


                        function fitExtent(features) {
                            var fitExtent = false;
                            var extent = ol.extent.createEmpty();
                            if (features && features.length > 0) {
                                features.forEach(function (f) {
                                    ol.extent.extend(extent, f.getGeometry().getExtent());
                                });
                                fitExtent = true;
                            }
                            if (fitExtent) {
                                map.getView().fit(extent, map.getSize(), {
                                    padding: [20, 20, 20, 20],
                                    maxZoom: maxZoom
                                });
                            }
                        }

                        /***************************/
                        /** Map creation          **/
                        /***************************/

                        map.addLayer(nearMissLayer);
                    });
                }
            };
        }])
})();
