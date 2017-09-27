/**
 * Defines the main Vessel Traffic Service (VTS) map layer.
 * Please note that there are 2 services related to VTS: mapVtsAreaService and VesselTrafficServiceReportCtrl.
 * They are different from each by that the mapservice has a map layer, much like other mapping services,
 * and the VesselTrafficServiceReport is the form to write reports to VTS centres.
 *
 * The mapservice uses localstorage to store which VTS centres are intersected by the loaded route.
 * The information is then used by the route overview sidepanel and the VesselTrafficServiceReportCtrl, if available.
 *
 */
angular.module('maritimeweb.vts-map')

    .service('mapVtsAreaService', ['$http', '$log', 'MapService', '$window', 'growl',
        function ($http, $log, MapService, $window, growl) {


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

            this.testForRoute = function(){ //just test if there is a route in localstorage
                var tmpStr = $window.localStorage['route_oLpoints'];
                if(tmpStr && tmpStr.length>9){
                    return true
                }else{
                    return false;
                }
            };

            this.returnRouteAsWKT = function(){
                var tmpStr = $window.localStorage['route_oLpoints'];
                try {
                    var wpPosArrArr = JSON.parse(tmpStr);
                    if (wpPosArrArr.length > 0) {
                        var wpLonLatArr = []; //needed to calculate intersect of route with VTS area
                        var routeWKT = "LINESTRING("; //begin LINESTRING
                        for (var i = 0; i != wpPosArrArr.length; i++) {
                            var lonlat = ol.proj.transform([wpPosArrArr[i][0], wpPosArrArr[i][1]], 'EPSG:3857', 'EPSG:4326');
                            routeWKT += lonlat[0] + " " + lonlat[1] + ",";
                            wpLonLatArr.push(lonlat);
                        }
                        routeWKT = routeWKT.slice(0, -1) + ")"; //remove trailing comma and terminate LINESTRING
                    }
                    return routeWKT;
                }catch(doNothing){
                    return false
                }
            };

            //Populate the areas array from service endpoint
            this.getVtsAreas = function () {
                return $http.get('/rest/vtsinterface');
            };
        }])

    .directive('mapVtsAreaLayer', ['MapService', '$window','growl',
        function (MapService, $window, growl) {
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
                                color: 'rgba(250, 0, 0, 0.1)',
                                width: 1
                            }),
                            fill: new ol.style.Fill({
                                color: 'rgba(238, 153, 0,0.1)'
                            })
                        });


                        // Construct the boundary layers
                        vtsareaLayer = new ol.layer.Vector({
                            title: 'VTS areas',
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


                        var vtsAreas = new ol.layer.Group({
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

                        scope.maxZoom = 5;
                        scope.highlightColourStroke ="rgba(250, 0, 0, 0.8)";
                        scope.highlightColourFill ="rgba(238, 153, 0,0.7)";
                        scope.normalColourStroke ="rgba(250, 0, 0, 0.5)";
                        scope.normalColourFill ="rgba(238, 153, 0,0.4)";

                        function styleFunctionNormal(areaName) {
                            var labelSize = 18 - (map.getView().getZoom() *.2)
                            return [
                                new ol.style.Style({
                                    text: new ol.style.Text({
                                        font: labelSize + 'px Calibri,sans-serif',
                                        fill: new ol.style.Fill({ color: '#333' }),
                                        stroke: new ol.style.Stroke({
                                            color: '#fff', width: 2
                                        }),
                                        // get the text from the feature - `this` is ol.Feature
                                        text: (map.getView().getZoom()>scope.maxZoom) ? areaName : ""  //areafeature.get('name')
                                    }),
                                    stroke: new ol.style.Stroke({
                                        color: scope.normalColourStroke,
                                        width: 1
                                    }),
                                    fill: new ol.style.Fill({
                                        color: scope.normalColourFill
                                    })

                                })
                            ];
                        }

                        function styleFunctionHighlight(areaName) {
                            var labelSize = 18 - (map.getView().getZoom() *.2)
                            return [
                                new ol.style.Style({
                                    text: new ol.style.Text({
                                        font: labelSize + 'px Calibri,sans-serif',
                                        fill: new ol.style.Fill({ color: '#000' }),
                                        stroke: new ol.style.Stroke({
                                            color: '#fff', width: 2
                                        }),
                                        // get the text from the feature - `this` is ol.Feature
                                        text: (map.getView().getZoom()>scope.maxZoom) ? areaName : ""  //areafeature.get('name')
                                    }),
                                    stroke: new ol.style.Stroke({
                                        color: scope.highlightColourStroke,
                                        width: 1
                                    }),
                                    fill: new ol.style.Fill({
                                        color: scope.highlightColourFill
                                    })

                                })
                            ];
                        }


                        //uses areas from localstorage - "onroute"(true/false) draws only if route intersects with area
                        scope.updateVtsAreas = function (onroute) {
                            var vts_areas_str = localStorage.getItem('vts_areas');
                            var vts_areas;
                            if(vts_areas_str && vts_areas_str.length>0) {
                                vts_areas = JSON.parse(vts_areas_str);
                                if(vts_areas.length > 0){
                                    vtsareaLayer.getSource().clear(); //cleanup first
                                    /** iterate through the object to draw all areas on map **/
                                    for(var i=0;i!=vts_areas.length;i++){
                                        var areaWKT = vts_areas[i].areaWKT; //console this for easy debugging
                                        if (!areaWKT || areaWKT == "" || areaWKT.length < 9) {
                                            growl.error(vts_areas[i].shortname + " does not have an area assigned.");
                                        }else{
                                            var format = new ol.format.WKT();
                                            var areafeature = format.readFeature(areaWKT, {
                                                dataProjection: 'EPSG:4326',
                                                featureProjection: 'EPSG:3857'
                                            });
                                            // //place something at center of mass of polygon - if needed (possibly a menu)
                                            // var polygon = turf.polygon(format.readFeature(areaWKT).getGeometry().getCoordinates());
                                            // var center = turf.centerOfMass(polygon);
                                            areafeature.set("vtsAreaID",vts_areas[i].id);
                                            areafeature.set("name",vts_areas[i].shortname);
                                            //styling
                                            areafeature.setStyle(styleFunctionNormal(vts_areas[i].shortname, vts_areas[i].id));
                                            //add to layer
                                            vtsareaLayer.getSource().addFeature(areafeature);
                                        }
                                    }
                                }
                            }
                        };

                        //on load, show route as well
                        scope.updateVtsRoute = function (data) { //draws route on map
console.log("calc intersects with route here");
                            // var format = new ol.format.WKT();
                            // var routefeature = format.readFeature(routeWKT, {
                            //     dataProjection: 'EPSG:4326',
                            //     featureProjection: 'EPSG:3857'
                            // });
                            // routefeature.setStyle(
                            //     new ol.style.Style({
                            //         stroke: new ol.style.Stroke({
                            //             color: '#bb0000',
                            //             width: 3
                            //         })
                            //     })
                            // );
                            // vectorSource.addFeature(routefeature);

                        };


                        /** Returns the ID of a VTS area when click on it **/
                        scope.getIdForPixel = function (pixel) {
                            var retId = null,
                                retFeature = null;
                            map.forEachFeatureAtPixel(pixel, function(feature, layer) {
                                var id = feature.get("vtsAreaID");
                                if ((layer == vtsareaLayer) && id) {
                                    retId = id;
                                    retFeature = feature;
                                }
                            });

                            return {id:retId, feature:retFeature};
                        };


                        /***************************/
                        /**        Listeners      **/
                        /***************************/

                        var zoomLevel = map.getView().getZoom();
                        scope.detectZoomEnd = function(){
                            if(zoomLevel != map.getView().getZoom()){
                                zoomLevel = map.getView().getZoom(); //get ready for next zoom
                                scope.updateVtsAreas(); //deactivates text when far out (low zoom)
                            }
                        };

                        map.on('moveend', scope.detectZoomEnd);

                        //mouse click or ontap
                        map.on('click', function(evt) { //display VTS area sidebar tab and VTS area information when click
                            var ret = scope.getIdForPixel(map.getEventPixel(evt.originalEvent));
                            if (ret.id != null) {
                                // $rootScope.activeTabIndex = 4;
                                // console.log("clicked:",ret.id);
                            }
                        });

                        //mouse over
                        scope.lastFeatureMouseOver = {}; //on mouseout, return feature back to normal, need to rememeber the last feature so it can be put back.
                        map.on('pointermove', function (evt) { //highlight VTS area when cursor over - indicates user interaction option
                            var ret = scope.getIdForPixel(map.getEventPixel(evt.originalEvent));
                            if (ret.id != null) {

                                if(Object.keys(scope.lastFeatureMouseOver).length < 1){
                                    scope.lastFeatureMouseOver = ret.feature;
                                    (ret.feature).setStyle(styleFunctionHighlight(scope.lastFeatureMouseOver.get("name")));
                                }
                                if(scope.lastFeatureMouseOver != ret.feature){
                                    (scope.lastFeatureMouseOver).setStyle(styleFunctionNormal(scope.lastFeatureMouseOver.get("name"))); //return previous to normal
                                    scope.lastFeatureMouseOver = ret.feature;
                                    (ret.feature).setStyle(styleFunctionHighlight(scope.lastFeatureMouseOver.get("name")));
                                }
                            }else { //mouse over map, may be mouseout of feature
                                if (Object.keys(scope.lastFeatureMouseOver).length > 0) {
                                    (scope.lastFeatureMouseOver).setStyle(styleFunctionNormal(scope.lastFeatureMouseOver.get("name"))); //return previous to normal
                                    scope.lastFeatureMouseOver = {}; //clear
                                }
                            }
                        });

                        //See changes in VTS areas array, then update map
                        scope.$watch('vtsAreas', function (data) {
                            $window.localStorage.setItem('vts_areas', JSON.stringify(data));
                            scope.updateVtsAreas();
                        }, true);

                        scope.$watch('vtsRoute', function (data) { //if route changes, trigger reload of route
                            scope.updateVtsRoute(data);
                        }, true);

                        //Toggle visibility of map layer
                        scope.$watch('vtsVisible', function (data) {
                            vtsAreas.setVisible(data);
                        }, true);

                    });
                }
            };
        }]);


