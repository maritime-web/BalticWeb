/*
 * Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function SatelliteLayer() {
    var that = this;

    this.context = {
        fillOpacity: function (feature) {
            return feature.attributes.borderOnly ? 0 : 0.1;
        }
    }

    this.init = function () {
        this.layers.boundingBoxes = new OpenLayers.Layer.Vector("SatelliteBoundingBoxes", {
            styleMap: new OpenLayers.StyleMap({
                "default": new OpenLayers.Style({
                    fillColor: "green",
                    fillOpacity: "${fillOpacity}",
                    strokeWidth: "1",
                    strokeColor: "green",
                    strokeOpacity: "0.3",
                    fontColor: "#000000",
                    fontSize: "12px",
                    fontFamily: "Courier New, monospace",
                    fontOpacity: "0.5",
                    fontWeight: "bold"
                }, {
                    context: this.context
                }),
                "select": new OpenLayers.Style({
                    fillColor: "green",
                    fillOpacity: "0.3",
                    strokeWidth: "1",
                    strokeColor: "#000",
                    strokeOpacity: "1"
                })
            }),
            metadata: {
                selectoverlapping: true
            }
        });

        this.selectableLayers = [this.layers.boundingBoxes];
        this.selectableAttribute = "tileSet";
    };

    function createTileSetFeature(tileSet, borderOnly) {
        var points = [];
        for (var j in tileSet.area) {
            points.push(embryo.map.createPoint(tileSet.area[j].lon, tileSet.area[j].lat));
        }
        var ring = new OpenLayers.Geometry.LinearRing(points);
        var feature = new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Polygon([ring]), {
            tileSet: tileSet.name,
            borderOnly: borderOnly
        });
        return feature;
    }

    function drawSatelliteImageBoundingBox() {
        that.layers.boundingBoxes.removeAllFeatures();
        that.select(null);
        if (that.layers && that.layers.satellite) {
            var tileSet = that.layers.satellite.metadata.tileSet;
            that.layers.boundingBoxes.addFeatures([createTileSetFeature(tileSet, true)]);
        }
    }

    function drawTileSetBoundingBoxes(tileSets) {
        var features = [];
        for (var index in tileSets) {
            var tileSet = tileSets[index];
            if (tileSet.area) {
                features.push(createTileSetFeature(tileSet, false));
            }
        }
        that.layers.boundingBoxes.removeAllFeatures();
        that.layers.boundingBoxes.addFeatures(features);
    }


    this.containsFilter = function () {
        return this.containsFeatures() && this.containsFeature(function (feature) {
            return !feature.attributes.borderOnly;
        }, this.layers.boundingBoxes);
    }

    this.draw = function (tileSets) {
        // enable multi select if tile set bounding boxes are drawn.
        // Disable otherwise, because other features must thus be selectable
        that.map.selectMultiple(tileSets && tileSets.length > 0);

        if (!tileSets || tileSets.length <= 0) {
            drawSatelliteImageBoundingBox();
            return;
        }
        drawTileSetBoundingBoxes(tileSets);
    };

    this.showTiles = function (id, tileSet) {
        if (that.layers && that.layers.satellite) {
            that.removeTiles();
        }

        var url = tileSet.url;
        if (url && url.indexOf("${z}/${x}/${y}.png") < 0) {
            if (url.lastIndexOf("/") != (url.length - 1)) {
                url += "/";
            }
            url += "${z}/${x}/${y}.png";
        }
        that.layers.satellite = new OpenLayers.Layer.OSM("Satellite", url, {
            layers: 'basic',
            isBaseLayer: false,
            tileOptions: {crossOriginKeyword: null},
            metadata: {
                zIndex: 0,
                tileSet: tileSet
            }
        });

        that.map.add({
            group: id,
            layer: that.layers.satellite,
            select: false
        });
        drawSatelliteImageBoundingBox();
    }

    this.removeTiles = function () {
        if (that.layers && that.layers.satellite) {
            that.map.remove({
                layer: that.layers.satellite
            });
        }
        delete that.layers.satellite;

        drawSatelliteImageBoundingBox();
    }

    this.isDisplayed = function (tileSet) {
        if (!that.layers || !that.layers.satellite) {
            return false;
        }

        return that.layers.satellite.url.indexOf(tileSet.url) >= 0;
    }

    this.clear = function () {
        this.prototype.clear();
    };
}

SatelliteLayer.prototype = new EmbryoLayer();