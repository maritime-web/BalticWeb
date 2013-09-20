embryo.vessel = {};

embryo.eventbus.VesselSelectedEvent = function(id) {
    var event = jQuery.Event("VesselSelectedEvent");
    event.vesselId = id;
    return event;
};

embryo.eventbus.VesselUnselectedEvent = function() {
    var event = jQuery.Event("VesselUnselectedEvent");
    return event;
};

embryo.eventbus.registerShorthand(embryo.eventbus.VesselSelectedEvent, "vesselSelected");
embryo.eventbus.registerShorthand(embryo.eventbus.VesselUnselectedEvent, "vesselUnselected");

/**
 * Vessel object
 * 
 * @param vesselId vessels unique id
 * @param vessel JSON vessel data
 * @param markerScale
 *            Scale of the marker
 * @returns vessel object
 */
function Vessel(vesselId, vessel, markerScale) {
    this.id = vesselId;
    this.lat = vessel[1];
    this.lon = vessel[2];
    
    // Set color and vessel type
    this.color = vessel[4];
    switch(vessel[4]){
    case "0" : this.colorName = "blue"; break;
    case "1" : this.colorName = "gray"; break;
    case "2" : this.colorName = "green"; break;
    case "3" : this.colorName = "orange"; break;
    case "4" : this.colorName = "purple"; break;
    case "5" : this.colorName = "red"; break;
    case "6" : this.colorName = "turquoise"; break;
    case "7" : this.colorName = "yellow"; break;
    default : this.colorName = "unknown";
    }
    
    // Moored or not
    if (vessel[5] == 1) {
        this.moored = true;
    }
    
    // Set image properties
    if (this.moored){
        this.image = "vessel_" + this.colorName + "_moored.png";
        this.imageWidth = 12;
        this.imageHeight = 12;
    } else {
        this.image = "vessel_" + this.colorName + ".png";
        this.imageWidth = 20;
        this.imageHeight = 10;
    }
    this.imageYOffset = -this.imageHeight/2;
    this.imageXOffset = -this.imageWidth/2;
    
    this.degree = vessel[0];
    
    // Not anonymous
    if (vessel.length > 6){
        this.mmsi = vessel[6];
        this.vesselName = vessel[7];
        this.imo = vessel[9];
    }
}
    
$(function() {
    var vesselSize = 1;
    var vesselTransparency = 0.3;
    var vessels = [];
    var lastRequestId = 0;

    var vesselLayer;
    var selectionLayer;
    var markerLayer;
    
    var selectedFeature;

    embryo.vessel.lookupVessel = function(id) {
        for (var i in vessels) {
            if (vessels[i].id == id) return vessels[i];
        }
        return null;
    }

    embryo.vessel.goToVesselLocation = function (vessel) {
	var center = new OpenLayers.LonLat(vessel.lon, vessel.lat).transform(
	    new OpenLayers.Projection("EPSG:4326"), embryo.map.internalMap.getProjectionObject());
        embryo.map.internalMap.setCenter(center, focusZoom);
    }

    embryo.vessel.selectVessel = function (vessel) {
        var feature = null;
        for (var i in vesselLayer.features) {
            if (vesselLayer.features[i].attributes.vessel.id == vessel.id) feature = vesselLayer.features[i];
        }
        selectedFeature = feature;
        redrawSelection();
        embryo.eventbus.fireEvent(embryo.eventbus.VesselSelectedEvent(feature.attributes.id));
    }

    /**
     * Loads and draws all vessels in the view.
     */
    function loadVesselList() {
        var messageId = embryo.messagePanel.show( { text: "Loading vessels ..." })
        
        $.ajax({
            url: embryo.baseUrl+listUrl,
            data: { 
                requestId: lastRequestId
            },
            success: function (result) {
                embryo.messagePanel.replace(messageId, { text: result.vesselsInWorld + " vessels loaded.", type: "success" })

                if (result.requestId != lastRequestId) return;
                
                vessels = [];

                for (var i in result.vesselList.vessels) {
                    vessels.push(new Vessel(i, result.vesselList.vessels[i], 1));
                }
                
                drawVessels();
            },
            error: function(data) {
                embryo.messagePanel.replace(messageId, { text: "Server returned error code: " + data.status + " loading vessels.", type: "error" });
                console.log("Server returned error code: " + data.status + " loading vessels.");
            }
        });
    }
    
    /**
     * Draws all known vessels using vector points styled to show images. Vessels
     * are drawn based on their color, angle and whether they are moored on not.
     */
    function drawVessels() {
        var vesselFeatures = [];
        
        // Iterate through vessels where value refers to each vessel.
        $.each(vessels, function(key, value) {
            var attr = {
                id : value.id,
                angle : value.degree - 90,
                opacity : function() { return vesselTransparency },
                image : "img/" + value.image,
                imageWidth : function() { return value.imageWidth * vesselSize },
                imageHeight : function() { return value.imageHeight * vesselSize },
                imageYOffset : function() { return value.imageYOffset * vesselSize },
                imageXOffset : function() { return value.imageXOffset * vesselSize },
                type : "vessel",
                vessel : value
            }

            // var geom = new OpenLayers.Geometry.Point(value.lon, value.lat)
            //    .transform(new OpenLayers.Projection("EPSG:4326"), embryo.mapPanel.map.getProjectionObject());

            var geom = embryo.map.createPoint(value.lon, value.lat);

            var feature = new OpenLayers.Feature.Vector(geom, attr);

            if (selectedFeature && selectedFeature.attributes.vessel.id == attr.vessel.id) {
                // vesselFeatures.push(selectedFeature);
            } else {
                vesselFeatures.push(feature);
            }
        });

	// Remove old features except selected feature
	var arr = vesselLayer.features.slice();
	var idx = arr.indexOf(selectedFeature);
	if (idx != -1) arr.splice(idx, 1);
	vesselLayer.addFeatures(vesselFeatures);
	vesselLayer.destroyFeatures(arr);

        vesselLayer.redraw();

        redrawSelection();
        redrawMarker();
    }

    /**
     * Redraws all features in vessel layer and selection layer. Features are
     * vessels.
     */
    function redrawSelection() {
        selectionLayer.removeAllFeatures();
        
        // Set search result in focus
        if (selectedFeature) {
            selectionLayer.addFeatures([
                new OpenLayers.Feature.Vector(
                    new OpenLayers.Geometry.Point(selectedFeature.geometry.x, selectedFeature.geometry.y), {
                        id : -1,
                        angle : selectedFeature.attributes.angle - 90,
                        opacity : 1,
                        image : "img/selection.png",
                        imageWidth : 32,
                        imageHeight : 32,
                        imageYOffset : -16,
                        imageXOffset : -16,
                        type : "selection"
                    })
            ]);
        }
        
        selectionLayer.redraw();
    }
    
    function redrawMarker() {
        markerLayer.removeAllFeatures();

        if (embryo.vessel.markedVesselId) {
            var markedVessel = embryo.vessel.lookupVessel(embryo.vessel.markedVesselId);
            var geom = embryo.map.createPoint(markedVessel.lon, markedVessel.lat);

            markerLayer.addFeatures([
                new OpenLayers.Feature.Vector(
                    new OpenLayers.Geometry.Point(geom.x, geom.y), {
                        id : -1,
                        angle : 0,
                        opacity : 1,
                        image : "img/green_marker.png",
                        imageWidth : 32 * vesselSize,
                        imageHeight : 32 * vesselSize,
                        imageYOffset : -16 * vesselSize,
                        imageXOffset : -16 * vesselSize,
                        type : "marker"
                    })
            ]);
        }
        
        markerLayer.redraw();
    }
        
    // Create vector layer with a stylemap for vessels
    vesselLayer = new OpenLayers.Layer.Vector("Vessels", {
        styleMap : new OpenLayers.StyleMap({
            "default" : {
                externalGraphic : "${image}",
                graphicWidth : "${imageWidth}",
                graphicHeight : "${imageHeight}",
                graphicYOffset : "${imageYOffset}",
                graphicXOffset : "${imageXOffset}",
                rotation : "${angle}",
                graphicOpacity : "${opacity}"
            },
            "select" : {
                cursor : "crosshair",
                externalGraphic : "${image}"
            }
        })
    });
    
    embryo.map.add({
        group: "vessel",
        layer: vesselLayer,
        select: true,
        hoved: true
    });

    // Create vector layer with a stylemap for the selection image
    markerLayer = new OpenLayers.Layer.Vector("Markers", {
        styleMap : new OpenLayers.StyleMap({
            "default" : {
                externalGraphic : "${image}",
                graphicWidth : "${imageWidth}",
                graphicHeight : "${imageHeight}",
                graphicYOffset : "${imageYOffset}",
                graphicXOffset : "${imageXOffset}",
                rotation : "${angle}"
            },
            "select" : {
                cursor : "crosshair",
                externalGraphic : "${image}"
            }
        })
    });
    
    embryo.map.add({ 
        group: "vessel",
        layer: markerLayer 
    });
    
    // Create vector layer with a stylemap for the selection image
    selectionLayer = new OpenLayers.Layer.Vector("Selection", {
        styleMap : new OpenLayers.StyleMap({
            "default" : {
                externalGraphic : "${image}",
                graphicWidth : "${imageWidth}",
                graphicHeight : "${imageHeight}",
                graphicYOffset : "${imageYOffset}",
                graphicXOffset : "${imageXOffset}",
                rotation : "${angle}"
            },
            "select" : {
                cursor : "crosshair",
                externalGraphic : "${image}"
            }
        })
    });

    embryo.map.add({
        group: "vessel",
        layer: selectionLayer 
    });
        
    function onSelect(event) {
        var feature = event.feature;
        if (selectedFeature != null) onUnselect();
        selectedFeature = feature;
        redrawSelection();
        embryo.eventbus.fireEvent(embryo.eventbus.VesselSelectedEvent(feature.attributes.id));
    };
    
    function onUnselect(event) {
        selectedFeature = null;
        redrawSelection();
        embryo.eventbus.fireEvent(embryo.eventbus.VesselUnselectedEvent());
    }
    
    vesselLayer.events.on({
        featureselected : onSelect,
        featureunselected : onUnselect
    });
    
    embryo.mapInitialized(function() {
        setInterval(loadVesselList, loadFrequence);
        loadVesselList();
    });

    embryo.groupChanged(function(e) {
        if (e.groupId == "vessel") {
            vesselTransparency = 1;
            vesselLayer.redraw();
        } else {
            vesselTransparency = 0.3;
            vesselLayer.redraw();
        }
    });

    embryo.hover(function(e) {
        if (e.feature.attributes.vessel) {
            var id = "name_"+e.feature.attributes.id;
            $.getJSON(embryo.baseUrl + detailsUrl, {
                past_track : '1',
                id : e.feature.attributes.id
            }, function(result) {
                $("#"+id).html(
                    "<span class='label label-info'>"+result.name+"</span>"
                );
            });
            return "<div id="+id+" class=vesselNameBox></div>";
        } else {
            return null;
        }
    });

    embryo.map.internalMap.events.register("zoomend", embryo.map.internalMap, function() {
        var newVesselSize = 0.5;
        if (embryo.map.internalMap.zoom >= 4) newVesselSize = 0.75;
        if (embryo.map.internalMap.zoom >= 6) newVesselSize = 1;

        if (vesselSize != newVesselSize) {
            vesselSize = newVesselSize;
            vesselLayer.redraw();
        }
    });


});
