var metocModule = angular.module('embryo.metoc', []);

metocModule.factory('MetocService', function($http) {
    return {
        getMetoc : function(routeId, callback) {
            var messageId = embryo.messagePanel.show({
                text : "Loading metoc ..."
            });

            var url = embryo.baseUrl + 'rest/metoc/' + routeId;

            $http.get(url, {
                responseType : 'json'
            }).success(function(result) {
                embryo.messagePanel.replace(messageId, {
                    text : "Metoc loaded.",
                    type : "success"
                });

                callback(result);
            }).error(function(data, status) {
                embryo.messagePanel.replace(messageId, {
                    text : "Failed loading metoc. Server returned error: " + status,
                    type : "error"
                });
                callback(null);
            });
        }
    };
});

embryo.metoc = {};

metocModule.run(function(MetocService) {
    embryo.metoc.service = MetocService;
})

var defaultCurrentLow = 1.0;
var defaultCurrentMedium = 2.0;
var defaultCurrentWarnLimit = 4.0;
var defaultWaveLow = 1.0;
var defaultWaveMedium = 2.0;
var defaultWaveWarnLimit = 3.0;
var defaultWindWarnLimit = 10.0;

