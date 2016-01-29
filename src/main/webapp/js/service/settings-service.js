(function() {
    "use strict";

    var settingsServiceModule = angular.module('embryo.settingsService', []);

    settingsServiceModule.factory('SettingsService', function($http) {
        return {
            search : function(search, callback, error) {
                $http.get(embryo.baseUrl + "rest/log/search", {
                    params : search
                }).success(callback).error(function(data, status, headers, config) {
                    error(embryo.ErrorService.extractError(data, status, config));
                });
            },
            services : function(callback, error) {
                $http.get(embryo.baseUrl + "rest/log/services").success(callback).error(
                        function(data, status, headers, config) {
                            error(embryo.ErrorService.extractError(data, status, config));
                        });
            }

        };
    });

}());
