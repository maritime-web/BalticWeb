/**
 * Defines the main AIS vessel layer
 */

angular.module('maritimeweb.vessel')

    /** Service for accessing AIS vessel data **/
    .service('VesselService', ['$http', 'growl', '$uibModal',
        function($http, growl, $uibModal) {

            /** Returns the AIS vessels within the bbox */
            this.getVesselsInArea = function (zoomLvl, bbox) {
                var url = '';
                if (zoomLvl > 8){ // below  zoom level 8 a more detailed and data rich overview is created.
                    url += "rest/vessel/listarea?area="+ bbox;
                } else {
                    url += "rest/vessel/overview?area="+ bbox;
                }
                return $http.get(url);
            };

            /** Returns the details for the given MMSI **/
            this.details = function (vessel) {
                console.log("getting details in VesselService");
                return $http.get('/rest/vessel/details?mmsi=' + encodeURIComponent(vessel.mmsi));
            };

            /** Open the message details dialog **/
            this.showVesselInfoFromMMsi = function (mmsi) {

                var message = this.details(mmsi);
                return $uibModal.open({
                    controller: "VesselDialogCtrl",
                    templateUrl: "/prototype/vessel/vessel-details-dialog.html",
                    size: 'lg',
                    resolve: {
                        message: function () {
                            return message;
                        }
                    }
                });
            };


            /** Open the message details dialog **/
            this.showVesselInfo = function (vessel) {

                var message = this.details(vessel.mmsi);
                return $uibModal.open({
                    controller: "VesselDialogCtrl",
                    templateUrl: "/prototype/vessel/vessel-details-dialog.html",
                    size: 'lg',
                    resolve: {
                        message: function () {
                            return message;
                        }
                    }
                });
            };

            /** Saves the vessel details **/
            this.saveDetails = function (details) {
                return $http.post("/rest/vessel/save-details", details);
            };

            /** Returns the historical tracks for the given MMSI **/
            this.historicalTrack = function (mmsi) {
                return $http.get('/rest/vessel/historical-track?mmsi=' + encodeURIComponent(mmsi));
            };


            /** Returns the image and type text for the given vessel **/
            this.imageAndTypeTextForVessel = function (vo) {
                var colorName;
                var vesselType;
                switch (vo.type) {
                    case "0" :
                        colorName = "blue";
                        vesselType = "Passenger";
                        break;
                    case "1" :
                        colorName = "gray";
                        vesselType = "Undefined / unknown";
                        break;
                    case "2" :
                        colorName = "green";
                        vesselType = "Cargo";
                        break;
                    case "3" :
                        colorName = "orange";
                        vesselType = "Fishing";
                        break;
                    case "4" :
                        colorName = "purple";
                        vesselType = "Sailing and pleasure";
                        break;
                    case "5" :
                        colorName = "red";
                        vesselType = "Tanker";
                        break;
                    case "6" :
                        colorName = "turquoise";
                        vesselType = "Pilot, tug and others";
                        break;
                    case "7" :
                        colorName = "yellow";
                        vesselType = "High speed craft and WIG";
                        break;
                    default :
                        colorName = "gray";
                        vesselType = "Undefined / unknown";
                }

                if (vo.moored) {
                    return {
                        name: "vessel_" + colorName + "_moored.png",
                        type: vesselType,
                        width: 12,
                        height: 12,
                        xOffset: -6,
                        yOffset: -6
                    };
                } else {
                    return {
                        name: "vessel_" + colorName + ".png",
                        type: vesselType,
                        width: 20,
                        height: 10,
                        xOffset: -10,
                        yOffset: -5
                    };
                }
            };

        }])



    /**
     * The map-vessel-layer directive supports drawing a list of vessels on a map layer.
     * It will automatically load the vessels for the current map bounding box,
     * but only if the user is logged in.
     */
    .directive('mapVesselLayer', ['$rootScope', '$timeout', 'Auth', 'MapService', 'VesselService', 'growl',
        function ($rootScope, $timeout, Auth, MapService, VesselService, growl) {
            return {
                restrict: 'E',
                replace: false,
                template: '<div id="vessel-info"/>',
                require: '^olMap',
                scope: {
                    name:       '@',
                    alerts:     '=?',
                    vessels:    '=?'
                },
                link: function(scope, element, attrs, ctrl) {
                    var olScope = ctrl.getOpenlayersScope();
                    var vesselLayers;
                    var loadTimer;
                    scope.loggedIn = Auth.loggedIn;

                    olScope.getMap().then(function(map) {

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
                        scope.$on('$destroy', function() {
                            if (angular.isDefined(vesselLayers)) {
                                map.removeLayer(vesselLayers);
                            }
                            if (angular.isDefined(loadTimer)) {
                                $timeout.cancel(loadTimer);
                            }
                        });


                        /** TODO: Remove this method and create a methos similar to colorHexForVessel:
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

                        /** Create a historic vessel feature, with only lat,lon,cog,roc,ts. */
                        scope.createHistoricVesselFeature = function (vessel) {
                            var image = VesselService.imageAndTypeTextForVessel(vessel);

                            var markerStyle = new ol.style.Style({
                                image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
                                    anchor: [0.85, 0.5],
                                    opacity: 0.85,
                                    id: vessel.id,
                                    rotation: vessel.radian,
                                    src: 'img/' + image.name
                                }))
                            });

                            var vesselPosition = new ol.geom.Point(ol.proj.transform([vessel.x, vessel.y], 'EPSG:4326', 'EPSG:900913'));

                            var markerVessel = new ol.Feature({

                                type: image.type,
                                angle: vessel.angle,
                                radian: vessel.radian,  // (vessel.angle * (Math.PI / 180)),
                                latitude: vessel.y,
                                longitude: vessel.x,
                                geometry: vesselPosition
                            });
                            markerVessel.setStyle(markerStyle);
                            return markerVessel;

                        };



                        /** Create a vessel feature for any openlayers 3 map. */
                        scope.createVesselFeature = function (vessel) {
                            var image = VesselService.imageAndTypeTextForVessel(vessel);

                            var markerStyle = new ol.style.Style({
                                image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
                                    anchor: [0.85, 0.5],
                                    opacity: 0.85,
                                    id: vessel.id,
                                    rotation: vessel.radian,
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

                        scope.showVesselDetails = function(vessel) {
                            console.log("ais layer - showVesselDetails");
                            scope.vessel = vessel;
                            var vesselDetails = VesselService.details(vessel);
                            console.log("ais layer = vesselDetails" +vesselDetails);
                            growl.info("got vesseldetails " + vesselDetails);

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
                                    $rootScope.loadingData = false; // stop spinner
                                    growl.success('<b>' +scope.vessels.length +'</b> vessels loaded', {ttl: 3000});

                                })
                                .error(function (reason) {
                                    $rootScope.loadingData = false; // stop spinner
                                    console.log(reason);
                                    growl.error("Connection problems " + reason);
                                });

                        };


                        /** When the map extent changes, reload the Vessels's using a timer to batch up changes */
                        scope.mapChanged = function () {
                            if (MapService.isLayerVisible('vesselVectorLayer', vesselLayers)) {
                                if (loadTimer) {
                                    $timeout.cancel(loadTimer);
                                }
                                loadTimer = $timeout(scope.refreshVessels, 500);
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
                            visible: true
                        });

                        vesselLayers = new ol.layer.Group({
                            title: 'Vessels',
                            layers: [ vesselLayer ],
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

                        // display popup on click
                        map.on('click', function (evt) {
                            var zoomLvl = map.getView().getZoom();

                            if (zoomLvl > 8) {
                                var feature = map.forEachFeatureAtPixel(evt.pixel, function (feature, layer) {
                                    return feature;
                                }, null, function (layer) {
                                    return layer === vesselLayer;
                                });

                                if (feature) {
                                    var geometry = feature.getGeometry();
                                    var coord = geometry.getCoordinates();
                                    popup.setPosition(coord);

                                    $(elm).popover({
                                        placement: 'top',
                                        html: true,
                                        animation: true,
                                        delay: 500,
                                        trigger: 'focus',
                                        'template': '<div class="popover" role="tooltip"><div class="arrow"></div>'
                                        + '<h3 class="popover-title"></h3>'
                                        + '<div class="popover-content"></div>'
                                        + '</div>'
                                    });
                                    var mmsi = feature.get('mmsi');
                                    $(elm).attr('data-content',
                                        //'<div class="popover-content">' +
                                       // '<h3>' + feature.get('name') + '</h3>' +
                                        '<p><span class="glyphicon glyphicon-globe"></span> <a target="_blank" href="http://www.marinetraffic.com/ais/shipdetails.aspx?mmsi=' + feature.get('mmsi') + '">' + feature.get('mmsi') + '</a></p>' +
                                        '<p><span class="glyphicon glyphicon-phone-alt"></span> ' + feature.get('callSign') + '</p>' +
                                        '<p><span class="glyphicon glyphicon-tag"></span> ' + feature.get('type') + '</p>' +
                                        '<p><span class="glyphicon glyphicon-flag"></span> ' + ol.coordinate.toStringHDMS([feature.get('longitude'),feature.get('latitude')], 3) + '</p>' +
                                        '<p><span class="glyphicon glyphicon-flag"></span> ' + feature.get('angle') + '°</p>' 
                                      //   '<p><a href ng-click="VesselService.showVesselInfoFromMMsi(mmsi)" >' +feature.get('mmsi') +'</a><---</p>' +
                                       //     '<p><a href onclick="showVesselInfo('+ feature.get('mmsi') +')">more information on vessel</a> x <a href="#/vessel/'+ feature.get('mmsi') +'">link</a></a></p>'
                                    //    '</div>'
                                    );
                                    $(elm).attr( 'data-placement', 'top' );
                                    $(elm).attr( 'data-original-title', feature.get('name') );
                                    $(elm).attr( 'data-html', true );
                                    $(elm).attr( 'data-animation', true );


                                    $(elm).popover('show');
                                    var pan = ol.animation.pan({
                                            duration: 1500,
                                            source: /** @type {ol.Coordinate} */ (map.getView().getCenter())
                                      });
                                    map.beforeRender(pan);
                                    map.getView().setCenter(coord);


                                } else {
                                    $(elm).popover('destroy');
                                }
                            } else { // close popups when zoomed below lvl 8 and clicks on map...
                                $(elm).popover('destroy');
                                if (scope.loggedIn) {
                                    growl.success('<b>Zoom</b> in for more detailed information');
                                    return;
                                }
                            }
                        });


                    });
                }
            };
        }])


    .directive('route', ['$rootScope', '$timeout', 'MapService', 'VesselService', 'growl',
        function ($rootScope, $timeout, MapService, VesselService, growl) {
            return {
                restrict: 'E',
                require: '^olMap',
                template:
                /*  "<ul class='list-group  hidden-xs hidden-sm message-details-map col-md-6 col-lg-6'>" +
                        "<li class='list-group-item'>" +
                            "<btn ng-if='!animating' class='btn btn-info' id='start-animation' ng-click='startAnimation()'> <i class='fa fa-play' aria-hidden='true'></i> </btn> &nbsp;" +
                            "<btn ng-if='animating' class='btn btn-info' id='start-animation' ng-click='stopAnimation()'> <i class='fa fa-stop' aria-hidden='true'></i> </btn> &nbsp;" +
                            "<label for='speed'>" + " Animation speed:&nbsp;" + "<input id='speed' type='range' min='1' max='999' step='10' value='10'> " + "</label>" +
                        "</li>" +
                       "<li class='list-group-item'>" + "<label for='speed'>" + " Animation speed:&nbsp;" + "<input id='speed' type='range' min='1' max='999' step='10' value='10'> " + "</label>" + "</li>" +*/
/*                        "<li class='list-group-item'>" + "<span class='badge'>{{activeRouteTSTimeAgo}}</span>" + "Time ago:" + "</li>" +
                        "<li class='list-group-item'>" + "<span class='badge'>{{activeRouteTS}}</span>" + "Timestamp:" + "</li>" +
                        "<li class='list-group-item'>" + "<span class='badge'>{{activeRoutePoint | lonlat:{ decimals : 3, pp: true} }}</span>" + "Position:" + "</li>" +
                        "<li class='list-group-item'>" + "<span class='badge'>{{activeRouteCOG}}</span>" + "COG:" + "</li>" +
                        "<li class='list-group-item'>" + "<span class='badge'>{{activeRouteSOG}}</span>" + "SOG:" + "</li>" +



                "</ul>" */
                "<div class=' hidden-xs hidden-sm message-details-route col-md-3 col-lg-3'>" +
                "<btn ng-if='!animating' class='btn btn-success' id='start-animation' ng-click='startAnimation()'> <i class='fa fa-play' aria-hidden='true'></i> </btn> " +
                "<btn ng-if='animating' class='btn btn-danger' id='start-animation' ng-click='stopAnimation()' tooltip='stop animation' data-toggle='tooltip'  " +
                " data-placement='right' title='stop animation'> <i class='fa fa-stop' aria-hidden='true'></i> </btn><br>" +
                "<label for='speed'>" + " Animation speed:&nbsp;" + "<input id='speed' type='range' min='1' max='999' step='10' value='10'> " + "</label><br>" +
                "" +
                "" +
                "<span class='label label-primary'>{{activeRouteTSTimeAgo}}</span><br>" +
                "<span class='label label-primary'>{{activeRouteTS}}</span><br>" +
                "<span class='label label-primary'>{{activeRoutePoint | lonlat:{ decimals : 3, pp: true} }}</span><br>" +
                "<span class='label label-primary'>{{activeRouteCOG}}</span> COG <br>" +
                "<span class='label label-primary'>{{activeRouteSOG}}</span> SOG<br>" +
                "</div>"
                ,
                scope: {
                    name:         '@',
                    autoplay:     '=?',
                    points:       '=?',
                    feat:         '=?',
                    vessel:       '=?'
                },
                link: function(scope, element, attrs, ctrl) {
                    console.log("got route" + scope.points.length +
                                " and vessel " + scope.vessel
                                + " feat=" + scope.feat.length +
                            "autoplay=" + scope.autoplay

                    );
                    var olScope = ctrl.getOpenlayersScope();
                    var routeLayers;



                    olScope.getMap().then(function(map) {

                        var vectorSource = new ol.source.Vector({
                            features: []
                        });


                        var routeFeatureLayer = new ol.layer.Vector({
                            name: "routeVectorLayer",
                            title: "route",
                            source: vectorSource,
                            visible: true
                        });
               /*         var markerStyle = new ol.style.Style({
                            image: new ol.style.Circle({
                                radius: 4,
                                stroke: new ol.style.Stroke({
                                    color: '#00FF00',
                                    width: 2

                                }),
                                fill: new ol.style.Fill({
                                    color: '#0f00ff' // attribute colour
                                })
                            })
                        });

                         routeFeatureLayer.setStyle(markerStyle);
*/
/*                        angular.forEach(scope.points, function(key, value){
                            // make features
                            console.log(value + ' = ' + key);
                            var vesselPosition = new ol.geom.Point(ol.proj.transform(key, 'EPSG:4326', 'EPSG:900913'));
                            var markerVessel = new ol.Feature({
                                geometry: vesselPosition,
                                name: 'super name'
                            });
                            vesselLayer.getSource().addFeature(markerVessel);
                        });*/



                       // markerVessel.setStyle(markerStyle);
                         routeFeatureLayer.getSource().addFeatures(scope.feat);


                       // var center = MapService.fromLonLat([vesselPosition.coordinates[0], vesselPosition.coordinates[1]]);
                       // map.getView().setCenter(center);
                        map.getView().setCenter( routeFeatureLayer.getSource().getFeatures()[0].getGeometry().getCoordinates());
                     /*   console.log(angular.forEach(vesselLayer.getSource().getFeatures(), function(value, key) {
                            console.log(key + ': ' + value.getGeometry().getCoordinates());
                        }));*/

                        console.log("Hey look, it's a new route with " +  routeFeatureLayer.getSource().getFeatures().length + " features");

                        // Clean up when the layer is destroyed
                        scope.$on('$destroy', function () {
                            if (angular.isDefined(routeLayers)) {
                                map.removeLayer(routeLayers);
                            }
                        });


          /*              var polyline = [
                            [55.5, 13.2],
                            [55.4, 13.2],
                            [55.3, 13.2],
                            [55.2, 13.3],
                            [55.1, 13.4],
                            [55.0, 13.5],
                            [55.1, 13.5],
                            [55.2, 13.6],
                            [55.3, 13.7],
                            [55.4, 13.8],
                            [55.5, 13.9]
                        ];
                        polyline.forEach(function(item,index,arr){
                            console.log("#" + index + "  " + item);
                            console.log("?"  );
                            arr[index] = ol.proj.transform([ item[1],item[0]], 'EPSG:4326', 'EPSG:900913');

                        });*/
               /*         scope.feat.forEach(function(item,index,arr){
                            // console.log("#" + index + "  " + item);
                            console.log("#" + index + "?" + item.getGeometry() );
                           // arr[index] = ol.proj.transform([ item[1],item[0]], 'EPSG:4326', 'EPSG:900913');

                        });*/
                        var lineRoute = new ol.geom.LineString(scope.points);

                        var routeCoords = lineRoute.getCoordinates();
                        var routeLength = routeCoords.length;

                        var routeFeature = new ol.Feature({
                            type: 'route',
                            geometry: lineRoute
                        });
                        /*
                        var geoMarker = new ol.Feature({
                            type: 'geoMarker',
                            geometry: new ol.geom.Point(routeCoords[0])
                        });
                       var startMarker = new ol.Feature({
                            type: 'icon',
                            geometry: new ol.geom.Point(routeCoords[0])
                        });
                        var endMarker = new ol.Feature({
                            type: 'icon',
                            geometry: new ol.geom.Point(routeCoords[routeLength - 1])
                        });*/
                        var startMarker = scope.feat[0];
                        var endMarker = scope.feat[scope.feat.length-1];

                        var styles = {
                            'route': new ol.style.Style({
                                stroke: new ol.style.Stroke({
                                    width: 6, color: [237, 212, 0, 0.8]
                                })
                            }),
                            'icon': new ol.style.Style({
                                image: new ol.style.Icon({
                                    anchor: [0.5, 1],
                                    src: 'img/geolocation_marker.png'
                                })
                            }),
                            'geoMarker': new ol.style.Style({
                                image: new ol.style.Icon({
                                    anchor: [0.5, 1],
                                    src: 'img/geolocation_marker.png'
                                })
                            })
                        };

                        scope.animating = false;
                        var speed, now, orgIndexVal;
                        var speedInput = document.getElementById('speed');
                       // var startButton = document.getElementById('start-animation');

                        var animationLayer = new ol.layer.Vector({
                            source: new ol.source.Vector({
                                features: [routeFeature, startMarker, endMarker]
                            }),
                            style: function(feature) {
                                // hide geoMarker if animation is active
                                if (scope.animating && feature.get('type') === 'geoMarker') {
                                    return null;
                                }
                                return styles[feature.get('type')];
                            }
                        });

     /*                   var map = new ol.Map({
                            target: document.getElementById('map'),
                            loadTilesWhileAnimating: true,
                            view: new ol.View({
                                center: center,
                                zoom: 10,
                                minZoom: 2,
                                maxZoom: 19
                            }),
                            layers: [
                                /!*            new ol.layer.Tile({
                                 source: new ol.source.BingMaps({
                                 imagerySet: 'AerialWithLabels',
                                 key: 'AkGbxXx6tDWf1swIhPJyoAVp06H0s0gDTYslNWWHZ6RoPqMpB9ld5FY1WutX8UoF'
                                 })
                                 })*!/
                                new ol.layer.Tile({
                                    title: 'OpenStreetMap',
                                    type: 'base',
                                    visible: true,
                                    source: new ol.source.OSM()
                                })
                                ,
                                vectorLayer
                            ]
                        });*/
                        var index = 0;

                        var moveFeature = function(event) {
                            var vectorContext = event.vectorContext;
                            var frameState = event.frameState;

                            if (scope.animating) {
                                var elapsedTime = frameState.time - now;
                                // here the trick to increase speed is to jump some indexes
                                // on lineString coordinates
                                var index = Math.round(speed * elapsedTime / 1000);

                                // framerate diagnostics
                                //console.log("point=" + index + " out of " + routeLength + " diff " + (index - orgIndexVal));
                                //orgIndexVal = index;
                                if (index >= routeLength) {
                                    index = 0; // rewind, and loop
                                    console.log("loop, one more time");
                                    scope.stopAnimation(true);
                                    //return;
                                }

                               // var currentPoint = new ol.geom.Point(routeCoords[index]);
                                //var feature = new ol.Feature(currentPoint);
                                var feature = scope.feat[index];
                                vectorContext.drawFeature(feature, feature.getStyle());
                                //vectorContext.drawFeature(feature, feature.getStyle().getImage().setOpacity(1.0));
                                scope.activeRoutePoint = feature.get('position') ;
                                scope.activeRouteTS = feature.get('ts')  ;
                                scope.activeRouteTSTimeAgo = feature.get('tsTimeAgo')  ;
                                scope.activeRouteCOG = feature.get('cog') ;
                                scope.activeRouteSOG = feature.get('sog') ;

                                //index++;

                                //  vectorContext.drawFeature(feature, styles.geoMarker);


                            }
                            // tell OL3 to continue the postcompose animation
                            map.render();
                        };



                        scope.startAnimation =  function() {
                            if (scope.animating) {
                                scope.stopAnimation(false);
                            } else {

                                scope.animating = true;
                                now = new Date().getTime();
                                speed = speedInput.value;
                                //speed = 10;
                                // startButton.textContent = 'Cancel Animation';
                                // hide geoMarker
                               // geoMarker.setStyle(null);
                                // just in case you pan somewhere else
                                // map.getView().setCenter(center);
                                map.on('postcompose', moveFeature);
                                map.render();
                            }
                        };


                        /**
                         * @param {boolean} ended end of animation.
                         */
                        scope.stopAnimation = function(ended) {
                            scope.animating = false;
                          //  startButton.textContent = 'Start Animation';

                            // if animation cancelled set the marker at the beginning
                           /* var coord = ended ? routeCoords[routeLength - 1] : routeCoords[0];
                            /!** @type {ol.geom.Point} *!/ (geoMarker.getGeometry())
                                .setCoordinates(coord);
                            //remove listener
                            map.un('postcompose', moveFeature);*/
                        };

                        // automatically zoom and pan the map to fit my features
                        var extent = animationLayer.getSource().getExtent();
                        map.getView().fit(extent, map.getSize());

                        routeLayers = new ol.layer.Group({
                            title: 'Route',
                            layers: [  animationLayer ],
                            visible: true
                        });

                        map.addLayer(routeLayers);


                        if(scope.autoplay){
                            scope.startAnimation();
                        };

                        //startButton.addEventListener('click', startAnimation, false);
                    });
                }
            }
        }])

     /*******************************************************************
     * Controller that handles displaying vessel details in a dialog
     *******************************************************************/
    .controller('VesselDialogCtrl', ['$scope', '$window', 'VesselService', 'message', 'growl', 'timeAgo','$filter',
        function ($scope, $window, VesselService, message, growl, timeAgo, $filter) {
            'use strict';
           // console.log("Vessel data= " + JSON.stringify(message.data));
            $scope.warning = undefined;
            $scope.msg = message;

            $scope.getHistoricalTrack = function(mmsi, type) {
                 VesselService.historicalTrack(mmsi).then(function successCallback(response) {
                    // this callback will be called asynchronously
                    // when the response is available
                    $scope.historicalTrackOutput = response.data;
                     growl.success("Retrieved historical points " + response.data.length)

                     var linePoints = [];
                     angular.forEach(response.data, function(value, key) {
                         this.push(ol.proj.transform([value.lon, value.lat], 'EPSG:4326', 'EPSG:900913'));
                     }, linePoints);
                     $scope.routePoints = linePoints;

                     // Features are more detailed
                     var lineFeatures = [];
                     var generateHistoricalMarker = function (value) {
                         var vesselPosition = new ol.geom.Point(ol.proj.transform([value.lon, value.lat], 'EPSG:4326', 'EPSG:900913'));
                         var markerStyle = new ol.style.Style({
                             image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
                                 anchor: [1.0, 0.5],
                                 opacity: 0.90,
                                 id: value.ts,
                                 rotation: (value.cog - 90) * (Math.PI / 180),
                                 src: 'img/vessel_green.png'
                             }))
                             ,
                             text: new ol.style.Text({
                                 text: $filter('timeAgo')(value.ts) + ' - ' + $filter('date')(value.ts, 'yyyy-MM-dd HH:mm:ss Z', 'UTC') + ' UTC', // attribute code
                                 size: 10,
                                 offsetY: 20
                                 /*,
                                 font: "Arial",
                                 fill: new ol.style.Fill({
                                     color: 'green' // black text //
                                 })
                                 */
                             })
                         });

                         var markerVessel = new ol.Feature({
                             geometry: vesselPosition,
                             cog: value.cog,
                             sog: value.sog,
                             id: value.ts,
                             ts: $filter('date')(value.ts, 'yyyy-MM-dd HH:mm:ss Z', 'UTC') + ' UTC',
                             tsTimeAgo:  $filter('timeAgo')(value.ts),
                             position: $scope.toLonLat( value.lon, value.lat)
                         });
                         markerVessel.setStyle(markerStyle);
                         return markerVessel;
                     };
/*
                      iterate over the historical track data retrievied in the format
                         [{
                         "cog": 122.9,
                         "lat": 55.723106666666666,
                         "lon": 21.10174333333333,
                         "sog": 0,
                         "ts": 1465473596620
                         },
                         {...}
                         ]
                      */ 
                     angular.forEach(response.data, function(value, key) {
                         var markerVessel = generateHistoricalMarker(value);
                         this.push(markerVessel);
                     }, lineFeatures);
                     $scope.routeFeatures = lineFeatures;
                     console.log("$scope.routePoints.length="+$scope.routePoints.length) ;
                     console.log("$scope.routeFeatures.length="+$scope.routeFeatures.length) ;
                    return response;

                }, function errorCallback(response) {
                    // called asynchronously if an error occurs
                    // or server returns response with an error status.
                    console.error("Error historicalTrack=" + response.status);
                     growl.error("No historical for this vessel" + vessel.name);

                });

            };

            /** Returns the lat-lon attributes of the vessel */
            $scope.toLonLat = function (long, lati) {
                return {lon: long, lat: lati};
            };

            var navStatusTexts = {
                0: "Under way using engine",
                1: "At anchor",
                2: "Not under command",
                3: "Restricted manoeuvrability",
                4: "Constrained by her draught",
                5: "Moored",
                6: "Aground",
                7: "Engaged in fishing",
                8: "Under way",
                12: "Power-driven vessel pushing ahead or towing alongside",
                14: "Ais SART",
                15: "Undefined"
            };

            $scope.navStatusText = function (navStatus) {
                if (navStatus && navStatusTexts.hasOwnProperty(navStatus)) {
                    return navStatusTexts[navStatus]
                }
                return null;
            };
        }]);
 