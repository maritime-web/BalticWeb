(function() {
    var module = angular.module('embryo.ice', []);

    module.service('IceService', ['$http', function($http) {
        return {
            providers : function(success, error) {
                $http.get(embryo.baseUrl + "rest/ice/provider/list", {
                    timeout : embryo.defaultTimeout
                }).success(success).error(function(data, status, headers, config) {
                    error(embryo.ErrorService.errorStatus(data, status, "requesting ice chart providers"), status);
                });
            },
            getSelectedProvider : function(defaultProvider) {
                var value = getCookie("selectedProvider");
                if (!value) {
                    setCookie("selectedProvider", defaultProvider, 365);
                    value = defaultProvider;
                }
                return value;
            },
            setSelectedProvider : function(rovider) {
                setCookie("selectedProvider");
            },
            listByProvider : function(provider, success, error) {
                $http.get(embryo.baseUrl + "rest/ice/provider/" + provider + "/observations", {
                    timeout : embryo.defaultTimeout,
                }).success(success).error(function(data, status, headers, config) {
                    error(embryo.ErrorService.errorStatus(data, status, "requesting list of ice observations"), status);
                });
            }        };
    }]);

    embryo.ice = {
        delta : true,
        exponent : 3
    };

    module.run(function(IceService) {
        embryo.ice.service = IceService;
    });
})();
