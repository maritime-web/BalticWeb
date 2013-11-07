(function() {
    "use strict";

    var greenposModule = angular.module('embryo.greenpos', [ 'embryo.scheduleService', 'embryo.greenposService', ]);

    /*
     * Inspired by http://jsfiddle.net/zbjLh/2/
     */
    greenposModule.directive('resize', function($window) {
        return {
            restrict : 'A',
            link : function(scope, element, attrs) {

                var elemToMatch = $('#' + attrs.resize);
                scope.getElementDimensions = function() {
                    return {
                        'h' : elemToMatch.height(),
                    };
                };
                scope.$watch(scope.getElementDimensions, function(newValue, oldValue) {

                    scope.style = function() {
                        return {
                            'height' : (newValue.h) + 'px',
                        };
                    };
                }, true);

                var window = angular.element($window);
                window.bind('resize', function() {
                    scope.$apply(function() {
                    });
                });
            }
        };
    });

    var layer;

    $(function() {
        layer = new GreenposMarkerLayer();
        addLayerToMap("vessel", layer, embryo.map);
    })

    embryo.GreenPosCtrl = function($scope, ScheduleService, GreenposService, $timeout, RouteService) {
        $scope.editable = true;

        $scope.reportTypes = [ {
            id : "SP",
            name : "Sailing Plan Report"
        }, {
            id : "PR",
            name : "Position Report"
        }, {
            id : "FR",
            name : "Final Report"
        }, {
            id : "DR",
            name : "Deviation Report"
        } ]

        $scope.report = {
            type : "SP"
        }

        function evalGreenpos(greenpos) {
            if (!greenpos || !greenpos.ts) {
                $scope.report.type = "PR";
                return;
            }

            if (greenpos.reportType === 'FR') {
                $scope.report.type = "SP";
            }

            var now = Date.now();
            var period = GreenposService.getPeriod(now);

            // Allow for reports to be performed 15 minutes before reporting
            // hour.
            // if last report performed more than 15 minutes before reporting
            // period then perform new report
            if (greenpos.ts < (period.from - 900000) && now < (period.from + 1800000)) {
                $scope.report.type = "PR";
                return;
            }

            // if last report not performed more than ½ later than reporting
            // hour, then highlight.
            if (greenpos.ts < (period.from - 900000) && now >= (period.from + 1800000)) {
                $scope.report.type = "PR";
                return;
            }

            $scope.report.type = "SP";
        }
        ;

        $scope.visibility = {
            "SP" : [ "destination", "etaOfArrival", "personsOnBoard", "course", "speed", "weather", "ice" ],
            "PR" : [ "course", "speed", "weather", "ice" ],
            "FR" : [ "weather", "ice" ],
            "DR" : [ "deviation" ]
        };

        $scope.getLatLon = function() {
            return {
                lat : $scope.report.lat,
                lon : $scope.report.lon
            };
        };

        function reformat(value, formatter) {
            if (value) {
                value = value.trim();
                var parsed = parseFloat(value);
                if (parsed == value) {
                    return formatter(parsed);
                }
            }
            return null;
        }

        $("#gpLat").change(function() {
            var formatted = reformat($scope.report.lat, formatLatitude);
            if (formatted) {
                $scope.report.lat = formatted;
                $scope.$apply();
            }
        });

        $("#gpLon").change(function() {
            var formatted = reformat($scope.report.lon, formatLongitude);
            if (formatted) {
                $scope.report.lon = formatted;
                $scope.$apply();
            }
        });

        $scope.$watch($scope.getLatLon, function(newValue, oldValue) {
            if (newValue.lat && newValue.lon) {
                $scope.setPositionOnMap(newValue.lat, newValue.lon);
            }
        }, true);

        $scope.isVisible = function(fieldName) {
            if (!$scope.report || !$scope.report.type) {
                return true;
            }
            var fields = $scope.visibility[$scope.report.type];

            return fields.indexOf(fieldName) > -1;
        };

        $scope.sendReport = function() {
            $scope.message = null;

            GreenposService.save($scope.report, function() {
                $scope.message = "GreenPos report successfully submitted. ";

                if ($scope.deactivate) {
                    RouteService.setActiveRoute($scope.activeRouteId, false, function() {
                        $scope.message += "Active route successsfully deactivated. ";
                    });
                }
            });
        };

        $scope.clear = function() {

        };

        $scope.setPositionOnMap = function(latitude, longitude) {
            try {
                var lat = embryo.geographic.parseLatitude(latitude);
                var lon = embryo.geographic.parseLongitude(longitude);
                layer.draw(lon, lat);
            } catch (e) {
                layer.clear();
            }
        };

        this.hide = function() {
            $("#greenposReportPanel").css("display", "none");
            layer.clear();
        }

        this.show = function(c) {
            GreenposService.getLatestReport(c.vesselOverview.mmsi, function(latestReport) {
                evalGreenpos(latestReport);
                $("#greenposReportPanel").css("display", "block");
            });

            $scope.report.mmsi = c.vesselOverview.mmsi;
            $scope.report.callSign = c.vesselOverview.callSign;
            $scope.report.vesselName = c.vesselOverview.name;
            $scope.hasActiveRoute = (c.vesselDetails.additionalInformation.routeId != null);

            $scope.activeRouteId = c.vesselDetails.additionalInformation.routeId;

            $scope.$apply();

            $scope.$apply(function() {
                ScheduleService.getActiveVoyage(c.vesselOverview.mmsi, c.vesselDetails.additionalInformation.routeId,
                    function(voyageInfo) {
                        $scope.report.destination = voyageInfo.des;
                        $scope.report.etaOfArrival = voyageInfo.desEta;
                        if (voyageInfo.crew) {
                            $scope.report.personsOnBoard = voyageInfo.crew;
                        }
                        if (voyageInfo.passengers) {
                            if ($scope.report.personsOnBoard) {
                                $scope.report.personsOnBoard += voyageInfo.passengers;
                            } else {
                                $scope.report.personsOnBoard = voyageInfo.passengers;
                            }
                        }
                    }
                );
            });
        }

        this.title = "Greenpos Reporting";

        this.status = function(vesselOverview, vesselDetails) {
            return {
                code : "success",
                message : "OK"
            }
        }

        embryo.controllers.greenpos = this;
    };

    greenposModule.directive('sort', function() {
        return {
            restrict : 'A',
            scope : {
                options : '@',
                sort : '='
            },
            link : function(scope, element, attrs) {
                var sort, order;

                element.bind('click',
                        function() {
                            console.log(scope.sort);
                            console.log(scope.order);
                            console.log(attrs.sort);

                            if (!scope.sort || scope.sort != attrs.sort) {
                                scope.sort = attrs.sort;
                                scope.order = attrs.options && attrs.options.defaultorder ? attrs.options.defaultorder
                                        : 'DESC';
                                element.find('i').addClass('icon-chevron-up');
                            } else {
                                console.log('else');
                                scope.order = (scope.order == 'ASC' ? 'DESC' : 'ASC');
                                element.find('i').toggleClass('icon-chevron-up icon-chevron-down');
                            }

                            console.log(order);

                            console.log(scope.options);
                            console.log(attrs.options);
                            scope.options.fnSort(sort, order);
                        });

                scope.$watch('sort', function(newValue) {
                    // elem.find('i').toggleClass('');
                    console.log('wathcing:' + newValue);
                });

                element.append(' <i class="" style="vertical-align: middle; margin-bottom: 4px">');
            }
        };
    });

    embryo.GreenposListCtrl = function($scope, GreenposService) {
        $scope.max = 20;

        $scope.options = {
            fnSort : function(sort, order) {
                console.log('fnSort' + sort + order);
            }
        };

        GreenposService.findReports({
            start : 0,
            max : $scope.max,
            sort : 'time'
        }, function(reports) {
            $scope.reports = reports;
        });

        $scope.utc = function(dateValue) {
            var date = new Date(dateValue);
            date.setMinutes(date.getMinutes() + date.getTimezoneOffset());
            return date;
        };

        $scope.reportText = function(type) {
            if (type === 'SP') {
                return 'Sailing plan';
            }
            if (type === 'DR') {
                return 'Deviation';
            }
            if (type === 'FR') {
                return 'Final';
            }
            if (type === 'PR') {
                return 'Position';
            }
            return null;
        };
    };

}());
