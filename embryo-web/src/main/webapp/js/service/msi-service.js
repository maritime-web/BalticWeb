(function() {
    var module = angular.module('embryo.msi', []);

    module.service('MsiService', [ '$http', function($http) {
        return {
            list: function(success,error) {
                $http.get(embryo.baseUrl + "rest/msi/list", {
                    timeout : embryo.defaultTimeout
                }).success(success).error(function(data, status, headers, config) {
                    error(embryo.ErrorService.errorStatus(data, status, "requesting MSI warnings"), status);
                });
            }
        };
    }]);

    embryo.msi = {};

    module.run(function(MsiService) {
        embryo.msi.service = MsiService;
    })
})();
