function SarLayer() {

    var nmToMeters = embryo.geo.Converter.nmToMeters;

    var that = this;

    that.zoomLevels = [8, 10, 12];

    this.init = function () {
        var opacityFactor = {
            true: 1.0,
            false: 0.5
        }

        var context = {

            color: function (feature) {
                if (feature.attributes.type == "zone") {
                    return feature.attributes.status == embryo.sar.effort.Status.Active ? "#7D877A" : "red"
                }
                if (feature.attributes.type == 'dv') {
                    return "black";
                }
                if (feature.attributes.type) {
                    return feature.attributes.active ? "green" : "#999";
                }
                // extra feature (circle) added by ModifyFeature control
                return "red";
            },
            strokeWidth: function () {
                return that.zoomLevel >= 1 ? 2 : 1;
            },
            strokeOpacity: function (feature) {
                if (feature.attributes.type === 'dv') {
                    return 0.7 * opacityFactor[that.active];
                }
                return 0.6 * opacityFactor[that.active];
            },
            fillOpacity: function () {
                return 0.2 * opacityFactor[that.active];
            },
            label: function (feature) {
                if (feature.attributes.type == "areaLabel") {
                    return that.zoomLevel >= 1 ? feature.attributes.label : "";
                }
                if (feature.attributes.type == "zone") {
                    return that.zoomLevel >= 2 ? feature.attributes.label : "";
                }
                if (feature.attributes.type == "circleLabel" || feature.attributes.type == "lkpLabel") {
                    return that.zoomLevel >= 3 ? feature.attributes.label : "";
                }
                var value = feature.attributes.label ? feature.attributes.label : "";
                return value;
            },
            fontTransparency: function () {
                return opacityFactor[that.active]
            }
        };

        var defaultStyle = {
            orientation: true,
            fontOpacity: "${fontTransparency}",
            fillColor: "${color}",
            fillOpacity: "${fillOpacity}",
            strokeWidth: "${strokeWidth}",
            strokeColor: "${color}",
            strokeOpacity: "${strokeOpacity}",
            label: "${label}"
        }


        this.layers.sar = new OpenLayers.Layer.Vector("SAR Layer", {
            renderers: ['SVGExtended', 'VMLExtended', 'CanvasExtended'],
            styleMap: new OpenLayers.StyleMap({
                "default": new OpenLayers.Style(defaultStyle, {
                    context: context
                })
            })
        })

        var defaultEditStyle = {
            fontOpacity: "${fontTransparency}",
            fillColor: "${color}",
            fillOpacity: "${fillOpacity}",
            strokeWidth: "${strokeWidth}",
            strokeColor: "${color}",
            strokeOpacity: "${strokeOpacity}",
            pointRadius: 10,
            pointerEvents: "visible",
            label: "${label}"
        }

        this.layers.sarEdit = new OpenLayers.Layer.Vector("SAR Edit Layer", {
            //renderers: ['SVGExtended', 'VMLExtended', 'CanvasExtended'],
            styleMap: new OpenLayers.StyleMap({
                "default": new OpenLayers.Style(defaultEditStyle, {context: context}),
                "select": new OpenLayers.Style(defaultEditStyle, {context: context}),
                "temporary": new OpenLayers.Style(defaultEditStyle, {context: context})
            })
        });

        var dragHandlers = {
            onComplete: function (feature) {
                // list of points (components) are always created as A, B, C, D in drawEffortAllocationZone
                // An extra point A is added to the list, because OpenLayers is closing the polygon.
                // We are however just using the first 4 points (A, B, C and D)
                if (that.modified) {
                    var zoneUpdate = {
                        _id: feature.attributes.id,
                        area: {
                            A: that.map.transformToPosition(feature.geometry.components[0]),
                            B: that.map.transformToPosition(feature.geometry.components[1]),
                            C: that.map.transformToPosition(feature.geometry.components[2]),
                            D: that.map.transformToPosition(feature.geometry.components[3])
                        }
                    }
                    that.modified(zoneUpdate)
                }
                //this.controls.modify.activate();
            }
        }


        //this.controls.modify = new OpenLayers.Control.ModifyFeature(this.layers.sarEdit, {mode: OpenLayers.Control.ModifyFeature.RESHAPE});
        this.controls.drag = new OpenLayers.Control.DragFeature(this.layers.sarEdit, dragHandlers);


        /*

         this.layers.sarEdit.events.on({
         "beforefeaturemodified": report,
         "featuremodified": report,
         "afterfeaturemodified": report,
         "vertexmodified": report,
         "sketchmodified": report,
         "sketchstarted": report,
         "sketchcomplete": report
         });
         */

        /*
         *
         * onStart	{Function} Define this function if you want to know when a drag starts.
         onDrag	{Function} Define this function if you want to know about each move of a feature.
         onComplete	{Function} Define this function if you want to know when a feature is done dragging.
         onEnter	{Function} Define this function if you want to know when the mouse goes over a feature and thereby makes this feature a candidate for dragging.
         onLeave	{Function} Define this function if you want to know when the mouse goes out of the feature that was dragged.
         */

        //this.selectableLayers = [this.layers.sar];
        //this.selectableAttribute = "id";

    };

    function createSearchArea(searchArea, active) {
        var features = [];
        var pointA = embryo.map.createPoint(searchArea.A.lon, searchArea.A.lat);
        var pointB = embryo.map.createPoint(searchArea.B.lon, searchArea.B.lat);
        var pointC = embryo.map.createPoint(searchArea.C.lon, searchArea.C.lat);
        var pointD = embryo.map.createPoint(searchArea.D.lon, searchArea.D.lat);
        var square = new OpenLayers.Geometry.LinearRing([pointA, pointB, pointC, pointD]);
        features.push(new OpenLayers.Feature.Vector(square, {
            type: "area",
            active: active
        }));
        features.push(new OpenLayers.Feature.Vector(pointA, {
            type: "areaLabel",
            label: "A"
        }));
        features.push(new OpenLayers.Feature.Vector(pointB, {
            type: "areaLabel",
            label: "B"
        }));
        features.push(new OpenLayers.Feature.Vector(pointC, {
            type: "areaLabel",
            label: "C"
        }));
        features.push(new OpenLayers.Feature.Vector(pointD, {
            type: "areaLabel",
            label: "D"
        }));

        return features;
    }

    function addDriftVector(layer, positions) {
        var points = []
        var length = positions.length;
        for (var i = 0; i < length; i++) {
            points.push(embryo.map.createPoint(positions[i].lon, positions[i].lat));
        }
        var features = [new OpenLayers.Feature.Vector(new OpenLayers.Geometry.LineString(points), {
//            renderers: ['SVGExtended', 'VMLExtended', 'CanvasExtended'],
            type: "dv"
        })];
        layer.addFeatures(features);
    }

    function addLKP(layer, lkp) {
        var features = [new OpenLayers.Feature.Vector(embryo.map.createPoint(lkp.lon, lkp.lat), {
            label: "LKP",
            type: "lkpLabel"
        })];
        layer.addFeatures(features);
    }

    function prepareDriftVectors(lkp, twcPositions, leewayPositions) {
        var points = []
        if (lkp) {
            points.push(lkp);
        }
        var length = twcPositions ? twcPositions.length : 0;
        for (var i = 0; i < length; i++) {
            points.push(twcPositions[i]);
            points.push(leewayPositions[i])
        }
        return points;
    }

    this.containsDistanceCircle = function (vessel) {
        function featureFilter(feature) {
            return feature.attributes.id === vessel.mmsi && feature.attributes.type === 'circle';
        }

        return this.containsFeature(featureFilter, this.layers.sar);
    };

    this.containsNearestVessel = function (vessel) {
        function featureFilter(feature) {
            return feature.attributes.id === vessel.mmsi && feature.attributes.type === 'nearest';
        }

        return this.containsFeature(featureFilter, this.layers.sar);
    };

    function addSearchRing(features, circle, label, active) {
        var radiusInKm = nmToMeters(circle.radius) / 1000;
        var attributes = {
            type: 'circle',
            active: active
        }
        features.addFeatures(embryo.adt.createRing(circle.datum.lon, circle.datum.lat, radiusInKm, 1, attributes));

        var center = embryo.map.createPoint(circle.datum.lon, circle.datum.lat);
        features.addFeatures(new OpenLayers.Feature.Vector(center, {
            type: 'circleLabel',
            label: label
        }));
    }

    function addRdv(layer, lkp, datum) {
        addDriftVector(layer, [lkp, datum]);
    }


    /*
     TODO
     Temporary solution
     Should be moved into some general solution in map.js like it is for selectable layers
     */
    embryo.groupChanged(function (e) {
        if (e.groupId == "sar") {
            that.deactivateSelectable();
            //that.controls.modify.activate();
            that.controls.drag.activate();
        } else {
            that.activateSelectable();
            //that.controls.modify.deactivate();
            that.controls.drag.deactivate();
        }

        //this.deactivateSelectable();
        //this.controls.modify.activate();
        //this.activateControls()


        /*
         //Code like below can enable selection of both modifiable features and other features e.g. vessels

         var selectableLayers = this.map.selectLayerByGroup["vessel"];
         selectableLayers = selectableLayers.concat(this.layers.sarEdit);

         this.controls.modify.standalone = true;
         this.controls.modify.activate();
         this.deactivateSelectable();
         this.map.selectControl.setLayer(selectableLayers);

         var that = this;

         this.layers.sarEdit.events.on({
         featureselected: function(evt) { that.controls.modify.selectFeature(evt.feature); },
         featureunselected: function(evt) { that.controls.modify.unselectFeature(evt.feature); }
         });

         this.activateSelectable()
         */
        //this.deactivateControls();
        //this.controls.drag.activate();
        //this.activateControls();
        //this.activateSelectable();

    });



    this.draw = function (sarDocuments) {
        this.layers.sar.removeAllFeatures();
        this.layers.sarEdit.removeAllFeatures();
        for (var index in sarDocuments) {
            if (embryo.sar.Type.SearchArea === sarDocuments[index].docType) {
                this.drawSar(sarDocuments[index]);
            } else if (embryo.sar.Type.EffortAllocation === sarDocuments[index].docType) {
                this.drawEffortAllocationZone(sarDocuments[index]);
            } else if (embryo.sar.Type.SearchPattern === sarDocuments[index].docType) {
                this.drawSearchPattern(sarDocuments[index]);
            }
        }
        this.layers.sar.refresh();
        this.layers.sarEdit.refresh();




    }

    this.drawSar = function (sar) {
        var active = sar.status != embryo.SARStatus.ENDED;
        if (sar.output.datum) {
            addSearchRing(this.layers.sar, sar.output, "Datum", active);

            this.layers.sar.addFeatures(createSearchArea(sar.output.searchArea, active));

            addLKP(this.layers.sar, sar.input.lastKnownPosition);
            addRdv(this.layers.sar, sar.input.lastKnownPosition, sar.output.datum);
            addDriftVector(this.layers.sar, prepareDriftVectors(sar.input.lastKnownPosition, sar.output.currentPositions, sar.output.windPositions))
        } else if (sar.output.downWind) {
            addSearchRing(this.layers.sar, sar.output.downWind, "Datum down wind", active);
            addSearchRing(this.layers.sar, sar.output.min, "Datum min", active);
            addSearchRing(this.layers.sar, sar.output.max, "Datum max", active);

            this.layers.sar.addFeatures(createSearchArea(sar.output.searchArea, active));
            /*
             this.layers.sar.addFeatures(createSearchArea(sar.output.searchArea2), {
             type: 'area'
             });
             */
            addLKP(this.layers.sar, sar.input.lastKnownPosition);
            addRdv(this.layers.sar, sar.input.lastKnownPosition, sar.output.downWind.datum);
            addRdv(this.layers.sar, sar.input.lastKnownPosition, sar.output.min.datum);
            addRdv(this.layers.sar, sar.input.lastKnownPosition, sar.output.max.datum);
            addDriftVector(this.layers.sar, prepareDriftVectors(sar.input.lastKnownPosition, sar.output.currentPositions, sar.output.downWind.datumPositions))
            addDriftVector(this.layers.sar, prepareDriftVectors(null, sar.output.currentPositions, sar.output.min.datumPositions))
            addDriftVector(this.layers.sar, prepareDriftVectors(null, sar.output.currentPositions, sar.output.max.datumPositions))
        }

    };

    this.drawEffortAllocationZone = function (effAll) {
        var area = effAll.area;

        var pointA = embryo.map.createPoint(area.A.lon, area.A.lat);
        var pointB = embryo.map.createPoint(area.B.lon, area.B.lat);
        var pointC = embryo.map.createPoint(area.C.lon, area.C.lat);
        var pointD = embryo.map.createPoint(area.D.lon, area.D.lat);
        var square = new OpenLayers.Geometry.LinearRing([pointA, pointB, pointC, pointD]);
        var feature = new OpenLayers.Feature.Vector(square, {
            type: "zone",
            status: effAll.status,
            label: effAll.name,
            id: effAll._id
        });

        if (effAll.status === embryo.sar.effort.Status.Active) {
            this.layers.sar.addFeatures([feature])
        } else {
            this.layers.sarEdit.addFeatures([feature]);
        }
    };

    this.drawSearchPattern = function (pattern) {
    };
}

SarLayer.prototype = new EmbryoLayer();

/*
 * Can be used to create only one distance layer instance and reuse this as
 */
var SarLayerSingleton = {
    instance: null,
    getInstance: function () {
        return this.instance;
    }
}

embryo.postLayerInitialization(function () {
    SarLayerSingleton.instance = new SarLayer();
    addLayerToMap("sar", SarLayerSingleton.instance, embryo.map);
})


