function IcePrognosesLayer() {

    OpenLayers.Strategy.RuleCluster = OpenLayers.Class(OpenLayers.Strategy.Cluster, {
        /**
         * the rule to use for comparison
         */
        rule : null,
        /**
         * Method: shouldCluster Determine whether to include a feature in a
         * given cluster.
         * 
         * Parameters: cluster - {<OpenLayers.Feature.Vector>} A cluster.
         * feature - {<OpenLayers.Feature.Vector>} A feature.
         * 
         * Returns: {Boolean} The feature should be included in the cluster.
         */
        shouldCluster : function(cluster, feature) {
            var superProto = OpenLayers.Strategy.Cluster.prototype;
            return this.rule.evaluate(cluster.cluster[0]) && this.rule.evaluate(feature) && superProto.shouldCluster.apply(this, arguments);
        },
        CLASS_NAME : "OpenLayers.Strategy.RuleCluster"
    });

    this.init = function() {
        this.zoomLevels = [ 4, 6, 11 ];

        var that = this;

        this.context = {
            transparency : function() {
                return that.active ? 0.5 : 0.25;
            },
            size : function(feature) {
                return [ 16, 20, 24, 24 ][that.zoomLevel];
            },
            offset : function() {
                return -that.context.size() / 2;
            },
            zoomDependentDescription : function(feature) {
                if (that.zoomLevel < 3) {
                    return "";
                }
                return that.context.description(feature);
            },
            description : function(feature) {
                return feature.cluster ? feature.cluster.length + " Ice prognoses locations" : feature.attributes.iceDescription.Number + ": "
                        + feature.attributes.iceDescription.Placename;
            },
            display : function(feature) {
                return "yes";
            }
        };

        this.layers.iceprognoses = new OpenLayers.Layer.Vector("IcePrognoses", {
            styleMap : new OpenLayers.StyleMap({
                "default" : new OpenLayers.Style({
                    // externalGraphic : "img/inshoreIceReport.png",
                    graphicOpacity : "${transparency}",
                    graphicWidth : '${size}',
                    graphicHeight : '${size}',
                    graphicYOffset : "${offset}",
                    graphicXOffset : "${offset}",
                    fillColor : '${level}',
                    strokeWidth : 1,
                    strokeColor : '#333333',
                    strokeOpacity : 1,
                    fontColor : "#000000",
                    fontSize : "10px",
                    fontFamily : "Courier New, monospace",
                    // label : "${obs}",
                    fontOpacity : 1,
                    fontWeight : "bold",
                    labelOutlineWidth : 0,
                    labelYOffset : -20
                }, {
                    context : this.context
                }),
                "select" : new OpenLayers.Style({
                    // externalGraphic : "img/inshoreIceReport.png",
                    graphicOpacity : 1,
                    graphicWidth : 24,
                    graphicHeight : 24,
                    graphicXOffset : -12,
                    graphicYOffset : -12,
                    backgroundGraphic : "img/ring.png",
                    backgroundXOffset : -16,
                    backgroundYOffset : -16,
                    backgroundHeight : 32,
                    backgroundWidth : 32,
                    fontOpacity : 1,
                    fontColor : "#000",
                    fontSize : "10px",
                    fontFamily : "Courier New, monospace",
                    label : "${obs}",
                    fontWeight : "bold",
                    labelOutlineWidth : 0,
                    labelYOffset : -20,
                    display : "${display}",
                }, {
                    context : this.context
                })
            })
        });

        this.selectableLayers = [ this.layers.iceprognoses ];
        this.selectableAttribute = "number";
    };

    this.getConcentrationLevel = function(obs) {
        if(obs < 0.0001) {
            return '#669999';
        } else if(obs < 0.001) {
            return '#996666';
        } else if(obs < 0.01) {
            return '#cc3333';
        } else {
            return '#ff0000';
        }
    };

    this.drawConcentration = function(prognosis, time) {
        var index = prognosis.variables['Ice concentration'];
        var lats = prognosis.metadata.lat;
        var lons = prognosis.metadata.lon;
        var features = [];
        
        for(var data in prognosis.data) {
            var d = prognosis.data[data];
            if(time == d.time && d.obs[index]) {
                var level = this.getConcentrationLevel(d.obs[index]);
                var lat = lats[d.lat];
                var lon = lons[d.lon];
                //var location = embryo.map.createPoint(lons[d.lon], lats[d.lat]);
                var half = 0.5;
                var points = [embryo.map.createPoint(lon - half, lat - half), embryo.map.createPoint(lon + half, lat - half), embryo.map.createPoint(lon + half, lat + half), embryo.map.createPoint(lon - half, lat + half)];
                //var square = OpenLayers.Geometry.Polygon.createRegularPolygon(location, 100, 4, 0);
                var square = new OpenLayers.Geometry.LinearRing(points);
                var feature = new OpenLayers.Feature.Vector(square, {level : level, obs : d.obs[index]});
                features.push(feature);
                console.log('Ice conc: ' + lats[d.lat] + ':' + lons[d.lon] + ' - ' + d.obs[index]);
            }
        }
        return features;
    };

    this.drawThickness = function(prognosis, time) {
        var index = prognosis.variables['Ice thickness'];
        var lats = prognosis.metadata.lat;
        var lons = prognosis.metadata.lon;
        var features = [];
        
        for(var data in prognosis.data) {
            var d = prognosis.data[data];
            if(time == d.time && d.obs[index]) {
                var level = this.getConcentrationLevel(d.obs[index]);
                var lat = lats[d.lat];
                var lon = lons[d.lon];
                //var location = embryo.map.createPoint(lons[d.lon], lats[d.lat]);
                var points = [embryo.map.createPoint(lon - 0.5, lat - 0.5), embryo.map.createPoint(lon + 0.5, lat - 0.5), embryo.map.createPoint(lon + 0.5, lat + 0.5), embryo.map.createPoint(lon - 0.5, lat + 0.5)];
                //var square = OpenLayers.Geometry.Polygon.createRegularPolygon(location, 100, 4, 0);
                var square = new OpenLayers.Geometry.LinearRing(points);
                var feature = new OpenLayers.Feature.Vector(square, {level : level, obs : d.obs[index]});
                features.push(feature);
                console.log('Ice thickness: ' + lats[d.lat] + ':' + lons[d.lon] + ' - ' + d.obs[index]);
            }
        }
        return features;
    };

    this.drawSpeed = function(prognosis, time) {
        var indexEast = prognosis.variables['Ice speed east'];
        var indexNorth = prognosis.variables['Ice speed north'];
        var lats = prognosis.metadata.lat;
        var lons = prognosis.metadata.lon;
        var features = [];
        var half = 0.2;
        
        for(var data in prognosis.data) {
            var d = prognosis.data[data];
            var east = d.obs[indexEast];
            var north = d.obs[indexNorth];
            if(time == d.time && (east || north)) {
                var level = this.getConcentrationLevel(Math.sqrt(north * north + east * east));
                
                var lat = lats[d.lat];
                var lon = lons[d.lon];
//                var origin = {x : lon - east, y : lat - north};
//                var dest = {x : lon + east, y : lat + north};
//                var points = [embryo.map.createPoint(lon - east, lat - north), embryo.map.createPoint(lon + east, lat + north)];
                var points = new Array(embryo.map.createPoint(lon, lat + half), embryo.map.createPoint(lon + half, lat + (half * 0.2)), embryo.map.createPoint(
                        lon + (half * 0.2), lat + (half * 0.2)), embryo.map.createPoint(lon + (half * 0.2), lat - (half * 0.5)), embryo.map.createPoint(lon - (half * 0.2), lat - (half * 0.5)), embryo.map.createPoint(
                        lon - (half * 0.2), lat + (half * 0.2)), embryo.map.createPoint(lon - half, lat + (half * 0.2)));
                
                var rad = Math.acos(north / Math.sqrt(north * north + east * east));
                var factor = north > 0 ? 1 : -1;
                var degrees = Math.round(factor * rad * 180 / Math.PI);
                
                var linearRing = new OpenLayers.Geometry.LinearRing(points);
                linearRing.rotate(degrees, embryo.map.createPoint(lon, lat));
                var feature = new OpenLayers.Feature.Vector(linearRing, {level : level, obs : east + '/' + north});
                features.push(feature);
                
                console.log('Ice speed: ' + lat + ':' + lon + ' - ' + east + '/' + north + '; cos: ' + Math.acos(north / Math.sqrt(north * north + east * east) ));
            }
        }
        return features;
    };

    this.draw = function(prognosis, time, mapType) {
        var that = this;
        that.clear();


        var features = [];
        switch (mapType) {
        case 'iceConcentration':
            features = this.drawConcentration(prognosis, time);
            break;
        case 'iceThickness':
            features = this.drawThickness(prognosis, time);
            break;
        case 'iceSpeed':
            features = this.drawSpeed(prognosis, time);
            break;
        }

        function drawFeature(lat, lon, time, obs) {
            if (obs && obs[0]) {
                var points = new Array(embryo.map.createPoint(lon - 0.25, lat - 0.2), embryo.map.createPoint(lon + 0.05, lat + 0.2), embryo.map.createPoint(
                        lon, lat + 0.25), embryo.map.createPoint(lon + 0.25, lat + 0.25), embryo.map.createPoint(lon + 0.25, lat), embryo.map.createPoint(
                        lon + 0.2, lat + 0.05), embryo.map.createPoint(lon - 0.2, lat - 0.25));
                var linearRing = new OpenLayers.Geometry.LinearRing(points);

                var feature = new OpenLayers.Feature.Vector(linearRing);

                return feature;
            }
            return null;
        }

        that.layers.iceprognoses.addFeatures(features);
        that.layers.iceprognoses.refresh();

    };
    
    this.wipe = function() {
        this.clear();
        this.layers.iceprognoses.refresh();
    };
}

IcePrognosesLayer.prototype = new EmbryoLayer();
