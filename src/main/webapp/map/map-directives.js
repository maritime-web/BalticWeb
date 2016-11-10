/**
 * Base map directives.
 * <p/>
 * Inspiration:
 * https://github.com/tombatossals/angular-openlayers-directive
 * <p/>
 * Example usage:
 * <pre>
 *   <ol-map class="map" map-state="state.map">
 *      <map-tile-layer name="OSM" visible="true" source="OSM"></map-tile-layer>
 *      <map-vessel-layer name="Vessels" visible="false"></map-aton-layer>
 *      <map-layer-switcher></map-layer-switcher>
 *   </ol-map>
 * </pre>
 */
angular.module('maritimeweb.map')

    /**
     * A latitude directive that may be used with an input field
     */
    .directive('latitude', function() {
        return positionDirective('latitude', formatLatitude, parseLatitude);
    })


    /**
     * A longitude directive that may be used with an input field
     */
    .directive('longitude', function() {
        return positionDirective('longitude', formatLongitude, parseLongitude);
    })


    /**
     * Formats a position
     */
    .filter('lonlat', ['$rootScope', function() {
        return function(input, args) {
            args = args || {};
            return input && input.lat && input.lon ? formatLatLon(input, args.decimals, args.pp).trim() : '';
        };
    }])

    /**
     * Displays trusted content
     */
    .filter('toTrusted', ['$sce', function ($sce) {
        return function (value) {
            return $sce.trustAsHtml(value);
        };
    }])

    /**
     * Defines the parent ol-map directive.
     */
    .directive('olMap', ['$rootScope', '$q', '$timeout', 'MapService', '$window', function ($rootScope, $q, $timeout, MapService, $window) {
        return {
            restrict: 'EA',
            replace: true,
            transclude: true,
            template: "<div class='map {{class}}' ng-cloak ng-transclude>" +
            "</div>",
            scope: {
                mapState: '=',
                readonly: '=',
                minzoom: '=',
                extent: '=',
                swlon: '=',  // 9
                swlat: '=', // 53
                nelon: '=', // 31
                nelat: '=' // 66
                // 9, 53, 31, 66
                // sw_lon, sw_lat, ne_lon, ne_lat

            },

            controller: function($scope) {

                var _map = $q.defer();

                $scope.getMap = function() {
                    return _map.promise;
                };

                $scope.setMap = function(map) {
                    _map.resolve(map);
                };

                this.getOpenlayersScope = function() {
                    return $scope;
                };
            },

            link: function(scope, element, attrs) {
                var isDefined = angular.isDefined;
                var updateSizeTimer;


                // Clean-up
                element.on('$destroy', function() {

                    if (isDefined(updateSizeTimer)) {
                        $timeout.cancel(updateSizeTimer);
                        updateSizeTimer = null;
                    }
                });


                // Set width and height if they are defined
                if (isDefined(attrs.width)) {
                    if (isNaN(attrs.width)) {
                        element.css('width', attrs.width);
                    } else {
                        element.css('width', attrs.width + 'px');
                    }
                }


                if (isDefined(attrs.height)) {
                    if (isNaN(attrs.height)) {
                        element.css('height', attrs.height);
                    } else {
                        element.css('height', attrs.height + 'px');
                    }
                }


                // Enable rotation on mobile devices
                var controls = scope.readonly ? [] : ol.control.defaults({ rotate: false }).extend([new ol.control.Rotate()]);
                var interactions = scope.readonly ? [] : ol.interaction.defaults({ altShiftDragRotate: true, pinchRotate: true});
             //   console.log("extent" +[scope.swlon, scope.swlat, scope.nelon, scope.nelat]);

                var balticExtent = ol.proj.transformExtent([scope.swlon, scope.swlat, scope.nelon, scope.nelat], 'EPSG:4326', 'EPSG:3857');
               // var balticExtent = ol.proj.transformExtent([9, 53, 31, 66], 'EPSG:4326', 'EPSG:3857');
                var layers = [];
                var view = new ol.View({
                    zoom: 7,
                    minZoom: scope.minzoom,
                    extent: balticExtent
                });
                var map = new ol.Map({
                    target: angular.element(element)[0],
                    layers: layers,
                    view: view,
                    controls: controls,
                    interactions: interactions
                });
                //map.addControl(new ol.control.Rotate());

                // Set extent (center and zoom) of the map.
                scope.updateMapExtent = function (initial) {
                    // Default values
                    var center = MapService.defaultCenterLonLat();
                    var zoom = MapService.defaultZoomLevel();

                    // Check if the center is defined in the directive attributes or in the mapState
                    if (initial && isDefined(attrs.lat) && isDefined(attrs.lon)) {
                        center = [  parseFloat(attrs.lon), parseFloat(attrs.lat) ];
                    } else if (isDefined(scope.mapState) && isDefined(scope.mapState.center)) {
                        center = scope.mapState.center;
                    }

                    // Check if the zoom is defined in the directive attributes or in the mapState
                    if (initial && isDefined(attrs.zoom)) {
                        zoom = parseFloat(attrs.zoom);
                    } else if (isDefined(scope.mapState) && isDefined(scope.mapState.zoom)) {
                        zoom = scope.mapState.zoom;
                    }

                    // Update the map
                    view.setCenter(MapService.fromLonLat(center));
                    view.setZoom(zoom);
                    
                };
                scope.updateMapExtent(true);


                // Check for the map reload flag
                if (isDefined(scope.mapState) && isDefined(scope.mapState.reloadMap)) {
                    scope.$watch("mapState.reloadMap", function (reload) {
                        if (reload) {
                            scope.mapState['reloadMap'] = false;
                            scope.updateMapExtent(false);
                        }
                    }, true);
                }


                // Whenever the map extent is changed, record the new extent in the mapState
                if (isDefined(scope.mapState)) {
                    scope.mapChanged = function () {
                        var extent = view.calculateExtent(map.getSize());
                        scope.mapState['zoom'] = view.getZoom();
                        scope.mapState['center'] = MapService.round(MapService.toLonLat(view.getCenter()), 4);
                        scope.mapState['extent'] = MapService.round(MapService.toLonLatExtent(extent), 4);
                        scope.mapState['wktextent'] = MapService.extentToWkt(extent);
                        $window.localStorage.setItem('mapState-storage', JSON.stringify(scope.mapState)); // storing map state
                        scope.$$phase || scope.$apply();
                    };
                    map.on('moveend', scope.mapChanged);
                }


                // Update the map size if the element size changes.
                // In theory, this should not be necessary, but it seems to fix a problem
                // where maps are sometimes distorted
                scope.updateSize = function () {
                    updateSizeTimer = null;
                    map.updateSize();
                };
                scope.$watchGroup([
                    function() { return element[0].clientWidth; },
                    function() { return element[0].clientHeight; }
                ], function () {
                    if (isDefined(updateSizeTimer)) {
                        $timeout.cancel(updateSizeTimer);
                    }
                    updateSizeTimer = $timeout(scope.updateSize, 100);
                });


                // Resolve the map object to the promises
                scope.setMap(map);
            }
        };
    }])


    /**
     * The map-tile-layer directive will add a simple tile layer to the map
     */
    .directive('mapTileLayer', [function () {
        return {
            restrict: 'E',
            replace: false,
            require: '^olMap',
            scope: {
                name: '@',
                visible: '=',
                source: '@',
                sourceProperties: '='
            },
            link: function(scope, element, attrs, ctrl) {
                var olScope = ctrl.getOpenlayersScope();
                var olLayer;

                olScope.getMap().then(function(map) {

                    scope.$on('$destroy', function() {
                        if (angular.isDefined(olLayer)) {
                            map.removeLayer(olLayer);
                        }
                    });

                    switch (scope.source) {
                        case 'MapQuest':
                            olLayer = new ol.layer.Tile({
                                title: scope.name,
                                source: new ol.source.MapQuest(scope.sourceProperties)
                            });
                            break;

                        case 'OSM':
                            olLayer = new ol.layer.Tile({
                                title: scope.name,
                                source: new ol.source.OSM()
                            });
                            break;

                        case 'WMS':
                            olLayer = new ol.layer.Tile({
                                title: scope.name,
                                source: new ol.source.TileWMS(scope.sourceProperties)
                            });
                            break;
                    }

                    // If the layer got created, add it
                    if (olLayer) {
                        olLayer.setVisible(scope.visible);
                        map.addLayer(olLayer);
                    }

                });

            }
        };
    }])

    /**
     * The map-layer-group adds an entire layer group to the map
     */
    .directive('mapLayerGroup', [function () {
        return {
            restrict: 'E',
            replace: false,
            require: '^olMap',
            scope: {
                layerGroup: '='
            },
            link: function(scope, element, attrs, ctrl) {
                var olScope = ctrl.getOpenlayersScope();

                olScope.getMap().then(function(map) {

                    if (angular.isDefined(scope.layerGroup)) {
                        map.addLayer(scope.layerGroup);
                    }

                    scope.$on('$destroy', function() {
                        if (angular.isDefined(scope.layerGroup)) {
                            map.removeLayer(scope.layerGroup);
                        }
                    });
                });
            }
        };
    }])


    /**
     * The map-overview adds an overview map to the map.
     */
    .directive('mapOverview', [function () {
        return {
            restrict: 'E',
            require: '^olMap',
            scope: {
                collapsed: '='
            },
            link: function(scope, element, attrs, ctrl) {
                var olScope         = ctrl.getOpenlayersScope();
                var overviewMap = new ol.control.OverviewMap({
                    collapsed: scope.collapsed || false,
                    layers: [
                        new ol.layer.Tile({
                            source: new ol.source.OSM({ layer: 'sat' })
                        })
                    ],
                    collapseLabel: '-',
                    label: '+'
                });

                olScope.getMap().then(function(map) {
                    map.addControl(overviewMap);

                    // When destroyed, clean up
                    scope.$on('$destroy', function() {
                        map.removeControl(overviewMap);
                    });

                });
            }
        };
    }])


    /**
     * The map-layer-switcher adds a layer switcher to the map.
     */
    .directive('mapLayerSwitcher', [function () {
        return {
            restrict: 'E',
            require: '^olMap',
            link: function(scope, element, attrs, ctrl) {
                var olScope         = ctrl.getOpenlayersScope();
                var layerSwitcher   = new ol.control.LayerSwitcher();

                olScope.getMap().then(function(map) {
                    map.addControl(layerSwitcher);

                    // When destroyed, clean up
                    scope.$on('$destroy', function() {
                        map.removeControl(layerSwitcher);
                    });

                });
            }
        };
    }])


    /**
     * The map-current-pos-btn directive will add a current-position button to the map. The button centers on current position and places a image marker.
     */
    .directive('mapCurrentPosBtn', ['$window', 'MapService', 'growl', function ($window, MapService, growl) {
        return {
            restrict: 'E',
            replace: false,
            require: '^olMap',
            template:
                "<span class='map-current-pos-btn'  tooltip='Go to current position' data-toggle='tooltip' data-placement='right' title='Go to current position' >" +
                    "<span><i class='fa fa-location-arrow' ng-click='currentPos()'aria-hidden='true'></i></span>" +
                //"  <span class='glyphicon glyphicon-map-marker' ng-click='currentPos()' tooltip='Current Position'></span>" +
                "</span>",
            scope: {
            },
            link: function(scope, element, attrs, ctrl) {
                var olScope     = ctrl.getOpenlayersScope();
                olScope.getMap().then(function(map) {
                    scope.currentPos = function () {
                        scope.loadingData = true; // start spinner
                        $window.navigator.geolocation.getCurrentPosition(function (pos) {
                            console.log('Got current position', pos.coords);

                            var center = MapService.fromLonLat([pos.coords.longitude, pos.coords.latitude]);
                            map.getView().setCenter(center);
                            map.getView().setZoom(15);

                            var markerPosition = new ol.geom.Point(ol.proj.transform([pos.coords.longitude, pos.coords.latitude], 'EPSG:4326', 'EPSG:900913'));
                            var markerStyle = new ol.style.Style({
                                image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
                                    anchor: [0.5, 0.5],
                                    src: 'img/geolocation_marker.png'
                                }))
                            });

                            var markerFeature = new ol.Feature({
                                geometry: markerPosition
                            });
                            markerFeature.setStyle(markerStyle);

                            var vectorSource = new ol.source.Vector({
                                features: [markerFeature]
                            });

                            var vectorLayer = new ol.layer.Vector({
                                source: vectorSource
                            });
                            map.addLayer(vectorLayer);

                            scope.loadingData = false; // stop spinner

                        }, function () {
                            scope.loadingData = false; // stop spinner
                            console.error('Unable to get current position');
                            growl.error('Unable to retrieve current position');
                        });
                    }

                });

            }
        };
    }])

    /**
     * The map-current-pos-btn directive will add a current-position button to the map.
     */
    .directive('mapCurrentPosOrientationBtn', ['$window', 'MapService', function ($window, MapService) {
        return {
            restrict: 'E',
            replace: false,
            require: '^olMap',
            template:
            "<span class='map-current-pos-btn'>" +
            " <span><i class='fa fa-location-arrow' aria-hidden='true' ng-click='currentPosOrientation()' tooltip='Current Position and orientation' ></i></span>" +
            "</span>",
            scope: {
            },
            link: function(scope, element, attrs, ctrl) {
                var olScope     = ctrl.getOpenlayersScope();
                olScope.getMap().then(function(map) {
                    scope.currentPosOrientation = function () {
                        scope.loadingData = true; // start spinner
                        $window.navigator.geolocation.getCurrentPosition(function (pos) {

                            var center = MapService.fromLonLat([pos.coords.longitude, pos.coords.latitude]);
                            map.getView().setCenter(center);
                            map.getView().setZoom(15);

                            var markerPosition = new ol.geom.Point(ol.proj.transform([pos.coords.longitude, pos.coords.latitude], 'EPSG:4326', 'EPSG:900913'));
                            var markerStyle = new ol.style.Style({
                                image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
                                    anchor: [0.5, 0.5],
                                    src: 'img/geolocation_marker.png'
                                }))
                            });

                            var markerFeature = new ol.Feature({
                                geometry: markerPosition
                            });
                            markerFeature.setStyle(markerStyle);

                            var vectorSource = new ol.source.Vector({
                                features: [markerFeature]
                            });

                            var vectorLayer = new ol.layer.Vector({
                                source: vectorSource
                            });
                            map.addLayer(vectorLayer);

                            var deviceOrientation = new ol.DeviceOrientation();
                            deviceOrientation.setTracking(true);

                            // tilt the map
                            deviceOrientation.on(['change:beta', 'change:gamma'], function(event) {
                                var center = map.getView().getCenter();
                                var resolution = view.getResolution();
                                var beta = event.target.getBeta() || 0;
                                var gamma = event.target.getGamma() || 0;

                                center[0] -= resolution * gamma * 25;
                                center[1] += resolution * beta * 25;
                                console.log("devices orientation is changing. Beta=" + beta + " gamma=" + gamma);

                                map.getView().setCenter(map.getView().constrainCenter(center));
                            });

                            scope.loadingData = false; // stop spinner

                        }, function () {
                            scope.loadingData = false; // stop spinner
                            console.error('Unable to get current position');
                            growl.error('Unable to retrieve current position');
                        });
                    }

                });

            }
        };
    }])

    /**
     * The map-scale-line directive will add a scale line to the map.
     */
    .directive('mapScaleLine', [function () {
        return {
            restrict: 'E',
            replace: true,
            require: '^olMap',
            template:
            "<span class='map-scale-line'  tooltip='nautical miles' data-toggle='tooltip' data-placement='bottom' title='nautical miles'></span>",
            scope: {
                units    : '@',
                minWidth : '='
            },
            link: function(scope, element, attrs, ctrl) {
                var olScope     = ctrl.getOpenlayersScope();
                var scaleLine   = new ol.control.ScaleLine({
                    className: 'ol-scale-line',
                    units: scope.units || 'nautical',
                    minWidth: scope.minWidth || 80,
                    target: angular.element(element)[0]
                });

                olScope.getMap().then(function(map) {

                    map.addControl(scaleLine);

                    // When destroyed, clean up
                    scope.$on('$destroy', function() {
                        map.removeControl(scaleLine);
                    });
                });
            }
        };
    }])

    /**
     * The map-mouse-position directive will add a current-mouse position panel the map.
     */
    .directive('mapMousePosition', ['MapService', function (MapService) {
        return {
            restrict: 'E',
            replace: true,
            require: '^olMap',
            template:
                "<div class='map-mouse-position'>{{currentPos | lonlat:{ decimals : 2, pp: true }  }}</div>",
            scope: {
            },
            link: function(scope, element, attrs, ctrl) {
                var olScope         = ctrl.getOpenlayersScope();
                scope.currentPos    = undefined;

                olScope.getMap().then(function(map) {

                    // Update the tooltip whenever the mouse is moved
                    map.on('pointermove', function(evt) {
                        var lonlat = MapService.toLonLat(evt.coordinate);
                        scope.currentPos = { lon: lonlat[0], lat: lonlat[1] };
                        scope.$$phase || scope.$apply();
                    });

                 /*
                    $(map.getViewport()).on('mouseout', function() {
                        scope.currentPos = undefined;
                        scope.$$phase || scope.$apply();
                    });*/
                });
            }
        };
    }])
    /**
     *
     */
    .directive('mapSidebarBtn', ['MapService', '$log', '$rootScope', function (MapService, $log, $rootScope) {
        return {
            restrict: 'E',
            replace: true,
            require: '^olMap',
            template:
            "<span class='sidebar-toggle-btn' ng-click='toggleSideBarList()'  " +
            " tooltip='Toggle detailed list-view' data-toggle='tooltip' data-placement='right' title='Toggle detailed list-view'>" +
            "   <span >" +
            "<i ng-if='!$root.showgraphSidebar' class='fa fa-list fa-lg ' aria-hidden='true'></i> </span>" +
            "<i ng-if='$root.showgraphSidebar' class='fa fa-times fa-lg ' aria-hidden='true'></i> </span>" +
            "</span>" ,
            scope: {
            },
            link: function(scope, element, attrs, ctrl) {
                var olScope         = ctrl.getOpenlayersScope();
                $rootScope.showgraphSidebar = true;
                $log.info();
                scope.toggleSideBarList = function(){
                    $rootScope.showgraphSidebar = !$rootScope.showgraphSidebar;
                };
                scope.disableSidebar = function(){
                    $rootScope.showgraphSidebar = false;
                }
            }
        };
    }]);

