function RouteLayer() {
    this.init = function() {
        var colors = {
            "active" : "#FF0000",
            "planned" : "#D5672D",
            "other" : "#3E7D1D"
        // Another green : "#2AAC0C"
        // orig green 2a6237
        };
        var that = this;

        this.layers = [];
        // Create vector layer for routes

        var yourDefault = OpenLayers.Util.applyDefaults({
            strokeWidth : 2,
            strokeDashstyle : 'dashdot',
            strokeColor : "${getColor}",
            strokeOpacity : "${getOpacity}",
        }, OpenLayers.Feature.Vector.style["default"]);

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

    this.createVectorFeatures = function(route, colorKey) {
        var features = [];

        if (route && route.wps) {
            var firstPoint = true;
            var previousWps = null;
            var points = [];

            for ( var index in route.wps) {
                if (!firstPoint && previousWps.heading === 'GC') {
                    var linePoints = this.createGeoDesicLineAsGeometryPoints({
                        y : previousWps.latitude,
                        x : previousWps.longitude
                    }, {
                        y : route.wps[index].latitude,
                        x : route.wps[index].longitude
                    });
                    linePoints.shift();
                    points = points.concat(linePoints);
                }
 
                points = points.concat(this.toGeometryPoints([ {
                    y : route.wps[index].latitude,
                    x : route.wps[index].longitude
                } ]));
                firstPoint = false;
                previousWps = route.wps[index];
            }

            var multiLine = new OpenLayers.Geometry.MultiLineString([ new OpenLayers.Geometry.LineString(points) ]);
            var feature = new OpenLayers.Feature.Vector(multiLine, {
                featureType : 'route',
                route : route,
                colorKey : colorKey
            });

            features.push(feature);
        }

        return features;
    }
    // function removeDrawnRoutes(layer, colorKey){
    // if(colorKey == "active"){
    // var features = layer.getFeaturesByAttribute('colorKey','active');
    // layer.removeFeatures(features);
    // }else{
    // var features = layer.getFeaturesByAttribute('colorKey','planned');
    // layer.removeFeatures(features);
    // features = layer.getFeaturesByAttribute('colorKey','other');
    // layer.removeFeatures(features);
    // }
    // }

    this.draw = function(route, colorKey) {
        this.layers.route.removeAllFeatures();
        // removeDrawnRoutes(this.layers.route, colorKey);

        if (route && route.wps) {
            var features = this.createVectorFeatures(route, colorKey);
            this.layers.route.addFeatures(features);
            this.layers.route.refresh();
        }
    };
}

RouteLayer.prototype = new EmbryoLayer();

/*
 * Can be used to create only one route layer instance and reuse this as
 */
var RouteLayerSingleton = {
    instance : null,
    getInstance : function() {
        if (this.instance == null) {
            this.instance = new RouteLayer();
            addLayerToMap("vessel", this.instance, embryo.map);
        }
        return this.instance;
    }
}
