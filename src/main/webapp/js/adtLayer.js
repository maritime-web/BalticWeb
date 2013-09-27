$(function() {
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
            $("#adtDistance h4").html("Distance: " + e.measure.toFixed(3) + "  " + e.units);
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

        distance = Math.min(10000, distance);
        distance = Math.max(0, distance);

        var noRings = parseInt($("#adtNoRingsField").val());

        noRings = Math.min(5, noRings);
        noRings = Math.max(0, noRings);

        for (var l = 1; l <= noRings; l ++) {
            var points = calculateRing(longitude, latitude, l*distance, 200);

            var feature = new OpenLayers.Feature.Vector(
                new OpenLayers.Geometry.Polygon([new OpenLayers.Geometry.LinearRing(points)])
            );

            layers.rings.addFeatures([ feature ]);

        }
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
