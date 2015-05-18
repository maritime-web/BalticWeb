function SarLayer() {

    var nmToMeters = embryo.geo.Converter.nmToMeters;

    var that = this;

    that.zoomLevels = [8, 12];

    this.init = function () {
        var context = {
            color: function (feature) {
                if (feature.attributes.type === 'dv') {
                    return "black";
                }
                return "green"
            },
            strokeWidth: function () {
                return that.zoomLevel >= 1 ? 2 : 1;
            },
            strokeOpacity: function (feature) {
                if (feature.attributes.type === 'dv') {
                    return 0.8;
                }
                return 0.7;
            },
            label: function (feature) {
                if (feature.attributes.type == "areaLabel") {
                    return that.zoomLevel >= 1 ? feature.attributes.label : "";
                }
                if (feature.attributes.type == "circleLabel" || feature.attributes.type == "lkpLabel") {
                    return that.zoomLevel >= 2 ? feature.attributes.label : "";
                }
                return feature.attributes.label ? feature.attributes.label : "";
            }
        };

        this.layers.lines = new OpenLayers.Layer.Vector("SAR Layer", {
            renderers: ['SVGExtended', 'VMLExtended', 'CanvasExtended'],
            styleMap: new OpenLayers.StyleMap({
                "default": new OpenLayers.Style({
                    orientation: true,
                    fillColor: "${color}",
                    fillOpacity: 0.3,
                    strokeWidth: "${strokeWidth}",
                    strokeColor: "${color}",
                    strokeOpacity: "${strokeOpacity}",
                    label: "${label}"
                }, {
                    context: context
                })

            })
        });
    };

    function createSearchArea(searchArea) {
        var features = [];
        var pointA = embryo.map.createPoint(searchArea.A.lon, searchArea.A.lat);
        var pointB = embryo.map.createPoint(searchArea.B.lon, searchArea.B.lat);
        var pointC = embryo.map.createPoint(searchArea.C.lon, searchArea.C.lat);
        var pointD = embryo.map.createPoint(searchArea.D.lon, searchArea.D.lat);
        var square = new OpenLayers.Geometry.LinearRing([pointA, pointB, pointC, pointD]);
        features.push(new OpenLayers.Feature.Vector(square, {
            type: "area"
        }));
        features.push(new OpenLayers.Feature.Vector(pointA, {
            type: "areaLabel",
            label: "A"
        }));
        features.push(new OpenLayers.Feature.Vector(pointB, {
            type: "areaLabel",
            label: "B"
        }));
        features.push(new OpenLayers.Feature.Vector(pointC, {
            type: "areaLabel",
            label: "C"
        }));
        features.push(new OpenLayers.Feature.Vector(pointD, {
            type: "areaLabel",
            label: "D"
        }));

        return features;
    }

    function addDriftVector(layer, positions) {
        var points = []
        var length = positions.length;
        for (var i = 0; i < length; i++) {
            points.push(embryo.map.createPoint(positions[i].lon, positions[i].lat));
        }
        var features = [new OpenLayers.Feature.Vector(new OpenLayers.Geometry.LineString(points), {
            renderers: ['SVGExtended', 'VMLExtended', 'CanvasExtended'],
            type: "dv"
        })];
        layer.addFeatures(features);
    }

    function addLKP(layer, lkp) {
        var features = [new OpenLayers.Feature.Vector(embryo.map.createPoint(lkp.lon, lkp.lat), {
            label: "LKP",
            type: "lkpLabel"
        })];
        layer.addFeatures(features);
    }

    function prepareDriftVectors(lkp, twcPositions, leewayPositions) {
        var points = []
        if (lkp) {
            points.push(lkp);
        }
        var length = twcPositions ? twcPositions.length : 0;
        for (var i = 0; i < length; i++) {
            points.push(twcPositions[i]);
            points.push(leewayPositions[i])
        }
        return points;
    }

    this.containsDistanceCircle = function (vessel) {
        function featureFilter(feature) {
            return feature.attributes.id === vessel.mmsi && feature.attributes.type === 'circle';
        }

        return this.containsFeature(featureFilter, this.layers.lines);
    };

    this.containsNearestVessel = function (vessel) {
        function featureFilter(feature) {
            return feature.attributes.id === vessel.mmsi && feature.attributes.type === 'nearest';
        }

        return this.containsFeature(featureFilter, this.layers.lines);
    };

    function addSearchRing(features, circle, label) {
        var radiusInKm = nmToMeters(circle.radius) / 1000;
        features.addFeatures(embryo.adt.createRing(circle.datum.lon, circle.datum.lat, radiusInKm, 1, undefined, 'circle'));

        var center = embryo.map.createPoint(circle.datum.lon, circle.datum.lat);
        features.addFeatures(new OpenLayers.Feature.Vector(center, {
            type: 'circleLabel',
            label: label
        }));
    }

    function addRdv(layer, lkp, datum) {
        addDriftVector(layer, [lkp, datum]);
    }

    this.draw = function (sar) {
        this.layers.lines.removeAllFeatures();

        if (sar.output.datum) {
            addSearchRing(this.layers.lines, sar.output, "Datum");

            this.layers.lines.addFeatures(createSearchArea(sar.output.searchArea));

            addLKP(this.layers.lines, sar.input.lastKnownPosition);
            addRdv(this.layers.lines, sar.input.lastKnownPosition, sar.output.datum);
            addDriftVector(this.layers.lines, prepareDriftVectors(sar.input.lastKnownPosition, sar.output.currentPositions, sar.output.windPositions))
        } else if (sar.output.downWind) {
            addSearchRing(this.layers.lines, sar.output.downWind, "Datum down wind");
            addSearchRing(this.layers.lines, sar.output.min, "Datum min");
            addSearchRing(this.layers.lines, sar.output.max, "Datum max");

            this.layers.lines.addFeatures(createSearchArea(sar.output.searchArea));
            /*
             this.layers.lines.addFeatures(createSearchArea(sar.output.searchArea2), {
             type: 'area'
             });
             */
            addLKP(this.layers.lines, sar.input.lastKnownPosition);
            addRdv(this.layers.lines, sar.input.lastKnownPosition, sar.output.downWind.datum);
            addRdv(this.layers.lines, sar.input.lastKnownPosition, sar.output.min.datum);
            addRdv(this.layers.lines, sar.input.lastKnownPosition, sar.output.max.datum);
            addDriftVector(this.layers.lines, prepareDriftVectors(sar.input.lastKnownPosition, sar.output.currentPositions, sar.output.downWind.datumPositions))
            addDriftVector(this.layers.lines, prepareDriftVectors(null, sar.output.currentPositions, sar.output.min.datumPositions))
            addDriftVector(this.layers.lines, prepareDriftVectors(null, sar.output.currentPositions, sar.output.max.datumPositions))
        }

        this.layers.lines.refresh();

    };
}

SarLayer.prototype = new EmbryoLayer();

/*
 * Can be used to create only one distance layer instance and reuse this as
 */
var SarLayerSingleton = {
    instance: null,
    getInstance: function () {
        return this.instance;
    }
}

embryo.postLayerInitialization(function () {
    SarLayerSingleton.instance = new SarLayer();
    addLayerToMap("sar", SarLayerSingleton.instance, embryo.map);
})


