/* Copyright Pierre GIRAUD, https://gist.github.com/pgiraud/6131715

 * Published under WTFPL license. 
 * 
 * Customized by Jacob Avlund the Zen master
 * 
 * */

/**
 * @requires OpenLayers/Renderer/SVG.js
 */
OpenLayers.Renderer.SVGExtended = OpenLayers.Class(OpenLayers.Renderer.SVG, {

    eraseGeometry: function(geometry, featureId) {
        this.removeArrows(geometry);
        return OpenLayers.Renderer.SVG.prototype.eraseGeometry.apply(this, arguments);
    },

    drawFeature: function(feature, style) {
        if (feature.geometry) {
            this.removeArrows(feature.geometry);
        }
        return OpenLayers.Renderer.SVG.prototype.drawFeature.apply(this, arguments);
    },

    /**
     * Method: drawLineString
     * Method which extends parent class by also drawing an arrow in the middle
     * of the line to represent it's orientation.
     */
    drawLineString: function(node, geometry) {
        this.drawArrows(geometry, node._style);
        return OpenLayers.Renderer.SVG.prototype.drawLineString.apply(this, arguments);
    }
});

/**
 * @requires OpenLayers/Renderer/Canvas.js
 */
OpenLayers.Renderer.CanvasExtended = OpenLayers.Class(OpenLayers.Renderer.Canvas, {

    eraseGeometry: function(geometry, featureId) {
        this.removeArrows(geometry);
        return OpenLayers.Renderer.Canvas.prototype.eraseGeometry.apply(this, arguments);
    },

    drawFeature: function(feature, style) {
        if (feature.geometry) {
            this.removeArrows(feature.geometry);
        }
        return OpenLayers.Renderer.Canvas.prototype.drawFeature.apply(this, arguments);
    },

    /**
     * Method: drawLineString
     * Method which extends parent class by also drawing an arrow in the middle
     * of the line to represent it's orientation.
     */
    drawLineString: function(geometry, style) {
        this.drawArrows(geometry, style);
        return OpenLayers.Renderer.Canvas.prototype.drawLineString.apply(this, arguments);
    }
});

/**
 * @requires OpenLayers/Renderer/VML.js
 */
OpenLayers.Renderer.VMLExtended = OpenLayers.Class(OpenLayers.Renderer.VML, {

    eraseGeometry: function(geometry, featureId) {
        this.removeArrows(geometry);
        return OpenLayers.Renderer.VML.prototype.eraseGeometry.apply(this, arguments);
    },

    drawFeature: function(feature, style) {
        if (feature.geometry) {
            this.removeArrows(feature.geometry);
        }
        return OpenLayers.Renderer.VML.prototype.drawFeature.apply(this, arguments);
    },

    /**
     * Method: drawLineString
     * Method which extends parent class by also drawing an arrow in the middle
     * of the line to represent it's orientation.
     */
    drawLineString: function(node, geometry) {
        this.drawArrows(geometry, node._style);
        return OpenLayers.Renderer.VML.prototype.drawLineString.apply(this, arguments);
    }
});

OpenLayers.Renderer.prototype.removeArrows = function(geometry) {
    var i;
    // remove any arrow already drawn
    // FIXME may be a performance issue
    var children = this.vectorRoot.childNodes,
        arrowsToRemove = [];
    for (i = 0; i < children.length; i++) {
        var child = children[i];
        if ((geometry.components && child.id.indexOf(geometry.components[0].id + "_arrow") != -1) || child.id.indexOf(geometry.id + "_arrow") != -1) {
            arrowsToRemove.push(child);
        }
    }
    for (i = 0; i < arrowsToRemove.length; i++) {
        this.vectorRoot.removeChild(arrowsToRemove[i]);
    }
};
OpenLayers.Renderer.prototype.drawArrows = function(geometry, style) {
    var i;
    if (style.orientation != "false" && style.orientation) {
        var pts = geometry.components;
        var prevArrow = null,
            distance = null;
        for (i = 0, len = pts.length; i < len - 1; ++i) {
            var prevVertex = pts[i];
            var nextVertex = pts[i + 1];
            var x = (prevVertex.x + nextVertex.x) / 2;
            var y = (prevVertex.y + nextVertex.y) / 2;
            var arrow = new OpenLayers.Geometry.Point(x, y);

            arrow.id = geometry.id + '_arrow_' + i;
            style = OpenLayers.Util.extend({}, style);
            style.graphicName = "arrow";
            style.pointRadius = 4;
            style.strokeDashstyle = "solid"
            style.rotation = this.getOrientation(prevVertex, nextVertex);


            if (prevArrow) {
                var pt1 = embryo.map.internalMap.getPixelFromLonLat(new OpenLayers.LonLat(arrow.x, arrow.y)),
                    pt2 = embryo.map.internalMap.getPixelFromLonLat(new OpenLayers.LonLat(prevArrow.x, prevArrow.y)),
                    w = pt2.x - pt1.x,
                    h = pt2.y - pt1.y;
                distance = Math.sqrt(w*w + h*h);
            }
            // don't draw every arrow, ie. ensure that there is enough space
            // between two
            if (!prevArrow || distance > 40) {
                this.drawGeometry(arrow, style, arrow.id);
                prevArrow = arrow;
            }
        }
    }
};

OpenLayers.Renderer.prototype.getOrientation = function(pt1, pt2) {
    var x = pt2.x - pt1.x;
    var y = pt2.y - pt1.y;

    var rad = Math.acos(y / Math.sqrt(x * x + y * y));
    // negative or positive
    var factor = x > 0 ? 1 : -1;

    return Math.round(factor * rad * 180 / Math.PI);
};

OpenLayers.Renderer.symbol.arrow = [0, 2, 1, 0, 2, 2, 1, 0, 0, 2];