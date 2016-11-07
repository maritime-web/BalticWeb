/**
 * Defines the main NW-NM message layer. navigational warnings and notices to mariners
 *
 * TODO: Lifecycle management, convert to use backend data...
 */
angular.module('maritimeweb.nasa-satellite')

/** Service for retrieving NASA services **/
    .service('SatelliteService', ['$http', '$log', 'timeAgo', '$filter',
        function ($http, $log, timeAgo, $filter) {

            this.serviceID = function () {
                return 'urn:mrn:mcl:service:technical:dma:tiles-service'
            };
            this.serviceVersion = function () {
                return '0.2'
            };

            /**
             * Get NASA Services for WKT
             */
            this.getNasaServices = function (wkt) {
                var params = wkt ? '?wkt=' + encodeURIComponent(wkt) : '';
                var pathParam1 = encodeURIComponent(this.serviceID());
                var pathParam2 = encodeURIComponent(this.serviceVersion());
                var request = '/rest/service/lookup/' + pathParam1 + '/' + pathParam2 + params;
                return $http.get(request);
            };

            // http://stackoverflow.com/questions/8619879/javascript-calculate-the-day-of-the-year-1-366
            this.isLeapYear = function (anydate) {
                var year = anydate.getFullYear();
                if ((year & 3) != 0) return false;
                return ((year % 100) != 0 || (year % 400) == 0);
            };

            // Get Day of Year
            this.getDOY = function (anydate) {
                var dayCount = [0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334];
                var mn = anydate.getMonth();
                var dn = anydate.getDate();
                var dayOfYear = dayCount[mn] + dn;
                if (mn > 1 && this.isLeapYear(anydate)) dayOfYear++;
                return dayOfYear;
            };

            /**
             * Convert a Service registry instance to a OpenLayer tile layer.
             * @param aServiceInstance
             * @param daysAgo i.e. 0, 1, 2 days ago. 0 = today, 2 = two days ago.
             * @returns {ol.layer.Tile}
             */
            this.createTileLayerFromService = function (aServiceInstance, daysAgo) {
             //   $log.debug("createTileLayerFromService  aServiceInstance.url=" + aServiceInstance.url);
                var url = aServiceInstance.url; // http://satellite.e-navigation.net:8080/BalticSea.latest.terra.250m/{z}/{x}/{y}.png
                var description = "";
                var timeAgo;
                if (daysAgo == 0 || !daysAgo) {
                    url = url.replace("{date}", "latest");
                    description = "latest";
                } else {
                    var now = new Date();
                    now.setDate(now.getDate() - (daysAgo+1));
                    var year = now.getUTCFullYear();
                    var dayofyear = this.getDOY(now);
                    url = url.replace("{date}", year + "" + dayofyear);
                    description = $filter('timeAgo')(now.getTime()); // neatly filtered timestamp
                    timeAgo = now.getTime();
                }

                var nasaAttributions = [
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
                    timeago: timeAgo,
                    zIndex: 0,
                    source: new ol.source.XYZ({
                        urls: [url],
                        attributions: nasaAttributions,
                        minZoom: 3,
                        maxZoom: 8,
                        tilePixelRatio: 1.000000
                    }),
                    visible: false
                });
            };

 /*           this.createSatelliteLayers = function (serviceInstances) {
                var self = this;
                var layers = [];
                angular.forEach(serviceInstances, function (aServiceInstance) {
                    layers.push(self.createTileLayerFromService(aServiceInstance, 0));
                });
                return layers;
            }*/

        }])
    .directive('mapSatelliteLayer', ['$rootScope', '$timeout', 'Auth', 'MapService', 'SatelliteService', 'growl', '$log', '$window',
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
                    var satelliteLayers;
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
                        var satelliteLayers = [];

                        var layerGroup = new ol.layer.Group({
                            title: 'Satellite Imagery',
                            layers: satelliteLayers,
                            visible: true,
                            zIndex: 0

                        });
                        layerGroup.setVisible(true);
                        map.addLayer(layerGroup);


                        // Clean up when the layer is destroyed
                        scope.$on('$destroy', function () {
                            $log.debug("Satellite layer destroyed");
                            if (angular.isDefined(layerGroup)) {
                                map.removeLayer(layerGroup);
                            }
                            if (angular.isDefined(loadTimer)) {
                                $timeout.cancel(loadTimer);
                            }
                        });


                        /** Refreshes the list of satellite images from the service registry */
                        scope.refreshServiceRegistry = function () {
                            var mapState = JSON.parse($window.localStorage.getItem('mapState-storage')) ? JSON.parse($window.localStorage.getItem('mapState-storage')) : {};
                            var wkt = mapState['wktextent'];

                            $rootScope.mapWeatherLayers = layerGroup; // add group-layer to rootscope so it can be enabled/disabled


                            SatelliteService.getNasaServices(wkt).success(function (services, status) {

                                // Update the selected status from localstorage
                                if (status == 204) {
                                    $rootScope.SatelliteServicesStatus = 'false';
                                    $window.localStorage[SatelliteService.serviceID()] = 'false';
                                    while($rootScope.mapWeatherLayers.getLayers().getArray().length > 0) {
                                        $rootScope.mapWeatherLayers.getLayers().getArray().pop().setVisible(false);
                                    }
                                    // TODO: Need to store visibility state for each satellite instance...
                                }

                                if (status == 200) {
                                    $rootScope.SatelliteServicesStatus = 'true';
                                    $window.localStorage[SatelliteService.serviceID()] = 'true';
                                    angular.forEach(services, function (service) {
                                        var shouldAddService = true;
                                        if ($rootScope.mapWeatherLayers.getLayers().getArray().length > 0) {
                                            angular.forEach($rootScope.mapWeatherLayers.getLayers().getArray(), function (existingServices) {
                                                if (existingServices.get('id') == service.instanceId) {
                                                    //$log.debug("Already Found " + service.name + " satellite in local list - move on");
                                                    shouldAddService = false;
                                                }
                                            });
                                        }

                                        if (shouldAddService) {
                                            $log.debug("### Adding satellite instance " + service.name);
                                            // add to localstorage, false as default


                                            for (var i = 0; i < 1; i++) { // only yesterday. Extend here if want to display more older layers
                                                var instanceLayer = SatelliteService.createTileLayerFromService(service, i);
                                                $rootScope.mapWeatherLayers.getLayers().getArray().push(instanceLayer);
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
                                $log.error("Error retrieving NASA service " + error);
                                layerGroup.setVisible(false);
                            });

                        };

                        // update the map when a user pan-move ends.
                        map.on('moveend', scope.mapChanged);

                    });
                }
            };
        }]);


