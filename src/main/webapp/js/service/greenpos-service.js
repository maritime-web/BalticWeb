(function() {
    "use strict";

    var serviceModule = angular.module('embryo.greenposService', [ 'embryo.storageServices' ]);

    serviceModule.factory('GreenposService', function($rootScope, $http, SessionStorageService) {
        var latestGreenposKey = function(maritimeId) {
            return 'latestgreenpos_' + maritimeId;
        };

        var reportsUrl = embryo.baseUrl + 'rest/greenpos';
        var findReportsUrl = reportsUrl + '/list/';

        return {
            getLatestReport : function(mmsi, callback) {
                var remoteCall = function(onSuccess) {
                    var url = embryo.baseUrl + 'rest/greenpos/latest/' + mmsi;
                    $http.get(url).success(onSuccess).error(function (data) {
                        callback(null);
                    });
                };
                SessionStorageService.getItem(latestGreenposKey(mmsi), callback, remoteCall);
            },
            getLatest : function(mmsi, callback) {
                $http.get(reportsUrl + "/latest/" + mmsi).success(callback);
            },
            get : function(id, callback) {
                var url = reportsUrl + "/" + id;
                $http.get(url).success(callback);
            },
            findReports : function(params, callback) {
                var key, url = findReportsUrl;

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
            save : function(greenpos, callback, error) {
                $http.post(embryo.baseUrl + 'rest/greenpos', greenpos).success(function() {
                    SessionStorageService.removeItem(latestGreenposKey(greenpos.mmsi));
                    callback();
                }).error(function(data, status, headers, config) {
                    error(embryo.ErrorService.extractError(data, status, config));
                });
            },
            getPeriod : function(dateLong) {
                var date = new Date(dateLong);
                if (date.getUTCHours() >= 0 && date.getUTCHours() < 6) {
                    return {
                        from : Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), 0, 0),
                        to : Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), 6, 0),
                    };
                } else if (date.getUTCHours() >= 6 && date.getUTCHours() < 12) {
                    return {
                        from : Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), 6, 0),
                        to : Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), 12, 0),
                    };
                }
                if (date.getUTCHours() >= 12 && date.getUTCHours() < 18) {
                    return {
                        from : Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), 12, 0),
                        to : Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), 18, 0),
                    };
                }
                if (date.getUTCHours() >= 18 && date.getUTCHours() <= 23) {
                    return {
                        from : Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), 18, 0),
                        to : Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate() + 1, 0, 0),
                    };
                }
            }

        };
    });

    embryo.greenpos = {};
    serviceModule.run(function(GreenposService) {
        embryo.greenpos.service = GreenposService;
    });

}());
