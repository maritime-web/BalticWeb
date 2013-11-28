var greenposScope;

(function() {
    "use strict";

    var greenposModule = angular.module('embryo.greenpos', [ 'embryo.scheduleService', 'embryo.greenposService', 'embryo.position']);

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
                    scope.$apply();
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

        // Beautiful thing that makes angular update form validity.

        $scope.report = {
            type : "PR"
        }

        $.each($scope.reportTypes, function (k, v) {
            setTimeout(function () {
                $scope.report.type = v.id;
                $scope.$apply();
            }, v * 10);
        });

        function evalGreenpos(greenpos) {
            if (!greenpos || !greenpos.ts) {
                $scope.report.type = "SP";
                return;
            }

            if (greenpos.type === 'FR') {
                $scope.report.type = "SP";
                return;
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

            if (greenpos.type === 'PR' || greenpos.type === 'DR') {
                $scope.report.type = "PR";
            } else {
                $scope.report.type = "SP";
            }
        }

        $scope.visibility = {
            "SP" : [ "destination", "eta", "personsOnBoard", "course", "speed", "weather", "ice" ],
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

        $scope.$watch($scope.getLatLon, function(newValue, oldValue) {
            $scope.setPositionOnMap(newValue.lat, newValue.lon);
        }, true);

        $scope.isVisible = function(fieldName) {
            if (!$scope.report || !$scope.report.type) {
                return true;
            }
            var fields = $scope.visibility[$scope.report.type];

            return fields.indexOf(fieldName) > -1;
        };

//        $scope.$watch("greenPosForm", function(newValue, oldValue) {
//            console.log("greenposForm");
//            console.log(newValue);
//        }, true);
//        $scope.$watch("greenPosForm.gpShipName", function(newValue, oldValue) {
//            console.log("gpShipName");
//            console.log(newValue);
//        }, true);
//        $scope.$watch("greenPosForm.gpShipName.$error", function(newValue, oldValue) {
//            console.log("error");
//            console.log(newValue);
//        }, true);

        $scope.sendReport = function() {
            $scope.message = null;
            GreenposService.save($scope.report, function() {
                $scope.message = "Greenpos report successfully submitted. ";
                if ($scope.deactivate) {
                    RouteService.setActiveRoute($scope.activeRouteId, false, function() {
                        $scope.message += "Active route successsfully deactivated. ";
                    }, function(error) {
                        $scope.alertMessages = error;
                        $scope.alertMessages[0] += "Greenpos submitted successfully, but failed route deactivation.";
                    });
                }
            }, function(error) {
                $scope.alertMessages = error;
            });
        };

        $scope.reset = function() {
            $scope.warningMessages = null;
            $scope.alertMessages = null;
            $scope.message = null;
        };

        $scope.setPositionOnMap = function(latitude, longitude) {
            if (longitude !== null && latitude !== null && typeof latitude != "undefined" && typeof longitude != "undefined") {
                layer.draw(longitude, latitude);
            } else {
                layer.clear();
            }
        };

        this.hide = function() {
            layer.clear();
        }

        this.show = function(vesselOverview, vesselDetails) {
            $scope.report.mmsi = vesselOverview.mmsi;
            $scope.report.callSign = vesselOverview.callSign;
            $scope.report.vesselName = vesselOverview.name;
            $scope.hasActiveRoute = (vesselDetails.additionalInformation.routeId != null);

            $scope.activeRouteId = vesselDetails.additionalInformation.routeId;

            ScheduleService.getActiveVoyage(
                vesselOverview.mmsi,
                vesselDetails.additionalInformation.routeId,
                function(voyageInfo) {
                    $scope.report.destination = voyageInfo.des;
                    $scope.report.eta = voyageInfo.desEta;
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

                }, function(errorMsgs) {
                    $scope.warningMessages = errorMsgs;
                }
            );

            GreenposService.getLatestReport(vesselOverview.mmsi, function(latestReport) {
                evalGreenpos(latestReport);
                $("#greenposReportPanel").css("display", "block");
            });

            $scope.$apply();
        }

        this.title = "Greenpos Reporting";

        this.available = function(vesselOverview, vesselDetails) {
            if (vesselOverview.inArcticWeb)
                return {
                    text : "OK",
                    klass : "success",
                    action : "edit"
                };
            return false;
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

                            if (!scope.sort || scope.sort != attrs.sort) {
                                scope.sort = attrs.sort;
                                scope.order = attrs.options && attrs.options.defaultorder ? attrs.options.defaultorder
                                        : 'DESC';
                                element.find('i').addClass('icon-chevron-up');
                            } else {
                                scope.order = (scope.order == 'ASC' ? 'DESC' : 'ASC');
                                element.find('i').toggleClass('icon-chevron-up icon-chevron-down');
                            }

                            scope.options.fnSort(sort, order);
                        });

                scope.$watch('sort', function(newValue) {
                    // elem.find('i').toggleClass('');
                });

                element.append(' <i class="" style="vertical-align: middle; margin-bottom: 4px">');
            }
        };
    });

    embryo.GreenposListCtrl = function($scope, GreenposService) {
        $scope.max = 10;
        // $scope.options = {
        // fnSort : function(sort, order) {
        // console.log('fnSort' + sort + order);
        // }
        // };

        embryo.controllers.greenposListView = {
            title : "Greenpos Reports",
            available : function(vesselOverview, vesselDetails) {
                return vesselOverview.inArcticWeb;
            },
            show : function(vesselOverview, vesselDetails) {
                $("#greenposListPanel").css("display", "block");

                $scope.vessel = vesselDetails;

                GreenposService.findReports({
                    mmsi : $scope.vessel.mmsi,
                    start : 0,
                    max : $scope.max,
                    sort : 'time'
                }, function(reports) {
                    $scope.reports = reports;
                });

                $scope.$apply();
            },
            hide : function() {
                $("#greenposListPanel").css("display", "none");
            }
        };

        $scope.formatDateTime = function(timeInMillis) {
            
            return formatTime(timeInMillis);
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
