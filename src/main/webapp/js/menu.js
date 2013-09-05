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
		text : 'Your Ship',
		required : [ 'yourShip' ],
		menuItems : [ {
			text : 'Zoom to Ship',
			href : '#',
			onClick : ''
		}, {
			text : 'Ship Information',
			href : '#',
		}, {
			text : 'Ship Report',
			href : 'index.html#/report',
			onClick : ''
		}, {
			text : 'Voyage Plan',
			href : '#',
			onClick : ''
		}, {
			text : 'Edit Active Route',
			href : '#',
			onClick : ''
		}, {
			text : 'Upload Active Route',
			href : '#',
			onClick : ''
		} ]
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
