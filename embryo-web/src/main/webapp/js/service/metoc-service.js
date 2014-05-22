(function() {

    var metocModule = angular.module('embryo.metoc', []);

    metocModule.factory('MetocService', function($http) {
        return {
            listMetoc : function(routeIds, callback) {
                var messageId = embryo.messagePanel.show({
                    text : "Loading metoc ..."
                });

                var ids = "";
                for(var index in routeIds){
                    if(ids.length !== 0){
                        ids += ":";
                    }
                    ids += routeIds[index];
                }
                
                var url = embryo.baseUrl + 'rest/metoc/list/' + ids;

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
})();
