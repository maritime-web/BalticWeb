function SarLayer() {

    var nmToMeters = embryo.geo.Converter.nmToMeters;

    var that = this;

    that.zoomLevels = [12];

    this.init = function () {
        var context = {
            color: function (feature) {
                if (feature.attributes.type === 'rdv') {
                    return "black";
                }
                return "green"
            },
            strokeWidth: function (feature) {
                return that.zoomLevel >= 1 ? 2 : 1;
            },
            strokeOpacity: function (feature) {
                if (feature.attributes.type === 'rdv') {
                    return 0.8;
                }
                return 0.7;
            },
            label: function (feature) {
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
            label: "A"
        }));
        features.push(new OpenLayers.Feature.Vector(pointB, {
            label: "B"
        }));
        features.push(new OpenLayers.Feature.Vector(pointC, {
            label: "C"
        }));
        features.push(new OpenLayers.Feature.Vector(pointD, {
            label: "D"
        }));

        return features;
    }

    function createTwcLeewayVectors(lastKnownPosition, twcPositions, leewayPositions) {
        var features = [];
        var points = [embryo.map.createPoint(lastKnownPosition.lon, lastKnownPosition.lat)];
        var length = twcPositions ? twcPositions.length : 0;
        for (var i = 0; i < length; i++) {
            points.push(embryo.map.createPoint(twcPositions[i].lon, twcPositions[i].lat));
            points.push(embryo.map.createPoint(leewayPositions[i].lon, leewayPositions[i].lat));
        }
        features.push(new OpenLayers.Feature.Vector(new OpenLayers.Geometry.LineString(points), {
            renderers: ['SVGExtended', 'VMLExtended', 'CanvasExtended'],
            id: "rdv-temp",
            type: "rdv"
        }));
        return features;
    }

    function createRDV(lastKnownPosition, datum) {
        var features = [];
        var points = [embryo.map.createPoint(lastKnownPosition.lon, lastKnownPosition.lat), embryo.map.createPoint(datum.lon, datum.lat)];
        features.push(new OpenLayers.Feature.Vector(new OpenLayers.Geometry.LineString(points), {
            renderers: ['SVGExtended', 'VMLExtended', 'CanvasExtended'],
            id: "rdv",
            type: "rdv"
        }));
        return features;
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
        features.addFeatures(embryo.adt.createRing(circle.datum.lon, circle.datum.lat, radiusInKm, 1, undefined, 'circle'), {
            type: 'circle',
            label: label
        });

        var center = embryo.map.createPoint(circle.datum.lon, circle.datum.lat);
        features.addFeatures(new OpenLayers.Feature.Vector(center, {
            label: label
        }));
    }

    function addRdv(features, lkp, datum) {
        features.addFeatures(createRDV(lkp, datum), {
            type: 'rdv'
        });
    }

    function addTwcLeewayVectors(features, lkp, currentPositions, windPositions) {
        features.addFeatures(createTwcLeewayVectors(lkp, currentPositions, windPositions), {
            type: 'rdv'
        });
    }


    this.draw = function (sar) {
        this.layers.lines.removeAllFeatures();

        if (sar.output.datum) {
            addSearchRing(this.layers.lines, sar.output, "Datum");

            this.layers.lines.addFeatures(createSearchArea(sar.output.searchArea));

            addRdv(this.layers.lines, sar.input.lastKnownPosition, sar.output.datum);

            this.layers.lines.addFeatures(createTwcLeewayVectors(sar.input.lastKnownPosition, sar.output.currentPositions, sar.output.windPositions), {
                type: 'rdv'
            });
        } else if (sar.output.downWind) {
            addSearchRing(this.layers.lines, sar.output.downWind, "Datum down wind");
            addSearchRing(this.layers.lines, sar.output.min, "Datum min");
            addSearchRing(this.layers.lines, sar.output.max, "Datum max");


            this.layers.lines.addFeatures(createSearchArea(sar.output.searchArea), {
                type: 'area'
            });

            addRdv(this.layers.lines, sar.input.lastKnownPosition, sar.output.downWind.datum);
            addRdv(this.layers.lines, sar.input.lastKnownPosition, sar.output.min.datum);
            addRdv(this.layers.lines, sar.input.lastKnownPosition, sar.output.max.datum);

            addTwcLeewayVectors(this.layers.lines, sar.input.lastKnownPosition, sar.output.currentPositions, sar.output.downWind.datumPositions)
            addTwcLeewayVectors(this.layers.lines, sar.input.lastKnownPosition, sar.output.currentPositions, sar.output.min.datumPositions)
            addTwcLeewayVectors(this.layers.lines, sar.input.lastKnownPosition, sar.output.currentPositions, sar.output.max.datumPositions)

        }

        this.layers.lines.refresh();

    };

    this.drawNearestVessels = function (selectedVessel, allVessels) {
        var vessels = [];

        $.each(allVessels, function (k, v) {
            if (v.mmsi != selectedVessel.mmsi) {
                if (embryo.getMaxSpeed(v) > 0.0) {
                    distance = embryo.adt.measureDistanceGc(selectedVessel.x, selectedVessel.y, v.x, v.y);
                    var o = {
                        distance: distance,
                        timeInMinutes: (distance / (embryo.getMaxSpeed(v) * 1.852) / 60),
                        vessel: v
                    }
                    if (o.distance > 0) {
                        vessels.push(o);
                    }
                }
            }
        });

        vessels.sort(function (a, b) {
            if (a.timeInMinutes == Infinity && b.timeInMinutes == Infinity) {
                return 0;
            }
            if (a.timeInMinutes == Infinity && b.timeInMinutes != Infinity) {
                return 100;
            }
            if (a.timeInMinutes != Infinity && b.timeInMinutes == Infinity) {
                return -100;
            }

            return a.timeInMinutes - b.timeInMinutes;
        });

        for (var i = 0; i < 5; i++) {
            var toVessel = vessels[i];

            this.layers.lines.addFeatures([new OpenLayers.Feature.Vector(new OpenLayers.Geometry.LineString([
                embryo.map.createPoint(selectedVessel.x, selectedVessel.y), embryo.map.createPoint(toVessel.vessel.x, toVessel.vessel.y)]), {
                id: selectedVessel.mmsi,
                type: 'nearest'

            })]);

            var labelFeature = new OpenLayers.Feature.Vector(embryo.map.createPoint(toVessel.vessel.x, toVessel.vessel.y));
            var maxSpeedLabel;
            if (toVessel.vessel.awsog) {
                maxSpeedLabel = "AW Max Speed: " + embryo.getMaxSpeed(toVessel.vessel) + " kn";
            } else if (toVessel.vessel.ssog) {
                maxSpeedLabel = "Service Speed: " + embryo.getMaxSpeed(toVessel.vessel) + " kn";
            } else if (toVessel.vessel.sog) {
                maxSpeedLabel = "SOG: " + embryo.getMaxSpeed(toVessel.vessel) + " kn";
            }

            labelFeature.attributes = {
                id: selectedVessel.mmsi,
                type: 'nearest',
                labelXOffset: 0,
                labelYOffset: -15,
                label: toVessel.vessel.name
                + ": "
                + formatNauticalMile(toVessel.distance)
                + (embryo.getMaxSpeed(toVessel.vessel) == Infinity ? "" : ", " + formatHour(toVessel.distance / (embryo.getMaxSpeed(toVessel.vessel) * 1.852))
                + " hours, " + maxSpeedLabel)
            };
            this.layers.labels.addFeatures([labelFeature]);
        }
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


