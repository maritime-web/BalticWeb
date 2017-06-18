/**
 * Defines the main No-go area layer.
 *
 */
angular.module('maritimeweb.no-go-area')

    /** Service for accessing No Go areas **/
    .service('NoGoAreaService', ['$http', '$log',
        function($http, $log) {

        /*
            NOGO - is still not a service in the service registry
         */
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
                    url: 'https://service-lb.e-navigation.net/nogo/area/wkt',
                    headers: {
                        'Content-Type': 'application/json;charset=UTF-8'
                    },
                    data: {
                        "draught": draught,
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
    .directive('mapNoGoLayer', ['$rootScope', '$timeout', 'MapService', 'NoGoAreaService', '$log', 'growl', '$interval', 'timeAgo', '$filter', 'Auth',
        function ($rootScope, $timeout, MapService, NoGoAreaService, $log, growl, $interval, timeAgo, $filter, Auth) {
            return {
                restrict: 'E',
                require: '^olMap',
                template:'',
                /*'<div class="map-no-go-btn panel panel-default">' +
                '<div class="panel-heading" ng-click="noGoCollapsed = !noGoCollapsed">' +
                'No Go <i  ng-if="!noGoCollapsed" class="fa fa-caret-down" aria-hidden="true"></i> <button class="pull-right" ng-if="noGoCollapsed" >  <i  class="fa fa-caret-up" aria-hidden="true"></i></button>' +
                '<button ng-if="loading"><i class="fa fa-cog fa-spin fa-lg fa-fw"></i><span class="sr-only">Loading...</span></button>' +
                '<button ng-if="noGoCollapsed && loggedIn" class="pull-right" ng-click="clearNoGo()">Clear <i   class="fa fa-undo" aria-hidden="true"></i></button>' +
                '</div>' +

                '<div uib-collapse="!noGoCollapsed" class="panel-body">' +
                    "<div ng-if='loggedIn'>" +
                        "<div class='well well-sm'>  <br>" +
                        "<div>Check if a vessel can pass safely. Enter the minimum required sea level depth.</div>" +
                            "<div class='form-group '>" +

                            "<div><label for='depth'><small>Minimum depth:</small></label> <input id='depth' type='number' ng-model='ship.draught' class='form-control input-sm col-lg-1'> </input> </div>" +
                            "</div>" +
                            "<div>" +
                                 "<button data-toggle='tooltip' data-placement='bottom' title='Retrieve No Go Zone' ng-click='getNoGoAreaUI()'><i class='fa fa-area-chart' aria-hidden='true'  ></i> Mark non-safe area red</button> " +
                            "</div>" +
                        "</div>" +
                    "<div class='well well-sm'> Start an animation where the time is increased with 1 hour.<br> Usefull in areas with high tidal where sea depth levels changes during the day.   <br>" +
                    "<div><label>Ago:</label>  {{timeAgoString}}</div>" +
                    "<div><label>Time:</label>  <small> {{time}} </small>" + "</div>" + // <input type='text' ng-model='time' class='form-control small'> </input>
                    "<button ng-click='doGruntAnimation()' data-toggle='tooltip' data-placement='bottom' title='Animate - Increase time for zone with one hour'><i class='fa fa-play' aria-hidden='true'  ></i> Time animation</button> " +
                    //" <span data-toggle='tooltip' data-placement='bottom' title='Retrieve No Go Zone + 1 hour'><i class='fa fa-step-forward' aria-hidden='true' ng-click='getNextNoGoArea()'  ></i></span> " +
                    "</div>" +

                    "<div class='well well-sm'> Start an animation where the minimum depth is increased with 0.5 meters. <br>" +
                    " <button  ng-click='doIncreaseDraughtAnimation()' data-toggle='tooltip' data-placement='bottom' title='Animate - Increase min. depth 0,5 meters'><i class='fa fa-play' aria-hidden='true'  ></i>Depth animation</button> " +
                    "</div>" +
                "</div>" +
                "</div>" +
                "<div ng-if='!loggedIn && noGoCollapsed' class='well well-sm'>" +
                "<p>Login is required</p>" +
                "<button class='btn btn-default' ng-click='login()'>Login</button>" +
                "</div>" +
                "</div>" +
                '</div>' +
                '</div>',*/

                link: function(scope, element, attrs, ctrl) {

                    var olScope = ctrl.getOpenlayersScope();
                    var noGoLayer;
                    var noGoGroupLayer;
                    var serviceAvailableLayer;
                    var boundaryLayer;
                    var loadTimer;

                    const top_nw_lon = 56.30;
                    const bottom_se_lon = 54.4;
                    const right_nw_lat = 13.0;
                    const left_se_lat = 10.0;
                   /* scope.ship = {};
                    scope.ship.draught = 6;
                    scope.time = new Date();
                    scope.timeAgoString = "";
                    scope.loading = false;
                    scope.animating = false;
*/
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
                            name: 'NoGoArea',
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
                            layers: [  serviceAvailableLayer, boundaryLayer]
                        });
                        noGoGroupLayer.setZIndex(11);
                        noGoGroupLayer.setVisible(true);

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

                        //scope.drawServiceLimitation();

                        map.addLayer(noGoGroupLayer);
                        $rootScope.mapNoGoLayer = noGoGroupLayer;


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




                        scope.clearNoGo = function() {
                            $log.info("Clear no go");
                            serviceAvailableLayer.getSource().clear();
                            boundaryLayer.getSource().clear();
                            scope.loading = false;
                        };

                        scope.getNextNoGoArea = function(){
                            if(!scope.time){
                                scope.time = new Date();
                            }
                            scope.time.setHours(scope.time.getHours() + 1);

                            scope.getNoGoArea(scope.time);
                        };

                        scope.getNextNoGoAreaIncreaseDraught = function(){
                            if(!scope.time){
                                scope.time = new Date();
                            }
                            scope.ship.draught = scope.ship.draught + 0.5 ;

                            scope.getNoGoArea(scope.time);
                        };

                        scope.doGruntAnimation = function(){
                            $log.info("doGruntAnimation");
                            $interval(scope.getNextNoGoArea, 2200, 8);
                        };

                        scope.doIncreaseDraughtAnimation = function(){
                            $log.info("doIncreaseDraughtAnimation");
                            $interval(scope.getNextNoGoAreaIncreaseDraught, 2200, 8);
                        };

                        scope.getNoGoAreaUI = function(){
                            scope.time = new Date();
                           // scope.ship_draught = scope.ship_draught +0.0;

                            scope.getNoGoArea(scope.time);
                        };

                        /** When the map extent changes, reload the nogo's using a timer to batch up changes */
                        scope.mapChanged = function () {
                            //if (MapService.isLayerVisible('NoGoArea', noGoGroupLayer)) {
                            if (noGoGroupLayer.getVisible()) {
                                if (loadTimer) {
                                    $timeout.cancel(loadTimer);
                                }
                                loadTimer = $timeout(scope.getNoGoAreaUI(), 5000);
                            }
                        };

                        scope.getNoGoArea = function(){

                            $log.info("$rootScope.mapNoGoLayer =" + scope.mapNoGoLayer);
                            $log.info("$rootScope.nogo =" + scope.nogo);
                            $log.info("$rootScope.nogo.ship.draught =" + scope.nogo.ship.draught);
                            $log.info("$rootScope.nogo.timeAgoString =" + scope.nogo.timeAgoString);
                            $log.info("$rootScope.nogo.time =" + scope.nogo.time);
                            $log.info("$rootScope.nogo.loading =" + scope.nogo.loading);
                            $log.info("$rootScope.nogo.animating =" + scope.nogo.animating);
                            scope.nogo.loading = true;
                           scope.drawServiceLimitation();



                            var bboxBLTR = scope.clientBBOXAndServiceLimit();
                            var now = scope.nogo.time.toISOString();
                            NoGoAreaService.getNoGoAreas(scope.nogo.ship.draught, bboxBLTR[0],bboxBLTR[1],bboxBLTR[2],bboxBLTR[3], now).then(
                                function(response) {
                                    $log.debug("bboxBLTR=" +bboxBLTR + " Time= " + now);
                                    $log.debug("Status=" + response.status);
                                    boundaryLayer.getSource().clear();

                                    var olFeature = MapService.wktToOlFeature(response.data.wkt);
                                    boundaryLayer.getSource().addFeature(olFeature);
                                    scope.nogo.loading= false;
                                    scope.nogo.timeAgoString = $filter('timeAgo')(scope.nogo.time);
                                    growl.info("No-Go zone retrieved and marked with red. <br> "
                                        + scope.nogo.ship.draught + " meters depth.<br>"
                                        + scope.nogo.timeAgoString + " <br> "+ scope.nogo.time.toISOString());
                                }, function(error) {
                                    scope.nogo.loading= false;
                                    boundaryLayer.getSource().clear();
                                    $log.error(error);
                                    if(error.data.message){
                                        growl.error(error.data.message);
                                    }
                            });

                        };

                        // update the map when a user pan-move ends.
                        //map.on('moveend', scope.mapChanged);

                        // listens when visibility on map has been toggled.
                        boundaryLayer.on('change:visible', scope.mapChanged);
                        noGoGroupLayer.on('change:visible', scope.mapChanged);

                        /*
                        scope.$watch("nogo.time", console.log("times changing" + scope.nogo.time),true);
                        scope.$watch("nogo.ship.draught", console.log("depth changing" + scope.nogo.ship.draught), true);
                        */

                        scope.loggedIn = Auth.loggedIn;

                        scope.login = function () {
                            Auth.authz.login();
                        };

                    });
                }
            };
        }]);




