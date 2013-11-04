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

    var scheduleUrl = embryo.baseUrl + 'rest/schedule/';

    var scheduleServiceModule = angular.module('embryo.scheduleService', [ 'embryo.storageServices' ]);

    scheduleServiceModule.factory('ScheduleService', function($http, ShipService, SessionStorageService) {
        var currentSchedule = 'schedule_current';
        var activeVoyage = 'voyage_active';

        return {
            getYourActive : function(mmsi, callback) {
                var voyageStr = sessionStorage.getItem(activeVoyage+mmsi);
                if (!voyageStr) {
                    ShipService.getYourShip(function(yourShip) {
                        var remoteCall = function(onSuccess) {
                            $http.get(scheduleUrl + 'active/' + mmsi).success(onSuccess);
                        };

                        SessionStorageService.getItem(activeVoyage+mmsi, callback, remoteCall);
                    });
                } else {
                    callback(JSON.parse(voyageStr));
                }
            },
            getSchedule : function(mmsi, callback) {
                var remoteCall = function(onSuccess) {
                    $http.get(scheduleUrl + mmsi).success(onSuccess);
                };
                SessionStorageService.getItem(currentSchedule, callback, remoteCall);
            },
            getVoyageInfo : function(mmsi, voyageId, callback) {
                function findVoyageIndex(voyageId, schedule) {
                    for (var index = 0; index < schedule.voyages.length; index++) {
                        if (schedule.voyages[index].maritimeId === voyageId) {
                            return index;
                        }
                    }
                    return null;
                }
                function buildVoyageInfo(index, schecule, callback) {
                    var result = {
                        id : schecule.voyages[index].maritimeId,
                        dep : schecule.voyages[index].berthName,
                        depEta : schecule.voyages[index].departure,
                    };
                    if( index < schecule.voyages.length - 1){
                        result.des = schecule.voyages[index + 1].berthName;
                        result.desEta = schecule.voyages[index + 1].arrival;
                    }
                    return result;
                }
                this.getSchedule(mmsi, function(schedule) {
                    var index = findVoyageIndex(voyageId, schedule);
                    if (index == null) {
                        callback(null);
                        return;
                    }
                    var voyageInfo = buildVoyageInfo(index, schedule);
                    callback(voyageInfo);
                });
            },
            save : function(schedule, callback) {
                $http.put(scheduleUrl + 'save', schedule).success(function() {
                    SessionStorageService.removeItem(currentSchedule);
                    callback();
                });
            }
        };
    });

}());
