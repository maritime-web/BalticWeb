
angular.module('maritimeweb.app')

    .controller("AppController", ['$scope', '$http', '$window', '$timeout', 'Auth', 'MapService', 'VesselService', 'NwNmService',
        function ($scope, $http, $window, $timeout, Auth, MapService, VesselService, NwNmService) {
            var loadTimerService = false;
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
            $scope.mapState = {};
            $scope.mapBackgroundLayers = MapService.createStdBgLayerGroup();
            $scope.mapWeatherLayers = MapService.createStdWeatherLayerGroup();
            $scope.mapMiscLayers = MapService.createStdMiscLayerGroup();
            //$scope.mapTrafficLayers = ""; // is set in the ais-vessel-layer

            // Alerts
            $scope.alerts = [
                {type: 'success', msg: 'Welcome to MaritimeWeb', timeout: 3000}
            ];

            /** Closes the alert at the given index */
            $scope.closeAlert = function (index) {
                $scope.alerts.splice(index, 1);
            };


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
            $scope.nwNmMessages = [];

            /** Reloads the NW-NM services **/
            $scope.refreshNwNmServices = function () {

                $scope.nwNmServices.length = 0;

                NwNmService.getNwNmServices($scope.mapState['wktextent'])
                    .success(function (services) {
                        // Update the selected status from localstorage
                        angular.forEach(services, function (service) {
                            $scope.nwNmServices.push(service);
                            service.selected = $window.localStorage[service.instanceId] == 'true';
                        })
                    })
                    .error(function(error){
                    console.error("Error getting NW NM service. Reason=" + error);
                })
            };

            var stopWatching = $scope.$watch("mapState['wktextent']", function (newValue) {
                if (angular.isDefined(newValue)) {

                    if (loadTimerService) {
                        $timeout.cancel(loadTimerService);
                    }
                    loadTimerService = $timeout(function () {
                            //console.log("load timer start");
                            $scope.refreshNwNmServices();
                        }, 5000);
                }
            });


            /** Update the selected status of the service **/
            $scope.nwNmSelected = function(service) {
                $window.localStorage[service.instanceId] = service.selected;
            };


            /** Toggle the selected status of the layer **/
            $scope.toggleLayer = function(layer) {
                (layer.getVisible() == true) ? layer.setVisible(false) : layer.setVisible(true); // toggle layer visibility
                if(layer.getVisible()){
                    $scope.alerts.push({
                        msg: 'Activating ' + layer.get('title') + ' layer',
                        type: 'info',
                        timeout: 3000
                    });
                }
            };

            /** Toggle the selected status of the service **/
            $scope.toggleService = function(service) {
                service.selected  = (service.selected == true) ? false : true; // toggle layer visibility
                if(service.selected){
                    $scope.alerts.push({
                        msg: 'Activating ' + service.name + ' layer',
                        type: 'info',
                        timeout: 3000
                    });
                }
            };

            /** Toggle the selected status of the service **/
            $scope.switchBaseMap = function(basemap) {
                //console.log('Switching BaseMap');
                // disable every basemaps
                angular.forEach($scope.mapBackgroundLayers.getLayers().getArray(), function(value){
                   // console.log("disabling " + value.get('title'));
                    value.setVisible(false)
                });
                basemap.setVisible(true);// activate selected basemap

                $scope.alerts.push({
                    msg: 'Activating map ' + basemap.get('title') ,
                    type: 'info',
                    timeout: 3000
                });
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

        }]);
