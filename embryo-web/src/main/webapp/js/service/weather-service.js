(function() {
    var module = angular.module('embryo.weather.service', []);

    module.service('WeatherService', [
            '$http',
            'CookieService',
            '$interval',
            function($http, CookieService, $interval) {
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
                                    subscription.callbacks[i](null, subscription.weather);
                                }
                            }
                        }
                    }
                }
                
                function mergeWeatherStructure(weather){
                    if(weather && weather.forecast){
                        for ( var index in weather.forecast.districts) {
                            var forecastDistrict = weather.forecast.districts[index];
                            forecastDistrict.warning = weather.warning.districts[forecastDistrict.name];
                            forecastDistrict.validTo = weather.forecast.to;
                        }
                    }
                    return weather;
                }

                function getWeatherData() {
                    service.weather(function(weather) {
                        subscription.weather = mergeWeatherStructure(weather);
                        notifySubscribers();
                    }, function(error, status) {
                        notifySubscribers(error);
                    });
                }

                service = {
                    weather : function(success, error) {
                        var messageId = embryo.messagePanel.show({
                            text : "Requesting weather forecast and warnings..."
                        });

                        $http.get(embryo.baseUrl + "rest/weather/dmi/greenland", {
                            timeout : embryo.defaultTimeout
                        }).success(function(weather) {
                            embryo.messagePanel.replace(messageId, {
                                text : "Weather forecast downloaded.",
                                type : "success"
                            });
                            success(weather);
                        }).error(
                                function(data, status, headers, config) {
                                    var errorMsg = embryo.ErrorService.errorStatus(data, status,
                                            "requesting weather forecast and warnings");
                                    embryo.messagePanel.replace(messageId, {
                                        text : errorMsg,
                                        type : "error"
                                    });
                                    error(errorMsg, status);
                                });
                    },
                    subscribe : function(callback) {
                        if (subscription == null) {
                            subscription = {
                                callbacks : [],
                                weather : null,
                                interval : null
                            };
                        }
                        var id = subscription.callbacks.push(callback);

                        if (subscription.interval == null) {
                            subscription.interval = $interval(getWeatherData, interval);
                            getWeatherData();
                        }
                        if (subscription.weather) {
                            callback(null, subscription.weather);
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
                    update : function() {
                        if (subscription.interval) {
                            $interval.cancel(subscription.interval);
                            subscription.interval = $interval(getWeatherData, interval);
                        }
                        getWeatherData();
                    }
                };

                return service;
            } ]);
})();
