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

    var url = 'rest/routeUpload/single/';

    var module = angular.module('embryo.routeUpload', [ 'embryo.voyageService', 'ui.bootstrap', 'blueimp.fileupload' ]);

    module.config([ '$httpProvider', 'fileUploadProvider', function($httpProvider, fileUploadProvider) {
        delete $httpProvider.defaults.headers.common['X-Requested-With'];
        fileUploadProvider.defaults.redirect = window.location.href.replace(/\/[^\/]*$/, '/cors/result.html?%s');
        if (false) {
            // Demo settings:
            angular.extend(fileUploadProvider.defaults, {
                // Enable image
                // resizing, except for
                // Android and Opera,
                // which actually
                // support image
                // resizing, but fail to
                // send Blob objects via
                // XHR requests:
                disableImageResize : /Android(?!.*Chrome)|Opera/.test(window.navigator.userAgent),
                maxFileSize : 5000000,
                acceptFileTypes : /(\.|\/)(gif|jpe?g|png)$/i
            });
        }
    } ]);

    embryo.RouteUploadCtrl = function($scope, VesselService, VoyageService, RouteService) {
        function initUpload() {
            if ($scope.mmsi, $scope.voyageId) {
                VoyageService.getVoyageInfo($scope.mmsi, $scope.voyageId, function(voyageInfo) {
                    $scope.voyageInfo = voyageInfo;
                });
            }
        };

        embryo.RouteUploadCtrl.show = function(mmsi, voyageId) {
            $scope.mmsi = mmsi;
            $scope.voyageId = voyageId;
            $scope.reset();

            $("#routeUploadPanel").css("display", "block");
        };

        embryo.RouteUploadCtrl.hide = function(mmsi, voyageId) {
            $("#routeUploadPanel").css("display", "hide");
        };

        
        // Choosing a new file will replace the old one
        $scope.$on('fileuploadadd', function(e, data) {
            $scope.queue = [];
        });

        $scope.options = {
            url : url,
            done : function(e, data) {
                $.each(data.result.files, function(index, file) {
                    $scope.message = "Uploaded route file '" + file.name + "'";
                    $scope.uploadedFile = file;

                    console.log(file);
                    
                    if($scope.activate){
                        VesselService.updateVesselDetailParameter($scope.mmsi, "additionalInformation.routeId", file.routeId);
                        $scope.message +=  " and activated the route";
                    }
                    // HACK: need to reload voyage plan
                    sessionStorage.clear();
                });
            }
        };

        $scope.uploadAndActivate = function(){
            $scope.activate=true;
            $scope.submit();
        };
        
        $scope.uploaded = function() {
            return $scope.uploadedFile !== null && typeof $scope.uploadedFile === "object";
        };

        $scope.reset = function() {
            // $scope.fileUploadForm.clear(); - coming in Angular 1.1.1
            $scope.uploadedFile = null;
            $scope.message = null;
            $scope.activate = false;
            $scope.cancel();
            initUpload();
        };

        $scope.$on('fileuploadsubmit', function(e, data) {
            $scope.message = null;

            if (typeof $scope.voyageInfo !== "undefined") {
                data.formData = {
                    voyageId : $scope.voyageInfo.id,
                    active : $scope.activate
                };
            }

        });
    };

}());
