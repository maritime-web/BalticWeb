embryo.adt = {
}

$(function() {
    embryo.adt.measureDistanceGc = function(lon1, lat1, lon2, lat2) {
        // From http://www.movable-type.co.uk/scripts/latlong.html
        var R = 6371; // km
        var dLat = toRad(lat2-lat1);
        var dLon = toRad(lon2-lon1);
        var lat1 = toRad(lat1);
        var lat2 = toRad(lat2);

        var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        var d = R * c;
        return d;
    }

    embryo.adt.createRing = function(longitude, latitude, radius, noRings) {
        var result = [];
        for (var l = 1; l <= noRings; l ++) {
            var points = calculateRing(longitude, latitude, l*radius, 200);

            var feature = new OpenLayers.Feature.Vector(
                new OpenLayers.Geometry.Polygon([new OpenLayers.Geometry.LinearRing(points)])
            );

            result.push(feature);
        }
        return result;
    }

    var groupSelected = false;

    var selectedControl = null;

    var ringPosition = null;

    var layers = {
        distance: new OpenLayers.Layer.Vector("Distance Layer"),
        area: new OpenLayers.Layer.Vector("Area Layer"),
        rings: new OpenLayers.Layer.Vector("Rings Layer", {
            styleMap: new OpenLayers.StyleMap({
                "default": new OpenLayers.Style({
                    fillColor: "#f80",
                    fillOpacity: 0.1,
                    strokeWidth: 2,
                    strokeColor: "#f80",
                    strokeOpacity: 0.7
                })
            })
        })
    }

    var controls = {
        distance: new OpenLayers.Control.Measure(
            OpenLayers.Handler.Path, {
                persist: true,
                layer: layers.distance
            }
        ),
        area: new OpenLayers.Control.Measure(
            OpenLayers.Handler.Polygon, {
                persist: true,
                layer: layers.area
            }
        ),
        rings: embryo.map.createClickControl(function (lonlat) {
            ringPosition = lonlat;
            drawRing();
        })
    }

    controls.distance.events.on({
        measure: function (e) {
            var geometry = event.geometry;
            var units = event.units;
            var measure = event.measure;
            $("#adtDistance h4").html("Distance: " + formatNauticalMile(e.measure));
        }
    });

    controls.area.events.on({
        measure: function (e) {
            var geometry = event.geometry;
            var units = event.units;
            var measure = event.measure;
            $("#adtArea h4").html("Area: " + e.measure.toFixed(3) + "  " + e.units + "<sup>2</sup>");
        }
    });

    /*

    for (var i in layers) {
        embryo.map.add({
            group: "adt",
            layer: layers[i]
        });
    }

    for (var i in controls) {
        embryo.map.add({
            control: controls[i]
        });
    }

    */

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
        for (var i = 0; i < noPoints; i++) {
            var brng = Math.PI * 2 * i / noPoints;
            var lat2 = Math.asin( Math.sin(lat1)*Math.cos(d/R) +
                          Math.cos(lat1)*Math.sin(d/R)*Math.cos(brng) );
            var lon2 = lon1 + Math.atan2(Math.sin(brng)*Math.sin(d/R)*Math.cos(lat1),
                                 Math.cos(d/R)-Math.sin(lat1)*Math.sin(lat2));

            points.push(embryo.map.createPoint(toDegree(lon2), toDegree(lat2)));
        }
        return points;
    }

    function drawRing() {
        layers.rings.removeAllFeatures();

        if (ringPosition == null) {
            $("#adtRings h4").html("");
            return;
        }

        $("#adtRings h4").html("Ring placed at " + formatLonLat(ringPosition));

        var longitude = ringPosition.lon;
        var latitude = ringPosition.lat

        var distance = parseInt($("#adtDistanceField").val());

        distance = Math.min(1000, distance);
        distance = Math.max(0, distance);

        distance = distance * 1.852;

        var noRings = parseInt($("#adtNoRingsField").val());

        noRings = Math.min(5, noRings);
        noRings = Math.max(0, noRings);

        layers.rings.addFeatures(embryo.adt.createRing(longitude, latitude, distance, noRings));

    }

    function updateSelectedControl() {
        for (var i in controls) {
            if (i == selectedControl && groupSelected) {
                controls[i].activate();
            } else {
                controls[i].deactivate();
            }
        }

        for (var i in layers) {
            layers[i].setVisibility(i == selectedControl && groupSelected);
        }
    }

    embryo.groupChanged(function(e) {
        if (e.groupId == "adt") {
            $("#adtDistance h4").html("");
            selectedControl = "distance";
            groupSelected = true;
            $("#adtControlPanel").css("display", "block");
            $("#adtControlPanel .collapse").data("collapse", null)
            openCollapse("#adtControlPanel .accordion-body:first");
        } else {
            groupSelected = false;
            $("#adtControlPanel").css("display", "none");
        }
        updateSelectedControl();
    });

    embryo.ready(function() {
        $("#adtDistance").on("show", function(e) {
            selectedControl = "distance";
            updateSelectedControl();
            $("#adtDistance h4").html("");
        });
        $("#adtArea").on("show", function(e) {
            selectedControl = "area";
            updateSelectedControl();
            $("#adtArea h4").html("");
        });

        $("#adtRings").on("show", function(e) {
            selectedControl = "rings";
            ringPosition = null;
            updateSelectedControl();
            drawRing();
        });

        $("#adtRings input").change(drawRing);
    });

});
