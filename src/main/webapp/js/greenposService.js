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

        var findReportsUrl = embryo.baseUrl + 'rest/greenpos/list/';

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
            findReports : function(params, callback){
                $http.get(findReportsUrl, {
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
