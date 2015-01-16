(function() {
    var module = angular.module('embryo.forecast.service', []);

    var forecastPath = 'rest/forecasts/';
    
    module.service('ForecastService', [ '$http', function($http) {
        var service = {
            forecastSelected: null,
            replaceAllButSelected: function (existingForecasts, newForecasts) {
                if (!service.forecastSelected) {
                    return newForecasts;
                }

                var selectedIndex = -1;
                var length = existingForecasts.length;
                for (var index = 0; index < length; index++) {
                    if (existingForecasts[index].id == service.forecastSelected) {
                        selectedIndex = index;
                    }
                }
                if (selectedIndex >= 0) {
                    existingForecasts.splice(selectedIndex + 1, existingForecasts.length - 1 - selectedIndex);
                    existingForecasts.splice(0, selectedIndex);
                }
                length = newForecasts.length;
                for (var index = 0; index < length; index++) {
                    var forecast = newForecasts[index];
                    if (forecast.id != service.forecastSelected) {
                        existingForecasts.push(forecast);
                    }
                }

                existingForecasts.sort(function (f1, f2) {
                    return f1.area.localeCompare(f2.area);
                });

                return existingForecasts;
            },
            listWaveForecasts : function(success, error) {
                var messageId = embryo.messagePanel.show({
                    text : "Requesting wave forecasts..."
                });
                $http.get(embryo.baseUrl + forecastPath + 'waves', {
                    timeout : embryo.defaultTimeout
                }).success(function(forecasts) {
                    embryo.messagePanel.replace(messageId, {
                        text : "Wave forecasts downloaded.",
                        type : "success"
                    });
                    success(forecasts);
                }).error(function(data, status, headers, config) {
                    var errorMsg = embryo.ErrorService.errorStatus(data, status, "requesting wave forecasts");
                    embryo.messagePanel.replace(messageId, {
                        text : errorMsg,
                        type : "error"
                    });
                    error(errorMsg, status);
                });
            },
            getWaveForecast : function(id, success, error) {
                $http.get(embryo.baseUrl + forecastPath + 'waves/' + id, {
                    timeout : embryo.defaultTimeout
                }).success(function(forecast) {
                    success(forecast);
                }).error(function(data, status, headers, config) {

                });
            },
            listIceForecasts : function(success, error) {
                var messageId = embryo.messagePanel.show({
                    text : "Requesting ice forecasts..."
                });
                $http.get(embryo.baseUrl + forecastPath + 'ice', {
                    timeout : embryo.defaultTimeout
                }).success(function(forecasts) {
                    embryo.messagePanel.replace(messageId, {
                        text : "Ice forecasts downloaded.",
                        type : "success"
                    });
                    success(forecasts);
                }).error(function(data, status, headers, config) {
                    var errorMsg = embryo.ErrorService.errorStatus(data, status, "requesting ice forecasts");
                    embryo.messagePanel.replace(messageId, {
                        text : errorMsg,
                        type : "error"
                    });
                    error(errorMsg, status);
                });
            },
            getIceForecast : function(id, success, error) {
                $http.get(embryo.baseUrl + forecastPath + 'ice/' + id, {
                    timeout : embryo.defaultTimeout
                }).success(function(forecast) {
                    success(forecast);
                }).error(function(data, status, headers, config) {

                });
            },
            listCurrentForecasts : function(success, error) {
                var messageId = embryo.messagePanel.show({
                    text : "Requesting current forecasts..."
                });
                $http.get(embryo.baseUrl + forecastPath + 'currents', {
                    timeout : embryo.defaultTimeout
                }).success(function(forecasts) {
                    embryo.messagePanel.replace(messageId, {
                        text : "Current forecasts downloaded.",
                        type : "success"
                    });
                    success(forecasts);
                }).error(function(data, status, headers, config) {
                    var errorMsg = embryo.ErrorService.errorStatus(data, status, "requesting current forecasts");
                    embryo.messagePanel.replace(messageId, {
                        text : errorMsg,
                        type : "error"
                    });
                    error(errorMsg, status);
                });
            },
            getCurrentForecast : function(id, success, error) {
                $http.get(embryo.baseUrl + forecastPath + 'currents/' + id, {
                    timeout : embryo.defaultTimeout
                }).success(function(forecast) {
                    success(forecast);
                }).error(function(data, status, headers, config) {

                });
            }

        };

        return service;
    } ]);
})();