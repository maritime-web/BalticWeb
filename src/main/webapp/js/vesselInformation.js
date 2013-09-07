// Vessel information + tracks for individual vessels
// Listens for vessel selected events.

embryo.vesselInformation = {
    renderShortTable: function (data) {
        var html = "";
        
        var egenskaber = {
            "MMSI": data.mmsi,
            "Class": data["class"],
            "Callsign": data.callsign,
            "Cargo": data.cargo,
            "Country": data.country,
            "SOG": data.sog,
            "COG": data.cog,
            "Destination": data.destination,
            "Nav status": data.navStatus,
            "ETA": data.eta
        }
        
        $.each(egenskaber, function(k,v) {
            if (v != null) html += "<tr><th>"+k+"</th><td>"+v+"</td></tr>";
        });

        return html;
    },
    showAesDialog: function (data) {
        var html = "";
        
        var egenskaber = {
            "MMSI": data.mmsi,
            "Class": data["class"],
            "Name": data.name,
            "Callsign": data.callsign,
            "Lat": data.lat,
            "Lon": data.lon,
            "IMO": data.imo,
            "Source": data.source,
            "Type": data.type,
            "Cargo": data.cargo,
            "Country": data.country,
            "SOG": data.sog,
            "COG": data.cog,
            "Heading": data.heading,
            "Draught": data.draught,
            "ROT": data.rot,
            "Width": data.width,
            "Length": data.length,
            "Destination": data.destination,
            "Nav status": data.navStatus,
            "ETA": data.eta,
            "Pos acc": data.posAcc,
            "Last report": data.lastReport,
            "More information": ((data.link != null) ? "<a href=\""+data.link+"\" target=new>"+data.link+"</a>" : null)
        }
        
        $.each(egenskaber, function(k,v) {
            if (v != null) html += "<tr><th>"+k+"</th><td>"+v+"</td></tr>";
        });

        $("#aesModal h2").html("AES Information - " + data.name);
        $("#aesModal table").html(html);
        $("#aesModal").modal("show");
    }
}

$(function() {
    /**
     * Draws the past track. If tracks are null, it will simply remove all tracks
     * and draw nothing.
     * 
     * @param tracks
     *            Array of tracks
     */
    function drawPastTrack(tracks) {
        
        // Remove old tracks
        tracksLayer.removeAllFeatures();
        timeStampsLayer.removeAllFeatures();
        
        // Get time stamp distance
        var CL = false;
        var tracksBetweenTimeStamps;
        if (embryo.mapPanel.map.zoom >= vesselZoomLevel) {
            tracksBetweenTimeStamps = tracksBetweenTimeStampsVL;
        } else {
            tracksBetweenTimeStamps = tracksBetweenTimeStampsCL;
            CL = true;
        }
        
        // Draw tracks
        if (tracks && includePastTracks) {
            var lastLon;
            var lastLat;
            var firstPoint = true;
            var untilTimeStamp = 0;
            
            for (track in tracks) {
                var currentTrack = tracks[track];
                if (!firstPoint) {
                    // Insert line
                    var points = new Array(
                        new OpenLayers.Geometry.Point(lastLon,lastLat).
                            transform(new OpenLayers.Projection("EPSG:4326"), embryo.mapPanel.map.getProjectionObject()),
                        new OpenLayers.Geometry.Point(currentTrack.lon, currentTrack.lat).
                            transform(new OpenLayers.Projection("EPSG:4326"), embryo.mapPanel.map.getProjectionObject())
                    );
                    
                    var line = new OpenLayers.Geometry.LineString(points);
                    var lineFeature = new OpenLayers.Feature.Vector(line);
                    tracksLayer.addFeatures([ lineFeature ]);
                    
                    // Insert timeStamp?
                    if (untilTimeStamp == 0
                        && parseInt(track) + tracksBetweenTimeStamps < tracks.length
                        && includeTimeStamps && (includeTimeStampsOnCL || !CL)) {
                        
                        var timeStampPos = points[0];
                        var timeStampFeature = new OpenLayers.Feature.Vector(
                            timeStampPos);
                        
                        // Remove date from time
                        var time = (new Date(currentTrack.time)).toTimeString();
                        
                        // Change to 24h clock
                        time = to24hClock(time);
                        
                        timeStampFeature.attributes = {
                            timeStamp : time
                        };
                        timeStampsLayer.addFeatures([ timeStampFeature ]);
                        
                        untilTimeStamp = tracksBetweenTimeStamps;
                        
                    } else {
                        untilTimeStamp--;
                    }
                }
                lastLon = currentTrack.lon;
                lastLat = currentTrack.lat;
                firstPoint = false;
            }
            
            // Draw features
            tracksLayer.refresh();
            timeStampsLayer.refresh();
        }
    }

    function openCollapse(id) {
        // $(id).parents(".accordion").find(".collapse[id!="+id.substring(1)+"]").collapse({ toggle: false, parent: "#accordion2"});
        if (!$(id).hasClass("in")) $("a[href="+id+"]").click();
    }

    function closeCollapse(id) {
        if ($(id).hasClass("in")) $("a[href="+id+"]").click();
    }

    function showVesselInformation(data) {
        $("a[href=#vcpSelectedShip]").html("Selected Ship - "+data.name);
        $("#selectedAesInformation table").html(embryo.vesselInformation.renderShortTable(data));
        openCollapse("#vcpSelectedShip");
        $("#selectedAesInformationLink").off("click");
        $("#selectedAesInformationLink").on("click", function() {
            embryo.vesselInformation.showAesDialog(data);
        });

    }
    
    var tracksLayer = new OpenLayers.Layer.Vector("trackLayer", {
        styleMap : new OpenLayers.StyleMap({
            'default' : {
                strokeColor : pastTrackColor,
                strokeOpacity : pastTrackOpacity,
                strokeWidth : pastTrackWidth
            }
        })
    });
    
    var timeStampsLayer = new OpenLayers.Layer.Vector("timeStampsLayer", {
        styleMap : new OpenLayers.StyleMap({
            'default' : {
                label : "${timeStamp}",
                fontColor : timeStampColor,
                fontSize : timeStampFontSize,
                fontFamily : timeStampFontFamily,
                fontWeight : timeStampFontWeight,
                labelAlign : "${align}",
                labelXOffset : "${xOffset}",
                labelYOffset : "${yOffset}",
                labelOutlineColor : timeStamtOutlineColor,
                labelOutlineWidth : 5,
                labelOutline : 1
            }
        })
    });
    
    embryo.mapPanel.map.addControl(new OpenLayers.Control.DrawFeature(tracksLayer, OpenLayers.Handler.Path));
    embryo.mapPanel.map.addLayer(timeStampsLayer);
    embryo.mapPanel.map.addLayer(tracksLayer);

    embryo.vesselSelected(function(e) {
        $("#vesselInformationPanel").css("display", "none");

        // var messageId = embryo.messagePanel.show( { text: "Loading vessel data ..." })
            console.log("e.vesselId is "+e.vesselId);
        
        $.ajax({
            url: embryo.baseUrl+detailsUrl,
            data: { 
                id : e.vesselId, 
                past_track: 1 
            },
            success: function (result) {
	        if (result.pastTrack != null) drawPastTrack(result.pastTrack.points);
                showVesselInformation(result);

                // embryo.messagePanel.replace(messageId, { text: "Vessel data loaded.", type: "success" })
                
            },
            error: function(data) {
                // embryo.messagePanel.replace(messageId, { text: "Server returned error code: " + data.status + " loading vessel data.", type: "error" });
                embryo.messagePanel.show({ text: "Server returned error code: " + data.status + " loading vessel data.", type: "error" });
            }
        });
    
    });

    embryo.vesselUnselected(function() {
        closeCollapse("#vcpSelectedShip");
        $("a[href=#vcpSelectedShip]").html("Selected Ship");
        drawPastTrack(null);
    });

    embryo.focusGroup("vessels", function() {
        $("#vesselControlPanel").css("display", "block");
    });
    
    embryo.unfocusGroup("vessels", function() {
        $("#vesselControlPanel").css("display", "none");
    });

});
