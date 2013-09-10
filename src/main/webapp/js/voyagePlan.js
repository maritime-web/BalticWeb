/*
 * Dependencies:
 * 
 * aisview.js
 * aisviewUI.js
 * route.js
 * ....
 */

(function() {
	"use strict";

	var berthUrl = 'rest/berth/search';

	embryo.angular.factory('VoyageRestService', function($resource) {
		var defaultParams = {};
		var actions = {
			getActive : {
				params : {
					action : 'active'
				},
				method : 'GET',
				isArray : false,
			}
		};
		return $resource('rest/voyage/:action/:id', defaultParams, actions);
	});

	embryo.angular.factory('VoyageService', function($http, VoyageRestService, ShipService, SessionStorageService) {

		var currentPlan = 'voyagePlan_current';
		var activeVoyage = 'voyage_active';

		return {
			getYourActive : function(onSuccess) {
				var voyageStr = sessionStorage.getItem('activeVoyage');
				if (!voyageStr) {
					ShipService.getYourShip(function(yourShip) {
						var voyage = VoyageRestService.getActive({
							id : yourShip.maritimeId
						}, function() {
							// only cache objects with values (empty
							// objects has ngResource REST methods).
							if (voyage.maritimeId) {
								var voyageStr = JSON.stringify(voyage);
								sessionStorage.setItem('activeVoyage', voyageStr);
							}
							onSuccess(voyage);
						});
					});
				} else {
					onSuccess(JSON.parse(voyageStr));
				}
			},
			getActiveVoyage : function(mmsi, callback) {
				SessionStorageService.getItem(activeVoyage, function(voyageId) {
					if (voyageId && typeof voyageId !== 'undefined') {
						SessionStorageService.getItem()

					}
				});

			},
			getCurrent : function(mmsi, callback) {
				var remoteCall = function(onSuccess) {
					$http.get('rest/voyage/' + mmsi + '/current', {
						responseType : 'json'
					}).success(onSuccess);
				};
				SessionStorageService.getItem(currentPlan, callback, remoteCall);
			},
			getVoyages : function(mmsi, callback) {
				$http.get('rest/voyage/typeahead/' + mmsi, {
					responseType : 'json'
				}).success(callback);
			},
			save : function(plan, callback) {
				$http.put('rest/voyage/savePlan', plan, {
					responseType : 'json'
				}).success(function() {
					SessionStorageService.removeItem(currentPlan);
					callback();
				});
			}
		};
	});

	embryo.VoyagePlanCtrl = function($scope, $routeParams, VoyageService, RouteService) {
		var voyagePlan;

		$scope.mmsi = $routeParams.mmsi;
		
		$scope.options = {
				"Yes" : true,
				"No" : false
		};

		var loadVoyage = function() {
			if ($routeParams.voyage === 'current') {
				VoyageService.getCurrent($scope.mmsi, function(plan) {
					voyagePlan = plan;
					$scope.voyages = voyagePlan.voyages.slice();
					$scope.voyages.push({});
				});
			}

		};
		var loadActiveRoute = function() {
			RouteService.getActive($scope.mmsi, function(route) {
				$scope.activeRoute = route;
			});
		};

		$scope.berths = {
			name : 'embryo_berths',
			prefetch : {
				url : berthUrl,
				// 1 time
				ttl : 3600000
			},
			remote : berthUrl
		};

		loadVoyage();
		loadActiveRoute();

		$scope.getLastVoyage = function() {
			if (!$scope.voyages) {
				return null;
			}
			return $scope.voyages[$scope.voyages.length - 1];
		};

		$scope.$watch($scope.getLastVoyage, function(newValue, oldValue) {
			// add extra empty voyage on initialization
			if (newValue && Object.keys(newValue).length > 0 && Object.keys(oldValue).length === 0) {
				$scope.voyages.push({});
			}
		}, true);

		$scope.del = function(index) {
			$scope.voyages.splice(index, 1);
		};

		$scope.berthSelected = function(voyage, datum) {
			if (typeof datum !== 'undefined') {
				voyage.latitude = datum.latitude;
				voyage.longitude = datum.longitude;
			}
		};

		$scope.isActive = function(voyage) {
			if (!voyage || !voyage.routeId) {
				return false;
			}

			if (!$scope.activeRoute) {
				return false;
			}

			return $scope.activeRoute.id === voyage.routeId;
		};

		$scope.uploadLink = function(voyage) {
//			if (!voyage.maritimeId) {
//				voyage.maritimeId = Math.uuid(17);
//			}

			return '#/routeUpload/' + $scope.mmsi + '/' + voyage.maritimeId;
		};
		
		$scope.routeDisabled = function(voyage){
			return typeof voyage.maritimeId === 'undefined';
		};

		$scope.editRouteLink = function(voyage) {
			if (voyage.routeId) {
				return '#/routeEdit/' + $scope.mmsi + '/' + voyage.routeId;
			}
//			if (!voyage.maritimeId) {
//				voyage.maritimeId = Math.uuid(17);
//			}
			return '#/routeNew/' + $scope.mmsi + '/' + voyage.maritimeId;
		};

		$scope.activate = function(voyage) {
			RouteService.setActiveRoute(voyage.routeId, true, function() {
				loadActiveRoute();
			});
		};
		$scope.deactivate = function(voyage) {
			RouteService.setActiveRoute(voyage.routeId, false, function() {
				loadActiveRoute();
			});
		};

		$scope.reset = function() {
			$scope.message = null;
			$scope.alertMessage = null;
			loadVoyage();
			loadActiveRoute();
		};
		$scope.save = function() {
			// remove last empty element
			voyagePlan.voyages = $scope.voyages.slice(0, $scope.voyages.length - 1);

			VoyageService.save(voyagePlan, function() {
				$scope.message = "Voyage plan saved successfully";
				loadVoyage();
				loadActiveRoute();
			});
		};
	};
}());
