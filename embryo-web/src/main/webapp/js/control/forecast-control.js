$(function() {

    var forecastLayer = new ForecastLayer();
    addLayerToMap("forecasts", forecastLayer, embryo.map);

    var module = angular.module('embryo.forecast.control', [ 'embryo.metoc', 'ui.bootstrap.accordion', 'embryo.control', 'embryo.forecast.service' ]);

    module.controller("ForecastController", [ '$scope', function($scope) {
        $scope.selected = {};

        $scope.forecastSelected = '';

        $scope.$on("$destroy", function() {
            embryo.controllers.settings.close();
        });
    } ]);

    module.controller('WaveForecastCtrl', [ '$scope', 'ForecastService', function($scope, ForecastService) {

        $scope.reloadMap = function(wipe) {
            if ($scope.pc.forecastSelected) {
                forecastLayer.drawWaveForecast($scope.data, $scope.current);
            } else if (wipe === true) {
                forecastLayer.clear();
            }
        };

        $scope.getForecast = function(p, $event) {
            $event.preventDefault();
            if (p.id == $scope.pc.forecastSelected) {
                $scope.pc.forecastSelected = '';
                $scope.reloadMap(true);
                return;
            }
            ForecastService.getWaveForecast(p.name, function(forecast) {
                $scope.errorMsg = null;
                $scope.data = forecast;
                var time = forecast.metadata.time;
                $scope.start = 0;
                $scope.end = time.length - 1;
                $scope.current = $scope.start;

                $scope.updateCurrentDate = function() {
                    var t = time[$scope.current];
                    $scope.currentDate = formatTime(t);
                    $scope.reloadMap();
                };
                $scope.$watch('current', $scope.updateCurrentDate);
                $scope.updateCurrentDate();

                for (var i = 0; i < $scope.waveForecasts.length; i++) {
                    $scope.waveForecasts[i].selected = false;
                }
                $scope.pc.forecastSelected = p.id;

                $scope.reloadMap();
            }, function(error, status) {
                $scope.errorMsg = error;
            });
        };

    } ]);

    module.controller('WaveForecastsCtrl', [ '$scope', 'ForecastService', function($scope, ForecastService) {

        // Enable below once DMI starts delivering data.
        
        /*ForecastService.listWaveForecasts(function(forecasts) {
            $scope.errorMsg = null;
            $scope.waveForecasts = [];
            for (var i = 0; i < forecasts.length; i++) {
                $scope.waveForecasts.push({
                    name : forecasts[i].name,
                    id : forecasts[i].id,
                    selected : false
                });
            }
        }, function(error, status) {
            $scope.errorMsg = error;
        });*/
    } ]);
    
    module.controller('CurrentForecastCtrl', [ '$scope', 'ForecastService', function($scope, ForecastService) {
        
        $scope.reloadMap = function(wipe) {
            if ($scope.pc.forecastSelected) {
                forecastLayer.drawCurrentForecast($scope.data, $scope.current);
            } else if (wipe === true) {
                forecastLayer.clear();
            }
        };

        $scope.getForecast = function(p, $event) {
            $event.preventDefault();
            if (p.id == $scope.pc.forecastSelected) {
                $scope.pc.forecastSelected = '';
                $scope.reloadMap(true);
                return;
            }
            ForecastService.getCurrentForecast(p.id, function(forecast) {
                $scope.errorMsg = null;
                $scope.data = forecast;
                var time = forecast.metadata.time;
                $scope.start = 0;
                $scope.end = time.length - 1;
                $scope.current = $scope.start;

                $scope.updateCurrentDate = function() {
                    var t = time[$scope.current];
                    $scope.currentDate = formatTime(t);
                    $scope.reloadMap();
                };
                $scope.$watch('current', $scope.updateCurrentDate);
                $scope.updateCurrentDate();

                for (var i = 0; i < $scope.currentForecasts.length; i++) {
                    $scope.currentForecasts[i].selected = false;
                }
                $scope.pc.forecastSelected = p.id;

                $scope.reloadMap();
            }, function(error, status) {
                $scope.errorMsg = error;
            });
        };    }]);
    
    module.controller('CurrentForecastsCtrl', [ '$scope', 'ForecastService', function($scope, ForecastService) {
    	ForecastService.listCurrentForecasts(function(forecasts) {
            $scope.errorMsg = null;
            $scope.currentForecasts = [];
            for (var i = 0; i < forecasts.length; i++) {
                $scope.currentForecasts.push(forecasts[i]);
                $scope.currentForecasts[i].selected = false;
                $scope.currentForecasts[i].timestamp = formatTime($scope.currentForecasts[i].timestamp);
            }
        }, function(error, status) {
            $scope.errorMsg = error;
        });
    }]);

    module.controller('IceForecastCtrl', [ '$scope', 'ForecastService', function($scope, ForecastService) {

        $scope.reloadMap = function(wipe) {
            if ($scope.pc.forecastSelected) {
                forecastLayer.drawIceForecast($scope.data, $scope.current, $scope.selectedVariable);
            } else if (wipe === true) {
                forecastLayer.clear();
            }
        };

        $scope.$watch('selectedVariable', $scope.reloadMap);

        $scope.getForecast = function(p, $event) {
            $event.preventDefault();
            if (p.id == $scope.pc.forecastSelected) {
                $scope.pc.forecastSelected = '';
                $scope.reloadMap(true);
                return;
            }
            ForecastService.getIceForecast(p.id, function(forecast) {
                $scope.errorMsg = null;
                $scope.data = forecast;
                var time = forecast.metadata.time;
                $scope.start = 0;
                $scope.end = time.length - 1;
                $scope.current = $scope.start;
                $scope.selectedVariable = 'iceConcentration';
                
                if(!("Ice concentration" in forecast.variables)) {
                	$scope.noIceConcentration = true;
                }
                if(!("Ice thickness" in forecast.variables)) {
                	$scope.noIceThickness = true;
                }
                if(!("Ice speed north" in forecast.variables) || !("Ice speed east" in forecast.variables)) {
                	$scope.noIceSpeed = true;
                }
                if(!("Ice accretion risk" in forecast.variables)) {
                	$scope.noIceAccretion = true;
                }

                $scope.updateCurrentDate = function() {
                    var t = time[$scope.current];
                    $scope.currentDate = formatTime(t);
                    $scope.reloadMap();
                };
                $scope.$watch('current', $scope.updateCurrentDate);
                $scope.updateCurrentDate();

                for (var i = 0; i < $scope.iceForecasts.length; i++) {
                    $scope.iceForecasts[i].selected = false;
                }
                $scope.pc.forecastSelected = p.id;
            }, function(error, status) {
                $scope.errorMsg = error;
            });

        };
    } ]);
    
    module.controller('IceForecastsCtrl', [ '$scope', 'ForecastService', function($scope, ForecastService) {

    	ForecastService.listIceForecasts(function(forecasts) {
            $scope.errorMsg = null;
            $scope.iceForecasts = [];
            for (var i = 0; i < forecasts.length; i++) {
            	$scope.iceForecasts.push(forecasts[i]);
                $scope.iceForecasts[i].selected = false;
                $scope.iceForecasts[i].timestamp = formatTime($scope.iceForecasts[i].timestamp);
            }
        }, function(error, status) {
            $scope.errorMsg = error;
        });
    } ]);

});