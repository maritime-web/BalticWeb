/**
 * @classdesc
 * Represents the state of a vessel at a point in time in a Near miss context.
 * @constructor
 * @param {*} vesselState
 */
function NearMissVesselStateModel(vesselState) {
    var that = this;
    var rawState = vesselState;
    that.mmsi = rawState.mmsi;
    that.time = moment(rawState.time).utc();
    that.dimensions = rawState.dimensions;
    that.cog = rawState.cog;
    that.hdg = rawState.hdg;
    that.longitude = rawState.position.lon;
    that.latitude = rawState.position.lat;

    that.getLonLat = function () {
        return [rawState.position.lon, rawState.position.lat];
    };

    that.getRadianHdg = function () {
        return (-rawState.hdg) * (Math.PI / 180);
    };

    that.getPointsForSafetyZone = function () {
        var a = rawState.safetyZone.a;
        var b = rawState.safetyZone.b;
        console.log('a: ' + a);

        var c = that.getSafetyZoneCenterPosition();
        var elipseLonLatsPos = [];
        var elipseLonLatsNeg = [];
        var metersPrDeg = 111139;
        var translateX = c[0];
        var translateY = c[1];

        var step = a/20;
        var smallStep = a/100;
        var verySmallStep = a/400;
        var x = -a;
        while (x < a) {
            addCoords();
            x = nextX();
        }

        x = a;
        addCoords();

        function addCoords() {
            var yPos = calculateY(x);
            var yNeg = -yPos;
            var xTransformed = x/metersPrDeg + translateX;
            yPos = yPos / metersPrDeg + translateY;
            yNeg = yNeg / metersPrDeg + translateY;
            elipseLonLatsPos.push([xTransformed, yPos]);
            elipseLonLatsNeg.push([xTransformed, yNeg]);
        }

        function nextX() {
            var candidate = x + step;

            if (x < -smallStep || x >= (a - smallStep)) {
                candidate = x + verySmallStep;
            } else if (x < -step || x >= (a - step - 2*smallStep)) {
                candidate = x + smallStep;
            }

            return candidate;
        }

        function calculateY(xPos) {
            return (b/a * Math.sqrt(a*a - xPos*xPos));
        }

        return elipseLonLatsPos.concat(elipseLonLatsNeg.reverse());
    };

    that.getSafetyZoneCenterPosition = function () {
        return [rawState.safetyZone.centerPosition.lon, rawState.safetyZone.centerPosition.lat];
    }
}
