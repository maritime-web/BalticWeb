(function() {
    "use strict";

    var module = angular.module('embryo.vessel.controller', [ 'embryo.vessel.service','embryo.controller.reporting' ]);

    embryo.vessel.information =

    module.controller('VesselInformationEditCtrl', [ '$scope', 'VesselService', 'VesselInformation',
            function($scope, VesselService, VesselInformation) {
                $scope.editable = false;

                $scope.helipadOptions = {
                    "Yes" : true,
                    "No" : false
                };

                $scope.provider = {
                    title : "Vessel Information",
                    type : "edit",
                    doShow : false,
                    available : function(vesselOverview, vesselDetails) {
                        if (vesselOverview.inAW) {
                            return {
                                text : "OK",
                                klass : "success",
                                action : "edit"
                            };
                        }
                        return false;
                    },
                    show : function(vesselOverview, vesselDetails) {
                        $("#maxSpeed").focus();
                        this.doShow = true;
                        $scope.vessel = vesselDetails;
                        $scope.message = null;
                        $scope.alertMessages = null;
                    },
                    shown : function(vesselOverview, vesselDetails) {
                        return this.doShow;
                    },
                    close : function() {
                        this.doShow = false;
                    }
                }

                VesselInformation.addInformationProvider($scope.provider);

                $scope.close = function($event) {
                    $event.preventDefault();
                    $scope.provider.close();
                }

                $scope.save = function() {
                    $scope.message = null;
                    $scope.alertMessages = null;
                    VesselService.saveDetails($scope.vessel, function() {
                        $scope.message = "Vessel information successfully submitted";
                    }, function(error, status) {
                        $scope.alertMessages = error;
                    });
                };

                $scope.reset = function() {
                    // TODO find a way to hide these
                    $scope.message = null;
                    $scope.alertMessages = null;
                };
            } ]);

    module.controller('VesselInformationViewCtrl', [ '$scope', 'VesselService', 'VesselInformation', function($scope, VesselService, VesselInformation) {
        $scope.provider = {
            title : "Vessel Information",
            type : "view",
            doShow : false,
            available : function(vesselOverview, vesselDetails) {
                return vesselOverview.inAW;
            },
            show : function(vesselOverview, vesselDetails) {
                this.doShow = true;
                $("#maxSpeed").focus();
                $scope.vessel = vesselDetails;
            },
            shown: function(vo, vd){
                return this.doShow;
            },
            close : function() {
                this.doShow = false;
            }
        };

        VesselInformation.addInformationProvider($scope.provider);

        $scope.close = function($event) {
            $event.preventDefault();
            $scope.provider.close();
        }

    } ]);
}());
