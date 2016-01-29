function GreenposMarkerLayer() {
    this.init = function () {
        var defTemplate = OpenLayers.Util.applyDefaults({
            strokeWidth : 2,
            strokeColor : "black",
            fillColor : "yellow",
            fillOpacity: 1,
            graphicName : "x",
            pointRadius : 15
        }, OpenLayers.Feature.Vector.style["default"]);

        this.layers.point = new OpenLayers.Layer.Vector("pointLayer", {
            styleMap : new OpenLayers.StyleMap({
                'default' : defTemplate
            })
        });
    }

    this.draw = function (lon, lat) {
        this.layers.point.removeAllFeatures();
        this.layers.point.addFeatures([new OpenLayers.Feature.Vector(embryo.map.createPoint(lon, lat))]);
        this.layers.point.refresh();
    }
}

GreenposMarkerLayer.prototype = new EmbryoLayer();
