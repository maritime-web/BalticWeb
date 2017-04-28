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
                        "nx": 10,
                        "ny": 10

                    }
                };
                return $http(req);
            };

        }])
    /**
     * The map-no-Go-Area-Layer directive
     */

.directive('mapWeatherLayer', ['$rootScope', 'WeatherService', 'MapService', '$log', 'growl', '$interval', 'timeAgo', '$filter', 'Auth',
    function($rootScope, WeatherService, MapService, $log, growl, $interval, timeAgo, $filter, Auth){
        return {
            restrict: 'E',
            require: '^olMap',
            template:
            "<span class='map-weather-btn col-lg-3 hidden-xs hidden-sm' >" +
                '<button type="button" class="btn btn-default" ng-click="getWeatherAreaUI()">W</button>' +
            "</span>",
            scope: {
                name:           '@'
            },
            link: function(scope, element, attrs, ctrl) {

                var olScope = ctrl.getOpenlayersScope();
                var weatherLayer;
                var weatherGroupLayer;
                var serviceAvailableLayer;
                var boundaryLayer;
                const top_nw_lon = 56.30;
                const bottom_se_lon = 54.4;
                const right_nw_lat = 13.0;
                const left_se_lat = 10.0;
                scope.time = new Date();
                scope.timeAgoString = "";

                /*const top_nw_lon = 56.36316;
                 const bottom_se_lon = 54.36294;
                 const right_nw_lat = 13.149009;
                 const left_se_lat = 9.419409;
                 */

                olScope.getMap().then(function(map) {


                    // Clean up when the layer is destroyed
                    scope.$on('$destroy', function() {
                        if (angular.isDefined(noGoLayer)) {
                            map.removeLayer(noGoLayer);
                        }
                    });

                    /***************************/
                    /** noGoLayer Layers      **/
                    /***************************/


                    var noGoStyleRed = new ol.style.Style({
                        stroke: new ol.style.Stroke({
                            color: 'rgba(255, 0, 10, 0.5)',
                            width: 1
                        }),
                        fill: new ol.style.Fill({
                            color: 'rgba(255, 0, 10, 0.10)'
                        })
                    });
                    var availableServiceStyle = new ol.style.Style({
                        stroke: new ol.style.Stroke({
                            color: 'rgba(0, 255, 10, 0.8)',
                            width: 3
                        })
                    });


                    // Construct the boundary layers
                    boundaryLayer = new ol.layer.Vector({
                        title: 'Weather Service Layer',
                        zIndex: 11,
                        source: new ol.source.Vector({
                            features: new ol.Collection(),
                            wrapX: false
                        }),
                        style: [ noGoStyleRed ]
                    });

                    serviceAvailableLayer  = new ol.layer.Vector({
                        title: 'Service Available - Weather',
                        zIndex: 11,
                        source: new ol.source.Vector({
                            features: new ol.Collection(),
                            wrapX: false
                        }),
                        style: [ availableServiceStyle ]
                    });

                    serviceAvailableLayer.setZIndex(12);
                    serviceAvailableLayer.setVisible(true);
                    serviceAvailableLayer.getSource().clear();


                    boundaryLayer.setZIndex(11);
                    boundaryLayer.setVisible(true);


                    /***************************/
                    /** Map creation          **/
                    /***************************/

                    // Construct No Go Layer Group layer
                    var noGoGroupLayer = new ol.layer.Group({
                        title: scope.name || 'No Go Service',
                        zIndex: 11,
                        layers: [ boundaryLayer, serviceAvailableLayer]
                    });
                    noGoGroupLayer.setZIndex(11);
                    noGoGroupLayer.setVisible(true);

                    map.addLayer(noGoGroupLayer);

                    /** get the current bounding box in Bottom left  Top right format. */
                    scope.clientBBOXAndServiceLimit = function () {
                        var bounds = map.getView().calculateExtent(map.getSize());
                        var extent = ol.proj.transformExtent(bounds, MapService.featureProjection(), MapService.dataProjection());
                        var l = Math.floor(extent[0] * 100) / 100;
                        var b = Math.floor(extent[1] * 100) / 100;
                        var r = Math.ceil(extent[2] * 100) / 100;
                        var t = Math.ceil(extent[3] * 100) / 100;


                        if(l < left_se_lat) {l= left_se_lat;}
                        if(b < bottom_se_lon) { b = bottom_se_lon;}
                        if(r >  right_nw_lat){ r =  right_nw_lat;}
                        if(t > top_nw_lon) { t = top_nw_lon;}

                        // hard coded service limitations...
                        /*      if(l < 9.419409) {l = 9.419410;}
                         if(b < 54.36294) { b = 54.36294;}
                         if(r >  13.149009){ r =  13.149010;}
                         if(t > 56.36316) { t = 56.36326;}*/
                        return [b , l , t , r ];
                    };


                    scope.drawServiceLimitation = function() {
                        try {
                            var olServiceActiveArea = MapService.wktToOlFeature('POLYGON(('
                                + left_se_lat + ' ' + bottom_se_lon + ',  '
                                + right_nw_lat + ' ' + bottom_se_lon +', '
                                + right_nw_lat + ' ' + top_nw_lon + ', '
                                + left_se_lat + ' ' + top_nw_lon + ', '
                                + left_se_lat +' ' + bottom_se_lon + '))');
                            serviceAvailableLayer.getSource().addFeature(olServiceActiveArea);
                        } catch (error) {
                            $log.error("Error displaying Service Available boundary");
                        }
                    };

                    scope.getNextNoGoArea = function(){
                        if(!scope.time){
                            scope.time = new Date();
                        }
                        scope.time.setHours(scope.time.getHours() + 1);
                        scope.getNoGoArea(scope.time);
                    };


                    scope.doGruntAnimation = function(){
                        $log.info("doGruntAnimation");
                        $interval(scope.getNextNoGoArea, 2200, 8);
                    };

                    scope.doFakeGruntAnimation = function(){
                        $log.info("doIncreaseDraughtAnimation");
                        $interval(scope.getNextNoGoAreaIncreaseDraught, 2200, 8);
                    };

                    scope.getWeatherAreaUI = function(){
                        scope.time = new Date();
                        scope.getWeatherInArea(scope.time);
                    };

                    scope.getWeatherInArea = function(time){
                        scope.drawServiceLimitation();
                        if(!time){
                            time = new Date();
                        }

                        scope.time = time;

                        var bboxBLTR = scope.clientBBOXAndServiceLimit();
                        var now = time.toISOString();
                        WeatherService.getWeather( bboxBLTR[0],bboxBLTR[1],bboxBLTR[2],bboxBLTR[3], now).then(
                            function(response) {
                                $log.debug("bboxBLTR=" +bboxBLTR + " Time= " + now);
                                $log.debug("Status=" + response.status);
                                $log.debug("Response data: " + response.data);
                                $log.debug("Response data: " + response.data.forecastDate);
                                $log.debug("Response points: " + response.data.points);
                                boundaryLayer.getSource().clear();
/*
                                var olFeature = MapService.wktToOlFeature(response.data.wkt);
                                boundaryLayer.getSource().addFeature(olFeature);
                                scope.timeAgoString = $filter('timeAgo')(scope.time);
                                growl.info("No-Go zone retrieved and marked with red. <br> "
                                    + scope.ship_draught + " meters draught.<br>"
                                    + timeAgoString + " <br> "+ scope.time.toISOString());*/
                            }, function(error) {
                                boundaryLayer.getSource().clear();
                                $log.error(error);
                                if(error.data.message){
                                    growl.error(error.data.message);
                                }

                            });

                    };

                    scope.loggedIn = Auth.loggedIn;

                    scope.login = function () {
                        Auth.authz.login();
                    };

                });
            }
        };
    }

    ]);