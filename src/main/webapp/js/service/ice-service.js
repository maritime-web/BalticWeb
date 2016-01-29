(function() {
    var module = angular.module('embryo.ice.service', [ 'embryo.storageServices' ]);

    module.service('IceService', [
            '$http',
            '$interval',
        function ($http, $interval) {
            function listByChartType(type, success, error) {
                var messageId = embryo.messagePanel.show({
                    text: "Requesting list of ice charts ..."
                });

                function onSuccess(data) {
                    embryo.messagePanel.replace(messageId, {
                        text: "List of " + data.length + (type == "iceberg" ? " icebergs" : " ice charts")
                            + " downloaded.",
                        type: "success"
                    });
                    success(data);
                    }

                function onError(data, status, headers, config) {
                    var txt = "requesting list of ice charts";
                    var errorMsg = embryo.ErrorService.errorStatus(data, status, txt);
                    embryo.messagePanel.replace(messageId, {
                        text: errorMsg,
                        type: "error"
                    });
                    error(errorMsg, status);
                    }

                $http.get(embryo.baseUrl + "rest/ice/" + type + "/observations", {
                    timeout: embryo.defaultTimeout,
                }).success(onSuccess).error(onError);
                }

            var service = {
                    iceCharts : function(success, error) {
                        listByChartType("iceChart", success, error);
                    },
                    icebergs : function(success, error) {
                        listByChartType("iceberg", success, error);
                    },
                    inshoreIceReport : function(success, error) {
                        var messageId = embryo.messagePanel.show({
                            text : "Requesting inshore ice report ..."
                        });
                        function onSuccess(data) {
                            var count = data && data.observations ? Object.keys(data.observations).length : 0;
                            
                            embryo.messagePanel.replace(messageId, {
                                text : "Inshore ice report with " + count + " observations downloaded.",
                                type : "success"
                            });
                            success(data);
                        }
                        function onError(data, status, headers, config) {
                            var txt = "requesting inshore ice report";
                            var errorMsg = embryo.ErrorService.errorStatus(data, status, txt);
                            embryo.messagePanel.replace(messageId, {
                                text : errorMsg,
                                type : "error"
                            });
                            error(errorMsg, status);
                        }

                        $http.get(embryo.baseUrl + "rest/inshore-ice-report/provider/dmi", {
                            timeout: embryo.defaultTimeout
                        }).success(onSuccess).error(onError);
                    }
                };

                return service;
            } ]);

    embryo.ice = {
        delta : true,
        exponent : 3
    };

    module.run(function(IceService) {
        embryo.ice.service = IceService;
    });
})();
