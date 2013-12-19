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
                if (vesselOverview.inArcticWeb) {
                    return { text: "OK", klass: "success", action: "edit" }
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
            },
            hide : function() {
                $("#vesselInformationEditPanel").css("display", "none");
            }
        };

        $scope.save = function() {
            $scope.message = null;
            VesselService.saveDetails($scope.vessel, function(jqXHR, data) {
                if(data != null){
                    $scope.message = "vessel information successfully submitted";
                }else if(jqXHR != null){
                    
                    console.log(jqXHR);
                    
                    $scope.alertMessages = embryo.ErrorService.extractError(jqXHR.statusText, jqXHR.status, null);
                }
                $scope.$apply();
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
