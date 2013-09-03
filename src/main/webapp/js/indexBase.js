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

	embryo.angular = angular.module('embryo', [ 'ngResource' ]);
	// , 'ui.bootstrap'


	embryo.angular.factory('AisRestService', function($resource) {
		var defaultParams = {};
		var actions = {
			findVesselsByMmsi : {
				params : {
					action : 'vessel_search'
				},
				method : 'GET',
				isArray : false,
			}
		};
		return $resource('json_proxy/:action?argument=:mmsi', defaultParams,
				actions);
	});
	
	embryo.angular.factory('SessionStorageService', function() {
		return {
			getItem : function(key, callback, remoteCall) {
				var dataStr = sessionStorage.getItem(key);
				if (!dataStr) {
					var onSuccess = function(data) {
						// only cache objects with values
						if (data && Object.keys(data).length > 0) {
							var dataStr = JSON.stringify(data);
							sessionStorage.setItem(key, dataStr);
						}
						callback(data);
					};

					remoteCall(onSuccess);
				} else {
					var data = JSON.parse(dataStr);
					callback(data);
				}
			},
			setItem : function(key, data) {
				var dataStr = JSON.stringify(data);
				sessionStorage.setItem(key, dataStr);
			}
		};
	});

	embryo.angular.factory('LocalStorageService', function() {
		return {
			getItem : function(key, callback, remoteCall) {
				var dataStr = localStorage.getItem(key);
				if (!dataStr) {
					var onSuccess = function(data) {
						// only cache objects with values
						if (data && Object.keys(data).length > 0) {
							var dataStr = JSON.stringify(data);
							localStorage.setItem(key, dataStr);
						}
						callback(data);
					};

					remoteCall(onSuccess);
				} else {
					var data = JSON.parse(dataStr);
					callback(data);
				}
			},
			setItem : function(key, data) {
				var dataStr = JSON.stringify(data);
				localStorage.setItem(key, dataStr);
			}
		};
	});


	embryo.angular.config([ '$routeProvider', function($routeProvider) {
		$routeProvider.when('/test', {
			templateUrl : 'partials/testPartial.html',
			controller : embryo.GreenPosCtrl
		}).when('/ship', {
			templateUrl : 'partials/shipInformation.html',
			controller : embryo.ShipInformationCtrl
		}).when('/reportlist', {
			templateUrl : 'partials/greenposList.html',
			controller : embryo.GreenPosListCtrl
		}).when('/report/:mmsi', {
			templateUrl : 'partials/greenposReport.html',
			controller : embryo.GreenPosCtrl
		}).otherwise({
			redirectTo : '/report'
		});
		
		// $locationProvider.html5Mode(true);
	} ]);

	embryo.angular.directive('msgRequired', function() {
		return {
			link : function(scope, element, attrs) {
				element.text('Value required');
				element.addClass('msg-invalid');

				attrs.$set('ngShow', attrs.msgRequired + '$error.required'
						&& greenPosForm.gpPersons + '.$dirty');

				// watch the expression, and update the UI on change.
				scope.$watch('greenPosForm.gpPersons',
						function(value, oldValue) {
							console.log(value);
							// console.log(value.$dirty);
							// console.log(value.$error.required);

							// if(value.$dirty && value.$error.required){
							// element.show();
							// }else{
							// element.hide();
							// }
						});
			}
		};

	});
}());
