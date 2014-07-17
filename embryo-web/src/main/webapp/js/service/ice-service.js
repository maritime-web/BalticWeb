(function() {
    var module = angular.module('embryo.ice.service', [ 'embryo.storageServices' ]);

    module.service('IceService', [
            '$http',
            '$interval',
            'CookieService',
            function($http, $interval, CookieService) {
                var subscriptions = {};
                var service = null;
                var providerInterval = 7 * 24 * 60 * 1000 * 60;
                var interval = 2 * 60 * 1000 * 60;

                function notifySubscribers(key, error) {
                    if (subscriptions[key]) {
                        for ( var i in subscriptions[key].callbacks) {
                            if (subscriptions[key].callbacks[i]) {
                                if (error) {
                                    subscriptions[key].callbacks[i](error);
                                } else {
                                    if (subscriptions[key].value != null) {
                                        subscriptions[key].callbacks[i](null, subscriptions[key].value);
                                    }
                                }
                            }
                        }
                    }

                    if (key == "providers" && !error) {
                        if (subscriptions["iceCharts"].value != null) {
                            notifySubscribers("iceCharts");
                        }
                        if (subscriptions["icebergs"].value != null) {
                            notifySubscribers("icebergs");
                        }
                    }
                }

                function getLoader(key) {
                    var f = function() {
                        service[key](function(value) {
                            subscriptions[key].value = value;
                            notifySubscribers(key);
                        }, function(error, status) {
                            notifySubscribers(key, error);
                        });
                    };
                    return f;
                }

                function validateAndAdjustSelectedProvider(providers) {
                    if (!providers || providers.length == 0) {
                        service.setSelectedProvider(null);
                        return;
                    }
                    service.getSelectedProvider(providers[0]);
                }

                service = {
                    providers : function(success, error) {
                        var messageId = embryo.messagePanel.show({
                            text : "Requesting ice chart providers ..."
                        });

                        function onSuccess(providers) {
                            embryo.messagePanel.replace(messageId, {
                                text : "List of " + providers.length + " ice chart providers downloaded",
                                type : "success"
                            });
                            validateAndAdjustSelectedProvider(providers);
                            success(providers);
                        }

                        function onError(data, status, headers, config) {
                            var errorMsg = embryo.ErrorService.errorStatus(data, status,
                                    "requesting ice chart providers");
                            embryo.messagePanel.replace(messageId, {
                                text : errorMsg,
                                type : "error"
                            });
                            error ? error(errorMsg, status) : null;
                        }

                        $http.get(embryo.baseUrl + "rest/ice/provider/list", {
                            timeout : embryo.defaultTimeout
                        }).success(onSuccess).error(onError);
                    },
                    getSelectedProvider : function(defaultProvider) {
                        var value = CookieService.get("dma-ice-provider-sel-");
                        if (!value) {
                            CookieService.set("dma-ice-provider-sel-", defaultProvider, 365);
                            value = defaultProvider;
                        }
                        return value;
                    },
                    setSelectedProvider : function(provider) {
                        CookieService.set("dma-ice-provider-selected-", provider);
                    },
                    listByProvider : function(type, provider, success, error) {
                        var messageId = embryo.messagePanel.show({
                            text : "Requesting list of ice charts ..."
                        });

                        function onSuccess(data) {
                            embryo.messagePanel.replace(messageId, {
                                text : "List of " + data.length + (type == "iceberg" ? " icebergs" : " ice charts")
                                        + " downloaded.",
                                type : "success"
                            });
                            success(data);
                        }

                        function onError(data, status, headers, config) {
                            var txt = "requesting list of ice charts";
                            var errorMsg = embryo.ErrorService.errorStatus(data, status, txt);
                            embryo.messagePanel.replace(messageId, {
                                text : errorMsg,
                                type : "error"
                            });
                            error(errorMsg, status);
                        }

                        $http.get(embryo.baseUrl + "rest/ice/provider/" + type + "/" + provider + "/observations", {
                            timeout : embryo.defaultTimeout,
                        }).success(onSuccess).error(onError);
                    },
                    iceCharts : function(success, error) {
                        var provider = this.getSelectedProvider("");
                        if (provider) {
                            this.listByProvider("iceChart", provider.key, success, error);
                        }
                    },
                    icebergs : function(success, error) {
                        var provider = this.getSelectedProvider("");
                        if (provider) {
                            this.listByProvider("iceberg", provider.key, success, error);
                        }
                    },
                    subscribe : function(callbackConfig) {
                        var ids = {};
                        var keys = Object.keys(callbackConfig);
                        for ( var index in keys) {
                            var key = keys[index];
                            if (key != "name") {
                                if (!subscriptions[key]) {
                                    subscriptions[key] = {
                                        callbacks : [],
                                        names : [],
                                        interval : null,
                                        value : null
                                    };
                                }

                                for ( var index in subscriptions[key].names) {
                                    if (callbackConfig["name"] === subscriptions[key].names[index]) {
                                        ids[key] = index;
                                    }
                                }
                                if (!ids[key]) {
                                    ids[key] = subscriptions[key].callbacks.push(callbackConfig[key]);
                                    subscriptions[key].names.push(callbackConfig["name"]);

                                    if (subscriptions[key].interval == null) {
                                        var intervalValue = (key === "providers" ? providerInterval : interval);
                                        subscriptions[key].interval = $interval(getLoader(key), intervalValue);
                                        getLoader(key)();
                                    }

                                }
                                if (subscriptions[key].value) {
                                    callbackConfig[key](null, subscriptions[key].value);
                                }
                            }
                        }

                        return ids;
                    },
                    unsubscribe : function(ids) {
                        var keys = Object.keys(ids);
                        for ( var index in keys) {
                            var key = keys[index];
                            subscriptions[key].callbacks.splice(ids[key], 1);
                            subscriptions[key].names.splice(ids[key], 1);
                            var allDead = subscriptions[key].callbacks.length == 0;
                            if (allDead) {
                                clearInterval(subscriptions[key].interval);
                                delete subscriptions[key];
                            }
                        }
                    },
                    update : function(type) {
                        function reload(key) {
                            var intervalValue = (key === "providers" ? providerInterval : interval);
                            $interval.cancel(subscriptions[key].interval);
                            subscriptions[key].interval = $interval(getLoader(key), intervalValue);
                            getLoader(key)();
                        }

                        if (type) {
                            reload(type);
                        } else {
                            var keys = Object.keys(subscriptions);
                            for ( var index in keys) {
                                var key = keys[index];
                                if (key != "providers" && subscriptions[key].interval) {
                                    reload(key);
                                }
                            }

                        }
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
