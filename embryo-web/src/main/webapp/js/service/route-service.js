(function() {
    "use strict";

    embryo.route = {};

    var module = angular.module('embryo.routeService', [ 'embryo.storageServices' ]).service('RouteService',

    function($rootScope, $http, SessionStorageService, LocalStorageService) {
        var routeKey = function(routeId) {
            return 'route_' + routeId;
        };
        
        var loggedInVesselDetails;
        function updateLoggedInVessel(error, vesselOverview, vesselDetails) {
            loggedInVesselDetails = vesselDetails;
        }
        embryo.authenticated(function() {
            if (embryo.authentication.shipMmsi) {
                embryo.vessel.service.subscribe(embryo.authentication.shipMmsi, updateLoggedInVessel);
            }
        });

        return {            
            getActive : function(mmsi, success, error) {
                var url = embryo.baseUrl + 'rest/route/active/' + mmsi;
                $http.get(url).success(success);
            },
            getActiveMeta : function(mmsi, success, error) {
                var url = embryo.baseUrl + 'rest/route/active/meta/' + mmsi;
                $http.get(url).success(success);
            },
            setActiveRoute : function(routeId, activity, callback, error) {
                $http.put(embryo.baseUrl + 'rest/route/activate/', {
                    routeId : routeId,
                    active : activity
                }).success(callback).error(function(data, status, headers, config) {
                    error(embryo.ErrorService.extractError(data, status, config));
                });
            },
            getRoute : function(routeId, callback) {
                // should routes be cached?
                var remoteCall = function(onSuccess) {
                    $http.get(embryo.baseUrl + 'rest/route/' + routeId).success(onSuccess);
                };
                SessionStorageService.getItem(routeKey(routeId), callback, remoteCall);
            },
            save : function(route, voyageId, success, error) {
                $http.put(embryo.baseUrl + 'rest/route/save', {
                    route : route,
                    voyageId : voyageId
                }).success(function() {
                    SessionStorageService.setItem(routeKey(route.id), route);
                    success();
                }).error(function(data, status, headers, config) {
                    error(embryo.ErrorService.extractError(data, status, config));
                });
            },
            saveAndActivate : function(route, voyageId, callback, error) {
                $http.put(embryo.baseUrl + 'rest/route/save/activate', {
                    route : route,
                    voyageId : voyageId
                }).success(function() {
                    SessionStorageService.setItem(routeKey(route.id), route);
                    callback();
                }).error(function(data, status, headers, config) {
                    error(embryo.ErrorService.getText(data, status, config));
                });
            },
            getRouteType : function(mmsi, routeId) {
                if (mmsi == embryo.authentication.shipMmsi) {
                    if (routeId == loggedInVesselDetails.additionalInformation.routeId) {
                        return "active";
                    } else {
                        return "planned";
                    }
                }   
                return "other";
            }
        };
    });

    module.run(function(RouteService) {
        embryo.route.service = RouteService;
    })
}());
