(function() {
    var module = angular.module('embryo.vessel', []);

    var lastRequestId = 0;

    module.service('VesselService', function() {
        return {
            list: function(callback) {
                $.ajax({
                    url: embryo.baseUrl + "json_proxy/vessel_list",
                    timeout: embryo.defaultTimeout,
                    data: {
                        requestId: lastRequestId
                    },
                    success: function(data) {
                        if (data.requestId != lastRequestId) return;
                        callback(null, data.vesselList.vessels);
                    },
                    error: function(data) {
                        callback(data);
                    }
                });
            },
            details: function(id, callback) {
                $.ajax({
                    url: embryo.baseUrl + "rest/vessel/details",
                    timeout: embryo.defaultTimeout,
                    data: {
                        id : id,
                        past_track: 0
                    },
                    success: function(data) {
                        callback(null, data);
                    },
                    error: function(data) {
                        callback(data);
                    }
                });
            },
            search: function(argument, callback) {
                $.ajax({
                    url: embryo.baseUrl + "json_proxy/vessel_search",
                    timeout: embryo.defaultTimeout,
                    data: {
                        argument: argument
                    },
                    success: function(data) {
                        callback(null, data.vessels)
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                        callback(jqXHR, null);
                    }
                })
            },
            clientSideSearch: function(argument, callback) {
                if (argument == null || argument == "") return [];

                var result = [];

                $.each(embryo.vessel.allVessels(), function (k,v) {
                    if (v.vesselName) {
                        if ((v.vesselName.toLowerCase().indexOf(argument.toLowerCase()) == 0) || 
                            (v.vesselName.toLowerCase().indexOf(" "+argument.toLowerCase()) >= 0)) {
                            result.push(v);
                        }
                    }
                })

                callback(result);
            }
        };
    });

    module.run(function(VesselService) {
        embryo.vessel.service = VesselService;
    })
})();
