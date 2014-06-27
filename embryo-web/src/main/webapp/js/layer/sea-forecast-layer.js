function SeaForecastLayer() {
    
    this.init = function() {
        var that = this;

        this.context = {
            transparency : function() {
                return that.active ? 0.5 : 0.25;
            },
            fillColor : function(feature){
                //#ff0000
                //#f2dede
                return feature.attributes.district.warning ? "#ff0000" : "transparent";
            }
        
        };

        this.layers.forecasts = new OpenLayers.Layer.Vector("SeaForecasts", {
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
                    label : "${id}\n${name}",
                    fontOpacity : "${transparency}",
                    fontWeight : "bold"
                }, {
                    context : this.context
                }),
                "temporary" : new OpenLayers.Style({
                    fillColor : "${fillColor}",
                    fillOpacity : "${transparency}",
                    strokeWidth : "1",
                    strokeColor : "#000000",
                    strokeOpacity : "0.7",
                }, {
                    context : this.context
                }),
                "select" : new OpenLayers.Style({
                    fillColor : "${fillColor}",
                    fillOpacity : "${transparency}",
                    strokeWidth : "1",
                    strokeColor : "#000",
                    strokeOpacity : "1",
                }, {
                    context : this.context
                })
            })
        });
        
        this.selectableLayers = [this.layers.forecasts];
        this.selectableAttribute = "district";
    };

    this.draw = function(shapes, callback) {

        var that = this;
        function drawFragment(shape, fragment) {
            var rings = [];
            var polygons = fragment.polygons;

            for ( var k in polygons) {
                var polygon = polygons[k];

                var points = [];
                for ( var j in polygon) {
                    var p = polygon[j];

                    if (j >= 1) {
                        var diff = Math.abs(polygon[j - 1].x - p.x);
                        // if (diff > 350 && !(Math.abs(polygon[j-1].x) ==
                        // Math.abs(p.x) && polygon[j-1].y == p.y)) {
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
                name : fragment.description.name,
                id : fragment.description.Id,
                district : fragment.district
            });
            that.layers.forecasts.addFeatures([ feature ]);
            that.layers.forecasts.refresh();
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
    };
}

SeaForecastLayer.prototype = new EmbryoLayer();
