angular.module('maritimeweb.weather')
/**
 * The weather-Layer directive
 */

    .directive('mapWeatherLayer', ['$rootScope', 'WeatherService', 'MapService', '$log', 'growl', '$interval', 'timeAgo', '$filter', 'Auth',
        function ($rootScope, WeatherService, MapService, $log, growl, $interval, timeAgo, $filter, Auth) {
            return {
                restrict: 'E',
                require: '^olMap',
                template: "<form class='map-weather-box'>" +



                "<div> {{time}} </div>" +
                "<div><input type='number' ng-model='hoursOffset' min='0' max='72' /> hours from now</div>" +
                " <div class='btn-group' data-toggle='buttons'>" +
                    "<div class=''>"+
                        "<label>Wind <input type='radio' ng-model='typeForecast.name' value='wind'></label>" +
                    "</div>" +
                    "<div class=''>"+
                        "<label>Density <input type='radio' ng-model='typeForecast.name' value='density'></label>" +
                    "</div>" +
                    "<div class=''>"+
                        "<label>Current <input type='radio' ng-model='typeForecast.name' value='current'></label>" +
                    "</div>" +

                    "<div class=''>"+
                        "<label>Sea level <input type='radio' ng-model='typeForecast.name' value='sealevel'></label>" +
                    "</div>" +
                "</div>" +
                //"  <span class='glyphicon glyphicon-map-marker' ng-click='currentPos()' tooltip='Current Position'></span>" +

                "</span>" +
                "<div>" +
                "<span class='btn btn-primary btn-large' ng-click='getWeatherAreaUI()' tooltip='Get METOC data in area' data-toggle='tooltip' " +
                "data-placement='right' title='Get METOC data in area' > " +
                " Retrieve METOC <i class='fa fa-download' aria-hidden='true'></i></span> information from DMI" +
                "</div>" +
                "<div>" +
                //"<span class='map-weather btn btn-primary btn-large' ng-click='drawLineString()'>line</span>" +
                "<span class=' btn btn-success btn-large' ng-click='animateWeatherOverTime()' tooltip='increase 15 minuttes' data-toggle='tooltip' " +
                "data-placement='right' title='Animate' > Animate <i class='fa fa-play' aria-hidden='true'></i></span> " +
                "</div>" +
                "</form>"

                ,
                scope: {
                    name: '@'
                },
                link: function (scope, element, attrs, ctrl) {

                    var olScope = ctrl.getOpenlayersScope();
                    var serviceAvailableLayer;
                    var metocContentLayer;
                    const top_nw_lon = 56.30;
                    const bottom_se_lon = 54.4;
                    const right_nw_lat = 13.0;
                    const left_se_lat = 10.0;
                    scope.time = new Date();
                    scope.timeAgoString = "";
                    scope.hoursOffset = 0;
                    scope.typeForecast = {
                        name: 'wind'
                    };


                    /*const top_nw_lon = 56.36316;
                     const bottom_se_lon = 54.36294;
                     const right_nw_lat = 13.149009;
                     const left_se_lat = 9.419409;
                     */


                    // #####


                    olScope.getMap().then(function (map) {


                        // Clean up when the layer is destroyed
                        scope.$on('$destroy', function () {
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
                        metocContentLayer = new ol.layer.Vector({
                            title: 'Weather Service Layer',
                            zIndex: 11,
                            source: new ol.source.Vector({
                                features: new ol.Collection(),
                                wrapX: false
                            }),
                            style: [noGoStyleRed]
                        });

                        serviceAvailableLayer = new ol.layer.Vector({
                            title: 'Service Available - Weather',
                            zIndex: 11,
                            source: new ol.source.Vector({
                                features: new ol.Collection(),
                                wrapX: false
                            }),
                            style: [availableServiceStyle]
                        });

                        serviceAvailableLayer.setZIndex(12);
                        serviceAvailableLayer.setVisible(true);
                        serviceAvailableLayer.getSource().clear();


                        metocContentLayer.setZIndex(11);
                        metocContentLayer.setVisible(true);


                        /***************************/
                        /** Map creation          **/
                        /***************************/

                        // Construct No Go Layer Group layer
                        var metocGroupLayer = new ol.layer.Group({
                            title: scope.name || 'No Go Service',
                            zIndex: 11,
                            layers: [metocContentLayer, serviceAvailableLayer]
                        });
                        metocGroupLayer.setZIndex(11);
                        metocGroupLayer.setVisible(true);

                        map.addLayer(metocGroupLayer);

                        var scale = 1;


                        var startPt = new ol.geom.Point(0, 0).transform('EPSG:4326', 'EPSG:3857');
                        var endPt = new ol.geom.Point(20, 75).transform('EPSG:4326', 'EPSG:3857');
                        var style = new ol.style.Style({
                            stroke: new ol.style.Stroke({
                                color: 'rgba(0, 255, 10, 0.8)',
                                width: 60
                            })
                        });
//style


                        scope.drawAnimatedLine = function (startPt, endPt, style, steps, time, fn) {


                            var line = new ol.geom.LineString([startPt, endPt]);
                            //var fea = new ol.feature(line); //line, {}, style);
                            var feature = new ol.Feature({
                                geometry: line,
                                finished: false
                            });
                            //feature.setStyle(style);
                            var featureArray = [feature];
                            //boundaryLayer.getSource().addFeatures(featureArray);
                            serviceAvailableLayer.getSource().addFeature(feature);

                            /*
                             var directionX = (endPt.x - startPt.x) / steps;
                             var directionY = (endPt.y - startPt.y) / steps;
                             var i = 0;
                             var prevLayer;
                             var ivlDraw = setInterval(function () {
                             if (i > steps) {
                             clearInterval(ivlDraw);
                             if (fn) fn();
                             return;
                             }
                             var newEndPt = new ol.geom.Point(startPt.x + i * directionX, startPt.y + i * directionY);
                             var line = new ol.geom.LineString([startPt, newEndPt]);
                             var fea = new ol.Feature({geometry: line, labelPoint: newEndPt, name : 'animated'}); //line, {}, style);
                             fea.setStyle(style);
                             //var vec = new OpenLayers.Layer.Vector();

                             boundaryLayer.getSource().addFeatures([fea]);
                             //map.addLayer(vec);
                             if(prevLayer) map.removeLayer(prevLayer);
                             prevLayer = vec;
                             i++;
                             }, time / steps);
                             */
                        };

                        scope.drawAnimatedLine(new ol.geom.Point([11, 55]).transform('EPSG:4326', 'EPSG:900913'),
                            new ol.geom.Point([16, 65]).transform('EPSG:4326', 'EPSG:900913'),
                            style, 50, 2000, null);


                        /** get the current bounding box in Bottom left  Top right format. */
                        scope.clientBBOXAndServiceLimit = function () {
                            var bounds = map.getView().calculateExtent(map.getSize());
                            var extent = ol.proj.transformExtent(bounds, MapService.featureProjection(), MapService.dataProjection());
                            var l = Math.floor(extent[0] * 100) / 100;
                            var b = Math.floor(extent[1] * 100) / 100;
                            var r = Math.ceil(extent[2] * 100) / 100;
                            var t = Math.ceil(extent[3] * 100) / 100;


                            if (l < left_se_lat) {
                                l = left_se_lat;
                            }
                            if (b < bottom_se_lon) {
                                b = bottom_se_lon;
                            }
                            if (r > right_nw_lat) {
                                r = right_nw_lat;
                            }
                            if (t > top_nw_lon) {
                                t = top_nw_lon;
                            }

                            // hard coded service limitations...
                            /*      if(l < 9.419409) {l = 9.419410;}
                             if(b < 54.36294) { b = 54.36294;}
                             if(r >  13.149009){ r =  13.149010;}
                             if(t > 56.36316) { t = 56.36326;}*/
                            return [b, l, t, r];
                        };

                        /** Create a waypoint feature, with  lat,lon,. */
                        scope.createFeature = function () {
                            var markerStyle = new ol.style.Style({
                                image: new ol.style.Stroke({
                                    color: 'red',
                                    width: 20
                                })

                            });

                            var waypointPositionStart = new ol.geom.Point(ol.proj.transform([parseFloat(10.0), parseFloat(55.0)], 'EPSG:4326', 'EPSG:900913'));
                            var waypointPositionEnd = new ol.geom.Point(ol.proj.transform([parseFloat(10.0), parseFloat(55.0)], 'EPSG:4326', 'EPSG:900913'));

                            var markWaypoint = this.createOpenLayerFeature(waypointPosition, waypoint);
                            markWaypoint.setId(waypoint.id);
                            markWaypoint.setStyle(markerStyle);
                            return markWaypoint;
                        };

                        scope.drawLineString = function () {
                            console.log("draw linestring");
                            try {
                                //var coordinates = [[0, 0], [0, 5088000], [3330000, 3330000],  [3333300, 0], [0,0]];
                                var coordinates = [
                                    ol.proj.transform([11, 65], 'EPSG:4326', 'EPSG:3857'),
                                    ol.proj.transform([22, 55], 'EPSG:4326', 'EPSG:3857'),
                                    ol.proj.transform([11, 51], 'EPSG:4326', 'EPSG:3857'),
                                    ol.proj.transform([8, 52], 'EPSG:4326', 'EPSG:3857'),
                                    ol.proj.transform([10, 55], 'EPSG:4326', 'EPSG:3857')
                                ];

                                /*
                                 var line = new ol.geom.LineString([
                                 new ol.geom.Point([55, 11]).transform('EPSG:4326', 'EPSG:3857'),
                                 new ol.geom.Point([11, 55]).transform('EPSG:4326', 'EPSG:3857'),
                                 new ol.geom.Point([16, 65]).transform('EPSG:4326', 'EPSG:3857')
                                 ]); */
                                var line = new ol.geom.LineString(coordinates);
                                //var fea = new ol.feature(line); //line, {}, style);
                                var feature = new ol.Feature({
                                    geometry: line,
                                    finished: false
                                });
                                //feature.setStyle(style);
                                //var featureArray = [feature];
                                serviceAvailableLayer.setVisible(true);
                                serviceAvailableLayer.getSource().clear();
                                serviceAvailableLayer.getSource().addFeature(feature);
                                //var coordinates = [[0, 0], [0, 5088000], [3330000, 3330000],  [3333300, 0], [0,0]];
                                /*                     var coordinates = [
                                 ol.proj.transform([ 11, 65],'EPSG:4326', 'EPSG:3857'),
                                 ol.proj.transform([ 22, 55],'EPSG:4326', 'EPSG:3857'),
                                 ol.proj.transform([11 , 51],'EPSG:4326', 'EPSG:3857'),
                                 ol.proj.transform([8 , 52],'EPSG:4326', 'EPSG:3857'),
                                 ol.proj.transform([10 , 55],'EPSG:4326', 'EPSG:3857')
                                 ];

                                 var layerLines = new ol.layer.Vector({
                                 source: new ol.source.Vector({
                                 features: [new ol.Feature({
                                 geometry: new ol.geom.LineString(coordinates),
                                 name: 'Line'
                                 })]
                                 })
                                 });

                                 map.addLayer(layerLines);*/
                            } catch (error) {
                                $log.error("Error displaying Service Available boundary");
                            }
                        };

                        scope.drawServiceLimitation = function () {
                            try {
                                var olServiceActiveArea = MapService.wktToOlFeature('POLYGON(('
                                    + left_se_lat + ' ' + bottom_se_lon + ',  '
                                    + right_nw_lat + ' ' + bottom_se_lon + ', '
                                    + right_nw_lat + ' ' + top_nw_lon + ', '
                                    + left_se_lat + ' ' + top_nw_lon + ', '
                                    + left_se_lat + ' ' + bottom_se_lon + '))');
                                serviceAvailableLayer.getSource().addFeature(olServiceActiveArea);
                            } catch (error) {
                                $log.error("Error displaying Service Available boundary");
                            }
                        };

                        scope.getNextNoGoArea = function () {
                            if (!scope.time) {
                                scope.time = new Date();
                            }
                            scope.time.setHours(scope.time.getHours() + 1);
                            scope.getNoGoArea(scope.time);
                        };

                        scope.animateIncreaseTime = function () {

                            if (!scope.hoursOffset) {

                                scope.hoursOffset = 0;
                            } else if ( scope.hoursOffset > 72){
                                scope.hoursOffset = 0;
                            }
                            // scope.time.setHours(scope.time.getHours() + 0.25);
                            scope.hoursOffset += 1;
                            scope.getWeatherAreaUI();
                        }

                        scope.animateWeatherOverTime = function () {
                            $log.info("animateWeatherOverTime - doGruntAnimation");
                            $interval(scope.animateIncreaseTime, 2200, 72);
                        }

                        scope.getWeatherAreaUI = function () {
                            //scope.time = new Date();
                            console.log("getWeatherAreaUI " + scope.hoursOffset + " " + scope.time + " typeForecast=" + scope.typeForecast.name);
                            if (scope.typeForecast.name === "current") {
                                scope.getWeatherInArea(scope.time, scope.hoursOffset, false, true, false, false);
                            }
                            else if (scope.typeForecast.name === "wind") {
                                scope.getWeatherInArea(scope.time, scope.hoursOffset, true, false, false, false);
                            }
                            else if (scope.typeForecast.name === "density") {
                                scope.getWeatherInArea(scope.time, scope.hoursOffset, false, false, true, false);
                            }
                            else if (scope.typeForecast.name === "sealevel") {
                                scope.getWeatherInArea(scope.time, scope.hoursOffset, false, false, false, true);
                            } else {
                                scope.getWeatherInArea(scope.time, scope.hoursOffset, true, true, true, true);
                            }

                        };

                        scope.findWeatherIcon = function (windstr) {

                            var markerImageNamePath = "img/wind/";


                            //Determine wind marker image to display
                            if (windstr < 1.9) {
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
                            return markerImageNamePath;
                        };


                        scope.getWeatherInArea = function (time, hoursoffset, wind, current, density, sealevel) {
                            //scope.drawServiceLimitation();
                            if (!time) {
                                time = new Date();
                            }

                            if (hoursoffset) {
                                time = new Date();
                                time.setTime(time.getTime() + (hoursoffset * 60 * 60 * 1000));
                                console.log("time " + time);
                            }

                            scope.time = time;
                            console.log("scope.time " + scope.time);

                            var bboxBLTR = scope.clientBBOXAndServiceLimit();
                            var now = time.toISOString();
                            WeatherService.getWeather(bboxBLTR[0], bboxBLTR[1], bboxBLTR[2], bboxBLTR[3], now, wind, current, density, sealevel).then(
                                function (response) {
                                    $log.debug("bboxBLTR=" + bboxBLTR + " Time= " + now + " wind=" + wind + " current=" + current + " density=" + density + " Sea Level " + sealevel);
                                    $log.debug("Status=" + response.status);
                                    $log.debug("Response data: " + response.data.forecastDate);
                                    var features = new Array(response.data.points.length);

                                    if (response.data.points.length > 0) {
                                        try{
                                            metocContentLayer.getSource().clear();
                                        }catch (error){
                                            console.error(error);
                                        }
                                        var i = 0;

                                        response.data.points.forEach(function (weatherObj) {

                                            /*
                                             {
                                             "coordinate": {
                                             "lon": 11.5,
                                             "lat": 55.15
                                             },
                                             "windDirection": 252.4,
                                             "windSpeed": 1.9,
                                             "currentDirection": 143.4,
                                             "currentSpeed": 0.03,
                                             "density": 1009.55
                                             },
                                             */

                                            if (weatherObj.windSpeed && weatherObj.windDirection) {

                                                var markerPosition = new ol.geom.Point(ol.proj.transform([weatherObj.coordinate.lon, weatherObj.coordinate.lat], 'EPSG:4326', 'EPSG:900913'));

                                                var iconlocFeature = new ol.Feature({
                                                    geometry: markerPosition,
                                                    name: 'Wind',
                                                    windStrength: weatherObj.windSpeed,
                                                    windDirection: weatherObj.windDirection,
                                                });

                                                var iconlocStyle = new ol.style.Style({
                                                    image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
                                                        anchor: [0.5, 0.5],
                                                        anchorXUnits: 'fraction',
                                                        anchorYUnits: 'fraction',
                                                        rotation: (weatherObj.windDirection - (180)) * (Math.PI / 180),//degToRad(weatherObj.windDirection),
                                                        rotateWithView: true,
                                                        src: scope.findWeatherIcon(weatherObj.windSpeed)
                                                    })),
                                                    text: new ol.style.Text({
                                                        font: 'bold 10px helvetica,sans-serif',
                                                        text: "" + weatherObj.windSpeed, // + " " + weatherObj.windDirection + "° ",
                                                        offsetX: 2,
                                                        offsetY: 2, //waypointtextoffset * scale,
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
                                                metocContentLayer.getSource().addFeature(iconlocFeature);
                                            }

                                            if (weatherObj.currentSpeed && weatherObj.currentDirection) {

                                                var markerPosition = new ol.geom.Point(ol.proj.transform([weatherObj.coordinate.lon, weatherObj.coordinate.lat], 'EPSG:4326', 'EPSG:900913'));

                                                var iconWatFeature = new ol.Feature({
                                                    geometry: markerPosition,
                                                    name: 'Weather',
                                                    windStrength: weatherObj.windSpeed,
                                                    windDirection: weatherObj.windDirection,
                                                    waterCurrent: weatherObj.currentSpeed,
                                                    waterDirection: weatherObj.currentDirection,
                                                    density: weatherObj.density
                                                });

                                                var iconWatStyle = new ol.style.Style({
                                                    image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
                                                        anchor: [0.5, 0.5],
                                                        anchorXUnits: 'fraction',
                                                        anchorYUnits: 'fraction',
                                                        rotation: (weatherObj.currentDirection ) * (Math.PI / 180),//degToRad(weatherObj.windDirection),
                                                        rotateWithView: true,
                                                        src: "img/wave/mark01.png"
                                                    })),
                                                    text: new ol.style.Text({
                                                        font: 'bold 10px helvetica,sans-serif',
                                                        text: "" + weatherObj.currentSpeed , //+ "water - " + weatherObj.currentDirection + " ", //"° ",
                                                        offsetX: 0,
                                                        offsetY: 0, //waypointtextoffset * scale,
                                                        scale: 1,
                                                        fill: new ol.style.Fill({
                                                            color: '#0000ff'
                                                        }),
                                                        stroke: new ol.style.Stroke({
                                                            color: '#fff',
                                                            width: 1
                                                        })
                                                    })
                                                });


                                                iconWatFeature.setStyle(iconWatStyle);
                                                metocContentLayer.getSource().addFeature(iconWatFeature);
                                            }
                                            if (weatherObj.density) {
                                                // var markerPosition = new ol.geom.Point(ol.proj.transform([11, 55]), 'EPSG:4326', 'EPSG:900913');
                                                var markerPosition = new ol.geom.Point(ol.proj.transform([weatherObj.coordinate.lon, weatherObj.coordinate.lat], 'EPSG:4326', 'EPSG:900913'));

                                                var iconDensFeature = new ol.Feature({
                                                    geometry: markerPosition,
                                                    name: 'Density',
                                                    density: weatherObj.density
                                                });

                                                var iconDensStyle = new ol.style.Style({

                                                    text: new ol.style.Text({
                                                        font: 'bold 12px helvetica,sans-serif',
                                                        text:  ""+weatherObj.density,
                                                        offsetX: 0,
                                                        offsetY: 0, //waypointtextoffset * scale,
                                                        scale: 1,
                                                        fill: new ol.style.Fill({
                                                            color: '#0000ff'
                                                        }),
                                                        stroke: new ol.style.Stroke({
                                                            color: '#fff',
                                                            width: 1
                                                        })
                                                    })
                                                });

                                                iconDensFeature.setStyle(iconDensStyle);
                                                metocContentLayer.getSource().addFeature(iconDensFeature);
                                            }

                                            if (weatherObj.seaLevel) {
                                                // var markerPosition = new ol.geom.Point(ol.proj.transform([11, 55]), 'EPSG:4326', 'EPSG:900913');
                                                var markerPosition = new ol.geom.Point(ol.proj.transform([weatherObj.coordinate.lon, weatherObj.coordinate.lat], 'EPSG:4326', 'EPSG:900913'));

                                                var iconSeaLevelFeature = new ol.Feature({
                                                    geometry: markerPosition,
                                                    name: 'SeaLevel',
                                                    density: weatherObj.seaLevel
                                                });

                                                var iconSeaLevelStyle = new ol.style.Style({

                                                    text: new ol.style.Text({
                                                        font: 'bold 12px helvetica,sans-serif',
                                                        text:  ""+weatherObj.seaLevel,
                                                        offsetX: 0,
                                                        offsetY: 0, //waypointtextoffset * scale,
                                                        scale: 1,
                                                        fill: new ol.style.Fill({
                                                            color: '#0000ff'
                                                        }),
                                                        stroke: new ol.style.Stroke({
                                                            color: '#fff',
                                                            width: 1
                                                        })
                                                    })
                                                });

                                                iconSeaLevelFeature.setStyle(iconSeaLevelStyle);
                                                metocContentLayer.getSource().addFeature(iconSeaLevelFeature);
                                            }


                                            i++;
                                        })
                                    }
                                }, function (error) {
                                    metocContentLayer.getSource().clear();
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


                    //Weather On Route Marker (WORM) generator
                    //var WORMWaveparams = { text: '2,5', rot: -135, anchor: [0.52, 0.25] };
                    var retWORMWaveStyle = function (scale, wavedir, wavestr) {
                        if (!scale) scale = 1;
                        if (!wavedir) wavedir = 180;
                        if (!wavestr) wavestr = 0;
                        var WORMWaveStyleReturn = new ol.style.Style({
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
                        return WORMWaveStyleReturn;
                    };

                    //var WORMCurrentparams = { text: '2', rot: -135, anchor:  };
                    var retWORMCurrentStyle = function (scale, currdir, currstr) {
                        if (!scale) scale = 1;
                        if (!currdir) currdir = 180;
                        if (!currstr) currstr = 0;
                        var WORMCurrentStyleReturn = new ol.style.Style({
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
                        return WORMCurrentStyleReturn;
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

                    var calcSinCosFromAngle = function (xy, angle, radius) { //requires ('x' or 'y'), angle in degrees and radius in px.
                        var SinCos;
                        if (xy == 'x') SinCos = radius * Math.cos(angle); // Calculate the x position of the element.
                        if (xy == 'y') SinCos = radius * Math.sin(angle); // Calculate the y position of the element.
                        return SinCos;
                    }

                    // convert degrees to radians
                    var degToRad = function (deg) {
                        return deg * Math.PI * 2 / 360;
                    }
                    // convert radians to degrees
                    var radToDeg = function (rad) {
                        return rad * 360 / (Math.PI * 2);
                    }


                    var WORMarkers = []; //array for multiple markers along route
                    var generateWORM = function (identifier, type, lon, lat, scale, winddir, windstr, currdir, currstr, wavedir, wavestr) { //type is given so it can be styled, identifier must be unique.
                        if (!lon || !lat) {
                            lon = 0;
                            lat = 0;
                        }
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


                        if (!waveheight || waveheight == 0) waveheight = "";
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
                    };

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
                                opacity: (currstr != "") ? 1 : 0,
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
                        if (windstr < 1.9) {
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
                        if (!lon || !lat) {
                            lon = 0;
                            lat = 0;
                        }

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
                            src: 'img/wind/mark005.png'
                        });
                        iconFeature3.setStyle(retWORMWindStyle(scale, winddir, windstr, markertext, wavedir));
                        iconFeature3.setId(type + '_windmarker');

                        return [iconFeature, iconFeature2, iconFeature3];
                    };


                    var retLoadingIconStyle = function () {
                        var WORMLoadinStyle = new ol.style.Style({
                            image: new ol.style.Icon(({
                                opacity: 1,
                                scale: 0.3,
                                anchor: [(0.5), (0.5)],
                                anchorXUnits: 'fraction',
                                anchorYUnits: 'fraction',
                                src: 'img/loadingicon.png',
                            }))
                        });
                        return WORMLoadinStyle;
                    };


                    var retLoadingIcon = function (lonlat) {
                        var iconFeature = new ol.Feature({
                            geometry: new ol.geom.Point(lonlat).transform('EPSG:4326', 'EPSG:3857'),
                            name: 'WOR_loadingicon',
                            src: 'img/loading.gif',
                        });
                        iconFeature.setStyle(retLoadingIconStyle()); //generated style
                        iconFeature.setId("WORMLoadingIcon");
                        return [iconFeature];

                    };

                }
            };
        }

    ]);