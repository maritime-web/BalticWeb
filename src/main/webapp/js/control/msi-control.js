$(function() {

    var msiLayer;
    embryo.postLayerInitialization(function() {
        msiLayer = new MsiLayer();
        addLayerToMap("msi", msiLayer, embryo.map);
    });

    var module = angular.module('embryo.msi.controllers', [ 'embryo.msi.service' ]);

    module.controller("MsiLayerControl", [ '$scope', 'MsiService', function($scope, MsiService) {
        MsiService.subscribe(function(error, warnings){
            msiLayer.draw(warnings);
        });
    } ]);

    module.controller("MsiControl", [ '$scope', 'MsiService', function($scope, MsiService) {
        $scope.regions = [];
        $scope.warnings = [];
        $scope.selected = {};
        
        MsiService.subscribe(function(error, warnings, regions, selectedRegions){
            for ( var x in regions) {
                if ($.inArray(regions[x].name, selectedRegions) != -1) {
                    regions[x].selected = true;
                }
            }
            $scope.regions = regions;
            $scope.warnings = warnings;
        });

        $scope.showMsi = function() {
            var regionNames = MsiService.regions2Array($scope.regions);
            MsiService.setSelectedRegions(regionNames);
            MsiService.update();
        };

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
    } ]);

});
