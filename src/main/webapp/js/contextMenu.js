
embryo.mapPanel.contextMenu = function(params) {
	// A control class for capturing click events...
	// OpenLayers.Control.Click = OpenLayers.Class(OpenLayers.Control, {
	//
	// defaultHandlerOptions : {
	// 'single' : true,
	// 'double' : true,
	// 'pixelTolerance' : 0,
	// 'stopSingle' : false,
	// 'stopDouble' : false
	// },
	// handleRightClicks : true,
	// initialize : function(options) {
	// this.handlerOptions = OpenLayers.Util.extend({},
	// this.defaultHandlerOptions);
	// OpenLayers.Control.prototype.initialize.apply(this, arguments);
	// this.handler = new OpenLayers.Handler.Click(this,
	// this.eventMethods, this.handlerOptions);
	// },
	// CLASS_NAME : "OpenLayers.Control.Click"
	//
	// });

	// Add an instance of the Click control that listens to various click
	// events:
	// var oClick = new OpenLayers.Control.Click({
	// eventMethods : {
	// 'rightclick' : function(e) {
	// console.log('rightclick with event:');
	// console.log(e);
	// },
	// 'dblrightclick' : function(e) {
	// console.log('dblrightclick at ' + e.xy.x + ',' + e.xy.y);
	// }
	// }
	// });

	// embryo.mapPanel.map.addControl(oClick);
	// oClick.activate();

};

embryo.contextMenu = {
	init : function() {
		// Get control of the right-click event:
		document.getElementById('map').oncontextmenu = function(e) {
			e = e ? e : window.event;
			if (e.preventDefault)
				e.preventDefault(); // For non-IE browsers.
			else
				return false; // For IE browsers.
		};

		$('#map').contextmenu(
				{
					target : '#context-menu',
					before : function(e) {
						embryo.contextMenu.selectedFeature = embryo.route.layer
								.getFeatureFromEvent(e);

						// Provoke an update of rendered menus
						angular.element($('#context-menu')).scope().$apply(
								function() {
								});
						return true;
					}
				});
	},

	menuItems : [],

	addMenuItems : function(newItems) {

		var context = this;
		// This method may be called from outside angular.
		// Make sure angular discovers the update.
		angular.element($('#context-menu')).scope().$apply(function() {
			context.menuItems = context.menuItems.concat(newItems);
		});
	},

	Ctrl : function($scope) {
		$scope.getMenuItems = function() {
			return embryo.contextMenu.menuItems;
		};
		$scope.getLayers = function() {
			if(embryo.map.internalMap){
				return embryo.map.internalMap.layers;
			}
			return [];
		};
		
		$scope.getSelectedFeature = function() {
			return embryo.contextMenu.selectedFeature;
		};
		$scope.shown = function(item, feature) {
			var feature = this.getSelectedFeature();
			var featureType = feature && feature.data ? feature.data.featureType
					: null;

			if (item.shown4FeatureType) {
				return item.shown4FeatureType === featureType;
			}

			if (item.shown) {
				return item.shown(feature);
			}

			return true;
		};

		$scope.choose = function(item, feature) {
			item.choose($scope, feature);
		};
		$scope.toggleLayer = function(layer) {
			layer.setVisibility(!layer.getVisibility());
		};
		
		$scope.getIcon = function(layer){
			return layer.getVisibility()? 'icon-ok' : 'icon-ban-circle'; 
		};
	}
};