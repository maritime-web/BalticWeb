$(function() {
    "use strict";

    angular.module('embryo.administration.log', [ 'embryo.logService', 'embryo.authentication', 'embryo.datepicker' ]);

    embryo.LogCtrl = function($scope, $timeout, LogService) {
        $scope.services = [];

        $scope.searchRequest = {
            service : "",
            count : 5,
            from : new Date().getTime() - 1000 * 60 * 60 * 48
        };

        function updateEntries(data) {
            data.sort(function(a, b) {
                return b.date - a.date;
            });

            var servicesTemp = [];
            for ( var i in data) {
                if (servicesTemp.indexOf(data[i].service) < 0) {
                    servicesTemp.push(data[i].service);
                }
            }
            servicesTemp.sort();

            var logs = [];
            for ( var k in servicesTemp) {
                logs.push(servicesTemp[k]);
                for ( var i = 0; i < data.length; i++) {
                    if (data[i].service == servicesTemp[k]) {
                        logs.push(data[i]);
                    }
                }
            }
            $scope.logs = logs;
        }

        $scope.formatTime = function(date) {
            return formatTime(date);
        };

        function refreshLogEntries() {
            LogService.search($scope.searchRequest, updateEntries, function(errorMsgs) {
                $scope.alertMessages = errorMsgs;
            });
        }

        LogService.services(function(services) {
            $scope.services = [ "" ].concat(services);
        }, function(errorMsgs) {
            $scope.alertMessages = errorMsgs;
        });

        $timeout(refreshLogEntries, 60 * 1000);
        refreshLogEntries();

        $scope.search = function() {
            refreshLogEntries();
        };
    };
}());