$(function() {
    var groupSelected = false;

    var selectedControl = null;

    var layers = {
        distance: new OpenLayers.Layer.Vector("Distance Layer"),
        area: new OpenLayers.Layer.Vector("Area Layer"),
        ring: new OpenLayers.Layer.Vector("Ring Layer"),
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
        )
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

    embryo.authenticated(function() {

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
    });

});
