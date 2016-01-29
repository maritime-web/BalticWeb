(function() {

    var metocModule = angular.module('embryo.metoc', [ 'embryo.storageServices' ]);

    metocModule.factory('MetocService', [
            '$http',
            'CookieService',
            function($http, CookieService) {
                var defaultCurrentLow = 1.0;
                var defaultCurrentMedium = 2.0;
                var defaultWaveLow = 1.0;
                var defaultWaveMedium = 2.0;

                var cookieName = "dma-metoc-defaultWarnLimit-" + embryo.authentication.userName;

                var defaultWarnLimits = {
                    defaultCurrentWarnLimit : 4.0,
                    defaultWaveWarnLimit : 2.0,
                    defaultWindWarnLimit : 15.0,
                };

                var cookieDefaults = CookieService.get(cookieName);
                if (cookieDefaults) {
                    defaultWarnLimits = cookieDefaults;
                }

                function buildWind() {
                    var windLimits = [];
                    for ( var max = 5; max <= 105; max += 5) {
                        var img = "img/wind_legend/mark" + (max < 10 ? "00" : max < 100 ? "0" : "") + max + ".png";
                        windLimits.push({
                            img : img,
                            min : max - 5,
                            max : max
                        });
                    }
                    return windLimits;
                }
                ;

                function buildLimits(imageBase, limits) {
                    var result = [];
                    for ( var index = 0; index < limits.length; index++) {
                        var img = imageBase + "0" + (index + 1) + ".png";
                        var limit = {
                            img : img,
                            min : limits[index],
                        };
                        if (index != limits.length - 1) {
                            limit.max = limits[index + 1];
                        }
                        result.push(limit);
                    }
                    return result;
                }

                function applyWarnLimits(limits, defaultWarnLimit) {
                    for ( var index = 0; index < limits.length; index++) {
                        if (limits[index].min >= defaultWarnLimit) {
                            limits[index].img = limits[index].img.replace(".png", "red.png");
                        } else if (limits[index].max && limits[index].min <= defaultWarnLimit
                                && defaultWarnLimit < limits[index].max) {
                            limits[index].img = limits[index].img.replace(".png", "red.png");
                        }
                    }
                    return limits;
                }

                return {
                    listMetoc : function(routeIds, callback, error) {
                        var messageId = embryo.messagePanel.show({
                            text : "Loading forecasts on route ..."
                        });

                        var ids = "";
                        for ( var index in routeIds) {
                            if (ids.length !== 0) {
                                ids += ":";
                            }
                            ids += routeIds[index];
                        }

                        var url = embryo.baseUrl + 'rest/metoc/list/' + ids;
                        var that = this;

                        $http.get(url, {
                            responseType : 'json'
                        }).success(function(result) {
                            var count = that.forecastCount(result);
                            
                            embryo.messagePanel.replace(messageId, {
                                text : "List of " + count + " forecasts downloaded.",
                                type : count > 0 ? "success" : "error"
                            });

                            callback(result);
                        }).error(function(data, status) {
                            embryo.messagePanel.replace(messageId, {
                                text : "Failed loading metoc. Server returned error: " + status,
                                type : "error"
                            });
                            if (error) {
                                error();
                            }
                        });
                    },
                    forecastCount : function(metocs){
                        var count = 0;
                        for (index in metocs) {
                            if (metocs[index].forecasts) {
                                count += metocs[index].forecasts.length;
                            }
                        }
                        return count;
                    },
                    getWaveLimits : function() {
                        var defaultWaveWarnLimit = this.getDefaultWarnLimits().defaultWaveWarnLimit;
                        var waveLimits = buildLimits("img/wave_legend/mark", [ 0, defaultWaveLow, defaultWaveMedium ]);
                        return applyWarnLimits(waveLimits, defaultWaveWarnLimit);
                    },
                    getCurrentLimits : function() {
                        var defaultCurrentWarnLimit = this.getDefaultWarnLimits().defaultCurrentWarnLimit;
                        var currentLimits = buildLimits("img/current_legend/mark", [ 0, defaultCurrentLow,
                                defaultCurrentMedium ]);
                        return applyWarnLimits(currentLimits, defaultCurrentWarnLimit);
                    },
                    getWindLimits : function() {
                        var defaultWindWarnLimit = this.getDefaultWarnLimits().defaultWindWarnLimit;
                        var windLimits = buildWind();
                        return applyWarnLimits(windLimits, defaultWindWarnLimit);
                    },
                    getDefaultWarnLimits : function() {
                        return defaultWarnLimits;
                    },
                    saveDefaultWarnLimits : function(warnLimits) {
                        defaultWarnLimits = warnLimits;
                        CookieService.set(cookieName, defaultWarnLimits);
                    },
                    waveImage : function(waveHeight) {

                    },
                    windImage : function() {

                    },
                    windImage : function() {

                    }
                };
            } ]);

    embryo.metoc = {};

    metocModule.run(function(MetocService) {
        embryo.metoc.service = MetocService;
    });
})();
