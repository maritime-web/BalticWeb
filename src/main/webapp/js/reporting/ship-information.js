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

    var vesselModule = angular.module('embryo.shipInformation', [ 'embryo.vessel' ]);

    embryo.VesselInformationCtrl = function($scope, VesselService) {
        $scope.helipadOptions = {
            "Yes" : true,
            "No" : false
        };

        embryo.authenticated(function() {
            if (embryo.authentication.shipMmsi) {
                embryo.vessel.service.subscribe(embryo.authentication.shipMmsi,
                    function(error, vesselOverview, vesselDetails) {
                        $scope.vessel = vesselDetails;
                        $scope.$apply();
                    }
                );
            }
        });

        $scope.save = function() {
            $scope.message = null;
            VesselService.saveDetails($scope.vessel, function(error, data) {
                $scope.message = "vessel information successfully submitted";
                console.log("data", data);
                $scope.$apply();
            });
        };

        $scope.reset = function() {
            // TODO find a way to hide these
            $scope.message = null;
            $scope.alertMessage = null;
        };
    };

}());
