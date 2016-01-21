function EmbryoLayer() {
    this.layers = [];
    this.controls = [];

    this.redraw = function () {
        for (var i in this.layers){
            this.layers[i].redraw();
        }
    };

    this.active = false;

    this.show = function () {
        this.active = true
        this.redraw();
    };

    this.hide = function () {
        this.active = false;
        this.redraw();
    };

    this.containsFeatures = function () {
        for (var index in this.layers) {
            if (this.layers[index].features && this.layers[index].features.length > 0) {
                return true;
            }
        }
        return false;
    };

    this.containsFeature = function (input, layer) {
        function inLayer(input, layer) {
            for (var j in layer.features) {
                if (typeof input == 'function' && input(layer.features[j]))
                    return true;
                else if (layer.features[j].attributes.id === input)
                    return true;
            }
            return false;
        }

        if (layer) {
            return inLayer(input, layer);
        } else {
            for (var index in this.layers) {
                if (inLayer(input, this.layers[index])) {
                    return true;
                }
            }
        }
        return false;
    };

    this.hideFeatures = function (input) {
        function hideFeaturesInLayer(input, layer) {
            var toRemove = [];
            for (var j in layer.features) {
                if (typeof input == 'function' && input(layer.features[j])) {
                    toRemove.push(layer.features[j]);
                } else if (layer.features[j].attributes.id === input) {
                    toRemove.push(layer.features[j]);
                }
            }
            layer.removeFeatures(toRemove);
        }

        for (var index in this.layers) {
            hideFeaturesInLayer(input, this.layers[index]);
        }
    };


    this.zoomLevels = [];
    this.zoomLevel = 0;

    this.zoom = function (level) {
        var newZoomLevel = 0;
        for (var i in this.zoomLevels) {
            if (level >= this.zoomLevels[i])
                newZoomLevel = parseFloat(i) + 1;
        }

        if (this.zoomLevel != newZoomLevel) {
            this.zoomLevel = newZoomLevel;
            this.redraw();
        }
    };
    
    this.zoomToCoords = function(minPoint, maxPoint) {
		this.map.zoomToCoords(minPoint, maxPoint);
    };

    this.zoomToFeatures = function (input, force) {

        function extendBoundsFromFeaturesInLayer(bounds, input, layer) {
            for (var j in layer.features) {
                if (typeof input == 'function' && input(layer.features[j])) {
                    bounds.extend(layer.features[j].geometry.getBounds());
                } else if (layer.features[j].attributes.id === input) {
                    bounds.extend(layer.features[j].geometry.getBounds());
                }
            }
            return bounds;
        }

        var bounds = new OpenLayers.Bounds();
        for (var index in this.layers) {
            bounds = extendBoundsFromFeaturesInLayer(bounds, input, this.layers[index]);
        }
        if (bounds && bounds.left && bounds.right && bounds.bottom && bounds.top) {
            this.map.zoomToBounds(bounds, force);
        }
    };


    this.selectListeners = [];
    this.selectableLayers = null;
    this.selectableAttribute = null;

    this.bindSelectEvents = function () {
        if (!this.selectableLayers)
            return;

        var that = this;

        function emit(value) {
            for (var i in that.selectListeners)
                that.selectListeners[i](value);
        }

        for (var l in this.selectableLayers) {
            this.selectableLayers[l].events.on({
                featureclick: function (e) {
                    // featureclick enables support for selection of overlapping polygons
                    // SelectControl on map has to be disabled for this to work
                    if (e.feature.layer.metadata && e.feature.layer.metadata.selectoverlapping) {
                        emit(eval("e.feature.attributes." + that.selectableAttribute));
                        e.feature.layer.drawFeature(
                            e.feature,
                            'select'
                        );
                    }
                },
                nofeatureclick: function (e) {
                    // nofeatureclick enables support for selection of overlapping polygons
                    // SelectControl on map has to be disabled for this to work
                    emit(null);
                    if (e.layer.metadata && e.layer.metadata.selectoverlapping) {
                        for (var index in e.layer.features) {
                            e.layer.drawFeature(
                                e.layer.features[index],
                                'default'
                            );
                        }
                    }
                },
                featureselected: function (e) {
                    if (e.feature.cluster) {
                        var result = [];
                        for (var i in e.feature.cluster) {
                            result.push(eval("e.feature.cluster[i].attributes." + that.selectableAttribute));
                        }
                        emit(result);
                    } else {
                        emit(eval("e.feature.attributes." + that.selectableAttribute));
                    }
                },
                featureunselected: function (e) {
                    emit(null);
                }
            });
        }
    };

    this.select = function (a, b) {
        if (a instanceof Function) {
            this.selectListeners.push(a);
        } else if (b instanceof Function) {
            this.selectListeners[a] = b;
        } else {
            var didSelect = false;
            for (var l in this.selectableLayers) {
                var layer = this.selectableLayers[l];
                for (var i in layer.features) {
                    var feature = layer.features[i];
                    // this.selectableAttribute may contain object property expression, e.g. vessel.mmsi
                    if (eval("feature.attributes." + this.selectableAttribute) == a) {
                        this.map.select(feature);
                        didSelect = true;
                    }
                }
            }
            if (!didSelect)
                this.map.select(null);
        }
    };

    this.clear = function () {
        for (var i in this.layers)
            this.layers[i].removeAllFeatures();
        for (var i in this.layers)
            this.layers[i].refresh();
    };
    
    this.activateSelectable = function(){
    	this.map.activateSelectable();
    };
    
    this.deactivateSelectable = function(){
    	this.map.deactivateSelectable();
    };
    
    this.activateControls = function(){
        for (var key in this.controls) {
            var control = this.controls[key];
            if(!control.active) {
            	control.activate();
            }
        }    	
    };
    
    this.deactivateControls = function(){
        for (var key in this.controls) {
            var control = this.controls[key];
            if(control.active) {
            	control.deactivate();
            }
        }    	
    };
    
    this.zoomToExtent = function () {
        this.map.zoomToExtent(this.layers);
    };

    this.createGeoDesicLine = function (p1, p2) {
        var generator = new arc.GreatCircle(p1, p2, {
            'foo': 'bar'
        });
        var line = generator.Arc(100, {
            offset: 10
        });

        var points = [];
        for (var i in line.geometries) {
            for (j in line.geometries[i].coords) {
                points.push({
                    x: line.geometries[i].coords[j][0],
                    y: line.geometries[i].coords[j][1]
                });
            }
        }

        return points;
    };

    this.toGeometryPoints = function (points) {
        var geometryPoints = [];
        for (var index in points) {
            geometryPoints.push(embryo.map.createPoint(points[index].x, points[index].y));
        }
        return geometryPoints;
    };

    this.createGeoDesicLineAsGeometryPoints = function (p1, p2) {
        var generator = new arc.GreatCircle(p1, p2, {
            'foo': 'bar'
        });
        var line = generator.Arc(100, {
            offset: 10
        });

        var points = [];
        for (var i in line.geometries) {
            for (j in line.geometries[i].coords) {
                points.push(embryo.map.createPoint(line.geometries[i].coords[j][0], line.geometries[i].coords[j][1]));
            }
        }

        return points;
    };
}

// This will be moved into map.js later

function addLayerToMap(id, layer, map) {
    layer.map = map;
    layer.init();
    layer.bindSelectEvents();

    for (var i in layer.layers) {
        var select = false;
        for (var l in layer.selectableLayers) {
            if (layer.selectableLayers[l] == layer.layers[i]) {
                select = true;
                break;
            }
        }
        map.add({
            group: id,
            layer: layer.layers[i],
            select: select
        });
    }
 
	for (var i in layer.controls) {
        map.internalMap.addControl(layer.controls[i]);
    }

    // initialize layer zoomLevel value
    layer.zoom(map.internalMap.zoom);
    // register listener for future zoom level changes.
    layer.zoomListener = function () {
        layer.zoom(map.internalMap.zoom);
    }
    map.internalMap.events.register("zoomend", map, layer.zoomListener);

    embryo.groupChanged(function (e) {
        if (e.groupId == id) {
            layer.show();
        } else {
            layer.hide();
        }
    });

}
