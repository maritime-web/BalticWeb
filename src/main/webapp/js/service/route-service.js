(function () {
    "use strict";

    embryo.route = {};

    var module = angular.module('embryo.routeService', [ 'embryo.storageServices' ]).service('RouteService',

        function ($rootScope, $http, SessionStorageService) {
            var routeKey = function (routeId) {
                return 'route_' + routeId;
            };

            var selectedRoutes = [];

            return {
                getActive: function (mmsi, success, error) {
                    var url = embryo.baseUrl + 'rest/route/active/' + mmsi;
                    $http.get(url).success(success);
                },
                getActiveMeta: function (mmsi, success, error) {
                    var url = embryo.baseUrl + 'rest/route/active/meta/' + mmsi;
                    $http.get(url).success(success);
                },
                setActiveRoute: function (routeId, activity, callback, error) {
                    $http.put(embryo.baseUrl + 'rest/route/activate/', {
                        routeId: routeId,
                        active: activity
                    }).success(callback).error(function (data, status, headers, config) {
                        error(embryo.ErrorService.extractError(data, status, config));
                    });
                },
                getRoute: function (routeId, callback) {
                    // should routes be cached?
                    var remoteCall = function (onSuccess) {
                        $http.get(embryo.baseUrl + 'rest/route/' + routeId).success(onSuccess);
                    };
                    SessionStorageService.getItem(routeKey(routeId), callback, remoteCall);
                },
                getRoutes: function (routeIds, onSuccess, onError) {
                    var messageId = embryo.messagePanel.show({
                        text: "Loading routes ... "
                    });
                    var ids = "";
                    for (var index in routeIds) {
                        if (ids.length > 0) {
                            ids += ":";
                        }
                        ids += routeIds[index];
                    }
                    $http.get(embryo.baseUrl + 'rest/route/list/' + ids).success(function (routes) {
                        embryo.messagePanel.replace(messageId, {
                            text: routes.length + " routes loaded.",
                            type: "success"
                        });
                        onSuccess(routes);
                    }).error(function () {
                        var errorMsg = embryo.ErrorService.extractError(data, status, config);
                        embryo.messagePanel.replace(messageId, {
                            text: errorMsg,
                            type: "error"
                        });
                        if (onError) {
                            onError(errorMsg);
                        }
                    });
                },
                save: function (route, voyageId, success, error) {
                    $http.put(embryo.baseUrl + 'rest/route/save', {
                        route: route,
                        voyageId: voyageId
                    }).success(function () {
                        SessionStorageService.setItem(routeKey(route.id), route);
                        success();
                    }).error(function (data, status, headers, config) {
                        error(embryo.ErrorService.extractError(data, status, config));
                    });
                },
                saveAndActivate: function (route, voyageId, callback, error) {
                    $http.put(embryo.baseUrl + 'rest/route/save/activate', {
                        route: route,
                        voyageId: voyageId
                    }).success(function () {
                        SessionStorageService.setItem(routeKey(route.id), route);
                        callback();
                    }).error(function (data, status, headers, config) {
                        error(embryo.ErrorService.getText(data, status, config));
                    });
                },
                addSelectedRoute: function (route) {
                    selectedRoutes.push(route);
                },
                clearSelection: function () {
                    return selectedRoutes = [];
                },
                removeSelection: function (route) {
                    var index = -1;
                    for (var j in selectedRoutes) {
                        if (selectedRoutes[j].id === route.id) {
                            index = j;
                        }
                    }
                    if (index >= 0) {
                        selectedRoutes.splice(index, 1);
                    }
                },
                getSelectedRoutes: function () {
                    return selectedRoutes;
                }
            };
        });

    module.run(function (RouteService) {
        embryo.route.service = RouteService;
    });
}());
