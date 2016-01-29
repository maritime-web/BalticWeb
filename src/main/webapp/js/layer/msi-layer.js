function MsiLayer() {
    this.init = function() {
        this.zoomLevels = [4, 6];

        var that = this;

        var noTransparency = browser.isChrome() && parseFloat(browser.chromeVersion())== 34;
        var context = {
            transparency: function() {
                if(noTransparency){
                    return 1.0;      
                }
                return that.active ? 0.8 : 0.4;
            },
            labelTransparency: function() {
                return (that.zoomLevel > 1) && that.active ? 0.8 : 0.01;
            },
            polygonTransparency: function() {
                return that.active ? 0.3 : 0.15;
            },
            offset: function() {
                return -context.size() / 2;
            },
            size: function() {
                return [16, 20, 24][that.zoomLevel];
            },
            description: function(feature) {
                return feature.cluster ? feature.cluster.length + ' warnings' : feature.data.description;
            }
        };

        this.layers.msi = new OpenLayers.Layer.Vector("MSI", {
            styleMap: new OpenLayers.StyleMap({
                "default": new OpenLayers.Style({
                    graphicOpacity: "${transparency}",
                    externalGraphic : "img/msi.png",
                    graphicWidth : "${size}",
                    graphicHeight : "${size}",
                    graphicYOffset : "${offset}",
                    graphicXOffset : "${offset}",
                    fontColor: "#000",
                    fontSize: "10px",
                    fontOpacity: "${labelTransparency}",
                    fontFamily: "Courier New, monospace",
                    label : "${description}",
                    fontWeight: "bold",
                    labelOutlineWidth : 0,
                    labelYOffset: -20,
                    fillColor: "#ad57a1",
                    fillOpacity: "${polygonTransparency}",
                    strokeWidth: 3,
                    strokeColor: "#8f2f7b",
                    strokeOpacity: "${polygonTransparency}"

                }, { context: context }),
                "select": new OpenLayers.Style({
                    graphicOpacity: 1,
                    externalGraphic : "img/msi.png",
                    graphicWidth : 24,
                    graphicHeight : 24,
                    graphicYOffset : -12,
                    graphicXOffset : -12,
                    backgroundGraphic: "img/ring.png",
                    backgroundXOffset: -16,
                    backgroundYOffset: -16,
                    backgroundHeight: 32,
                    backgroundWidth: 32,

                    fontColor: "#000",
                    fontOpacity: 1,
                    fontSize: "10px",
                    fontFamily: "Courier New, monospace",
                    label : "${description}",
                    fill: true,
                    fillOpacity: 0.6,
                    strokeOpacity: 0.8
                }, { context: context} )

            }),
            strategies: [
                    new OpenLayers.Strategy.Cluster({
                        distance: 25,
                        threshold: 3
                    })
            ]
        });

        this.selectableLayers = [this.layers.msi];
        this.selectableAttribute = "msi";
    };

    this.draw = function(data) {
        this.layers.msi.removeAllFeatures();

        var features = [];

        for (var i in data) {
            var attr = {
                id : i,
                description: data[i].enctext,
                type : "msi",
                msi : data[i]
            };

            switch (data[i].type) {
                case "Point":
                case "Points":
                    for (var j in data[i].points) {
                        var p = data[i].points[j];
                        features.push(new OpenLayers.Feature.Vector(this.map.createPoint(p.longitude, p.latitude), attr));
                    }
                    break;
                case "Polygon":
                    var points = [];

                    for (var j in data[i].points) {
                        var p = data[i].points[j];
                        points.push(this.map.createPoint(p.longitude, p.latitude));
                    }

                    features.push(new OpenLayers.Feature.Vector(
                        new OpenLayers.Geometry.Polygon([new OpenLayers.Geometry.LinearRing(points)]), attr
                    ));

                    break;
                case "Polyline":
                    var points = [];

                    for (var j in data[i].points) {
                        var p = data[i].points[j];
                        points.push(this.map.createPoint(p.longitude, p.latitude));
                    }

                    features.push(new OpenLayers.Feature.Vector(
                        new OpenLayers.Geometry.LineString(points), attr
                    ));

/*
                    features.push(new OpenLayers.Feature.Vector(
                        new OpenLayers.Geometry.Curve([new OpenLayers.Geometry.LineString(points)]), attr
                    ));
*/

                    break;
            }

        }

        this.layers.msi.addFeatures(features);
    };
}

MsiLayer.prototype = new EmbryoLayer();
