angular.module('maritimeweb').config(['$locationProvider', '$routeProvider',
    function config($locationProvider, $routeProvider) {
        //$locationProvider.hashPrefix('!');

        $routeProvider.
        when('/disclaimer', {
            templateUrl: 'partials/disclaimer.html'
        }).
        when('/about', {
            templateUrl: 'partials/about-page-wrapper.html'
        }).
        when('/vessel/:mmsi', {
            controller: 'VesselDetailsCtrl',
            templateUrl: 'vessel/vessel-details.html'
        }).
        when('/vessel/:mmsi/route', {
            templateUrl: 'route/route-load.html'
        }).
        when('/route-demo', {
            templateUrl: 'route/route-load.html'
        }).
        otherwise({
            //controller: 'AppController',
            templateUrl: 'partials/map.html'
        });
    }
]);