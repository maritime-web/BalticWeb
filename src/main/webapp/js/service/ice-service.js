(function() {
    var module = angular.module('embryo.ice', []);

    module.service('IceService', function() {
        return {
            list: function(callback) {
                $.ajax({
                    url: embryo.baseUrl+"rest/ice/list",
                    data: { },
                    success: function(data) {
                        callback(null, data);
                    },
                    error: function(error) {
                        callback(error);
                    }
                });
            },
            shapes: function(ids, callback) {
                $.ajax({
                    url: embryo.baseUrl+"rest/shapefile/multiple/" + ids,
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

    embryo.ice = {};

    module.run(function(IceService) {
        embryo.ice.service = IceService;
    })
})()
