angular.module('maritimeweb.serviceregistry')
    .directive('mapServiceRegistryLayer', ['$rootScope', '$timeout', 'Auth', 'ServiceRegistryService', 'MapService','growl', '$log',
        function ($rootScope, $timeout, Auth, ServiceRegistryService, MapService, growl, $log) {
            return {
                restrict: 'E',
                replace: false,
                //template: '<div>SR - layer</div>',
                require: '^olMap',
                scope: {
                    name: '@',
                    alerts: '=?',
                    vessels: '=?'
                },
                link: function ($scope, element, attrs, ctrl) {
                    var olScope = ctrl.getOpenlayersScope();

                    $scope.loggedIn = Auth.loggedIn;


                    olScope.getMap().then(function (map) {


                        /***************************/
                        /** MaritimeCloud Layers      **/
                        /***************************/


                        // Construct the boundary layers
                        var boundaryLayer = new ol.layer.Vector({
                            id: 'mcboundary',
                            //title: 'MaritimeCloud Service Instance AREA',
                            title: 'mcboundary',
                            name: 'mcboundary',
                            zIndex: 11,
                            source: new ol.source.Vector({
                                features: new ol.Collection(),
                                wrapX: false
                            }),
                            style: [ServiceRegistryService.greenServiceStyle]
                        });


                        var duration = 3000;

                        boundaryLayer.setZIndex(11);
                        boundaryLayer.setVisible(true);


                        /***************************/
                        /** Map creation          **/
                        /***************************/

                        // Construct No Go Layer Group layer
                        var mcSRGroupLayer = new ol.layer.Group({
                            title: 'MC Service Registry',
                            name: 'MC Service Registry',
                            zIndex: 11,
                            layers: [boundaryLayer]
                        });
                        mcSRGroupLayer.setZIndex(11);
                        mcSRGroupLayer.setVisible(true);

                        map.addLayer(mcSRGroupLayer);

                        $rootScope.mapMCLayers = mcSRGroupLayer; // add group-layer to rootscope so it can be enabled/disabled

                        /**
                         * Add a click handler to the map to mark the features.
                         */
                        map.on('singleclick', function (evt) {
                            $rootScope.highlightedInstances = []; // reset all selections
                            $rootScope.highlightedInstancescoordinate = [];

                            var features = $rootScope.mapMCLayers.getLayers().getArray()[0].getSource().getFeatures();
                            $log.info("Features found " + features.length);

                            angular.forEach(features, function (feature) { // reset all highlights
                                feature.setStyle(ServiceRegistryService.greenServiceStyle);
                            });

                            var feature = map.forEachFeatureAtPixel(evt.pixel, function (feature) {
                                /*
                                 $log.info("feature found instanceId " + feature.instanceId);
                                 $log.info("feature found id " + feature.id);
                                 $log.info("feature found name " + feature.name);
                                 */
                                feature.setStyle(ServiceRegistryService.highlightServiceRed);

                                $rootScope.highlightedInstances.push(feature.instanceId);
                                return false;
                            }, {hitTolerance: 4});

                            var tmp = MapService.toLonLat(evt.coordinate); //evt.coordinate;
                            $rootScope.highlightedInstancescoordinate = { lon: tmp[0], lat: tmp[1] }; // MapService.toLonLat(evt.coordinate); //evt.coordinate;
                            $rootScope.highlightedInstancescoordinateXY = MapService.toLonLat(evt.coordinate);

                            $rootScope.$apply();


                            var markerStyle = new ol.style.Style({
                                image: new ol.style.Circle({
                                    radius: 4,
                                    stroke: new ol.style.Stroke({
                                        color: '#330505',
                                        width: 1
                                    }),
                                    fill: new ol.style.Fill({
                                        color: '#FF3333' // attribute colour
                                    })
                                })
                            });

                            var markerFeature = new ol.Feature({
                                geometry: new ol.geom.Point(evt.coordinate) //new ol.geom.Point()
                            });
                            markerFeature.setStyle(markerStyle);

                            $rootScope.mapMCLayers.getLayers().getArray()[0].getSource().addFeature(markerFeature);

                        })

                    })
                }
            }
        }]);
