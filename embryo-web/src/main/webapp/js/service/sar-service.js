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
        var difference = (this.data.startTs - this.data.lastKnownPosition.ts) / 60 / 60 / 1000;
        this.timeElapsed = difference;

        this.hoursElapsed = Math.floor(difference);
        this.minutesElapsed = Math.round((difference - this.hoursElapsed) * 60);


        var surfaceDriftPoints = this.data.surfaceDrift;

        var startTs = this.data.lastKnownPosition.ts;

        var weatherPointsValidFor = [];
        var datumPositions = [];
        var currentPositions = [];

        var validFor = null;
        var lastDatumPosition = null

        var nmToMeters = embryo.geo.Converter.nmToMeters;

        var i = 0;
        for (i = 0; i < this.data.surfaceDriftPoints.length; i++) {
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
                var startTime = this.data.surfaceDriftPoints[i + 1].ts;
                validFor = (startTime - currentTs) / 60 / 60 / 1000;
            }

            var currentTWC = this.data.surfaceDriftPoints[i].twcSpeed * validFor;

            var startingLocation = null;

            if (i == 0) {
                startingLocation = new embryo.geo.Position(this.data.lastKnownPosition.lon, this.data.lastKnownPosition.lat);
            } else {
                startingLocation = lastDatumPosition;
            }

            var currentPos = startingLocation.transformPosition(this.data.surfaceDriftPoints[i].twcDirection, nmToMeters(currentTWC));
            currentPositions.push(currentPos)

            var leewaySpeed = this.data.searchObject.leewaySpeed(this.data.surfaceDriftPoints[i].leewaySpeed);
            var leeway = leewaySpeed * validFor;

            var downWind = this.data.surfaceDriftPoints[i].downWind
            if (!downWind) {
                downWind = this.data.surfaceDriftPoints[i].leewayDirection - 180;
            }

            var leewayPos = currentPos.transformPosition(downWind, nmToMeters(leeway));
            datumPositions.push(leewayPos);
            lastDatumPosition = leewayPos;
        }

        this.datum = lastDatumPosition;
        this.windList = datumPositions;
        this.currentList = currentPositions;

        if (datumPositions.length > 1) {
            var pos = datumPositions[datumPositions.length - 2];
            this.rdvDirection = pos.bearingTo(lastDatumPosition, embryo.geo.Heading.RL);
            this.rdvDistance = pos.distanceTo(lastDatumPosition, embryo.geo.Heading.RL);
            // RDV Speed
            this.rdvSpeed = this.rdvDistance / validFor;
        } else {
            var lastKnownPosition = new embryo.geo.Position(this.data.lastKnownPosition.lon, this.data.lastKnownPosition.lat);
            this.rdvDirection = lastKnownPosition.bearingTo(lastDatumPosition, embryo.geo.Heading.RL);
            this.rdvDistance = lastKnownPosition.distanceTo(lastDatumPosition, embryo.geo.Heading.RL);
            // RDV Speed
            this.rdvSpeed = this.rdvDistance / this.timeElapsed;
        }


        this.radius = ((this.data.searchObject.x + this.data.searchObject.y) + 0.3 * this.rdvDistance) * this.data.safetyFactor;

        this.searchArea = this.calculateSearchArea(this.datum, this.radius, this.rdvDirection);
    }

    RapidResponse.prototype.calculateSearchArea = function (datum, radius, rdvDirection) {
        var nmToMeters = embryo.geo.Converter.nmToMeters;
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


    function DatumPoint(data) {
        this.setData(data);
        this.calculate();
    }

    DatumPoint.prototype = new Operation();

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
