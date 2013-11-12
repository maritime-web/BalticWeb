function ScheduleLayer(color) {
    this.init = function() {
        var that = this;

        this.layers = [];
        // Create vector layer for routes

        // Find a better color code. How to convert sRGB to HTML codes?
        var yourDefault = OpenLayers.Util.applyDefaults({
            strokeWidth : 2,
            strokeDashstyle : 'dashdot',
            strokeColor : color,
            strokeOpacity : "${getOpacity}",
            fillColor : "${getColor}",
            fillOpacity : "${getOpacity}"
        }, OpenLayers.Feature.Vector.style["default"]);

        var context = {
            getOpacity : function() {
                return that.active ? 1 : 0.3;
            }
        };

        var select = OpenLayers.Util.applyDefaults({}, OpenLayers.Feature.Vector.style.select);
        var selectStyle = new OpenLayers.Style(select);

        var temporary = OpenLayers.Util.applyDefaults({}, OpenLayers.Feature.Vector.style.temporary);
        var temporaryStyle = new OpenLayers.Style(temporary);

        this.layers.schedule = new OpenLayers.Layer.Vector("scheduleLayer", {
            styleMap : new OpenLayers.StyleMap({
                'default' : new OpenLayers.Style(yourDefault, {
                    context : context
                }),
                'select' : selectStyle,
                'temporary' : temporaryStyle
            })
        });
    }

    function createVectorFeatures(voyages) {
        var features = [];

        if (voyages) {
            var firstPoint = true;
            var currentPoint;
            var previousPoint = null;

            var points = [];
            var lines = [];

            for ( var index in voyages) {
                var lat = parseLatitude(voyages[index].latitude);
                var lon = parseLongitude(voyages[index].longitude);

                currentPoint = embryo.map.createPoint(lon, lat);

                if (!firstPoint) {
                    lines.push(new OpenLayers.Geometry.LineString([ previousPoint, currentPoint ]));
                }
                firstPoint = false;
                previousPoint = currentPoint;
            }

            var multiLine = new OpenLayers.Geometry.MultiLineString(lines);
            var feature = new OpenLayers.Feature.Vector(multiLine, {
                featureType : 'schedule',
                schedule : schedule
            });

            features.push(feature);
        }
        return features;
    }
    ;

    this.draw = function(schedule) {
        this.layers.schedule.removeAllFeatures();

        if (schedule) {
            this.layers.schedule.addFeatures(createVectorFeatures(schedule));
            this.layers.schedule.refresh();
        }
    }
}

ScheduleLayer.prototype = new EmbryoLayer();
