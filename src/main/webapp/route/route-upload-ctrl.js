angular.module('maritimeweb.route')
/*******************************************************************
 * Controller that handles uploading an RTZ route for a vessel
 *  Its
 *******************************************************************/
    .controller('RouteUploadCtrl', ['$scope', '$routeParams', '$window', 'VesselService', 'growl', 'timeAgo', '$filter', 'Upload', '$timeout',
        function ($scope, $routeParams, $window, VesselService, growl, timeAgo, $filter, Upload, $timeout) {
            'use strict';

            console.log("RouteUploadCtrl routeParams.mmsi=" + $routeParams.mmsi);

            $scope.$watch('files', function () {
                $scope.upload($scope.files);
            });
            $scope.$watch('file', function () {
                if ($scope.file != null) {
                    $scope.files = [$scope.file];
                }
            });
            $scope.log = '';

            $scope.upload = function (files) {
                if (files && files.length) {
                    for (var i = 0; i < files.length; i++) {
                        var file = files[i];
                        if (!file.$error) {
                            Upload.upload({
                                url: 'http://localhost:5984/planned-route/',
                                //url: 'https://angular-file-upload-cors-srv.appspot.com/upload',
                                contentType: "application/xml",
                                data: {
                                    username: $scope.username,
                                    file: file
                                }
                            }).then(function (resp) {
                                $timeout(function() {
                                    $scope.log = 'file: ' +
                                        resp.config.data.file.name +
                                        ', Response: ' + JSON.stringify(resp.data) +
                                        '\n' + $scope.log;
                                });
                            }, null, function (evt) {
                                var progressPercentage = parseInt(100.0 *
                                    evt.loaded / evt.total);
                                $scope.log = 'progress: ' + progressPercentage +
                                    '% ' + evt.config.data.file.name + '\n' +
                                    $scope.log;
                            });
                        }
                    }
                }
            };

        }]);
