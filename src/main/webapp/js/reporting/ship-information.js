/*
 * Dependencies:
 * 
 * aisview.js
 * aisviewUI.js
 * position.js
 * ....
 */

(function() {
    "use strict";

    var vesselModule = angular.module('embryo.shipInformation', [ 'embryo.shipService' ]);

    embryo.VesselInformationCtrl = function($scope, ShipService) {
        $scope.helipadOptions = {
            "Yes" : true,
            "No" : false
        };

        var loadData = function() {
            ShipService.getYourShip(function(vessel) {
                $scope.vessel = vessel;
            });
        };

        $scope.$on('$viewContentLoaded', function(event) {
            loadData();
        });

        $scope.save = function() {
            $scope.message = null;
            ShipService.save($scope.vessel, function() {
                $scope.message = "vessel information successfully submitted";
            });
        };

        $scope.reset = function() {
            // TODO find a way to hide these
            $scope.message = null;
            $scope.alertMessage = null;
            loadData();
        };
    };

}());
