$(function() {
    var groupSelected = true;
    var iconSize = 1;

    var context = {
        labelTransparency: function() { return (groupSelected && iconSize > 1) ? 0.7 : 0.01 },
        transparency: function() { return groupSelected ? 0.8 : 0.5 },
        offset: function() { return -context.size() / 2; },
        size: function() { return [12, 16, 24][iconSize] }
    };

    var msiLayer = new OpenLayers.Layer.Vector("MSI", {
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
                labelYOffset: -20
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
                fill: true
            }, { context: context} )

        })
    });

    embryo.map.add({
        group: "msi",
        layer: msiLayer,
        select: true 
    });

    embryo.groupChanged(function(e) {
        if (e.groupId == "msi") {
            groupSelected = true;
            $("#msiControlPanel").css("display", "block");
            msiLayer.redraw();
            $("#msiControlPanel .collapse").data("collapse", null)
            openCollapse("#msiControlPanel .accordion-body:first");
        } else {
            groupSelected = false;
            $("#msiControlPanel").css("display", "none");
            msiLayer.redraw();
        }
    });

    function formatDate(dato) {
        if (dato == null) return "-";
        var d = new Date(dato);
        return d.getFullYear()+"-"+(""+(101+d.getMonth())).slice(1,3)+"-"+(""+(100+d.getDate())).slice(1,3);
    }
    
    function formatTime(dato) {
        if (dato == null) return "-";
        var d = new Date(dato);
        return formatDate(dato) + " " + d.getHours()+":"+(""+(100+d.getMinutes())).slice(1,3);
    }

    embryo.authenticated(function() {
        var messageId = embryo.messagePanel.show( { text: "Requesting active MSI warnings ..." })

        $.ajax({
            url: embryo.baseUrl+"rest/msi/list",
            data: { },
            success: function(data) {
                data = data.sort(function(a,b) {
                    return b.created-a.created;
                });

                for (var i in data) data[i].id = i;

                // Update overview table

                var html = "<tr><th>Date</th><th>Type</th><th>Area</th></tr>";

                for (var i in data) {
                    html += "<tr index="+i+" style='cursor:pointer'><td>"+formatDate(data[i].created)+"</td><td>"+data[i].enctext+"</td><td>"+data[i].mainArea+" - "+data[i].subArea+"</td>";               }

                $("#msiOverview table").html(html);
       
                $("#msiOverview tr").click(function(e) {
                    var msi = data[$(this).attr("index")];
                    // showMsiInformation(msi);
                    for (var i in msiLayer.features) {
                        if (msiLayer.features[i].attributes.msi == msi) {
                            embryo.map.select(msiLayer.features[i]);
                        }
                    }

                    embryo.map.setCenter(msi.longitude, msi.latitude, 8);
                });

                // Add features

                var features = [];

                for (var i in data) {
                    var attr = {
                        id : i,
                        description: data[i].enctext,
                        type : "msi",
                        msi : data[i]
                    }

                    var geom = embryo.map.createPoint(data[i].longitude, data[i].latitude);

                    features.push(new OpenLayers.Feature.Vector(geom, attr));
                }

                msiLayer.addFeatures(features);

                embryo.messagePanel.replace(messageId, { text: data.length + " MSI warnings returned.", type: "success" })
            },
            error: function(data) {
                embryo.messagePanel.replace(messageId, { text: "Server returned error code: " + data.status + " requesting MSI warnings.", type: "error" })
            }
        });
    });
    
    embryo.ready(function() {
        function fixAccordionSize() {
            $("#msiOverview .accordion-inner").css("overflow", "auto");
            $("#msiOverview .accordion-inner").css("max-height", Math.max(100, $(window).height()-350)+"px"); 
        }
        
        $(window).resize(fixAccordionSize);
        
        fixAccordionSize();
    });

    function showMsiInformation(msi) {
        var html = "";

        html += "<tr><td>Created</td><td>"+formatDate(msi.created)+"</td></tr>";
        html += "<tr><td>Updated</td><td>"+formatDate(msi.updated)+"</td></tr>";
        html += "<tr><td>Main Area</td><td>"+msi.mainArea+"</td></tr>";
        html += "<tr><td>Sub Area</td><td>"+msi.subArea+"</td></tr>";

        $("#msiSelectedItem table").html(html);
        $("#msiSelectedItem p").html(msi.text);
        $("a[href=#msiSelectedItem]").html("Selected Warning - "+msi.enctext);
        openCollapse("#msiSelectedItem");
        $("#msiOverview tr").removeClass("alert");
        $("#msiOverview tr[index="+msi.id+"]").addClass("alert");
    }

    function hideMsiInformation() {
        closeCollapse("#msiSelectedItem");
        $("a[href=#msiSelectedItem]").html("Selected Warning");
        $("#msiOverview tr").removeClass("alert");
    }

    msiLayer.events.on({
        featureselected: function(e) {
            showMsiInformation(e.feature.attributes.msi);
        },
        featureunselected: function(e) {
            hideMsiInformation();
        }
    });

    embryo.map.internalMap.events.register("zoomend", embryo.map.internalMap, function() {
        var newIconSize = 0;
        if (embryo.map.internalMap.zoom >= 5) newIconSize = 1;
        if (embryo.map.internalMap.zoom >= 7) newIconSize = 2;

        if (iconSize != newIconSize) {
            iconSize = newIconSize;
            msiLayer.redraw();
        }
    });

});
