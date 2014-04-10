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
            getYourActive : function(mmsi, callback) {
                var voyageStr = sessionStorage.getItem(activeVoyage+mmsi);
                if (!voyageStr) {
                    ShipService.getYourShip(function(yourShip) {
                        var remoteCall = function(onSuccess) {
                            $http.get(voyageUrl + 'active/' + mmsi).success(onSuccess);
                        };

                        SessionStorageService.getItem(activeVoyage+mmsi, callback, remoteCall);
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
                    $http.get(scheduleUrl + 'overview/' + mmsi).success(onSuccess);
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
            getVoyageInfo : function(mmsi, voyageId, callback) {
                function findVoyageIndex(voyageId, schedule) {
                    for (var index = 0; index < schedule.voyages.length - 1; index++) {
                        if (schedule.voyages[index].maritimeId === voyageId) {
                            return index;
                        }
                    }
                    return null;
                }
                function buildVoyageInfo(index, schecule, callback) {
                    var result = {
                        id : schecule.voyages[index].maritimeId,
                        dep : schecule.voyages[index].location,
                        depEta : schecule.voyages[index].departure,
                    };
                    if( index < schecule.voyages.length - 1){
                        result.des = schecule.voyages[index + 1].location;
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
            save : function(plan, callback) {
                $http.put(voyageUrl + 'savePlan', plan).success(function() {
                    SessionStorageService.removeItem(currentPlan);
                    callback();
                    $rootScope.$broadcast('yourshipDataUpdated');
                });
            },
            saveSchedule : function(schedule, callback) {
                $http.put(scheduleUrl + 'save', schedule).success(function() {
                    SessionStorageService.removeItem(currentPlan);
                    callback();
                    $rootScope.$broadcast('yourshipDataUpdated');
                });
            }
        };
    });

}());
