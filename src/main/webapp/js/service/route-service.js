/*
 * Dependencies:
 * 
 * aisview.js
 * aisviewUI.js
 * ....
 */

(function() {
    "use strict";

    embryo.route = {};

    var module = angular.module('embryo.routeService', [ 'embryo.storageServices' ]).service('RouteService',

    function($rootScope, $http, SessionStorageService, LocalStorageService) {
        var routeKey = function(routeId) {
            return 'route_' + routeId;
        };

        return {
            getActive : function(mmsi, callback) {
                var remoteCall = function(onSuccess) {
                    var url = embryo.baseUrl + 'rest/route/active/' + mmsi;
                    $http.get(url).success(onSuccess);
                };

                remoteCall(callback);
            },
            setActiveRoute : function(routeId, activity, callback) {
                $http.put(embryo.baseUrl + 'rest/route/activate/', {
                    routeId : routeId,
                    active : activity
                }).success(callback);
            },
            getRoute : function(routeId, callback) {
                // should routes be cached?
                var remoteCall = function(onSuccess) {
                    $http.get(embryo.baseUrl + 'rest/route/' + routeId).success(onSuccess);
                };
                SessionStorageService.getItem(routeKey(routeId), callback, remoteCall);
            },
            save : function(route, voyageId, callback) {
                $http.put(embryo.baseUrl + 'rest/route', {
                    route : route,
                    voyageId : voyageId
                }).success(function() {

                    console.log("something happened");

                    SessionStorageService.setItem(routeKey(route.id), route);
                    callback();
                }).error(function() {
                    console.log("error");

                });
            },
            saveAndActivate : function(route, voyageId, callback) {
                $http.put(embryo.baseUrl + 'rest/route/save/activate', {
                    route : route,
                    voyageId : voyageId
                }).success(function() {
                    SessionStorageService.setItem(routeKey(route.id), route);
                    callback();
                });
            }

        };
    });

    module.run(function(RouteService) {
        embryo.route.service = RouteService;
    })
}());
