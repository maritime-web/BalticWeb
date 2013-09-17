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

    // embryo.angular.controller('embryo.RouteUploadCtrl', [ '$scope',
    // function($scope) {

    embryo.RouteUploadCtrl = function($scope, $routeParams, VoyageService) {
        var initUpload = function() {
            var index;
            if ($routeParams.mmsi) {
                VoyageService.getVoyages($routeParams.mmsi, function(voyages) {
                    $scope.voyages = voyages;

                    if ($routeParams.voyageId) {
                        for (index in $scope.voyages) {
                            if ($scope.voyages[index].id === $routeParams.voyageId) {
                                $scope.selectedVoyage = $scope.voyages[index];
                                return;
                            }
                        }
                    }
                });
            }
        };

        initUpload();

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
            if (typeof $scope.selectedVoyage !== "undefined") {
                data.formData = {
                    voyageId : $scope.selectedVoyage.id
                };
            }
        });
    };

}());
