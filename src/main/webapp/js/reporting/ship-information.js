(function() {
    "use strict";

    embryo.VesselInformationEditCtrl = function($scope, VesselService) {
        $scope.editable = false;

        $scope.helipadOptions = {
            "Yes" : true,
            "No" : false
        };

        embryo.controllers.vesselInformationEdit = {
            title : "Vessel Information Edit",
            status : function(vesselOverview, vesselDetails) {
                var status = {
                    message : "OK",
                    code : "success"
                }
                return status;
            },
            show : function(context) {
                $("#vesselInformationEditPanel").css("display", "block");
                $("#maxSpeed").focus();

                $scope.vessel = context.vesselDetails;
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


    embryo.VesselInformationViewCtrl = function($scope, VesselService) {
        embryo.controllers.vesselInformationView = {
            title : "Vessel Information View",
            status : function(vesselOverview, vesselDetails) {
                var status = {
                    message : "OK",
                    code : "success"
                }
                return status;
            },
            show : function(context) {
                $("#vesselInformationViewPanel").css("display", "block");
                $("#maxSpeed").focus();

                $scope.vessel = context.vesselDetails;
                $scope.$apply(function() {
                });
            },
            hide : function() {
                $("#vesselInformationViewPanel").css("display", "none");
            }
        };
    };
}());
