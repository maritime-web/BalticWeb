(function() {
    var module = angular.module('embryo.msi', []);

    module.service('MsiService', function() {
        return {
            list: function(callback) {
                $.ajax({
                    url: embryo.baseUrl+"rest/msi/list",
                    data: { },
                    success: function(data) {
                        callback(null, data);
                    },
                    error: function(error) {
                        callback(error);
                    }
                });
            }
        };
    });

    embryo.msi = {};

    module.run(function(MsiService) {
        embryo.msi.service = MsiService;
    })
})()
