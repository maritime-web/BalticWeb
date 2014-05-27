function IceLayer() {
    this.init = function() {
        var that = this;

        var iceContext = {
            transparency : function() {
                return that.active ? 0.5 : 0.25;
            }
        };

        this.layers.ice = new OpenLayers.Layer.Vector("Ice", {
            styleMap : new OpenLayers.StyleMap({
                "default" : new OpenLayers.Style({
                    fillColor : "${fillColor}",
                    fillOpacity : "${transparency}",
                    strokeWidth : "1",
                    strokeColor : "#000000",
                    strokeOpacity : "0.2",
                    fontColor : "#000000",
                    fontSize : "12px",
                    fontFamily : "Courier New, monospace",
                    label : "${description}",
                    fontOpacity : "${transparency}",
                    fontWeight : "bold"
                }, {
                    context : iceContext
                }),
                "temporary" : new OpenLayers.Style({
                    fillColor : "${fillColor}",
                    fillOpacity : "${transparency}",
                    strokeWidth : "1",
                    strokeColor : "#000000",
                    strokeOpacity : "0.7",
                }, {
                    context : iceContext
                }),
                "select" : new OpenLayers.Style({
                    fillColor : "${fillColor}",
                    fillOpacity : "${transparency}",
                    strokeWidth : "1",
                    strokeColor : "#000",
                    strokeOpacity : "1",
                }, {
                    context : iceContext
                })
            })/*,
            strategies: [
                 new OpenLayers.Strategy.Cluster({
                      distance: 10,
                      threshold: 3
                 })
            ]*/
        });
        this.layers.iceberg = new OpenLayers.Layer.Vector("Ice", {
            styleMap : new OpenLayers.StyleMap({
                "default" : new OpenLayers.Style({
                    externalGraphic : 'img/iceberg.png',
                    graphicWidth : 10,
                    graphicHeight : 12,
                    graphicOpacity : "${transparency}",
                    strokeWidth : "1",
                    strokeColor : "#000000",
                    strokeOpacity : "0.2"
                }, {
                    context : iceContext
                }),
                "temporary" : new OpenLayers.Style({
                    externalGraphic : 'img/iceberg.png',
                    graphicWidth : 10,
                    graphicHeight : 12,
                    graphicOpacity : "${transparency}",
                    strokeWidth : "1",
                    strokeColor : "#000000",
                    strokeOpacity : "0.7",
                }, {
                    context : iceContext
                }),
                "select" : new OpenLayers.Style({
                    externalGraphic : 'img/iceberg.png',
                    graphicWidth : 10,
                    graphicHeight : 12,
                    graphicOpacity : "${transparency}",
                    strokeWidth : "1",
                    strokeColor : "#000",
                    strokeOpacity : "1",
                }, {
                    context : iceContext
                })
            })/*,
            strategies: [
                 new OpenLayers.Strategy.Cluster({
                      distance: 10,
                      threshold: 3
                 })
            ]*/
        });
        this.selectableLayer = this.layers.ice;
        this.selectableAttribute = "iceDescription";
    };

    this.draw = function(chartType, shapes, callback) {
        function colorByDescription(description) {

            if (description.CT == 92 && parseInt(description.FA) == 8){
                return "#979797";
            } else if (description.CT == 79 || description.CT > 80)
                return "#ff0000";
            if (description.CT == 57 || description.CT > 60)
                return "#ff7c06";
            if (description.CT == 24 || description.CT > 30)
                return "#ffff00";
            if (description.CT >= 10)
                return "#8effa0";
            return "#96C7FF";
        }

        this.layers.ice.removeAllFeatures();

        var waterCount = 0;

        var that = this;
        function drawFragment(shape, fragment) {
            var rings = [];
            var polygons = fragment.polygons;

            for ( var k in polygons) {
                var polygon = polygons[k];

                // var copy = [];
                // for(var counter in polygon){
                // copy.push({x:polygon[counter].x, y: polygon[counter].y});
                // }
                var points = [];
                for ( var j in polygon) {
                    var p = polygon[j];

                    if (j >= 1) {
                        var diff = Math.abs(polygon[j - 1].x - p.x);
//                        if (diff > 350 && !(Math.abs(polygon[j-1].x) == Math.abs(p.x) && polygon[j-1].y == p.y)) {
                        if (diff > 350) {
                            if (p.x < polygon[j - 1].x) {
                                p.x += 360;
                            } else {
                                p.x -= 360;
                            }
                        }
                    }

                    points.push(embryo.map.createPoint(p.x, p.y));
                }
                rings.push(new OpenLayers.Geometry.LinearRing(points));
            }

            var feature = new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Polygon(rings), {
                fillOpacity : function() {
                    return 0.4 * groupOpacity;
                },
                fillColor : colorByDescription(fragment.description),
                iceDescription : fragment.description,
                description : ""
            });
            feature.attributes.iceDescription = $.extend(fragment.description, {
                source : shape.description.id
            });
            if (fragment.description.POLY_TYPE == 'I') {
                feature.attributes.description = "";
            } else if (fragment.description.POLY_TYPE == 'W') {
                feature.attributes.description = waterCount == 0 ? shape.description.id : "";
                // modify description to make sure we show it is open water
                feature.attributes.iceDescription.CT = "1";
                waterCount++;
            }
            that.layers.ice.addFeatures([ feature ]);
            //that.layers.ice.strategies[0].deactivate();
            that.layers.iceberg.removeAllFeatures();
            that.layers.ice.refresh();
        }

        function drawFragments(shape, fragments) {
            if (fragments.length > 0) {
                var fragment = fragments.pop();

                drawFragment(shape, fragment);

                window.setTimeout(function() {
                    drawFragments(shape, fragments);
                }, 20);
            } else {
                if (callback) {
                    callback();
                }
            }
        }
        
        function drawPoints() {
            /*var styleData = {
                    externalGraphic : 'img/iceberg.png',
                    graphicWidth : 10,
                    graphicHeight : 12,
                    fillOpacity : "${transparency}"
            };
            var style = new OpenLayers.Style(styleData, {context : attrs});*/
            for(var f in fragments) {
                var feature = new OpenLayers.Feature.Vector(embryo.map.createPoint(fragments[f].description.Long, fragments[f].description.Lat));
                that.layers.iceberg.addFeatures([feature]);
            }
            //that.layers.ice.strategies[0].activate();
            that.layers.ice.removeAllFeatures();
            that.layers.iceberg.refresh();
            callback();
        }

        for ( var l in shapes) {
            var shape = shapes[l];
            var ice = shape.fragments;

            fragments = ice.slice(0);
            if(chartType == 'iceberg') {
                drawPoints(fragments);
            } else {
                drawFragments(shape, fragments);
            }
        }
    };
}

IceLayer.prototype = new EmbryoLayer();
