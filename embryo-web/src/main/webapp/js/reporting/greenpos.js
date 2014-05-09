var greenposScope;

(function() {
    "use strict";

    var greenposModule = angular.module('embryo.greenpos', [ 'embryo.scheduleService', 'embryo.greenposService', 'embryo.course', 'embryo.position' ]);

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

    var layer = null;

    $(function() {
        layer = new GreenposMarkerLayer();
        addLayerToMap("vessel", layer, embryo.map);
    });

    embryo.GreenPosCtrl = function($scope, ScheduleService, GreenposService, VesselService, $timeout, RouteService,
            VesselInformation) {
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
        } ];

        var reportNames = {};
        for ( var index in $scope.reportTypes) {
            var reportType = $scope.reportTypes[index];
            reportNames[reportType.id] = reportType.name;
        }

        // Beautiful thing that makes angular update form validity.

        $scope.report = {
            type : "PR"
        };

        $.each($scope.reportTypes, function(k, v) {
            setTimeout(function() {
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
            "SP" : [ "destination", "eta", "personsOnBoard", "course", "speed", "route", "weather", "ice" ],
            "PR" : [ "course", "speed", "weather", "ice" ],
            "FR" : [ "weather", "ice" ],
            "DR" : [ "route" ]
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

        $scope.sendReport = function() {
            $scope.warningMessages = null;
            $scope.alertMessages = null;
            $scope.reportAcknowledgement = null;

            var deactivateRoute = {
                value : $scope.deactivate && $scope.report.type == "FR",
                routeId : vesselDetails.additionalInformation.routeId
            };

            var inclWps = $scope.inclWps && ($scope.report.type == "SP" || $scope.report.type == "DR");

            $scope.report.recipients = [];
            var possibleRecipients;
            possibleRecipients = [ 'coastalcontrol', 'greenpos' ];
            for ( var x in possibleRecipients) {
                if($scope.report[possibleRecipients[x]]) {
                    $scope.report.recipients.push(possibleRecipients[x]);
                }
                delete $scope.report[possibleRecipients[x]];
            }
            
            GreenposService.save($scope.report, deactivateRoute, inclWps, function(email) {
                for(var x in $scope.report.recipients) {
                    $scope.report[$scope.report.recipients[x]] = true;
                }
                $scope.reportAcknowledgement = reportNames[$scope.report.type];
                $scope.userEmail = email;
                $scope.recipientName = '';
                $scope.recipientName += $scope.report.coastalcontrol ? 'Coastal Control' : '';
                $scope.recipientName += $scope.report.coastalcontrol && $scope.report.greenpos ? '/' : '';
                $scope.recipientName += $scope.report.greenpos ? 'ArcticCommand' : '';
                if ($scope.deactivate && $scope.report.type == "FR") {
                    VesselService.updateVesselDetailParameter($scope.report.mmsi, "additionalInformation.routeId", "");
                }
            }, function(error) {
                $scope.alertMessages = error;
            });
            
            
        };

        $scope.reset = function() {
            $scope.warningMessages = null;
            $scope.alertMessages = null;
            $scope.reportAcknowledgement = null;
            $scope.greenPosForm.$setPristine();

            initData();
        };

        $scope.setPositionOnMap = function(latitude, longitude) {
            if (longitude !== null && latitude !== null && typeof latitude != "undefined" && typeof longitude != "undefined") {
                layer.draw(longitude, latitude);
            } else {
                layer.clear();
            }
        };

        this.close = function() {
            layer.clear();
        };

        var vesselOverview = null, vesselDetails = null;

        VesselInformation.addInformationProvider({
            title : "Reporting",
            available : function(vesselOverview, vesselDetails) {
                if (vesselOverview.inAW)
                    return {
                        text : "OK",
                        klass : "success",
                        action : "edit"
                    };
                return false;
            },
            show : function(vesselOverview2, vesselDetails2) {
                vesselOverview = vesselOverview2;
                vesselDetails = vesselDetails2;

                initData();

                $scope.$apply();
            },
            hide : function() {
                $scope.warningMessages = null;
                $scope.alertMessages = null;
                $scope.reportAcknowledgement = null;
                $scope.greenPosForm.$setPristine();
            }
        });

        function initData() {
            $scope.report = {
                mmsi : vesselOverview.mmsi,
                callSign : vesselOverview.callSign,
                vesselName : vesselOverview.name
            };
            $scope.hasActiveRoute = (vesselDetails.additionalInformation.routeId && vesselDetails.additionalInformation.routeId.length > 0);
            $scope.inclWps = $scope.hasActiveRoute;

            ScheduleService.getActiveVoyage(vesselOverview.mmsi, vesselDetails.additionalInformation.routeId, function(voyageInfo) {
                if (!voyageInfo)
                    return;
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

                $scope.report.description = !voyageInfo.dep ? "" : "From " + voyageInfo.dep + " ";
                $scope.report.description += (!voyageInfo.des ? "" : "to " + voyageInfo.des);

            }, function(errorMsgs) {
                $scope.warningMessages = errorMsgs;
            });

            GreenposService.getLatestReport(vesselOverview.mmsi, function(latestReport) {
                evalGreenpos(latestReport);
                $("#greenposReportPanel").css("display", "block");
            });

        }
    };

    greenposModule.directive('sort', function() {
        return {
            restrict : 'A',
            scope : {
                options : '@',
                sort : '='
            },
            link : function(scope, element, attrs) {
                var sort = null, order = null;

                element.bind('click', function() {

                    if (!scope.sort || scope.sort != attrs.sort) {
                        scope.sort = attrs.sort;
                        scope.order = attrs.options && attrs.options.defaultorder ? attrs.options.defaultorder : 'DESC';
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

        embryo.controllers.greenposListView = {
            title : "Reports",
            available : function(vesselOverview, vesselDetails) {
                return vesselDetails.additionalInformation.greenpos;
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
            }
        };

        $scope.formatDateTime = function(timeInMillis) {
            return formatTime(timeInMillis);
        };

        $scope.formatCourse = function(course) {
            return formatCourse(course);
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
