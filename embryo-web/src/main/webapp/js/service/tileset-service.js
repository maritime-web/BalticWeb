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
(function () {
    var module = angular.module('embryo.tileSet.service', [ 'embryo.storageServices' ]);

    module.service('TileSetService', [
        '$http',
        'CookieService',
        function ($http, $interval, CookieService) {
            var service = {
                listByType: function (type, success, error) {
                    var messageId = embryo.messagePanel.show({
                        text: "Requesting list of " + type + " tile sets ..."
                    });

                    function onSuccess(data) {
                        embryo.messagePanel.replace(messageId, {
                            text: "List of " + data.length + " " + type + " tile sets downloaded.",
                            type: "success"
                        });
                        success(data);
                    }

                    function onError(data, status, headers, config) {
                        var txt = "requesting list of " + type + " tile sets";
                        var errorMsg = embryo.ErrorService.errorStatus(data, status, txt);
                        embryo.messagePanel.replace(messageId, {
                            text: errorMsg,
                            type: "error"
                        });
                        error(errorMsg, status);
                    }

                    $http.get(embryo.baseUrl + "rest/tileset/list/" + type, {
                        timeout: embryo.defaultTimeout
                    }).success(onSuccess).error(onError);
                },
                addQualifiers: function (tileSets) {
                    for (var index in tileSets) {
                        var parts = tileSets[index].name.split("_");
                        if (parts[2].indexOf("terra") || parts[2].indexOf("aqua")) {
                            var moreParts = parts[2].split("-");
                            tileSets[index].timeOfDay = moreParts[1].replace("aqua", "P.M.").replace("terra", "A.M.");
                            tileSets[index].qualifier = moreParts[2];
                        }
                    }
                    return tileSets;
                },
                boundingBoxToPolygon: function (tileSets) {
                    for (var index in tileSets) {
                        var bb = tileSets[index].extend;
                        var polygon = [];
                        polygon.push({lat: bb.maxX, lon: bb.maxY});
                        polygon.push({lat: bb.minX, lon: bb.maxY});
                        polygon.push({lat: bb.minX, lon: bb.minY});
                        polygon.push({lat: bb.maxX, lon: bb.minY});
                        tileSets[index].area = polygon;
                    }
                    return tileSets;
                }


            };

            return service;
        } ]);
})();
