/*
 * Dependencies:
 * 
 * aisview.js
 * aisviewUI.js
 * position.js
 * ....
 */

(function() {
    "use strict";

    embryo.angular = angular.module('embryo', [ 'embryo.shipInformation', 'embryo.voyagePlan', 'embryo.routeEdit',
            'embryo.routeUpload', 'embryo.greenpos', 'embryo.reportComp' ]);
    // , 'ui.bootstrap'

    embryo.angular.config([ '$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
        // $locationProvider.html5Mode(true);

        $routeProvider.when('/ship', {
            templateUrl : 'partials/shipInformation.html',
            controller : embryo.ShipInformationCtrl
        }).when('/voyagePlan/:mmsi', {
            templateUrl : 'partials/voyagePlan.html',
            controller : embryo.VoyagePlanCtrl
        }).when('/voyagePlan/:mmsi/:voyage', {
            templateUrl : 'partials/voyagePlan.html',
            controller : embryo.VoyagePlanCtrl
        }).when('/routeEdit/:mmsi', {
            templateUrl : 'partials/routeEdit.html',
            controller : embryo.RouteEditCtrl
        }).when('/routeEdit/:mmsi/:routeId', {
            templateUrl : 'partials/routeEdit.html',
            controller : embryo.RouteEditCtrl
        }).when('/routeUpload/:mmsi', {
            templateUrl : 'partials/routeUpload.html'// ,
        // controller : embryo.GreenPosListCtrl
        }).when('/routeUpload/:mmsi/:voyageId', {
            templateUrl : 'partials/routeUpload.html'// ,
        // controller : embryo.GreenPosListCtrl
        }).when('/reportlist', {
            templateUrl : 'partials/greenposList.html',
            controller : embryo.GreenPosListCtrl
        }).when('/report', {
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
                    console.log(value);
                    // console.log(value.$dirty);
                    // console.log(value.$error.required);

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
