(function() {
    "use strict";

    var userServiceModule = angular.module('embryo.userService', []);

    userServiceModule.factory('UserService', function($http) {
        return {
            userList : function(callback, error) {
                $http.get(embryo.baseUrl + "rest/user/list").success(callback).error(
                        function(data, status, headers, config) {
                            error(embryo.ErrorService.extractError(data, status, config));
                        });
            },
            create : function(user, callback, error) {
                $http.put(embryo.baseUrl + "rest/user/create", user).success(callback).error(
                        function(data, status, headers, config) {
                            error(embryo.ErrorService.extractError(data, status, config));
                        });
            },
            edit : function(user, callback, error) {
                $http.put(embryo.baseUrl + "rest/user/edit", user).success(callback).error(
                        function(data, status, headers, config) {
                            error(embryo.ErrorService.extractError(data, status, config));
                        });
            },
            deleteUser : function(userName, callback, error) {
                $http({
                    method : "delete",
                    url : embryo.baseUrl + "rest/user/delete/" + userName
                }).success(callback).error(function(data, status, headers, config) {
                    error(embryo.ErrorService.extractError(data, status, config));
                });
            },
            sourceFilters: function (success, error) {
                $http.get(embryo.baseUrl + "rest/user/available-source-filters", {
                    timeout: embryo.defaultTimeout
                }).success(success).error(function (data, status) {
                    error(embryo.ErrorService.errorStatus(data, status, "loading AIS source filters"), status);
                });
            }
        };
    });

}());
