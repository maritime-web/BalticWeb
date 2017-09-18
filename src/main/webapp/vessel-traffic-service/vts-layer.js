/**
 * Defines the main Vessel Traffic Service (VTS) map layer.
 * Please note that there are 2 services related to VTS: mapVtsAreaService and VesselTrafficServiceReportCtrl.
 * They are different from each by that the mapservice has a map layer, much like other mapping services,
 * and the VesselTrafficServiceReport is the form to write reports to VTS centres.
 *
 * The mapservice does use localstorage to store which VTS centres are intersected by the loaded route.
 * The information is then used by the route overview sidepanel and the VesselTrafficServiceReportCtrl, if available.
 *
 */
angular.module('maritimeweb.vts-map')

    .service('mapVtsAreaService', ['$http', '$log', 'MapService', 'VtsHelperService', '$window', 'growl',
        function ($http, $log, MapService, VtsHelperService, $window, growl) {


            this.toggleVtsAreasLayerEnabled = function () {
                this.vts_map_show = $window.localStorage['vts_map_show'];
                (this.vts_map_show == true || this.vts_map_show == "true") ? this.vts_map_show = true : this.vts_map_show = false; //is string, need bool

                var msg = "", state = false;
                if (!this.vts_map_show) {
                    state = true;
                    msg = "Activating VTS areas.";
                    $window.localStorage.setItem('vts_map_show', state);
                    // this.getVtsAreasAndDisplayOnMap();
                } else {
                    state = false;
                    msg = "Deactivating VTS areas.";
                    $window.localStorage.setItem('vts_map_show', state);
                }
                growl.info(msg);
                return state;
            };

            //Populate the areas array from service endpoint
            var VTSData = []; //local array
            this.getVtsAreas = function () {
                return $http.get('/rest/vtsinterface');
            };
        }])

    .directive('mapVtsAreaLayer', ['$rootScope', '$timeout', 'MapService', '$window',
        function ($rootScope, $timeout, MapService, $window) {
            return {
                restrict: 'E',
                require: '^olMap',
                template: "",
                scope: {
                    vtsAreas: '=?',
                    vtsVisible: '=?'
                },
                link: function (scope, element, attrs, ctrl) {

                    /***************************/
                    /** VTS Layer             **/
                    /***************************/

                    var olScope = ctrl.getOpenlayersScope();
                    var vtsareaLayer;
                    var maxZoom = scope.maxZoom ? parseInt(scope.maxZoom) : 12;

                    olScope.getMap().then(function (map) {

                        // Clean up when the layer is destroyed
                        scope.$on('$destroy', function () {
                            if (angular.isDefined(vtsareaLayer)) {
                                map.removeLayer(vtsareaLayer);
                            }
                        });

                        var boundaryStyle = new ol.style.Style({
                            stroke: new ol.style.Stroke({
                                color: 'rgba(250, 0, 0, 0.5)',
                                width: 1
                            }),
                            fill: new ol.style.Fill({
                                color: 'rgba(238, 153, 0,0.4)'
                            })
                        });


                        // Construct the boundary layers
                        vtsareaLayer = new ol.layer.Vector({
                            title: 'Navigational Warnings',
                            zIndex: 11,
                            source: new ol.source.Vector({
                                features: new ol.Collection(),
                                wrapX: false
                            }),
                            style: [boundaryStyle],
                            name: 'vts-vector-layer'

                        });
                        vtsareaLayer.setZIndex(11);
                        vtsareaLayer.setVisible(true);

                        var areaWKT = "";
                        if (!areaWKT || areaWKT == "" || areaWKT.length < 9) {
                            areaWKT = "POLYGON((12 56,12 55,13 55,13 56,12 56))"; //just a default area
                        }
                        var format = new ol.format.WKT();

                        var areafeature = format.readFeature(areaWKT, {
                            dataProjection: 'EPSG:4326',
                            featureProjection: 'EPSG:3857'
                        });

                        vtsareaLayer.getSource().addFeature(areafeature);


                        vtsAreas = new ol.layer.Group({
                            title: scope.name || 'VTS',
                            zIndex: 11,
                            layers: [vtsareaLayer]
                        });
                        vtsAreas.setZIndex(11);
                        vtsAreas.setVisible(true);

                        map.addLayer(vtsAreas);


                        /***************************/
                        /**        Functions      **/
                        /***************************/


                        scope.updateVtsAreas = function () {
                            var areaWKT = "";
                            vtsareaLayer.getSource().clear();

                            if (!areaWKT || areaWKT == "" || areaWKT.length < 9) {
                                areaWKT = "POLYGON((12 56,12 55,13 55,13 56,12 56))"; //just a default area
                            }
                            var format = new ol.format.WKT();
                            var areafeature = format.readFeature(areaWKT, {
                                dataProjection: 'EPSG:4326',
                                featureProjection: 'EPSG:3857'
                            });
                            console.log("areaWKT:", areafeature);

                            vtsareaLayer.getSource().addFeature(areafeature);
                        };


                        /***************************/
                        /**        Listeners      **/
                        /***************************/

                        map.on('moveend', scope.updateVtsAreas);

                        scope.$watch('vtsAreas', function (data) {
                            console.log("bob3:",data);
                        }, true);

                        scope.$watch('vtsVisible', function (data) {
                            console.log("box 4: vts-visible:",data);
                        }, true);


                        // if (scope.fitExtent == 'true') {
                        //     var fitExtent = false;
                        //     var extent = ol.extent.createEmpty();
                        //
                        //     if (fitExtent) {
                        //         map.getView().fit(extent, map.getSize(), {
                        //             padding: [20, 20, 20, 20],
                        //             maxZoom: maxZoom
                        //         });
                        //     }
                        // }

                    });
                }
            };
        }]);


