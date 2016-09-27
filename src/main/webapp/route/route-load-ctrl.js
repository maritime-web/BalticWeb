angular.module('maritimeweb.route')
/*******************************************************************
 * Controller that handles uploading an RTZ route for a vessel
 *  and generate the needed open-layers features.
 *******************************************************************/
    .controller('RouteLoadCtrl', ['$scope', '$rootScope', '$http', '$routeParams', '$window', 'growl', 'timeAgo', '$filter', 'Upload', '$timeout', 'fileReader', '$log',
        function ($scope, $rootScope, $http, $routeParams, $window, growl, timeAgo, $filter, Upload, $timeout, fileReader, $log) {
            'use strict';
            $log.debug("RouteLoadCtrl routeParams.mmsi=" + $routeParams.mmsi);
            $scope.activeWayPoint = 0;

            // debug menu starts collapsed.
            $scope.xmlCollapsed = true;
            $scope.jsonCollapsed = true;
            $scope.jsonFeatCollapsed = true;

            //
            var can = document.getElementById('rtzchart');
            var ctx = can.getContext('2d');

            $scope.instantiateListsforCharts = function () {
                var charts = {};
                charts.listSpeed = []; // speed
                charts.listMinSpeed = []; // speed
                charts.listMaxSpeed = []; // speed
                charts.listPortsidextd = [];
                charts.listStarboardxtd = [];
                charts.listWaypointLabels = [];  // speed labels
                charts.listRadius = [];
                charts.listETA = [];
                charts.listID = [];
                return charts;
            };

            var charts = $scope.instantiateListsforCharts();

            $scope.sampleRTZdata = [
                {id: 'ExamplefileworkswithENSI.rtz', name: 'Talin - Helsinki'},
                {id: 'Helsinki_to_Rotterdam_via_Aarhus-Bremerhaven.rtz', name: 'BIMCO - Helsinki to Rotterdam'},
                /*  {id: 'muugaPRVconsprnt.rtz', name: 'Talin - Helsinki'},*/
                {id: 'hesastofuru.rtz', name: 'Helsinki - Stockholm'},
                {id: 'kielPRV.rtz', name: 'Helsinki - Kiel'}
            ];
            $scope.sampleFile = $scope.sampleRTZdata[0].id;

            var resetChartArrays = function () {
                charts.listMinSpeed.splice(0, charts.listMinSpeed.length);
                charts.listMaxSpeed.splice(0, charts.listMaxSpeed.length);
                charts.listStarboardxtd.splice(0, charts.listStarboardxtd.length);
                charts.listPortsidextd.splice(0, charts.listPortsidextd.length);
                charts.listSpeed.splice(0, charts.listSpeed.length);
                charts.listWaypointLabels.splice(0, charts.listWaypointLabels.length);
                charts.listRadius.splice(0, charts.listRadius.length);
                charts.listETA.splice(0, charts.listETA.length);
                charts.listID.splice(0, charts.listID.length);
            };
            /**
             * Adds the feature placeholder to all relevant chart lists.
             * @param feature
             */
            var addFeatureToCharts = function (feature) {
                charts.listMinSpeed.push(feature.speedMin);
                charts.listMaxSpeed.push(feature.speedMax);
                charts.listStarboardxtd.push(feature.starboardXTD);
                charts.listPortsidextd.push(feature.portsideXTD);
                charts.listSpeed.push(feature.speed);
                charts.listWaypointLabels.push(feature.wayname);
                charts.listRadius.push(feature.radius);
                charts.listETA.push(feature.eta);
                charts.listID.push(feature.id);
            };

            var calculateDistanceAndDirectionToNextPoint = function (key, json_result, way_value, feature) {
                if (key + 1 < json_result.route.waypoints.waypoint.length) {
                    $log.log(key);
                    $log.log("#" + key + "Next point #" + (key + 1) + " " + JSON.stringify(json_result.route.waypoints.waypoint[key + 1].position) + " compared to" + JSON.stringify(way_value.position));
                    /* $log.log("getBearing:" + geolib.getBearing(
                     {latitude: 52.518611, longitude: 13.408056},
                     {latitude: 51.519475, longitude: 7.46694444}
                     ));

                     $log.log("getRhumbLineBearing:" + geolib.getRhumbLineBearing(
                     {latitude: 52.518611, longitude: 13.408056},
                     {latitude: 51.519475, longitude: 7.46694444}
                     ));

                     $log.log("getDistance meter:" + geolib.getDistance(
                     {latitude: 52.518611, longitude: 13.408056},
                     {latitude: 51.519475, longitude: 7.46694444}
                     ));

                     $log.log("getDistance km:" + geolib.convertUnit('km',geolib.getDistance(
                     {latitude: 52.518611, longitude: 13.408056},
                     {latitude: 51.519475, longitude: 7.46694444})
                     ));*/
                    var next_point_lat = json_result.route.waypoints.waypoint[key + 1].position._lat;
                    var next_point_lon = json_result.route.waypoints.waypoint[key + 1].position._lon;
                    var current_pos = {latitude: way_value.position._lat, longitude: way_value.position._lon};
                    var next_pos = {latitude: next_point_lat, longitude: next_point_lon};

                    feature['distance'] = geolib.convertUnit('sm', geolib.getDistance(current_pos, next_pos));

                    if (feature['geometryType'] === 'Orthodrome'){ // A great circle, also known as an orthodrome
                        feature['direction'] = geolib.getBearing(current_pos, next_pos);
                        $log.log("getBearing:" + feature['direction'
                                ]   );
                    } else{
                        // Loxodrome (or rhumb line) is a line crossing all meridians at a constant angle.
                        feature['direction'] = geolib.getRhumbLineBearing(current_pos, next_pos);
                        $log.log("getRhumbLineBearing:" + feature['direction']   );
                    }
                }
            };

            /**
             * Generate a openlayers features array, animated features and ol path points array from the transformed RTZ JSON.
             * @param json_result transformed RTZ JSON from an RTZ xml
             */
            var createOpenLayersFeatFromRTZ = function (json_result) {
                $scope.rtzJSON = json_result; // used for debugging.
                $scope.rtzName = json_result.route.routeInfo._routeName;

                $scope.oLfeatures = []; // openlayers features
                $scope.oLanimatedfeatures = []; // openlayers animated features
                $scope.oLpoints = [];
                $scope.totaldistance = 0;

                var defaultWayPoint = {}; // standard waypoint
                if (json_result.route.waypoints.defaultWaypoint) {
                    defaultWayPoint = json_result.route.waypoints.defaultWaypoint;
                    $log.info("defaultWayPoint" + JSON.stringify(defaultWayPoint));
                }

                var calculateDistanceAndDirection = function (key, way_value, feature) {
                    /**
                     * Calculate the distance and the angle given the current point
                     */
                    if (key + 1 < json_result.route.waypoints.waypoint.length) {
                        var next_point_lat = json_result.route.waypoints.waypoint[key + 1].position._lat;
                        var next_point_lon = json_result.route.waypoints.waypoint[key + 1].position._lon;
                        var current_pos = {latitude: way_value.position._lat, longitude: way_value.position._lon};
                        var next_pos = {latitude: next_point_lat, longitude: next_point_lon};

                        feature['distance'] = geolib.convertUnit('sm', geolib.getDistance(current_pos, next_pos));
                        // $log.log("getDistance:" + feature['distance']   );
                        $scope.totaldistance += feature['distance'];

                        if (feature['geometryType'] === 'Orthodrome') { // A great circle, also known as an orthodrome
                            feature['direction'] = geolib.getBearing(current_pos, next_pos);
                              $log.log("getBearing:" + feature['direction']   );
                        } else { // Loxodrome (or rhumb line) is a line crossing all meridians at a constant angle.
                            feature['direction'] = geolib.getRhumbLineBearing(current_pos, next_pos);
                             $log.log("getRhumbLineBearing:" + feature['direction']   );
                        }

                        //feature['radian'] = feature['direction'] ? ((feature['direction'] - 90) * (Math.PI / 180)) : 0;
                        feature['radian'] =  ((feature['direction'] - 90) * (Math.PI / 180));

                        if (!feature['direction'] && feature['direction']!=0 ) {
                            feature['direction'] = 0.0;
                            feature['radian'] = 0.0;
                            $log.error("its undefined. Feature" + JSON.stringify(feature));

                        }
                    } else {
                        feature['direction'] = 0.0;
                        feature['radian'] = 0.0;

                    }
                };
                if (!json_result.route.waypoints.waypoint &&
                    json_result.route.waypoints.waypoint.length > 0) {
                    growl.error("No Waypoints in RTZ file");
                    $log.error("No Waypoints in RTZ file");
                } else {
                    angular.forEach(json_result.route.waypoints.waypoint, function (way_value, key) {

                        $scope.oLpoints.push(ol.proj.transform([parseFloat(way_value.position._lon), parseFloat(way_value.position._lat)], 'EPSG:4326', 'EPSG:900913'));

                        var feature = {
                            id: way_value._id,
                            wayname: way_value._name,
                            radius: (way_value._radius) ? way_value._radius : defaultWayPoint._radius,
                            position: way_value.position,
                            leg: (way_value.leg) ? way_value.leg : defaultWayPoint.leg
                        };

                        if (defaultWayPoint.leg) {
                            feature['speedMin'] = (defaultWayPoint.leg._speedMin) ? defaultWayPoint.leg._speedMin : '';
                            feature['speedMax'] = (defaultWayPoint.leg._speedMax) ? defaultWayPoint.leg._speedMax : '';
                            feature['geometryType'] = (defaultWayPoint.leg._geometryType) ? defaultWayPoint.leg._geometryType : '';
                            feature['portsideXTD'] = (defaultWayPoint.leg._portsideXTD) ? defaultWayPoint.leg._portsideXTD : '';
                            feature['starboardXTD'] = (defaultWayPoint.leg._starboardXTD) ? defaultWayPoint.leg._starboardXTD : '';
                        }

                        if (way_value.leg) {
                            feature['speedMin'] = (way_value.leg._speedMin) ? way_value.leg._speedMin : feature['speedMin'];
                            feature['speedMax'] = (way_value.leg._speedMax) ? way_value.leg._speedMax : feature['speedMax'];
                            feature['geometryType'] = (way_value.leg._geometryType) ? way_value.leg._geometryType : feature['geometryType'];
                            feature['portsideXTD'] = (way_value.leg._portsideXTD) ? way_value.leg._portsideXTD : feature['portsideXTD'];
                            feature['starboardXTD'] = (way_value.leg._starboardXTD) ? way_value.leg._starboardXTD : feature['starboardXTD'];
                        }
                        calculateDistanceAndDirection(key, way_value, feature);

                        if (typeof(json_result.route.schedules.schedule.calculated) !== "undefined") {

                            angular.forEach(json_result.route.schedules.schedule.calculated.sheduleElement, function (schedule_value, key) {
                                if (way_value._id == schedule_value._waypointId) { // pairing schedule events with waypoints
                                    feature['speed'] = schedule_value._speed;
                                    feature['eta'] = schedule_value._eta;
                                    feature['etats'] = Date.parse(schedule_value._eta);
                                    feature['etatimeago'] = $filter('timeAgo')(Date.parse(schedule_value._eta));
                                }
                            });
                        } else {
                            growl.error("No schedule for route");
                        }

                        addFeatureToCharts(feature);
                        $scope.oLfeatures.push($scope.createWaypointFeature(feature));
                        $scope.oLanimatedfeatures.push($scope.createAnimatedWaypointFeature(feature));
                    });
                }

            };

            /**
             * convience method for loading a sample rtz route
             */
            $scope.autoPreloadRTZfile = function () {
                $http.get('/route/sample-rtz-files/' + $scope.sampleFile, {
                    transformResponse: function (data, headers) {
                        $scope.rtzXML = data;
                        $scope.rtzJSON = fileReader.transformRtzXMLtoJSON(data);
                        return $scope.rtzJSON;
                    }
                }).then(function (result) {
                    resetChartArrays();
                    createOpenLayersFeatFromRTZ(result.data);
                });
            };

            $scope.getFile = function () {
                $scope.progress = 0;
                fileReader.readAsDataUrl($scope.file, $scope)
                    .then(function (result) {
                        $scope.rtzXML = result;
                        $scope.rtzJSON = fileReader.transformRtzXMLtoJSON(result);
                        resetChartArrays();
                        createOpenLayersFeatFromRTZ($scope.rtzJSON);
                    });
            };

            $scope.$on("fileProgress", function (e, progress) {
                $scope.progress = progress.loaded / progress.total;
            });

            /**
             * Creates a openlayer feature from a position and a waypoint
             * @param waypointPosition
             * @param waypoint
             * @returns {ol.Feature}
             */
            $scope.createOpenLayerFeature = function (waypointPosition, waypoint) {
                var markWaypoint = new ol.Feature({
                    geometry: waypointPosition,
                    name: waypoint.wayname,
                    wayname: waypoint.wayname,
                    id: waypoint.id,
                    lon: waypoint.position._lon,
                    lat: waypoint.position._lat,
                    radius: waypoint.radius,
                    eta: waypoint.eta,
                    speed: waypoint.speed,
                    //leg: waypoint.leg,
                    speedmax: waypoint.speedMax,
                    speedmin: waypoint.speedMin,
                    geometrytype: waypoint.geometryType,
                    portsidextd: waypoint.portsideXTD,
                    starboardxtd: waypoint.starboardXTD,
                    distance: waypoint.distance,
                    direction: waypoint.direction,
                    radian: waypoint.radian,
                    //ts: $filter('date')(value.ts, 'yyyy-MM-dd HH:mm:ss Z', 'UTC') + ' UTC',
                    etatimeago: waypoint.etatimeago,
                    position: $scope.toLonLat(waypoint.position._lon, waypoint.position._lat)
                });
                return markWaypoint;
            };

            /** Create a waypoint feature, with  lat,lon,. */
            $scope.createWaypointFeature = function (waypoint) {
                var markerStyle = new ol.style.Style({
                    image: new ol.style.Circle({
                        radius: 3,
                        stroke: new ol.style.Stroke({
                            color: 'red',
                            width: 2
                        }),
                        fill: new ol.style.Fill({
                            color: [255, 0, 0, 0.5]
                        })
                    })
                });

                var waypointPosition = new ol.geom.Point(ol.proj.transform([parseFloat(waypoint.position._lon), parseFloat(waypoint.position._lat)], 'EPSG:4326', 'EPSG:900913'));

                var markWaypoint = this.createOpenLayerFeature(waypointPosition, waypoint);
                markWaypoint.setId(waypoint.id);
                markWaypoint.setStyle(markerStyle);
                return markWaypoint;
            };

            /** Create a waypoint feature, with  lat,lon,. */
            $scope.createAnimatedWaypointFeature = function (waypoint) {

                var waypointPosition = new ol.geom.Point(ol.proj.transform([parseFloat(waypoint.position._lon), parseFloat(waypoint.position._lat)], 'EPSG:4326', 'EPSG:900913'));
                var markWaypoint = this.createOpenLayerFeature(waypointPosition, waypoint);
                markWaypoint.setId(waypoint.id);
                var animatedMarkerStyle;
                if(!markWaypoint.get('radian') && markWaypoint.get('radian') == 0){
                    $log.debug("radian is not defined ID=" + markWaypoint.get('id'));
                    markWaypoint['radian'] = 0;
                     animatedMarkerStyle = new ol.style.Style({
                        image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
                            anchor: [0.5, 0.5],
                            anchorXUnits: 'fraction',
                            anchorYUnits: 'fraction',
                            opacity: 0.85,
                            rotation:  markWaypoint.get('radian'),
                            rotateWithView: false,
                            src: 'img/vessel_green_moored.png'
                        }))
                    });

                }else {
                     animatedMarkerStyle = new ol.style.Style({
                        image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
                            anchor: [0.5, 0.5],
                            anchorXUnits: 'fraction',
                            anchorYUnits: 'fraction',
                            opacity: 0.85,
                            rotation:  markWaypoint.get('radian'),
                            rotateWithView: false,
                            src: 'img/vessel_green.png'
                        }))
                    });
                }
                markWaypoint.setStyle(animatedMarkerStyle);
                return markWaypoint;
            };


            // while watch if a new RTZ route has been uploaded
            $scope.$watch("sampleFile", function (newValue, oldValue) {
                if (newValue) {
                    $log.log("sample file uploaded" + $scope.sampleFile);
                    $scope.autoPreloadRTZfile(); // TODO: disable the auto load later on
                    $window.scrollTo(0, 0);
                }
            }, true);


            // while watch if a new active waypoint has been selected via the chart or the table. If so, we pop the popup for that Openlayer Feature.
            $rootScope.$watch("activeWayPoint", function (newValue, oldValue) {
                if (newValue) {
                    $log.log("update active waypoint " + newValue + " ");
                    //$log.log(can);

                    // we need to draw something on the chart. Something that can indicate the current active waypoint we
                    var chart_x_coord = (can.width / $scope.oLfeatures.length - 1 ) * ($rootScope.activeWayPoint);
                    ctx.beginPath();
                    ctx.moveTo(chart_x_coord, 0);
                    ctx.lineTo(chart_x_coord, can.height);
                    ctx.lineWidth = 4;
                    ctx.strokeStyle = '#ff0000';
                    //ctx.stroke();
                    ctx.beginPath();
                    ctx.moveTo(chart_x_coord + 3, 0);
                    ctx.lineTo(chart_x_coord + 3, can.height);
                    ctx.lineWidth = 1;
                    ctx.strokeStyle = '#000000';
                    // ctx.stroke();
                }

            }, true);
            // $scope.autoPreloadRTZfile(); // TODO: disable the auto load later on

            // SPEED Charts
            // Chart.js with speed-over-ground

            /* angular.forEach(response.data, function (value, key) {
             listMinSpeed.push(value.sog);
             listWaypointLabels.push($filter('timeAgo')(value.ts) + ' - ' + $filter('date')(value.ts, 'yyyy-MM-dd HH:mm:ss Z', 'UTC') + ' UTC');
             });
             */
            $scope.sogChartlabels = charts.listWaypointLabels;
            $scope.sogChartseries = ['Speed', 'Min. speed', 'Max. speed', 'starboard', 'portside', 'radius'];
            $scope.sogChartdata = [charts.listSpeed, charts.listMinSpeed, charts.listMaxSpeed, charts.listStarboardxtd, charts.listPortsidextd, charts.listRadius];
            $scope.onClick = function (points, evt) {
                angular.forEach(points, function (value, key) {
                    if (value._index != null && value._index >= 0) {
                        $rootScope.activeWayPoint = (points[0]._index + 1);
                    }
                });
                console.log("#" + $rootScope.activeWayPoint);
                $rootScope.$apply();
            };
            $scope.colours = [{ // default
                "fillColor": "rgba(224, 108, 112, 1)",
                "strokeColor": "rgba(207,100,103,1)",
                "pointColor": "rgba(220,220,220,1)",
                "pointStrokeColor": "#fff",
                "pointHighlightFill": "#fff",
                "pointHighlightStroke": "rgba(151,187,205,0.8)"
            }];


            $scope.sogChartdatasetOverride = [{
                yAxisID: 'y-axis-1',
                // yAxisID: 'y-axis-2',
                borderJoinStyle: 'round',
                pointRadius: 1,
                pointHitRadius: 10,
                pointBorderColor: "rgba(0,0,0,1)",
                pointBackgroundColor: "#fff",
                pointBorderWidth: 1
            }];
            $scope.sogChartoptions = {
                responsive: true,
                showLines: true,

                legend: {
                    display: true,
                    labels: {
                        fontColor: 'rgb(100, 149, 237)'
                    }
                },
                responsiveAnimationDuration: 1500,
                scales: {
                    yAxes: [
                        {
                            id: 'y-axis-1',
                            type: 'linear',
                            label: 'Speed',
                            display: true,
                            position: 'left'
                        }
                        /*       ,
                         {
                         id: 'y-axis-2',
                         type: 'linear',
                         label: 'radius',
                         display: true,
                         position: 'right'
                         }*/
                    ],
                    xAxes: [{
                        display: true
                    }]
                }
            };

            /** Returns the lat-lon attributes of the vessel */
            $scope.toLonLat = function (long, lati) {
                return {lon: long, lat: lati};
            };

            /**
             * select id from table row
             * @param idSelectedRow
             */
            $scope.setSelected = function (idSelectedRow) {
                $rootScope.activeWayPoint = idSelectedRow;
            };


        }]);

