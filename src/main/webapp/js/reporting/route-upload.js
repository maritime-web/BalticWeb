(function() {
    "use strict";

    var route = false;
    var routeUrl = embryo.baseUrl + 'rest/routeUpload/single';
    var scheduleUrl = embryo.baseUrl + 'rest/routeUpload/schedule';

    var module = angular.module('embryo.routeUpload', [ 'embryo.scheduleService', 'embryo.routeService',
            'blueimp.fileupload', 'embryo.controller.reporting' ]);

    module.config([ '$httpProvider', 'fileUploadProvider', function($httpProvider, fileUploadProvider) {
        delete $httpProvider.defaults.headers.common['X-Requested-With'];
        fileUploadProvider.defaults.redirect = window.location.href.replace(/\/[^\/]*$/, '/cors/result.html?%s');
        angular.extend(fileUploadProvider.defaults, {
            maxFileSize : 1000000,
            // Ideally, routes and schedules should make different checks, but
            // this can't be done at config time.
            acceptFileTypes : /map\d{4}t|((\.|\/)(txt|rou|rt3|xls|route))$/i,
            messages : {
                acceptFileTypes : 'File type not allowed. Accepted types are txt, rou, rt3, xls and route.',
                maxFileSize : 'File is too large. Size may not exceed 1 MB.',
            }
        });
    } ]);

    module.controller('RouteUploadCtrl', [
            '$scope',
            'VesselService',
            'ScheduleService',
            'RouteService',
            'VesselInformation',
            '$timeout',
            function($scope, VesselService, ScheduleService, RouteService, VesselInformation, $timeout) {
                function initUpload() {
                    if ($scope.mmsi && $scope.voyageId) {
                        ScheduleService.getVoyageInfo($scope.mmsi, $scope.voyageId, function(voyageInfo) {
                            $scope.voyageInfo = voyageInfo;
                        }, function(errorMsgs) {
                            $scope.alertMessages = errorMsgs;
                        });
                    }
                }

                $scope.provider = {
                    doShow : false,
                    title : "Upload",
                    show : function(context) {
                        if (context) {
                            if (context.schedule) {
                                $scope.lastDeparture = context.departure;
                                route = false;
                            } else {
                                $scope.mmsi = context.vesselDetails.mmsi;
                                $scope.voyageId = context.voyageId;
                                route = true;
                            }
                            $scope.vesselDetails = context.vesselDetails;
                        }
                        $scope.route = route;
                        $scope.options = {
                            url : route ? routeUrl : scheduleUrl,
                            dataType : 'json',
                        };

                        $scope.reset();
                        initUpload();
                        this.doShow = true;
                    },
                    close : function() {
                        this.doShow = false;
                    }

                };

                $scope.close = function($event) {
                    $event.preventDefault();
                    $scope.provider.close();
                }

                embryo.controllers.uploadroute = $scope.provider;
                VesselInformation.addInformationProvider($scope.provider);

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
                };

                // Choosing a new file will replace the old one
                $scope.$on('fileuploadadd', function(e, data) {
                    $scope.reset();
                    var fileExtension = getFileExtension(data);
                    // parse file if browser supports FileReader API and
                    // populate
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

                    VesselInformation.hideAll();
                    embryo.controllers.editroute.show({
                        mmsi : $scope.mmsi,
                        routeId : $scope.uploadedFile.routeId,
                        voyageId : $scope.voyageInfo.id
                    });
                };

                function done(e, data) {
                    if (route) {
                        $.each(data.result.files, function(index, file) {
                            $scope.uploadedFile = file;
                            if ($scope.activate.value) {
                                VesselService.updateVesselDetailParameter($scope.mmsi, "additionalInformation.routeId",
                                        file.routeId);
                            }
                            ScheduleService.clearYourSchedule();
                        });
                    } else {
                        $timeout(function() {
                            embryo.controllers.schedule.updateSchedule({
                                scheduleResponse : data.result,
                                vesselDetails : $scope.vesselDetails,
                            });
                            // $timeout solves the following problem:
                            // 'Closing this provider without $timeout usage,
                            // will cause the DOM to disappear while the
                            // jquery-fileupload
                            // plugin is still handling the response. It may
                            // consequently try to call a method on a fileupload
                            // object no
                            // longer being initialized causing
                            // Error: cannot call methods on fileupload prior to
                            // initialization; attempted to call method 'option'
                            $scope.provider.close();
                        }, 10);
                    }
                }

                $scope.$on('fileuploaddone', done);

                $scope.activeVoyage = function() {
                    if (route) {
                        return $scope.vesselDetails.additionalInformation.routeId == $scope.voyageInfo.routeId;
                    }
                    return null;
                };

                $scope.uploaded = function() {
                    return $scope.uploadedFile !== null && typeof $scope.uploadedFile === "object";
                };

                $scope.reset = function() {
                    // $scope.fileUploadForm.clear(); - coming in Angular 1.1.1
                    $scope.uploadedFile = null;
                    $scope.message = null;
                    $scope.alertMessages = null;
                    $scope.activate = {
                        value : false
                    };
                    $scope.nameFromFile = null;
                    $scope.names = null;

                    if ($scope.$$childHead && $scope.$$childHead.cancel)
                        $scope.$$childHead.cancel();

                    // if already uploaded then cleared by setting queue empty
                    $scope.num = 0;
                    $scope.queue = [];

                };

                $scope.$on('fileuploadsubmit', function(e, data) {
                    $scope.message = null;

                    if ($scope.voyageInfo != null) {
                        data.formData = {
                            voyageId : $scope.voyageInfo.id,
                            active : $scope.activate.value,
                        };

                        if ($scope.nameFromFile) {
                            data.formData.name = $scope.nameFromFile;
                        }
                    } else if (!route) {
                        data.formData = {
                            mmsi : $scope.vesselDetails.mmsi
                        };

                        if($scope.lastDeparture){
                            data.formData.lastDeparture  = $scope.lastDeparture;
                        }
                    }

                });
            } ]);

}());
