/**
 * Defines the main AIS vessel layer
 */

angular.module('maritimeweb.vessel')
    /**
     * The map-vessel-layer directive supports drawing a list of vessels on a map layer.
     * It will automatically load the vessels for the current map bounding box,
     * but only if the user is logged in.
     */
    .directive('mapVesselLayer', ['$rootScope', '$timeout', 'Auth', 'MapService', 'VesselService', 'growl', '$log',
        function ($rootScope, $timeout, Auth, MapService, VesselService, growl, $log) {
            return {
                restrict: 'E',
                replace: false,
                template: '<div id="vessel-info" class="ng-cloak"></div>' +
                '<div id="popup" class="ol-popup">' +
                '<a href id="popup-closer" class="ol-popup-closer"></a>' +
                '<h3 class="popover-title">{{vessel.name}}</h3>' +
                '<div class="popover-content">' +
                '<p uib-popover="MMSI number" popover-placement="left" popover-trigger="mouseenter">MMSI: {{vessel.mmsi}}</p>' +
                '<p uib-popover="Radio call sign" popover-placement="left" popover-trigger="mouseenter">Radio: {{vessel.callsign}} </p>' +
                '<p uib-popover="Type of vessel i.e. Tanker, Passenger, Fishing etc." popover-placement="left" popover-trigger="mouseenter">Type: {{vessel.type}}</p>' +
                '<p uib-popover="GPS position in latitude longitude" popover-placement="left" popover-trigger="mouseenter">Position: {{vessel.position}}</p>' +
                '<p uib-popover="Direction of the ship in degrees" popover-placement="left" popover-trigger="mouseenter">COG: {{vessel.angle}}°</p>' +
                //'<p><button uib-popover="Retrieve more detailed information about {{vessel.name}} i.e. past track, destination, estimated-time-of-arrivel, size, speed-over-ground, country of origin, IMO number and more" popover-trigger="mouseenter"' +
                //' popover-placement="bottom" type="button" class="btn btn-primary"' +
                //' ng-click="getMoreVesselDetails()">More details</button></p>' +
                '<p><a ng-href="#/vessel/{{vessel.mmsi}}">More details</a></p>' +
                '</div>' +
                '</div>',
                require: '^olMap',
                scope: {
                    name: '@',
                    alerts: '=?',
                    vessels: '=?'
                },
                link: function (scope, element, attrs, ctrl) {
                    var olScope = ctrl.getOpenlayersScope();
                    var vesselLayers;
                    var loadTimer;
                    scope.loggedIn = Auth.loggedIn;

                    olScope.getMap().then(function (map) {

                        /** get the current bounding box in Bottom left  Top right format. */
                        scope.clientBBOX = function () {
                            var bounds = map.getView().calculateExtent(map.getSize());
                            var extent = ol.proj.transformExtent(bounds, MapService.featureProjection(), MapService.dataProjection());
                            var l = Math.floor(extent[0] * 100) / 100;
                            var b = Math.floor(extent[1] * 100) / 100;
                            var r = Math.ceil(extent[2] * 100) / 100;
                            var t = Math.ceil(extent[3] * 100) / 100;
                            return b + "|" + l + "|" + t + "|" + r + "";
                        };


                        // Clean up when the layer is destroyed
                        scope.$on('$destroy', function () {
                            if (angular.isDefined(vesselLayers)) {
                                map.removeLayer(vesselLayers);
                            }
                            if (angular.isDefined(loadTimer)) {
                                $timeout.cancel(loadTimer);
                            }
                        });


                        /** TODO: Remove this method and create a method similar to colorHexForVessel:
                         * a simple function that given one color can darken or lighten it.
                         * Given two colors, the function mixes the two, and returns the blended color.
                         * This funtion is bluntly copy/pasted from http://stackoverflow.com/questions/5560248/programmatically-lighten-or-darken-a-hex-color-or-rgb-and-blend-colors
                         * by http://stackoverflow.com/users/693927/pimp-trizkit
                         * usage
                         * var color1 = "#FF343B";
                         * var color2 = "#343BFF";
                         * var color3 = "rgb(234,47,120)";
                         * var color4 = "rgb(120,99,248)";
                         * var shadedcolor1 = shadeBlend(0.75,color1);
                         * var shadedcolor3 = shadeBlend(-0.5,color3);
                         * var blendedcolor1 = shadeBlend(0.333,color1,color2);
                         * var blendedcolor34 = shadeBlend(-0.8,color3,color4); // Same as using 0.8
                         * @param p percentage of shade or highlight
                         * @param c0 first color
                         * @param c1 OPTIONAL second color, only for blending
                         * @returns A string with a color.
                         */
                        scope.shadeBlend = function (p, c0, c1) {
                            var n = p < 0 ? p * -1 : p, u = Math.round, w = parseInt;
                            if (c0.length > 7) {
                                var f = c0.split(","), t = (c1 ? c1 : p < 0 ? "rgb(0,0,0)" : "rgb(255,255,255)").split(","), R = w(f[0].slice(4)), G = w(f[1]), B = w(f[2]);
                                return "rgb(" + (u((w(t[0].slice(4)) - R) * n) + R) + "," + (u((w(t[1]) - G) * n) + G) + "," + (u((w(t[2]) - B) * n) + B) + ")"
                            } else {
                                var f = w(c0.slice(1), 16), t = w((c1 ? c1 : p < 0 ? "#000000" : "#FFFFFF").slice(1), 16), R1 = f >> 16, G1 = f >> 8 & 0x00FF, B1 = f & 0x0000FF;
                                return "#" + (0x1000000 + (u(((t >> 16) - R1) * n) + R1) * 0x10000 +
                                    (u(((t >> 8 & 0x00FF) - G1) * n) + G1) * 0x100 + (u(((t & 0x0000FF) - B1) * n) + B1)).toString(16).slice(1)
                            }
                        };

                        /**
                         * Given a vessels type number betweeen 0-7, return a color in RGB hex format.
                         * @param vo = a vessel
                         * @returns a color in hex format i.e. #0000ff, #737373, #40e0d0
                         *
                         */
                        scope.colorHexForVessel = function (vo) {
                            var colorName;
                            switch (vo.type) {
                                case "0" :
                                    colorName = "#0000ff";
                                    break; // blue
                                case "1" :
                                    colorName = "#737373";
                                    break; // grey
                                case "2" :
                                    colorName = "#00cc00";
                                    break; // green
                                case "3" :
                                    colorName = "#ffa500";
                                    break; // orange
                                case "4" :
                                    colorName = "#800080";
                                    break; // purple
                                case "5" :
                                    colorName = "#ff0000";
                                    break; // red
                                case "6" :
                                    colorName = "#40e0d0";
                                    break; // turquoise
                                case "7" :
                                    colorName = "#ffff00";
                                    break; // yellow
                                default :
                                    colorName = "#737373"; // grey
                            }
                            return colorName;
                        };

                        /** Create a simplified vessel feature, with only lat,lon,type. */
                        scope.createMinimalVesselFeature = function (vessel) {
                            var colorHex = scope.colorHexForVessel(vessel);
                            var shadedColor = scope.shadeBlend(-0.15, colorHex, undefined);

                            var markerStyle = new ol.style.Style({
                                image: new ol.style.Circle({
                                    radius: 3,
                                    stroke: new ol.style.Stroke({
                                        color: shadedColor,
                                        width: 1
                                    }),
                                    fill: new ol.style.Fill({
                                        color: colorHex // attribute colour
                                    })
                                })
                            });
                            var vesselPosition = new ol.geom.Point(ol.proj.transform([vessel.x, vessel.y], 'EPSG:4326', 'EPSG:900913'));
                            var markerVessel = new ol.Feature({
                                geometry: vesselPosition,
                                type: vessel.type
                            });
                            markerVessel.setStyle(markerStyle);
                            return markerVessel;
                        };

                        /** Create a vessel feature for any openlayers 3 map. */
                        scope.createVesselFeature = function (vessel) {
                            var image = VesselService.imageAndTypeTextForVessel(vessel);

                            var markerStyle = new ol.style.Style({
                                image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
                                    anchor: [0.5, 1.0],
                                    opacity: 0.85,
                                    id: vessel.id,
                                    rotation: vessel.radian,
                                    rotateWithView: true,
                                    src: 'img/' + image.name
                                }))
                            });

                            var vesselPosition = new ol.geom.Point(ol.proj.transform([vessel.x, vessel.y], 'EPSG:4326', 'EPSG:900913'));
                            var markerVessel = new ol.Feature({
                                name: vessel.name,
                                id: vessel.id,
                                type: image.type,
                                angle: vessel.angle,
                                radian: vessel.radian,  // (vessel.angle * (Math.PI / 180)),
                                callSign: vessel.callSign,
                                mmsi: vessel.mmsi,
                                latitude: vessel.y,
                                longitude: vessel.x,
                                geometry: vesselPosition
                            });
                            markerVessel.setStyle(markerStyle);
                            return markerVessel;
                        };

                        /** Refreshes the list of vessels from the server */
                        scope.refreshVessels = function () {

                            if (!scope.loggedIn) {
                                growl.info('Log in to see vessels');
                                return;
                            }
                            $rootScope.loadingData = true; // start spinner
                            growl.info('Fetching vessel data <i class="fa fa-cog fa-spin  fa-fw"></i><span class="sr-only">Loading...</span>', {ttl: 3000});

                            var zoomLvl = map.getView().getZoom();
                            scope.vessels.length = 0;
                            VesselService.getVesselsInArea(zoomLvl, scope.clientBBOX())
                                .success(function (vessels) {

                                    var features = [];
                                    for (var i = 0; i < vessels.length; i++) {
                                        var vessel = vessels[i];
                                        var vesselData = {
                                            name: vessel.name || "",
                                            type: vessel.type,
                                            x: vessel.x,
                                            y: vessel.y,
                                            angle: vessel.angle,
                                            radian: (vessel.angle - 90) * (Math.PI / 180),
                                            mmsi: vessel.mmsi || "",
                                            callSign: vessel.callSign || "",
                                            moored: vessel.moored || false,
                                            inBW: vessel.inAW || false
                                        };
                                        scope.vessels.push(vesselData);

                                        var vesselFeature;
                                        if (zoomLvl > 8) {
                                            vesselFeature = scope.createVesselFeature(vesselData);

                                        } else {
                                            vesselFeature = scope.createMinimalVesselFeature(vesselData);
                                        }
                                        features.push(vesselFeature);
                                    }

                                    vesselLayer.getSource().clear();
                                    vesselLayer.getSource().addFeatures(features);
                                    vesselLayer.setZIndex(10);
                                    $rootScope.loadingData = false; // stop spinner
                                    growl.success('<b>' + scope.vessels.length + '</b> vessels loaded', {ttl: 3000});

                                })
                                .error(function (reason) {
                                    $rootScope.loadingData = false; // stop spinner
                                    $log.error(reason);
                                    growl.error("Connection problems " + reason);
                                });

                        };
                        scope.getMoreVesselDetails = function () {
                            VesselService.showVesselInfoFromMMsi(scope.vessel.mmsi);

                        };

                        /** When the map extent changes, reload the Vessels's using a timer to batch up changes */
                        scope.mapChanged = function () {
                            if (MapService.isLayerVisible('vesselVectorLayer', vesselLayers)) {
                                if (loadTimer) {
                                    $timeout.cancel(loadTimer);
                                }
                                loadTimer = $timeout(scope.refreshVessels, 500);
                            }else{
                                scope.vessels.length = 0;
                            }
                        };

                        // Create vessel layer
                        var vessselLayerAttributions = [
                            new ol.Attribution({
                                html: '<div class="panel panel-info">' +
                                '<div class="panel-heading">Traffic information</div>' +
                                '<div class="panel-body">' +
                                '<span>' +
                                'Vessel AIS Traffic information <a href="http://www.helcom.fi/">Helcom</a> AIS Data' +
                                '</span>' +
                                '</div>'
                            }),
                            ol.source.OSM.ATTRIBUTION
                        ];

                        var vectorSource = new ol.source.Vector({
                            features: [], //to begin with, add an array empty array vessel features
                            attributions: vessselLayerAttributions
                        });

                        var vesselLayer = new ol.layer.Vector({
                            name: "vesselVectorLayer",
                            title: "Vessels - Helcom",
                            source: vectorSource,
                            visible: false
                        });

                        vesselLayers = new ol.layer.Group({
                            title: 'Vessels',
                            layers: [vesselLayer],
                            visible: true
                        });

                        map.addLayer(vesselLayers);
                        $rootScope.mapTrafficLayers = vesselLayers; // add group-layer to rootscope so it can be enabled/disabled

                        // update the map when a user pan-move ends.
                        map.on('moveend', scope.mapChanged);



                        // listens when visibility on map has been toggled.
                        vesselLayers.on('change:visible', scope.mapChanged);

                        /***************************/
                        /** Vessel Details        **/
                        /***************************/

                        var elm = document.getElementById('vessel-info');

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
                        var closer = document.getElementById('popup-closer');

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
                        closer.onclick = function () {
                            overlay.setPosition(undefined);
                            closer.blur();
                            return false;
                        };

                        map.addOverlay(overlay);

                        /**
                         * Add a click handler to the map to render the popup.
                         */
                        map.on('singleclick', function (evt) {

                            var zoomLvl = map.getView().getZoom();

                            if (zoomLvl > 8) {
                                var feature = map.forEachFeatureAtPixel(evt.pixel, function (feature, layer) {
                                    return feature;
                                }, null, function (layer) {
                                    return layer === vesselLayer;
                                });

                                if (feature) {
                                    var geometry = feature.getGeometry();
                                    var coordinate = evt.coordinate;
                                    scope.vessel = {};
                                    scope.vessel.name = feature.get('name');
                                    scope.vessel.mmsi = feature.get('mmsi');
                                    scope.vessel.type = feature.get('type');
                                    scope.vessel.angle = feature.get('angle');
                                    scope.vessel.callsign = feature.get('callSign');
                                    scope.vessel.position = ol.coordinate.toStringHDMS([feature.get('longitude'), feature.get('latitude')], 3);
                                    overlay.setPosition(coordinate);
                                } else {
                                    //$(elm).popover('destroy');
                                    // console.log("destroy");
                                }
                            } else { // close popups when zoomed below lvl 8 and clicks on map...
                                //$(elm).popover('destroy');
                             /*   if (scope.loggedIn) {
                                    growl.success('<b>Zoom</b> in for more detailed information');
                                    return;
                                }*/
                            }

                        });
                    });
                }
            };
        }]);