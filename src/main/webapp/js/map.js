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

$(function() {
    embryo.map = {
        add: function(d) {
            if (d.layer) {
                embryo.mapPanel.map.addLayer(d.layer);
                if (d.select) embryo.mapPanel.add2SelectFeatureCtrl(d.layer);
            }
            if (d.control) {
                embryo.mapPanel.map.addControl(d.control);
            }
        },
        remove: function(id) {
            var layers = embryo.mapPanel.map.getLayersByName(id);
            
            for (var k in layers) {
                embryo.mapPanel.map.removeLayer(layers[k]);
            }
        },
        createPoint: function(longitude, latitude) {
            return new OpenLayers.Geometry.Point(longitude, latitude)
                .transform(new OpenLayers.Projection("EPSG:4326"), embryo.mapPanel.map.getProjectionObject());
        }
    };

    embryo.mapPanel = {
        add2SelectFeatureCtrl : function(layer) {
            var layers = this.selectControl.layers;
            layers.push(layer);
            this.selectControl.setLayer(layers);
        },
        
        add2HoverFeatureCtrl : function(layer) {
            /*var layers = this.hoverControl.layers;
            layers.push(layer);
            this.hoverControl.setLayer(layers);*/
        },

        map: new OpenLayers.Map({
            div : "map",
            fractionalZoom : false
        }),

        selectControl: new OpenLayers.Control.SelectFeature([], {
            clickout : true,
            toggle : true,
            id : 'ClickCtrl'
        }),

        hoverControl: new OpenLayers.Control.SelectFeature([], {
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
        })
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
    
    embryo.mapPanel.map.events.includeXY = true;
    
    embryo.mapPanel.map.addControl(embryo.mapPanel.hoverControl);
    embryo.mapPanel.hoverControl.activate();
    
    embryo.mapPanel.map.addControl(embryo.mapPanel.selectControl);
    embryo.mapPanel.selectControl.activate();
    
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
            embryo.mapPanel.map.getProjectionObject()
        );
    }

    /**
     * Method for saving the current view into a cookie.
     */
    function saveViewCookie() {
        var center = embryo.mapPanel.map.getCenter();
        setCookie("dma-ais-zoom", embryo.mapPanel.map.zoom, 30);
        var lonlat = new OpenLayers.LonLat(embryo.mapPanel.map.center.lon, embryo.mapPanel.map.center.lat).
            transform(
                embryo.mapPanel.map.getProjectionObject(), // from Spherical Mercator Projection
                new OpenLayers.Projection("EPSG:4326") // to WGS 1984
            );
        setCookie("dma-ais-lat", lonlat.lat, 30);
        setCookie("dma-ais-lon", lonlat.lon, 30);

        // console.log("zoom: " + embryo.mapPanel.map.zoom + " " + lonlat.lat + " " + lonlat.lon);
    }
    
    /**
     * Get settings from cookies
     */
    function loadViewCookie() {
        var zoom = getCookie("dma-ais-zoom");
        var lat = getCookie("dma-ais-lat");
        var lon = getCookie("dma-ais-lon");
        if (zoom && lat && lon) {
            embryo.mapPanel.map.setCenter(transformPosition(parseFloat(lon), parseFloat(lat)), parseInt(zoom));
        }
    }
    
    embryo.mapPanel.map.events.register("movestart", embryo.mapPanel.map, function() {
        embryo.eventbus.fireEvent(embryo.eventbus.UnHighLightEvent(null));
    });

    embryo.mapPanel.map.events.register("moveend", embryo.mapPanel.map, saveViewCookie);
    
    // Position zoom control to the right
    
    function moveZoomControl() {
        $(".olControlZoom").css("left", $(window).width()-40);
    }

    $(window).resize(moveZoomControl);
    moveZoomControl();
    
    embryo.authenticated(function() {
        embryo.mapPanel.map.projection = new OpenLayers.Projection(embryo.authentication.projection);
        
        var osm = new OpenLayers.Layer.OSM(
            "OSM",
            "http://a.tile.openstreetmap.org/${z}/${x}/${y}.png", {
                'layers' : 'basic',
                'isBaseLayer' : true
            }
        );

        embryo.mapPanel.map.addLayer(osm);

        loadViewCookie();
    });

    $("#zoomAll").click(function() {
        embryo.mapPanel.map.setCenter(transformPosition(-70, 72), 3);
    });
});

