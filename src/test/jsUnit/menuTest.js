/*

describe('Menu Controller', function() {

	describe('embryo.MenuCtrl', function() {

		it('Should create menu with two menu headers and each with two menu items', function() {
			// create map and add base layer
			// add 2 test layers

			embryo.user.menu = embryo.menu = [ {
				text : 'Your Ship',
				required : [ 'yourShip' ],
				menuItems : [ {
					text : 'Zoom to Ship',
					href : '#',
					onClick : ''
				}, {
					text : 'Ship Information',
					href : '#'
				} ]
			}, {
				text : 'Selected Ship',
				required : [ 'yourShip', 'selectedShip' ],
				menuItems : [ {
					text : 'VoyagePlan',
					href : '#'
				}, {
					text : 'Ship Information',
					href : '#'
				} ]
			} ];
			
			
			embryo.user.features = [ 'yourShip' ];
			var scope = {};
			var ctrl = new embryo.MenuCtrl(scope);

			expect(scope.getMenuHeaders().length).toBe(2);
			expect(scope.getMenuHeaders()[0].text).toBe('Your Ship');
			expect(scope.getMenuHeaders()[0].menuItems.length).toBe(2);
			expect(scope.getMenuHeaders()[0].menuItems[0].text).toBe('Zoom to Ship');
			expect(scope.getMenuHeaders()[0].menuItems[1].text).toBe('Ship Information');
			
			expect(scope.getMenuHeaders()[1].text).toBe('Selected Ship');
			expect(scope.getMenuHeaders()[1].menuItems.length).toBe(2);
			expect(scope.getMenuHeaders()[1].menuItems[0].text).toBe('VoyagePlan');
			expect(scope.getMenuHeaders()[1].menuItems[1].text).toBe('Ship Information');
		});

		it('A menu header should only be visible if user has required permissions', function() {
			// create map and add base layer
			// add 2 test layers

			embryo.user.menu = embryo.menu = [ {
				text : 'Your Ship',
				required : [ 'yourShip' ],
				menuItems : [ {
					text : 'Zoom to Ship',
					href : '#',
					onClick : ''
				}, {
					text : 'Ship Information',
					href : '#'
				} ]
			} ];
			
			embryo.user.features = [ 'yourShip' ];
			var scope = {};
			var ctrl = new embryo.MenuCtrl(scope);

			expect(scope.isVisible(scope.getMenuHeaders()[0])).toBe(true);

//			user.features = [ 'somePermission', 'someOtherPermission' ];
//
//			expect(scope.isVisible(scope.getMenuHeaders()[0])).toBe(false);
});
	});

});
*/