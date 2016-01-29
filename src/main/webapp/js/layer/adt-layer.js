embryo.adt = {}

$(function() {
    embryo.adt.measureDistanceGc = function(lon1, lat1, lon2, lat2) {
        // From http://www.movable-type.co.uk/scripts/latlong.html
        var R = 6371; // km
        var dLat = toRad(lat2 - lat1);
        var dLon = toRad(lon2 - lon1);
        var lat1 = toRad(lat1);
        var lat2 = toRad(lat2);

        var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1)
                * Math.cos(lat2);
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        var d = R * c;
        return d;
    }

    embryo.adt.createRing = function (longitude, latitude, radius, noRings, attributes) {
        var result = [];
        for ( var l = 1; l <= noRings; l++) {
            var points = calculateRing(longitude, latitude, l * radius, 200);

            var feature = new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Polygon(
                    [new OpenLayers.Geometry.LinearRing(points)]),
                attributes
            );

            result.push(feature);
        }
        return result;
    }

    function toRad(degree) {
        return degree / 360 * 2 * Math.PI;
    }

    function toDegree(rad) {
        return rad * 360 / 2 / Math.PI;
    }

    function calculateRing(longitude, latitude, radius, noPoints) {
        var points = [];
        var lat1 = toRad(latitude);
        var lon1 = toRad(longitude);
        var R = 6371; // earths mean radius
        var d = radius;
        for ( var i = 0; i < noPoints; i++) {
            var brng = Math.PI * 2 * i / noPoints;
            var lat2 = Math.asin(Math.sin(lat1) * Math.cos(d / R) + Math.cos(lat1) * Math.sin(d / R) * Math.cos(brng));
            var lon2 = lon1
                    + Math.atan2(Math.sin(brng) * Math.sin(d / R) * Math.cos(lat1), Math.cos(d / R) - Math.sin(lat1)
                            * Math.sin(lat2));

            points.push(embryo.map.createPoint(toDegree(lon2), toDegree(lat2)));
        }
        return points;
    }

});
