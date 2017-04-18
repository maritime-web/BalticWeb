/**
 * Defines the main No-go area layer.
 *
 */
angular.module('maritimeweb.no-go-area')

    /** Service for accessing No Go areas **/
    .service('NoGoAreaService', ['$http',
        function($http) {

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

            this.getNoGoAreas = function (draught, nw_lon, nw_lat, se_lon, se_lat, time) {
                var req = {
                    method: 'POST',
                    url: 'http://service-lb.e-navigation.net/nogo/area/wkt',
                    headers: {
                        'Content-Type': 'application/json;charset=UTF-8'
                    },
                    data: {
                        "draught": 6,
                        "northWest": {
                            "lon": 12.1,
                            "lat": 55.74
                        },
                        "southEast": {
                            "lon": 12.5,
                            "lat": 55.48
                        },
                        "time": "2017-04-18T11:46:22.804Z"
                    }
                };

                $http(req).then(function(data) {
                    console.log(data);
                    return data;
                }, function(error) {
                    console.log(error);
                    return error;
                });
            };

        }])

    /**
     * The map-no-Go-Area-Layer directive
     */
    .directive('mapNoGoLayer', ['$rootScope', '$timeout', 'MapService', 'NoGoAreaService',
        function ($rootScope, $timeout, MapService, NoGoAreaService) {
            return {
                restrict: 'E',
                require: '^olMap',
                template:    "<span class='map-no-go-btn'>" +
                                " <span><i class='fa fa-area-chart' aria-hidden='true' ng-click='getNoGoArea()' tooltip='getNoGoArea' ></i></span>" +
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



                        /***************************/
                        /** Message List Handling **/
                        /***************************/


                        /** Updates the service boundary layer **/
                        scope.updateServiceBoundaryLayer = function () {

                            boundaryLayer.getSource().clear();
                            try {
                                var olFeature = MapService.wktToOlFeature(service.boundary);
                                boundaryLayer.getSource().addFeature(olFeature);
                            } catch (error) {
                                console.error("Error displaying no go boundary");
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
                        scope.clientBBOX = function () {
                            var bounds = map.getView().calculateExtent(map.getSize());
                            var extent = ol.proj.transformExtent(bounds, MapService.featureProjection(), MapService.dataProjection());
                            var l = Math.floor(extent[0] * 100) / 100;
                            var b = Math.floor(extent[1] * 100) / 100;
                            var r = Math.ceil(extent[2] * 100) / 100;
                            var t = Math.ceil(extent[3] * 100) / 100;
                            return [b , l , t , r ];
                        };


                        scope.getNoGoArea = function(){
                            var olFeature = MapService.wktToOlFeature('POLYGON((9.419409 54.36294,  13.149009 54.36294, 13.149009 56.36316, 9.419409 56.36316, 9.419409 54.36294))');
                            serviceAvailableLayer.getSource().addFeature(olFeature);
                            serviceAvailableLayer.getSource().addFeature(map.getView().getExtent().toGeometry());
                            bboxBLTR = scope.clientBBOX();
                            scope.nogoarea = NoGoAreaService.getNoGoAreas(6, bboxBLTR[3],bboxBLTR[2],bboxBLTR[1],bboxBLTR[0],0, null);
                            console.log(scope.nogoarea);
                            // growl.info("Do a No go area request " + scope.nogoarea);
                        };

                    });
                }
            };
        }]);




