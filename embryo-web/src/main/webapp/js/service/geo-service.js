(function () {
    "use strict";

    embryo.geo = {}
    embryo.geo.Heading = Object.freeze({"RL": "RL", "GC": "GC"})

    var VincentyCalculationType = Object.freeze({
        "DISTANCE": "distance",
        "INITIAL_BEARING": "init-bearing",
        FINAL_BEARING: "final-bearing"
    })

    /**
     * Vincenty formula
     */
    function vincentyFormula(latitude1, longitude1, latitude2, longitude2, type) {
        var a = 6378137;
        var b = 6356752.3142;
        var f = 1 / 298.257223563; // WGS-84 ellipsoid
        var L = toRadians(longitude2 - longitude1);
        var U1 = Math.atan((1 - f) * Math.tan(embryo.Math.toRadians(latitude1)));
        var U2 = Math.atan((1 - f) * Math.tan(embryo.Math.toRadians(latitude2)));
        var sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
        var sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);

        var lambda = L;
        var lambdaP = 2 * Math.PI;
        var iterLimit = 20;
        var sinLambda = 0;
        var cosLambda = 0;
        var sinSigma = 0;
        var cosSigma = 0;
        var sigma = 0;
        var sinAlpha = 0;
        var cosSqAlpha = 0;
        var cos2SigmaM = 0;
        var C;
        while (Math.abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0) {
            sinLambda = Math.sin(lambda);
            cosLambda = Math.cos(lambda);
            sinSigma = Math.sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda)
            + (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));
            if (sinSigma == 0) {
                return 0; // co-incident points
            }
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            cosSqAlpha = 1 - sinAlpha * sinAlpha;
            cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
            if (Number.isNaN(cos2SigmaM)) {
                cos2SigmaM = 0; // equatorial line
            }
            C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
            lambdaP = lambda;
            lambda = L + (1 - C) * f * sinAlpha
            * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
        }
        if (iterLimit == 0) {
            return Number.NaN; // formula failed to converge
        }

        if (type == VincentyCalculationType.DISTANCE) {
            var uSq = cosSqAlpha * (a * a - b * b) / (b * b);
            var A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
            var B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
            var deltaSigma = B * sinSigma * (cos2SigmaM + B / 4
                * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B / 6 * cos2SigmaM
                * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));
            var distance = b * A * (sigma - deltaSigma);
            return distance;
        }
        // initial bearing
        var fwdAz = embryo.Math.toDegrees(Math.atan2(cosU2 * sinLambda, cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));
        if (type == VincentyCalculationType.INITIAL_BEARING) {
            return fwdAz;
        }
        // final bearing
        return embryo.Math.toDegrees(Math.atan2(cosU1 * sinLambda, -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda));
    }

    function CoordinateSystem() {
    }

    CoordinateSystem.CARTESIAN = function () {
    }

    CoordinateSystem.CARTESIAN.distanceBetween = function (latitude1, longitude1, latitude2, longitude2) {
        var lat1 = embryo.Math.toRadians(latitude1);
        var lat2 = embryo.Math.toRadians(latitude2);
        var dLat = embryo.Math.toRadians(latitude2 - latitude1);
        var dLon = embryo.Math.toRadians(Math.abs(longitude2 - longitude1));
        var dPhi = Math.log(Math.tan(lat2 / 2.0 + 0.7853981633974483) / Math.tan(lat1 / 2.0 + 0.7853981633974483));
        var q = dPhi == 0.0 ? Math.cos(lat1) : dLat / dPhi;
        if (dLon > 3.141592653589793) {
            dLon = 6.283185307179586 - dLon;
        }

        return Math.sqrt(dLat * dLat + q * q * dLon * dLon) * 6371.0087714 * 1000.0;
    }

    CoordinateSystem.GEODETIC = function () {
    };

    CoordinateSystem.GEODETIC.distanceBetween = function (latitude1, longitude1, latitude2, longitude2) {
        return vincentyFormula(latitude1, longitude1, latitude2, longitude2, VincentyCalculationType.DISTANCE);
    }

    /**
     * Encapsulation of an ellipsoid, and declaration of common reference
     * ellipsoids.
     *
     * @author Mike Gavaghan
     */
    embryo.geo.Ellipsoid = function (semiMajorAxisInMeters, semiMinorAxisInMeters, flattening, inverseFlattening) {
        /** Semi major axis (meters). */
        this.semiMajorAxisInMeters = semiMajorAxisInMeters;
        /** Semi minor axis (meters). */
        this.semiMinorAxisInMeters = semiMinorAxisInMeters;
        this.flattening = flattening;
        this.inverseFlattening = inverseFlattening;
    }
    /**
     * Build an Ellipsoid from the semi major axis measurement and the inverse
     * flattening.
     */
    embryo.geo.Ellipsoid.fromAAndInverseF = function (semiMajor, inverseFlattening) {
        var f = 1.0 / inverseFlattening;
        var b = (1.0 - f) * semiMajor;
        return new embryo.geo.Ellipsoid(semiMajor, b, f, inverseFlattening);
    };

    /**
     * Build an Ellipsoid from the semi major axis measurement and the
     * flattening.
     */
    embryo.geo.Ellipsoid.fromAAndF = function (semiMajorMeters, flattening) {
        var inverseF = 1.0 / flattening;
        var b = (1.0 - flattening) * semiMajorMeters;
        return new embryo.geo.Ellipsoid(semiMajorMeters, b, flattening, inverseF);
    }

    /** The WGS84 ellipsoid. */
    embryo.geo.Ellipsoid.WGS84 = embryo.geo.Ellipsoid.fromAAndInverseF(6378137.0, 298.257223563);

    /** The GRS80 ellipsoid. */
    embryo.geo.Ellipsoid.GRS80 = embryo.geo.Ellipsoid.fromAAndInverseF(6378137.0, 298.257222101);

    /** The GRS67 ellipsoid. */
    embryo.geo.Ellipsoid.GRS67 = embryo.geo.Ellipsoid.fromAAndInverseF(6378160.0, 298.25);

    /** The ANS ellipsoid. */
    embryo.geo.Ellipsoid.ANS = embryo.geo.Ellipsoid.fromAAndInverseF(6378160.0, 298.25);

    /** The WGS72 ellipsoid. */
    embryo.geo.Ellipsoid.WGS72 = embryo.geo.Ellipsoid.fromAAndInverseF(6378135.0, 298.26);

    /** The Clarke1858 ellipsoid. */
    embryo.geo.Ellipsoid.CLARKE1858 = embryo.geo.Ellipsoid.fromAAndInverseF(6378293.645, 294.26);

    /** The Clarke1880 ellipsoid. */
    embryo.geo.Ellipsoid.CLARKE1880 = embryo.geo.Ellipsoid.fromAAndInverseF(6378249.145, 293.465);

    /** A spherical "ellipsoid". */
    embryo.geo.Ellipsoid.SPHERE = embryo.geo.Ellipsoid.fromAAndF(6371000, 0.0);


    embryo.geo.Converter = {
        knots2Ms: function (knots) {
            return knots * 1.852 / 3.6;
        },
        ms2Knots: function (ms) {
            return ms * 3.6 / 1.852;
        },
        nmToMeters: function (nm) {
            return nm * 1852;
        },
        metersToNm: function (meters) {
            return meters / 1852;
        }
    }

    embryo.geo.formatLongitude = function (longitude) {
        var ns = "E";
        if (longitude < 0) {
            ns = "W";
            longitude *= -1;
        }
        var hours = Math.floor(longitude);
        longitude -= hours;
        longitude *= 60;
        var lonStr = longitude.toFixed(3);
        while (lonStr.indexOf('.') < 2) {
            lonStr = "0" + lonStr;
        }

        return (hours / 1000.0).toFixed(3).substring(2) + " " + lonStr + ns;
    }

    embryo.geo.formatLatitude = function (latitude) {
        var ns = "N";
        if (latitude < 0) {
            ns = "S";
            latitude *= -1;
        }
        var hours = Math.floor(latitude);
        latitude -= hours;
        latitude *= 60;
        var latStr = latitude.toFixed(3);
        while (latStr.indexOf('.') < 2) {
            latStr = "0" + latStr;
        }

        return (hours / 100.0).toFixed(2).substring(2) + " " + latStr + ns;
    }

    embryo.geo.reverseDirection = function (direction) {
        var newDirection = direction + 180;

        if (newDirection > 360) {
            newDirection = newDirection - 360;
        }

        if (newDirection < -0) {
            newDirection = newDirection + 360;
        }
        return newDirection;
    }

    function decimalAdjust(type, value, exp) {
        // If the exp is undefined or zero...
        if (typeof exp === 'undefined' || +exp === 0) {
            return Math[type](value);
        }
        value = +value;
        exp = +exp;
        // If the value is not a number or the exp is not an integer...
        if (isNaN(value) || !(typeof exp === 'number' && exp % 1 === 0)) {
            return NaN;
        }
        // Shift
        value = value.toString().split('e');
        value = Math[type](+(value[0] + 'e' + (value[1] ? (+value[1] - exp) : -exp)));
        // Shift back
        value = value.toString().split('e');
        return +(value[0] + 'e' + (value[1] ? (+value[1] + exp) : exp));
    }

    embryo.Math = {};
    embryo.Math.toRadians = function (degree) {
        return degree / 360 * 2 * Math.PI;
    }

    embryo.Math.toDegrees = function (rad) {
        return rad * 360 / 2 / Math.PI;
    }

    embryo.Math.round10 = function (value, exp) {
        return decimalAdjust('round', value, exp);
    };


    /**
     * This method is a slightly modified reimplementation in JavaScript of dk.dma.epd.common.util.Calculator.calculateEndingGlobalCoordinates()
     *
     * @param ellipsoid
     * @param startPosition
     * @param startBearing
     * @param distance
     * @param endBearing
     * @returns {latitude}
     */
    embryo.geo.Ellipsoid.prototype.calculateEndingGlobalCoordinates =
        function (startPosition, startBearing, distance) {

            var ellipsoid = this;

            var a = ellipsoid.semiMajorAxisInMeters;
            var b = ellipsoid.semiMajorAxisInMeters;

            var aSquared = a * a;
            var bSquared = b * b;
            var f = ellipsoid.flattening;
            var phi1 = embryo.Math.toRadians(startPosition.lat);
            var alpha1 = embryo.Math.toRadians(startBearing);

            var cosAlpha1 = Math.cos(alpha1);
            var sinAlpha1 = Math.sin(alpha1);
            var tanU1 = (1.0 - f) * Math.tan(phi1);
            var cosU1 = 1.0 / Math.sqrt(1.0 + tanU1 * tanU1);
            var sinU1 = tanU1 * cosU1;

            // eq. 1
            var sigma1 = Math.atan2(tanU1, cosAlpha1);

            // eq. 2
            var sinAlpha = cosU1 * sinAlpha1;

            var sin2Alpha = sinAlpha * sinAlpha;
            var cos2Alpha = 1 - sin2Alpha;
            var uSquared = cos2Alpha * (aSquared - bSquared) / bSquared;

            // eq. 3
            var A = 1 + uSquared / 16384 * (4096 + uSquared * (-768 + uSquared * (320 - 175 * uSquared)));

            // eq. 4
            var B = uSquared / 1024 * (256 + uSquared * (-128 + uSquared * (74 - 47 * uSquared)));

            // iterate until there is a negligible change in sigma
            var deltaSigma;
            var sOverbA = distance / (b * A);
            var sigma = sOverbA;
            var sinSigma;
            var prevSigma = sOverbA;
            var sigmaM2;
            var cosSigmaM2;
            var cos2SigmaM2;

            for (; ;) {
                // eq. 5

                sigmaM2 = 2.0 * sigma1 + sigma;
                cosSigmaM2 = Math.cos(sigmaM2);
                cos2SigmaM2 = cosSigmaM2 * cosSigmaM2;
                sinSigma = Math.sin(sigma);
                var cosSignma = Math.cos(sigma);

                // eq. 6
                deltaSigma = B * sinSigma
                * (cosSigmaM2 + B / 4.0 * (cosSignma * (-1 + 2 * cos2SigmaM2) - B / 6.0
                * cosSigmaM2 * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM2)));

                // eq. 7
                sigma = sOverbA + deltaSigma;

                // break if undefined detected to avoid infinite loop
                if (sigma === undefined || sigma === NaN) {
                    break;
                }
                // break after converging to tolerance
                if (Math.abs(sigma - prevSigma) < 0.0000000000001) {
                    break;
                }

                prevSigma = sigma;
            }

            sigmaM2 = 2.0 * sigma1 + sigma;
            cosSigmaM2 = Math.cos(sigmaM2);
            cos2SigmaM2 = cosSigmaM2 * cosSigmaM2;

            var cosSigma = Math.cos(sigma);
            sinSigma = Math.sin(sigma);

            // eq. 8
            var phi2 = Math.atan2(
                sinU1 * cosSigma + cosU1 * sinSigma * cosAlpha1,
                (1.0 - f)
                * Math.sqrt(sin2Alpha
                + Math.pow(sinU1 * sinSigma - cosU1 * cosSigma
                * cosAlpha1, 2.0)));

            // eq. 9
            // This fixes the pole crossing defect spotted by Matt Feemster. When a
            // path passes a pole and essentially crosses a line of latitude twice -
            // once in each direction - the longitude calculation got messed up.
            // Using atan2 instead of atan fixes the defect. The change is in the next 3 lines.
            // double tanLambda = sinSigma * sinAlpha1 / (cosU1 * cosSigma - sinU1 * sinSigma * cosAlpha1);
            // double lambda = Math.atan(tanLambda);
            var lambda = Math.atan2(sinSigma * sinAlpha1, cosU1 * cosSigma - sinU1 * sinSigma * cosAlpha1);

            // eq. 10
            var C = f / 16 * cos2Alpha * (4 + f * (4 - 3 * cos2Alpha));

            // eq. 11
            var L = lambda - (1 - C) * f * sinAlpha * (sigma + C * sinSigma * (cosSigmaM2 + C * cosSigma * (-1 + 2 * cos2SigmaM2)));

            // eq. 12
            //var alpha2 = Math.atan2(sinAlpha, -sinU1 * sinSigma + cosU1 * cosSigma * cosAlpha1);

            // build result
            var latitude = embryo.Math.toDegrees(phi2);
            var longitude = startPosition.lon + embryo.Math.toDegrees(L);

            //if (endBearing != null && endBearing.length > 0) {
            //    endBearing[0] = Math.toDegrees(alpha2);
            //}

            return new embryo.geo.Position(longitude, latitude);

        }

    embryo.geo.Position = function (lon, lat) {
        this.lon = lon;
        this.lat = lat;
    }

    /**
     * @param startBearing
     * @param distance
     * @returns a new instance of Position moved according to the parameters startBearing and distance.
     */
    embryo.geo.Position.prototype.transformPosition = function (startBearing, distance) {
        var startLocation = this;
        var sphere = embryo.geo.Ellipsoid.SPHERE;
        var dest = sphere.calculateEndingGlobalCoordinates(startLocation, startBearing, distance);
        return dest;
    }

    embryo.geo.Position.prototype.rhumbLineBearingTo = function (destination) {

        var lat1 = embryo.Math.toRadians(this.lat);
        var lat2 = embryo.Math.toRadians(destination.lat);
        var phi = Math.log(Math.tan(lat2 / 2.0 + 0.7853981633974483) / Math.tan(lat1 / 2.0 + 0.7853981633974483));
        var lon = embryo.Math.toRadians(destination.lon - this.lon);
        if (Math.abs(lon) > 3.141592653589793) {
            lon = lon > 0.0 ? -(6.283185307179586 - lon) : 6.283185307179586 + lon;
        }
        var bearing = Math.atan2(lon, phi);
        return (embryo.Math.toDegrees(bearing) + 360.0) % 360.0;
    }

    embryo.geo.Position.prototype.geodesicInitialBearingTo = function (destination) {
        return vincentyFormula(this.lat, this.lon, destination.lat, destination.lon, VincentyCalculationType.INITIAL_BEARING);
    }

    embryo.geo.Position.prototype.bearingTo = function (destination, heading) {
        if (heading == embryo.geo.Heading.RL) {
            return this.rhumbLineBearingTo(destination);
        } else {
            return this.geodesicInitialBearingTo(destination);
        }
    }

    embryo.geo.Position.prototype.geodesicDistanceTo = function (destination) {
        return CoordinateSystem.GEODETIC.distanceBetween(this.lat, this.lon, destination.lat, destination.lon, VincentyCalculationType.DISTANCE);
    }

    embryo.geo.Position.prototype.rhumbLineDistanceTo = function (destination) {
        return CoordinateSystem.CARTESIAN.distanceBetween(this.lat, this.lon, destination.lat, destination.lon, VincentyCalculationType.DISTANCE);
    }

    /**
     * Find distance between this point and a destination point given heading
     *
     * @param destination
     * @param heading
     */
    embryo.geo.Position.prototype.distanceTo = function (destination, heading) {
        var distanceInMeters;
        if (heading == embryo.geo.Heading.RL) {
            distanceInMeters = this.rhumbLineDistanceTo(destination);
        } else {
            distanceInMeters = this.geodesicDistanceTo(destination);
        }
        return embryo.geo.Converter.metersToNm(distanceInMeters);
    }


})();
