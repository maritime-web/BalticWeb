(function () {

    angular.module('maritimeweb.near-miss')

    /**
     * Supports illustrating a near miss situation between two vessels
     */
    .directive('mapNearMissLayer', ['$rootScope', '$timeout', 'MapService',
        function ($rootScope, $timeout, MapService) {
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

                    function ownFilter(state) {
                        return scope.ownVesselMmsi === state.mmsi;
                    }


                    olScope.getMap().then(function(map) {

                        // Clean up when the layer is destroyed
                        scope.$on('$destroy', function() {
                            if (angular.isDefined(nearMissLayer)) {
                                map.removeLayer(nearMissLayer);
                            }
                        });

                        var vesselStyle = new ol.style.Style({
                            fill: new ol.style.Fill({ color: 'rgba(255, 0, 255, 0.2)' }),
                            stroke: new ol.style.Stroke({ color: '#8B008B', width: 1 })
                        });

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


                        function createOwnRouteFeature() {
                            var lonLats = ownVesselStates.map(function (state) {
                                return [state.position.lon, state.position.lat];
                            });
                            var ownRoute = MapService.createLineString(lonLats);
                            console.log(ownRoute.getExtent());

                            var ownRouteFeature = new ol.Feature({
                                geometry: ownRoute
                            });
                            ownRouteFeature.setStyle(routeStyle);

                            nearMissLayer.getSource().addFeature(ownRouteFeature);
                        }

                        createOwnRouteFeature();

                        function createOwnVesselFeature() {
                            function toLonLat(p) {
                                return [p.geometry.coordinates[0], p.geometry.coordinates[1]];
                            }

                            var dim = ownVesselLastState.dimensions;
                            var pos = ownVesselLastState.position;
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
                            var radianHdg = (ownVesselLastState.hdg / 360) * 2 * Math.PI;
                            poly.rotate(radianHdg, anchor);
                            console.log(poly.getExtent());
                            var ownVesselFeature = new ol.Feature({
                                geometry: poly
                            });

                            ownVesselFeature.setStyle(vesselStyle);

                            nearMissLayer.getSource().addFeature(ownVesselFeature);
                        }

                        createOwnVesselFeature();

                        function createOwnSafetyZone() {
                            var cog = ownVesselLastState.cog;
                            var a = ownVesselLastState.safetyZone.a;
                            var b = ownVesselLastState.safetyZone.b;

                            var c = [ownVesselLastState.safetyZone.centerPosition.lon, ownVesselLastState.safetyZone.centerPosition.lat];
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
                            poly.rotate(-Math.PI/2 + cog, MapService.fromLonLat(c));

                            var safetyZoneFeature = new ol.Feature({
                                geometry: poly
                            });

                            safetyZoneFeature.setStyle(safetyZoneStyle);

                            nearMissLayer.getSource().addFeature(safetyZoneFeature);

                        }

                        createOwnSafetyZone();

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
