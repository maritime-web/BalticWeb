$(function() {

    var msiLayer = new MsiLayer();
    addLayerToMap("msi", msiLayer, embryo.map);

    var module = angular.module('embryo.msi.controllers', [ 'embryo.msi.service' ]);

    module.controller("MsiController", [ '$scope', '$interval', 'MsiService', function($scope, $interval, MsiService) {
        var showRegions = function(data) {
            $scope.regions = data;
            var cookie = getCookie("dma-msi-regions-" + embryo.authentication.userName);
            var savedData = cookie ? JSON.parse(cookie) : getRegionsAsArray(true);
            for ( var x in $scope.regions) {
                if (!savedData || $.inArray($scope.regions[x].name, savedData) != -1) {
                    $scope.regions[x].selected = true;
                }
            }
            requestMsiList(savedData);
        };
        var getRegionsAsArray = function(all) {
            var regions = [];
            for ( var x in $scope.regions) {
                if (all || $scope.regions[x].selected) {
                    regions.push($scope.regions[x].name);
                }
            }
            return regions;
        };
        $scope.showMsi = function() {
            var regions = getRegionsAsArray();
            setCookie("dma-msi-regions-" + embryo.authentication.userName, JSON.stringify(regions), 30);
            requestMsiList(regions);
        };

        $scope.selected = {};

        MsiService.regions(showRegions);

        var requestClosure = function() {
            var regions = getRegionsAsArray();
            requestMsiList(regions);
        };

        $interval(requestClosure, 1 * 60 * 1000 * 60);

        msiLayer.select("msi", function(msi) {
            $scope.selected.open = !!msi;
            $scope.selected.msi = msi;
            if (!$scope.$$phase) {
                $scope.$apply(function() {
                });
            }
        });

        $scope.formatDate = function(timeInMillis) {
            return formatDate(timeInMillis);
        };

        function requestMsiList(regions) {
            var messageId = embryo.messagePanel.show({
                text : "Requesting active MSI warnings ..."
            });

            MsiService.list(regions, function(msiList) {
                msiLayer.clear();
                if (msiList.length == 0) {
                    embryo.messagePanel.replace(messageId, {
                        text : data.length + " MSI warnings returned.",
                        type : "success"
                    });
                    return;
                }
                msiList = msiList.sort(function(a, b) {
                    return b.created - a.created;
                });
                $scope.warnings = msiList;

                msiLayer.draw(msiList);

                embryo.messagePanel.replace(messageId, {
                    text : msiList.length + " MSI warnings returned.",
                    type : "success"
                });
            }, function(errorMsg, status) {
                embryo.messagePanel.replace(messageId, {
                    text : errorMsg,
                    type : "error"
                });
            });

            $scope.selectMsi = function(msi) {
                switch (msi.type) {
                case "Point":
                    embryo.map.setCenter(msi.points[0].longitude, msi.points[0].latitude, 8);
                    break;
                case "Points":
                case "Polygon":
                case "Polyline":
                    embryo.map.setCenter(msi.points[0].longitude, msi.points[0].latitude, 8);
                    break;
                }
                msiLayer.select(msi);
            };
        }
    } ]);

});
