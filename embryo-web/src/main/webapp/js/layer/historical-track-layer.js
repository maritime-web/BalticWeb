function HistoricalTrackLayer() {

    this.init = function() {
        this.layers.tracks = new OpenLayers.Layer.Vector("trackLayer", {
            styleMap : new OpenLayers.StyleMap({
                'default' : {
                    strokeColor : "#CC2222",
                    strokeWidth : 3
                }
            })
        });

        this.layers.timestamp = new OpenLayers.Layer.Vector("timeStampsLayer", {
            styleMap : new OpenLayers.StyleMap({
                'default' : {
                    label : "${timeStamp}",
                    fontColor : "black",
                    fontSize : "11px",
                    fontFamily : embryo.defaultFontFamily,
                    fontWeight : "bold",
                    labelAlign : "${align}",
                    labelXOffset : "${xOffset}",
                    labelYOffset : "${yOffset}",
                    labelOutlineColor : "#fff",
                    labelOutlineWidth : 2,
                    labelOutline : 1,
                    pointRadius : 3,
                    fill : true,
                    fillColor : "#CC2222",
                    strokeColor : "#CC2222",
                    stroke : true
                }
            })
        });
    }

    this.draw = function(tracks, id) {
        // Remove old tracks
        if (tracks == null || tracks.length < 2)
            return;

        // Draw tracks layer

        for ( var i = 1; i < tracks.length; i++) {
            // Insert line
            var points = new Array(embryo.map.createPoint(tracks[i - 1].lon, tracks[i - 1].lat), embryo.map
                    .createPoint(tracks[i].lon, tracks[i].lat));

            var line = new OpenLayers.Geometry.LineString(points);
            var lineFeature = new OpenLayers.Feature.Vector(line, {
                id : id
            });
            this.layers.tracks.addFeatures([ lineFeature ]);
        }

        // Draw timestamps layer

        var maxNoTimestampsToDraw = 5;

        var delta = (maxNoTimestampsToDraw - 1) / (tracks[tracks.length - 1].ts - tracks[0].ts - 1);

        var oldHatCounter = -1;

        for ( var i in tracks) {
            var track = tracks[i];

            var hatCounter = Math.floor((track.ts - tracks[0].ts) * delta);

            if (oldHatCounter != hatCounter) {
                oldHatCounter = hatCounter;

                var timeStampFeature = new OpenLayers.Feature.Vector(embryo.map.createPoint(track.lon, track.lat));

                timeStampFeature.attributes = {
                    id : id,
                    timeStamp: formatTime(track.ts),
                    align : "lm",
                    xOffset : 10
                };

                this.layers.timestamp.addFeatures([ timeStampFeature ]);
            }
        }

        this.layers.tracks.refresh();
        this.layers.timestamp.refresh();
    }
}

HistoricalTrackLayer.prototype = new EmbryoLayer();
