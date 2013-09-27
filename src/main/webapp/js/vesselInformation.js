// Vessel information + tracks for individual vessels
// Listens for vessel selected events.

embryo.vesselInformation = {
    mmsi : null,    
        
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
        embryo.vesselInformation.mmsi = data.mmsi;
        
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
var module = angular.module('embryo.vesselControl', ['embryo.selectedShip', 'embryo.reportComp']);

$(function() {
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

        if (tracks == null || tracks.length < 2) return;
        
        // Draw tracks layer

        for (var i = 1; i < tracks.length; i++) {
            // Insert line
            var points = new Array(
                embryo.map.createPoint(tracks[i-1].lon, tracks[i-1].lat),
                embryo.map.createPoint(tracks[i].lon, tracks[i].lat)
            );
            
            var line = new OpenLayers.Geometry.LineString(points);
            var lineFeature = new OpenLayers.Feature.Vector(line);
            tracksLayer.addFeatures([ lineFeature ]);
        }
        
        // Draw timestamps layer

        var maxNoTimestampsToDraw = 5;

        var delta = (maxNoTimestampsToDraw - 1) / (tracks[tracks.length - 1].time - tracks[0].time - 1);
        
        var oldHatCounter = -1;

        for (var i in tracks) {
            var track = tracks[i];

            var hatCounter = Math.floor((track.time - tracks[0].time) * delta);

            if (oldHatCounter != hatCounter) {
                oldHatCounter = hatCounter;
 
                var timeStampFeature = new OpenLayers.Feature.Vector(embryo.map.createPoint(track.lon, track.lat));
                
                time = formatTime(track.time);
                
                timeStampFeature.attributes = {
                    timeStamp : time,
                    align: "lm",
                    xOffset: 10
                };
                
                timeStampsLayer.addFeatures([ timeStampFeature ]);
            }
        }
        
        // Draw features
        tracksLayer.refresh();
        timeStampsLayer.refresh();

        setLayerOpacityById("timeStampsLayer", 0);
        setLayerOpacityById("trackLayer", 0.4);
    }

    function showVesselInformation(data) {
        openCollapse("#vcpSelectedShip");
        $("a[href=#vcpSelectedShip]").html("Selected Ship - "+data.name);
        $("#selectedAesInformation table").html(embryo.vesselInformation.renderShortTable(data));
        $("#selectedAesInformationLink").off("click");
        $("#selectedAesInformationLink").on("click", function(e) {
            e.preventDefault();
            embryo.vesselInformation.showAesDialog(data);
        });
    }
    
    var tracksLayer = new OpenLayers.Layer.Vector("trackLayer", {
        styleMap : new OpenLayers.StyleMap({
            'default' : {
                strokeColor : pastTrackColor,
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
                labelOutlineColor : "#fff",
                labelOutlineWidth : 2,
                labelOutline : 1,
                pointRadius: 3,
                fill: true,
                fillColor : pastTrackColor,
                strokeColor : pastTrackColor,
                stroke: true
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

    /*embryo.map.add({
        group: "vessel",
        control: new OpenLayers.Control.DrawFeature(tracksLayer, OpenLayers.Handler.Path)
    });*/

    embryo.vesselSelected(function(e) {
        $("#vesselInformationPanel").css("display", "none");

        // var messageId = embryo.messagePanel.show( { text: "Loading vessel data ..." })
        
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
                    $("#viewHistoricalTrack").on("click", function(e) {
                        e.preventDefault();
                        embryo.map.zoomToExtent([tracksLayer]);
                        setLayerOpacityById("timeStampsLayer", 0.8);
                        setLayerOpacityById("trackLayer", 0.4);
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
            $("#vesselControlPanel .collapse").data("collapse", null)
            openCollapse("#vesselControlPanel .accordion-body:first");
        } else {
            $("#vesselControlPanel").css("display", "none");
        }
    });

});
