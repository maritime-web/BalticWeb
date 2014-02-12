function IceLayer() {
    this.init = function() {
        var that = this;

        var iceContext = {
            transparency : function() {
                return that.active ? 0.5 : 0.25;
            }
        }

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
            })
        });

        this.selectableLayer = this.layers.ice;
        this.selectableAttribute = "iceDescription";
    }

    this.draw = function(shapes, callback) {
        function colorByDescription(description) {

            if (description.CT == 92 && parseInt(description.FA) == 8){
                return "#979797";
            } else if (description.CT == 79 || description.CT > 80)
                return "#ff0000";
            if (description.CT == 57 || description.CT > 60)
                return "#ff7c06";
            if (description.CT == 24 || description.CT > 30)
                return "#ffff00";
            if (description.CT > 10)
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
                // try to fix polygon crossing the world - not possible.
                // if((polygon[polygon.length - 1].x - polygon[0].x) > 180){
                // points.push(embryo.map.createPoint(polygon[0].x - 360,
                // polygon[0].y));
                // } else
//                if ((polygon[polygon.length - 1].x - polygon[0].x) < -180) {
//                    console.log(polygon[polygon.length - 1]);
//                    console.log(points[points.length - 1]);

                    // points.push(embryo.map.createPoint(polygon[0].x + 360,
                    // polygon[0].y));
//                }

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

        for ( var l in shapes) {
            var shape = shapes[l];
            var ice = shape.fragments;

            fragments = ice.slice(0);
            drawFragments(shape, fragments);
        }
    }
}

IceLayer.prototype = new EmbryoLayer();
