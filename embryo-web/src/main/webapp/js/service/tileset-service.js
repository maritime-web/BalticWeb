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
                }
            };

            return service;
        } ]);
})();
