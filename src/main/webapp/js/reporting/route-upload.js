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

    var url = embryo.baseUrl + 'rest/routeUpload/single';

    var module = angular.module('embryo.routeUpload', [ 'embryo.scheduleService', 'embryo.routeService',
            'blueimp.fileupload' ]);

    module.config([ '$httpProvider', 'fileUploadProvider', function($httpProvider, fileUploadProvider) {
        delete $httpProvider.defaults.headers.common['X-Requested-With'];
        fileUploadProvider.defaults.redirect = window.location.href.replace(/\/[^\/]*$/, '/cors/result.html?%s');
        angular.extend(fileUploadProvider.defaults, {
            maxFileSize : 1000000,
            acceptFileTypes : /(\.|\/)(txt|rou|rt3)$/i,
            messages: {
                acceptFileTypes: 'File type not allowed. Accepted types are txt, rou and rt3.',
                maxFileSize: 'File is too large. Size may not exceed 1 MB.',
            }
        });
    } ]);

    embryo.RouteUploadCtrl = function($scope, VesselService, ScheduleService, RouteService) {
        function initUpload() {
            if ($scope.mmsi && $scope.voyageId) {
                ScheduleService.getVoyageInfo($scope.mmsi, $scope.voyageId, function(voyageInfo) {
                    $scope.voyageInfo = voyageInfo;
                }, function(errorMsgs) {
                    $scope.alertMessages = errorMsgs;
                });
            }
        }

        embryo.controllers.uploadroute = {
            show : function(context) {
                embryo.vessel.actions.hide();
                $scope.vesselDetails = context.vesselDetails;
                $scope.mmsi = context.vesselDetails.mmsi;
                $scope.voyageId = context.voyageId;
                $scope.reset();
                initUpload();
                $("#routeUploadPanel").css("display", "block");
            }
        };

        $scope.formatDateTime = function(timeInMillis) {
            return formatTime(timeInMillis);
        };

        $scope.fileSupport = window.FileReader ? true : false;

        // Choosing a new file will replace the old one
        $scope
                .$on(
                        'fileuploadadd',
                        function(e, data) {
                            $scope.queue = [];

                            if (data.files.length > 0 && data.files[0].name.toLocaleLowerCase().match(/.rt3$/)) {
                                $scope.fileExtension = "rt3";

                                // parse file and populate Transas Route
                                // Schedule Names if browser supports FileReader
                                // API
                                if ($scope.fileSupport) {
                                    var f = event.target.files[0];
                                    var reader = new FileReader();
                                    reader.onload = (function(theFile) {
                                        return function(e) {
                                            var xml = jQuery.parseXML(e.target.result);
                                            var xmlDoc = $(xml);
                                            $scope
                                                    .$apply(function() {
                                                        $scope.transasSchedules = xmlDoc.find(
                                                                "Calculations Calculation").map(function(index, elem) {
                                                            return $(elem).attr("CalcName");
                                                        });
                                                        if ($scope.transasSchedules.length > 0) {
                                                            $scope.scheduleName = $scope.transasSchedules[$scope.transasSchedules.length > 1 ? 1
                                                                    : 0];
                                                        }
                                                    });
                                        };
                                    })(f);
                                    reader.readAsText(f);
                                }
                            }
                        });

        $scope.edit = function($event) {
            $event.preventDefault();

            embryo.controllers.editroute.show({
                mmsi : $scope.mmsi,
                routeId : $scope.uploadedFile.routeId,
                voyageId : $scope.voyageInfo.id
            });
        };

        function done(e, data) {
            $.each(data.result.files, function(index, file) {
                $scope.uploadedFile = file;
                if ($scope.activate) {
                    VesselService.updateVesselDetailParameter($scope.mmsi, "additionalInformation.routeId",
                            file.routeId);
                }
                // HACK: need to reload voyage plan
                sessionStorage.clear();
            });
        }

        $scope.options = {
            url : url,
            dataType : 'json',
        };

        $scope.$on('fileuploaddone', done);

        $scope.uploadAndActivate = function() {
            $scope.activate = true;
            $scope.submit();
        };

        $scope.activeVoyage = function() {
            return $scope.vesselDetails.additionalInformation.routeId == $scope.voyageInfo.routeId;
        };

        $scope.uploaded = function() {
            return $scope.uploadedFile !== null && typeof $scope.uploadedFile === "object";
        };

        $scope.reset = function() {
            // $scope.fileUploadForm.clear(); - coming in Angular 1.1.1
            $scope.uploadedFile = null;
            $scope.message = null;
            $scope.alertMessages = null;
            $scope.activate = false;
            $scope.scheduleName = null;
            $scope.fileExtension = null;
            $scope.cancel();
            // if already uploaded then cleared by setting queue empty
            $scope.queue = [];
            
        };

        $scope.$on('fileuploadsubmit', function(e, data) {
            $scope.message = null;

            if ($scope.voyageInfo != null) {
                data.formData = {
                    voyageId : $scope.voyageInfo.id,
                    active : $scope.activate,
                };

                if ($scope.fileExtension == "rt3") {
                    data.formData.schedule = $scope.scheduleName;
                }
            }
        });
    };

}());
