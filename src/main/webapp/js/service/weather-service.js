(function() {
    var module = angular.module('embryo.weather.service', []);

    module.service('WeatherService', [
            '$http',
        function ($http) {

                function mergeWeatherStructure(weather) {
                    if (weather && weather.forecast) {
                        for ( var index in weather.forecast.districts) {
                            var forecastDistrict = weather.forecast.districts[index];

                            if (weather.warnings.gale[forecastDistrict.name]
                                    || weather.warnings.storm[forecastDistrict.name]
                                    || weather.warnings.icing[forecastDistrict.name]) {

                                forecastDistrict.warnings = {
                                    gale : weather.warnings.gale[forecastDistrict.name],
                                    storm : weather.warnings.storm[forecastDistrict.name],
                                    icing: weather.warnings.icing[forecastDistrict.name]
                                };
                            }
                            forecastDistrict.validTo = weather.forecast.to;
                        }
                    }
                    return weather;
                }

            var service = {
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
                            var merged = mergeWeatherStructure(weather);
                            success(merged);
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
                    }
                };

                return service;
            } ]);
})();
