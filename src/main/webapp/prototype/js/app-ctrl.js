
angular.module('maritimeweb.app')

    .controller("AppController", ['$scope', '$http', '$window', '$timeout', 'Auth', 'MapService', 'VesselService', 'NwNmService',
        function ($scope, $http, $window, $timeout, Auth, MapService, VesselService, NwNmService) {

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


            // Alerts
            $scope.alerts = [
                {type: 'success', msg: 'Welcome to MaritimeWeb', timeout: 2000}
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
                NwNmService.getNwNmServices()
                    .success(function (services) {
                        // Update the selected status from localstorage
                        angular.forEach(services, function (service) {
                            $scope.nwNmServices.push(service);
                            service.selected = $window.localStorage[service.instanceId] == 'true';
                        })
                    });
            };
            $scope.refreshNwNmServices();


            /** Update the selected status of the service **/
            $scope.nwNmSelected = function(service) {
                $window.localStorage[service.instanceId] = service.selected;
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
