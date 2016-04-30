
angular.module('maritimeweb.app')

    .controller("AppController", ['$scope', '$http', '$timeout', 'Auth', 'MapService', 'NwNmService',
        function ($scope, $http, $timeout, Auth, MapService, NwNmService) {

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


        // NW-NM messages
        $scope.nwNmMessages = [];

        // Vessels
        $scope.vessels = [];

        // Alerts 
        $scope.alerts = [
            {type: 'success', msg: 'Welcome to MaritimeWeb', timeout: 2000}
        ];

        
        /** Closes the alert at the given index */
        $scope.closeAlert = function (index) {
            $scope.alerts.splice(index, 1);
        };


        /** Show the details of the message */
        $scope.showNwNmDetails = function (message) {
            NwNmService.showMessageInfo(message);
        };
    }]);
