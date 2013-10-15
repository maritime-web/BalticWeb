function HistoricalTrackLayer() {
    this.init = function () {
        this.layers.tracks = new OpenLayers.Layer.Vector("trackLayer", {
            styleMap : new OpenLayers.StyleMap({
                'default' : {
                    strokeColor : pastTrackColor,
                    strokeWidth : pastTrackWidth
                }
            })
        });

        this.layers.timestamp = new OpenLayers.Layer.Vector("timeStampsLayer", {
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
    }

    this.draw = function (tracks) {
        // Remove old tracks

        this.layers.tracks.removeAllFeatures();
        this.layers.timestamp.removeAllFeatures();

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
            this.layers.tracks.addFeatures([ lineFeature ]);
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

                this.layers.timestamp.addFeatures([ timeStampFeature ]);
            }
        }

        this.layers.tracks.refresh();
        this.layers.timestamp.refresh();
    }
}

HistoricalTrackLayer.prototype = new EmbryoLayer();
