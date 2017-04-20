/**
 * Defines the main No-go area layer.
 *
 */
angular.module('maritimeweb.no-go-area')

    /** Service for accessing No Go areas **/
    .service('NoGoAreaService', ['$http', '$log',
        function($http, $log) {

            this.serviceID = function(){ return 'urn:mrn:mcl:service:design:dma:no-go-area'};
            this.serviceVersion = function(){ return '0.1'};

            /**
             * {
              "draught": 6,
              "northWest": {
                "lon": 12,
                "lat": 55.74
              },
              "southEast": {
                "lon": 12.5,
                "lat": 55.48
              },
              "time": "2017-04-06T11:46:22.804Z"
            }
             */

            this.getNoGoAreas = function (draught, se_lat,  nw_lon,  nw_lat, se_lon, time) {
                //  55.36 12.08 55.6 12.66 0
                $log.debug(
                    "{ nw_lon " + nw_lon +
                    ", nw_lat " +  nw_lat +
                        "} ### {" +
                    " se_lon " + se_lon +
                    ", se_lat " + se_lat +
                    "} time " + time);
                var req = {
                    method: 'POST',
                    url: 'http://service-lb.e-navigation.net/nogo/area/wkt',
                    headers: {
                        'Content-Type': 'application/json;charset=UTF-8'
                    },
                    data: {
                        "draught": 6,
                        "northWest": {
                            "lon": nw_lon,      // 12.1,
                            "lat": nw_lat       // 55.74
                        },
                        "southEast": {
                            "lon": se_lon,       //  12.5,
                            "lat": se_lat       // 55.48
                        },
                        "time": time //"2017-04-06T11:46:22.804Z"
                    }
                };
                return $http(req);
            };

        }])

    /**
     * The map-no-Go-Area-Layer directive
     */
    .directive('mapNoGoLayer', ['$rootScope', '$timeout', 'MapService', 'NoGoAreaService', '$log', 'growl', '$interval', 'timeAgo', '$filter',
        function ($rootScope, $timeout, MapService, NoGoAreaService, $log, growl, $interval, timeAgo, $filter) {
            return {
                restrict: 'E',
                require: '^olMap',
                template:    "<span class='map-no-go-btn hidden-xs hidden-sm'>" +
                                "<div>No-Go Zone</div>" +
                                " <span  tooltip='Retrieve No Go Zone' data-toggle='tooltip' data-placement='bottom' title='Retrieve No Go Zone'><i class='fa fa-area-chart' aria-hidden='true' ng-click='getNoGoAreaUI()'  ></i></span> " +
                                " <span  tooltip='Animate No Go Zone ' data-toggle='tooltip' data-placement='bottom' title='Animate'><i class='fa fa-play' aria-hidden='true' ng-click='doGruntAnimation()' ></i></span> " +
                                " <span  tooltip='Retrieve No Go Zone + 1 hour' data-toggle='tooltip' data-placement='bottom' title='Retrieve No Go Zone + 1 hour'><i class='fa fa-step-forward' aria-hidden='true' ng-click='getNextNoGoArea()'  ></i></span> " +
                                " <span  tooltip='Fake Animate No Go Zone ' data-toggle='tooltip' data-placement='bottom' title='Animate'><i class='fa fa-star' aria-hidden='true' ng-click='doFakeGruntAnimation()'  ></i></span> " +

                "</span>",
                scope: {
                    name:           '@'
                },
                link: function(scope, element, attrs, ctrl) {

                    var olScope = ctrl.getOpenlayersScope();
                    var noGoLayer;
                    var noGoGroupLayer;
                    var serviceAvailableLayer;
                    var boundaryLayer;
                    const top_nw_lon = 56.30;
                    const bottom_se_lon = 54.4;
                    const right_nw_lat = 13.0;
                    const left_se_lat = 10.0;
                    scope.draught_counter = 6;

                    /*const top_nw_lon = 56.36316;
                    const bottom_se_lon = 54.36294;
                    const right_nw_lat = 13.149009;
                    const left_se_lat = 9.419409;
                    */

                    olScope.getMap().then(function(map) {


                        // Clean up when the layer is destroyed
                        scope.$on('$destroy', function() {
                            if (angular.isDefined(noGoLayer)) {
                                map.removeLayer(noGoLayer);
                            }
                        });

                        /***************************/
                        /** noGoLayer Layers      **/
                        /***************************/


                        var noGoStyleRed = new ol.style.Style({
                                stroke: new ol.style.Stroke({
                                    color: 'rgba(255, 0, 10, 0.5)',
                                    width: 1
                                }),
                                fill: new ol.style.Fill({
                                    color: 'rgba(255, 0, 10, 0.10)'
                                })
                            });
                        var availableServiceStyle = new ol.style.Style({
                            stroke: new ol.style.Stroke({
                                color: 'rgba(0, 255, 10, 0.8)',
                                width: 3
                            })
                        });


                        // Construct the boundary layers
                        boundaryLayer = new ol.layer.Vector({
                            title: 'Calculated NO GO AREA',
                            zIndex: 11,
                            source: new ol.source.Vector({
                                features: new ol.Collection(),
                                wrapX: false
                            }),
                            style: [ noGoStyleRed ]
                        });

                        serviceAvailableLayer  = new ol.layer.Vector({
                            title: 'Service Available - NO GO AREA',
                            zIndex: 11,
                            source: new ol.source.Vector({
                                features: new ol.Collection(),
                                wrapX: false
                            }),
                            style: [ availableServiceStyle ]
                        });

                        serviceAvailableLayer.setZIndex(12);
                        serviceAvailableLayer.setVisible(true);
                        serviceAvailableLayer.getSource().clear();


                        boundaryLayer.setZIndex(11);
                        boundaryLayer.setVisible(true);


                        /***************************/
                        /** Map creation          **/
                        /***************************/

                        // Construct No Go Layer Group layer
                         var noGoGroupLayer = new ol.layer.Group({
                            title: scope.name || 'No Go Service',
                            zIndex: 11,
                            layers: [ boundaryLayer, serviceAvailableLayer]
                        });
                        noGoGroupLayer.setZIndex(11);
                        noGoGroupLayer.setVisible(true);

                        map.addLayer(noGoGroupLayer);

                        /** get the current bounding box in Bottom left  Top right format. */
                        scope.clientBBOXAndServiceLimit = function () {
                            var bounds = map.getView().calculateExtent(map.getSize());
                            var extent = ol.proj.transformExtent(bounds, MapService.featureProjection(), MapService.dataProjection());
                            var l = Math.floor(extent[0] * 100) / 100;
                            var b = Math.floor(extent[1] * 100) / 100;
                            var r = Math.ceil(extent[2] * 100) / 100;
                            var t = Math.ceil(extent[3] * 100) / 100;


                            if(l < left_se_lat) {l= left_se_lat;}
                            if(b < bottom_se_lon) { b = bottom_se_lon;}
                            if(r >  right_nw_lat){ r =  right_nw_lat;}
                            if(t > top_nw_lon) { t = top_nw_lon;}

                            // hard coded service limitations...
                      /*      if(l < 9.419409) {l = 9.419410;}
                            if(b < 54.36294) { b = 54.36294;}
                            if(r >  13.149009){ r =  13.149010;}
                            if(t > 56.36316) { t = 56.36326;}*/
                            return [b , l , t , r ];
                        };


                        scope.drawServiceLimitation = function() {
                            try {
                                var olServiceActiveArea = MapService.wktToOlFeature('POLYGON(('
                                    + left_se_lat + ' ' + bottom_se_lon + ',  '
                                    + right_nw_lat + ' ' + bottom_se_lon +', '
                                    + right_nw_lat + ' ' + top_nw_lon + ', '
                                    + left_se_lat + ' ' + top_nw_lon + ', '
                                    + left_se_lat +' ' + bottom_se_lon + '))');
                                serviceAvailableLayer.getSource().addFeature(olServiceActiveArea);
                            } catch (error) {
                                $log.error("Error displaying Service Available boundary");
                            }
                        };

                        scope.getNextNoGoArea = function(){
                            if(!scope.time){
                                scope.time = new Date();
                            }
                            scope.time.setHours(scope.time.getHours() + 1);
                            scope.getNoGoArea(scope.time);
                        };

                        scope.getNextFakeNoGoArea = function(){
                            if(!scope.time){
                                scope.time = new Date();
                            }
                            scope.draught_counter = scope.draught_counter +3 ;
                            scope.time.setHours(scope.time.getHours() + 1);
                            scope.getNoGoArea(scope.time, scope.draught_counter);
                        };

                        scope.doGruntAnimation = function(){
                            $log.info("doGruntAnimation");
                            $interval(scope.getNextNoGoArea, 2200, 8);
                        };

                        scope.doFakeGruntAnimation = function(){
                            $log.info("doFakeGruntAnimation");
                            $interval(scope.getNextFakeNoGoArea, 2200, 8);
                        };

                        scope.getNoGoAreaUI = function(){
                            scope.time = new Date();
                            scope.getNoGoArea(scope.time);
                        };




                        scope.getNoGoArea = function(time, draught){
                            scope.drawServiceLimitation();
                            scope.draught_counter = draught ? draught : 6;
                            if(!time){
                                time = new Date();
                            }

                            scope.time = time;

                            var bboxBLTR = scope.clientBBOXAndServiceLimit();
                            var now = time.toISOString();
                            NoGoAreaService.getNoGoAreas(scope.draught_counter, bboxBLTR[0],bboxBLTR[1],bboxBLTR[2],bboxBLTR[3], now).then(
                                function(response) {
                                    $log.debug("bboxBLTR=" +bboxBLTR + " Time= " + now);
                                    $log.debug("Status=" + response.status);
                                    boundaryLayer.getSource().clear();

                                    var olFeature = MapService.wktToOlFeature(response.data.wkt);
                                    boundaryLayer.getSource().addFeature(olFeature);
                                    var timeAgoString = $filter('timeAgo')(scope.time);
                                    growl.info("No-Go zone retrieved and marked with red. <br> "
                                        + scope.draught_counter + " meters draught.<br>"
                                        + timeAgoString + " <br> "+ scope.time.toISOString())
                                }, function(error) {
                                    boundaryLayer.getSource().clear();
                                    $log.error(error.data.message);
                                    growl.error(error.data.message);

                            });

                        };

                    });
                }
            };
        }]);




