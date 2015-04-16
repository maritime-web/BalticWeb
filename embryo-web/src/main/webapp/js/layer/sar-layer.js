function SarLayer() {

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
                    strokeOpacity: "${strokeOpacity}"
                }, {
                    context: context
                })

            })
        });
    };

    function createSearchArea(searchArea) {
        var points = [embryo.map.createPoint(searchArea.A.lon, searchArea.A.lat),
            embryo.map.createPoint(searchArea.B.lon, searchArea.B.lat),
            embryo.map.createPoint(searchArea.C.lon, searchArea.C.lat),
            embryo.map.createPoint(searchArea.D.lon, searchArea.D.lat)];
        var square = new OpenLayers.Geometry.LinearRing(points);
        var feature = new OpenLayers.Feature.Vector(square, {
            type: "area"
        });
        return feature;
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

    this.draw = function (sar) {
        this.layers.lines.removeAllFeatures();

        if (sar.datum) {
            var radiusMeters = embryo.geo.Converter.nmToMeters(sar.radius) / 1000;
            this.layers.lines.addFeatures(embryo.adt.createRing(sar.datum.lon, sar.datum.lat, radiusMeters, 1, undefined, 'circle'), {
                type: 'circle'
            });

            this.layers.lines.addFeatures(createSearchArea(sar.searchArea), {
                type: 'area'
            });

            this.layers.lines.addFeatures(createRDV(sar.data.lastKnownPosition, sar.datum), {
                type: 'rdv'
            });
            this.layers.lines.addFeatures(createTwcLeewayVectors(sar.data.lastKnownPosition, sar.currentPositions, sar.windPositions), {
                type: 'rdv'
            });
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
        if (this.instance == null) {
            this.instance = new SarLayer();
            addLayerToMap("sar", this.instance, embryo.map);
        }
        return this.instance;
    }
}
