angular.module('maritimeweb.app')

    .controller("AppController", [
        '$scope', '$http', '$window', '$timeout', 'Auth', 'MapService',
        'VesselService', 'NwNmService', 'SatelliteService', 'growl', '$uibModal', '$log',
    function ($scope, $http, $window, $timeout, Auth, MapService, VesselService, NwNmService, SatelliteService, growl, $uibModal, $log) {

        // Cancel any pending NW-NN queries
        var loadTimerService = undefined;
        $scope.$on("$destroy", function() {
            if (loadTimerService) {
                $timeout.cancel(loadTimerService);
            }
        });


        $scope.welcomeToBalticWebModal = function (size) {
            $uibModal.open({
                animation: 'true',
                templateUrl: 'partials/welcome.html',
                controller: 'AcceptTermsCtrl',
                size: size
            })
        };

        $scope.loggedIn = Auth.loggedIn;

        /** Logs the user in via Keycloak **/
        $scope.login = function () {
            Auth.authz.login();
        };

        /** Logs the user out via Keycloak **/
        $scope.logout = function () {
            Auth.authz.logout();
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
        $scope.mapBackgroundLayers = MapService.createStdBgLayerGroup();
        //$scope.mapWeatherLayers = MapService.createStdWeatherLayerGroup();
        $scope.mapMiscLayers = MapService.createStdMiscLayerGroup();
        //$scope.mapTrafficLayers = ""; // is set in the ais-vessel-layer


        var accepted_terms = $window.localStorage.getItem('terms_accepted_ttl');
        $log.info("accepted_terms ttl = " + accepted_terms);
        var now = new Date();

        if (accepted_terms == null || (new Date(accepted_terms).getTime() < now )) {
            $scope.welcomeToBalticWebModal('lg');
        } else {
            growl.info("Welcome back");
        }



        /**************************************/
        /** Vessel sidebar functionality      **/
        /**************************************/

        // Vessels
        $scope.vessels = [];

        /** Returns the icon to use for the given vessel **/
        $scope.iconForVessel = function (vo) {
            return '/img/' + VesselService.imageAndTypeTextForVessel(vo).name;
        };

        /** Returns the lat-lon attributes of the vessel */
        $scope.toLonLat = function (vessel) {
            return {lon: vessel.x, lat: vessel.y};
        };

        /**************************************/
        /** NW-NM sidebar functionality      **/
        /**************************************/

        $scope.nwNmServices = [];
        $scope.satelliteInstances =  [];
        $scope.nwNmMessages = [];
        $scope.nwNmLanguage = 'en';
        $scope.nwNmType = {
            NW: $window.localStorage['nwNmShowNw'] != 'false',
            NM: $window.localStorage['nwNmShowNm'] == 'true'
        };

        /**
         * Computes the current NW-NM service boundary
         */
        $scope.currentNwNmBoundary = function () {
            return $scope.mapState['wktextent'];
        };


        /** Schedules reload of the NW-NM services **/
        $scope.refreshNwNmServices = function () {
            if (loadTimerService) {
                $timeout.cancel(loadTimerService);
            }
            loadTimerService = $timeout(function () {
                $scope.loadServicesFromRegistry();
            }, 1000);
        };

        // Refresh the service list every time the NW-NM boundary changes
        $scope.$watch($scope.currentNwNmBoundary, $scope.refreshNwNmServices);


        /** Loads the  services **/
        $scope.loadServicesFromRegistry = function () {
            //$log.debug("     ...loadServicesFromRegistry");
            var wkt = $scope.currentNwNmBoundary();

            NwNmService.getNwNmServices(wkt)
                .success(function (services, status) {
                    //$log.debug("NVNM Status " + status);
                    $scope.nwNmServices.length = 0;

                    // Update the selected status from localstorage
                    var instanceIds = [];
                    if(status==204){
                        $scope.nwNmServicesStatus = 'false';
                        $window.localStorage[NwNmService.serviceID()] = 'false';
                        $scope.nwNmMessages = [];

                    }

                    if(status==200){
                        $scope.nwNmServicesStatus = 'true';
                        $window.localStorage[NwNmService.serviceID()] = 'true';


                            angular.forEach(services, function (service) {
                            $scope.nwNmServices.push(service);
                            service.selected = $window.localStorage[service.instanceId] == 'true';
                            if (service.selected) {
                                instanceIds.push(service.instanceId);
                            }
                        });

                        // Load messages for all the selected service instances
                        var mainType = null;
                        if ($scope.nwNmType.NW && !$scope.nwNmType.NM) {
                            mainType = 'NW';
                        } else if (!$scope.nwNmType.NW && $scope.nwNmType.NM) {
                            mainType = 'NM';
                        }
                        if($window.localStorage[NwNmService.serviceID()]){
                            NwNmService
                                .getPublishedNwNm(instanceIds, $scope.nwNmLanguage, mainType, wkt)
                                .success(function (messages) {
                                    $scope.nwNmMessages = messages;
                                });
                        }
                    }
                })
                .error(function (error) {
                    // growl.error("Error getting NW NM service. Reason=" + error);
                    $window.localStorage[NwNmService.serviceID()] = 'false';

                    $log.debug("Error getting NW NM service. Reason=" + error);
                })
        };


        /** Called when the NW-NM type selection has been changed **/
        $scope.nwNmTypeChanged = function () {
            $window.localStorage['nwNmShowNw'] = '' + $scope.nwNmType.NW;
            $window.localStorage['nwNmShowNm'] = '' + $scope.nwNmType.NM;
            $scope.loadServicesFromRegistry();
        };


        /** Update the selected status of the service **/
        $scope.nwNmSelected = function (service) {
            $window.localStorage[service.instanceId] = service.selected;
            $scope.loadServicesFromRegistry();
        };


        /** Show the details of the message */
        $scope.showNwNmDetails = function (message) {
            NwNmService.showMessageInfo(message);
        };


        /** Returns the area heading for the message with the given index */
        $scope.nwnmAreaHeading = function (index) {
            var msg = $scope.nwNmMessages[index];
            return NwNmService.getAreaHeading(msg);
        };


        /** Toggle the selected status of the layer **/
        $scope.toggleLayer = function (layer) {
            (layer.getVisible() == true) ? layer.setVisible(false) : layer.setVisible(true); // toggle layer visibility
            if (layer.getVisible()) {
                growl.info('Activating ' + layer.get('title') + ' layer');
            }
        };

        /** Toggle the selected status of the service **/
        $scope.toggleService = function (service) {
            service.selected = (service.selected != true); // toggle layer visibility
            if (service.selected) {
                growl.info('Activating ' + service.name + ' layer');
            }
        };

        /** Toggle the selected status of the service **/
        $scope.switchBaseMap = function (basemap) {
            angular.forEach($scope.mapBackgroundLayers.getLayers().getArray(), function (value) { // disable every basemaps
                // console.log("disabling " + value.get('title'));
                value.setVisible(false)
            });
            basemap.setVisible(true);// activate selected basemap
            growl.info('Activating map ' + basemap.get('title'));
        };

        /** Toggle the selected status of the service **/
        $scope.switchService = function (groupLayers, layerToBeActivated) {
            angular.forEach(groupLayers, function (layerToBeDisabled) { // disable every basemaps
                layerToBeDisabled.setVisible(false);
                //$log.debug(" ol disabling " + layerToBeDisabled.get('id'));
                $window.localStorage.setItem(layerToBeDisabled.get('id'), false );
            });

            layerToBeActivated.selected = (layerToBeActivated.selected != true); // toggle service visibility. if already active
            if (layerToBeActivated.selected) {
                layerToBeActivated.setVisible(true);// activate selected basemap
                growl.info('Activating map ' + layerToBeActivated.get('title'));
                $window.localStorage.setItem(layerToBeActivated.get('id'), true );
            }


        };

        $scope.showVesselDetails = function (vessel) {
            $log.info("mmsi" + vessel);
            //var vesselDetails = VesselService.details(vessel.mmsi);
            VesselService.showVesselInfoFromMMsi(vessel);
            //console.log("App Ctr received = vesselDetails" +JSON.stringify(vesselDetails));
            //growl.info("got vesseldetails " + JSON.stringify(vesselDetails));
            growl.info("Vessel details retrieved");

        };

    }]);

