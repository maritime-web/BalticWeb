$(function() {

    var metocLayer = new MetocLayer();
    addLayerToMap("weather", metocLayer, embryo.map);

    var module = angular.module('embryo.weather.control',
            [ 'embryo.metoc', 'ui.bootstrap.accordion', 'embryo.control' ]);

    module.controller("WeatherController", [ '$scope', function($scope) {
        $scope.selected = {};

        $scope.ms2Knots = function(ms){
            return Math.round(ms2Knots(ms) * 100) / 100;
        }

        metocLayer.select("metocCtrl", function(forecast) {
            $scope.selected.open = !!forecast;
            $scope.selected.forecast = forecast; 
            if (!$scope.$$phase) {
                $scope.$apply(function() {
                });
            }
        });
        
        $scope.formatTs = function(ts){
            return formatTime(ts);
        };
        
        $scope.$on("$destroy", function() {
            embryo.controllers.settings.close();
        });
    } ]);

    module.controller("MetocController", [ '$scope', 'RouteService', 'MetocService', 'Subject',
            function($scope, RouteService, MetocService, Subject) {
                $scope.routes = [];
                $scope.selectedOpen = false;

                function available(route) {
                    return (Math.abs(route.etaDep - Date.now()) < 1000 * 3600 * 55) || Date.now() < route.eta;
                }

                if (Subject.getDetails().shipMmsi) {
                    $scope.routes.push({
                        name : 'Active route',
                        available : false,
                        ids : null
                    });

                    RouteService.getActiveMeta(embryo.authentication.shipMmsi, function(route) {
                        $scope.routes[0].available = available(route);
                        $scope.routes[0].ids = [ route.id ];
                    });
                }

                $scope.routes.push({
                    name : 'Selected routes',
                    available : false,
                    ids : []
                });

                $scope.$watch(RouteService.getSelectedRoutes, function(newValue, oldValue) {
                    var routes = RouteService.getSelectedRoutes();

                    for ( var index in routes) {
                        if (available(routes[index])) {
                            $scope.routes[$scope.routes.length - 1].available = true;
                            $scope.routes[$scope.routes.length - 1].ids.push(routes[index].id);
                        }
                    }
                });

                $scope.$watch(function() {
                    return MetocService.getDefaultWarnLimits();
                }, function(defaultLimits) {
                    if ($scope.metocs) {
                        metocLayer.draw($scope.metocs);
                    }
                }, true);

                function clearScope() {
                    $scope.shown = null;
                    $scope.selectedForecast = null;
                    $scope.metocs = null;
                }

                $scope.toggleShowMetoc = function($event, route) {
                    $event.preventDefault();
                    metocLayer.clear();
                    if (!$scope.shown || $scope.shown.name !== route.name) {
                        MetocService.listMetoc(route.ids, function(metocs) {
                            if (MetocService.forecastCount(metocs) > 0) {
                                $scope.shown = route;
                                $scope.metocs = metocs;
                                metocLayer.draw(metocs);
                                metocLayer.zoomToExtent();
                            } else {
                                clearScope();
                            }
                        });
                    } else {
                        clearScope();
                    }
                };

            } ]);

    module.controller("SettingsCtrl", [ '$scope', 'MetocService', function($scope, MetocService) {
        var warnLimits = MetocService.getDefaultWarnLimits();
        $scope.settings = [ {
            text : "Warning limit for waves",
            value : warnLimits.defaultWaveWarnLimit,
            type : "number",
            unit : "meter"
                
        }, {
            text : "Warning limit for current",
            value : warnLimits.defaultCurrentWarnLimit,
            type : "number",
            unit : "knots"
        }, {
            text : "Warning limit for wind",
            value : warnLimits.defaultWindWarnLimit,
            type : "number",
            unit : "knots"
        } ];

        $scope.save = function() {
            MetocService.saveDefaultWarnLimits({
                defaultWaveWarnLimit : $scope.settings[0].value,
                defaultCurrentWarnLimit : $scope.settings[1].value,
                defaultWindWarnLimit : $scope.settings[2].value
            });
            
            $scope.message = "Weater forecast settings saved.";
        };

        $scope.provider =  {
            doShow : false,
            show : function(context) {
                $scope.message = null;
                this.doShow = true;
                $scope.title = context.title;
            },
            close : function(){
                this.doShow = false;
            }
        };
        
        $scope.close = function($event) {
            $event.preventDefault();
            $scope.provider.close();
        }

        embryo.controllers.settings = $scope.provider;

    } ]);

    module.controller("SettingsMetocCtrl", [ '$scope', function($scope) {
        $scope.open = function($event) {
            $event.preventDefault();
            embryo.controllers.settings.show({
                title : "Forecast on route"
            });
        };
    } ]);

    module.controller("LegendsController", [ '$scope', 'MetocService', function($scope, MetocService) {
        function buildLimits(limits) {
            var result = [];
            for ( var index = 0; index < limits.length; index += 2) {
                var object = {
                    "first" : limits[index],
                }
                if (index < limits.length - 2) {
                    object["second"] = limits[index + 1];
                }
                result.push(object);
            }
            return result;
        }

        $scope.$watch(function() {
            return MetocService.getDefaultWarnLimits();
        }, function(defaultLimits) {
            $scope.waveLimits = buildLimits(MetocService.getWaveLimits());
            $scope.currentLimits = buildLimits(MetocService.getCurrentLimits());
            $scope.windLimits = buildLimits(MetocService.getWindLimits());
        }, true);
        
        $scope.knots2Ms = function(knots){
            return Math.round(knots2Ms(knots) * 10) / 10;
        }
    } ]);
});
