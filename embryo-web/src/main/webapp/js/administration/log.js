$(function() {
    "use strict";

    angular.module('embryo.log', [ 'embryo.logService', 'embryo.authentication', 'embryo.datepicker' ]);

    embryo.LogCtrl = function($scope, $timeout, LogService) {
        $scope.services = [];

        $scope.searchRequest = {
            service : "",
            count : 5,
            from : new Date().getTime() - 1000 * 60 * 60 * 48
        };

        function updateEntries(data) {

            console.log(data);

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

        embryo.authenticated(function() {
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
        });
    };
}());

// embryo.authenticated(function() {

function refreshLogEntries() {
    $.ajax({
        url : embryo.baseUrl + "rest/log/list",
        data : {},
        success : function(data) {
            data.sort(function(a, b) {
                return b.date - a.date;
            });

            var services = [];

            for ( var i in data) {
                if (services.indexOf(data[i].service) < 0)
                    services.push(data[i].service);
            }

            services.sort();

            var html = "<tr><th>Time (UTC)</th><th>Status</th><th>Message</th></tr>";

            $.each(services, function(i, service) {
                html += "<tr><td colspan=3><h5>" + service + "</h5></td></tr>";

                var count = 0;

                $.each(data, function(k, v) {
                    if (v.service == service && count < 5) {
                        var status = v.status;

                        switch (status) {
                        case "OK":
                            status = "<span class='label label-success'>" + status + "</span>";
                            break;
                        case "ERROR":
                            status = "<span class='label label-important'>" + status + "</span>";
                            break;
                        }

                        html += "<tr><td>" + formatTime(v.date) + "</td><td>" + status + "</td><td>" + v.message
                                + "</td></tr>";

                        if (v.stackTrace) {
                            html += "<tr><td colspan=3><pre>" + v.stackTrace + "</pre></td></tr>";
                        }

                        count++;
                    }
                })
            })

            $("#latestLogEntries").html(html);
        },
        error : function(error) {
        }
    });
}

// setInterval(refreshLogEntries, 60 * 1000);
//
// refreshLogEntries();

// })
