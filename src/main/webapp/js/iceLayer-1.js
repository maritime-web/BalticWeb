$(function() {
    var map = embryo.mapPanel.map;

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

    function createIceTable(d) {
        function c(v) {
            switch (v) {
            case "00": return "Ice Free";
            case "01": return "Open Water (< 1/10)";
            case "02": return "Bergy Water";
            case "10": return "1/10";
            case "12": return "1/10 to 2/10";
            case "13": return "1/10 to 3/10";
            case "20": return "2/10";
            case "23": return "2/10 to 3/10";
            case "24": return "2/10 to 4/10";
            case "30": return "3/10";
            case "34": return "3/10 to 4/10";
            case "35": return "3/10 to 5/10";
            case "40": return "4/10";
            case "45": return "4/10 to 5/10";
            case "46": return "4/10 to 6/10";
            case "50": return "5/10";
            case "56": return "5/10 to 6/10";
            case "57": return "5/10 to 7/10";
            case "60": return "6/10";
            case "67": return "6/10 to 7/10";
            case "68": return "6/10 to 8/10";
            case "70": return "7/10";
            case "78": return "7/10 to 8/10";
            case "79": return "7/10 to 9/10";
            case "80": return "8/10";
            case "81": return "8/10 to 10/10";
            case "89": return "8/10 to 9/10";
            case "90": return "9/10";
            case "91": return "9/10 to 10/10, 9+/10";
            case "92": return "10/10";
            // case "99": return "Unknown/Undetermined";
            // case "-9": return "Null Value";
            default: return "n/a";
            }
        }

        function s(v) {
            switch (v) {
            case "00": return "Ice Free";
            case "80": return "No stage of development";
            case "81": return "New Ice (<10 cm)";
            case "82": return "Nilas Ice Rind (<10 cm)";
            case "83": return "Young Ice (10 to 30 cm)";
            case "84": return "Grey Ice (10 to 15 cm)";
            case "85": return "Grey – White Ice (15 to 30 cm)";
            case "86": return "First Year Ice (>30 cm) or Brash Ice";
            case "87": return "Thin First Year Ice (30 to 70 cm)";
            case "88": return "Thin First Year Ice (stage 1)";
            case "89": return "Thin First Year Ice (stage 2)";
            case "90": return "Code not currently assigned";
            case "91": return "Medium First Year Ice (70 to 120 cm)";
            case "92": return "Code not currently assigned";
            case "93": return "Thick First Year Ice (>120 cm)";
            case "94": return "Code not currently assigned";
            case "95": return "Old Ice";
            case "96": return "Second Year Ice";
            case "97": return "Multi-Year Ice";
            case "98": return "Glacier Ice (Icebergs)";
            // case "99": return "Unknown/Undetermined";
            // case "-9": return "";
            default: return "n/a";
            }
        }

        function f(v) {
            switch (parseFloat(v)) {
            case 11: return "Strips and Patches (1/10)";
            case 12: return "Strips and Patches (2/10)";
            case 13: return "Strips and Patches (3/10)";
            case 14: return "Strips and Patches (4/10)";
            case 15: return "Strips and Patches (5/10)";
            case 16: return "Strips and Patches (6/10)";
            case 17: return "Strips and Patches (7/10)";
            case 18: return "Strips and Patches (8/10)";
            case 19: return "Strips and Patches (9/10)";
            case 20: return "Strips and Patches (10/10)";
            case 0: return "Pancake Ice";
            case 1: return "Shuga/Small Ice Cake, Brash Ice";
            case 2: return "Ice Cake";
            case 3: return "Small Floe";
            case 4: return "Medium Floe";
            case 5: return "Big Floe";
            case 6: return "Vast Floe";
            case 7: return "Giant Floe";
            case 8: return "Fastened (Fast) Floe";
            case 9: return "Growlers, Floebergs, Floebits";
            case 10: return "Icebergs";
            default: return "n/a";
            }
            return v;
        }

        var html = "";

        function o(egenskaber) {
            $.each(egenskaber, function(k,v) {
                html += "<tr><th>"+k+"</th><td>"+v+"</td></tr>";
            });
        }

        html += "<tr><th colspan=2 style=background-color:#eee>Total</th></tr>";
        
        o({ 
            "Concentration": c(d.CT),
            "Stage of Development (S0)": s(d.CN),
            "Stage of Development (Sd)": s(d.CD),
            "Form of Ice": f(d.CF)
        });

        html += "<tr><th colspan=2 style=background-color:#eee>Thickest Partial</th></tr>";

        o({
            "Concentration": c(d.CA),
            "Stage of Development": s(d.SA),
            "Form of Ice": f(d.FA)
        });

        html += "<tr><th colspan=2 style=background-color:#eee>Second Thickest Partial</th></tr>";

        o({
            "Concentration": c(d.CB),
            "Stage of Development": s(d.SB),
            "Form of Ice": f(d.FB)
        });
        
        html += "<tr><th colspan=2 style=background-color:#eee>Third Thickest Partial</th></tr>";

        o({
            "Concentration": c(d.CC),
            "Stage of Development": s(d.SC),
            "Form of Ice": f(d.FC)
        });
        

        return html;
    }
    

    function showIceInformation(iceDescription) {
        $("a[href=#icpSelectedIce]").html("Selected Ice Observation");
        // $("#icpSelectedIce p").html(createIceEggHtml($.extend(iceDescription, { size: 160})));
        $("#icpSelectedIce table").html(createIceTable($.extend(iceDescription, { size: 160})));
        $("#icpSelectedIce p").html("Source: "+iceDescription.source);
        openCollapse("#icpSelectedIce");
    }

    function hideIceInformation() {
        closeCollapse("#icpSelectedIce");
    }

    function setupLayers(shapes) {
        var start = new Date().getTime();

        embryo.map.remove("Ice");
        embryo.map.remove("Water");

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
                    strokeColor: "#000",
                    strokeOpacity: 1
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
                            var p = polygon[j];
                            points.push(embryo.map.createPoint(p.x, p.y));
                        }

                        rings.push(new OpenLayers.Geometry.LinearRing(points));
                    }

                    var feature = new OpenLayers.Feature.Vector(
                        new OpenLayers.Geometry.Polygon(rings), {
                            fillColor: colorByDescription(ice[i].description),
                            iceDescription: ice[i].description
                        }
                    );

                    feature.iceDescription = $.extend(ice[i].description, { source: shape.description.id });

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
                            var p = polygon[j];
                            points.push(embryo.map.createPoint(p.x, p.y));
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
        
        embryo.map.add({
            group: "ice",
            layer: iceLayer, 
            select: true 
        });

        embryo.map.add({
            group: "ice",
            layer: waterLayer
        });
        
        /*
        map.addLayer(iceLayer);
        map.addLayer(waterLayer);
        
    	embryo.mapPanel.add2SelectFeatureCtrl(iceLayer);
        */


        iceLayer.events.on({
            featureselected: function(e) {
                showIceInformation(e.feature.attributes.iceDescription);
            },
            featureunselected: function(e) {
                hideIceInformation();
            }
        });

        console.log("Ice and water layers addded. - "+(new Date().getTime() - start));
    }

    function requestShapefile(name) {
        console.log("Requesting " + name + " data ...");

        var messageId = embryo.messagePanel.show( { text: "Requesting " + name + " data ..." })

        $.ajax({
            url: embryo.baseUrl+"rest/shapefile/multiple/" + name,
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

    embryo.authenticated(function() {
        requestShapefile("201304100920_CapeFarewell_RIC,201308141200_Greenland_WA,201308132150_Qaanaaq_RIC,201308070805_NorthEast_RIC");
    });
    
    embryo.hover(function(e) {
	if (e.feature.iceDescription) {
	    return createIceEggHtml($.extend(e.feature.iceDescription, { size: 100}));
	} else {
	    return null;
        }
    });

    embryo.focusGroup("ice", function() {
        setLayerOpacityById("Water", 1);
        $("#iceControlPanel").css("display", "block");
    });
    
    embryo.unfocusGroup("ice", function() {
        setLayerOpacityById("Water", 0.3);
        $("#iceControlPanel").css("display", "none");
    });
});

