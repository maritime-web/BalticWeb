function EmbryoLayer() {
    this.layers = [];
    this.controls = [];

    this.redraw = function() {
        for ( var i in this.layers) {
            this.layers[i].redraw();
        }
    }

    this.active = false;

    this.show = function() {
        this.active = true;
        this.redraw();
    }

    this.hide = function() {
        this.active = false;
        this.redraw();
    }

    this.zoomLevels = [];
    this.zoomLevel = 0;

    this.zoom = function(level) {
        var newZoomLevel = 0;
        for ( var i in this.zoomLevels) {
            if (level >= this.zoomLevels[i])
                newZoomLevel = parseFloat(i) + 1;
        }

        if (this.zoomLevel != newZoomLevel) {
            this.zoomLevel = newZoomLevel;
            this.redraw();
        }
    }

    this.selectListeners = [];
    this.selectableLayer = null;
    this.selectableAttribute = null;

    this.bindSelectEvents = function() {
        if (this.selectableLayer == null)
            return;

        var that = this;

        function emit(value) {
            for ( var i in that.selectListeners)
                that.selectListeners[i](value);
        }

        this.selectableLayer.events.on({
            featureselected : function(e) {
                emit(eval("e.feature.attributes." + that.selectableAttribute));
            },
            featureunselected : function(e) {
                emit(null);
            }
        });
    }

    this.select = function(a, b) {
        if (a instanceof Function) {
            this.selectListeners.push(a);
        } else if (b instanceof Function) {
            this.selectListeners[a] = b;
        } else {
            var didSelect = false;
            for ( var i in this.selectableLayer.features) {
                var feature = this.selectableLayer.features[i];
                if (eval("feature.attributes." + this.selectableAttribute) == a) {
                    this.map.select(feature);
                    didSelect = true;
                }
            }
            if (!didSelect)
                this.map.select(null);
        }
    }

    this.clear = function() {
        for ( var i in this.layers)
            this.layers[i].removeAllFeatures();
        for ( var i in this.layers)
            this.layers[i].refresh();
    }

    this.zoomToExtent = function() {
        this.map.zoomToExtent(this.layers);
    }

    this.createGeoDesicLine = function(p1, p2) {
        var generator = new arc.GreatCircle(p1, p2, {
            'foo' : 'bar'
        });
        var line = generator.Arc(100, {
            offset : 10
        });
        
        var points = []
        for(var i in line.geometries){
            for(j in line.geometries[i].coords){
                points.push({x : line.geometries[i].coords[j][0],y : line.geometries[i].coords[j][1]});
            }
        }
            
        return points;
    }

    this.toGeometryPoints = function(points) {
        var geometryPoints = [];
        for ( var index in points) {
            geometryPoints.push(embryo.map.createPoint(points[index].x, points[index].y));
        }
        return geometryPoints;
    }

    this.createGeoDesicLineAsGeometryPoints = function(p1, p2) {
        var generator = new arc.GreatCircle(p1, p2, {
            'foo' : 'bar'
        });
        var line = generator.Arc(100, {
            offset : 10
        });
        
        var points = [];
        for(var i in line.geometries){
            for(j in line.geometries[i].coords){
                points.push(embryo.map.createPoint(line.geometries[i].coords[j][0],line.geometries[i].coords[j][1]));
            }
        }
            
        return points;
    }
}

// This will be moved into map.js later

function addLayerToMap(id, layer, map) {
    layer.map = map;
    layer.init();
    layer.bindSelectEvents();

    for ( var i in layer.layers) {
        map.add({
            group : id,
            layer : layer.layers[i],
            select : layer.layers[i] == layer.selectableLayer
        })
    }

    for ( var i in layer.controls) {
        map.add({
            group : id,
            layer : layer.controls[i]
        })
    }

    map.internalMap.events.register("zoomend", map, function() {
        layer.zoom(map.internalMap.zoom)
    });

    embryo.groupChanged(function(e) {
        if (e.groupId == id) {
            layer.show();
        } else {
            layer.hide();
        }
    });

}
