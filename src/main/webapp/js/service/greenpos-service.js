(function() {
    "use strict";

    var serviceModule = angular.module('embryo.greenposService', [ 'embryo.storageServices' ]);

    serviceModule.factory('GreenposService', function ($rootScope, $http, SessionStorageService, LocalStorageService) {
        var latestGreenposKey = function(maritimeId) {
            return 'latestgreenpos_' + maritimeId;
        };
        var nextNumberKey = function (maritimeId, recipient) {
            return 'embryo_nextnumber_' + maritimeId + "_" + recipient;
        };


        var reportsUrl = embryo.baseUrl + 'rest/greenpos';
        var findReportsUrl = reportsUrl + '/list/';

        return {
            getLatestReport : function(mmsi, callback) {
                var remoteCall = function(onSuccess) {
                    var url = embryo.baseUrl + 'rest/greenpos/latest/' + mmsi;
                    $http.get(url).success(onSuccess).error(function () {
                        callback(null);
                    });
                };
                SessionStorageService.getItem(latestGreenposKey(mmsi), callback, remoteCall);
            },
            getLatest : function(callback) {
                $http.get(reportsUrl + "/latest/").success(callback);
            },
            get : function(id, callback) {
                var url = reportsUrl + "/" + id;
                $http.get(url).success(callback);
            },
            findReports : function(params, callback) {
                var url = findReportsUrl;

                if (params && Object.keys(params).length > 0) {
                    url = url + "?";
                }

                for (key in params) {
                    url = url + key + "=" + params[key] + "&";
                }

                if (url.charAt(url.length - 1) === '&') {
                    url = url.substring(0, url.length - 1);
                }

                $http.get(url).success(callback);
            },
            save : function(greenpos, deactivateRoute, inclWps, callback, error) {
                var request = {
                        includeActiveRoute : inclWps,
                        activeRoute : {
                            routeId: deactivateRoute.value ? deactivateRoute.routeId : null,
                            active: deactivateRoute.value ? false : null
                        },
                        report : greenpos
                };
                $http.post(embryo.baseUrl + 'rest/greenpos/save', request).success(function(email) {
                    SessionStorageService.removeItem(latestGreenposKey(greenpos.mmsi));

                    LocalStorageService.setItem(nextNumberKey(greenpos.mmsi, greenpos.recipient), {
                        number: greenpos.number,
                        ts: new Date().getTime()
                    });

                    callback(email);
                }).error(function(data, status, headers, config) {
                    error(embryo.ErrorService.extractError(data, status, config));
                });
            },
            getPeriod : function(dateLong) {
                var date = new Date(dateLong);
                if (date.getUTCHours() >= 0 && date.getUTCHours() < 6) {
                    return {
                        from : Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), 0, 0),
                        to: Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), 6, 0)
                    };
                } else if (date.getUTCHours() >= 6 && date.getUTCHours() < 12) {
                    return {
                        from : Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), 6, 0),
                        to: Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), 12, 0)
                    };
                }
                if (date.getUTCHours() >= 12 && date.getUTCHours() < 18) {
                    return {
                        from : Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), 12, 0),
                        to: Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), 18, 0)
                    };
                }
                if (date.getUTCHours() >= 18 && date.getUTCHours() <= 23) {
                    return {
                        from : Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), 18, 0),
                        to: Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate() + 1, 0, 0)
                    };
                }
            },
            defaultReportType: function (greenpos) {
                if (!greenpos || !greenpos.ts) {
                    return "SP";
                }
                if (greenpos.type === 'FR') {
                    return "SP";
                }
                // Allow for reports to be performed 15 minutes before reporting
                // hour.
                // if last report performed more than 15 minutes before reporting
                // period then perform new report
                //if (greenpos.ts < (period.from - 900000) && now < (period.from + 1800000)) {
                //    return "PR";
                //}
                // if last report not performed more than ½ later than reporting
                // hour, then highlight.
                //if (greenpos.ts < (period.from - 900000) && now >= (period.from + 1800000)) {
                //    return "PR";
                //}
                return (greenpos.type === 'SP' || greenpos.type === 'PR' || greenpos.type === 'DR') ? "PR" : "SP";
            },
            nextReportNumber: function (mmsi, recipient, reportType, callback) {
                if (reportType == "SP") {
                    callback({
                        number: 1,
                        uncertainty: false
                    });
                } else {
                    LocalStorageService.getItem(nextNumberKey(mmsi, recipient), function (reportNumber) {
                        if (!reportNumber) {
                            callback({
                                number: 1,
                                uncertainty: true
                            });
                        } else {
                            callback({
                                number: reportNumber.number + 1,
                                uncertainty: new Date().getTime() - reportNumber.ts >= 7 * 60 * 60 * 1000
                            });
                        }
                    })
                }
            }
        };
    });

    embryo.greenpos = {};
    serviceModule.run(function(GreenposService) {
        embryo.greenpos.service = GreenposService;
    });

}());
