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
                if (vesselOverview.inAW) {
                    return { text: "OK", klass: "success", action: "edit" };
                }
                return false;
            },
            show : function(vesselOverview, vesselDetails) {
                $("#vesselInformationEditPanel").css("display", "block");
                // $("#maxSpeed").focus();

                $scope.vessel = vesselDetails;
                $scope.message = null;
                $scope.alertMessages = null;
                $scope.$apply(function() {
                });
            }
        };

        $scope.save = function() {
            $scope.message = null;
            $scope.alertMessages = null;
            VesselService.saveDetails($scope.vessel, function() {
                $scope.message = "vessel information successfully submitted";
            }, function(error, status){
                $scope.alertMessages = error;
            });
        };

        $scope.reset = function() {
            // TODO find a way to hide these
            $scope.message = null;
            $scope.alertMessages = null;
        };
    };


    embryo.VesselInformationViewCtrl = function($scope, VesselService) {
        embryo.controllers.vesselInformationView = {
            title : "Vessel Information",
            available : function(vesselOverview, vesselDetails) {
                return vesselOverview.inAW;
            },
            show : function(vesselOverview, vesselDetails) {
                $("#vesselInformationViewPanel").css("display", "block");
                $("#maxSpeed").focus();

                $scope.vessel = vesselDetails;
                $scope.$apply(function() {
                });
            }
        };
    };
}());
