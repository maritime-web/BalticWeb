(function() {
    "use strict";

    var scheduleUrl = embryo.baseUrl + 'rest/schedule/';

    var scheduleServiceModule = angular.module('embryo.scheduleService', [ 'embryo.storageServices' ]);

    scheduleServiceModule.factory('ScheduleService', function($http, SessionStorageService) {
        var currentSchedule = 'schedule_current';
        var activeVoyage = 'voyage_active';

        function buildVoyageInfo(index, schecule) {
            var result = {
                id : schecule.voyages[index].maritimeId,
                dep : schecule.voyages[index].berthName,
                depEta : schecule.voyages[index].departure,
                crew : schecule.voyages[index].crew,
                passengers : schecule.voyages[index].passengers,
                routeId : schecule.voyages[index].route.id,
            };
            if (index < schecule.voyages.length - 1) {
                result.des = schecule.voyages[index + 1].berthName;
                result.desEta = schecule.voyages[index + 1].arrival;
            }
            return result;
        }

        return {
            getActiveVoyage : function(mmsi, routeId, callback ,error) {
                function findVoyageIndex(schedule) {
                    for ( var index = 0; index < schedule.voyages.length; index++) {
                        if (schedule.voyages[index].route.id === routeId) {
                            return index;
                        }
                    }
                    return null;
                }
                this.getYourSchedule(mmsi, function(schedule) {
                    var index = findVoyageIndex(schedule);
                    if (index == null) {
                        callback(null);
                        return;
                    }
                    var voyageInfo = buildVoyageInfo(index, schedule);
                    callback(voyageInfo);
                }, function(errorMsgs){
                    error(errorMsgs);
                });
            },
            getYourSchedule : function(mmsi, callback, error) {
                var remoteCall = function(onSuccess) {
                    $http.get(scheduleUrl + mmsi).success(onSuccess).error(function(data, status, headers, config) {
                        error(embryo.ErrorService.extractError(data, status, config));
                    });
                };
                SessionStorageService.getItem(currentSchedule, callback, remoteCall);
            },
            getSchedule : function(mmsi, callback, error) {
                $http.get(scheduleUrl + mmsi).success(callback).error(function(data, status, headers, config) {
                    error(embryo.ErrorService.extractError(data, status, config));
                });
            },
            getVoyageInfo : function(mmsi, voyageId, callback, error) {
                function findVoyageIndex(voyageId, schedule) {
                    for ( var index = 0; index < schedule.voyages.length; index++) {
                        if (schedule.voyages[index].maritimeId === voyageId) {
                            return index;
                        }
                    }
                    return null;
                }
                this.getYourSchedule(mmsi, function(schedule) {
                    var index = findVoyageIndex(voyageId, schedule);
                    if (index == null) {
                        callback(null);
                        return;
                    }
                    var voyageInfo = buildVoyageInfo(index, schedule);
                    callback(voyageInfo);
                }, function(errorMsgs) {
                    error(errorMsgs);
                });
            },
            save : function(schedule, callback, error) {
                $http.put(scheduleUrl + 'save', schedule).success(function() {
                    SessionStorageService.removeItem(currentSchedule);
                    callback();
                }).error(function(data, status, headers, config) {
                    error(embryo.ErrorService.extractError(data, status, config));
                });
            }
        };
    });

}());
