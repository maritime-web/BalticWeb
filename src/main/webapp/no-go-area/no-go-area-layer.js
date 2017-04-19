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
                $log.info(
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
    .directive('mapNoGoLayer', ['$rootScope', '$timeout', 'MapService', 'NoGoAreaService', '$log', 'growl',
        function ($rootScope, $timeout, MapService, NoGoAreaService, $log, growl) {
            return {
                restrict: 'E',
                require: '^olMap',
                template:    "<span class='map-no-go-btn'>" +
                                " <span><i class='fa fa-area-chart' aria-hidden='true' ng-click='getNoGoArea()' tooltip='Retrieve No-Go area' ></i></span>" +
                                " <span><i class='fa fa-caret-square-o-right' aria-hidden='true' ng-click='getNextNoGoArea()' tooltip='Next' ></i></span>" +
                             "</span>",
                scope: {
                    name:           '@'
                },
                link: function(scope, element, attrs, ctrl) {
                    var olScope = ctrl.getOpenlayersScope();
                    var noGoLayer;
                    var serviceAvailableLayer;
                    var boundaryLayer;
                    var maxZoom = scope.maxZoom ? parseInt(scope.maxZoom) : 12;

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


                        var boundaryStyle = new ol.style.Style({
                                stroke: new ol.style.Stroke({
                                    color: 'rgba(255, 0, 10, 0.5)',
                                    width: 1
                                }),
                                fill: new ol.style.Fill({
                                    color: 'rgba(255, 0, 10, 0.05)'
                                })
                            });
                        var availableStyle = new ol.style.Style({
                            stroke: new ol.style.Stroke({
                                color: 'rgba(0, 255, 0, 0.8)',
                                width: 2
                            })
                        });


                        // Construct the boundary layers
                        boundaryLayer = new ol.layer.Vector({
                            title: 'NO GO AREA',
                            zIndex: 11,
                            source: new ol.source.Vector({
                                features: new ol.Collection(),
                                wrapX: false
                            }),
                            style: [ boundaryStyle ]
                        });

                        serviceAvailableLayer  = new ol.layer.Vector({
                            title: 'Service Available - NO GO AREA',
                            zIndex: 11,
                            source: new ol.source.Vector({
                                features: new ol.Collection(),
                                wrapX: false
                            }),
                            style: [ availableStyle ]
                        });

                        serviceAvailableLayer.setZIndex(11);
                        serviceAvailableLayer.setVisible(true);

                        serviceAvailableLayer.getSource().clear();
                        try {
                            var olFeature = MapService.wktToOlFeature('POLYGON((9.419409 54.36294,  13.149009 54.36294, 13.149009 56.36316, 9.419409 56.36316, 9.419409 54.36294))');
                            serviceAvailableLayer.getSource().addFeature(olFeature);
                        } catch (error) {
                            console.error("Error displaying Service Available boundary");
                        }

                        boundaryLayer.setZIndex(11);
                        boundaryLayer.setVisible(true);

                        /** Updates the service boundary layer **/
                        scope.updateServiceBoundaryLayer = function () {

                            boundaryLayer.getSource().clear();
                            try {
                                var olFeature = MapService.wktToOlFeature(service.boundary);
                                boundaryLayer.getSource().addFeature(olFeature);
                            } catch (error) {
                                $log.error("Error displaying no go boundary");
                                growl.error("Error displaying no go boundary");
                            }

                        };


                        /***************************/
                        /** Map creation          **/
                        /***************************/

                        // Construct No Go Layer Group layer
                        noGoGroupLayer = new ol.layer.Group({
                            title: scope.name || 'No Go Area',
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
                            // hard coded service limitations...
                            if(l < 9.419409) {l= 9.419409;}
                            if(b < 54.36294) { b = 54.36294;}
                            if(r >  13.149009){ r =  13.149009;}
                            if(t > 56.36316) { t = 56.36316;}
                            return [b , l , t , r ];
                        };


                        scope.getNoGoArea = function(){
                            var olFeature = MapService.wktToOlFeature('POLYGON((9.419409 54.36294,  13.149009 54.36294, 13.149009 56.36316, 9.419409 56.36316, 9.419409 54.36294))');
                            //var olFeatureExtent = MapService.wktToOlFeature(map.getView().extent.toGeometry().toString());

                            serviceAvailableLayer.getSource().addFeature(olFeature);
                            // serviceAvailableLayer.getSource().addFeature(olFeatureExtent);
                            boundaryLayer.getSource().clear();

                            bboxBLTR = scope.clientBBOXAndServiceLimit();
                            var date = new Date();
                            var now = date.toISOString();
                            NoGoAreaService.getNoGoAreas(6, bboxBLTR[0],bboxBLTR[1],bboxBLTR[2],bboxBLTR[3], now).then(
                                function(response) {
                                //console.log(data);
                                    $log.debug("bboxBLTR=" +bboxBLTR + " Time= " + now);
                                    $log.debug("Status=" + response.status);
                                    $log.debug("WKT=" + response.data.wkt);
                                    var olFeature = MapService.wktToOlFeature(response.data.wkt);
                                    boundaryLayer.getSource().addFeature(olFeature);

                                }, function(error) {
                                    boundaryLayer.getSource().clear();
                                    $log.error(error.data.message);
                                    growl.error(error.data.message);

                            });


                            // growl.info("Do a No go area request " + scope.nogoarea);
                        };

                    });
                }
            };
        }]);




