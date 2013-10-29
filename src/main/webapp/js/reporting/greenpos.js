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

    embryo.GreenPosCtrl = function($scope, $routeParams, ShipService, VoyageService, GreenposService, AisRestService,
            $timeout, RouteService) {
        
        
        $scope.editable = true;

        $scope.report = {
            type : "SP",
        };

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
        };

        ShipService.getYourShip(function(ship) {
            GreenposService.getLatestReport(ship.maritimeId, function(latestReport) {
                evalGreenpos(latestReport);
            });
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
                    $scope.loadMap();
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
                    AisRestService.findVesselsByMmsi(newValue.mmsi, function(searchResult) {
                        var vessels = [];
                        for ( var vesselId in searchResult.vessels) {
                            var vesselJSON = searchResult.vessels[vesselId];
                            var vessel = new Vessel(vesselId, vesselJSON);
                            vessels.push(vessel);
                        }
                        $scope.setVesselsOnMap(vessels);
                    });
                }
            }, true);

            $scope.setVesselsOnMap = function(vessels) {
                var features = [];
                for ( var index in vessels) {
                    var value = vessels[index];
                    var attr = {
                        id : value.id,
                        angle : value.degree - 90,
                        opacity : 1,
                        image : "img/" + value.image,
                        imageWidth : value.imageWidth,
                        imageHeight : value.imageHeight,
                        imageYOffset : value.imageYOffset,
                        imageXOffset : value.imageXOffset,
                        type : "vessel",
                        vessel : value
                    };

                    // transform from WGS 1984 to Spherical Mercator Projection
                    var geom = new OpenLayers.Geometry.Point(value.lon, value.lat).transform(new OpenLayers.Projection(
                            $scope.projection), $scope.map.getProjectionObject());

                    // Use styled vector points
                    features.push(new OpenLayers.Feature.Vector(geom, attr));
                }

                $scope.vesselLayer.removeAllFeatures();
                $scope.vesselLayer.addFeatures(features);
                $scope.vesselLayer.refresh();
            };
        }

        $scope.loadMap = function() {
            $scope.map = new OpenLayers.Map({
                div : "greenPosMap",
                projection : 'EPSG:900913',
                fractionalZoom : false
            });

            var osm = new OpenLayers.Layer.OSM("OSM", "http://a.tile.openstreetmap.org/${z}/${x}/${y}.png", {
                'layers' : 'basic',
                'isBaseLayer' : true
            });
            $scope.map.addLayer(osm);

            var renderer = OpenLayers.Util.getParameters(window.location.href).renderer;
            renderer = (renderer) ? [ renderer ] : OpenLayers.Layer.Vector.prototype.renderers;

            var defTemplate = OpenLayers.Util.applyDefaults({
                strokeWidth : 2,
                strokeColor : "blue", // using context.getColor(feature)
                fillColor : "blue", // using context.getColor(feature)
                graphicName : "x",
                pointRadius : 5
            }, OpenLayers.Feature.Vector.style["default"]);

            $scope.pointLayer = new OpenLayers.Layer.Vector("pointLayer", {
                styleMap : new OpenLayers.StyleMap({
                    'default' : defTemplate
                }),
                renderes : renderer
            });

            $scope.vesselLayer = new OpenLayers.Layer.Vector("staticLayer", {
                styleMap : new OpenLayers.StyleMap({
                    "default" : {
                        externalGraphic : "${image}",
                        graphicWidth : "${imageWidth}",
                        graphicHeight : "${imageHeight}",
                        graphicYOffset : "${imageYOffset}",
                        graphicXOffset : "${imageXOffset}",
                        rotation : "${angle}",
                        strokeDashstyle : 'dash',
                        strokeColor : "red", // using
                        // context.getColor(feature)
                        strokeWidth : 3
                    // strokeOpacity
                    },
                    "select" : {
                        cursor : "crosshair",
                        externalGraphic : "${image}"
                    }
                }),
                renderers : renderer
            });
            $scope.map.addLayer($scope.vesselLayer);
            $scope.map.addLayer($scope.pointLayer);

            var initialLat = 74.00;
            var initialLon = -40.0;
            var initialZoom = 3;

            var center = $scope.transformPosition(initialLon, initialLat);
            $scope.map.setCenter(center, initialZoom);

            if ($scope.report.lat && $scope.report.lon) {
                $scope.setPositionOnMap($scope.report.lat, $scope.report.lon);
            }
        };

        $scope.setPositionOnMap = function(latitude, longitude) {
            if ($scope.pointLayer) {
                var lat = embryo.geographic.parseLatitude(latitude);
                var lon = embryo.geographic.parseLongitude(longitude);

                var point = new OpenLayers.Geometry.Point(lon, lat).transform(new OpenLayers.Projection(
                        $scope.projection), $scope.map.getProjectionObject());

                var pointFeature = new OpenLayers.Feature.Vector(point);
                $scope.pointLayer.removeAllFeatures();
                $scope.pointLayer.addFeatures([ pointFeature ]);
                $scope.pointLayer.refresh();
            }
        };

        $scope.transformPosition = function(lon, lat) {
            // transform from WGS 1984 to Spherical Mercator Projection
            return new OpenLayers.LonLat(lon, lat).transform(new OpenLayers.Projection($scope.projection), $scope.map
                    .getProjectionObject());
        };
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
