(function () {
    "use strict";

    var module = angular.module('embryo.sar.service', []);

    embryo.sar = {}
    // A way to create an enumeration like construction in JavaScript
    embryo.sar.types = Object.freeze({"RapidResponse": "rr", "DatumPoint": "dp", "DatumLine": "dl", "BackTrack": "bt"})

    function Leeway(x, y, divergence, text) {
        this.x = x;
        this.y = y;
        this.divergence = divergence;
        this.text = text;
    }

    Leeway.prototype.leewaySpeed = function (leewaySpeed) {
        var result = this.x * leewaySpeed;
        if (this.y) {
            result += this.y;
        }
        return result;
    }

    function Direction(name, degrees) {
        this.name = name;
        this.degrees = degrees;
    }

    var leewayObjectTypes = [];
    leewayObjectTypes.push(Object.freeze(new Leeway(0.011, 0.068, 30, "Person in water (PIW)")));
    leewayObjectTypes.push(Object.freeze(new Leeway(0.029, 0.039, 20, "Raft (4-6 person), unknown drift anker status")));
    leewayObjectTypes.push(Object.freeze(new Leeway(0.018, 0.027, 16, "Raft (4-6 person) with drift anker")));
    leewayObjectTypes.push(Object.freeze(new Leeway(0.038, -0.041, 20, "Raft (4-6 person) without drift anker")));
    leewayObjectTypes.push(Object.freeze(new Leeway(0.036, -0.086, 14, "Raft (15-25 person), unknown drift anker status")));
    leewayObjectTypes.push(Object.freeze(new Leeway(0.031, -0.070, 12, "Raft (15-25 person) with drift anker")));
    leewayObjectTypes.push(Object.freeze(new Leeway(0.039, -0.060, 12, "Raft (15-25 person) without drift anker")));
    leewayObjectTypes.push(Object.freeze(new Leeway(0.034, 0.040, 22, "Dinghy (Flat buttom)")));
    leewayObjectTypes.push(Object.freeze(new Leeway(0.030, 0.080, 15, "Dinghy (Keel)")));
    leewayObjectTypes.push(Object.freeze(new Leeway(0.017, undefined, 15, "Dinghy (Capsized)")));
    leewayObjectTypes.push(Object.freeze(new Leeway(0.011, 0.240, 15, "Kayak with Person")));
    leewayObjectTypes.push(Object.freeze(new Leeway(0.020, undefined, 15, "Surfboard with Person")));
    leewayObjectTypes.push(Object.freeze(new Leeway(0.023, 0.100, 12, "Windsurfer with Person. Mast and sail in water")));
    leewayObjectTypes.push(Object.freeze(new Leeway(0.030, undefined, 48, "Sailboat (Long keel)")));
    leewayObjectTypes.push(Object.freeze(new Leeway(0.040, undefined, 48, "Sailboat (Fin keel)")));
    leewayObjectTypes.push(Object.freeze(new Leeway(0.069, -0.080, 19, "Motorboat")));
    leewayObjectTypes.push(Object.freeze(new Leeway(0.042, undefined, 48, "Fishing Vessel")));
    leewayObjectTypes.push(Object.freeze(new Leeway(0.040, undefined, 33, "Trawler")));
    leewayObjectTypes.push(Object.freeze(new Leeway(0.028, undefined, 48, "Coaster")));
    leewayObjectTypes.push(Object.freeze(new Leeway(0.020, undefined, 10, "Wreckage")));

    var directions = [];
    directions.push(new Direction("N", 0));
    directions.push(new Direction("NNE", 22.5));
    directions.push(new Direction("NE", 45));
    directions.push(new Direction("ENE", 67.5));
    directions.push(new Direction("E", 90));
    directions.push(new Direction("ESE", 112.50));
    directions.push(new Direction("SE", 135.00));
    directions.push(new Direction("SSE", 157.50));
    directions.push(new Direction("S", 180.00));
    directions.push(new Direction("SSW", 202.50));
    directions.push(new Direction("SW", 225.00));
    directions.push(new Direction("WSW", 247.50));
    directions.push(new Direction("W", 270.00));
    directions.push(new Direction("WNW", 292.50));
    directions.push(new Direction("NW", 315.00));
    directions.push(new Direction("NNW", 337.50));
    directions = Object.freeze(directions);

    function directionDegrees(value) {
        if (typeof value !== 'string') {
            return value;
        }
        for (var index in directions) {
            if (directions[index].name === value) {
                return directions[index].degrees;
            }
        }
        return parseInt(value, 10);
    }


    function Operation() {
    }

    Operation.prototype.setData = function (data) {
        // TODO validate general purpose data
        this.data = data;
    }

    function RapidResponse(data) {
        //that.validate(data);
        this.setData(data);
        this.calculate();
    }

    RapidResponse.prototype = new Operation();
    RapidResponse.prototype.calculate = function () {
        assertObjectFieldValue(this.data, "startTs");
        assertObjectFieldValue(this.data.lastKnownPosition, "ts");
        assertObjectFieldValue(this.data.lastKnownPosition, "lon");
        assertObjectFieldValue(this.data.lastKnownPosition, "lat");
        assertObjectFieldValue(this.data, "xError");
        assertObjectFieldValue(this.data, "yError");
        assertObjectFieldValue(this.data, "safetyFactor");

        var difference = (this.data.startTs - this.data.lastKnownPosition.ts) / 60 / 60 / 1000;
        this.timeElapsed = difference;

        this.hoursElapsed = Math.floor(difference);
        this.minutesElapsed = Math.round((difference - this.hoursElapsed) * 60);

        var startTs = this.data.lastKnownPosition.ts;

        var datumPositions = [];
        var currentPositions = [];

        var validFor = null;
        var lastDatumPosition = null

        var nmToMeters = embryo.geo.Converter.nmToMeters;

        var i = 0;
        for (i = 0; i < this.data.surfaceDriftPoints.length; i++) {
            assertObjectFieldValue(this.data.surfaceDriftPoints[i], "ts");
            assertObjectFieldValue(this.data.surfaceDriftPoints[i], "twcSpeed");
            assertObjectFieldValue(this.data.surfaceDriftPoints[i], "twcDirection");
            assertObjectFieldValue(this.data.surfaceDriftPoints[i], "leewaySpeed");
            assertObjectFieldValue(this.data.surfaceDriftPoints[i], "leewayDirection");

            // Do we have a next?
            // How long is the data point valid for?
            // Is it the last one?
            if (i == this.data.surfaceDriftPoints.length - 1) {
                // It's the last one - let it last the remainder
                validFor = (this.data.startTs - startTs) / 60 / 60 / 1000;
            } else {
                var currentTs = this.data.surfaceDriftPoints[i].ts;
                if (currentTs < this.data.lastKnownPosition.ts) {
                    currentTs = this.data.lastKnownPosition.ts;
                }
                startTs = this.data.surfaceDriftPoints[i + 1].ts;
                validFor = (startTs - currentTs) / 60 / 60 / 1000;
            }

            var currentTWC = this.data.surfaceDriftPoints[i].twcSpeed * validFor;

            var startingLocation = null;

            if (i == 0) {
                startingLocation = new embryo.geo.Position(this.data.lastKnownPosition.lon, this.data.lastKnownPosition.lat);
            } else {
                startingLocation = lastDatumPosition;
            }
            var twcDirectionInDegrees = directionDegrees(this.data.surfaceDriftPoints[i].twcDirection);
            var currentPos = startingLocation.transformPosition(twcDirectionInDegrees, nmToMeters(currentTWC));
            currentPositions.push(currentPos)

            var leewaySpeed = this.data.searchObject.leewaySpeed(this.data.surfaceDriftPoints[i].leewaySpeed);
            var leewayDriftDistance = leewaySpeed * validFor;

            var downWind = this.data.surfaceDriftPoints[i].downWind
            if (!downWind) {
                downWind = directionDegrees(this.data.surfaceDriftPoints[i].leewayDirection) - 180;
            }

            var leewayPos = currentPos.transformPosition(downWind, nmToMeters(leewayDriftDistance));
            datumPositions.push(leewayPos);
            lastDatumPosition = leewayPos;
        }

        this.datum = lastDatumPosition;
        this.windList = datumPositions;
        this.currentList = currentPositions;

        if (datumPositions.length > 1) {
            var pos = datumPositions[datumPositions.length - 2];
            this.rdv = calculateRdv(pos, lastDatumPosition, validFor);
        } else {
            var lastKnownPosition = new embryo.geo.Position(this.data.lastKnownPosition.lon, this.data.lastKnownPosition.lat);
            this.rdv = calculateRdv(lastKnownPosition, lastDatumPosition, this.timeElapsed);
        }

        this.radius = ((this.data.xError + this.data.yError) + 0.3 * this.rdv.distance) * this.data.safetyFactor;

        this.searchArea = this.calculateSearchArea(this.datum, this.radius, this.rdv.direction);
    }

    RapidResponse.prototype.calculateSearchArea = function (datum, radius, rdvDirection) {
        var nmToMeters = embryo.geo.Converter.nmToMeters;
        var reverseDirection = embryo.geo.reverseDirection;

        assertValue(datum.lat, "datum.lat")
        assertValue(datum.lon, "datum.lon")
        assertValue(radius, "radius")
        assertValue(rdvDirection, "rdvDirection")

        // Search box
        // The box is square around the circle, with center point at datum
        // Radius is the calculated Radius
        // data.getRdvDirection()
        var verticalDirection = rdvDirection;
        var horizontalDirection = verticalDirection + 90;

        if (horizontalDirection > 360) {
            horizontalDirection = horizontalDirection - 360;
        }

        // First top side of the box
        var topCenter = datum.transformPosition(verticalDirection, nmToMeters(radius));

        // Bottom side of the box
        var bottomCenter = datum.transformPosition(reverseDirection(verticalDirection), nmToMeters(radius));

        // Go left radius length
        var a = topCenter.transformPosition(reverseDirection(horizontalDirection), nmToMeters(radius));
        var b = topCenter.transformPosition(horizontalDirection, nmToMeters(radius));
        var c = bottomCenter.transformPosition(horizontalDirection, nmToMeters(radius));
        var d = bottomCenter.transformPosition(reverseDirection(horizontalDirection), nmToMeters(radius));

        return {
            A: a,
            B: b,
            C: c,
            D: d,
            totalSize: radius * radius * 4
        }
    };

    function calculateRdv(fromPosition, toPosition, timebetweenInHours) {
        var rdv = {}
        rdv.direction = fromPosition.bearingTo(toPosition, embryo.geo.Heading.RL);
        rdv.distance = fromPosition.distanceTo(toPosition, embryo.geo.Heading.RL);
        rdv.speed = rdv.distance / timebetweenInHours;
        return rdv;
    }


    function DatumPoint(data) {
        this.setData(data);
        this.calculate();
    }
    DatumPoint.prototype = new Operation();
    DatumPoint.prototype.calculate = function () {
        assertObjectFieldValue(this.data, "startTs");
        assertObjectFieldValue(this.data.lastKnownPosition, "ts");
        assertObjectFieldValue(this.data.lastKnownPosition, "lon");
        assertObjectFieldValue(this.data.lastKnownPosition, "lat");
        assertObjectFieldValue(this.data, "xError");
        assertObjectFieldValue(this.data, "yError");
        assertObjectFieldValue(this.data, "safetyFactor");

        var difference = (this.data.startTs - this.data.lastKnownPosition.ts) / 60 / 60 / 1000;
        this.timeElapsed = difference;

        this.hoursElapsed = Math.floor(difference);
        this.minutesElapsed = Math.round((difference - this.hoursElapsed) * 60);

        var startTs = this.data.lastKnownPosition.ts;

        var datumPositions = [];
        var currentPositions = [];

        var validFor = null;
        var lastDatumPosition = null

        var nmToMeters = embryo.geo.Converter.nmToMeters;

        var i;
        for (i = 0; i < this.data.surfaceDriftPoints.length; i++) {
            assertObjectFieldValue(this.data.surfaceDriftPoints[i], "ts");
            assertObjectFieldValue(this.data.surfaceDriftPoints[i], "twcSpeed");
            assertObjectFieldValue(this.data.surfaceDriftPoints[i], "twcDirection");
            assertObjectFieldValue(this.data.surfaceDriftPoints[i], "leewaySpeed");
            assertObjectFieldValue(this.data.surfaceDriftPoints[i], "leewayDirection");

            // Do we have a next?
            // How long is the data point valid for?
            // Is it the last one?
            if (i == this.data.surfaceDriftPoints.length - 1) {
                // It's the last one - let it last the remainder
                validFor = (this.data.startTs - startTs) / 60 / 60 / 1000;
            } else {
                var currentTs = this.data.surfaceDriftPoints[i].ts;
                if (currentTs < this.data.lastKnownPosition.ts) {
                    currentTs = this.data.lastKnownPosition.ts;
                }
                startTs = this.data.surfaceDriftPoints[i + 1].ts;
                validFor = (startTs - currentTs) / 60 / 60 / 1000;
            }

            var currentTWC = this.data.surfaceDriftPoints[i].twcSpeed * validFor;

            var startingLocation = null;

            if (i == 0) {
                startingLocation = new embryo.geo.Position(this.data.lastKnownPosition.lon, this.data.lastKnownPosition.lat);
            } else {
                startingLocation = lastDatumPosition;
            }
            var twcDirectionInDegrees = directionDegrees(this.data.surfaceDriftPoints[i].twcDirection);
            var currentPos = startingLocation.transformPosition(twcDirectionInDegrees, nmToMeters(currentTWC));
            currentPositions.push(currentPos)

            var leewaySpeed = this.data.searchObject.leewaySpeed(this.data.surfaceDriftPoints[i].leewaySpeed);
            var leewayDriftDistance = leewaySpeed * validFor;

            var downWind = this.data.surfaceDriftPoints[i].downWind
            if (!downWind) {
                downWind = directionDegrees(this.data.surfaceDriftPoints[i].leewayDirection) - 180;
            }

            var leewayPos = currentPos.transformPosition(downWind, nmToMeters(leewayDriftDistance));
            datumPositions.push(leewayPos);
            lastDatumPosition = leewayPos;
        }

        this.datum = lastDatumPosition;
        this.windList = datumPositions;
        this.currentList = currentPositions;

        this.rdv = {}

        if (datumPositions.length > 1) {
            var pos = datumPositions[datumPositions.length - 2];
            this.rdv.direction = pos.bearingTo(lastDatumPosition, embryo.geo.Heading.RL);
            this.rdv.distance = pos.distanceTo(lastDatumPosition, embryo.geo.Heading.RL);
            // RDV Speed
            this.rdv.speed = this.rdv.distance / validFor;
        } else {
            var lastKnownPosition = new embryo.geo.Position(this.data.lastKnownPosition.lon, this.data.lastKnownPosition.lat);
            this.rdv.direction = lastKnownPosition.bearingTo(lastDatumPosition, embryo.geo.Heading.RL);
            this.rdv.distance = lastKnownPosition.distanceTo(lastDatumPosition, embryo.geo.Heading.RL);
            // RDV Speed
            this.rdv.speed = this.rdv.distance / this.timeElapsed;
        }

        this.radius = ((this.data.xError + this.data.yError) + 0.3 * this.rdv.distance) * this.data.safetyFactor;

        this.searchArea = this.calculateSearchArea(this.datum, this.radius, this.rdv.direction);
    }

    function DatumLine(data) {
        this.setData(data);
        this.calculate();
    }

    DatumLine.prototype = new Operation();

    function BackTrack(data) {
        this.setData(data);
        this.calculate();
    }

    BackTrack.prototype = new Operation();

    module.service('SarService', function () {
        var selectedSar;
        var listeners = {};

        function notifyListeners() {
            for (var key in listeners) {
                listeners[key](selectedSar);
            }
        }

        var service = {
            sarTypes: function () {
                return embryo.sar.types;
            },
            directions: function () {
                return directions;
            },
            queryDirections: function (query) {
                var upperCased = query.toUpperCase();
                var result = []
                for (var index in directions) {
                    if (directions[index].name.indexOf(upperCased) >= 0) {
                        result.push(directions[index]);
                    }
                }
                return result;
            },
            searchObjectTypes: function () {
                return leewayObjectTypes;
            },
            selectedSar: function (sar) {
                selectedSar = sar;
                notifyListeners();
            },
            registerSelectedSarListener: function (name, fn) {
                listeners[name] = fn;

                if (selectedSar) {
                    fn(selectedSar);
                }
            },
            createSarOperation: function (data) {
                if (!data.type) {
                    throw new Error("type must be specified");
                }

                switch (data.type) {
                    case (embryo.sar.types.RapidResponse) :
                        return new RapidResponse(data);
                    case (embryo.sar.types.DatumPoint) :
                        return new DatumPoint(data);
                    case (embryo.sar.types.DatumLine) :
                        return new DatumLine(data);
                    case (embryo.sar.types.BackTrack) :
                        return new BackTrack(data);
                    default :
                        throw new Error("Unknown sar type " + data.type);
                }
            }


        };

        return service;
    });

})();
