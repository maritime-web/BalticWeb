embryo.eventbus.MapInitialized = function() {
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

$(function() {
    var selectLayerByGroup = { };

    var map = new OpenLayers.Map({
        div : "map",
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

    embryo.map = {
        add: function(d) {
            if (d.layer) {
                map.addLayer(d.layer);

                if (selectLayerByGroup[d.group] == null)
                    selectLayerByGroup[d.group] = [];

                if (d.select)
                    selectLayerByGroup[d.group].push(d.layer);

            }
            if (d.control) {
                map.addControl(d.control);
            }
        },
        createPoint: function(longitude, latitude) {
            return new OpenLayers.Geometry.Point(longitude, latitude)
                .transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject());
        },
        select: function(feature) {
            selectControl.unselectAll();
            selectControl.select(feature);
        },
        zoomToExtent: function(layers) {
            var extent = new OpenLayers.Bounds();
            
            extent.bottom= 9999999;
            extent.left= 9999999;
            extent.right= -9999999;
            extent.top= -9999999;
            
            for (var i in layers) {
                var e = layers[i].getDataExtent();
                extent.bottom = Math.min(extent.bottom, e.bottom);
                extent.left = Math.min(extent.left, e.left);
                extent.top = Math.max(extent.top, e.top);
                extent.right = Math.max(extent.right, e.right);
            }
            
            map.zoomToExtent(extent);
        },
        internalMap: map
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
    
    map.addControl(hoverControl);
    hoverControl.activate();
    
    map.addControl(selectControl);
    selectControl.activate();
    
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
        return new OpenLayers.LonLat(lon, lat).transform(
            new OpenLayers.Projection("EPSG:4326"),
            map.getProjectionObject()
        );
    }

    /**
     * Method for saving the current view into a cookie.
     */
    function saveViewCookie() {
        var center = map.getCenter();
        setCookie("dma-ais-zoom", map.zoom, 30);
        var lonlat = new OpenLayers.LonLat(map.center.lon, map.center.lat).
            transform(
                map.getProjectionObject(), // from Spherical Mercator Projection
                new OpenLayers.Projection("EPSG:4326") // to WGS 1984
            );
        setCookie("dma-ais-lat", lonlat.lat, 30);
        setCookie("dma-ais-lon", lonlat.lon, 30);
    }
    
    /**
     * Get settings from cookies
     */
    function loadViewCookie() {
        var zoom = getCookie("dma-ais-zoom");
        var lat = getCookie("dma-ais-lat");
        var lon = getCookie("dma-ais-lon");
        if (zoom && lat && lon) {
            map.setCenter(transformPosition(parseFloat(lon), parseFloat(lat)), parseInt(zoom));
        }
    }
    
    map.events.register("movestart", map, function() {
        embryo.eventbus.fireEvent(embryo.eventbus.UnHighLightEvent(null));
    });

    map.events.register("moveend", map, saveViewCookie);
    
    // Position zoom control to the right
    
    function moveZoomControl() {
        $(".olControlZoom").css("left", $(window).width()-40);
    }

    $(window).resize(moveZoomControl);
    moveZoomControl();
    
    embryo.authenticated(function() {
        map.projection = new OpenLayers.Projection(embryo.authentication.projection);
        
        var osm = new OpenLayers.Layer.OSM(
            "OSM",
            "http://a.tile.openstreetmap.org/${z}/${x}/${y}.png", {
                'layers' : 'basic',
                'isBaseLayer' : true
            }
        );

        map.addLayer(osm);

        loadViewCookie();

        embryo.eventbus.fireEvent(embryo.eventbus.MapInitialized());
    });

    $("#zoomAll").click(function() {
        map.setCenter(transformPosition(-70, 72), 3);
    });

    embryo.groupChanged(function(e) {
        selectControl.unselectAll();
        selectControl.setLayer(selectLayerByGroup[e.groupId]);
    });
});

