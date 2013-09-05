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

	embryo.angular.config([ '$httpProvider', 'fileUploadProvider', function($httpProvider, fileUploadProvider) {
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

	//embryo.angular.controller('embryo.RouteUploadCtrl', [ '$scope', function($scope) {

	embryo.RouteUploadCtrl = function($scope){
		
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
		};

		// TODO Find out how to reset prefetch/typeahead upon new ship
		var mmsi = '220443000';
		var vUrl = 'rest/voyage/typeahead/' + mmsi;
		$scope.voyageData = {
			name : 'routeupload_voyages' + mmsi,
			prefetch : {
				url : vUrl,
				// 1 minut
				ttl : 60000
			},
			remote : vUrl
		};

		$scope.$on('fileuploadsubmit', function(e, data) {
			if (typeof $scope.voyageDatum !== "undefined") {
				data.formData = {
					voyageId : $scope.voyageDatum.id
				};
			}
		});

		$scope.$on('$viewContentLoaded', function() {
			$scope.reset();
		});
	};

	embryo.routeUpload = {};
	embryo.routeUpload.Ctrl = function($scope, $element) {

		$scope.open = function(options) {
			$scope.clear();
			if (options && options.preSelectedVoyage) {
				$scope.voyage.isPreselected = true;

				if (options.preSelectedVoyage.id) {
					$scope.voyage.id = options.preSelectedVoyage.id;
					$('#routeUpload').find('form').find('input[name="voyageId"]').val(options.preSelectedVoyage.id);
				}
				if (options.preSelectedVoyage.name) {
					$scope.voyage.name = options.preSelectedVoyage.name;
					$('#routeUpload').find('#voyageName').typeahead('setQuery', options.preSelectedVoyage.name);
				}
			} else {
				$scope.voyage.isPreselected = false;
				$scope.voyage.id = null;
				$scope.voyage.name = null;
			}

			if (options && options.onclose) {
				$scope.onclose = options.onclose;
			}
			$('#routeUpload').find('.modal').modal('show');
		};

	};

}());
