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

    var voyageServiceModule = angular.module('embryo.voyageService', [ 'embryo.storageServices', 'ngResource' ]);

    voyageServiceModule.factory('VoyageRestService', function($resource) {
        var defaultParams = {};
        var actions = {
            getActive : {
                params : {
                    action : 'active'
                },
                method : 'GET',
                isArray : false,
            }
        };
        return $resource(embryo.baseUrlForAngularResource + 'rest/voyage/:action/:id', defaultParams, actions);
    });

    voyageServiceModule.factory('VoyageService',
            function($rootScope, $http, VoyageRestService, ShipService, SessionStorageService) {
                var currentPlan = 'voyagePlan_current';
                var activeVoyage = 'voyage_active';

                return {
                    getYourActive : function(onSuccess) {
                        var voyageStr = sessionStorage.getItem('activeVoyage');
                        if (!voyageStr) {
                            ShipService.getYourShip(function(yourShip) {
                                var voyage = VoyageRestService.getActive({
                                    id : yourShip.maritimeId
                                }, function() {
                                    // only cache objects with values (empty
                                    // objects has ngResource REST methods).
                                    if (voyage.maritimeId) {
                                        var voyageStr = JSON.stringify(voyage);
                                        sessionStorage.setItem('activeVoyage', voyageStr);
                                    }
                                    onSuccess(voyage);
                                });
                            });
                        } else {
                            onSuccess(JSON.parse(voyageStr));
                        }
                    },
                    getCurrentPlan : function(mmsi, callback) {
                        var remoteCall = function(onSuccess) {
                            $http.get(voyageUrl + mmsi + '/current', {
                                responseType : 'json'
                            }).success(onSuccess);
                        };
                        SessionStorageService.getItem(currentPlan, callback, remoteCall);
                    },
                    getVoyages : function(mmsi, callback) {
                        $http.get(voyageTypeaheadUrl + mmsi, {
                            responseType : 'json'
                        }).success(callback);
                    },
                    save : function(plan, callback) {
                        $http.put(voyageUrl + 'savePlan', plan, {
                            responseType : 'json'
                        }).success(function() {
                            SessionStorageService.removeItem(currentPlan);
                            callback();
                            $rootScope.$broadcast('yourshipDataUpdated');
                        });
                    }
                };
            });

}());
