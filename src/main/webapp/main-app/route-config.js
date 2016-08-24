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
            controller: 'VesselDialogCtrl',
            templateUrl: 'vessel/vessel-details-dialog.html'
        }).
        otherwise({
            templateUrl: 'partials/map.html'
        });
    }
]);