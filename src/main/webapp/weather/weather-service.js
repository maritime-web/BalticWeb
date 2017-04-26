angular.module('maritimeweb.weather')

    .service('WeatherService', ['$http',
        function ($http) {

            this.getWeather = function (se_lat,  nw_lon,  nw_lat, se_lon, time) {

                var req = {
                    method: 'POST',
                    url: 'https://service-lb.e-navigation.net/weather/grid',
                    headers: {
                        'Content-Type': 'application/json;charset=UTF-8'
                    },
                    data: {

                        "parameters": {
                            "wind": true, //returns in angle & m/sec
                            "current": true, //returns in angle & m/sec
                            "wave": true //returns in angle & height in metres
                        },
                        "northWest": {
                            "lon": nw_lon,
                            "lat": nw_lat
                        },
                        "southEast": {
                            "lon": se_lon,
                            "lat": se_lat
                        },
                        "time": time//"2017-04-19T14:10:00Z"

                    }
                };
                return $http(req);
            };

        }]);