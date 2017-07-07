angular.module('maritimeweb.serviceregistry')

/** Service for accessing AIS vessel data **/
    .service('ServiceRegistryService', ['$http', 'growl',
        function ($http, growl) {

            this.getServiceInstances = function (wkt) {
                var params = wkt ? '?wkt=' + encodeURIComponent(wkt) : '';
                var request = '/rest/service/lookup/' + params;
                return $http.get(request);
            };

            /**
             * Convert a Service registry instance to a OpenLayer tile layer.
             * @param aServiceInstance
             * @param daysAgo i.e. 0, 1, 2 days ago. 0 = today, 2 = two days ago.
             * @returns {ol.layer.Tile}
             */
            this.createWKTFromService = function (aServiceInstance, daysAgo) {
                //   $log.debug("createTileLayerFromService  aServiceInstance.url=" + aServiceInstance.url);
                var url = aServiceInstance.url; // http://satellite.e-navigation.net:8080/BalticSea.latest.terra.250m/{z}/{x}/{y}.png
                var description = "";
                var timeAgo;

                var mcNoGoAttributions = [
                    new ol.Attribution({
                        html: '<div class="panel panel-info">' +
                        '<div class="panel-heading">Satellite image from NASA</div>' +
                        '<div class="panel-body">' +
                        '<span>We acknowledge the use of data products or imagery from the Land, Atmosphere Near real-time Capability for EOS (LANCE) system operated by the NASA/GSFC/Earth Science Data and Information System (ESDIS) with funding provided by NASA/HQ.</span>' +
                        '</div>' +
                        '</div>'

                    }),
                    ol.source.OSM.ATTRIBUTION
                ];
                return new ol.layer.Tile({
                    id: aServiceInstance.instanceId,
                    title: aServiceInstance.name, // 'NASA: one day ago - Aqua Satellite image',
                    description: description,

                    zIndex: 0,
                    source: new ol.source.XYZ({
                        urls: [url],
                        attributions: mcNoGoAttributions,
                        minZoom: 3,
                        maxZoom: 8,
                        tilePixelRatio: 1.000000
                    }),
                    visible: false
                });
            };

        }])
    .directive('mapMCServiceInstanceLayer', ['$rootScope', '$timeout', 'Auth', 'MapService', 'ServiceRegistryService', 'growl', '$log', '$window',
        function ($rootScope, $timeout, Auth, MapService, SatelliteService, growl, $log, $window) {
            return {
                restrict: 'E',
                replace: false,
                template: '',
                require: '^olMap',
                scope: {
                    name: '@'
                },
                link: function (scope, element, attrs, ctrl) {
                    var olScope = ctrl.getOpenlayersScope();
                    var loadTimer;
                    scope.loggedIn = Auth.loggedIn;

                    /** When the map extent changes, reload the Vessels's using a timer to batch up changes */
                    scope.mapChanged = function () {
                        if (scope.loggedIn) {
                            if (loadTimer) {
                                $timeout.cancel(loadTimer);
                            }
                            loadTimer = $timeout(scope.refreshServiceRegistry, 1000);
                        }
                    };

                    olScope.getMap().then(function (map) {
                        var mcServiceRegistryInstanceLayers = [];

                        var layerGroup = new ol.layer.Group({
                            title: 'MC Service Registry',
                            layers: mcServiceRegistryInstanceLayers,
                            visible: true,
                            zIndex: 0

                        });
                        layerGroup.setVisible(true);
                        map.addLayer(layerGroup);


                        // Clean up when the layer is destroyed
                        scope.$on('$destroy', function () {
                            $log.debug("MC layer destroyed");
                            if (angular.isDefined(layerGroup)) {
                                map.removeLayer(layerGroup);
                            }
                            if (angular.isDefined(loadTimer)) {
                                $timeout.cancel(loadTimer);
                            }
                        });


                        /** Refreshes the list of satellite images from the service registry */
                        scope.refreshServiceRegistry = function () {
                            $log.info("refreshing service register");
                            var mapState = JSON.parse($window.localStorage.getItem('mapState-storage')) ? JSON.parse($window.localStorage.getItem('mapState-storage')) : {};
                            var wkt = mapState['wktextent'];

                            $rootScope.mapMCLayers = layerGroup; // add group-layer to rootscope so it can be enabled/disabled


                            ServiceRegistryService.getServiceInstances(wkt).success(function (services, status) {

                                // Update the selected status from localstorage
                                if (status == 204) {
                                    $rootScope.mcServiceRegistryInstanceStatus = 'false';
                                    $window.localStorage[ServiceRegistryService.serviceID()] = 'false';
                                    while($rootScope.mapWeatherLayers.getLayers().getArray().length > 0) {
                                        $rootScope.mapWeatherLayers.getLayers().getArray().pop().setVisible(false);
                                    }
                                    // TODO: Need to store visibility state for each satellite instance...
                                }

                                if (status == 200) {
                                    $rootScope.mcServiceRegistryInstancesStatus = 'true';
                                    $window.localStorage[ServiceRegistryService.serviceID()] = 'true';
                                    angular.forEach(services, function (service) {
                                        var shouldAddService = true;

                                        if ($rootScope.mapWeatherLayers.getLayers().getArray().length > 0) {
                                            angular.forEach($rootScope.mapMCLayers.getLayers().getArray(), function (existingServices) {
                                                if (existingServices.get('id') == service.instanceId) {
                                                    //$log.debug("Already Found " + service.name + " satellite in local list - move on");
                                                    shouldAddService = false;
                                                }
                                            });
                                        }

                                        if (shouldAddService) {
                                            $log.debug("### Adding SR instance " + service.name);
                                            // add to localstorage, false as default


                                            for (var i = 0; i < 1; i++) { // only yesterday. Extend here if want to display more older layers
                                                var instanceLayer = ServiceRegistryService.createTileLayerFromService(service, i);
                                                $rootScope.mapMCLayers.getLayers().getArray().push(instanceLayer);
                                                map.addLayer(instanceLayer);
                                                instanceLayer.on('change:visible', scope.mapChanged);
                                                // var active = $window.localStorage.getItem(service.instanceId);
                                                // $log.debug("     active" + active + " #" + service.instanceId);
                                                if ( $window.localStorage.getItem(service.instanceId) === "true") {
                                                    $log.info("layer should be active!");
                                                    instanceLayer.setVisible(true);
                                                    instanceLayer.setZIndex(0);
                                                } else {
                                                    $window.localStorage.setItem(service.instanceId, false);
                                                }
                                            }
                                        }
                                    });

                                }

                            }).error(function (error) {
                                $log.error("Error retrieving MC service instances " + error);
                                layerGroup.setVisible(false);
                            });

                        };

                        // update the map when a user pan-move ends.
                        map.on('moveend', scope.mapChanged);

                    });
                }
            };
        }]);