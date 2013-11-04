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

        embryo.controllers.vesselInformation = {
            title : "Vessel Information",
            status : function(vesselOverview, vesselDetails) {
                var status = {
                    message : "OK",
                    code : "success"
                }
                return status;
            },
            show : function(context) {
                $("#vesselInformationPanel").css("display", "block");
                $("#maxSpeed").focus();

                $scope.vessel = context.vesselDetails;
                $scope.$apply(function() {
                });
            },
            hide : function() {
                $("#vesselInformationPanel").css("display", "none");
            }
        };

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
