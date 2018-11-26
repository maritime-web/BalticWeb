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

        var c = that.getSafetyZoneCenterPosition();
        var elipseLonLatsPos = [];
        var elipseLonLatsNeg = [];
        var stepCount = 64;
        var step = 2*a/stepCount;
        var metersPrDeg = 111139;
        var translateX = c[0];
        var translateY = c[1];

        for (var i = 0; i <= stepCount; i++) {
            var x = (-a + i * step);
            var yPos = (b/a * Math.sqrt(a*a - x*x));
            var yNeg = -yPos;
            x = x/metersPrDeg + translateX;
            yPos = yPos / metersPrDeg + translateY;
            yNeg = yNeg / metersPrDeg + translateY;
            elipseLonLatsPos.push([x, yPos]);
            elipseLonLatsNeg.push([x, yNeg]);
        }

        return elipseLonLatsPos.concat(elipseLonLatsNeg.reverse());
    };

    that.getSafetyZoneCenterPosition = function () {
        return [rawState.safetyZone.centerPosition.lon, rawState.safetyZone.centerPosition.lat];
    }
}
