function ScheduleLayer() {
    this.init = function() {
        var colors = {
                "own" : "#000000",
                "other" : "#999999"
        };
        var that = this;

        this.layers = [];
        // Create vector layer for routes
        
        OpenLayers.DirectionStyle = OpenLayers.Util.extend({orientation : true}, OpenLayers.Feature.Vector.style["default"]);

        // Find a better color code. How to convert sRGB to HTML codes?
        var yourDefault = OpenLayers.Util.applyDefaults({
            strokeWidth : 1,
            strokeColor : "${getColor}",
            strokeOpacity : "${getOpacity}",
            fillColor : "${getColor}",
            fillOpacity : "${getOpacity}"
        }, OpenLayers.DirectionStyle);

        var context = {
            getOpacity : function() {
                return that.active ? 1 : 0.3;
            },
            getColor : function(feature) {
                return colors[feature.data.colorKey];
            }
        };

        var select = OpenLayers.Util.applyDefaults({}, OpenLayers.Feature.Vector.style.select);
        var selectStyle = new OpenLayers.Style(select);

        var temporary = OpenLayers.Util.applyDefaults({}, OpenLayers.Feature.Vector.style.temporary);
        var temporaryStyle = new OpenLayers.Style(temporary);

        this.layers.schedule = new OpenLayers.Layer.Vector("scheduleLayer", {
            renderers: ['SVGExtended', 'VMLExtended', 'CanvasExtended'],
            styleMap : new OpenLayers.StyleMap({
                'default' : new OpenLayers.Style(yourDefault, {
                    context : context
                }),
                'select' : selectStyle,
                'temporary' : temporaryStyle
            })
        });
    };

    function createVectorFeatures(voyages, colorKey) {
        var features = [];

        if (voyages) {
            var points = [];
            for ( var index in voyages) {
                points.push(embryo.map.createPoint(voyages[index].longitude, voyages[index].latitude));
            }

            var multiLine = new OpenLayers.Geometry.MultiLineString(new OpenLayers.Geometry.LineString(points));
            var feature = new OpenLayers.Feature.Vector(multiLine, {
                featureType : 'schedule',
                schedule : schedule,
                colorKey : colorKey
            });

            features.push(feature);
        }
        return features;
    }
    ;

    this.draw = function(schedule, colorKey) {
        this.layers.schedule.removeAllFeatures();

        if (schedule) {
            this.layers.schedule.addFeatures(createVectorFeatures(schedule, colorKey));
            this.layers.schedule.refresh();
        }
    };
}

ScheduleLayer.prototype = new EmbryoLayer();

/*
 * Can be used to create only one schedule layer instance and reuse this as
 */
var ScheduleLayerSingleton = {
    instance : null,
    getInstance : function() {
        if (this.instance == null) {
            this.instance = new ScheduleLayer();
            addLayerToMap("vessel", this.instance, embryo.map);
        }
        return this.instance;
    }
};