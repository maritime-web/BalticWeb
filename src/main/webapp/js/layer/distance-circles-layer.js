function DistanceCirclesLayer() {
    this.init = function () {
        this.layers.rings = new OpenLayers.Layer.Vector("Vessel - Rings Layer", {
            styleMap: new OpenLayers.StyleMap({
                "default": new OpenLayers.Style({
                    fillColor: "#f80",
                    fillOpacity: 0.1,
                    strokeWidth: 2,
                    strokeColor: "#f80",
                    strokeOpacity: 0.7
                })
            })
        });
    }

    this.draw = function (vessel, vesselDetails) {
        this.layers.rings.removeAllFeatures();
        this.layers.rings.addFeatures(embryo.adt.createRing(vessel.x, vessel.y, vesselDetails.ais.sog * 3 * 1.852, 3));
    }
}

DistanceCirclesLayer.prototype = new EmbryoLayer();
