angular.module('maritimeweb.serviceregistry')

    .controller("MCSRController", [
        '$scope', '$http', '$window', '$timeout', 'Auth', 'MapService',
        'VesselService', 'NwNmService', 'SatelliteService', 'ServiceRegistryService', 'growl', '$uibModal', '$log', '$interval', '$rootScope',
        function ($scope, $http, $window, $timeout, Auth, MapService, VesselService, NwNmService, SatelliteService, ServiceRegistryService, growl, $uibModal, $log, $interval, $rootScope) {
            // var olScope = $scope.getOpenlayersScope();

            $rootScope.showgraphSidebar = false; // rough disabling of the sidebar
            $scope.highlightedInstance = {};
            $rootScope.highlightedInstances = [];



            $scope.loggedIn = Auth.loggedIn;

            /** Logs the user in via Keycloak **/
            $scope.login = function () {
                Auth.authz.login();
                //TODO sample Stena Danica 265177000. You can change this to anything you want, or even better take it from the login token.
                $window.localStorage.setItem('mmsi', 265177000);
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

            // $scope.mapMCLayers =  $scope.createMCLayerGroup();
            $scope.mapBackgroundLayers = MapService.createStdBgLayerGroup();

            // $scope.mapNoGoLayer =  MapService.createNoGoLayerGroup(); // is set in the no-go-layer
            //$scope.mcServiceRegistryInstances = ServiceRegistryService.getServiceInstances('POLYGON((9.268411718750002%2053.89831670389188%2C9.268411718750002%2057.58991390302003%2C18.392557226562502%2057.58991390302003%2C18.392557226562502%2053.89831670389188%2C9.268411718750002%2053.89831670389188))');
            $scope.mcServiceRegistryInstances = [];

            $scope.clearServiceRegistry = function () {
                var layersInGroup = $rootScope.mapMCLayers.getLayers().getArray();
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
                $scope.highlightedInstance.boundary = instance.boundary;
                $rootScope.highlightedInstancescoordinate = [];
                $rootScope.highlightedInstances = [];

                var features = $rootScope.mapMCLayers.getLayers().getArray()[0].getSource().getFeatures();
                $log.info("Features found " + features.length);

                angular.forEach(features, function(feature) {
                    feature.setStyle(ServiceRegistryService.greenServiceStyle);
                });

                var feature = $rootScope.mapMCLayers.getLayers().getArray()[0].getSource().getFeatureById(instance.instanceId);
                if(feature){
                    $log.info("Feature found by id " + feature.getId());

                    feature.setStyle(ServiceRegistryService.highlightServiceRed);
                    $rootScope.mapMCLayers.getLayers().getArray()[0].getSource().addFeature(feature);
                }else{
                    $log.error("Service instance has no geom");
                }
            };

            $scope.isHighlighted = function(id) {
                return $rootScope.highlightedInstances.indexOf(id) !== -1;
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
                                    $rootScope.mapMCLayers.getLayers().getArray()[0].getSource().addFeature(olFeature);

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

