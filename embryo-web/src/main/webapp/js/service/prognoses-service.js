(function() {
    var module = angular.module('embryo.prognoses.service', []);

    module.service('PrognosesService', [ '$http', function($http) {
        var service = {
            listWavePrognoses : function(success, error) {
                var messageId = embryo.messagePanel.show({
                    text : "Requesting wave forecasts..."
                });
                $http.get(embryo.baseUrl + 'rest/prognoses/waves', {
                    timeout : embryo.defaultTimeout
                }).success(function(prognoses) {
                    embryo.messagePanel.replace(messageId, {
                        text : "Wave forecasts downloaded.",
                        type : "success"
                    });
                    success(prognoses);
                }).error(function(data, status, headers, config) {
                    var errorMsg = embryo.ErrorService.errorStatus(data, status, "requesting wave forecasts");
                    embryo.messagePanel.replace(messageId, {
                        text : errorMsg,
                        type : "error"
                    });
                    error(errorMsg, status);
                });
            },
            getWavePrognosis : function(id, success, error) {
                $http.get(embryo.baseUrl + 'rest/prognoses/waves/' + id, {
                    timeout : embryo.defaultTimeout
                }).success(function(prognosis) {
                    success(prognosis);
                }).error(function(data, status, headers, config) {

                });
            },
            listIcePrognoses : function(success, error) {
                var messageId = embryo.messagePanel.show({
                    text : "Requesting ice forecasts..."
                });
                $http.get(embryo.baseUrl + 'rest/prognoses/ice', {
                    timeout : embryo.defaultTimeout
                }).success(function(prognoses) {
                    embryo.messagePanel.replace(messageId, {
                        text : "Ice forecasts downloaded.",
                        type : "success"
                    });
                    success(prognoses);
                }).error(function(data, status, headers, config) {
                    var errorMsg = embryo.ErrorService.errorStatus(data, status, "requesting ice forecasts");
                    embryo.messagePanel.replace(messageId, {
                        text : errorMsg,
                        type : "error"
                    });
                    error(errorMsg, status);
                });
            },
            getIcePrognosis : function(id, success, error) {
                $http.get(embryo.baseUrl + 'rest/prognoses/ice/' + id, {
                    timeout : embryo.defaultTimeout
                }).success(function(prognosis) {
                    success(prognosis);
                }).error(function(data, status, headers, config) {

                });
            },
            listCurrentPrognoses : function(success, error) {
                var messageId = embryo.messagePanel.show({
                    text : "Requesting current forecasts..."
                });
                $http.get(embryo.baseUrl + 'rest/prognoses/currents', {
                    timeout : embryo.defaultTimeout
                }).success(function(prognoses) {
                    embryo.messagePanel.replace(messageId, {
                        text : "Current forecasts downloaded.",
                        type : "success"
                    });
                    success(prognoses);
                }).error(function(data, status, headers, config) {
                    var errorMsg = embryo.ErrorService.errorStatus(data, status, "requesting current forecasts");
                    embryo.messagePanel.replace(messageId, {
                        text : errorMsg,
                        type : "error"
                    });
                    error(errorMsg, status);
                });
            },
            getCurrentPrognosis : function(id, success, error) {
                $http.get(embryo.baseUrl + 'rest/prognoses/currents/' + id, {
                    timeout : embryo.defaultTimeout
                }).success(function(prognosis) {
                    success(prognosis);
                }).error(function(data, status, headers, config) {

                });
            }

        };

        return service;
    } ]);
})();