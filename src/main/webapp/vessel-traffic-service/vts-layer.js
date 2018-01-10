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

    .service('mapVtsAreaService', ['$http', '$log', 'MapService', '$window', 'growl', 'Auth',
        function ($http, $log, MapService, $window, growl, Auth) {

            this.toggleVtsAreasLayerEnabled = function () {
                this.vts_map_show = $window.localStorage['vts_map_show'];
                (this.vts_map_show == true || this.vts_map_show == "true") ? this.vts_map_show = true : this.vts_map_show = false; //is string, need bool

                var msg = "", state = false;
                if (!this.vts_map_show) {
                    state = true;
                    msg = "Activating VTS areas.";
                    $window.localStorage.setItem('vts_map_show', state);
                } else {
                    state = false;
                    msg = "Deactivating VTS areas.";
                    $window.localStorage.setItem('vts_map_show', state);
                }
                growl.info(msg);
                return state;
            };

            this.toggleVtsAreasOnlyIntersectingRouteEnabled = function(){
                this.vts_onroute_only = $window.localStorage['vts_onroute_only'];
                (this.vts_onroute_only == true || this.vts_onroute_only == "true") ? this.vts_onroute_only = true : this.vts_onroute_only = false; //is string, need bool

                var msg = "", state = false;
                if (!this.vts_onroute_only) {
                    state = true;
                    msg = "Displaying VTS areas intersecting with current route";
                    $window.localStorage.setItem('vts_onroute_only', state);
                } else {
                    state = false;
                    msg = "Displaying all registered VTS areas";
                    $window.localStorage.setItem('vts_onroute_only', state);
                }
                growl.info(msg);
                return state;
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

    .directive('mapVtsAreaLayer', ['MapService', '$window','growl', '$uibModal', 'VtsHelperService',
        function (MapService, $window, growl, $uibModal, VtsHelperService) {
            return {
                restrict: 'E',
                require: '^olMap',
                template: '<div id="vts-info" class="ng-cloak"></div>' +
                '<div id="popup" class="ol-popup">' +
                '<a href id="vts-popup-closer" class="ol-popup-closer"></a>' +
                '<h3 class="popover-title">{{vtsPopupVtsShortname}} - {{vtsPopupVtsCountry}}</h3>' +
                '<div class="popover-content">' +
                '<p ng-show="vtsPopupVtsCallsign.length>0">Callsign: {{vtsPopupVtsCallsign}}</p>' +
                '<p ng-show="vtsPopupVtsVhf1.length>0">VHF: {{vtsPopupVtsVhf1}}</p>' +
                '<p ng-show="vtsPopupVtsVhf2.length>0">VHF2: {{vtsPopupVtsVhf2}}</p>' +
                '<p ng-show="vtsPopupVtsTel1.length>0">Tel: {{vtsPopupVtsTel1}}</p>' +
                '<p ng-show="vtsPopupVtsEmail.length>0">Email: {{vtsPopupVtsEmail}}</p>' +
                '<p><span><button class="btn btn-primary"  type="button" ng-click="activateVTSForm()">Send VTS report now</button></span></p>' +
                '</div>' +
                '</div>',
                scope: {
                    vtsAreas: '=?', //VTS areas object from service endpoint
                    vtsRoute: '=?', //route ETAs
                    vtsVisible: '=?', //toggles layer visble or not
                    vtsOnrouteOnly: '=?', //filter for intersecting
                },
                link: function (scope, element, attrs, ctrl) {


                    /* popup information vars*/
                    scope.clearVtsPopup = function(){
                        scope.vtsPopupVtsShortname = "";
                        scope.vtsPopupVtsCallsign = "";
                        scope.vtsPopupVtsVhf1 = "";
                        scope.vtsPopupVtsVhf2 = "";
                        scope.vtsPopupVtsTel1 = "";
                        scope.vtsPopupVtsTel2 = "";
                        scope.vtsPopupVtsEmail = "";
                        scope.vtsPopupVtsIconImage = "";
                        scope.vtsPopupVtsCountry = "";
                        scope.vtsPopupVtsVhfReserve = "";
                    };
                    scope.clearVtsPopup(); //init
                    scope.currentlySelectedVtsArea = 0;

                    /***************************/
                    /** VTS Layer             **/
                    /***************************/

                    var olScope = ctrl.getOpenlayersScope();
                    scope.onrouteOnly = false; //only draw VTS areas if true

                    olScope.getMap().then(function (map) {
                        var vtsareaLayer;

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
                        scope.highlightColourFill ="rgba(238, 153, 0,0.4)";
                        scope.normalColourStroke ="rgba(250, 0, 0, 0.5)";
                        scope.normalColourFill ="rgba(238, 153, 0,0.2)";
                        scope.highlightColourFillTmp = scope.highlightColourFill; //needed for zoomlevel transparency change

                        function styleFunctionNormal(areaName) {
                            var labelSize = 18 - (map.getView().getZoom() *.2);
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
                            var labelSize = 18 - (map.getView().getZoom() *.2);
                            (zoomLevel > 5 ) ? scope.highlightColourFill = "rgba(238, 153, 0,"+(.4 / (zoomLevel/3))+")": scope.highlightColourFill = scope.highlightColourFillTmp;
                            return [
                                new ol.style.Style({
                                    text: new ol.style.Text({
                                        font: labelSize + 'px Calibri,sans-serif',
                                        fill: new ol.style.Fill({ color: '#000' }),
                                        stroke: new ol.style.Stroke({
                                            color: '#fff', width: 3
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


                        //uses areas from localstorage - "onrouteonly"(true/false) draws only if route intersects with area
                        scope.updateVtsAreas = function (onrouteonly) {
                            //draws VTS areas on map, skips areas not intersecting route if onrouteonly == true
                            var vts_areas_str = localStorage.getItem('vts_areas');
                            var vts_areas;
                            if(vts_areas_str && vts_areas_str.length>0) {
                                vts_areas = JSON.parse(vts_areas_str);
                                if(vts_areas.length > 0){
                                    vtsareaLayer.getSource().clear(); //cleanup first
                                    /** iterate through the object to draw all areas on map **/
                                    var intersectingAreasArr = [];
                                    var centerAreasArr = [];
                                    for(var i=0;i!=vts_areas.length;i++){
                                        var areaWKT = vts_areas[i].areaWKT; //console this for easy debugging
                                        if (!areaWKT || areaWKT == "" || areaWKT.length < 9) {
                                            growl.error(vts_areas[i].shortname + " does not have an area assigned");
                                        }else{

                                            if(scope.isAreaOnRoute(areaWKT)) intersectingAreasArr.push(vts_areas[i].id);
                                            var drawArea = false;
                                            if(onrouteonly && scope.isAreaOnRoute(areaWKT)){
                                                drawArea = true;
                                            }else if(!onrouteonly){
                                                drawArea = true;
                                            }
                                            if(drawArea){
                                                var format = new ol.format.WKT();
                                                var areafeature = format.readFeature(areaWKT, {
                                                    dataProjection: 'EPSG:4326',
                                                    featureProjection: 'EPSG:3857'
                                                });
                                                //saves center of area in localstorage
                                                var polygon = turf.polygon(format.readFeature(areaWKT).getGeometry().getCoordinates());
                                                var center = turf.center(polygon);
                                                centerAreasArr.push({areaId:vts_areas[i].id,areaCenter:center.geometry.coordinates})

                                                //generate the areas for map layer
                                                areafeature.set("vtsAreaID",vts_areas[i].id);
                                                areafeature.set("name",vts_areas[i].shortname);
                                                //styling
                                                areafeature.setStyle(styleFunctionNormal(vts_areas[i].shortname, vts_areas[i].id));
                                                //add to layer
                                                vtsareaLayer.getSource().addFeature(areafeature);
                                            }
                                        }
                                    }
                                    //app-ctrl watches changes to this storage itam, populates sidemenu list accordingly
                                    $window.localStorage.setItem('vts_intersectingareas', JSON.stringify(intersectingAreasArr));
                                    $window.localStorage.setItem('vts_center_of_areas', JSON.stringify(centerAreasArr));
                                }
                            }
                        };

                        /** returns true if an area is intersected by the route in localstorage (vts_areas & route_oLpoints) **/
                        scope.isAreaOnRoute = function (areaWKT) {
                            var wpPosArrArr = JSON.parse($window.localStorage.getItem('route_oLpoints'));
                            if(wpPosArrArr && wpPosArrArr.length>2) {
                                var wpLonLatArr = []; //needed to calculate intersect of route with VTS area

                                for (var i = 0; i != wpPosArrArr.length; i++) {
                                    var lonlat = ol.proj.transform([wpPosArrArr[i][0], wpPosArrArr[i][1]], 'EPSG:3857', 'EPSG:4326');
                                    wpLonLatArr.push(lonlat);
                                }

                                //Find ETA of intersect at VTS area line
                                var routeline = turf.lineString(wpLonLatArr);

                                //make WKT to poly
                                var tmpWKT = areaWKT.replace("POLYGON((", '').replace('))', '');
                                tmpWKT = tmpWKT.replace(/,([\s])+/g, ',');
                                var tmpPoly = tmpWKT.split(',');
                                var areaAsPoly = [], tmpAreaAsPoly = [];
                                for (var i = 0; i != tmpPoly.length; i++) {
                                    var tmpPos = tmpPoly[i].split(" ");
                                    tmpAreaAsPoly.push([parseFloat(tmpPos[0]), parseFloat(tmpPos[1])]);
                                }
                                if (tmpAreaAsPoly[0] != tmpAreaAsPoly[tmpAreaAsPoly.length]) tmpAreaAsPoly.push(tmpAreaAsPoly[0]); //last pos must be same as first pos

                                areaAsPoly.push(tmpAreaAsPoly); //inception array
                                var vtsarea = turf.polygon(areaAsPoly);
                                var intersection = turf.intersect(vtsarea, routeline);

                                return (intersection) ? true : false;
                            }
                            return false; //no route available
                        };


                        /** Returns the ID of a VTS area when click on it **/
                        scope.getIdForPixel = function (pixel) {
                            var retId = null,
                                retFeature = null, mmsi = null;
                            map.forEachFeatureAtPixel(pixel, function(feature, layer) {
                                var id = feature.get("vtsAreaID");
                                var mmsi = feature.get("mmsi");
                                if ((layer == vtsareaLayer) && id) {
                                    retId = id;
                                    retFeature = feature;

                                }
                            });

                            return {id:retId, feature:retFeature, mmsi:mmsi};
                        };

                        /***************************/
                        /** VTS Center Details    **/
                        /***************************/

                        var elm = document.getElementById('vts-info');

                        var popup = new ol.Overlay({
                            element: elm,
                            positioning: 'bottom-center',
                            stopEvent: false
                        });
                        map.addOverlay(popup);
                        /**
                         * Elements that make up the popup.
                         */
                        var container = document.getElementById('popup');
                        var content = document.getElementById('popup-content');
                        var closer = document.getElementById('vts-popup-closer');

                        /**
                         * Create an overlay to anchor the popup to the map.
                         */
                        var overlay = new ol.Overlay(/** @type {olx.OverlayOptions} */ ({
                            element: container,
                            autoPan: true,
                            autoPanAnimation: {
                                duration: 250
                            }
                        }));

                        /**
                         * Add a click handler to hide the popup.
                         * @return {boolean} Don't follow the href.
                         */
                        closer.onclick = function () { //clear and close the popup
                            scope.vtsPopupVtsShortname = "";
                            scope.vtsPopupVtsCallsign = "";
                            scope.vtsPopupVtsVhf1 = "";
                            scope.vtsPopupVtsVhf2 = "";
                            scope.vtsPopupVtsTel1 = "";
                            scope.vtsPopupVtsTel2 = "";
                            scope.vtsPopupVtsEmail = "";
                            scope.vtsPopupVtsIconImage = "";
                            scope.vtsPopupVtsCountry = "";
                            scope.vtsPopupVtsVhfReserve = "";
                            overlay.setPosition(undefined);
                            closer.blur();
                            return false;
                        };

                        map.addOverlay(overlay);

                        scope.activateVTSForm = function (size) {
                            localStorage.setItem('vts_current_id', scope.currentlySelectedVtsArea);

                            if(!size) size='lg';
                            growl.info('Activating Vessel Traffic Control');
                            $uibModal.open({
                                animation: 'true',
                                templateUrl: 'vessel-traffic-service/vessel-traffic-service-form.html',
                                controller: 'VesselTrafficServiceReportCtrl',
                                size: size
                            });
                            scope.clearVtsPopup();
                            overlay.setPosition(undefined);
                        };


                        /***************************/
                        /**        Listeners      **/
                        /***************************/

                        var zoomLevel = map.getView().getZoom();
                        scope.detectZoomEnd = function(){
                            if(zoomLevel != map.getView().getZoom()){
                                zoomLevel = map.getView().getZoom(); //get ready for next zoom
                                scope.updateVtsAreas(scope.onrouteOnly); //deactivates text when far out (low zoom)
                            }
                        };

                        map.on('moveend', scope.detectZoomEnd);

                        //mouse click or ontap
                        map.on('click', function(evt) { //display VTS area sidebar tab and VTS area information when click
                            if(map.getView().getZoom() < 14) {
                                scope.clearVtsPopup();
                                var ret = scope.getIdForPixel(map.getEventPixel(evt.originalEvent));
                                if (ret.id != null) {
                                    var vts_areas_str = localStorage.getItem('vts_areas');
                                    var vts_areas;
                                    if (vts_areas_str && vts_areas_str.length > 0) {
                                        vts_areas = JSON.parse(vts_areas_str);
                                        if (vts_areas.length > 0) {
                                            var vtsNum = 0;
                                            for (var i = 0; i != vts_areas.length; i++) { //find in array which areas ID has ret.id
                                                if (parseInt(vts_areas[i].id) == parseInt(ret.id)) {
                                                    vtsNum = i;
                                                    break;
                                                }
                                            }
                                            scope.currentlySelectedVtsArea = vts_areas[vtsNum].id;
                                            VtsHelperService.showVtsCenterSelect = false;

                                            //Fill in the VTS info into vars needed by popup
                                            scope.vtsPopupVtsShortname = vts_areas[vtsNum].shortname;
                                            scope.vtsPopupVtsCountry = vts_areas[vtsNum].country;
                                            scope.vtsPopupVtsCallsign = vts_areas[vtsNum].callsign;
                                            scope.vtsPopupVtsVhf1 = vts_areas[vtsNum].vhfchannel1;
                                            scope.vtsPopupVtsVhf2 = vts_areas[vtsNum].vhfchannel2;
                                            scope.vtsPopupVtsVhfReserve = vts_areas[vtsNum].vhfreservechannel1;
                                            scope.vtsPopupVtsTel1 = vts_areas[vtsNum].telephone;
                                            scope.vtsPopupVtsTel2 = vts_areas[vtsNum].telephone2;
                                            scope.vtsPopupVtsEmail = vts_areas[vtsNum].email;
                                            scope.vtsPopupVtsIconImage = vts_areas[vtsNum].iconImage;
                                            scope.$apply();

                                            function updateTimeout() { //update bug workaround
                                                var coordinate = evt.coordinate;
                                                overlay.setPosition(coordinate);
                                            }

                                            setTimeout(function () {
                                                updateTimeout();
                                            }, 10);
                                        }
                                    }
                                } else {
                                    scope.clearVtsPopup();
                                    overlay.setPosition(undefined);
                                    localStorage.setItem('vts_current_id', "");
                                }
                            }//respect zoomlevel
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
                            scope.updateVtsAreas(false);
                        }, true);

                        scope.$watch('vtsRoute', function (data) { //if route changes, trigger reload of route
                            growl.info("Route has been updated"); //Route points are loaded into localstorage by RTZ handler
                            scope.updateVtsAreas(false);
                        }, true);

                        //Toggle visibility of map layer
                        scope.$watch('vtsVisible', function (data) {
                            vtsAreas.setVisible(data); //addresses the map layer group - true/false
                        }, true);

                        //user toggles filter for displaying VTS areas on route only
                        scope.$watch('vtsOnrouteOnly', function (data) {
                            if(data===true){
                                growl.info("Displaying only VTS areas on route");
                            }else{
                                growl.info("Displaying all VTS areas available");
                            }
                            scope.onrouteOnly = data; //so zooming can figure out what to display
                            scope.updateVtsAreas(data); //  data:true/false
                            if(data) { //only center on areas when specifying
                                try { //displays nicely on map
                                    var extent = vtsareaLayer.getSource().getExtent();
                                    map.getView().fit(extent, map.getSize());
                                    map.getView().setZoom(zoomLevel);
                                } catch (cantExtent) {
                                }
                            }
                        }, true);


                        //user clicks on any of the VTS buttons on the list - run fitextent on it
                        scope.skipAtLoadCounter = 0; //skips this at load
                        scope.$watch(function () { return window.localStorage['vts_zoomto_area_id']; },function(newVal,oldVal){
                            if(newVal && newVal != "" && scope.skipAtLoadCounter > 0){
                                var vts_center_of_areas;
                                var tmp_vts_center_of_areas = localStorage.getItem('vts_center_of_areas');
                                if(tmp_vts_center_of_areas && tmp_vts_center_of_areas != ""){
                                    vts_center_of_areas = JSON.parse(tmp_vts_center_of_areas);
                                }
                                for(var p=0;p!=vts_center_of_areas.length;p++){ //finds the correct point on map and animates to it
                                    if(parseInt(vts_center_of_areas[p].areaId) == parseInt(newVal)){
                                        var setZoomLvl = 7;
                                        if(map.getView().getZoom()<7)setZoomLvl = 7; //zoom out a bit if too close
                                        if(map.getView().getZoom()>13)setZoomLvl = 7; //zoom out a bit if too far

                                        //move the view with zoom change
                                        map.getView().animate({
                                            center: ol.proj.transform(vts_center_of_areas[p].areaCenter, 'EPSG:4326', 'EPSG:3857'),//vts_center_of_areas[p].areaCenter,
                                            zoom: setZoomLvl,
                                            duration: 1000
                                        });

                                        //cleanup
                                        localStorage.setItem('vts_zoomto_area_id', ""); //clear this so user can re-click on the button
                                        scope.clearVtsPopup(); //remove popup if user had clicked it
                                        overlay.setPosition(undefined);
                                        localStorage.setItem('vts_current_id', "");
                                        break;
                                    }
                                }
                            }
                            scope.skipAtLoadCounter++;
                        });


                        // scope.$watch('vts', function (data) {
                        //     if(data===true){
                        //         growl.info("Displaying only VTS areas on route");
                        //     }else{
                        //         growl.info("Displaying all VTS areas available");
                        //     }
                        //     scope.onrouteOnly = data; //so zooming can figure out what to display
                        //     scope.updateVtsAreas(data); //  data:true/false
                        //     if(data) { //only center on areas when specifying
                        //         try { //displays nicely on map
                        //             var extent = vtsareaLayer.getSource().getExtent();
                        //             map.getView().fit(extent, map.getSize());
                        //             map.getView().setZoom(zoomLevel);
                        //         } catch (cantExtent) {
                        //         }
                        //     }
                        // }, true);




                    });
                }
            };
        }]);


