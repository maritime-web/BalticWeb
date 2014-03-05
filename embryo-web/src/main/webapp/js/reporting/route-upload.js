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
            acceptFileTypes : /(\.|\/)(txt|rou|rt3|route)$/i,
            messages : {
                acceptFileTypes : 'File type not allowed. Accepted types are txt, rou, rt3 and route.',
                maxFileSize : 'File is too large. Size may not exceed 1 MB.',
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

        function getFileExtension(data) {
            if (data.files.length > 0) {
                if (data.files[0].name.toLocaleLowerCase().match(/.rt3$/)) {
                    return "rt3";
                } else if (data.files[0].name.toLocaleLowerCase().match(/.route$/)) {
                    return "route";
                }
            }
            return null;
        }

        function chooseDefaultName() {
            if ($scope.names.length > 0) {
                $scope.nameFromFile = $scope.names[$scope.names.length > 1 ? 1 : 0];
            }
        }

        var parsers = {
            "rt3" : function(theFile) {
                return function(e) {
                    var xml = jQuery.parseXML(e.target.result);
                    var xmlDoc = $(xml);
                    $scope.$apply(function() {
                        $scope.names = xmlDoc.find("Calculations Calculation").map(function(index, elem) {
                            return $(elem).attr("CalcName");
                        });
                        chooseDefaultName();
                    });
                };
            },
            "route" : function(theFile) {
                return function(e) {
                    var xml = jQuery.parseXML(e.target.result);
                    var xmlDoc = $(xml);
                    $scope.$apply(function() {
                        $scope.names = xmlDoc.find("Summaries Name").map(function(index, elem) {
                            return $(elem).text();
                        });
                        chooseDefaultName();
                    });
                };
            }
        }

        // Choosing a new file will replace the old one
        $scope.$on('fileuploadadd', function(e, data) {
            $scope.reset();
            var fileExtension = getFileExtension(data);
            // parse file if browser supports FileReader API and populate
            // names dropdown with choices from file
            if ($scope.fileSupport && parsers[fileExtension]) {
                var f = event.target.files[0];
                var reader = new FileReader();
                reader.onload = (parsers[fileExtension])(f);
                reader.readAsText(f);
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
                ScheduleService.clearYourSchedule();
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
            $scope.nameFromFile = null;
            $scope.names = null;
            $scope.cancel();
            
            // if already uploaded then cleared by setting queue empty
            $scope.num = 0;
            $scope.queue = [];

        };

        $scope.$on('fileuploadsubmit', function(e, data) {
            $scope.message = null;

            if ($scope.voyageInfo != null) {
                data.formData = {
                    voyageId : $scope.voyageInfo.id,
                    active : $scope.activate,
                };

                if ($scope.nameFromFile) {
                    data.formData.name = $scope.nameFromFile;
                }
            }
        });
    };

}());