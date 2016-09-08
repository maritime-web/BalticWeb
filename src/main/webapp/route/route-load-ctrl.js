angular.module('maritimeweb.route')
/*******************************************************************
 * Controller that handles uploading an RTZ route for a vessel
 *  and generate the needed open-layers features.
 *******************************************************************/
    .controller('RouteLoadCtrl', ['$scope', '$http', '$routeParams', '$window', 'VesselService', 'growl', 'timeAgo', '$filter', 'Upload', '$timeout', 'fileReader', '$log',
        function ($scope, $http, $routeParams, $window, VesselService, growl, timeAgo, $filter, Upload, $timeout, fileReader,$log) {
            'use strict';
            console.log("RouteLoadCtrl routeParams.mmsi=" + $routeParams.mmsi);

            // debug menu starts collapsed.
            $scope.xmlCollapsed = true;
            $scope.jsonCollapsed = true;
            $scope.jsonFeatCollapsed = true;


            $scope.sampleRTZdata = [
                {id: 'ExamplefileworkswithENSI.rtz', name: 'Talin - Helsinki'},
              /*  {id: 'muugaPRVconsprnt.rtz', name: 'Talin - Helsinki'},*/
                {id: 'hesastofuru.rtz', name: 'Helsinki - Stockholm'},
                {id: 'kielPRV.rtz', name: 'Helsinki - Kiel'}
            ];
            $scope.sampleFile = $scope.sampleRTZdata[0].id;
            /**
             * Generate a openlayers features array and ol points array from the transformed RTZ JSON.
             * @param json_result transformed RTZ JSON from an RTZ xml
             */
            var createOpenLayersFeatFromRTZ = function (json_result) {

                $scope.rtzJSON = json_result; // used for debugging.
                $scope.rtzName = json_result.route.routeInfo._routeName;

                $scope.oLfeatures = [];
                $scope.oLpoints = [];

                angular.forEach(json_result.route.waypoints.waypoint, function (way_value, key) {

                    angular.forEach(json_result.route.schedules.schedule.calculated.sheduleElement, function (schedule_value, key) {

                        if (way_value._id == schedule_value._waypointId) { // pairing schedule events with waypoints
                            var feature = {
                                id: way_value._id,
                                wayname: way_value._name,
                                radius: way_value._radius,
                                position: way_value.position,
                                leg: way_value.leg,
                                speed: schedule_value._speed,
                                eta: schedule_value._eta
                            };

                            $scope.oLpoints.push( ol.proj.transform([parseFloat(way_value.position._lon), parseFloat(way_value.position._lat)], 'EPSG:4326', 'EPSG:900913'));
                            $scope.oLfeatures.push($scope.createWaypointFeature(feature));
                        }
                    });
                });

            };

            /**
             * convience method for loading a sample rtz route
             */
            $scope.autoPreloadRTZfile = function(){
                $http.get('/route/sample-rtz-files/' + $scope.sampleFile, {
                    transformResponse: function (data, headers) {
                        $scope.rtzXML = data;
                        $scope.rtzJSON = fileReader.transformRtzXMLtoJSON(data);
                        return $scope.rtzJSON;
                    }
                }).then(function(result){
                    createOpenLayersFeatFromRTZ(result.data);
                });
            };

            $scope.getFile = function () {
                $scope.progress = 0;
                fileReader.readAsDataUrl($scope.file, $scope)
                    .then(function(result) {
                        $scope.rtzXML = result;
                        $scope.rtzJSON = fileReader.transformRtzXMLtoJSON(result);
                        createOpenLayersFeatFromRTZ($scope.rtzJSON);
                    });
            };

            $scope.$on("fileProgress", function(e, progress) {
                $scope.progress = progress.loaded / progress.total;
            });

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

                var markWaypoint = new ol.Feature({
                    geometry: waypointPosition,
                    name: waypoint.wayname,
                    wayname: waypoint.wayname,
                    id: waypoint.id,
                    radius: waypoint.radius,
                    eta: waypoint.eta,
                    speed: waypoint.speed,
                    leg: waypoint.leg,
                    //ts: $filter('date')(value.ts, 'yyyy-MM-dd HH:mm:ss Z', 'UTC') + ' UTC',
                    //tsTimeAgo: $filter('timeAgo')(value.ts),
                    position: $scope.toLonLat(waypoint.position._lon, waypoint.position._lat)
                });
                markWaypoint.setStyle(markerStyle);
                return markWaypoint;
            };

            // while watch if a new RTZ route has been uploaded
            $scope.$watch("sampleFile", function(newValue, oldValue) {
                if (newValue){
                    $log.log("sample file uploaded" + $scope.sampleFile)
                    $scope.autoPreloadRTZfile(); // TODO: disable the auto load later on
                }


            }, true);
            $scope.autoPreloadRTZfile(); // TODO: disable the auto load later on
        }]);

