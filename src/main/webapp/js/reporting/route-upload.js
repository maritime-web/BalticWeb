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

    embryo.RouteUploadCtrl = function($scope, $rootScope, $location, VoyageService, RouteService) {
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
            $scope.$apply(function() {
            });

            initUpload();
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
                    $scope.message = "Uploaded route '" + file.name + "'";
                    $scope.uploadedFile = file;
                    // HACK: need to reload voyage plan
                    sessionStorage.clear();
                    RouteService.clearActiveFromCache();
                    $rootScope.$broadcast('yourshipDataUpdated');
                });
            }
        };

        $scope.uploaded = function() {
            return $scope.uploadedFile !== null && typeof $scope.uploadedFile === "object";
        };

        $scope.reset = function() {
            // $scope.fileUploadForm.clear(); - coming in Angular 1.1.1
            $scope.uploadedFile = null;
            $scope.message = null;
            $scope.cancel();
            initUpload();
        };

        $scope.$on('fileuploadsubmit', function(e, data) {
            $scope.message = null;

            if (typeof $scope.voyageInfo !== "undefined") {
                data.formData = {
                    voyageId : $scope.voyageInfo.id,
                    active : $location.path().lastIndexOf('active') > 0
                };
            }

            $rootScope.$broadcast('yourshipDataUpdated');

        });
    };

}());
