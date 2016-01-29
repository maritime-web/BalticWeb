embryo.eventbus.PostLayerInitialize = function () {
    var event = jQuery.Event("PostLayerInitializeEvent");
    return event;
};

embryo.eventbus.MapInitialized = function () {
    var event = jQuery.Event("MapInitializedEvent");
    return event;
};

embryo.eventbus.HighLightEvent = function(feature) {
    var event = jQuery.Event("HighLightEvent");
    event.feature = feature;
    return event;
};

embryo.eventbus.UnHighLightEvent = function(feature) {
    var event = jQuery.Event("UnHighLightEvent");
    event.feature = feature;
    return event;
};

embryo.eventbus.registerShorthand(embryo.eventbus.HighLightEvent, "highlight");
embryo.eventbus.registerShorthand(embryo.eventbus.UnHighLightEvent, "unhighlight");
embryo.eventbus.registerShorthand(embryo.eventbus.MapInitialized, "mapInitialized");
embryo.eventbus.registerShorthand(embryo.eventbus.PostLayerInitialize, "postLayerInitialization");

$(function() {

    var selectLayerByGroup = {};

    var controlsByGroup = {};

    var e1 = new OpenLayers.Bounds(-179, -89, 179, 89);

    e1.transform(new OpenLayers.Projection("EPSG:4326"), new OpenLayers.Projection(embryo.projection));

    var map = new OpenLayers.Map({
        div : "map",
        controls : [ new OpenLayers.Control.Navigation({
            dragPanOptions : {
                enableKinetic : false
            }
        }) ],
        projection : embryo.projection,
        maxExtent : e1,
        fractionalZoom : false
    });

    var selectControl = new OpenLayers.Control.SelectFeature([], {
        clickout : true,
        toggle : true,
        id : 'ClickCtrl'
    });
    
    var hoverControl = new OpenLayers.Control.SelectFeature([], {
        id : 'HoverCtrl',
        hover : true,
        // highlightOnly : true,
        eventListeners : {
            // OpenLayers does not have a general support for
            // featurehighlighted and featureunhighlighted events like
            // layer.events.on({featureselected:function(event){}})
            // Build our own using embryo.eventbus
            featurehighlighted : function(event) {
                embryo.eventbus.fireEvent(embryo.eventbus.HighLightEvent(event.feature));
            },
            featureunhighlighted : function(feature) {
                embryo.eventbus.fireEvent(embryo.eventbus.UnHighLightEvent(feature.feature));
            }
        }
    });

    function hasZIndex(layer) {
        return layer.metadata && layer.metadata.zIndex >= 0;
    }

    function sortLayers(olMap) {
        var layers = olMap.layers.slice(0);

        layers.sort(function (l1, l2) {
            if (!hasZIndex(l1) && !hasZIndex(l2)) {
                return 0;
            }
            if (hasZIndex(l1) && !hasZIndex(l2)) {
                return -1;
            }
            if (!hasZIndex(l1) && hasZIndex(l2)) {
                return 1;
            }
            return l1.metadata.zIndex - l2.metadata.zIndex;
        });

        for (var index = 0, len = layers.length; index < len && hasZIndex(layers[index]); index++) {
            olMap.setLayerIndex(layers[index], index);
        }
    }

    function hasZIndex(layer) {
        return layer.metadata && layer.metadata.zIndex >= 0;
    }

    function sortLayers(olMap) {
        var layers = olMap.layers.slice(0);

        layers.sort(function (l1, l2) {
            if (!hasZIndex(l1) && !hasZIndex(l2)) {
                return 0;
            }
            if (hasZIndex(l1) && !hasZIndex(l2)) {
                return -1;
            }
            if (!hasZIndex(l1) && hasZIndex(l2)) {
                return 1;
            }
            return l1.metadata.zIndex - l2.metadata.zIndex;
        });

        for (var index = 0, len = layers.length; index < len && hasZIndex(layers[index]); index++) {
            olMap.setLayerIndex(layers[index], index);
        }
    }

    var initialized = false;

    embryo.map = {
        selectControl: selectControl,
        selectLayerByGroup: selectLayerByGroup,

        add : function(d) {
            if (d.group && selectLayerByGroup[d.group] == null) {
                selectLayerByGroup[d.group] = [];
                controlsByGroup[d.group] = [];
            }

            if (d.layer) {
                map.addLayer(d.layer);
                if (d.select && d.group)
                    selectLayerByGroup[d.group].push(d.layer);
            }

            if (d.control) {
                map.addControl(d.control);
                if (d.group)
                    controlsByGroup[d.group].push(d.control);
            }

            sortLayers(map);
        },
        remove: function (d) {
            if (d.layer) {
                map.removeLayer(d.layer);
            }
        },
        createPoint : function(longitude, latitude) {
            return new OpenLayers.Geometry.Point(longitude, latitude).transform(new OpenLayers.Projection("EPSG:4326"),
                    map.getProjectionObject());
        },
        transformToPosition: function (point) {
            var pointRes = point.clone().transform(map.getProjectionObject(), new OpenLayers.Projection("EPSG:4326"));
            return {
                lon: pointRes.x,
                lat: pointRes.y
            };
        },
        select : function(feature) {
            selectControl.unselectAll();
            if (feature != null)
                selectControl.select(feature);
        },
        selectMultiple: function (selectMultiple) {
            if (selectMultiple) {
                selectControl.unselectAll();
                selectControl.deactivate();
            } else {
                selectControl.activate();
            }
        },
        selectMultiple: function (selectMultiple) {
            if (selectMultiple) {
                selectControl.unselectAll();
                selectControl.deactivate();
            } else {
                selectControl.activate();
            }
        },
        activateSelectable : function(){
        	
        	if(!selectControl.active) {
        		selectControl.activate();
        	}
        },
        deactivateSelectable : function(){
        	
        	if(selectControl.active) {
        		selectControl.deactivate();
        	}
		},
        zoomToExtent : function(layers) {
            var extent = new OpenLayers.Bounds();

            extent.bottom = 9999999;
            extent.left = 9999999;
            extent.right = -9999999;
            extent.top = -9999999;

            var dataPresent = false;

            for ( var i in layers) {
                var e = layers[i].getDataExtent();
                if (e != null) {
                    dataPresent = true;
                    extent.bottom = Math.min(extent.bottom, e.bottom);
                    extent.left = Math.min(extent.left, e.left);
                    extent.top = Math.max(extent.top, e.top);
                    extent.right = Math.max(extent.right, e.right);
                }
            }

            if (!dataPresent) {
                return;
            }

            // nudge extent so that the left overlay does not overlay the zoomed
            // layers

            var deltaV = extent.top - extent.bottom;
            var deltaH = extent.right - extent.left;

            extent.bottom -= deltaV * 0.05;
            extent.left -= deltaH * 0.35;
            extent.right += deltaH * 0.05;
            extent.top += deltaV * 0.1;

            map.zoomToExtent(extent);
        },
        zoomToCoords : function(minPoint, maxPoint) {
        	if(!(map.getExtent().containsPixel(minPoint) && map.getExtent().containsPixel(maxPoint))) {
        		var b = new OpenLayers.Bounds();
        		b.extend(minPoint);
        		b.extend(maxPoint);
        		map.zoomToExtent(b);
        	}
        },
        zoomToBounds: function (bounds, force) {
            if (!map.getExtent().containsBounds(bounds, false, true) || force) {
                var b = new OpenLayers.Bounds();
                b.extend(bounds);

                var deltaV = b.top - b.bottom;
                var deltaH = b.right - b.left;
                b.bottom -= deltaV * 0.05;
                b.left -= deltaH * 0.35;
                b.right += deltaH * 0.05;
                b.top += deltaV * 0.1;

                map.zoomToExtent(b);
            }
        },
        setCenter : function(longitude, latitude, zoom) {
            var pos = transformPosition(longitude, latitude);

            // set position to find center in pixels
            map.setCenter(pos, zoom);

            var newCenterPx = map.getPixelFromLonLat(pos);
            // check if new center will provoke a blue rectangel in the top when
            // using OSM tile Map (no tiles on the pole)
            // and correct center value to remove blue rectangel
            if (newCenterPx.y > (1000 - 50) / 2) {
                newCenterPx.y += (newCenterPx.y - (1000 - 50) / 2);
                pos = map.getLonLatFromPixel(newCenterPx);
            }

            map.setCenter(pos, zoom);
        },
        getPxFromPosition : function(longitude, latitude) {
            var lonLat = transformPosition(longitude, latitude);
            var pixel = map.getViewPortPxFromLonLat(lonLat);
            return pixel;
        },
        getLonLatFromPixel : function(x, y) {
            return map.getLonLatFromPixel(new OpenLayers.Pixel(x, y));
        },
        lonLatDifference : function(lonLat1, lonLat2) {
            var lonDiff = lonLat2.lon - lonLat1.lon;
            var latDiff = lonLat2.lat - lonlat1.lat;
            return new OpenLayers.LonLat(lonDiff, latDiff);
        },
        lonLatDifference : function(lonLat1, lonLat2) {
            var lonDiff = lonLat2.lon - lonLat1.lon;
            var latDiff = lonLat2.lat - lonLat1.lat;
            return new OpenLayers.LonLat(lonDiff, latDiff);
        },
        transformPosition : function(longitude, latitude){
            return transformPosition(longitude, latitude);
        },
        addToLonLat : function(lonLat, lonLatDiff) {
            return new OpenLayers.LonLat(lonLat.lon + lonLatDiff.lon, lonLat.lat + lonLatDiff.lat);
        },
        isWithinBorders : function(longitude, latitude) {
            var lonLat = transformPosition(longitude, latitude);
            return map.getExtent().containsLonLat(lonLat);
        },
        internalMap : map,
        createClickControl : function(handler) {
            var Control = OpenLayers.Class(OpenLayers.Control, {
                defaultHandlerOptions : {
                    'single' : true,
                    'double' : false,
                    'pixelTolerance' : 0,
                    'stopSingle' : false,
                    'stopDouble' : false
                },

                initialize : function(options) {
                    this.handlerOptions = OpenLayers.Util.extend({}, this.defaultHandlerOptions);
                    OpenLayers.Control.prototype.initialize.apply(this, arguments);
                    this.handler = new OpenLayers.Handler.Click(this, {
                        'click' : this.trigger
                    }, this.handlerOptions);
                },

                trigger : function(e) {
                    var lonlat1 = this.map.getLonLatFromPixel(e.xy);
                    var lonlat = new OpenLayers.LonLat(lonlat1.lon, lonlat1.lat).transform(this.map
                            .getProjectionObject(), new OpenLayers.Projection("EPSG:4326"));
                    return handler(lonlat);
                }
            });
            return new Control();
        },
        isInitialized: function () {
            return initialized;
        }

    };

    // Create one select control for all layers. This the only way to enable
    // selection of features in different active layers being shown at the
    // same
    // time. Also see below link.
    // http://gis.stackexchange.com/questions/13886/how-to-select-multiple-features-from-multiple-layers-in-openlayers
    // and http://openlayers.org/dev/examples/select-feature-multilayer.html

    // Reacting on select / unselect is performed like:
    // someLayer.events.on({
    // "featureselected" : function(e){ ... },
    // "featureunselected" : function(e){ ... }
    // });

    // add select control, registered for no layers
    // Layers will be registered on control through usage of
    // addSelectableLayer

    map.events.includeXY = true;

    /*
     * map.addControl(hoverControl); hoverControl.activate();
     */

    map.addControl(selectControl);
    selectControl.activate();

    map.events.register("mousemove", map, function(e) {
        var position = this.events.getMousePosition(e);
        pixel = new OpenLayers.Pixel(position.x, position.y);
        var lonLat = map.getLonLatFromPixel(pixel);
        if (lonLat) {
            // from Spherical Mercator Projection to WGS 1984
            lonLat.transform(map.getProjectionObject(), new OpenLayers.Projection("EPSG:4326"));
            $('#coords').html(formatLatitude(lonLat.lat) + ', ' + formatLongitude(lonLat.lon));
        }
    });

    /**
     * Transforms a position to a position that can be used by OpenLayers. The
     * transformation uses OpenLayers.Projection("EPSG:4326").
     * 
     * @param lon
     *            The longitude of the position to transform
     * @param lat
     *            The latitude of the position to transform
     * @returns The transformed position as a OpenLayers.LonLat instance.
     */
    function transformPosition(lon, lat) {
        return new OpenLayers.LonLat(lon, lat).transform(new OpenLayers.Projection("EPSG:4326"), map
                .getProjectionObject());
    }

    /**
     * Method for saving the current view into a cookie.
     */
    function saveViewCookie() {
        var center = map.getCenter();
        setCookie("dma-ais-zoom-" + embryo.authentication.userName, map.zoom, 30);
        var lonlat = new OpenLayers.LonLat(map.center.lon, map.center.lat).transform(map.getProjectionObject(), // from
            // Spherical Mercator Projection
        new OpenLayers.Projection("EPSG:4326") // to WGS 1984
        );
        setCookie("dma-ais-lat-" + embryo.authentication.userName, lonlat.lat, 30);
        setCookie("dma-ais-lon-" + embryo.authentication.userName, lonlat.lon, 30);
    }

    /**
     * Get settings from cookies
     */
    function loadViewCookie() {
        var zoom = getCookie("dma-ais-zoom-" + embryo.authentication.userName);
        var lat = getCookie("dma-ais-lat-" + embryo.authentication.userName);
        var lon = getCookie("dma-ais-lon-" + embryo.authentication.userName);
        if (zoom && lat && lon) {
            embryo.map.setCenter(parseFloat(lon), parseFloat(lat), parseInt(zoom));
        } else {
            embryo.map.setCenter(-65, 70, 3);
        }
    }

    map.events.register("movestart", map, function() {
        embryo.eventbus.fireEvent(embryo.eventbus.UnHighLightEvent(null));
    });

    map.events.register("moveend", map, saveViewCookie);

    // Position zoom control to the right

    function moveZoomControl() {
        $(".olControlZoom").css("left", $("#map").width() - 40);
        $(".olControlZoom").css("top", 50);
    }

    $(window).resize(moveZoomControl);
    moveZoomControl();

    function setupOsmMap() {
        var osm = new OpenLayers.Layer.OSM("OSM", embryo.authentication.osm, {
            'layers' : 'basic',
            'isBaseLayer' : true
        });
        map.addLayer(osm);
        map.setBaseLayer(osm);
    }

    function setupVectorMap(name) {
        var e1 = new OpenLayers.Bounds(-179, -89, 179, 89);

        e1.transform(new OpenLayers.Projection("EPSG:4326"), embryo.projection);

        var blankLayer = new OpenLayers.Layer("Blank", {
            isBaseLayer : true,
            numZoomLevels : 19,
            projection : map.projection,
            maxExtent : e1
        });

        map.addLayer(blankLayer);
        map.setBaseLayer(blankLayer);

        var layer = new WorldMapLayer();

        addLayerToMap("world", layer, embryo.map);
        embryo.shape.service.staticShapes("static." + name, {
            exponent : 2,
            delta : true
        }, function(data) {
            layer.draw(data);
        }, function(error, status) {
            embryo.logger.log("unhandled error", error);
        });
    }

    function removeMapLayers() {
        $.each([ "Blank", "World", "OSM" ], function(k, v) {
            $.each(map.getLayersByName(v), function(k, v) {
                map.removeLayer(v);
            });
        });
    }

    function setupBaseMap() {
        removeMapLayers();

        switch (embryo.baseMap) {
        case "osm":
            setupOsmMap();
            break;
        default:
            setupVectorMap(embryo.baseMap);
            break;
        }
    }

    function setupChromeAcceleration() {
        if (browser.isChrome() && parseFloat(browser.chromeVersion()) > 27) {
            var accelerate = getCookie("dma-ais-accelerate-" + embryo.authentication.userName);
            $("#map").removeClass("noaccelerate");
            if (accelerate != "true") {
                $("#map").addClass("noaccelerate");
            }
        }
    }

    embryo.authenticated(function() {
        var cookieMapName = getCookie("dma-ais-map-" + embryo.authentication.userName);
        if (cookieMapName)
            embryo.baseMap = cookieMapName;

        setupChromeAcceleration();
        setupBaseMap();

        loadViewCookie();

        // Event sequence has been created, because it is important that all layers are added before drawing on the map.
        // OpenLayers will otherwise draw e.g. routes incorrectly

        // Firing this event should provoke adding of layers to the map
        embryo.eventbus.fireEvent(embryo.eventbus.PostLayerInitialize());

        // Firing this event should enable other components to start drawing on the map.
        embryo.eventbus.fireEvent(embryo.eventbus.MapInitialized());

        initialized = true;
    });

    embryo.groupChanged(function(e) {
        selectControl.unselectAll();
        if (selectLayerByGroup[e.groupId]) {
            selectControl.setLayer(selectLayerByGroup[e.groupId]);
            for ( var i in controlsByGroup) {
                if (i == e.groupId) {
                    for ( var j in controlsByGroup[i])
                        controlsByGroup[i][j].activate();
                } else {
                    for ( var j in controlsByGroup[i])
                        controlsByGroup[i][j].deactivate();
                }
            }
        }
    });

    function registerEvents() {
        $("#switchBaseMapDialog .btn-primary").off();
        $("#switchBaseMapDialog .btn-primary").on('click', function(e) {
            var newMap;

            if ($("#bsOpenStreetMap").prop("checked"))
                newMap = "osm";
            if ($("#bsSimpleVectorMap").prop("checked"))
                newMap = "world_merc";

            $("#switchBaseMapDialog").modal("hide");

            if (browser.isChrome() && parseFloat(browser.chromeVersion()) > 27) {
                $("#accelerate").css("display", "none");
                var accelerate = $("#accelerate input").prop("checked");
                setCookie("dma-ais-accelerate-" + embryo.authentication.userName, accelerate, 30);
                setupChromeAcceleration(accelerate);
            }

            if (newMap != embryo.baseMap) {
                embryo.baseMap = newMap;
                setupBaseMap(embryo.baseMap);
                setCookie("dma-ais-map-" + embryo.authentication.userName, embryo.baseMap, 30);
            }
        });
    }

    var mapModule = angular.module('embryo.map', []);
    mapModule.directive('eMapInitialized', [function () {
        return {
            restrict: 'A',
            replace: true,
            template: embryo.templateFn('isInitialized()'),
            link: function (scope) {
                scope.isInitialized = embryo.map.isInitialized;
            }
        };

    }]);

    var zoomModule = angular.module('embryo.zoom', ['embryo.authentication']);

    zoomModule.controller('ZoomController', ['$scope', 'Subject', function ($scope, Subject) {
        var control = new OpenLayers.Control.Zoom({
            zoomInId : 'dmaZoomIn',
            zoomOutId : 'dmaZoomOut'
        });
        embryo.map.internalMap.addControl(control);

        $scope.zoomAll = function() {
            embryo.map.setCenter(-65, 70, 3);
        };

        $scope.switchBaseMap = function() {
            $("#bsOpenStreetMap").prop("checked", embryo.baseMap == "osm");
            $("#bsSimpleVectorMap").prop("checked", embryo.baseMap == "world_merc");

            if (browser.isChrome() && parseFloat(browser.chromeVersion()) > 27) {
                accelerate = getCookie("dma-ais-accelerate-" + embryo.authentication.userName);
                $("#accelerate input").prop("checked", accelerate == "true");
                $("#accelerate").css("display", "block");
            }
            $("#switchBaseMapDialog").modal("show");

            registerEvents();
        };

        $scope.zoomClasses = function() {
            var zoom2YourVessel = Subject.authorize('Sailor');
            return zoom2YourVessel ? 'e-btn-tall' : 'e-btn-low';
        };

        $scope.$on('$destroy', function() {
            embryo.map.internalMap.removeControl(control);
        });
    } ]);
    
});
