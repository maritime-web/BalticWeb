/*
 * Dependencies:
 * 
 * aisview.js
 * aisviewUI.js
 * position.js
 * ....
 */

(function() {
    "use strict";

    var greenposModule = angular.module('embryo.greenpos', [ 'embryo.voyageService', 'embryo.greenposService',
            'embryo.shipService' ]);

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

    embryo.GreenPosCtrl = function($scope, $routeParams, ShipService, VoyageService, GreenposService, AisRestService,
            $timeout, RouteService) {
        $scope.editable = true;

        $scope.report = {
            type : "SP",
            types : [
                { id: "SP", name: "Sailing Plan Report" },
                { id: "PR", name: "Position Report" },
                { id: "FR", name: "Final Report" },
                { id: "DR", name: "Deviation Report" }
            ]
        };

        function evalGreenpos(greenpos) {
            $scope.report.type = "FR";
            $scope.$apply();
            return;
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
        };

        embryo.authenticated(function() {
            if (embryo.authentication.shipMmsi) {
                embryo.vessel.service.subscribe(embryo.authentication.shipMmsi,
                    function(error, vesselOverview, vesselDetails) {
                        if (!error) {
                            GreenposService.getLatestReport(vesselDetails.maritimeId, function(latestReport) {
                                evalGreenpos(latestReport);
                            });
                        }
                    }
                );
            }
        });

        $scope.$on('$viewContentLoaded', function() {
            if (!$scope.map) {
                // postpone map loading sligtly, to let the resize directive set
                // the
                // sizes of the map container divs, before map loading. If not
                // done,
                // the
                // map is not loaded in correct size
                $timeout(function() {
                    // $scope.loadMap();
                }, 50);
            }
        });

        if ($routeParams.id) {
            $scope.editable = false;

            GreenposService.get($routeParams.id, function(report) {
                $scope.report = report;
            });

        } else {
            ShipService.getYourShip(function(yourShip) {
                $scope.report.mmsi = yourShip.mmsi;
                $scope.report.callSign = yourShip.callSign;
                $scope.report.vesselName = yourShip.name;
                $scope.report.vesselMaritimeId = yourShip.maritimeId;
                
                RouteService.getYourActive(yourShip.mmsi, function(route) {
                    $scope.hasActiveRoute = route ? true : false;
                });
            });

            VoyageService.getYourActive(function(voyage) {
                $scope.hasActiveRoute = voyage && voyage.berthName ? true : false;
                $scope.report.destination = voyage.berthName;
                $scope.report.etaOfArrival = voyage.arrival;
                if (voyage.crew) {
                    $scope.report.personsOnBoard = voyage.crew;
                }
                if (voyage.passengers) {
                    if ($scope.report.personsOnBoard) {
                        $scope.report.personsOnBoard += voyage.passengers;
                    } else {
                        $scope.report.personsOnBoard = voyage.passengers;
                    }
                }
            });


        }

        $scope.projection = "EPSG:4326";

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

                if($scope.deactivate){
                    console.log($scope.deactivate);
                    VoyageService.getYourActive(function(voyage) {
                        console.log($scope.deactivate);

                        RouteService.setActiveRoute(voyage.routeId, false, function(){
                            $scope.message += "Active route successsfully deactivated. ";
                        });
                    });
                }
            });
        };

        $scope.cancel = function() {
            console.log($scope.greenPosForm.gpCourse.$error.required);
            console.log($scope.greenPosForm.gpCourse.$error);
        };

        $scope.clear = function() {

        };

        if ($scope.editable) {
            $scope.getShip = function() {
                return {
                    maritimeId : $scope.report.shipMaritimeId,
                    name : $scope.report.shipName,
                    mmsi : $scope.report.mmsi,
                    callSign : $scope.report.callSign
                };
            };

            $scope.$watch($scope.getShip, function(newValue, oldValue) {
                if (newValue.mmsi) {
                    /* AisRestService.findVesselsByMmsi(newValue.mmsi, function(searchResult) {
                        var vessels = [];
                        for ( var vesselId in searchResult.vessels) {
                            var vesselJSON = searchResult.vessels[vesselId];
                            var vessel = new Vessel(vesselId, vesselJSON);
                            vessels.push(vessel);
                        }
                        $scope.setVesselsOnMap(vessels);
                    }); */
                }
            }, true);
        }

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
            layer.clear();
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
