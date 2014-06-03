function VesselLayer() {
    function imageForVessel(vo) {
        var colorName;

        switch (vo.type) {
            case "0" : colorName = "blue"; break;
            case "1" : colorName = "gray"; break;
            case "2" : colorName = "green"; break;
            case "3" : colorName = "orange"; break;
            case "4" : colorName = "purple"; break;
            case "5" : colorName = "red"; break;
            case "6" : colorName = "turquoise"; break;
            case "7" : colorName = "yellow"; break;
            default : colorName = "unknown";
        }

        if (vo.moored){
            return {
                name: "vessel_" + colorName + "_moored.png",
                width: 12,
                height: 12,
                xOffset: -6,
                yOffset: -6
            };
        } else {
            return {
                name: "vessel_" + colorName + ".png",
                width: 20,
                height: 10,
                xOffset: -10,
                yOffset: -5
            };
        }
    }

    this.init = function() {
        this.zoomLevels = [4, 6];

        var that = this;

	    var noTransparency = browser.isChrome() && parseFloat(browser.chromeVersion())== 34;
        this.context = {
            transparency: function() {
        		if(noTransparency){
        		    return 1.0;	
        		}
                return that.active ? 0.8 : 0.4;
            },
            vesselSize: function() {
                return [0.5, 0.75, 1.0][that.zoomLevel];
            }
        };

        this.layers.vessel = new OpenLayers.Layer.Vector("Vessels", {
            styleMap : new OpenLayers.StyleMap({
                "default" : new OpenLayers.Style({
                    externalGraphic : "${image}",
                    graphicWidth : "${imageWidth}",
                    graphicHeight : "${imageHeight}",
                    graphicYOffset : "${imageYOffset}",
                    graphicXOffset : "${imageXOffset}",
                    rotation : "${angle}",
                    graphicOpacity : "${transparency}"
                }, { context: this.context }),
                "select" : new OpenLayers.Style({
                    cursor : "crosshair",
                    externalGraphic : "${image}"
                }, { context: this.context })
            }, { context: this.context })
        });

        this.layers.marker=   new OpenLayers.Layer.Vector("Markers", {
            styleMap : new OpenLayers.StyleMap({
                "default" : new OpenLayers.Style({
                    externalGraphic : "${image}",
                    graphicWidth : "${imageWidth}",
                    graphicHeight : "${imageHeight}",
                    graphicYOffset : "${imageYOffset}",
                    graphicXOffset : "${imageXOffset}",
                    rotation : "${angle}",
                    graphicOpacity : "${transparency}"
                }, { context: this.context }),
                "select" : new OpenLayers.Style({
                    cursor : "crosshair",
                    externalGraphic : "${image}"
                }, { context: this.context })
            })
        });

        this.layers.selection = new OpenLayers.Layer.Vector("Selection", {
            styleMap : new OpenLayers.StyleMap({
                "default" : new OpenLayers.Style({
                    externalGraphic : "${image}",
                    graphicWidth : "${imageWidth}",
                    graphicHeight : "${imageHeight}",
                    graphicYOffset : "${imageYOffset}",
                    graphicXOffset : "${imageXOffset}",
                    graphicOpacity : "${transparency}",
                    rotation : "${angle}"
                }, { context: this.context }),
                "select" : new OpenLayers.Style({
                    cursor : "crosshair",
                    externalGraphic : "${image}"
                }, { context: this.context })
            }, { context: this.context })
        });

        this.layers.icon = new OpenLayers.Layer.Vector("Icons", {
            styleMap : new OpenLayers.StyleMap({
                "default" : new OpenLayers.Style({
                    externalGraphic : "${image}",
                    graphicWidth : "${imageWidth}",
                    graphicHeight : "${imageHeight}",
                    graphicYOffset : "${imageYOffset}",
                    graphicXOffset : "${imageXOffset}",
                    graphicOpacity : "${transparency}" /*,
                    fontColor: "#000",
                    fontSize: "10px",
                    fontOpacity: 1,
                    fontFamily: "Courier New, monospace",
                    fontWeight: "bold",
                    label: "${label}",
                    labelOutlineWidth : 0,
                    labelYOffset: 16 */
                }, { context: this.context })
            })
        });

        this.selectableLayers = [this.layers.vessel];
        this.selectableAttribute = "vessel.mmsi";
        this.selectedId = null;

        this.select(function(id) {
            that.selectedId = id;
            if(id) {
                RouteLayerSingleton.getInstance().clear();
                ScheduleLayerSingleton.getInstance().clear();
            }
            that.layers.selection.removeAllFeatures();

            $.each(that.layers.vessel.features, function (k,v) {
                if (v.attributes.vessel.mmsi == id) {
                    that.layers.selection.addFeatures([
                        new OpenLayers.Feature.Vector(
                             that.map.createPoint(v.attributes.vessel.x, v.attributes.vessel.y), {
                                id : -1,
                                angle : v.attributes.vessel.angle - 90,
                                opacity : 1,
                                image : "img/selection.png",
                                imageWidth : function() { return 32 * that.context.vesselSize(); },
                                imageHeight : function() { return 32 * that.context.vesselSize(); },
                                imageYOffset : function() { return -16 * that.context.vesselSize(); },
                                imageXOffset : function() { return -16 * that.context.vesselSize(); },
                                type : "selection"
                            })
                    ]);
                }
            });

            that.layers.selection.redraw();
        });
    };

    this.draw = function(vessels) {
        var selectedFeature = null;
        var that = this;

        $.each(this.layers.vessel.features, function(k, v) {
            if (v.attributes.vessel.mmsi == that.selectedId) {
                selectedFeature = v;
            }
        });

        var vesselLayer = this.layers.vessel;
        var vesselFeatures = [];
        var context = this.context;

        $.each(vessels, function(key, value) {
            var image = imageForVessel(value);
            var attr = {
                id : value.id,
                angle : value.angle - 90,
                image : "img/" + image.name,
                imageWidth : function() { return image.width * context.vesselSize(); },
                imageHeight : function() { return image.height * context.vesselSize(); },
                imageYOffset : function() { return image.yOffset * context.vesselSize(); },
                imageXOffset : function() { return image.xOffset * context.vesselSize(); },
                type : "vessel",
                vessel : value
            };

            var geom = embryo.map.createPoint(value.x, value.y);

            var feature = new OpenLayers.Feature.Vector(geom, attr);

            if (selectedFeature && selectedFeature.attributes.vessel.mmsi == attr.vessel.mmsi) {

            } else {
                if (value.type != null) vesselFeatures.push(feature);
            }
        });

        var arr = vesselLayer.features.slice();
        var idx = $.inArray(selectedFeature, arr);
        if (idx != -1) arr.splice(idx, 1);
        vesselLayer.addFeatures(vesselFeatures);
        vesselLayer.destroyFeatures(arr);
        vesselLayer.redraw();

        var markerLayer = this.layers.marker;

        markerLayer.removeAllFeatures();

        $.each(vessels, function(k, v) {
            if (that.markedVesselId == v.mmsi && (v.type != null)) {
                markerLayer.addFeatures([
                    new OpenLayers.Feature.Vector(
                        embryo.map.createPoint(v.x, v.y), {
                            id : -1,
                            angle : 0,
                            opacity : "{transparency}",
                            image : "img/green_marker.png",
                            imageWidth : function() { return 32 * context.vesselSize(); },
                            imageHeight : function() { return 32 * context.vesselSize(); },
                            imageYOffset : function() { return -16 * context.vesselSize(); },
                            imageXOffset : function() { return -16 * context.vesselSize(); },
                            type : "marker"
                        })
                ]);

            }
        });

        markerLayer.redraw();

        var iconLayer = this.layers.icon;

        iconLayer.removeAllFeatures();

        $.each(vessels, function(k, v) {
            if ((v.inAW) && (v.type != null)) {
                iconLayer.addFeatures([
                    new OpenLayers.Feature.Vector(
                        embryo.map.createPoint(v.x, v.y), {
                            id : -1,
                            angle : 0,
                            image : "img/aw-logo.png",
                            imageWidth : function() { return 32 * context.vesselSize(); },
                            imageHeight : function() { return 16 * context.vesselSize(); },
                            imageYOffset : function() { return 8 * context.vesselSize(); },
                            imageXOffset : function() { return -16 * context.vesselSize(); },
                            type : "marker"
                        })
                ]);
            }
        });

        iconLayer.redraw();
    };
}

VesselLayer.prototype = new EmbryoLayer();
