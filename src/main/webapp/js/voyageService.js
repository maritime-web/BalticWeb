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

    var voyageUrl = embryo.baseUrl + 'rest/voyage/', voyageTypeaheadUrl = embryo.baseUrl + 'rest/voyage/typeahead/';

    var voyageServiceModule = angular.module('embryo.voyageService', [ 'embryo.storageServices' ]);

    voyageServiceModule.factory('VoyageService',
            function($rootScope, $http, ShipService, SessionStorageService) {
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
                    getVoyages : function(mmsi, callback) {
                        $http.get(voyageTypeaheadUrl + mmsi).success(callback);
                    },
                    save : function(plan, callback) {
                        $http.put(voyageUrl + 'savePlan', plan).success(function() {
                            SessionStorageService.removeItem(currentPlan);
                            callback();
                            $rootScope.$broadcast('yourshipDataUpdated');
                        });
                    }
                };
            });

}());
