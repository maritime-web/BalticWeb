var map;

function setupBasicMapLayer() {
    var layer = new OpenLayers.Layer.WMS(
        "Basic Map",
        "http://vmap0.tiles.osgeo.org/wms/vmap0",
        { layers: 'basic' }
    );
    map.addLayer(layer);
}

function setLayerVisibility(id, value) {
    var layers = map.getLayersByName(id);

    for (var k in layers) {
        layers[k].setVisibility(value);
    }
}

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

    var html = "<div style=\"background-image:url('egg.png'); width:"+s(140)+"px; height: "+s(200)+"px; "+
        "background-size: 100% 100%; text-align:center; font-family:sans-serif; font-size: "+s(20)+"px;\">";

    html += "<div style=\"height: "+s(50)+"px; padding-top: "+s(25)+"px\">"+f(p.CT)+"</div>";
    html += "<div style=\"height: "+s(45)+"px; padding-top: "+s(15)+"px\">"+f(p.CA)+" "+f(p.CB)+" "+f(p.CC)+"</div>"
    html += "<div style=\"height: "+s(15)+"px; padding-top: "+s(5)+"px\">"+f(p.SA)+" "+f(p.SB)+" "+f(p.SC)+"</div>";
    html += "<div style=\"height: "+s(15)+"px; padding-top: "+s(5)+"px\">"+f(p.FA)+" "+f(p.FB)+" "+f(p.FC)+"</div>";
    html += "</div>";
    return html;
}

function showHover(e, d) {
    var lonlatCenter = e.feature.geometry.getBounds().getCenterLonLat();
    var pixelTopLeft = new OpenLayers.Pixel(0, 0);
    var lonlatTopLeft = map.getLonLatFromPixel(pixelTopLeft);
    pixelTopLeft = map.getPixelFromLonLat(lonlatTopLeft);

    var pixel = map.getPixelFromLonLat(lonlatCenter);

    var x = pixel.x - pixelTopLeft.x + $("#map").position().left;
    var y = pixel.y - pixelTopLeft.y + $("#map").position().top;

    $("#hoverDiv").css('display', 'block');
    $("#hoverDiv").css('top', y + 'px');
    $("#hoverDiv").css('left', x + 'px');
    d.size=75;
    $("#hoverDiv").html(createIceEggHtml(d));
}

function report(e) {
    console.log(e.type, e.feature.id);
    switch (e.type) {
    case "featurehighlighted":
        showHover(e, e.feature.iceDescription);
        break;
    case "featureunhighlighted":
        $("#hoverDiv").css('display', 'none');
        break;
    }

};

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
                        /*
                          pointMapped.transform(
                          new OpenLayers.Projection("EPSG:4326"),
                          map.getProjectionObject()
                          );
                        */
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
                
                iceLayer.addFeatures([ feature ]);
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
                        /*
                          pointMapped.transform(
                          new OpenLayers.Projection("EPSG:4326"),
                          map.getProjectionObject()
                          );
                        */
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

    var highlightCtrl = new OpenLayers.Control.SelectFeature(iceLayer, {
        hover: true,
        highlightOnly: true,
        renderIntent: "temporary",
        eventListeners: {
            beforefeaturehighlighted: report,
            featurehighlighted: report,
            featureunhighlighted: report
        }
    });

    map.addControl(highlightCtrl);

    highlightCtrl.activate();

    updateVisibility();
}

function requestShapefile(name) {
    $("#feedback").html("Requesting " + name + " data ...");

    var parameter = { };

    if ($("#resolution").val().trim() != "") {
        parameter.resolution = parseInt($("#resolution").val().trim());
    }

    $.ajax({
        url: "http://localhost:8080/arcticweb/rest/shapefile/multiple/" + name,
        data: parameter,
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
            $("#feedback").html(totalPolygons + " polygons. "+totalPoints+" points returned.");
            setupLayers(data);
        },
        error: function(data) {
            $("#feedback").html("Server returned error code: " + data.status);
        }
    });
}

function updateVisibility() {
    setLayerVisibility("Water", $("#waterVisibility").is(':checked'));
    setLayerVisibility("Ice", $("#iceVisibility").is(':checked'));
    setLayerVisibility("Basic Map", $("#mapVisibility").is(':checked'));
}

$(function() {
    map = new OpenLayers.Map('map');

    setupBasicMapLayer();

    map.zoomToMaxExtent();
    map.zoomIn();

    $("#fetchButton").click(function() {
        requestShapefile($("#fileName").val());
    });

    $("input:checkbox").click(updateVisibility);
});
