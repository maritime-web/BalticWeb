(function () {
    "use strict";

    var module = angular.module('embryo.sar.service', ['embryo.storageServices']);

    embryo.sar = {}
    // A way to create an enumeration like construction in JavaScript
    embryo.sar.types = Object.freeze({"RapidResponse": "rr", "DatumPoint": "dp", "DatumLine": "dl", "BackTrack": "bt"})

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

    embryo.SARStatus = Object.freeze({
        STARTED: "S",
        ENDED: "E"
    });

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

        console.log(this.input)

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

            console.log(searchObject);

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

    function getCalculator(sarType) {
        switch (sarType) {
            case (embryo.sar.types.RapidResponse) :
                return new RapidResponseCalculator();
            case (embryo.sar.types.DatumPoint) :
                return new DatumPointCalculator();
            case (embryo.sar.types.DatumLine) :
                return new DatumLineCalculator();
            case (embryo.sar.types.BackTrack) :
                return new BackTrackCalculator();
            default :
                throw new Error("Unknown sar type " + input.type);
        }
    }

    function clone(object) {
        console.log(clone);
        console.log(object)
        console.log(JSON.parse(JSON.stringify(object)));


        return JSON.parse(JSON.stringify(object));
    }


    module.service('SarService', ['$log', '$timeout', 'LivePouch', function ($log, $timeout, LivePouch) {
        var selectedSarById;
        var listeners = {};

        function notifyListeners() {
            for (var key in listeners) {
                listeners[key](selectedSarById);
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
            findSarIndex: function (sars, id) {
                for (var index in sars) {
                    if (sars[index].id == id) {
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
