angular.module('maritimeweb.app')

    .controller("MCSRController", [
        '$scope', '$http', '$window', '$timeout', 'Auth', 'MapService',
        'VesselService', 'NwNmService', 'SatelliteService', 'ServiceRegistryService', 'growl', '$uibModal', '$log', '$interval', '$rootScope',
        function ($scope, $http, $window, $timeout, Auth, MapService, VesselService, NwNmService, SatelliteService, ServiceRegistryService, growl, $uibModal, $log, $interval, $rootScope) {

            $rootScope.showgraphSidebar = false; // rough disabling of the sidebar
            $scope.highlightedInstance = {};


            $scope.mcStylePurple = new ol.style.Style({
                stroke: new ol.style.Stroke({
                    color: 'rgba(180, 0, 180, 0.5)',
                    width: 1
                }),
                fill: new ol.style.Fill({
                    color: 'rgba(180, 0, 180, 0.40)'
                })
            });
            $scope.greenServiceStyle = new ol.style.Style({
                stroke: new ol.style.Stroke({
                    color: 'rgba(0, 255, 10, 0.8)',
                    width: 3
                }),
                fill: new ol.style.Fill({
                    color: 'rgba(5, 200, 10, 0.05)'
                })
            });

            $scope.highlightServiceRed = new ol.style.Style({
                stroke: new ol.style.Stroke({
                    color: 'rgba(255, 0, 10, 0.5)',
                    width: 1
                }),
                fill: new ol.style.Fill({
                    color: 'rgba(255, 0, 10, 0.10)'
                })
            });
            /***************************/
            /** MaritimeCloud Layers      **/
            /***************************/
            $scope.createMCLayerGroup = function () {

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
                    style: [$scope.greenServiceStyle]
                });

                var serviceAvailableLayer = new ol.layer.Vector({
                    id: 'serviceavailboundary',
                    title: 'Service Available - NO GO AREA',
                    name: 'Service Available - NO GO AREA',
                    zIndex: 11,
                    source: new ol.source.Vector({
                        features: new ol.Collection(),
                        wrapX: false
                    }),
                    style: [$scope.greenServiceStyle]
                });

                var duration = 3000;
                function flash(feature) {
                    var start = new Date().getTime();
                    var listenerKey;

                    function animate(event) {
                        var vectorContext = event.vectorContext;
                        var frameState = event.frameState;
                        var flashGeom = feature.getGeometry().clone();
                        var elapsed = frameState.time - start;
                        var elapsedRatio = elapsed / duration;
                        // radius will be 5 at start and 30 at end.
                        var radius = ol.easing.easeOut(elapsedRatio) * 25 + 5;
                        var opacity = ol.easing.easeOut(1 - elapsedRatio);

                        var style = new ol.style.Style({
                            image: new ol.style.Circle({
                                radius: radius,
                                snapToPixel: false,
                                stroke: new ol.style.Stroke({
                                    color: 'rgba(255, 0, 0, ' + opacity + ')',
                                    width: 0.25 + opacity
                                })
                            })
                        });

                        vectorContext.setStyle(style);
                        vectorContext.drawGeometry(flashGeom);
                        if (elapsed > duration) {
                            ol.Observable.unByKey(listenerKey);
                            return;
                        }
                        // tell OpenLayers to continue postcompose animation
                        map.render();
                    }
                    listenerKey = map.on('postcompose', animate);
                }

                serviceAvailableLayer.getSource().on('addfeature', function(e) {
                    flash(e.feature);
                });


                serviceAvailableLayer.setZIndex(12);
                serviceAvailableLayer.setVisible(true);
                serviceAvailableLayer.getSource().clear();


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
                    layers: [boundaryLayer, serviceAvailableLayer]
                });
                mcSRGroupLayer.setZIndex(11);
                mcSRGroupLayer.setVisible(true);

                return mcSRGroupLayer;
            }


            // Cancel any pending NW-NN queries
            var loadTimerService = undefined;
            $scope.$on("$destroy", function () {
                if (loadTimerService) {
                    $timeout.cancel(loadTimerService);
                }
            });


            $scope.loggedIn = Auth.loggedIn;

            /** Logs the user in via Keycloak **/
            $scope.login = function () {
                //TODO sample Stena Danica 265177000. You can change this to anything you want, or even better login
            };

            /** Logs the user out via Keycloak **/
            $scope.logout = function () {
                Auth.authz.logout();
                $window.localStorage.setItem('mmsi', 0);
            };

            /** Returns the user name ,**/
            $scope.userName = function () {
                if (Auth.authz.idTokenParsed) {
                    return Auth.authz.idTokenParsed.name
                        || Auth.authz.idTokenParsed.preferred_username;
                }
                return undefined;
            };

            /** Enters the Keycloak account management **/
            $scope.accountManagement = function () {
                Auth.authz.accountManagement();
            };

            // Map state and layers
            $scope.mapState = JSON.parse($window.localStorage.getItem('mapState-storage')) ? JSON.parse($window.localStorage.getItem('mapState-storage')) : {};

            $scope.mapMCLayers =  $scope.createMCLayerGroup();
            $scope.mapBackgroundLayers = MapService.createStdBgLayerGroup();

            // $scope.mapNoGoLayer =  MapService.createNoGoLayerGroup(); // is set in the no-go-layer
            //$scope.mcServiceRegistryInstances = ServiceRegistryService.getServiceInstances('POLYGON((9.268411718750002%2053.89831670389188%2C9.268411718750002%2057.58991390302003%2C18.392557226562502%2057.58991390302003%2C18.392557226562502%2053.89831670389188%2C9.268411718750002%2053.89831670389188))');
            $scope.mcServiceRegistryInstances = [];

            $scope.clearServiceRegistry = function () {
                var layersInGroup = $scope.mapMCLayers.getLayers().getArray();
                for (var i = 0, l; i < layersInGroup.length; i++) {
                    l = layersInGroup[i];
                    l.getSource().clear();
                }
                for (var j = $scope.mcServiceRegistryInstances.length - 1; j >= 0; j--) {
                    $scope.mcServiceRegistryInstances.splice(j, 1);

                }
                $scope.highlightedInstance = {};
            };


            $scope.highlightInstance = function (instance) {
                $scope.highlightedInstance = {};
                $scope.highlightedInstance.id = instance.id;
                $scope.highlightedInstance.description = instance.description;
                $scope.highlightedInstance.name = instance.name;
                $scope.highlightedInstance.version = instance.version;
                $scope.highlightedInstance.instanceId = instance.instanceId;

                var features = $scope.mapMCLayers.getLayers().getArray()[0].getSource().getFeatures();
                $log.info("Features found " + features.length);

                angular.forEach(features, function(feature) {
                    $log.info("id= " + feature.getId());
                    feature.setStyle($scope.greenServiceStyle);
                });
                $log.info();

                var feature = $scope.mapMCLayers.getLayers().getArray()[0].getSource().getFeatureById(instance.instanceId);

                $log.info("Feature found by id " + feature.getId());

                feature.setStyle($scope.highlightServiceRed);
                $scope.mapMCLayers.getLayers().getArray()[0].getSource().addFeature(feature);

            };

            $scope.isThereAnyServiceRegistry = function () {
                $log.info("isThereAnyServiceRegistry");
                $scope.highlightedInstance = {};

                ServiceRegistryService.getServiceInstances().success(function (services, status) {

                    $scope.mcServiceRegistryInstances.length = 0;
                    // Update the selected status from localstorage
                    var instanceIds = [];
                    if (status == 204) {
                        $scope.mcServiceRegistryInstancesStatus = 'false';
                        $scope.mcServiceRegistryInstancesMessages = [];
                    }
                    if (status == 200) {
                        $scope.mcServiceRegistryInstancesStatus = 'true';

                        angular.forEach(services, function (service) {
                            $scope.mcServiceRegistryInstances.push(service);

                            if (service.boundary) {

                                try {
                                    // $log.info("Name: " + service.name + " Boundary: " + service.boundary + " ");
                                    var wktString = service.boundary.split('\+').join('').replace(/\s+\(\(/, '\(\('); // remove + and whitespaces from the wkt...
                                    // $log.info("wktString=" + wktString);

                                    var olFeature = MapService.wktToOlFeature(wktString);
                                    olFeature.id = service.instanceId;
                                    olFeature.setId(service.instanceId);
                                    olFeature.name = service.name;
                                    olFeature.version = service.version;
                                    olFeature.instanceId = service.instanceId;
                                    olFeature.description = service.description;
                                    $scope.mapMCLayers.getLayers().getArray()[0].getSource().addFeature(olFeature);

                                } catch (error) {
                                    $log.error("Error displaying service. " + "Name: " + service.name + " Boundary: " + service.boundary);
                                }
                                $log.info(service);
                            }

                        }, function (error) {
                            $rootScope.loading = false;
                            $log.error(error);
                            if (error.data.message) {
                                growl.error(error.data.message);
                            }
                        });
                    }
                });
            };




            /**
             * store all features in local storage, on a server or right now. Throw them on the root scope.
             */
            $scope.redirectToFrontpage = function () {
                $scope.loading = true;
                $log.debug("redirect to Frontpage");
                var redirect = function () {
                    //$rootScope.showgraphSidebar = true; // rough enabling of the sidebar

                    $scope.loading = false;
                    $window.location.href = '#';
                };
                $timeout(redirect, 100);
            };

            // reload services on startup
            if($scope.loggedIn){
                $scope.isThereAnyServiceRegistry();
            }

        }]);

