
describe('Context Menu Controller', function() {

	describe('embryo.contextMenu.Ctrl', function() {

		it('Should create context menu with 2 layers', function() {
			//create map and add base layer
			embryo.mapPanel.map = new OpenLayers.Map({
				div : "map",
				projection : 'EPSG:900913',
				fractionalZoom : false
			});
			
			// add 2 test layers
			embryo.mapPanel.map.addLayer(new OpenLayers.Layer.Vector("Layer 1"));
			embryo.mapPanel.map.addLayer(new OpenLayers.Layer.Vector("Layer 2"));

			var scope = {};
			
			var ctrl = new embryo.contextMenu.Ctrl(scope);
			
			expect(scope.getLayers().length).toBe(2);
			expect(scope.getLayers()[0].name).toBe('Layer 1');
			expect(scope.getLayers()[1].name).toBe('Layer 2');
		});
	});

	describe('embryo.contextMenu.Ctrl.toggleLayer', function() {

		it('Should toggle the visibility of the layer in question', function() {
			//create map and add base layer
			embryo.mapPanel.map = new OpenLayers.Map({
				div : "map",
				projection : 'EPSG:900913',
				fractionalZoom : false
			});
			
			// add 2 test layers
			embryo.mapPanel.map.addLayer(new OpenLayers.Layer.Vector("Layer 1"));
			embryo.mapPanel.map.addLayer(new OpenLayers.Layer.Vector("Layer 2"));

			// Setup controller and data
			var scope = {};
			var ctrl = new embryo.contextMenu.Ctrl(scope);
			var beforeToggle = scope.getLayers()[0].getVisibility();
			
			// test toggle
			scope.toggleLayer(scope.getLayers()[0]);
			expect(scope.getLayers()[0].getVisibility()).toBe(!beforeToggle);
		});
	});
});