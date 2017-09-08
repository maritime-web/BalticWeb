/**
 * Created by andreas on 9/6/17.
 */
angular.module('maritimeweb.route')
/*******************************************************************
 * Controller that handles uploading an RTZ route for a vessel
 *  Its
 *******************************************************************/
    .controller('WeatherCtrl', ['$scope', '$window', 'growl', 'timeAgo', '$filter',  '$timeout', '$rootScope',
        function ($scope, $window, growl, timeAgo, $filter, $timeout, $rootScope) {
            'use strict';

            console.log("WeatherCtrl is online");
            $rootScope.showgraphSidebar = false; // rough disbling of the sidebar
            $scope.mapState = JSON.parse($window.localStorage.getItem('mapState-storage')) ? JSON.parse($window.localStorage.getItem('mapState-storage')) : {}; // load the mnapstate


        }]);
