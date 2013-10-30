/*
 * Dependencies:
 * 
 * aisview.js
 * aisviewUI.js
 * route.js
 * ....
 */

(function() {
    "use strict";

    var voyageUrl = embryo.baseUrl + 'rest/voyage/', voyageTypeaheadUrl = embryo.baseUrl + 'rest/voyage/typeahead/', voyageShortUrl = embryo.baseUrl
            + 'rest/voyage/short/';

    var scheduleUrl = embryo.baseUrl + 'rest/schedule/';

    var voyageServiceModule = angular.module('embryo.voyageService', [ 'embryo.storageServices' ]);

    voyageServiceModule.factory('VoyageService', function($rootScope, $http, ShipService, SessionStorageService) {
        var currentPlan = 'voyagePlan_current';
        var activeVoyage = 'voyage_active';

        return {
            getYourActive : function(callback) {
                var voyageStr = sessionStorage.getItem(activeVoyage);
                if (!voyageStr) {
                    ShipService.getYourShip(function(yourShip) {
                        var remoteCall = function(onSuccess) {
                            $http.get(voyageUrl + 'active/' + yourShip.maritimeId).success(onSuccess);
                        };

                        SessionStorageService.getItem(activeVoyage, callback, remoteCall);
                    });
                } else {
                    callback(JSON.parse(voyageStr));
                }
            },
            getCurrentPlan : function(mmsi, callback) {
                var remoteCall = function(onSuccess) {
                    $http.get(voyageUrl + mmsi + '/current').success(onSuccess);
                };
                SessionStorageService.getItem(currentPlan, callback, remoteCall);
            },
            getSchedule : function(mmsi, callback) {
                var remoteCall = function(onSuccess) {
                    $http.get(scheduleUrl + 'overview/' + mmsi ).success(onSuccess);
                };
                SessionStorageService.getItem(currentPlan, callback, remoteCall);
            },
            getVoyages : function(mmsi, callback) {
                $http.get(voyageTypeaheadUrl + mmsi).success(callback);
            },
            getVoyagesShort : function(mmsi, callback) {
                function transformShort(voyagesShort) {
                    var index = 0;
                    var result = [];

                    for (index = 0; index < voyagesShort.length - 1; index++) {
                        var departure = voyagesShort[index];
                        var destination = voyagesShort[index + 1];
                        result.push({
                            maritimeId : departure.maritimeId, 
                            dep : departure.loc,
                            depEta : formatTime(departure.dep),
                            des : destination.loc,
                            desEta : formatTime(destination.arr)
                        });
                    }
                    if (voyagesShort.length > 0) {
                        result.push({
                            maritimeId : voyagesShort[voyagesShort.length - 1].maritimeId, 
                            dep : voyagesShort[voyagesShort.length - 1].loc,
                            depEta : formatTime(voyagesShort[voyagesShort.length - 1].arr)
                        });
                        
                    }
                    return result;
                }

                function transformCallback(voyagesShort) {
                    var voyages = transformShort(voyagesShort);
                    callback(voyages);
                }

                $http.get(voyageShortUrl + mmsi).success(transformCallback);
            },
            save : function(plan, callback) {
                $http.put(voyageUrl + 'savePlan', plan).success(function() {
                    SessionStorageService.removeItem(currentPlan);
                    callback();
                    $rootScope.$broadcast('yourshipDataUpdated');
                });
            },
            saveScedule : function(schedule, callback) {
                $http.put(scheduleUrl + 'save', schedule).success(function() {
                    SessionStorageService.removeItem(currentPlan);
                    callback();
                    $rootScope.$broadcast('yourshipDataUpdated');
                });
            }
        };
    });

}());
