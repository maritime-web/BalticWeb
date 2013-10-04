console.log('before loading metoc layer');

var metocModule = angular.module('embryo.metoc', []);

metocModule.factory('MetocService', function($http) {
    return {
        getMetoc : function(routeId, callback) {
            var messageId = embryo.messagePanel.show({
                text : "Loading metoc ..."
            });

            var url = embryo.baseUrl + 'rest/metoc/' + routeId;

            $http.get(url, {
                responseType : 'json'
            }).success(function(result) {
                embryo.messagePanel.replace(messageId, {
                    text : "Metoc loaded.",
                    type : "success"
                });

                callback(result);
            }).error(function(data, status) {
                embryo.messagePanel.replace(messageId, {
                    text : "Failed loading metoc. Server returned error: " + status,
                    type : "error"
                });
                callback(null);
            });
        }
    };
});
console.log('after');

embryo.metoc = {};

var defaultCurrentLow=1.0;
var defaultCurrentMedium=2.0;
var defaultCurrentWarnLimit=4.0;
var defaultWaveLow=1.0;
var defaultWaveMedium=2.0;
var defaultWaveWarnLimit=3.0;
var defaultWindWarnLimit=10.0;


embryo.metoc.draw = function(metoc) {
    var index, attr, geom, forecast, features = [];
    console.log('drawing metoc');

    console.log(metoc);

    for ( var index in metoc.forecasts) {
        forecast = metoc.forecasts[index];

        if (forecast.waveDir && forecast.waveHeight && forecast.wavePeriod) {
            attr = {
                type : "wave",
                created : metoc.created,
                forecast : forecast
            };
            geom = embryo.map.createPoint(forecast.lon, forecast.lat);
            features.push(new OpenLayers.Feature.Vector(geom, attr));
        }
        if (forecast.windDir && forecast.windSpeed) {
            attr = {
                type : "wind",
                created : metoc.created,
                forecast : forecast
            };
            geom = embryo.map.createPoint(forecast.lon, forecast.lat);
            features.push(new OpenLayers.Feature.Vector(geom, attr));
        }
        if (forecast.curDir && forecast.curSpeed) {
            attr = {
                type : "current",
                created : metoc.created,
                forecast : forecast
            };
            geom = embryo.map.createPoint(forecast.lon, forecast.lat);
            features.push(new OpenLayers.Feature.Vector(geom, attr));
        }
    }

    embryo.metoc.layer.addFeatures(features);
    embryo.metoc.layer.refresh();
};

var groupSelected;

embryo.metoc.initLayer = function() {
    console.log('Metoc layer initialized');

    var context = {
        transparency : function() {
            return groupSelected ? 0.8 : 0.5;
        },
        offset : function(feature) {
            return -context.height(feature) / 2;
        },
        size : function() {
            return 2 * embryo.map.internalMap.zoom / embryo.map.internalMap.numZoomLevels;
        }, 
        width : function(feature) {
            if(feature.attributes.type === 'wave'){
                return context.size() * 27;
            }
            if(feature.attributes.type === 'current'){
                return context.size() * 21;
            }
            if(feature.attributes.type === 'wind'){
                return context.size() * 27;
            }
            return 100;
        }, 
        height : function(feature) {
            if(feature.attributes.type === 'wave'){
                return context.size() * 93;
            }
            if(feature.attributes.type === 'current'){
                return context.size() * 253;
            }
            if(feature.attributes.type === 'wind'){
                return context.size() * 93;
            }
            return 100;
        }, 
        rotation : function(feature){
            if(feature.attributes.type === 'wave'){
                return feature.attributes.forecast.waveDir;
            }
            if(feature.attributes.type === 'current'){
                return feature.attributes.forecast.curDir;
            }
            if(feature.attributes.type === 'wind'){
                return feature.attributes.forecast.windDir;
            }
            return 0;
        },
        graphic : function(feature){
            if(feature.attributes.type === 'wave'){
                var waveHeight = feature.attributes.forecast.waveHeight;
                var markerDir = 'img/wave/mark';
                
                if(waveHeight >= 0 && waveHeight <= defaultWaveLow){
                    markerDir += "01";
                } else if (waveHeight > defaultWaveLow && waveHeight <= defaultWaveMedium){
                    markerDir += "02";
                } else if (waveHeight > defaultWaveMedium){
                    markerDir += "03";
                }
                
                if(waveHeight >= defaultWaveWarnLimit){
                    markerDir += "red.png";
                } else {
                    markerDir += ".png";
                }
                
                return markerDir;
            }
            if(feature.attributes.type === 'current'){
                var currentSpeedMs = feature.attributes.forecast.curSpeed;
                var markerDir = 'img/current/mark';
                var currentSpeedKn = currentSpeedMs * (3.6/1.852);
                
                if(currentSpeedKn >= 0 && currentSpeedKn <= defaultCurrentLow){
                    markerDir += "01";
                } else if (currentSpeedKn > defaultCurrentLow && currentSpeedKn <= defaultCurrentMedium){
                    markerDir += "02";
                } else if (currentSpeedKn > defaultCurrentMedium){
                    markerDir += "03";
                }
                
                if(currentSpeedKn >= defaultCurrentWarnLimit){
                    markerDir += "red.png";
                } else {
                    markerDir += ".png";
                }

                return markerDir;
            }
            if(feature.attributes.type === 'wind'){
                var markerDir = 'img/wind/mark';                
                var windSpeed = feature.attributes.forecast.windSpeed;

                var windSpeedKnots = windSpeed * (3.6/1.852);

                if(windSpeedKnots >= 0 && windSpeedKnots <= 5){
                    markerDir += "005";
                } else if (windSpeedKnots > 5 && windSpeedKnots <= 10){
                    markerDir += "010";
                } else if (windSpeedKnots > 10 && windSpeedKnots <= 15){
                    markerDir += "015";
                } else if (windSpeedKnots > 15 && windSpeedKnots <= 20){
                    markerDir += "020";
                } else if (windSpeedKnots > 20 && windSpeedKnots <= 25){
                    markerDir += "025";
                } else if (windSpeedKnots > 25 && windSpeedKnots <= 30){
                    markerDir += "030";
                } else if (windSpeedKnots > 30 && windSpeedKnots <= 35){
                    markerDir += "035";
                } else if (windSpeedKnots > 35 && windSpeedKnots <= 40){
                    markerDir += "040";
                } else if (windSpeedKnots > 40 && windSpeedKnots <= 45){
                    markerDir += "045";
                } else if (windSpeedKnots > 45 && windSpeedKnots <= 50){
                    markerDir += "050";
                } else if (windSpeedKnots > 50 && windSpeedKnots <= 55){
                    markerDir += "055";
                } else if (windSpeedKnots > 55 && windSpeedKnots <= 60){
                    markerDir += "060";
                } else if (windSpeedKnots > 60 && windSpeedKnots <= 65){
                    markerDir += "065";
                } else if (windSpeedKnots > 65 && windSpeedKnots <= 70){
                    markerDir += "070";
                } else if (windSpeedKnots > 70 && windSpeedKnots <= 75){
                    markerDir += "075";
                } else if (windSpeedKnots > 75 && windSpeedKnots <= 80){
                    markerDir += "080";
                } else if (windSpeedKnots > 80 && windSpeedKnots <= 85){
                    markerDir += "085";
                } else if (windSpeedKnots > 85 && windSpeedKnots <= 90){
                    markerDir += "090";
                } else if (windSpeedKnots > 90 && windSpeedKnots <= 95){
                    markerDir += "095";
                } else if (windSpeedKnots > 95 && windSpeedKnots <= 100){
                    markerDir += "100";
                } else if (windSpeedKnots > 100 && windSpeedKnots <= 105){
                    markerDir += "105";
                }else if (windSpeedKnots > 100){
                    markerDir += "105";
                }

                if(windSpeed >= defaultWindWarnLimit){
                    markerDir += "red.png";
                } else {
                    markerDir += ".png";
                }
                
                return markerDir;
            } 
        }
    };

    embryo.metoc.layer = new OpenLayers.Layer.Vector("METOC", {
        styleMap : new OpenLayers.StyleMap({
            "default" : new OpenLayers.Style({
                //graphicOpacity : "${transparency}",
                externalGraphic : "${graphic}",
                graphicWidth : "${width}",
                graphicHeight : "${height}",
                rotation: "${rotation}",
            }, {
                context : context
            })
        })
    });

    embryo.map.add({
        group : "vessel",
        layer : embryo.metoc.layer,
        select : false
    });
};

embryo.mapInitialized(embryo.metoc.initLayer);

$(function() {

    "use strict";
    embryo.groupChanged(function(e) {
        groupSelected = (e.groupId == "vessel");
        if (embryo.metoc.layer) {
            embryo.metoc.layer.redraw();
        }
    });
});
