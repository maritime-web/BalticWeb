(function() {
    var module = angular.module('embryo.msi.service', []);

    module.service('MsiService', [ '$http', 'CookieService', '$interval', function($http, CookieService, $interval) {
        var subscription = null;
        var service = null;
        var interval = 1 * 60 * 1000 * 60;

        function notifySubscribers(error) {
            if (subscription) {
                for ( var i in subscription.callbacks) {
                    if (subscription.callbacks[i]) {
                        if (error) {
                            subscription.callbacks[i](error);
                        } else {
                            subscription.callbacks[i](null, subscription.warnings, subscription.regions, subscription.selectedRegions);
                        }
                    }
                }
            }
        }

        
        function getMsiData() {
            function getWarnings(regionNames){
                subscription.selectedRegions = service.getSelectedRegions();
                if(!subscription.selectedRegions){
                    var regionNames = service.regions2Array(subscription.regions, true);
                    service.setSelectedRegions(regionNames);
                }
                service.list(subscription.selectedRegions, function(warnings){
                    subscription.warnings = warnings;
                    notifySubscribers();
                }, function(error, status){
                    notifySubscribers(error);
                });
            }
            
            if (!subscription.regions) {
                service.regions(function(regions) {
                    subscription.regions = regions;
                    getWarnings();
                });
            }else{
                getWarnings();
            }
        }

        service = {
            regions2Array : function(regions, all) {
                var result = [];
                for ( var x in regions) {
                    if (all || regions[x].selected) {
                        result.push(regions[x].name);
                    }
                }
                return result;
            },
            list : function(regions, success, error) {
                var messageId = embryo.messagePanel.show({
                    text : "Requesting active MSI warnings ..."
                });
                
                $http.get(embryo.baseUrl + "rest/msi/list?" + arrayToHttpParams(regions, 'regions'), {
                    timeout : embryo.defaultTimeout
                }).success(function(warnings){
                    embryo.messagePanel.replace(messageId, {
                        text : warnings.length + " MSI warnings returned.",
                        type : "success"
                    });
                    success(warnings);
                }).error(function(data, status, headers, config) {
                    var errorMsg = embryo.ErrorService.errorStatus(data, status, "requesting MSI warnings");
                    embryo.messagePanel.replace(messageId, {
                        text : errorMsg,
                        type : "error"
                    });
                    error(errorMsg, status);
                });
            },
            regions : function(success, error) {
                $http.get(embryo.baseUrl + 'rest/msi/regions', {
                    timeout : embryo.defaultTimeout
                }).success(success).error(function(data, status, headers, config) {
                    error(embryo.ErrorService.errorStatus(data, status, "requesting MSI regions"), status);
                });
            },
            setSelectedRegions : function(regions) {
                CookieService.set("dma-msi-regions-" + embryo.authentication.userName, regions, 30);
            },
            getSelectedRegions : function() {
                return CookieService.get("dma-msi-regions-" + embryo.authentication.userName);
            },
            subscribe : function(callback) {
                if (subscription == null) {
                    subscription = {
                        callbacks : [],
                        regions : null,
                        warnings : null,
                        interval : null
                    };
                }
                var id = subscription.callbacks.push(callback);

                if (subscription.interval == null) {
                    subscription.interval = $interval(getMsiData, interval);
                    getMsiData();
                }
                if (subscription.warnings) {
                    callback(null, subscription.warnings, subscription.regions, subscription.selectedRegions);
                }
                return {
                    id : id
                };
            },
            unsubscribe : function(id) {
                subscription.callbacks[id.id] = null;
                var allDead = true;
                for ( var i in subscription.callbacks)
                    allDead &= subscription.callbacks[i] == null;
                if (allDead) {
                    clearInterval(subscription.interval);
                    subscription = null;
                }
            },
            update : function(){
                if(subscription.interval){
                    $interval.cancel(subscription.interval);
                    subscription.interval = $interval(getMsiData, interval);
                }
                getMsiData();
            }
        };

        return service;
    } ]);
})();
