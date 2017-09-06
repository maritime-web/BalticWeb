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
                    /**
                     * {
                      "parameters": {
                        "wind": true,
                        "current": true,
                        "density": true
                      },
                      "northWest": {
                        "lon": 11.5,
                        "lat": 56.9
                      },
                      "southEast": {
                        "lon": 12.5,
                        "lat": 55.9
                      },
                      "time": "2017-04-28T14:10:00Z",
                       "nx": 10,
                      "ny": 10
                    }
                     */
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
                        "time": time, //"2017-04-19T14:10:00Z"
                      //  "nx": 30,
                      //  "ny": 30

                    }
                };
                return $http(req);
            };


            this.getWeatherOnRoute = function (rtz) {

                var req = {
                    method: 'POST',
                    url: 'https://service-lb.e-navigation.net/weather/rtz?wind=true&current=true&density=false&seaLevel=true',
                    headers: {
                        'Content-Type': 'application/xml;charset=UTF-8',
                        'Accept': 'application/json;charset=UTF-8'
                    },

                    data: rtz
                };
                return $http(req);
            };

        }]);