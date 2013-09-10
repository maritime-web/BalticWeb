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

	var serviceModule = angular.module('embryo.greenposService',['embryo.storageServices']);
	
	serviceModule.factory('GreenPosRest', function($resource) {
		var defaultParams = {};

		var actions = {
			activate : {
				params : {
					action : 'activate'
				},
				method : 'PUT',
				isArray : false,
			}
		};

		return $resource('rest/greenpos/:action/:id', defaultParams, actions);
	});

}());
