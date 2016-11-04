angular.module('maritimeweb.vessel')
/*******************************************************************
 * Controller that handles displaying vessel details in a dialog
 *  Its
 *******************************************************************/
    .controller('VesselDetailsCtrl', ['$scope', '$routeParams', '$window', 'VesselService', 'growl', 'timeAgo', '$filter', '$location', '$rootScope',
        function ($scope, $routeParams, $window, VesselService, growl, timeAgo, $filter, $location, $rootScope) {
            'use strict';

            console.log("VesselDetailsCtrl routeParams.mmsi=" + $routeParams.mmsi) ;
            $rootScope.showgraphSidebar = false;
            $scope.mmsi = $routeParams.mmsi;
            //$scope.msg = VesselService.detailsMMSI($scope.mmsi);

            VesselService.detailsMMSI($scope.mmsi).then(function(vesselDetails) {
                console.log('Success: ' + vesselDetails);
                $scope.msg = vesselDetails;

            }, function(reason) {
                console.log('Failed: ' + reason);
            });

            $scope.getHistoricalTrack = function (mmsi, type) {
                VesselService.historicalTrack(mmsi).then(function successCallback(response) {
                    growl.success("Retrieved historical points " + response.data.length);

                    var linePoints = [];
                    angular.forEach(response.data, function (value, key) {
                        this.push(ol.proj.transform([value.lon, value.lat], 'EPSG:4326', 'EPSG:900913'));
                    }, linePoints);

                    // Chart.js with speed-over-ground
                    var listSpeedOverGround = [];
                    var listSpeedOverGroundLabels = [];

                    angular.forEach(response.data, function (value, key) {
                        listSpeedOverGround.push(value.sog);
                        listSpeedOverGroundLabels.push($filter('timeAgo')(value.ts) + ' - ' + $filter('date')(value.ts, 'yyyy-MM-dd HH:mm:ss Z', 'UTC') + ' UTC');
                    });

                    $scope.sogChartlabels = listSpeedOverGroundLabels;
                    $scope.sogChartseries = ['Speed over Ground'];
                    $scope.sogChartdata = [listSpeedOverGround];
                    $scope.onClick = function (points, evt) {
                        console.log(points, evt);
                    };

                    $scope.sogChartdatasetOverride = [{
                        yAxisID: 'y-axis-1',
                        borderJoinStyle: 'round',
                        pointRadius: 1,
                        pointHitRadius: 10,
                        pointBorderColor: "rgba(0,0,0,1)",
                        pointBackgroundColor: "#fff",
                        pointBorderWidth: 1
                    }];
                    $scope.sogChartoptions = {
                        responsive: true,
                        responsiveAnimationDuration: 1500,
                        scales: {
                            yAxes: [
                                {
                                    id: 'y-axis-1',
                                    type: 'linear',
                                    display: true,
                                    position: 'left'
                                }
                            ],
                            xAxes: [{
                                display: false
                            }]
                        }
                    };

                    // Map - Features are more detailed
                    var lineFeatures = [];

                    var generateHistoricalMarker = function (value) {
                        var vesselPosition = new ol.geom.Point(ol.proj.transform([value.lon, value.lat], 'EPSG:4326', 'EPSG:900913'));
                        var markerStyle = new ol.style.Style({
                            image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
                                anchor: [0.5, 0.5],
                                opacity: 0.90,
                                id: value.ts,
                                rotation: (value.cog - 90) * (Math.PI / 180),
                                rotateWithView: true,
                                src: 'img/vessel_green.png'
                            }))
                            ,
                            text: new ol.style.Text({
                                text: $filter('timeAgo')(value.ts) + ' - ' + $filter('date')(value.ts, 'yyyy-MM-dd HH:mm:ss', 'UTC') , // attribute code
                                font: 'bold 14 Verdana',
                                offsetY: 40,
                                stroke: new ol.style.Stroke({color: "white", width: 5}),
                                size: 10
                            })
                        });

                        var markerVessel = new ol.Feature({
                            geometry: vesselPosition,
                            cog: value.cog,
                            sog: value.sog,
                            id: value.ts,
                            ts: $filter('date')(value.ts, 'yyyy-MM-dd HH:mm:ss Z', 'UTC') + ' UTC',
                            tsTimeAgo: $filter('timeAgo')(value.ts),
                            position: $scope.toLonLat(value.lon, value.lat)
                        });
                        markerVessel.setStyle(markerStyle);
                        return markerVessel;
                    };
                    /*
                     iterate over the historical track data retrievied in the format
                     [{
                     "cog": 122.9,
                     "lat": 55.723106666666666,
                     "lon": 21.10174333333333,
                     "sog": 0,
                     "ts": 1465473596620
                     },
                     {...}
                     ]
                     */
                    angular.forEach(response.data, function (value, key) {
                        var markerVessel = generateHistoricalMarker(value);
                        this.push(markerVessel);
                    }, lineFeatures);

                    $scope.routeFeatures = lineFeatures;  // detailed description of the historical tracks
                    $scope.routePoints = linePoints;  // simple version of the route, list of [[lon,lat],[lon,lat],...]
                    $scope.historicalTrackOutput = response.data; // the response data
                    return response;

                }, function errorCallback(response) {
                    // called asynchronously if an error occurs
                    // or server returns response with an error status.
                    console.error("Error historicalTrack=" + response.status);
                    growl.error("No historical for this vessel");

                });

            };

            /** Returns the lat-lon attributes of the vessel */
            $scope.toLonLat = function (long, lati) {
                return {lon: long, lat: lati};
            };

            var navStatusTexts = {
                0: "Under way using engine",
                1: "At anchor",
                2: "Not under command",
                3: "Restricted manoeuvrability",
                4: "Constrained by her draught",
                5: "Moored",
                6: "Aground",
                7: "Engaged in fishing",
                8: "Under way",
                12: "Power-driven vessel pushing ahead or towing alongside",
                14: "Ais SART",
                15: "Undefined"
            };

            $scope.navStatusText = function (navStatus) {
                if (navStatus && navStatusTexts.hasOwnProperty(navStatus)) {
                    return navStatusTexts[navStatus]
                }
                return null;
            };
        }]);