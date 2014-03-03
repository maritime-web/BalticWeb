(function() {
    "use strict";

    embryo.angular = angular.module('embryo', [ 'embryo.shipInformation', 'embryo.voyagePlan', 'embryo.schedule' ,'embryo.routeEdit',
            'embryo.routeUpload', 'embryo.greenpos', 'embryo.reportControl' ]);
    // , 'ui.bootstrap'
    
    embryo.AppReportController = function($scope, $window){
        $scope.back = function(){
            $window.history.back();
        };
    };

    embryo.angular.config([ '$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
        // $locationProvider.html5Mode(true);

        $routeProvider.when('/vessel', {
            templateUrl : 'partials/vesselInformation.html',
            controller : embryo.VesselInformationCtrl
        }).when('/voyagePlan/:mmsi', {
            templateUrl : 'partials/schedule.html',
        }).when('/voyagePlan/:mmsi/:voyage', {
            templateUrl : 'partials/schedule.html',
        }).when('/routeEdit/:mmsi', {
            templateUrl : 'partials/routeEdit.html',
            controller : embryo.RouteEditCtrl
        }).when('/routeEdit/:mmsi/:routeId', {
            templateUrl : 'partials/routeEdit.html',
            controller : embryo.RouteEditCtrl
        }).when('/routeUpload/:mmsi', {
            templateUrl : 'partials/routeUpload.html'// ,
        }).when('/routeUpload/:mmsi/voyage/:voyageId', {
            templateUrl : 'partials/routeUpload.html'// ,
        }).when('/routeUpload/:mmsi/active', {
            templateUrl : 'partials/routeUpload.html'// ,
        }).when('/reportlist', {
            templateUrl : 'partials/greenposList.html',
            controller : embryo.GreenposListCtrl
        }).when('/report', {
            templateUrl : 'partials/greenposReport.html',
            controller : embryo.GreenPosCtrl
        }).when('/report/view/:id', {
            templateUrl : 'partials/greenposReport.html',
            controller : embryo.GreenPosCtrl
        }).otherwise({
            redirectTo : 'map.html'
        });

    } ]);

    embryo.angular.directive('msgRequired', function() {
        return {
            link : function(scope, element, attrs) {
                element.text('Value required');
                element.addClass('msg-invalid');

                attrs.$set('ngShow', attrs.msgRequired + '$error.required' && greenPosForm.gpPersons + '.$dirty');

                // watch the expression, and update the UI on change.
                scope.$watch('greenPosForm.gpPersons', function(value, oldValue) {

                    // if(value.$dirty && value.$error.required){
                    // element.show();
                    // }else{
                    // element.hide();
                    // }
                });
            }
        };

    });
}());
