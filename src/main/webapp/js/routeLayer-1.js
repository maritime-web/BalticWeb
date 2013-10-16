function RouteLayer(color) {
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

        this.layers.route = new OpenLayers.Layer.Vector("routeLayer", {
            styleMap : new OpenLayers.StyleMap({
                'default' : new OpenLayers.Style(yourDefault, {
                    context : context
                }),
                'select' : selectStyle,
                'temporary' : temporaryStyle
            })
        });
    }

    function createVectorFeatures(route) {
        var features = [];

        if (route && route.waypoints) {
            var firstPoint = true;
            var currentPoint;
            var previousPoint = null;

            var points = [];
            var lines = [];

            for ( var index in route.waypoints) {
                currentPoint = embryo.map
                        .createPoint(route.waypoints[index].longitude, route.waypoints[index].latitude);

                // points.push(embryo.route.createWaypointFeature(currentPoint));
                if (!firstPoint) {
                    lines.push(new OpenLayers.Geometry.LineString([ previousPoint, currentPoint ]));
                }
                firstPoint = false;
                previousPoint = currentPoint;
            }

            var multiLine = new OpenLayers.Geometry.MultiLineString(lines);
            var feature = new OpenLayers.Feature.Vector(multiLine, {
                featureType : 'route',
                route : route
            });

            features.push(feature);
        }
        return features;
    };

    this.draw = function(route) {
        this.layers.route.removeAllFeatures();

        if (route && route.waypoints) {
            this.layers.route.addFeatures(createVectorFeatures(route));
            this.layers.route.refresh();
        }
    }
}

RouteLayer.prototype = new EmbryoLayer();

