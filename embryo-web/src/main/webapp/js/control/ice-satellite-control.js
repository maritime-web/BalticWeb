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
$(function () {

    var module = angular.module('embryo.satellite-ice.control', [ 'ui.bootstrap.accordion', 'embryo.control',
        'embryo.tileSet.service']);

    var group = "ice";
    var satellite = new SatelliteLayer();
    addLayerToMap(group, satellite, embryo.map);

    function sortTileSets(data) {
        data.sort(function (ts1, ts2) {
            var s = ts2.ts - ts1.ts;
            if (s != 0 || !ts2.qualifier || !ts1.qualifier) {
                return s;
            }

            return ts1.qualifier.localeCompare(ts2.qualifier);
        });

        var sources = [];
        for (var i in data) {
            var name = data[i].source + " - " + data[i].sourceType
            if (sources.indexOf(name) < 0)
                sources.push(name);
        }
        sources.sort();

        var tileSets = [];
        for (var j in sources) {
            var source = sources[j];
            tileSets.push(source);
            for (var i in data) {
                var name = data[i].source + " - " + data[i].sourceType
                if (name == source) {
                    tileSets.push(data[i]);
                }
            }
        }
        return tileSets;
    }

    function addQualifiers(tileSets) {
        for (var index in tileSets) {
            var parts = tileSets[index].name.split("_");
            if (parts[2].indexOf("terra") || parts[2].indexOf("aqua")) {
                var moreParts = parts[2].split("-");
                tileSets[index].qualifier = moreParts[1] + " " + moreParts[2];
            }
        }
        return tileSets;
    }


    function iceSatelliteController($scope, TileSetService, $timeout) {
        TileSetService.listByType("satellite-ice", function (tileSets) {
            tileSets = addQualifiers(tileSets);
            $scope.tileSets = sortTileSets(tileSets);

            //satellite.draw($scope.tileSets);
        }, function (error) {

        });

        $scope.isSelectedClasses = function (tileSet) {
            if ($scope.selected == tileSet.name) {
                return "alert alert-success";
            }
            return "";
        }

        $scope.formatDate = function (millis) {
            return formatDate(millis);
        }

        $scope.formatDateTime = function (millis) {
            return formatTime(millis);
        }

        $scope.displayTileSet = function ($event, tileSet) {
            $event.preventDefault();

            console.log(tileSet);

            satellite.showTiles(group, tileSet);
//            delete chartsDisplayed[chart.type];
        }

        $scope.hideTileSet = function ($event, tileSet) {
            $event.preventDefault();

            //delete chartsDisplayed[chart.type];

            satellite.removeTiles(tileSet);
        }

        $scope.isDisplayed = function (tileSet) {
            return satellite.isDisplayed(tileSet);
        }

        $scope.zoom = function ($event, chart) {
            $event.preventDefault();
            satellite.zoomToExtent();
        }


        satellite.select("tileSet", function (tileSetName) {
            console.log(tileSetName);
            if (tileSetName) {
                $scope.selected = tileSetName;
            } else {
                $scope.selected = null;
            }

            if (!$scope.$$phase) {
                $scope.$apply(function () {
                });
            }
        });
    }

    module.controller("SatelliteIceController", [ '$scope', 'TileSetService', '$timeout', iceSatelliteController ]);

});
