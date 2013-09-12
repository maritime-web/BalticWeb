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

        var link = "http://www.marinetraffic.com/ais/shipdetails.aspx?mmsi="+data.mmsi;

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
            "More information": "<a href='"+link+"' target='new_window'>"+link+"</a>"
        }
        
        $.each(egenskaber, function(k,v) {
            if (v != null) html += "<tr><th>"+k+"</th><td>"+v+"</td></tr>";
        });

        $("#aesModal h2").html("AIS Information - " + data.name);
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
                        embryo.map.createPoint(lastLon,lastLat),
                        embryo.map.createPoint(currentTrack.lon, currentTrack.lat)
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
            setLayerOpacityById("timeStampsLayer", 0);
            setLayerOpacityById("trackLayer", 0.4);
        }
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
    
    embryo.map.add({
        group: "vessel",
        layer: timeStampsLayer,
    });

    embryo.map.add({
        group: "vessel",
        layer: tracksLayer,
    });

    embryo.map.add({
        group: "vessel",
        control: new OpenLayers.Control.DrawFeature(tracksLayer, OpenLayers.Handler.Path)
    });

    // embryo.mapPanel.map.addControl(new OpenLayers.Control.DrawFeature(tracksLayer, OpenLayers.Handler.Path));

    // embryo.mapPanel.map.addLayer(timeStampsLayer);
    // embryo.mapPanel.map.addLayer(tracksLayer);

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

                $("#viewHistoricalTrack").off("click");
                if (result.pastTrack != null) {
                    $("#viewHistoricalTrack").attr("href", "#");
                    $("#viewHistoricalTrack").on("click", function() {
                        embryo.mapPanel.map.zoomToExtent(tracksLayer.getDataExtent());
                        setLayerOpacityById("timeStampsLayer", 1);
                        setLayerOpacityById("trackLayer", 1);
                    });
                } else {
                    $("#viewHistoricalTrack").attr("href", "");
                }
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

    embryo.groupChanged(function(e) {
        if (e.groupId == "vessel") {
            $("#vesselControlPanel").css("display", "block");
        } else {
            $("#vesselControlPanel").css("display", "none");
        }
    });

});
