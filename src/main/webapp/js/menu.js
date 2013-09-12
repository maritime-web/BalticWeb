/*
 * Dependencies:
 * 
 * aisview.js
 * aisviewUI.js
 * ....
 */

(function() {
	"use strict";

	embryo.menu = [ {
		text : 'Vessels',
		required : [ 'yourShip' ],
	}, {
		text : 'Ice',
		required : [ 'yourShip' ],
	}

	];

	embryo.user = {
		features : [ 'yourShip', 'selectedShip' ]
	};

	embryo.MenuCtrl = function($scope) {
		$scope.getMenuHeaders = function() {
			return embryo.menu;
		};

		$scope.isVisible = function(menuItem) {
			var index;
			if (!menuItem.required) {
				return true;
			}

			for (index in menuItem.required) {
				if ($.inArray(menuItem.required[index], embryo.user.features) >= 0) {
					return true;
				}
			}

			return false;
		};
	};
}());
