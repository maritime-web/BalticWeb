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

                //Weather On Route Marker (WORM) generator
                //var WORMWaveparams = { text: '2,5', rot: -135, anchor: [0.52, 0.25] };
                var retWORMWaveStyle = function (scale, wavedir, wavestr) {
                    if (!scale) scale = 1;
                    if (!wavedir) wavedir = 180;
                    if (!wavestr) wavestr = 0;
                    var WORMWaveStyle = new ol.style.Style({
                        image: new ol.style.Icon({
                            opacity: 0.75,
                            rotation: degToRad(wavedir), //wavepointer is pointing lowerright
                            anchor: [(0.5), (0.5)],
                            anchorXUnits: 'fraction',
                            anchorYUnits: 'fraction',
                            src: 'img/WOR_backdropcircle.png', //needs path
                            scale: (0.5 * scale)
                        }),
                        text: new ol.style.Text({
                            font: '12px helvetica,sans-serif',
                            text: ('' + wavestr),
                            offsetX: calcSinCosFromAngle('x', wavedir, (36 * scale)),
                            offsetY: calcSinCosFromAngle('y', wavedir, (36 * scale)),
                            scale: (1 * scale),
                            //rotation: 360 * rnd * Math.PI / 180,
                            fill: new ol.style.Fill({
                                color: '#000'
                            }),
                            stroke: new ol.style.Stroke({
                                color: '#fff',
                                width: 1
                            })
                        })
                    });
                    return WORMWaveStyle;
                };

                //var WORMCurrentparams = { text: '2', rot: -135, anchor:  };
                var retWORMCurrentStyle = function (scale, currdir, currstr) {
                    if (!scale) scale = 1;
                    if (!currdir) currdir = 180;
                    if (!currstr) currstr = 0;
                    var WORMCurrentStyle = new ol.style.Style({
                        image: new ol.style.Icon({
                            opacity: 1,
                            rotation: degToRad(currdir), //currentpointer is pointing lowerright
                            anchor: [0.5, 0.5],
                            anchorXUnits: 'fraction',
                            anchorYUnits: 'fraction',
                            src: 'img/WOR_innercircle.png', //needs path
                            scale: (0.5 * scale)
                        }),
                        text: new ol.style.Text({
                            font: '10px helvetica,sans-serif',
                            text: ('' + currstr),
                            offsetX: calcSinCosFromAngle('x', currdir, (22 * scale)),
                            offsetY: calcSinCosFromAngle('y', currdir, (22 * scale)),
                            scale: (1 * scale),
                            //rotation: 360 * rnd * Math.PI / 180,
                            fill: new ol.style.Fill({
                                color: '#000'
                            }),
                            stroke: new ol.style.Stroke({
                                color: '#fff',
                                width: 1
                            })
                        })
                    });
                    return WORMCurrentStyle;
                };

                //var WORMWindparams = { rot: -135, anchor: [0.5, 0.5] };
                var retWORMWindStyle = function (scale, winddir, windstr) {
                    if (!scale) scale = 1;
                    if (!winddir) winddir = 180;
                    var WORMWindStyle = new ol.style.Style({
                        image: new ol.style.Icon(({
                            opacity: 1,
                            rotation: degToRad(winddir), //windpointer is straight is pointing straight down
                            anchor: [(0.52), (0.25)],
                            anchorXUnits: 'fraction',
                            anchorYUnits: 'fraction',
                            src: 'img/wind/mark005.png', //needs path and windstr to paint correct arrow
                            scale: (0.70 * scale)
                        }))
                    });
                    return WORMWindStyle;
                };

                var calcSinCosFromAngle = function(xy, angle, radius) { //requires ('x' or 'y'), angle in degrees and radius in px.
                    var SinCos;
                    if (xy == 'x') SinCos = radius * Math.cos(angle); // Calculate the x position of the element.
                    if (xy == 'y') SinCos = radius * Math.sin(angle); // Calculate the y position of the element.
                    return SinCos;
                }

                // convert degrees to radians
                var degToRad = function  (deg) {
                    return deg * Math.PI * 2 / 360;
                }
                // convert radians to degrees
                var radToDeg = function (rad) {
                    return rad * 360 / (Math.PI * 2);
                }


                var WORMarkers = []; //array for multiple markers along route
                var generateWORM = function (identifier, type, lon, lat, scale, winddir, windstr, currdir, currstr, wavedir, wavestr) { //type is given so it can be styled, identifier must be unique.
                    if (!lon || !lat) { lon = 0; lat = 0; }
                    var iconFeature = new ol.Feature({ //WAVEARROW
                        geometry: new ol.geom.Point([lon, lat]).transform('EPSG:4326', 'EPSG:3857'),
                        name: 'WOR_wavemarker',
                        type: type,
                        src: 'img/WOR_backdropcircle.png',
                    });
                    iconFeature.setStyle(retWORMWaveStyle(scale, wavedir, wavestr)); //generated style
                    iconFeature.setId(type + 'WOR_wavemarker');
                    //CURRENTARROW
                    var iconFeature2 = new ol.Feature({
                        geometry: new ol.geom.Point([lon, lat]).transform('EPSG:4326', 'EPSG:3857'),
                        name: 'WOR_currentmarker',
                        type: type,
                        src: 'img/WOR_innercircle.png',
                    });
                    iconFeature2.setStyle(retWORMCurrentStyle(scale, currdir, currstr));
                    iconFeature2.setId(type + 'WOR_currentmarker');
                    //WINDARROW
                    var iconFeature3 = new ol.Feature({
                        geometry: new ol.geom.Point([lon, lat]).transform('EPSG:4326', 'EPSG:3857'),
                        name: 'WOR_windmarker',
                        type: type,
                        src: 'img/wind/mark005.png',
                    });
                    iconFeature3.setStyle(retWORMWindStyle(scale, winddir, windstr));
                    iconFeature3.setId(type + 'WOR_windmarker');
                    return [iconFeature, iconFeature2, iconFeature3];
                };



                // #####

                //Weather On Route Marker (WORM) generator
                var retWORMWaveStyle = function (scale, wavedir, waveheight, markertext) {
                    if (!scale) scale = 1;
                    if (!wavedir) wavedir = 180;
                    wavedir += 45; //offset for icon
                    var useimage = (waveheight == "") ? 'img/WOR_backdropcircle_nowave.png' : 'img/WOR_backdropcircle.png';
                    if (markertext == "nodata") useimage = "img/WOR_nodata.png";


                    if (!waveheight || waveheight==0) waveheight = "";
                    var radOff = 0.47; //text offset in radians for current and wave indicator
                    var WORMWaveStyle = new ol.style.Style({
                        zIndex: 50,
                        image: new ol.style.Icon({
                            opacity: 0.75,
                            rotation: degToRad(wavedir), //wavepointer is pointing lowerright
                            anchor: [(0.5), (0.5)],
                            anchorXUnits: 'fraction',
                            anchorYUnits: 'fraction',
                            src: useimage,
                            scale: (0.5 * scale)
                        }),
                        text: new ol.style.Text({
                            font: '12px helvetica,sans-serif',
                            text: ('' + waveheight),
                            offsetX: calcSinCosFromAngle('x', degToRad(wavedir) + radOff, (40 * scale)),
                            offsetY: calcSinCosFromAngle('y', degToRad(wavedir) + radOff, (40 * scale)),
                            scale: (1 * scale),
                            fill: new ol.style.Fill({
                                color: '#000'
                            }),
                            stroke: new ol.style.Stroke({
                                color: '#fff',
                                width: 1
                            })
                        })
                    });
                    return WORMWaveStyle;
                }

                var retWORMCurrentStyle = function (scale, currdir, currstr, markertext) {
                    if (!scale) scale = 1;
                    if (!currdir) currdir = 180;
                    currdir += 45; //offset for icon
                    (!currstr) ? currstr = "" : currstr * 1.9438444924574; // make "" if nothing, or meter/sec to knots.
                    var useimage = 'img/WOR_innercircle.png';
                    if (markertext == "nodata") useimage = "img/emptyimage.png";

                    if (!currstr || currstr == 0) currstr = "";
                    var radOff = 0.25; //text offset in radians for current and wave indicator
                    var WORMCurrentStyle = new ol.style.Style({
                        zIndex: 51,
                        image: new ol.style.Icon({
                            opacity: (currstr!="")?1:0,
                            rotation: degToRad(currdir), //currentpointer is pointing lowerright
                            anchor: [0.5, 0.5],
                            anchorXUnits: 'fraction',
                            anchorYUnits: 'fraction',
                            src: useimage, //needs path
                            scale: (0.5 * scale)
                        }),
                        text: new ol.style.Text({
                            font: '10px helvetica,sans-serif',
                            text: ('' + currstr),
                            offsetX: calcSinCosFromAngle('x', degToRad(currdir) + radOff, (18 * scale)),
                            offsetY: calcSinCosFromAngle('y', degToRad(currdir) + radOff, (18 * scale)),
                            scale: (1 * scale),
                            fill: new ol.style.Fill({
                                color: '#000'
                            }),
                            stroke: new ol.style.Stroke({
                                color: '#fff',
                                width: 1
                            })
                        })
                    });
                    return WORMCurrentStyle;
                }


                var retWORMWindStyle = function (scale, winddir, windstr, markertext, wavedir) { //windstr is m/s - wavedir is needed to make offset greater if pointing south so text doesnt overlap.
                    if (!scale) scale = 1;
                    var waypointtextoffset = 46;
                    if (!winddir) winddir = 180; //default north
                    (!windstr) ? windstr = 1 : windstr * 1.9438444924574; // make 1 knot if nothing, or meter/sec to knots.
                    var markerImageNamePath = "img/wind/";


                    //Determine wind marker image to display
                    if (windstr < 1.9){
                        markerImageNamePath += 'mark000.png';
                    } else if (windstr >= 2 && windstr < 7.5) {
                        markerImageNamePath += 'mark005.png';
                    } else if (windstr >= 7.5 && windstr < 12.5) {
                        markerImageNamePath += 'mark010.png';
                    } else if (windstr >= 12.5 && windstr < 17.5) {
                        markerImageNamePath += 'mark015.png';
                    } else if (windstr >= 17.5 && windstr < 22.5) {
                        markerImageNamePath += 'mark020.png';
                    } else if (windstr >= 22.5 && windstr < 27.5) {
                        markerImageNamePath += 'mark025.png';
                    } else if (windstr >= 27.5 && windstr < 32.5) {
                        markerImageNamePath += 'mark030.png';
                    } else if (windstr >= 32.5 && windstr < 37.5) {
                        markerImageNamePath += 'mark035.png';
                    } else if (windstr >= 37.5 && windstr < 42.5) {
                        markerImageNamePath += 'mark040.png';
                    } else if (windstr >= 42.5 && windstr < 47.5) {
                        markerImageNamePath += 'mark045.png';
                    } else if (windstr >= 47.5 && windstr < 52.5) {
                        markerImageNamePath += 'mark050.png';
                    } else if (windstr >= 52.5 && windstr < 57.5) {
                        markerImageNamePath += 'mark055.png';
                    } else if (windstr >= 57.5 && windstr < 62.5) {
                        markerImageNamePath += 'mark060.png';
                    } else if (windstr >= 62.5 && windstr < 67.5) {
                        markerImageNamePath += 'mark065.png';
                    } else if (windstr >= 67.5 && windstr < 72.5) {
                        markerImageNamePath += 'mark070.png';
                    } else if (windstr >= 72.5 && windstr < 77.5) {
                        markerImageNamePath += 'mark075.png';
                    } else if (windstr >= 77.5 && windstr < 82.5) {
                        markerImageNamePath += 'mark080.png';
                    } else if (windstr >= 82.5 && windstr < 87.5) {
                        markerImageNamePath += 'mark085.png';
                    } else if (windstr >= 87.5 && windstr < 92.5) {
                        markerImageNamePath += 'mark090.png';
                    } else if (windstr >= 92.5 && windstr < 97.5) {
                        markerImageNamePath += 'mark095.png';
                    } else if (windstr >= 97.5) {
                        markerImageNamePath += 'mark100.png';
                    }

                    //move the text a bit lower if the wavearrow points down
                    if ((wavedir < 55 && wavedir > 0) || (wavedir > 305)) {
                        waypointtextoffset = 56;
                    }

                    var useimage = markerImageNamePath;
                    if (markertext == "nodata") {
                        useimage = "img/emptyimage.png";
                        markertext = "";
                    }


                    var WORMWindStyle = new ol.style.Style({
                        zIndex: 52,
                        image: new ol.style.Icon(({
                            opacity: 1,
                            rotation: degToRad(winddir), //windpointer is straight is pointing straight down
                            anchor: [(0.52), (0.25)],
                            anchorXUnits: 'fraction',
                            anchorYUnits: 'fraction',
                            src: useimage, //needs path and windstr to paint correct arrow
                            scale: (0.80 * scale)
                        })),
                        text: new ol.style.Text({
                            font: 'bold 12px helvetica,sans-serif',
                            text: "" + markertext,
                            offsetX: 0,
                            offsetY: waypointtextoffset * scale,
                            scale: (1 * scale),
                            fill: new ol.style.Fill({
                                color: '#000'
                            }),
                            stroke: new ol.style.Stroke({
                                color: '#fff',
                                width: 1
                            })
                        })
                    });
                    return WORMWindStyle;
                }



//creates a weather marker
                var generateWORM = function (identifier, type, lon, lat, scale, winddir, windstr, currdir, currstr, wavedir, waveheight, markertext) { //type is given so it can be styled.
                    if (!lon || !lat) { lon = 0; lat = 0; }

                    //display errormarker if no data
                    if (winddir == 0 && windstr == 0 && currdir == 0 && currstr == 0 && wavedir == 0 && waveheight == 0 && markertext == 0) {
                        markertext = "nodata"; //styling takes care of it from here
                    }


                    //WAVEARROW
                    var iconFeature = new ol.Feature({
                        geometry: new ol.geom.Point([lon, lat]).transform('EPSG:4326', 'EPSG:3857'),
                        name: 'WOR_wavemarker',
                        type: type,
                        identifier: identifier,
                        src: 'img/WOR_vessel_backdropcircle.png',
                    });
                    iconFeature.setStyle(retWORMWaveStyle(scale, wavedir, waveheight, markertext)); //change stylíng
                    iconFeature.setId(type + '_wavemarker');

                    //CURRENTARROW
                    var iconFeature2 = new ol.Feature({
                        geometry: new ol.geom.Point([lon, lat]).transform('EPSG:4326', 'EPSG:3857'),
                        name: 'WOR_currentmarker',
                        type: type,
                        identifier: identifier,
                        src: 'img/WOR_innercircle.png',
                    });
                    iconFeature2.setStyle(retWORMCurrentStyle(scale, currdir, currstr, markertext));
                    iconFeature2.setId(type + '_currentmarker');

                    //WINDARROW
                    var iconFeature3 = new ol.Feature({
                        geometry: new ol.geom.Point([lon, lat]).transform('EPSG:4326', 'EPSG:3857'),
                        name: 'WOR_windmarker',
                        type: type,
                        identifier: identifier,
                        src: 'img/wind/mark005.png',
                    });
                    iconFeature3.setStyle(retWORMWindStyle(scale, winddir, windstr, markertext, wavedir));
                    iconFeature3.setId(type + '_windmarker');

                    return [iconFeature, iconFeature2, iconFeature3];
                }




                var retLoadingIconStyle = function () {
                    var WORMLoadinStyle = new ol.style.Style({
                        image: new ol.style.Icon(({
                            opacity: 1,
                            scale:0.3,
                            anchor: [(0.5), (0.5)],
                            anchorXUnits: 'fraction',
                            anchorYUnits: 'fraction',
                            src: 'img/loadingicon.png',
                        }))
                    });
                    return WORMLoadinStyle;
                }



                var retLoadingIcon = function (lonlat) {
                    var iconFeature = new ol.Feature({
                        geometry: new ol.geom.Point(lonlat).transform('EPSG:4326', 'EPSG:3857'),
                        name: 'WOR_loadingicon',
                        src: 'img/loading.gif',
                    });
                    iconFeature.setStyle(retLoadingIconStyle()); //generated style
                    iconFeature.setId("WORMLoadingIcon");
                    return [iconFeature];

                }





                // #####



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

                    var scale = 1;
                    var waypointtextoffset = 46;


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

                    scope.getWeatherInArea = function (time) {
                        scope.drawServiceLimitation();
                        if (!time) {
                            time = new Date();
                        }

                        scope.time = time;

                        var bboxBLTR = scope.clientBBOXAndServiceLimit();
                        var now = time.toISOString();
                        WeatherService.getWeather(bboxBLTR[0], bboxBLTR[1], bboxBLTR[2], bboxBLTR[3], now).then(
                            function (response) {
                                $log.debug("bboxBLTR=" + bboxBLTR + " Time= " + now);
                                $log.debug("Status=" + response.status);
                                $log.debug("Response data: " + response.data);
                                $log.debug("Response data: " + response.data.forecastDate);
                                $log.debug("Response points: " + response.data.points);
                                $log.debug("Response points size: " + response.data.points.length);

                                var features = new Array(response.data.points.length);

                                if (response.data.points.length > 0) {
                                    boundaryLayer.getSource().clear();


                                    var i = 0;
                                    response.data.points.forEach(function (weatherObj) {
                                        if (weatherObj.windSpeed && weatherObj.windDirection) {


                                        /**
                                         *      "windDirection": 100.74,
                                         * "windSpeed": 10.46,
                                         * "currentDirection": 162.57,
                                         * "currentSpeed": 0.43
                                         */

                                            // var markerPosition = new ol.geom.Point(ol.proj.transform([11, 55]), 'EPSG:4326', 'EPSG:900913');
                                        var markerPosition = new ol.geom.Point(ol.proj.transform([weatherObj.coordinate.lon, weatherObj.coordinate.lat], 'EPSG:4326', 'EPSG:900913'));

                                        var iconlocFeature = new ol.Feature({
                                            geometry: markerPosition,
                                            name: 'Weather',
                                            windStrength: weatherObj.windSpeed,
                                            windDirection: weatherObj.windDirection,
                                            waterCurrent: weatherObj.currentSpeed,
                                            waterDirection: weatherObj.currentDirection
                                        });

                                        var iconlocStyle = new ol.style.Style({
                                            image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
                                                anchor: [1, 1],
                                                anchorXUnits: 'fraction',
                                                anchorYUnits: 'pixels',
                                                rotation:  (weatherObj.windDirection - (180)) * (Math.PI / 180),//degToRad(weatherObj.windDirection),
                                                rotateWithView: true,
                                                src: 'img/wind/mark005.png'
                                            })),
                                            text: new ol.style.Text({
                                                font: 'bold 12px helvetica,sans-serif',
                                                text: "" + weatherObj.windSpeed + "k - " + weatherObj.windDirection + "° ",
                                                offsetX: 0,
                                                offsetY: waypointtextoffset * scale,
                                                scale: (1 * scale),
                                                fill: new ol.style.Fill({
                                                    color: '#000'
                                                }),
                                                stroke: new ol.style.Stroke({
                                                    color: '#fff',
                                                    width: 1
                                                })
                                            })
                                        });


                                        iconlocFeature.setStyle(iconlocStyle);
                                        boundaryLayer.getSource().addFeature(iconlocFeature);
                                    }
                                        i++;
                                    })
                                }
                            }, function (error) {
                                boundaryLayer.getSource().clear();
                                $log.error(error);
                                if (error.data.message) {
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