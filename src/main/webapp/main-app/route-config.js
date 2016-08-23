angular.module('maritimeweb').config(['$locationProvider', '$routeProvider',
    function config($locationProvider, $routeProvider) {
        //$locationProvider.hashPrefix('!');

        $routeProvider.
        when('/disclaimer', {
            templateUrl: 'partials/disclaimer.html'
        }).
        when('/vessel/:mmsi', {
            templateUrl: 'vessel/vessel-details-dialog.html'
        }).
        otherwise({
            templateUrl: 'partials/map.html'
        });
    }
]);