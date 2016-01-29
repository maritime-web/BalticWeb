var greenposScope;

(function () {
    "use strict";

    var module = angular.module('embryo.greenpos', [ 'embryo.scheduleService', 'embryo.greenposService',
        'embryo.course', 'embryo.position', 'embryo.controller.reporting' ]);

    /*
     * Inspired by http://jsfiddle.net/zbjLh/2/
     */
    module.directive('resize', function ($window) {
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {

                var elemToMatch = $('#' + attrs.resize);
                scope.getElementDimensions = function () {
                    return {
                        'h': elemToMatch.height()
                    };
                };
                scope.$watch(scope.getElementDimensions, function (newValue, oldValue) {

                    scope.style = function () {
                        return {
                            'height': (newValue.h) + 'px'
                        };
                    };
                }, true);

                var window = angular.element($window);
                window.bind('resize', function () {
                    scope.$apply();
                });
            }
        };
    });

    var layer = null;

    embryo.postLayerInitialization(function(){
        layer = new GreenposMarkerLayer();
        addLayerToMap("vessel", layer, embryo.map);
    })

    embryo.GreenPosCtrl = function ($scope, ScheduleService, GreenposService, VesselService, $timeout, RouteService, VesselInformation) {
        $scope.deactivate = {
            value: false
        }

        $scope.inclWps = {
            value: true
        }


        $scope.report = {
            type: "PR"
        }

        $scope.recipients = {
            coastalcontrol: 'Coastal Control',
            greenpos: 'Greenpos'
        };

        $scope.reportTypes = [
            {
                id: "SP",
                name: "Sailing Plan Report"
            },
            {
                id: "PR",
                name: "Position Report"
            },
            {
                id: "FR",
                name: "Final Report"
            },
            {
                id: "DR",
                name: "Deviation Report"
            }
        ];

        var reportNames = {};
        for (var index in $scope.reportTypes) {
            var reportType = $scope.reportTypes[index];
            reportNames[reportType.id] = reportType.name;
        }

        // Beautiful thing that makes angular update form validity.

        $.each($scope.reportTypes, function (k, v) {
            setTimeout(function () {
                $scope.report.type = v.id;
                $scope.$apply();
            }, v * 10);
        });

        function setNumber() {
            if ($scope.report && $scope.report.mmsi && $scope.report.recipient) {
                GreenposService.nextReportNumber($scope.report.mmsi, $scope.report.recipient, $scope.report.type, function (nextNumber) {
                    $scope.report.number = nextNumber.number;
                    $scope.nextNumber = nextNumber;
                });
            }
        }

        $scope.visibility = {
            "SP": [ "destination", "eta", "personsOnBoard", "course", "speed", "route", "weather", "ice" ],
            "PR": [ "course", "speed", "weather", "ice" ],
            "FR": [ "weather", "ice" ],
            "DR": [ "route" ]
        };

        $scope.$watch("report.type", function () {
            if ($scope.position) {
                $scope.position.location = null;
            }
            if ($scope.voyageInfo) {
                if ($scope.report.type === "SP") {
                    $scope.position.location = $scope.voyageInfo.dep;
                    $scope.position.lat = $scope.voyageInfo.depLat;
                    $scope.position.lon = $scope.voyageInfo.depLon;
                } else if ($scope.report.type === "FR") {
                    $scope.position.location = $scope.voyageInfo.des;
                    $scope.position.lat = $scope.voyageInfo.desLat;
                    $scope.position.lon = $scope.voyageInfo.desLon;
                }
            }
            setNumber();
        }, true);

        $scope.$watch("report.recipient", function () {
            setNumber();
        }, true);

        $scope.$watch("position.location", function (newValue, oldValue) {
            $scope.report.lat = newValue ? $scope.position.lat : null;
            $scope.report.lon = newValue ? $scope.position.lon : null;
            $scope.updatePositionOnMap();
        }, true);

        $scope.$watch("position.use", function (newValue, oldValue) {
            $scope.report.lat = newValue ? $scope.position.lat : null;
            $scope.report.lon = newValue ? $scope.position.lon : null;
            $scope.updatePositionOnMap();
        }, true);

        $scope.isVisible = function (fieldName) {
            if (!$scope.report || !$scope.report.type) {
                return true;
            }
            var fields = $scope.visibility[$scope.report.type];

            // return fields.indexOf(fieldName) > -1;
            return $.inArray(fieldName, fields) > -1;
        };

        $scope.sendReport = function () {
            $scope.warningMessages = null;
            $scope.alertMessages = null;
            $scope.reportAcknowledgement = null;

            var deactivateRoute = {
                value: $scope.deactivate.value && $scope.report.type == "FR",
                routeId: vesselDetails.additionalInformation.routeId
            };

            var inclWps = $scope.inclWps.value && ($scope.report.type == "SP" || $scope.report.type == "DR");

            GreenposService.save($scope.report, deactivateRoute, inclWps, function (email) {
                $scope.reportAcknowledgement = reportNames[$scope.report.type];
                $scope.userEmail = email;
                $scope.recipientName = '';
                if ($scope.report.recipient) {
                    $scope.recipientName = $scope.report.recipient === 'greenpos' ? 'Arctic Command' : $scope.recipients[$scope.report.recipient];
                }
                if ($scope.deactivate && $scope.report.type == "FR") {
                    VesselService.updateVesselDetailParameter($scope.report.mmsi, "additionalInformation.routeId", "");
                }
            }, function (error) {
                $scope.alertMessages = error;
            });

        };

        $scope.reset = function (greenPosForm) {
            $scope.warningMessages = null;
            $scope.alertMessages = null;
            $scope.reportAcknowledgement = null;
            initData();
            greenPosForm.$setPristine();
        };

        function getReportPanelUpperRight() {
            var $reportPanel = $("#greenposReportPanel");
            var $pos = $reportPanel.position();
            if (!$pos) {
                return null;
            }
            return {
                right: $pos.left + $reportPanel.width(),
                top: $pos.top
            };
        }

        $scope.updatePositionOnMap = function () {
            var longitude = $scope.report.lon;
            var latitude = $scope.report.lat;

            if (!!longitude && !!latitude) {
                layer.draw(longitude, latitude);

                var pixel = embryo.map.getPxFromPosition(longitude, latitude);
                var reportPanelUpperRight = getReportPanelUpperRight();

                if (!!reportPanelUpperRight && (!embryo.map.isWithinBorders(longitude, latitude) || reportPanelUpperRight.right > pixel.x)) {
                    var lonLat = embryo.map.transformPosition(longitude, latitude);
                    var rPanelUpRight = embryo.map.getLonLatFromPixel(reportPanelUpperRight.right, reportPanelUpperRight.top);
                    var iMap = embryo.map.internalMap;
                    var destLon = rPanelUpRight.lon
                        + (iMap.getCenter().lon + iMap.getExtent().getWidth() / 2 - rPanelUpRight.lon) / 2;

                    var diff = embryo.map.lonLatDifference(new OpenLayers.LonLat(destLon, iMap.getCenter().lat), iMap.getCenter());
                    var newCenter = embryo.map.addToLonLat(lonLat, diff);
                    iMap.setCenter(newCenter, 4);
                }
            } else {
                layer.clear();
            }
        };

        this.close = function ($event) {
            $event.preventDefault();
            $scope.provider.close();
        };

        var vesselOverview = null, vesselDetails = null;

        $scope.provider = {
            title: "Reporting",
            type: "edit",
            doShow: false,
            available: function (vesselOverview, vesselDetails) {
                if (vesselOverview.inAW)
                    return {
                        text: "OK",
                        klass: "success",
                        action: "edit"
                    };
                return false;
            },
            show: function (vesselOverview2, vesselDetails2) {
                this.doShow = true;
                vesselOverview = vesselOverview2;
                vesselDetails = vesselDetails2;

                initData();
            },
            shown: function (vo, vd) {
                return this.doShow;
            },
            close: function () {
                layer.clear();
                this.doShow = false;
                $scope.warningMessages = null;
                $scope.alertMessages = null;
                $scope.reportAcknowledgement = null;
            }
        }

        $scope.close = function ($event, greenPosForm) {
            $event.preventDefault();
            $scope.provider.close();
            if (greenPosForm) {
                greenPosForm.$setPristine();
            }
        }

        VesselInformation.addInformationProvider($scope.provider);

        function initData() {
            $scope.position = {
                use: true
            };

            $scope.report = {
                mmsi: vesselOverview.mmsi,
                callSign: vesselOverview.callSign,
                vesselName: vesselOverview.name,
                type: "PR"
            };
            $scope.hasActiveRoute = (vesselDetails.additionalInformation.routeId && vesselDetails.additionalInformation.routeId.length > 0);
            $scope.inclWps.value = $scope.hasActiveRoute

            $scope.editVesselInformation = !vesselOverview || !vesselOverview.name || !vesselOverview.callSign;

            ScheduleService.getActiveVoyage(vesselOverview.mmsi, vesselDetails.additionalInformation.routeId, function (voyageInfo) {
                if (!voyageInfo) {
                    $scope.voyageInfo = null;
                    return
                }
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
                $scope.voyageInfo = voyageInfo;
                $scope.report.description = !voyageInfo.dep ? "" : "From " + voyageInfo.dep + " ";
                $scope.report.description += (!voyageInfo.des ? "" : "to " + voyageInfo.des);
                setNumber();
            }, function (errorMsgs) {
                $scope.warningMessages = errorMsgs;
            });

            GreenposService.getLatestReport(vesselOverview.mmsi, function (latestReport) {
                $scope.report.type = GreenposService.defaultReportType(latestReport);
            });

        }
    };

    module.directive('sort', function () {
        return {
            restrict: 'A',
            scope: {
                options: '@',
                sort: '='
            },
            link: function (scope, element, attrs) {
                var sort = null, order = null;

                element.bind('click',
                    function () {

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

                scope.$watch('sort', function (newValue) {
                    // elem.find('i').toggleClass('');
                });

                element.append(' <i class="" style="vertical-align: middle; margin-bottom: 4px">');
            }
        };
    });

    module.controller('GreenposListCtrl', [ '$scope', 'GreenposService', 'VesselInformation',
        function ($scope, GreenposService, VesselInformation) {
            $scope.max = 10;
            $scope.recipient = {
                coastalcontrol: true,
                greenpos: true
            };

            $scope.provider = {
                title: "Reports",
                type: "view",
                doShow: false,
                available: function (vesselOverview, vesselDetails) {
                    return vesselDetails.additionalInformation.greenpos;
                },
                show: function (vesselOverview, vesselDetails) {
                    this.doShow = true;
                    $scope.vessel = vesselDetails;

                    GreenposService.findReports({
                        mmsi: $scope.vessel.mmsi,
                        start: 0,
                        max: $scope.max,
                        sort: 'time'
                    }, function (reports) {
                        $scope.reports = reports;
                    });
                },
                shown: function (vesselOverview, vesselDetails) {
                    return this.doShow;
                },
                close: function () {
                    this.doShow = false;
                }
            };

            VesselInformation.addInformationProvider($scope.provider);

            $scope.close = function ($event) {
                $event.preventDefault();
                $scope.provider.close();
            }

            $scope.formatDateTime = function (timeInMillis) {
                return formatTime(timeInMillis);
            };

            $scope.formatCourse = function (course) {
                return formatCourse(course);
            };

            $scope.formatRecipient = function (recipient) {
                switch (recipient) {
                    case 'coastalcontrol':
                        return 'Coastal Control';
                    case 'greenpos':
                        return 'Greenpos';
                }
                ;
                return '';
            };

            $scope.reportText = function (type) {
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

            $scope.filterReports = function (report) {
                return $scope.recipient[report.recipient];
            };
        } ]);

}());
