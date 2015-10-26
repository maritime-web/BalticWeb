(function () {
    "use strict";

    var module = angular.module('embryo.sar.service', ['embryo.storageServices']);

    embryo.sar = {}
    // A way to create an enumeration like construction in JavaScript
    embryo.sar.Operation = Object.freeze({
        "RapidResponse": "rr",
        "DatumPoint": "dp",
        "DatumLine": "dl",
        "BackTrack": "bt"
    })

    embryo.sar.Type = Object.freeze({"SearchArea": "SA", "EffortAllocation": "EA", "SearchPattern": "SP"})

    embryo.SARStatus = Object.freeze({
        STARTED: "S",
        ENDED: "E"
    });

    embryo.sar.effort = {};
    embryo.sar.effort.VesselTypes = Object.freeze({
        SmallerVessel: "SV",
        Ship: "S"
    });

    embryo.sar.effort.TargetTypes = Object.freeze({
        PersonInWater: "PIW",
        Raft1Person: "R1",
        Raft4Persons: "R4",
        Raft6Persons: "R6",
        Raft8Persons: "R8",
        Raft10Persons: "R10",
        Raft15Persons: "R15",
        Raft20Persons: "R20",
        Raft25Persons: "R25",
        Motorboat15: "M15",
        Motorboat20: "M20",
        Motorboat33: "M33",
        Motorboat53: "M53",
        Motorboat78: "M78",
        Sailboat15: "SB15",
        Sailboat20: "SB20",
        Sailboat25: "SB25",
        Sailboat30: "SB30",
        Sailboat40: "SB40",
        Sailboat50: "SB50",
        Sailboat70: "SB70",
        Sailboat83: "SB83",
        Ship120: "SH120",
        Ship225: "SH225",
        Ship330: "SH330"
    });

    embryo.sar.effort.Status = Object.freeze({
        DraftSRU: "DS",
        DraftZone: "DZ",
        DraftModifiedOnMap: "DM",
        Active: "A"
    });



    function SearchObject(id, x, y, divergence, text) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.divergence = divergence;
        this.text = text;
    }

    SearchObject.prototype.leewaySpeed = function (leewaySpeed) {
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

    var searchObjectTypes = [];
    searchObjectTypes.push(Object.freeze(new SearchObject(0, 0.011, 0.068, 30, "Person in water (PIW)")));
    searchObjectTypes.push(Object.freeze(new SearchObject(1, 0.029, 0.039, 20, "Raft (4-6 person), unknown drift anker status")));
    searchObjectTypes.push(Object.freeze(new SearchObject(2, 0.018, 0.027, 16, "Raft (4-6 person) with drift anker")));
    searchObjectTypes.push(Object.freeze(new SearchObject(3, 0.038, -0.041, 20, "Raft (4-6 person) without drift anker")));
    searchObjectTypes.push(Object.freeze(new SearchObject(4, 0.036, -0.086, 14, "Raft (15-25 person), unknown drift anker status")));
    searchObjectTypes.push(Object.freeze(new SearchObject(5, 0.031, -0.070, 12, "Raft (15-25 person) with drift anker")));
    searchObjectTypes.push(Object.freeze(new SearchObject(6, 0.039, -0.060, 12, "Raft (15-25 person) without drift anker")));
    searchObjectTypes.push(Object.freeze(new SearchObject(7, 0.034, 0.040, 22, "Dinghy (Flat buttom)")));
    searchObjectTypes.push(Object.freeze(new SearchObject(8, 0.030, 0.080, 15, "Dinghy (Keel)")));
    searchObjectTypes.push(Object.freeze(new SearchObject(9, 0.017, undefined, 15, "Dinghy (Capsized)")));
    searchObjectTypes.push(Object.freeze(new SearchObject(10, 0.011, 0.240, 15, "Kayak with Person")));
    searchObjectTypes.push(Object.freeze(new SearchObject(11, 0.020, undefined, 15, "Surfboard with Person")));
    searchObjectTypes.push(Object.freeze(new SearchObject(12, 0.023, 0.100, 12, "Windsurfer with Person. Mast and sail in water")));
    searchObjectTypes.push(Object.freeze(new SearchObject(13, 0.030, undefined, 48, "Sailboat (Long keel)")));
    searchObjectTypes.push(Object.freeze(new SearchObject(14, 0.040, undefined, 48, "Sailboat (Fin keel)")));
    searchObjectTypes.push(Object.freeze(new SearchObject(15, 0.069, -0.080, 19, "Motorboat")));
    searchObjectTypes.push(Object.freeze(new SearchObject(16, 0.042, undefined, 48, "Fishing Vessel")));
    searchObjectTypes.push(Object.freeze(new SearchObject(17, 0.040, undefined, 33, "Trawler")));
    searchObjectTypes.push(Object.freeze(new SearchObject(18, 0.028, undefined, 48, "Coaster")));
    searchObjectTypes.push(Object.freeze(new SearchObject(19, 0.020, undefined, 10, "Wreckage")));

    function findSearchObjectType(id) {
        for (var index in searchObjectTypes) {
            if (searchObjectTypes[index].id === id) {
                return searchObjectTypes[index];
            }
        }
        return null;
    }

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


    function SarOperationCalculator() {
        this.output = {}
    }

    SarOperationCalculator.prototype.setInput = function (input) {
        this.input = input;
    }

    SarOperationCalculator.prototype.validate = function (input) {
        this.setInput(input)

        assertObjectFieldValue(this.input, "startTs");
        assertObjectFieldValue(this.input.lastKnownPosition, "ts");
        assertObjectFieldValue(this.input.lastKnownPosition, "lon");
        assertObjectFieldValue(this.input.lastKnownPosition, "lat");
        assertObjectFieldValue(this.input, "xError");
        assertObjectFieldValue(this.input, "yError");
        assertObjectFieldValue(this.input, "safetyFactor");

        for (var i = 0; i < this.input.surfaceDriftPoints.length; i++) {
            assertObjectFieldValue(this.input.surfaceDriftPoints[i], "ts");
            assertObjectFieldValue(this.input.surfaceDriftPoints[i], "twcSpeed");
            assertObjectFieldValue(this.input.surfaceDriftPoints[i], "twcDirection");
            assertObjectFieldValue(this.input.surfaceDriftPoints[i], "leewaySpeed");
            assertObjectFieldValue(this.input.surfaceDriftPoints[i], "leewayDirection");
        }
    }

    SarOperationCalculator.prototype.timeElapsed = function () {
        var difference = (this.input.startTs - this.input.lastKnownPosition.ts) / 60 / 60 / 1000;
        this.output.timeElapsed = difference;

        this.output.hoursElapsed = Math.floor(difference);
        this.output.minutesElapsed = Math.round((difference - this.output.hoursElapsed) * 60);
    }

    function RapidResponseCalculator() {
    }

    RapidResponseCalculator.prototype = new SarOperationCalculator();
    RapidResponseCalculator.prototype.calculate = function (input) {
        this.validate(input);

        this.timeElapsed();

        var startTs = this.input.lastKnownPosition.ts;

        var datumPositions = [];
        var currentPositions = [];

        var validFor = null;
        var lastDatumPosition = null

        var searchObject = findSearchObjectType(this.input.searchObject);

        for (var i = 0; i < this.input.surfaceDriftPoints.length; i++) {
            // Do we have a next?
            // How long is the data point valid for?
            // Is it the last one?
            if (i == this.input.surfaceDriftPoints.length - 1) {
                // It's the last one - let it last the remainder
                validFor = (this.input.startTs - startTs) / 60 / 60 / 1000;
            } else {
                var currentTs = this.input.surfaceDriftPoints[i].ts;
                if (currentTs < this.input.lastKnownPosition.ts) {
                    currentTs = this.input.lastKnownPosition.ts;
                }
                startTs = this.input.surfaceDriftPoints[i + 1].ts;
                validFor = (startTs - currentTs) / 60 / 60 / 1000;
            }

            var currentTWC = this.input.surfaceDriftPoints[i].twcSpeed * validFor;

            var startingLocation = null;

            if (i == 0) {
                startingLocation = new embryo.geo.Position(this.input.lastKnownPosition.lon, this.input.lastKnownPosition.lat);
            } else {
                startingLocation = lastDatumPosition;
            }
            var twcDirectionInDegrees = directionDegrees(this.input.surfaceDriftPoints[i].twcDirection);
            var currentPos = startingLocation.transformPosition(twcDirectionInDegrees, currentTWC);
            currentPositions.push(currentPos)

            var leewaySpeed = searchObject.leewaySpeed(this.input.surfaceDriftPoints[i].leewaySpeed);
            var leewayDriftDistance = leewaySpeed * validFor;

            var downWind = this.input.surfaceDriftPoints[i].downWind
            if (!downWind) {
                downWind = directionDegrees(this.input.surfaceDriftPoints[i].leewayDirection) - 180;
            }

            var leewayPos = currentPos.transformPosition(downWind, leewayDriftDistance);
            datumPositions.push(leewayPos);
            lastDatumPosition = leewayPos;
        }

        this.output.datum = lastDatumPosition;
        this.output.windPositions = datumPositions;
        this.output.currentPositions = currentPositions;

        if (datumPositions.length > 1) {
            var pos = datumPositions[datumPositions.length - 2];
            this.output.rdv = calculateRdv(pos, lastDatumPosition, validFor);
        } else {
            var lastKnownPosition = new embryo.geo.Position(this.input.lastKnownPosition.lon, this.input.lastKnownPosition.lat);
            this.output.rdv = calculateRdv(lastKnownPosition, lastDatumPosition, this.output.timeElapsed);
        }

        this.output.radius = ((this.input.xError + this.input.yError) + 0.3 * this.output.rdv.distance) * this.input.safetyFactor;
        this.calculateSearchArea(this.output.datum, this.output.radius, this.output.rdv.direction);

        return this.output;
    }

    RapidResponseCalculator.prototype.validateSearchAreaInput = function (datum, radius, rdvDirection) {
        assertValue(datum.lat, "datum.lat")
        assertValue(datum.lon, "datum.lon")
        assertValue(radius, "radius")
        assertValue(rdvDirection, "rdvDirection")
    }

    RapidResponseCalculator.prototype.calculateSearchArea = function (datum, radius, rdvDirection) {
        this.validateSearchAreaInput(datum, radius, rdvDirection);

        var reverseDirection = embryo.geo.reverseDirection;
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
        var topCenter = datum.transformPosition(verticalDirection, radius);

        // Bottom side of the box
        var bottomCenter = datum.transformPosition(reverseDirection(verticalDirection), radius);

        // Go left radius length
        var a = topCenter.transformPosition(reverseDirection(horizontalDirection), radius);
        var b = topCenter.transformPosition(horizontalDirection, radius);
        var c = bottomCenter.transformPosition(horizontalDirection, radius);
        var d = bottomCenter.transformPosition(reverseDirection(horizontalDirection), radius);

        this.output.searchArea = {
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

    function calculateRadius(xError, yError, rdvDistance, safetyFactor) {
        return ((xError + yError) + 0.3 * rdvDistance) * safetyFactor;
    }


    function DatumPointCalculator() {
    }

    DatumPointCalculator.prototype = new SarOperationCalculator();
    DatumPointCalculator.prototype.calculate = function (input) {
        this.validate(input)

        this.timeElapsed();

        var startTs = this.input.lastKnownPosition.ts;
        var datumDownwindPositions = [];
        var datumMinPositions = [];
        var datumMaxPositions = [];
        var currentPositions = []

        var validFor = null
        var lastKnownPosition = new embryo.geo.Position(this.input.lastKnownPosition.lon, this.input.lastKnownPosition.lat)
        var searchObject = findSearchObjectType(input.searchObject);

        for (var i = 0; i < this.input.surfaceDriftPoints.length; i++) {
            // Do we have a next?
            // How long is the data point valid for?
            // Is it the last one?
            if (i == this.input.surfaceDriftPoints.length - 1) {
                // It's the last one - let it last the remainder
                validFor = (this.input.startTs - startTs) / 60 / 60 / 1000;
            } else {
                var currentTs = this.input.surfaceDriftPoints[i].ts;
                if (currentTs < this.input.lastKnownPosition.ts) {
                    currentTs = this.input.lastKnownPosition.ts;
                }
                startTs = this.input.surfaceDriftPoints[i + 1].ts;
                validFor = (startTs - currentTs) / 60 / 60 / 1000;
            }

            var currentTWC = this.input.surfaceDriftPoints[i].twcSpeed * validFor;

            var startingLocation = null;

            if (i == 0) {
                startingLocation = lastKnownPosition;
            } else {
                startingLocation = datumDownwindPositions[i - 1];
            }

            var leewayDivergence = searchObject.divergence;

            var leewaySpeed = searchObject.leewaySpeed(this.input.surfaceDriftPoints[i].leewaySpeed);
            var leewayDriftDistance = leewaySpeed * validFor;

            var twcDirectionInDegrees = directionDegrees(this.input.surfaceDriftPoints[i].twcDirection);
            var currentPos = startingLocation.transformPosition(twcDirectionInDegrees, currentTWC);
            currentPositions.push(currentPos)

            // TODO move somewhere else
            var downWind = this.input.surfaceDriftPoints[i].downWind;
            if (!downWind) {
                downWind = directionDegrees(this.input.surfaceDriftPoints[i].leewayDirection) - 180;
            }

            // Are these calculations correct ?
            // why are previous datumDownwindPosition/datumMinPosition, datumMaxPosition never used.
            datumDownwindPositions.push(currentPos.transformPosition(downWind, leewayDriftDistance));
            datumMinPositions.push(currentPos.transformPosition(downWind - leewayDivergence, leewayDriftDistance));
            datumMaxPositions.push(currentPos.transformPosition(downWind + leewayDivergence, leewayDriftDistance));
        }

        function circleObjectValues(input, timeElapsed, positions) {
            var datum = positions[positions.length - 1];
            var rdv = calculateRdv(lastKnownPosition, datum, timeElapsed);
            var radius = calculateRadius(input.xError, input.yError, rdv.distance, input.safetyFactor);

            return {
                datum: datum,
                rdv: rdv,
                radius: radius,
                datumPositions: positions
            };
        }

        this.output.currentPositions = currentPositions
        this.output.downWind = circleObjectValues(this.input, this.output.timeElapsed, datumDownwindPositions);
        this.output.min = circleObjectValues(this.input, this.output.timeElapsed, datumMinPositions);
        this.output.max = circleObjectValues(this.input, this.output.timeElapsed, datumMaxPositions);

        this.calculateSearchArea(this.output.min, this.output.max, this.output.downWind);

        return this.output;
    }

    function calculateSearchAreaPointsForMinAndMax(tangent, bigCircle, smallCircle, direction) {
        var bearing = tangent.point2.rhumbLineBearingTo(tangent.point1);
        var A = smallCircle.center.transformPosition(bearing, smallCircle.radius).transformPosition(bearing - direction * 90, smallCircle.radius);
        var D = bigCircle.center.transformPosition(bearing + direction * 180, bigCircle.radius).transformPosition(bearing - direction * 90, bigCircle.radius)
        var B = A.transformPosition(bearing + direction * 90, bigCircle.radius * 2);
        var C = D.transformPosition(bearing + direction * 90, bigCircle.radius * 2);
        return {
            A: A,
            B: B,
            C: C,
            D: D
        }
    }

    function extendSearchAreaToIncludeDownWindCircle(tangent, area, downWind, direction) {
        var bearing = tangent.point2.rhumbLineBearingTo(tangent.point1);
        var result = {
            A: area.A,
            B: area.B,
            C: area.C,
            D: area.D
        }

        var dwD = downWind.datum.rhumbLineDistanceTo(area.D);
        var dwA = downWind.datum.rhumbLineDistanceTo(area.A);
        var DA = area.D.rhumbLineDistanceTo(area.A);

        var d = (Math.pow(dwD, 2) - Math.pow(dwA, 2) + Math.pow(DA, 2)) / (2 * DA);
        var h = Math.sqrt(Math.pow(dwD, 2) - Math.pow(d, 2));

        if (h < downWind.radius) {
            result.D = result.D.transformPosition(bearing - direction * 90, downWind.radius - h);
            result.A = result.A.transformPosition(bearing - direction * 90, downWind.radius - h);
        } else {
            result.B = result.B.transformPosition(bearing + direction * 90, h - downWind.radius);
            result.C = result.C.transformPosition(bearing + direction * 90, h - downWind.radius);
        }
        var AB = result.A.rhumbLineDistanceTo(result.B);
        result.totalSize = DA * AB;
        return result;
    }

    function calculateSearchAreaFromTangent(tangent, bigCircle, smallCircle, downWind, direction) {
        var area = calculateSearchAreaPointsForMinAndMax(tangent, bigCircle, smallCircle, direction);
        return extendSearchAreaToIncludeDownWindCircle(tangent, area, downWind, direction);
    }

    DatumPointCalculator.prototype.calculateSearchArea = function (min, max, downWind) {
        var startPos, endPos, startRadius, endRadius;
        if (min.radius > max.radius) {
            startPos = min.datum
            startRadius = min.radius;
            endPos = max.datum;
            endRadius = max.radius;
        } else {
            startPos = max.datum;
            startRadius = max.radius;
            endPos = min.datum;
            endRadius = min.radius;
        }

        var bigCircle = new embryo.geo.Circle(startPos, startRadius);
        var smallCircle = new embryo.geo.Circle(endPos, endRadius)

        var tangents = bigCircle.calculateExternalTangents(smallCircle);

        var area0 = calculateSearchAreaFromTangent(tangents[0], bigCircle, smallCircle, downWind, 1);
        var area1 = calculateSearchAreaFromTangent(tangents[1], bigCircle, smallCircle, downWind, -1);

        this.output.searchArea = area0.totalSize < area1.totalSize ? area0 : area1;
    }


    function DatumLineCalculator() {
    }

    DatumLineCalculator.prototype = new SarOperationCalculator();
    function BackTrackCalculator() {
    }

    BackTrackCalculator.prototype = new SarOperationCalculator();

    // TODO sweep widths values should be in own object?
    function createSweepWidths() {
        var smallShipSweepWidths = {};
        smallShipSweepWidths[embryo.sar.effort.TargetTypes.PersonInWater] = {
            1: 0.2,
            3: 0.2,
            5: 0.3,
            10: 0.3,
            15: 0.3,
            20: 0.3
        };
        smallShipSweepWidths[embryo.sar.effort.TargetTypes.Raft1Person] = {
            1: 0.7,
            3: 1.3,
            5: 1.7,
            10: 2.3,
            15: 2.6,
            20: 2.7
        };
        smallShipSweepWidths[embryo.sar.effort.TargetTypes.Raft4Persons] = {
            1: 0.7,
            3: 1.7,
            5: 2.2,
            10: 3.1,
            15: 3.5,
            20: 3.9
        };
        smallShipSweepWidths[embryo.sar.effort.TargetTypes.Raft6Persons] = {
            1: 0.8,
            3: 1.9,
            5: 2.6,
            10: 3.6,
            15: 4.3,
            20: 4.7
        };
        smallShipSweepWidths[embryo.sar.effort.TargetTypes.Raft8Persons] = {
            1: 0.8,
            3: 2.0,
            5: 2.7,
            10: 3.8,
            15: 4.4,
            20: 4.9
        };
        smallShipSweepWidths[embryo.sar.effort.TargetTypes.Raft10Persons] = {
            1: 0.8,
            3: 2.0,
            5: 2.8,
            10: 4.0,
            15: 4.8,
            20: 5.3
        };
        smallShipSweepWidths[embryo.sar.effort.TargetTypes.Raft15Persons] = {
            1: 0.9,
            3: 2.2,
            5: 3.0,
            10: 4.3,
            15: 5.1,
            20: 5.7
        };
        smallShipSweepWidths[embryo.sar.effort.TargetTypes.Raft20Persons] = {
            1: 0.9,
            3: 2.3,
            5: 3.3,
            10: 4.9,
            15: 5.8,
            20: 6.5
        };
        smallShipSweepWidths[embryo.sar.effort.TargetTypes.Raft25Persons] = {
            1: 0.9,
            3: 2.4,
            5: 3.9,
            10: 5.2,
            15: 6.3,
            20: 7.0
        };
        smallShipSweepWidths[embryo.sar.effort.TargetTypes.Motorboat15] = {
            1: 0.4,
            3: 0.8,
            5: 1.1,
            10: 1.5,
            15: 1.6,
            20: 1.8
        };
        smallShipSweepWidths[embryo.sar.effort.TargetTypes.Motorboat20] = {
            1: 0.8,
            3: 1.5,
            5: 2.2,
            10: 3.3,
            15: 4.0,
            20: 4.5
        };
        smallShipSweepWidths[embryo.sar.effort.TargetTypes.Motorboat33] = {
            1: 0.8,
            3: 1.9,
            5: 2.9,
            10: 4.7,
            15: 5.9,
            20: 6.8
        };
        smallShipSweepWidths[embryo.sar.effort.TargetTypes.Motorboat53] = {
            1: 0.9,
            3: 2.4,
            5: 3.9,
            10: 7.0,
            15: 9.3,
            20: 11.1
        };
        smallShipSweepWidths[embryo.sar.effort.TargetTypes.Motorboat78] = {
            1: 0.9,
            3: 2.5,
            5: 4.3,
            10: 8.3,
            15: 11.4,
            20: 14.0
        };
        smallShipSweepWidths[embryo.sar.effort.TargetTypes.Sailboat15] = {
            1: 0.8,
            3: 1.5,
            5: 2.1,
            10: 3.0,
            15: 3.6,
            20: 4.0
        };
        smallShipSweepWidths[embryo.sar.effort.TargetTypes.Sailboat20] = {
            1: 0.8,
            3: 1.7,
            5: 2.5,
            10: 3.7,
            15: 4.6,
            20: 5.1
        };
        smallShipSweepWidths[embryo.sar.effort.TargetTypes.Sailboat25] = {
            1: 0.9,
            3: 1.9,
            5: 2.8,
            10: 4.4,
            15: 5.4,
            20: 6.3
        };
        smallShipSweepWidths[embryo.sar.effort.TargetTypes.Sailboat30] = {
            1: 0.9,
            3: 2.1,
            5: 3.2,
            10: 5.3,
            15: 6.6,
            20: 7.7
        };
        smallShipSweepWidths[embryo.sar.effort.TargetTypes.Sailboat40] = {
            1: 0.9,
            3: 2.3,
            5: 3.8,
            10: 6.6,
            15: 8.6,
            20: 10.3
        };
        smallShipSweepWidths[embryo.sar.effort.TargetTypes.Sailboat50] = {
            1: 0.9,
            3: 2.4,
            5: 4.0,
            10: 7.3,
            15: 9.7,
            20: 11.6
        };
        smallShipSweepWidths[embryo.sar.effort.TargetTypes.Sailboat70] = {
            1: 0.9,
            3: 2.5,
            5: 4.2,
            10: 7.9,
            15: 10.7,
            20: 13.1
        };
        smallShipSweepWidths[embryo.sar.effort.TargetTypes.Sailboat83] = {
            1: 0.9,
            3: 2.5,
            5: 4.4,
            10: 8.3,
            15: 11.6,
            20: 14.2
        };
        smallShipSweepWidths[embryo.sar.effort.TargetTypes.Ship120] = {
            1: 1.4,
            3: 2.5,
            5: 4.6,
            10: 9.3,
            15: 13.2,
            20: 16.6
        };
        smallShipSweepWidths[embryo.sar.effort.TargetTypes.Ship225] = {
            1: 1.4,
            3: 2.6,
            5: 4.9,
            10: 10.3,
            15: 15.5,
            20: 20.2
        };
        smallShipSweepWidths[embryo.sar.effort.TargetTypes.Ship330] = {
            1: 1.4,
            3: 2.6,
            5: 4.9,
            10: 10.9,
            15: 16.8,
            20: 22.5
        };

        var largeShipSweepWidths = {};
        largeShipSweepWidths[embryo.sar.effort.TargetTypes.PersonInWater] = {
            1: 0.3,
            3: 0.4,
            5: 0.5,
            10: 0.5,
            15: 0.5,
            20: 0.5
        };
        largeShipSweepWidths[embryo.sar.effort.TargetTypes.Raft1Person] = {
            1: 0.9,
            3: 1.8,
            5: 2.3,
            10: 3.1,
            15: 3.4,
            20: 3.7
        };
        largeShipSweepWidths[embryo.sar.effort.TargetTypes.Raft4Persons] = {
            1: 1.0,
            3: 2.2,
            5: 3.0,
            10: 4.0,
            15: 4.6,
            20: 5.0
        };
        largeShipSweepWidths[embryo.sar.effort.TargetTypes.Raft6Persons] = {
            1: 1.1,
            3: 2.5,
            5: 3.4,
            10: 4.7,
            15: 5.5,
            20: 6.0
        };
        largeShipSweepWidths[embryo.sar.effort.TargetTypes.Raft8Persons] = {
            1: 1.1,
            3: 2.5,
            5: 3.5,
            10: 4.8,
            15: 5.7,
            20: 6.2
        };
        largeShipSweepWidths[embryo.sar.effort.TargetTypes.Raft10Persons] = {
            1: 1.1,
            3: 2.6,
            5: 3.6,
            10: 5.1,
            15: 6.1,
            20: 6.7
        };
        largeShipSweepWidths[embryo.sar.effort.TargetTypes.Raft15Persons] = {
            1: 1.1,
            3: 2.8,
            5: 3.8,
            10: 5.5,
            15: 6.5,
            20: 7.2
        };
        largeShipSweepWidths[embryo.sar.effort.TargetTypes.Raft20Persons] = {
            1: 1.2,
            3: 3.0,
            5: 4.1,
            10: 6.1,
            15: 7.3,
            20: 8.1
        };
        largeShipSweepWidths[embryo.sar.effort.TargetTypes.Raft25Persons] = {
            1: 1.2,
            3: 3.1,
            5: 4.3,
            10: 6.4,
            15: 7.8,
            20: 8.7
        };
        largeShipSweepWidths[embryo.sar.effort.TargetTypes.Motorboat15] = {
            1: 0.5,
            3: 1.1,
            5: 1.4,
            10: 1.9,
            15: 2.1,
            20: 2.3
        };
        largeShipSweepWidths[embryo.sar.effort.TargetTypes.Motorboat20] = {
            1: 1.0,
            3: 2.0,
            5: 2.9,
            10: 4.3,
            15: 5.2,
            20: 5.8
        };
        largeShipSweepWidths[embryo.sar.effort.TargetTypes.Motorboat33] = {
            1: 1.1,
            3: 2.5,
            5: 3.8,
            10: 6.1,
            15: 7.7,
            20: 8.8
        };
        largeShipSweepWidths[embryo.sar.effort.TargetTypes.Motorboat53] = {
            1: 1.2,
            3: 3.1,
            5: 5.1,
            10: 9.1,
            15: 12.1,
            20: 14.4
        };
        largeShipSweepWidths[embryo.sar.effort.TargetTypes.Motorboat78] = {
            1: 1.2,
            3: 3.2,
            5: 5.6,
            10: 10.7,
            15: 14.7,
            20: 18.1
        };
        largeShipSweepWidths[embryo.sar.effort.TargetTypes.Sailboat15] = {
            1: 1.0,
            3: 1.9,
            5: 2.7,
            10: 3.9,
            15: 4.7,
            20: 5.2
        };
        largeShipSweepWidths[embryo.sar.effort.TargetTypes.Sailboat20] = {
            1: 1.0,
            3: 2.2,
            5: 3.2,
            10: 4.8,
            15: 5.9,
            20: 6.6
        };
        largeShipSweepWidths[embryo.sar.effort.TargetTypes.Sailboat25] = {
            1: 1.1,
            3: 2.4,
            5: 3.6,
            10: 5.7,
            15: 7.0,
            20: 8.1
        };
        largeShipSweepWidths[embryo.sar.effort.TargetTypes.Sailboat30] = {
            1: 1.1,
            3: 2.7,
            5: 4.1,
            10: 6.8,
            15: 8.6,
            20: 10.0
        };
        largeShipSweepWidths[embryo.sar.effort.TargetTypes.Sailboat40] = {
            1: 1.2,
            3: 3.0,
            5: 4.9,
            10: 8.5,
            15: 11.2,
            20: 13.3
        };
        largeShipSweepWidths[embryo.sar.effort.TargetTypes.Sailboat50] = {
            1: 1.2,
            3: 3.1,
            5: 5.2,
            10: 9.4,
            15: 12.5,
            20: 15.0
        };
        largeShipSweepWidths[embryo.sar.effort.TargetTypes.Sailboat70] = {
            1: 1.2,
            3: 3.2,
            5: 5.5,
            10: 10.2,
            15: 13.9,
            20: 16.9
        };
        largeShipSweepWidths[embryo.sar.effort.TargetTypes.Sailboat83] = {
            1: 1.2,
            3: 3.3,
            5: 5.7,
            10: 10.8,
            15: 15.0,
            20: 18.4
        };
        largeShipSweepWidths[embryo.sar.effort.TargetTypes.Ship120] = {
            1: 1.8,
            3: 3.3,
            5: 6.0,
            10: 12.0,
            15: 17.1,
            20: 21.5
        };
        largeShipSweepWidths[embryo.sar.effort.TargetTypes.Ship225] = {
            1: 1.8,
            3: 3.4,
            5: 6.3,
            10: 13.4,
            15: 20.1,
            20: 26.0
        };
        largeShipSweepWidths[embryo.sar.effort.TargetTypes.Ship330] = {
            1: 1.8,
            3: 3.4,
            5: 6.4,
            10: 14.1,
            15: 21.8,
            20: 29.2
        };

        var sweepWidths = {};
        sweepWidths[embryo.sar.effort.VesselTypes.SmallerVessel] = smallShipSweepWidths;
        sweepWidths[embryo.sar.effort.VesselTypes.Ship] = largeShipSweepWidths;
        return sweepWidths
    }


    function EffortAllocationCalculator() {
    }

    EffortAllocationCalculator.prototype.lookupUncorrectedSweepWidth = function (sruType, targetType, visibility) {
        if (sruType === embryo.sar.effort.VesselTypes.SmallerVessel || sruType === embryo.sar.effort.VesselTypes.Ship) {
            return createSweepWidths()[sruType][targetType][visibility];
        }
        return 0.0
    }
    EffortAllocationCalculator.prototype.lookupVelocityCorrection = function (sruType) {
        // Velocity correction is only necessary for air born SRUs. Should it be used here?
        return 1;
    }
    EffortAllocationCalculator.prototype.lookupWeatherCorrectionFactor = function (wind, sea, targetType) {
        // TODO check i EPD om denne tolkning er korrekt
        function otherVessel(targetType) {
            return !(targetType === embryo.sar.effort.TargetTypes.PersonInWater
            || targetType === embryo.sar.effort.TargetTypes.Sailboat15
            || targetType === embryo.sar.effort.TargetTypes.Sailboat20
            || targetType === embryo.sar.effort.TargetTypes.Sailboat25);
        }

        if (wind < 0 || sea < 0)
            throw "Illegal value";

        if (wind > 25 || sea > 5) {
            return otherVessel(targetType) ? 0.9 : 0.25;
        }
        if ((15 < wind && wind <= 25) || (3 < sea && sea <= 5)) {
            return otherVessel(targetType) ? 0.9 : 0.5;
        }

        return 1;
    };
    EffortAllocationCalculator.prototype.calculateCorrectedSweepWidth = function (wu, fw, fv, ff) {
        return wu * fw * fv * ff;
    };
    EffortAllocationCalculator.prototype.calculateTrackSpacing = function (wc, PoD) {
        // S = W*(-5/8*ln(1-x))^(-5/7)
        var val1 = (-5.0 / 8.0) * Math.log(1 - PoD / 100);

        var val2 = Math.pow(val1, -5.0 / 7.0);
        return wc * val2;
    }
    EffortAllocationCalculator.prototype.calculateSearchEndurance = function (onSceneTime) {
        return onSceneTime * 0.85;
    };
    EffortAllocationCalculator.prototype.calculateZoneAreaSize = function (V, S, T) {
        return V * S * T;
    };
    EffortAllocationCalculator.prototype.getDatum = function (sar) {
        if (sar.input.type == embryo.sar.Operation.RapidResponse) {
            return sar.output.datum;
        } else if (sar.input.type == embryo.sar.Operation.DatumPoint) {
            return sar.output.downWind.datum;
        }
        return sar.output.datum;
    };
    EffortAllocationCalculator.prototype.calculateSearchArea = function (areaSize, datum, sarArea) {
        //In NM?
        var quadrantLength = Math.sqrt(areaSize);

        var sarA = new embryo.geo.Position(sarArea.A.lon, sarArea.A.lat);
        var sarB = new embryo.geo.Position(sarArea.B.lon, sarArea.B.lat);
        var sarD = new embryo.geo.Position(sarArea.D.lon, sarArea.D.lat);
        var center = new embryo.geo.Position(datum.lon, datum.lat);

        var bearingAB = sarA.rhumbLineBearingTo(sarB);
        var bearingDA = sarD.rhumbLineBearingTo(sarA);
        var zonePosBetweenAandB = center.transformPosition(bearingAB, quadrantLength / 2);

        var zoneArea = {};
        zoneArea.B = zonePosBetweenAandB.transformPosition(bearingDA, quadrantLength / 2);
        zoneArea.A = zoneArea.B.transformPosition(embryo.geo.reverseDirection(bearingAB), quadrantLength);
        zoneArea.C = zoneArea.B.transformPosition(embryo.geo.reverseDirection(bearingDA), quadrantLength);
        zoneArea.D = zoneArea.A.transformPosition(embryo.geo.reverseDirection(bearingDA), quadrantLength);

        return zoneArea;
    };

    EffortAllocationCalculator.prototype.calculate = function (allocationInputs, sar) {
        var allocations = [];
        for (var index in allocationInputs) {
            var input = allocationInputs[index];

            var wu = this.lookupUncorrectedSweepWidth(input.type, input.target, input.visibility);
            var fw = this.lookupWeatherCorrectionFactor();
            var fv = this.lookupVelocityCorrection(input.type);
            var wc = this.calculateCorrectedSweepWidth(wu, fw, fv, input.fatigue);
            var S = this.calculateTrackSpacing(wc, input.pod);
            var T = this.calculateSearchEndurance(input.time);
            var zoneAreaSize = this.calculateZoneAreaSize(input.speed, S, T);
            var datum = this.getDatum(sar);
            var area = this.calculateSearchArea(zoneAreaSize, datum, sar.output.searchArea);

            var allocation = clone(input);
            allocation.S = S;
            allocation.area = area;
            allocation.status = embryo.sar.effort.Status.DraftZone;
            allocations.push(allocation);
        }

        return allocations;
    }

    function getCalculator(sarType) {
        switch (sarType) {
            case (embryo.sar.Operation.RapidResponse) :
                return new RapidResponseCalculator();
            case (embryo.sar.Operation.DatumPoint) :
                return new DatumPointCalculator();
            case (embryo.sar.Operation.DatumLine) :
                return new DatumLineCalculator();
            case (embryo.sar.Operation.BackTrack) :
                return new BackTrackCalculator();
            default :
                throw new Error("Unknown sar type " + input.type);
        }
    }

    function clone(object) {
        return JSON.parse(JSON.stringify(object));
    }

    // USED IN sar-edit.js and sar-controller.js
    module.service('SarService', ['$log', '$timeout', 'LivePouch', function ($log, $timeout, LivePouch) {
        var selectedSarById;
        var listeners = {};

        var ddoc = {
            _id: '_design/sareffortview',
            views: {
                sareffortview: {
                    map: function (doc) {
                        if (doc.docType === embryo.sar.Type.EffortAllocation) {
                            emit(doc.effSarId);
                        }
                    }.toString()
                }
            }
        }

        // TODO move to CouchDB server
        LivePouch.get('_design/sareffortview').then(function (existing) {
            ddoc._rev = existing._rev;
            LivePouch.put(ddoc).then(function (result) {
                $log.debug("sareffortview update")
                $log.debug(result);
            }).catch(function (error) {
                $log.error("sareffortview update error")
                $log.error(error)
            });
        }).catch(function (error) {
            $log.error("error fetching _design");
            $log.error(error);
            LivePouch.put(ddoc).then(function (result) {
                $log.debug("sareffortview update")
                $log.debug(result);
            }).catch(function (error) {
                $log.error("sareffortview update error")
                $log.error(error)
            });
        });


        function notifyListeners() {
            for (var key in listeners) {
                listeners[key](selectedSarById);
            }
        }

        var service = {
            createSarId: function () {
                var now = new Date();
                return "AW-" + now.getUTCFullYear() + now.getUTCMonth() + now.getUTCDay() + now.getUTCHours() + now.getUTCMinutes() + now.getUTCSeconds() + now.getUTCMilliseconds();
            },
            sarTypes: function () {
                return embryo.sar.Operation;
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
                return searchObjectTypes;
            },
            findSearchObjectType: findSearchObjectType,
            selectSar: function (sarId) {
                selectedSarById = sarId;
                notifyListeners();
            },

            sarSelected: function (name, fn) {
                listeners[name] = fn;
                if (selectedSarById) {
                    fn(selectedSarById);
                }
            },


            validateSarInput: function (input) {
                // this was written to prevent Chrome browser running in indefinite loops
                getCalculator(input.type).validate(input);
            },
            createSarOperation: function (sarInput) {
                var clonedInput = clone(sarInput);
                var result = {
                    input: clonedInput,
                    output: getCalculator(clonedInput.type).calculate(clonedInput)
                }
                return result;
            },
            calculateEffortAllocations: function (allocationInputs, sar) {
                return new EffortAllocationCalculator().calculate(allocationInputs, sar)
            },
            findSarIndex: function (sars, id) {
                for (var index in sars) {
                    if (sars[index]._id == id) {
                        return index;
                    }
                }
                return null;
            },
            toSmallSarObject: function (sarDoc) {
                return {
                    id: sarDoc._id,
                    name: sarDoc.input.no,
                    status: sarDoc.status
                }
            },
            save: function (sarOperation) {

            }
        };

        return service;
    }]);

})();
