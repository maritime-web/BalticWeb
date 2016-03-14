function VesselLayer(conf) {
    this.config = conf;

    var createFeatures = null;

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
            default :
                colorName = "gray";
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

    /**
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
    function shadeBlend(p,c0,c1) {
        var n=p<0?p*-1:p,u=Math.round,w=parseInt;
        if(c0.length>7){
            var f=c0.split(","),t=(c1?c1:p<0?"rgb(0,0,0)":"rgb(255,255,255)").split(","),R=w(f[0].slice(4)),G=w(f[1]),B=w(f[2]);
            return "rgb("+(u((w(t[0].slice(4))-R)*n)+R)+","+(u((w(t[1])-G)*n)+G)+","+(u((w(t[2])-B)*n)+B)+")"
        }else{
            var f=w(c0.slice(1),16),t=w((c1?c1:p<0?"#000000":"#FFFFFF").slice(1),16),R1=f>>16,G1=f>>8&0x00FF,B1=f&0x0000FF;
            return "#"+(0x1000000+(u(((t>>16)-R1)*n)+R1)*0x10000+(u(((t>>8&0x00FF)-G1)*n)+G1)*0x100+(u(((t&0x0000FF)-B1)*n)+B1)).toString(16).slice(1)
        }
    }

    /**
     * Given a vessels type number betweeen 0-7, return a color in RGB hex format.
     * @param vo = a vessel
     * @returns a color in hex format i.e. #0000ff, #737373, #40e0d0
     *
     */
    function colorHexForVessel(vo) {
        var colorName;

        switch (vo.type) {
            case "0" : colorName = "#0000ff"; break; // blue
            case "1" : colorName = "#737373"; break; // grey
            case "2" : colorName = "#00cc00"; break; // green
            case "3" : colorName = "#ffa500"; break; // orange
            case "4" : colorName = "#800080"; break; // purple
            case "5" : colorName = "#ff0000"; break; // red
            case "6" : colorName = "#40e0d0"; break; // turquoise
            case "7" : colorName = "#ffff00"; break; // yellow
            default :
                colorName = "#737373"; // grey
        }
        return colorName;
    }

    this.init = function() {
        this.zoomLevels = [3, 4, 5, 6, 7, 8, 9, 10];

        var that = this;

	    var noTransparency = browser.isChrome() && parseFloat(browser.chromeVersion())== 34;
        this.context = {
            fontOpacity: function () {
                return that.active ? 0.8 : 0.3;
            },
            transparency: function (feature) {
                if (feature.attributes.type === "cluster") {
                    return that.active ? 0.4 : 0.2;
                }

        		if(noTransparency){
        		    return 1.0;	
        		}
                return that.active ? 0.8 : 0.4;
            },
            vesselSize: function() {
                return [0.5, 0.5, 0.55, 0.60, 0.65, 0.70, 0.8, 0.90, 1.0][that.zoomLevel];
            },
            label: function (feature) {
                return feature.attributes.label ? feature.attributes.label : '';
            },
            selectionSize: function (feature) {
                return 32 * that.context.vesselSize();
            },
            selectionOffSet: function (feature) {
                return -16 * that.context.vesselSize();
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
                    externalGraphic : "${image}",
                    graphicWidth : "${imageWidth}",
                    graphicHeight : "${imageHeight}",
                    graphicXOffset : "${imageXOffset}",
                    graphicYOffset : "${imageYOffset}",
                    graphicOpacity : "${transparency}",
                    backgroundGraphic: "img/selection.png",
                    backgroundWidth: "${selectionSize}",
                    backgroundHeight: "${selectionSize}",
                    backgroundXOffset: "${selectionOffSet}",
                    backgroundYOffset: "${selectionOffSet}",
                    rotation : "${angle}"
                }, { context: this.context })
            }, { context: this.context })
        });

        this.layers.unselectable = new OpenLayers.Layer.Vector("UnselectableFeatures", {
            styleMap: new OpenLayers.StyleMap({
                "default": new OpenLayers.Style({
                    externalGraphic : "${image}",
                    graphicWidth : "${imageWidth}",
                    graphicHeight : "${imageHeight}",
                    graphicYOffset : "${imageYOffset}",
                    graphicXOffset : "${imageXOffset}",
                    graphicOpacity: "${transparency}",
                    fillColor: "${fill}",
                    fillOpacity: "${transparency}",
                    strokeWidth: "1",
                    strokeColor: "#000000",
                    strokeOpacity: "0.2",
                    fontColor: "#000",
                    fontSize: "10px",
                    fontOpacity: "${fontOpacity}",
                    fontFamily: "Courier New, monospace",
                    fontWeight: "bold",
                    label: "${label}",
                    labelOutlineWidth: 0
                }, { context: this.context })
            })
        });

        this.selectableLayers = [this.layers.vessel];
        this.selectableAttribute = "vessel.mmsi";
        this.selectedId = null;

        this.select(function(id) {
            that.selectedId = id;
        });

        if (this.config && this.config.clusteringEnabled) {
            createFeatures = createClusterFeatures;
        } else {
            createFeatures = createVesselFeatures;
        }

    };

    this.zoom = function (level) {
        var newZoomLevel = 0;
        for (var i in this.zoomLevels) {
            if (level >= this.zoomLevels[i])
                newZoomLevel = parseFloat(i) + 1;
        }

        if (this.zoomLevel != newZoomLevel) {
            this.zoomLevel = newZoomLevel;
            this.draw2();
        }
    };


    function createMarkedFeature(vessel, context) {
        return new OpenLayers.Feature.Vector(
            embryo.map.createPoint(vessel.x, vessel.y), {
                id: -1,
                angle: 0,
                opacity: "{transparency}",
                image: "img/green_marker.png",
                imageWidth: function () {
                    return 32 * context.vesselSize();
                },
                imageHeight: function () {
                    return 32 * context.vesselSize();
                },
                imageYOffset: function () {
                    return -16 * context.vesselSize();
                },
                imageXOffset: function () {
                    return -16 * context.vesselSize();
                },
                type: "marker"
            });
    }

    function createVesselFeature(vessel, context) {
        var image = imageForVessel(vessel);
        var attr = {
            id: vessel.id,
            angle: vessel.angle - 90,
            image: "img/" + image.name,
            imageWidth: function () {
                return image.width * context.vesselSize();
            },
            imageHeight: function () {
                return image.height * context.vesselSize();
            },
            imageYOffset: function () {
                return image.yOffset * context.vesselSize();
            },
            imageXOffset: function () {
                return image.xOffset * context.vesselSize();
            },
            type: "vessel",
            vessel: vessel
        };
        var geom = embryo.map.createPoint(vessel.x, vessel.y);
        return new OpenLayers.Feature.Vector(geom, attr);
    }

    function createMinimalVesselFeature(vessel) {
        var colorHex = colorHexForVessel(vessel);
        var shadedColor = shadeBlend(-0.15,colorHex);


        var pointStyle = {fill:true, stroke: true, color: colorHex, fillColor: colorHex,
            strokeColor: shadedColor, strokeWidth: 1, pointRadius: 2};
        return new OpenLayers.Feature.Vector(
            embryo.map.createPoint(vessel.x, vessel.y), { description: 'info' }, pointStyle
        );

    }

    function createAwFeature(vessel, context) {
        return new OpenLayers.Feature.Vector(
            embryo.map.createPoint(vessel.x, vessel.y), {
                id: -1,
                angle: 0,
                image: "img/aw-logo.png",
                imageWidth: function () {
                    return 32 * context.vesselSize();
                },
                imageHeight: function () {
                    return 16 * context.vesselSize();
                },
                imageYOffset: function () {
                    return 8 * context.vesselSize();
                },
                imageXOffset: function () {
                    return -16 * context.vesselSize();
                },
                type: "marker"
            });
    }

    function createVesselFeatures(vessels, context, selectedFeature, markedVesselId) {
        var result = {
            vesselFeatures: [],
            unSelectable: [],
            awFeatures: []
        };
        console.log("createVesselFeatures vessel size: " + vessels.length);
        $.each(vessels, function (key, value) {

            // in the case that we only have lat,lon,type create a minimal vessel feature.
            if(value != null && value.mmsi == null &&
                value.type != null && value.id == null &&
                value.x && value.y && value.type){
                // this is a simplified overview vessel representation
                result.vesselFeatures.push(createMinimalVesselFeature(value));
            } // standard rich and detailed vessel
            else if (value.type != null && value.mmsi != null) {
                if (!selectedFeature || selectedFeature.attributes.vessel.mmsi != value.mmsi) {
                    result.vesselFeatures.push(createVesselFeature(value, context));
                }
                if (value.inAW) {
                    result.awFeatures.push(createAwFeature(value, context));
                }
            }
        });

        return result;
    }

    var clusterColors = [
        {color: "#ffdd00", densityLimit: 0.0, countLimit: 0},	// Yellow
        {color: "#ff8800", densityLimit: 0.00125, countLimit: 50},	// Orange
        {color: "#ff0000", densityLimit: 0.004, countLimit: 250},	// Red
        {color: "#ff00ff", densityLimit: 0.008, countLimit: 1000}	// Purple
    ];
    var baseColorsOn = "density";

    /**
     * Finds the color of a cluster based on either density or count.
     */
    function findClusterColor(cell) {
        if (baseColorsOn == "density") {
            for (var i = clusterColors.length - 1; i >= 0; i--) {
                if (cell.getDensity() >= clusterColors[i].densityLimit) {
                    return clusterColors[i].color;
                }
            }

        } else if (baseColorsOn == "count") {
            for (var i = clusterColors.length - 1; i >= 0; i--) {
                if (cell.count >= clusterColors[i].countLimit) {
                    return clusterColors[i].color;
                }
            }
        }

        return "#000000";
    }

    var clusterSizes = [
        {zoom: 1, size: 4.5},
        {zoom: 2, size: 2.5},
        {zoom: 3, size: 1.5},
        {zoom: 4, size: 0.75},
        {zoom: 5, size: 0.40},
        {zoom: 6, size: 0.20},
        {zoom: 7, size: 0.10},
        {zoom: 8, size: 0.05},
        {zoom: 9, size: 0.02}
    ];

    function getClusterSize(zoom) {
        for (var index in clusterSizes) {
            if (clusterSizes[index].zoom >= zoom) {
                return clusterSizes[index].size;
            }
        }

        return clusterSizes[clusterSizes.length - 1].size;
    }

    function createClusterFeatures(vessels, context, selectedFeature, zoom) {
        var result = {
            vesselFeatures: [],
            unSelectable: [],
            awFeatures: []
        };
        var size = getClusterSize(zoom);

        var grid = new Grid(size);
        var cluster = new Cluster(vessels, grid, 40);

        var cells = cluster.getCells();

        $.each(cells, function (key, cell) {
            if (cell.items && cell.items.length > 0) {
                for (var index in cell.items) {
                    var vessel = cell.items[index];

                    if (vessel.type != null) {
                        if (!selectedFeature || selectedFeature.attributes.vessel.mmsi != vessel.mmsi) {
                            result.vesselFeatures.push(createVesselFeature(vessel, context));
                        }
                        if (vessel.inAW) {
                            result.awFeatures.push(createAwFeature(vessel, context));
                        }
                    }
                }
            } else {
                var points = [];
                points.push(embryo.map.createPoint(cell.from.lon, cell.from.lat));
                points.push(embryo.map.createPoint(cell.to.lon, cell.from.lat));
                points.push(embryo.map.createPoint(cell.to.lon, cell.to.lat));
                points.push(embryo.map.createPoint(cell.from.lon, cell.to.lat));

                var rings = [new OpenLayers.Geometry.LinearRing(points)];
                var cellFeature = new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Polygon(rings), {
                    fill: findClusterColor(cell),
                    label: ("" + cell.count),
                    type: "cluster"
                });
                result.unSelectable.push(cellFeature);
            }

        });

        return result;
    }

    this.draw2 = function () {
        if (!this.vessels) {
            return
        }

        var selectedFeature = null;
        var that = this;

        $.each(this.layers.vessel.features, function (k, v) {
            if (v.attributes.vessel != null && v.attributes.vessel.mmsi != null && v.attributes.vessel.mmsi == that.selectedId) {
                selectedFeature = v;
            }
        });

        var vesselsCp = this.vessels.slice();
        var markedVessel = null;
        var markedIndex = null;
        $.each(vesselsCp, function (index, vessel) {
            if (vessel.mmsi == that.markedVesselId) {
                markedIndex = index;
                markedVessel = vessel;
            }
        });
        if (markedIndex != null) vesselsCp.splice(markedIndex, 1);

        var context = this.context;

        var features = createFeatures(vesselsCp, context, selectedFeature, this.zoomLevel);
        if (markedVessel != null) {
            features.vesselFeatures.push(createVesselFeature(markedVessel, context));
        }

        var vesselLayer = this.layers.vessel;
        var arr = vesselLayer.features.slice();
        var idx = $.inArray(selectedFeature, arr);
        if (idx != -1) arr.splice(idx, 1);
        console.log("features.vesselFeatures.length=" + features.vesselFeatures.length);
        console.log("removing arr.length=" + arr.length);

        vesselLayer.addFeatures(features.vesselFeatures);
        vesselLayer.destroyFeatures(arr);
        vesselLayer.redraw();


        this.layers.unselectable.removeAllFeatures();
        this.layers.unselectable.addFeatures(features.unSelectable);
        this.layers.unselectable.addFeatures(features.awFeatures);
        if (markedVessel != null) {
            this.layers.unselectable.addFeatures([createMarkedFeature(markedVessel, context)]);
        }

        this.layers.unselectable.redraw();

    };

    this.draw = function (vessels) {
        this.vessels = vessels;
        this.draw2();
    };
}

VesselLayer.prototype = new EmbryoLayer();
