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

    this.init = function () {
        this.layers.boundingBoxes = new OpenLayers.Layer.Vector("SatelliteBoundingBoxes", {
            styleMap: new OpenLayers.StyleMap({
                "default": new OpenLayers.Style({
                    fillColor: "green",
                    fillOpacity: "0.1",
                    strokeWidth: "1",
                    strokeColor: "#000000",
                    strokeOpacity: "0.2",
                    fontColor: "#000000",
                    fontSize: "12px",
                    fontFamily: "Courier New, monospace",
                    label: "${description}",
                    fontOpacity: "0.5",
                    fontWeight: "bold"
                }, {
                    context: this.context
                }),
                "select": new OpenLayers.Style({
                    fillColor: "green",
                    fillOpacity: "0.5",
                    strokeWidth: "1",
                    strokeColor: "#000",
                    strokeOpacity: "1"
                }, {
                    context: this.context
                })
            })
        });

        this.selectableLayers = [this.layers.boundingBoxes];
        this.selectableAttribute = "tileSet";
    };


    this.draw = function (tileSets) {
        var features = [];

        for (var index in tileSets) {
            var tileSet = tileSets[index];

            if (tileSet.extend) {

                var points = [];
                points.push(embryo.map.createPoint(tileSet.extend.minY, tileSet.extend.minX));
                points.push(embryo.map.createPoint(tileSet.extend.minY, tileSet.extend.maxX));
                points.push(embryo.map.createPoint(tileSet.extend.maxY, tileSet.extend.maxX));
                points.push(embryo.map.createPoint(tileSet.extend.maxY, tileSet.extend.minX));
//                points.push(embryo.map.createPoint(tileSet.extend.minX, tileSet.extend.minY));
//                points.push(embryo.map.createPoint(tileSet.extend.minX, tileSet.extend.maxY));
//                points.push(embryo.map.createPoint(tileSet.extend.maxX, tileSet.extend.maxY));
//                points.push(embryo.map.createPoint(tileSet.extend.maxX, tileSet.extend.minY));
                var ring = new OpenLayers.Geometry.LinearRing(points);
                features.push(new OpenLayers.Feature.Vector(new OpenLayers.Geometry.Polygon([ring]), {
                    tileSet: tileSet.name
                }));
            }

        }

        that.layers.boundingBoxes.removeAllFeatures();
        that.layers.boundingBoxes.addFeatures(features);
    };


    function transformLongitude(longitude) {
        var point = that.map.createPoint(longitude, 0);
        return point.x;
    }

    function transformLatitude(latitude) {
        var point = that.map.createPoint(0, latitude);
        return point.y;
    }

    this.showTiles = function (id, tileSet) {
        if (that.layers && that.layers.satellite) {
            that.removeTiles(tileSet);
        }

        var url = tileSet.url;
        if (url && url.indexOf("${z}/${x}/${y}.png") < 0) {
            if (url.lastIndexOf("/") != (url.length - 1)) {
                url += "/";
            }
            url += "${z}/${x}/${y}.png";
        }

//        var extent = []
//        extent.push(transformLongitude(tileSet.extend.minX));
//        extent.push(transformLatitude(tileSet.extend.minY));
//        extent.push(transformLongitude(tileSet.extend.maxX));
//        extent.push(transformLatitude(tileSet.extend.maxY));
//        var myFilter = new OpenLayers.Filter.Spatial({
//            type: OpenLayers.Filter.Spatial.BBOX,
//            value: OpenLayers.Bounds(extent)})
//        var filterStrategy = new OpenLayers.Strategy.Filter({filter: myFilter});

        that.layers.satellite = new OpenLayers.Layer.OSM("Satellite", url, {
            layers: 'basic',
            isBaseLayer: false,
            tileOptions: {crossOriginKeyword: null}
        });

        that.map.add({
            group: id,
            layer: that.layers.satellite,
            select: false
        });
    }

    this.removeTiles = function (tileSet) {
        if (that.layers && that.layers.satellite) {
            that.map.remove({
                layer: that.layers.satellite
            });
        }
        delete that.layers.satellite;
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