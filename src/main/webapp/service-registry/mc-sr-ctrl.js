angular.module('maritimeweb.app')

    .controller("MCSRController", [
        '$scope', '$http', '$window', '$timeout', 'Auth', 'MapService',
        'VesselService', 'NwNmService', 'SatelliteService', 'ServiceRegistryService', 'growl', '$uibModal', '$log', '$interval', '$rootScope',
        function ($scope, $http, $window, $timeout, Auth, MapService, VesselService, NwNmService, SatelliteService, ServiceRegistryService, growl, $uibModal, $log, $interval, $rootScope) {

            $rootScope.showgraphSidebar = false; // rough disabling of the sidebar

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

            $scope.mapMCLayers = MapService.createMCLayerGroup();
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
            };


            $scope.highlightInstance = function (instance) {
                $log.info("instance name " + instance.name);
            };

            $scope.isThereAnyServiceRegistry = function () {
                $log.info("isThereAnyServiceRegistry");

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


            /**************************************/
            /** Vessel Traffic Service functionality      **/
            /**************************************/

            $scope.displayServiceInstance = function (size) {
                growl.info('Display Service Instance');
                $uibModal.open({
                    animation: 'true',
                    templateUrl: 'service-registry/one instance.html',
                    controller: 'ServiceRegistryInstanceCtrlNotCreated', // TODO: create this controller if needed
                    size: size
                })
            };


            // $scope.update = function() {
            //     $scope.item.size.code = $scope.selectedItem.code
            //     // use $scope.selectedItem.code and $scope.selectedItem.name here
            //     // for other stuff ...
            // }


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
            $scope.isThereAnyServiceRegistry();

        }]);

