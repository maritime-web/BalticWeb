function PrognosesLayer() {

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
                return feature.cluster ? feature.cluster.length + " Prognoses locations" : feature.attributes.iceDescription.Number + ": "
                        + feature.attributes.iceDescription.Placename;
            },
            display : function(feature) {
                return "yes";
            }
        };

        this.layers.prognoses = new OpenLayers.Layer.Vector("Prognoses", {
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

        this.selectableLayers = [ this.layers.prognoses ];
        this.selectableAttribute = "number";
    };

    this.getIceConcentrationLevel = function(obs) {
        if (obs < 0.0001) {
            return '#00DE00';
        } else if (obs < 0.001) {
            return '#FFFF00';
        } else if (obs < 0.01) {
            return '#FA4242';
        } else {
            return '#E8B332';
        }
    };

    this.drawConcentration = function(prognosis, time) {
        var index = prognosis.variables['Ice concentration'];
        var lats = prognosis.metadata.lat;
        var lons = prognosis.metadata.lon;
        var features = [];

        for ( var data in prognosis.data) {
            var d = prognosis.data[data];
            if (time == d.time && d.obs[index]) {
                var level = this.getIceConcentrationLevel(d.obs[index]);
                var lat = lats[d.lat];
                var lon = lons[d.lon];
                // var location = embryo.map.createPoint(lons[d.lon],
                // lats[d.lat]);
                var half = 0.2;
                var points = [ embryo.map.createPoint(lon - half, lat - half), embryo.map.createPoint(lon + half, lat - half),
                        embryo.map.createPoint(lon + half, lat + half), embryo.map.createPoint(lon - half, lat + half) ];
                // var square =
                // OpenLayers.Geometry.Polygon.createRegularPolygon(location,
                // 100, 4, 0);
                var square = new OpenLayers.Geometry.LinearRing(points);
                var feature = new OpenLayers.Feature.Vector(square, {
                    level : level,
                    obs : d.obs[index]
                });
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

        for ( var data in prognosis.data) {
            var d = prognosis.data[data];
            if (time == d.time && d.obs[index]) {
                var level = this.getIceConcentrationLevel(d.obs[index]);
                var lat = lats[d.lat];
                var lon = lons[d.lon];
                // var location = embryo.map.createPoint(lons[d.lon],
                // lats[d.lat]);
                var half = 0.2;
                var points = [ embryo.map.createPoint(lon - half, lat - half), embryo.map.createPoint(lon + half, lat - half),
                        embryo.map.createPoint(lon + half, lat + half), embryo.map.createPoint(lon - half, lat + half) ];
                // var square =
                // OpenLayers.Geometry.Polygon.createRegularPolygon(location,
                // 100, 4, 0);
                var square = new OpenLayers.Geometry.LinearRing(points);
                var feature = new OpenLayers.Feature.Vector(square, {
                    level : level,
                    obs : d.obs[index]
                });
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

        for ( var data in prognosis.data) {
            var d = prognosis.data[data];
            var east = d.obs[indexEast];
            var north = d.obs[indexNorth];
            if (time == d.time && (east || north)) {
                var level = this.getIceConcentrationLevel(Math.sqrt(north * north + east * east));

                var lat = lats[d.lat];
                var lon = lons[d.lon];
                // var origin = {x : lon - east, y : lat - north};
                // var dest = {x : lon + east, y : lat + north};
                // var points = [embryo.map.createPoint(lon - east, lat -
                // north), embryo.map.createPoint(lon + east, lat + north)];
                var points = new Array(embryo.map.createPoint(lon, lat + half), embryo.map.createPoint(lon + half, lat + (half * 0.2)), embryo.map.createPoint(
                        lon + (half * 0.2), lat + (half * 0.2)), embryo.map.createPoint(lon + (half * 0.2), lat - (half * 0.5)), embryo.map.createPoint(lon
                        - (half * 0.2), lat - (half * 0.5)), embryo.map.createPoint(lon - (half * 0.2), lat + (half * 0.2)), embryo.map.createPoint(lon - half,
                        lat + (half * 0.2)));

                var rad = Math.acos(north / Math.sqrt(north * north + east * east));
                var degrees = Math.round(rad * 180 / Math.PI);

                var linearRing = new OpenLayers.Geometry.LinearRing(points);
                linearRing.rotate(degrees, embryo.map.createPoint(lon, lat));
                var feature = new OpenLayers.Feature.Vector(linearRing, {
                    level : level,
                    obs : east + '/' + north
                });
                features.push(feature);

                console.log('Ice speed: ' + lat + ':' + lon + ' - ' + east + '/' + north + '; cos: '
                        + Math.acos(north / Math.sqrt(north * north + east * east)));
            }
        }
        return features;
    };

    this.drawIcePrognosis = function(prognosis, time, mapType) {
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

        that.layers.prognoses.addFeatures(features);
        that.layers.prognoses.refresh();

    };

    this.getWaveConcentrationLevel = function(obs) {
        if (obs < 0.7) {
            return '#00DE00';
        } else if (obs < 1.0) {
            return '#FFFF00';
        } else if (obs < 1.3) {
            return '#FA4242';
        } else {
            return '#E8B332';
        }
    };

    this.drawWavePrognosisFeature = function(lat, lon, obs, vars) {
        var half = 0.2;
        if (obs && obs[0]) {
            var level = this.getWaveConcentrationLevel(obs[vars['Significant wave height']]);
            var points = new Array(embryo.map.createPoint(lon, lat + half), embryo.map.createPoint(lon + half, lat + (half * 0.2)), embryo.map.createPoint(lon
                    + (half * 0.2), lat + (half * 0.2)), embryo.map.createPoint(lon + (half * 0.2), lat - (half * 0.5)), embryo.map.createPoint(lon
                    - (half * 0.2), lat - (half * 0.5)), embryo.map.createPoint(lon - (half * 0.2), lat + (half * 0.2)), embryo.map.createPoint(lon - half, lat
                    + (half * 0.2)));
            var linearRing = new OpenLayers.Geometry.LinearRing(points);
            linearRing.rotate(obs[vars['Wave direction']], embryo.map.createPoint(lon, lat));
            var feature = new OpenLayers.Feature.Vector(linearRing, {
                level : level
            });
            console.log('Sign. wave height: ' + obs[vars['Significant wave height']]);
            return feature;
        }
        return null;
    };

    this.drawWavePrognosis = function(prognosis, time) {
        var that = this;
        that.clear();

        var vars = prognosis.variables;
        var lats = prognosis.metadata.lat;
        var lons = prognosis.metadata.lon;
        var features = [];

        for ( var d in prognosis.data) {
            var data = prognosis.data[d];
            if (time == data.time) {
                var feature = this.drawWavePrognosisFeature(lats[data.lat], lons[data.lon], data.obs, vars);
                if (feature) {
                    features.push(feature);
                }
            }
        }

        that.layers.prognoses.addFeatures(features);
        that.layers.prognoses.refresh();
    };

    this.drawCurrentPrognosis = function(prognosis, time) {
        var that = this;
        that.clear();

        var indexEast = prognosis.variables['Current east'];
        var indexNorth = prognosis.variables['Current north'];
        var lats = prognosis.metadata.lat;
        var lons = prognosis.metadata.lon;
        var features = [];
        var half = 0.2;

        for ( var data in prognosis.data) {
            var d = prognosis.data[data];
            var east = d.obs[indexEast];
            var north = d.obs[indexNorth];
            if (time == d.time && (east || north)) {
                var level = this.getIceConcentrationLevel(Math.sqrt(north * north + east * east));

                var lat = lats[d.lat];
                var lon = lons[d.lon];
                var points = new Array(embryo.map.createPoint(lon, lat + half), embryo.map.createPoint(lon + half, lat + (half * 0.2)), embryo.map.createPoint(
                        lon + (half * 0.2), lat + (half * 0.2)), embryo.map.createPoint(lon + (half * 0.2), lat - (half * 0.5)), embryo.map.createPoint(lon
                        - (half * 0.2), lat - (half * 0.5)), embryo.map.createPoint(lon - (half * 0.2), lat + (half * 0.2)), embryo.map.createPoint(lon - half,
                        lat + (half * 0.2)));

                var rad = Math.acos(north / Math.sqrt(north * north + east * east));
                var degrees = Math.round(rad * 180 / Math.PI);

                var linearRing = new OpenLayers.Geometry.LinearRing(points);
                linearRing.rotate(degrees, embryo.map.createPoint(lon, lat));
                var feature = new OpenLayers.Feature.Vector(linearRing, {
                    level : level,
                    obs : east + '/' + north
                });
                features.push(feature);

                console.log('Current speed: ' + lat + ':' + lon + ' - ' + east + '/' + north + '; cos: '
                        + Math.acos(north / Math.sqrt(north * north + east * east)));
            }
        }
        that.layers.prognoses.addFeatures(features);
        that.layers.prognoses.refresh();
    };

}

PrognosesLayer.prototype = new EmbryoLayer();
