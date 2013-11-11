(function() {
    "use strict";

    embryo.VesselInformationEditCtrl = function($scope, VesselService) {
        $scope.editable = false;

        $scope.helipadOptions = {
            "Yes" : true,
            "No" : false
        };

        embryo.controllers.vesselInformationEdit = {
            title : "Vessel Information",
            available : function(vesselOverview, vesselDetails) {
                return "OK";
            },
            show : function(vesselOverview, vesselDetails) {
                $("#vesselInformationEditPanel").css("display", "block");
                $("#maxSpeed").focus();

                $scope.vessel = vesselDetails;
                $scope.message = null;
                $scope.alertMessage = null;
                $scope.$apply(function() {
                });
            },
            hide : function() {
                $("#vesselInformationEditPanel").css("display", "none");
            }
        };

        $scope.save = function() {
            $scope.message = null;
            VesselService.saveDetails($scope.vessel, function(error, data) {
                $scope.message = "vessel information successfully submitted";
                $scope.$apply();
            });
        };

        $scope.reset = function() {
            // TODO find a way to hide these
            $scope.message = null;
            $scope.alertMessage = null;
        };
    };


    embryo.VesselInformationViewCtrl = function($scope, VesselService) {
        embryo.controllers.vesselInformationView = {
            title : "Vessel Information",
            available : function(vesselOverview, vesselDetails) {
                return vesselOverview.inArcticWeb;
            },
            show : function(vesselOverview, vesselDetails) {
                $("#vesselInformationViewPanel").css("display", "block");
                $("#maxSpeed").focus();

                $scope.vessel = vesselDetails;
                $scope.$apply(function() {
                });
            },
            hide : function() {
                $("#vesselInformationViewPanel").css("display", "none");
            }
        };
    };
}());
