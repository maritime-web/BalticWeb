describe('Route Upload Controller', function() {

	describe('RouteUploadCtrl', function() {

        beforeEach(function() {
            module('embryo.routeUpload');
        });

//        beforeEach(inject(function($injector, $provide) {
//			var rootScope = $injector.get('$rootScope');
//			scope = rootScope.$new();
//
//			var VesselService = {
//				getVoyages : function(mmsi) {
//					return []
//				}
//			};
//			
//			$provide.value('VesselService', VesselService);
//			
//			embryo.controllers = {}
//		}));

		/**
		 * Executes tests as unit tests, i.e. no real upload is performed here.
		 * This should be written in end-2-end test
		 */
//		it('uploaded returns true if route has been uploaded with success', inject(function($controller) {
//            var ctrl = $controller("RouteUploadCtrl", {$scope: scope });
//			expect(scope.uploaded()).toBe(false);
//			scope.uploadedFile = {
//				id : 'foo',
//				name : 'uploadedroute.txt'
//			};
//			expect(scope.uploaded()).toBe(true);
//		}));

		/**
		 * Reset clears all state in the page
		 */
		// it('uploaded returns true if route has been uploaded with success',
		// function() {
		// scope.queue = [{
		// name : 'foo.txt',
		// size : 555
		// }];
		//
		// scope.uploadedFile = {
		// id : 'foo',
		// name : 'foo.txt',
		// size : 555
		// };
		// scope.message = "uploaded with success";
		//
		// scope.reset();
		//			
		// expect(scope.queue.length).toBe(empty);
		// expect(scope.message).toBe(null);
		// expect(scope.uploadedFile).toBe(null);
		// });
	});

});