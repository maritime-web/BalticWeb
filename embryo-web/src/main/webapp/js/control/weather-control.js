$(function() {

    var metocLayer = new MetocLayer();
    addLayerToMap("weather", metocLayer, embryo.map);

    var module = angular.module('embryo.weather.control',
            [ 'embryo.metoc', 'ui.bootstrap.accordion', 'embryo.control' ]);

    module.controller("WeatherController", [ '$scope', 'RouteService', 'MetocService',
            function($scope, RouteService, MetocService) {
                $scope.routes = [];
                $scope.selectedOpen = false;

                function available(route) {
                    return Math.abs((route.etaDep - Date.now()) < 1000 * 3600 * 55) || Date.now() < route.eta;
                }

                if (embryo.authentication.shipMmsi) {
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

                $scope.toggleShowMetoc = function($event, route) {
                    $event.preventDefault();
                    metocLayer.clear();
                    if (!$scope.shown || $scope.shown.name !== route.name) {
                        MetocService.listMetoc(route.ids, function(metocs) {
                            $scope.shown = route;
                            $scope.metocs = metocs;
                            metocLayer.draw(metocs);
                            metocLayer.zoomToExtent();
                        });
                    } else {
                        $scope.shown = null;
                        $scope.selectedForecast = null;
                        $scope.metocs = null;
                    }
                };

                metocLayer.select("metocCtrl", function(forecast) {
                    $scope.selectedForecast = forecast;
                    $scope.$apply(function() {
                    });
                });

            } ]);

    module.controller("SelectController", [ '$scope', function($scope) {
        $scope.showSelected = function(selected) {
            $scope.selected = selected;
        };
    } ]);

    module.controller("SettingsCtrl", [ '$scope', 'MetocService', function($scope, MetocService) {
        var warnLimits = MetocService.getDefaultWarnLimits();
        $scope.settings = [ {
            text : "Warning limit for waves",
            value : warnLimits.defaultWaveWarnLimit,
            type : "number"
        }, {
            text : "Warning limit for current",
            value : warnLimits.defaultCurrentWarnLimit,
            type : "number"
        }, {
            text : "Warning limit for wind",
            value : warnLimits.defaultWindWarnLimit,
            type : "number"
        } ];

        $scope.save = function() {
            MetocService.saveDefaultWarnLimits({
                defaultWaveWarnLimit : $scope.settings[0].value,
                defaultCurrentWarnLimit : $scope.settings[1].value,
                defaultWindWarnLimit : $scope.settings[2].value
            });
        };

        embryo.controllers.settings = {
            show : function(context) {
                $scope.title = context.title;
                $("#settingsPanel").css("display", "block");
            }
        };

    } ]);

    module.controller("SettingsMetocCtrl", [ '$scope', function($scope) {
        $scope.open = function($event) {
            $event.preventDefault();

            embryo.controllers.settings.show({
                title : "Forecasts on route"
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
    } ]);
});
