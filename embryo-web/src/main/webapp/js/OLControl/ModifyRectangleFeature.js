/* Copyright (c) 2006-2013 by OpenLayers Contributors (see authors.txt for
 * full list of contributors). Published under the 2-clause BSD license.
 * See license.txt in the OpenLayers distribution or repository for the
 * full text of the license. */

/**
 * @requires OpenLayers/Control.js
 * @requires OpenLayers/Handler/Drag.js
 */

/**
 * Class: embryo.Control.ModifyRectangleFeature
 * Control to modify rectangular features.  When dragged feature is moved on the map.
 * When un clicked, either after dragging or after simple click, points
 * are rendered in the center of each rectangle edge. These points can be used to drag
 * each edge in perpendicular direction to the edge itself. While doing this
 * the area of the rectangle is maintained by moving the two perpendicular edges
 * in the rectangle
 * Create a new control with the <embryo.Control.ModifyFeature> constructor.
 *
 * Inherits From:
 *  - <OpenLayers.Control>
 */

if (!embryo.Control) {
    embryo.Control = {};
}

embryo.Control.ModifyRectangleFeature = OpenLayers.Class(OpenLayers.Control, {


    /**
     * APIProperty: documentDrag
     * {Boolean} If set to true, dragging vertices will continue even if the
     *     mouse cursor leaves the map viewport. Default is false.
     */
    documentDrag: false,

    /**
     * APIProperty: geometryTypes
     * {Array(String)} To restrict modification to a limited set of geometry
     *     types, send a list of strings corresponding to the geometry class
     *     names.
     */
    geometryTypes: ["OpenLayers.Geometry.LinearRing"],

    /**
     * APIProperty: clickout
     * {Boolean} Unselect features when clicking outside any feature.
     *     Default is true.
     */
    clickout: true,

    /**
     * APIProperty: toggle
     * {Boolean} Unselect a selected feature on click.
     *      Default is true.
     */
    toggle: true,

    /**
     * APIProperty: standalone
     * {Boolean} Set to true to create a control without SelectFeature
     *     capabilities. Default is false.  If standalone is true, to modify
     *     a feature, call the <selectFeature> method with the target feature.
     *     Note that you must call the <unselectFeature> method to finish
     *     feature modification in standalone mode (before starting to modify
     *     another feature).
     */
    standalone: false,

    /**
     * Property: layer
     * {<OpenLayers.Layer.Vector>}
     */
    layer: null,

    /**
     * Property: feature
     * {<OpenLayers.Feature.Vector>} Feature currently available for modification.
     */
    feature: null,

    /**
     * Property: vertex
     * {<OpenLayers.Feature.Vector>} Vertex currently being modified.
     */
    vertex: null,

    /**
     * Property: virtualVertices
     * {Array(<OpenLayers.Feature.Vector>)} Virtual vertices in the middle
     *     of each edge.
     */
    virtualVertices: null,

    /**
     * Property: handlers
     * {Object}
     */
    handlers: null,

    /**
     * APIProperty: virtualStyle
     * {Object} A symbolizer to be used for virtual vertices.
     */
    virtualStyle: null,

    /**
     * APIProperty: vertexRenderIntent
     * {String} The renderIntent to use for vertices. If no <virtualStyle> is
     * provided, this renderIntent will also be used for virtual vertices, with
     * a fillOpacity and strokeOpacity of 0.3. Default is null, which means
     * that the layer's default style will be used for vertices.
     */
    vertexRenderIntent: null,

    /**
     * APIProperty: mode
     * {Integer} Bitfields specifying the modification mode. Defaults to
     *      embryo.sar.ModifyRectangleFeature.RESHAPE. To set the mode to a
     *      combination of options, use the | operator. For example, to allow
     *      the control to both resize and rotate features, use the following
     *      syntax
     * (code)
     * control.mode = embryo.sar.ModifyRectangleFeature.RESIZE |
     *                embryo.sar.ModifyRectangleFeature.ROTATE;
     *  (end)
     */
    mode: null,

    /**
     * Property: modified
     * {Boolean} The currently selected feature has been modified.
     */
    modified: false,

    /**
     * Property: radiusHandle
     * {<OpenLayers.Feature.Vector>} A handle for rotating/resizing a feature.
     */
    radiusHandle: null,

    /**
     * Constructor: embryo.Control.ModifyRectangleFeature
     * Create a new modify feature control.
     *
     * Parameters:
     * layer - {<OpenLayers.Layer.Vector>} Layer that contains features that
     *     will be modified.
     * options - {Object} Optional object whose properties will be set on the
     *     control.
     */
    initialize: function (layer, options) {
        options = options || {};
        this.layer = layer;
        this.virtualVertices = [];
        this.virtualStyle = OpenLayers.Util.extend({},
            this.layer.style ||
            this.layer.styleMap.createSymbolizer(null, options.vertexRenderIntent)
        );
        this.virtualStyle.fillOpacity = 0.3;
        this.virtualStyle.strokeOpacity = 0.3;
        this.mode = embryo.Control.ModifyRectangleFeature.RESHAPE;
        OpenLayers.Control.prototype.initialize.apply(this, [options]);

        // configure the drag handler
        var dragCallbacks = {
            down: function (pixel) {
                this.vertex = null;

                var feature = this.layer.getFeatureFromEvent(
                    this.handlers.drag.evt);
                if (feature) {
                    this.dragStart(feature, pixel);
                } else if (this.clickout && this.feature) {
                    this.unselectFeature(this.feature);
                }
            },
            move: function (pixel) {
                delete this._unselect;
                if (this.vertex) {
                    this.dragVertex(this.vertex, pixel);
                } else if (this.feature && (this.mode & embryo.Control.ModifyRectangleFeature.DRAG)) {
                    this.dragFeature(this.feature, pixel);
                }
            },
            up: function () {
                var feature = this.layer.getFeatureFromEvent(this.handlers.drag.evt);
                if (this.toggle && this._unselect === feature) {
                    this.unselectFeature(this._unselect);
                }
                this.handlers.drag.stopDown = false;
                this.resetVertices();
            },
            done: function (pixel) {
                if (this.vertex) {
                    this.dragComplete(this.vertex);
                } else if (this.feature && (this.mode & embryo.Control.ModifyRectangleFeature.DRAG)) {
                    this.dragFeatureComplete();
                }
            }
        };
        var _self = this;
        var dragOptions = {
            documentDrag: this.documentDrag,
            setEvent: function (evt) {
                var feature = _self.feature;
                OpenLayers.Handler.Drag.prototype.setEvent.apply(
                    this, arguments);
            },
            stopDown: false
        };

        // configure the drag handler
        this.handlers = {
            drag: new OpenLayers.Handler.Drag(this, dragCallbacks, dragOptions)
        };

        this.layer.events.on({
            "beforefeatureremoved": function (event) {
                if (_self.feature && _self.feature.id === event.feature.id) {
                    _self.unselectFeature(_self.feature);
                }
            }
        });
    },

    /**
     * Method: createVirtualVertex
     * Create a virtual vertex in the middle of the segment.
     *
     * Parameters:
     * point1 - {<OpenLayers.Geometry.Point>} First point of the segment.
     * point2 - {<OpenLayers.Geometry.Point>} Second point of the segment.
     *
     * Returns:
     * {<OpenLayers.Feature.Vector>} The virtual vertex created.
     */
    createVirtualVertex: function (point1, point2) {
        var x = (point1.x + point2.x) / 2;
        var y = (point1.y + point2.y) / 2;
        var point = new OpenLayers.Feature.Vector(
            new OpenLayers.Geometry.Point(x, y),
            null, this.virtualStyle
        );
        point._sketch = true;
        return point;
    },

    /**
     * APIMethod: destroy
     * Take care of things that are not handled in superclass.
     */
    destroy: function () {
        if (this.map) {
            this.map.events.un({
                "removelayer": this.handleMapEvents,
                "changelayer": this.handleMapEvents,
                scope: this
            });
        }
        this.layer = null;
        OpenLayers.Control.prototype.destroy.apply(this, []);
    },

    /**
     * APIMethod: activate
     * Activate the control.
     *
     * Returns:
     * {Boolean} Successfully activated the control.
     */
    activate: function () {
        if (OpenLayers.Control.prototype.activate.apply(this, arguments)) {
            this.moveLayerToTop();
            this.map.events.on({
                "removelayer": this.handleMapEvents,
                "changelayer": this.handleMapEvents,
                scope: this
            });
            return this.handlers.drag.activate();
        }
        return false;
    },

    /**
     * APIMethod: deactivate
     * Deactivate the control.
     *
     * Returns:
     * {Boolean} Successfully deactivated the control.
     */
    deactivate: function () {
        var deactivated = false;
        // the return from the controls is unimportant in this case
        if (OpenLayers.Control.prototype.deactivate.apply(this, arguments)) {
            this.moveLayerBack();
            this.map.events.un({
                "removelayer": this.handleMapEvents,
                "changelayer": this.handleMapEvents,
                scope: this
            });
            this.layer.removeFeatures(this.virtualVertices, {silent: true});
            this.handlers.drag.deactivate();
            var feature = this.feature;
            if (feature && feature.geometry && feature.layer) {
                this.unselectFeature(feature);
            }
            deactivated = true;
        }
        return deactivated;
    },


    createGeoPosition: function (point) {
        var position = embryo.map.transformToPosition(point);
        return new embryo.geo.Position(position.lon, position.lat);
    },

    createGeoRectangle: function (points) {
        var geoPositions = [];
        for (var i = 0; i < 4; i++) {
            geoPositions.push(this.createGeoPosition(points[i]));
        }
        return new embryo.geo.Rectangle(geoPositions);
    },


    /**
     * Method: beforeSelectFeature
     * Called before a feature is selected.
     *
     * Parameters:
     * feature - {<OpenLayers.Feature.Vector>} The feature about to be selected.
     */
    beforeSelectFeature: function (feature) {
        return this.layer.events.triggerEvent(
            "beforefeaturemodified", {feature: feature}
        );
    },

    /**
     * APIMethod: selectFeature
     * Select a feature for modification in standalone mode. In non-standalone
     * mode, this method is called when a feature is selected by clicking.
     * Register a listener to the beforefeaturemodified event and return false
     * to prevent feature modification.
     *
     * Parameters:
     * feature - {<OpenLayers.Feature.Vector>} the selected feature.
     */
    selectFeature: function (feature) {
        var indexOf = OpenLayers.Util.indexOf;

        if (this.feature === feature ||
            (this.geometryTypes && indexOf(this.geometryTypes,
                feature.geometry.CLASS_NAME) == -1)) {
            return;
        }
        if (this.beforeSelectFeature(feature) !== false) {
            if (this.feature) {
                this.unselectFeature(this.feature);
            }
            this.feature = feature;

            if (indexOf(this.layer.selectedFeatures, feature) == -1) {
                this.layer.selectedFeatures.push(feature);
            }

            this.layer.drawFeature(feature, 'select');
            this.modified = false;
        }
        // keep track of geometry modifications
        var modified = feature.modified;
        if (feature.geometry && !(modified && modified.geometry)) {
            this._originalGeometry = feature.geometry.clone();
        }
    },

    /**
     * APIMethod: unselectFeature
     * Called when the select feature control unselects a feature.
     *
     * Parameters:
     * feature - {<OpenLayers.Feature.Vector>} The unselected feature.
     */
    unselectFeature: function (feature) {
        delete this._unselect;
        this.layer.destroyFeatures(this.virtualVertices, {silent: true});
        this.virtualVertices = [];
        if (this.radiusHandle) {
            this.layer.destroyFeatures([this.radiusHandle], {silent: true});
            delete this.radiusHandle;
        }
        this.layer.drawFeature(this.feature, 'default');
        this.feature = null;
        OpenLayers.Util.removeItem(this.layer.selectedFeatures, feature);

        if (this.modified) {
            this.layer.events.triggerEvent("afterfeaturemodified", {
                feature: feature
            });
        }
        this.modified = false;
    },


    /**
     * Method: dragStart
     * Called by the drag handler before a feature is dragged.  This method is
     *     used to differentiate between points and vertices
     *     of higher order geometries.
     *
     * Parameters:
     * feature - {<OpenLayers.Feature.Vector>} The point or vertex about to be
     *     dragged.
     */
    dragStart: function (feature, pixel) {
        var isPoint = feature.geometry.CLASS_NAME == 'OpenLayers.Geometry.Point';
        if (!this.standalone &&
            ((!feature._sketch && isPoint) || !feature._sketch)) {
            if (this.toggle && this.feature === feature) {
                // mark feature for unselection
                this._unselect = feature;
            }
            this.selectFeature(feature);
            this.handlers.drag.stopDown = true;
            this.lastPixel = pixel;

            if (this.mode & embryo.Control.ModifyRectangleFeature.DRAG) {
                this.removeVertices();
            }
        }
        if (this.feature &&
            (feature._sketch || isPoint && feature === this.feature)) {
            // feature is a drag or virtual handle or point
            this.vertex = feature;
            this.handlers.drag.stopDown = true;
            this.removeVertices(feature);
        }
        this.zoneGeoRect = this.createGeoRectangle(this.feature.geometry.components);
    },

    /**
     * Method: dragVertex
     * Called by the drag handler with each drag move of a vertex.
     *
     * Parameters:
     * vertex - {<OpenLayers.Feature.Vector>} The vertex being dragged.
     * pixel - {<OpenLayers.Pixel>} Pixel location of the mouse event.
     */
    dragVertex: function (vertex, pixel) {
        var pos = this.map.getLonLatFromViewPortPx(pixel);
        var geom = vertex.geometry;
        var lonDiff = pos.lon - geom.x;
        var latDiff = pos.lat - geom.y;
        geom.move(lonDiff, latDiff);
        this.modified = true;
        /**
         * Five cases:
         * 1) dragging a simple point
         * 2) dragging a virtual vertex
         * 3) dragging a drag handle
         * 4) dragging a real vertex
         * 5) dragging a radius handle
         */
        if (this.feature.geometry.CLASS_NAME == "OpenLayers.Geometry.Point") {
            // dragging a simple point
            this.layer.events.triggerEvent("vertexmodified", {
                vertex: vertex.geometry,
                feature: this.feature,
                pixel: pixel
            });
        } else {
            if (vertex._index) {
                if (vertex._index == -1) {
                    vertex._index = OpenLayers.Util.indexOf(vertex.geometry.parent.components, vertex._next);
                }
                // dragging a center point / virtual vertex in RESHAPE mode
                var feature = this.feature;

                function movePoint(newRect, index) {
                    var position = newRect.positions[index];
                    var lonLat = embryo.map.transformPosition(position.lon, position.lat);

                    var point2 = feature.geometry.components[index];
                    var dx = lonLat.lon - point2.x;
                    var dy = lonLat.lat - point2.y;
                    point2.move(dx, dy);
                }

                try {
                    var geoPos = this.createGeoPosition(geom);
                    var newRect = this.zoneGeoRect.reshapeFixedArea(vertex._index - 1, geoPos)
                    movePoint(newRect, 0);
                    movePoint(newRect, 1);
                    movePoint(newRect, 2);
                    movePoint(newRect, 3);
                } catch (error) {
                    console.log(error);
                }
            }
            this.layer.drawFeature(this.feature, this.standalone ? undefined : 'select');
        }
        // keep the vertex on top so it gets the mouseout after dragging
        // this should be removed in favor of an option to draw under or
        // maintain node z-index
        this.layer.drawFeature(vertex);
    },

    /**
     * Method: dragFeature
     * Called by the drag handler, when the rectangular feature itself is being dragged.
     *
     * Parameters:
     * feature - {<OpenLayers.Feature.Vector>} The feature being dragged.
     */
    dragFeature: function (feature, pixel) {
        var res = this.map.getResolution();
        this.feature.geometry.move(res * (pixel.x - this.lastPixel.x),
            res * (this.lastPixel.y - pixel.y));
        this.layer.drawFeature(this.feature);
        this.lastPixel = pixel;
        this.modified = true;
    },

    /**
     * Method: dragComplete
     * Called by the drag handler when the feature dragging is complete.
     *
     * Parameters:
     * vertex - {<OpenLayers.Feature.Vector>} The vertex being dragged.
     */
    dragComplete: function (feature) {
        this.resetVertices();
        this.setFeatureState();
        this.layer.events.triggerEvent("featuremodified",
            {
                feature: this.feature,
                modified: this.modified
            });
    },

    /**
     * Method: dragComplete
     * Called by the drag handler when the feature dragging is complete.
     *
     * Parameters:
     * vertex - {<OpenLayers.Feature.Vector>} The vertex being dragged.
     */
    dragFeatureComplete: function () {
        if (this.modified) {
            this.layer.events.triggerEvent("featuremodified",
                {
                    feature: this.feature
                });
        }
    },


    /**
     * Method: setFeatureState
     * Called when the feature is modified.  If the current state is not
     *     INSERT or DELETE, the state is set to UPDATE.
     */
    setFeatureState: function () {
        if (this.feature.state != OpenLayers.State.INSERT &&
            this.feature.state != OpenLayers.State.DELETE) {
            this.feature.state = OpenLayers.State.UPDATE;
            if (this.modified && this._originalGeometry) {
                var feature = this.feature;
                feature.modified = OpenLayers.Util.extend(feature.modified, {
                    geometry: this._originalGeometry
                });
                delete this._originalGeometry;
            }
        }
    },

    removeVertices: function (exceptVertice) {
        function removeAllButVertice(vertices, exception) {
            var temp = []
            if (exceptVertice) {
                var index = OpenLayers.Util.indexOf(vertices, exception);
                if (index >= 0) {
                    temp = vertices.splice(index, 1);
                }
            }
            return temp;
        }

        if (this.virtualVertices.length > 0) {
            var temp = removeAllButVertice(this.virtualVertices, exceptVertice);
            this.layer.removeFeatures(this.virtualVertices, {silent: true});
            this.virtualVertices = temp;
        }
        if (this.radiusHandle) {
            this.layer.destroyFeatures([this.radiusHandle], {silent: true});
            this.radiusHandle = null;
        }
    },

    /**
     * Method: resetVertices
     */
    resetVertices: function () {
        this.removeVertices();
        if (this.feature &&
            this.feature.geometry.CLASS_NAME != "OpenLayers.Geometry.Point") {
            if (this.mode & embryo.Control.ModifyRectangleFeature.ROTATE) {
                this.collectRadiusHandle();
            }
            if (this.mode & embryo.Control.ModifyRectangleFeature.RESHAPE) {
                this.collectLineCenterPoints();
            }
        }
    },

    /**
     * Method: collectLineCenterPoints
     * Collect the line center points from the modifiable feature's geometry and push
     *     them on to the control's virtualVertices array.
     */
    collectLineCenterPoints: function () {
        this.virtualVertices = [];
        var control = this;

        function collectComponentCenterPoints(geometry) {
            var i, component, len;

            var numPoint = geometry.components.length;
            if (geometry.CLASS_NAME == "OpenLayers.Geometry.LinearRing") {
                numPoint -= 1;
            }

            for (i = 0; i < numPoint; ++i) {
                component = geometry.components[i];
                if (component.CLASS_NAME != "OpenLayers.Geometry.Point") {
                    collectComponentCenterPoints(component);
                }
            }

            // add virtual vertices in the middle of each edge
            if (geometry.CLASS_NAME != "OpenLayers.Geometry.MultiPoint") {
                var geometry = this.feature.geometry;

                for (i = 0, len = geometry.components.length; i < len - 1; ++i) {
                    var prevCenter = geometry.components[i];
                    var nextCenter = geometry.components[i + 1];
                    if (prevCenter.CLASS_NAME == "OpenLayers.Geometry.Point" &&
                        nextCenter.CLASS_NAME == "OpenLayers.Geometry.Point") {
                        var point = control.createVirtualVertex.call(control, prevCenter, nextCenter);
                        // set the virtual parent and intended index
                        point.geometry.parent = geometry;
                        point._index = i + 1;
                        control.virtualVertices.push(point);
                    }
                }
            }
        }

        collectComponentCenterPoints.call(this, this.feature.geometry);
        this.layer.addFeatures(this.virtualVertices, {silent: true});
    },


    /**
     * Method: collectRadiusHandle
     * Collect the radius handle for the selected geometry.
     */
    collectRadiusHandle: function () {
        var geometry = this.feature.geometry;
        var bounds = geometry.getBounds();
        var center = bounds.getCenterLonLat();
        var originGeometry = new OpenLayers.Geometry.Point(
            center.lon, center.lat
        );
        var radiusGeometry = new OpenLayers.Geometry.Point(
            bounds.right, bounds.bottom
        );
        var radius = new OpenLayers.Feature.Vector(radiusGeometry);
        var rotate = (this.mode & OpenLayers.Control.ModifyRectangleFeature.ROTATE);

        radiusGeometry.move = function (x, y) {
            OpenLayers.Geometry.Point.prototype.move.call(this, x, y);
            var dx1 = this.x - originGeometry.x;
            var dy1 = this.y - originGeometry.y;
            var dx0 = dx1 - x;
            var dy0 = dy1 - y;
            if (rotate) {
                var a0 = Math.atan2(dy0, dx0);
                var a1 = Math.atan2(dy1, dx1);
                var angle = a1 - a0;
                angle *= 180 / Math.PI;
                geometry.rotate(angle, originGeometry);
            }
        };
        radius._sketch = true;
        this.radiusHandle = radius;
        this.radiusHandle.renderIntent = this.vertexRenderIntent;
        this.layer.addFeatures([this.radiusHandle], {silent: true});
    },

    /**
     * Method: setMap
     * Set the map property for the control and all handlers.
     *
     * Parameters:
     * map - {<OpenLayers.Map>} The control's map.
     */
    setMap: function (map) {
        this.handlers.drag.setMap(map);
        OpenLayers.Control.prototype.setMap.apply(this, arguments);
    },

    /**
     * Method: handleMapEvents
     *
     * Parameters:
     * evt - {Object}
     */
    handleMapEvents: function (evt) {
        if (evt.type == "removelayer" || evt.property == "order") {
            this.moveLayerToTop();
        }
    },

    /**
     * Method: moveLayerToTop
     * Moves the layer for this handler to the top, so mouse events can reach
     * it.
     */
    moveLayerToTop: function () {
        var index = Math.max(this.map.Z_INDEX_BASE['Feature'] - 1,
                this.layer.getZIndex()) + 1;
        this.layer.setZIndex(index);

    },

    /**
     * Method: moveLayerBack
     * Moves the layer back to the position determined by the map's layers
     * array.
     */
    moveLayerBack: function () {
        var index = this.layer.getZIndex() - 1;
        if (index >= this.map.Z_INDEX_BASE['Feature']) {
            this.layer.setZIndex(index);
        } else {
            this.map.setLayerZIndex(this.layer,
                this.map.getLayerIndex(this.layer));
        }
    },

    CLASS_NAME: "embryo.Control.ModifyRectangleFeature"
});

/**
 * Constant: RESHAPE
 * {Integer} Constant used to make the control work in reshape mode
 */
embryo.Control.ModifyRectangleFeature.RESHAPE = 1;

/**
 * Constant: ROTATE
 * {Integer} Constant used to make the control work in rotate mode
 */
embryo.Control.ModifyRectangleFeature.ROTATE = 4;
/**
 * Constant: DRAG
 * {Integer} Constant used to make the control work in drag mode
 */
embryo.Control.ModifyRectangleFeature.DRAG = 8;
