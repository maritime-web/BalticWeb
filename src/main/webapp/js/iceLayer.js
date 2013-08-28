function createIceLayer() {
    var map = embryo.mapPanel.map;

    function removeLayerById(id) {
        var layers = map.getLayersByName(id);

        for (var k in layers) {
            map.removeLayer(layers[k]);
        }
    }

    function colorByDescription(description) {
        if (description.CT > 80) return "#ff0000";
        if (description.CT > 60) return "#e57425";
        if (description.CT > 40) return "#ffc05e";
        if (description.CT > 20) return "#fdfc39";
        return "#90fba4";
    }

    function createIceEggHtml(p) {
        function s(v) {
            return Math.round(v * p.size / 200.0);
        }

        function f(v) {
            if (v == -9) return "&middot;";
            return v;
        }

        var html = "<div style=\"background-image:url('img/egg.png'); width:"+s(140)+"px; height: "+s(200)+"px; "+
            "background-size: 100% 100%; text-align:center; font-family:sans-serif; font-size: "+s(20)+"px;\">";

        html += "<div style=\"height: "+s(50)+"px; padding-top: "+s(25)+"px\">"+f(p.CT)+"</div>";
        html += "<div style=\"height: "+s(45)+"px; padding-top: "+s(15)+"px\">"+f(p.CA)+" "+f(p.CB)+" "+f(p.CC)+"</div>"
        html += "<div style=\"height: "+s(15)+"px; padding-top: "+s(5)+"px\">"+f(p.SA)+" "+f(p.SB)+" "+f(p.SC)+"</div>";
        html += "<div style=\"height: "+s(15)+"px; padding-top: "+s(5)+"px\">"+f(p.FA)+" "+f(p.FB)+" "+f(p.FC)+"</div>";
        html += "</div>";
        return html;
    }

    function setupLayers(shapes) {
        removeLayerById("Ice");
        removeLayerById("Water");

        var iceLayer = new OpenLayers.Layer.Vector("Ice", {
            styleMap: new OpenLayers.StyleMap({
                "default": new OpenLayers.Style({
                    fillColor: "${fillColor}",
                    fillOpacity: 0.4,
                    strokeWidth: 1,
                    strokeColor: "#000000",
                    strokeOpacity: 0.2,
                }),
                "temporary": new OpenLayers.Style({
                    fillColor: "${fillColor}",
                    fillOpacity: 0.4,
                    strokeWidth: 1,
                    strokeColor: "#000000",
                    strokeOpacity: 0.7,
                }),
                "select": new OpenLayers.Style({
                    fillColor: "${fillColor}",
                    fillOpacity: 0.4,
                    strokeWidth: 1,
                    strokeColor: "#0033ff",
                    strokeOpacity: 0.7
                })
            })
        });

        var waterLayer = new OpenLayers.Layer.Vector("Water", {
            styleMap: new OpenLayers.StyleMap({
                "default": new OpenLayers.Style({
                    fillColor: "#5599ff",
                    fillOpacity: 0.2,
                    strokeWidth: 0,
                    strokeColor: "#000000",
                    strokeOpacity: 0,
                    fontColor: "#000000",
                    fontSize: "12px",
                    fontFamily: "Courier New, monospace",
                    label : "${description}",
                    fontWeight: "bold"
                })
            })
        });

        for (var l in shapes) {
            var shape = shapes[l];
            var ice = shape.fragments;
            for (var i in ice)
                if (ice[i].description.POLY_TYPE == 'I') {
                    var polygons = ice[i].polygons;

                    var rings = [];

                    for (var k in polygons) {
                        var polygon = polygons[k];

                        var points = [];

                        for (var j in polygon) {
                            var point = polygon[j];
                            var pointMapped = new OpenLayers.Geometry.Point(point.x, point.y);
                            pointMapped.transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject());
                            points.push(pointMapped);
                        }

                        rings.push(new OpenLayers.Geometry.LinearRing(points));
                    }

                    var feature = new OpenLayers.Feature.Vector(
                        new OpenLayers.Geometry.Polygon(rings), {
                            fillColor: colorByDescription(ice[i].description),
                        }
                    );

                    feature.iceDescription = ice[i].description;

                    iceLayer.addFeatures([ feature ], { iceDescription: ice[i].description });
                }

            // Water

            var rings = [];

            for (var i in ice)
                if (ice[i].description.POLY_TYPE == 'W') {
                    var polygons = ice[i].polygons;

                    for (var k in polygons) {
                        var polygon = polygons[k];

                        var points = [];

                        for (var j in polygon) {
                            var point = polygon[j];
                            var pointMapped = new OpenLayers.Geometry.Point(point.x, point.y);
                            pointMapped.transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject());
                            points.push(pointMapped);
                        }

                        rings.push(new OpenLayers.Geometry.LinearRing(points));
                    }

                }

            var feature = new OpenLayers.Feature.Vector(
                new OpenLayers.Geometry.Polygon(rings), {
                    description: shape.description.id
                }
            );

            waterLayer.addFeatures([ feature ]);
        }

        map.addLayer(iceLayer);
        map.addLayer(waterLayer);

    	embryo.mapPanel.add2HoverFeatureCtrl(iceLayer);

        console.log("Ice and water layers addded.")
    }

    function requestShapefile(name) {
        console.log("Requesting " + name + " data ...");

        var messageId = embryo.messagePanel.show( { text: "Requesting " + name + " data ..." })

        $.ajax({
            url: "rest/shapefile/multiple/" + name,
            data: { },
            success: function(data) {
                var totalPolygons = 0;
                var totalPoints = 0;
                for (var k in data) {
                    var s = data[k];
                    for (var i in s.fragments) {
                        totalPolygons += s.fragments[i].polygons.length;
                        for (var j in s.fragments[i].polygons)
                            totalPoints += s.fragments[i].polygons[j].length;
                    }
                }
                embryo.messagePanel.replace(messageId, { text: totalPolygons + " polygons. "+totalPoints+" points returned.", type: "success" })
                console.log(totalPolygons + " polygons. "+totalPoints+" points returned.");
                setupLayers(data);
            },
            error: function(data) {
                embryo.messagePanel.replace(messageId, { text: "Server returned error code: " + data.status + " requesting ice data.", type: "error" })
                console.log("Server returned error code: " + data.status + " requesting ice data.");
            }
        });
    }

    requestShapefile("201304100920_CapeFarewell_RIC,201308141200_Greenland_WA,201308132150_Qaanaaq_RIC,201308070805_NorthEast_RIC");

    embryo.mapPanel.hoveringHandlers.push(function(e) {
		if (e.feature.iceDescription) {
		    return createIceEggHtml($.extend(e.feature.iceDescription, { size: 100}));
		} else {
		    return null;
        }
    });

};

$(document).ready(createIceLayer);
