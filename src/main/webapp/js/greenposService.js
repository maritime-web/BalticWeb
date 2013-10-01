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

    var serviceModule = angular.module('embryo.greenposService', [ 'embryo.storageServices' ]);

    serviceModule.factory('GreenposService', function($rootScope, $http, SessionStorageService) {
        var latestGreenposKey = function(maritimeId) {
            return 'latestgreenpos_' + maritimeId;
        };

        var reportsUrl = embryo.baseUrl + 'rest/greenpos';
        var findReportsUrl = reportsUrl + '/list/';

        return {
            getLatestReport : function(shipMaritimeId, callback) {
                var remoteCall = function(onSuccess) {
                    var url = embryo.baseUrl + 'rest/greenpos/latest/' + shipMaritimeId;
                    $http.get(url, {
                        responseType : 'json'
                    }).success(onSuccess);
                };

                // last report maintained in SessionStorage as
                // 'latestgreenpos_shipMaritimeId' -> report
                SessionStorageService.getItem(latestGreenposKey(shipMaritimeId), callback, remoteCall);
            },
            get : function(id, callback) {
                var url = reportsUrl + "/" + id;
                $http.get(url, {
                    responseType : 'json'
                }).success(callback);
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

                $http.get(url, {
                    responseType : 'json'
                }).success(callback);
            },
            save : function(greenpos, callback) {
                $http.post(embryo.baseUrl + 'rest/greenpos', greenpos, {
                    responseType : 'json'
                }).success(function() {
                    SessionStorageService.removeItem(latestGreenposKey(greenpos.shipMaritimeId));
                    callback();
                    $rootScope.$broadcast('yourshipDataUpdated');
                });
            }
        };
    });
}());
