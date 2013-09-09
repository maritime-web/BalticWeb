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
				}).success(function(){
					SessionStorageService.setItem(currentPlan, plan);
					callback();
				});
			}
		};
	});

	embryo.VoyagePlanCtrl = function($scope, $routeParams, VoyageService) {
		var voyagePlan;

		var loadVoyage = function() {
			var mmsi = $routeParams.mmsi;
			if ($routeParams.voyage === 'current') {
				VoyageService.getCurrent(mmsi, function(plan) {
					voyagePlan = plan;
					$scope.voyages = voyagePlan.voyages.slice();
					$scope.voyages.push({});
				});
			}
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

		$scope.getLastVoyage = function() {
			if(!$scope.voyages){
				return null;
			}
			return $scope.voyages[$scope.voyages.length - 1];
		};

		$scope.$watch($scope.getLastVoyage, function(newValue, oldValue) {
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

		$scope.reset = function() {
			$scope.message = null;
			$scope.alertMessage = null;
			loadVoyage();
		};
		$scope.save = function() {
			// remove last empty element
			voyagePlan.voyages = $scope.voyages.slice(0, $scope.voyages.length - 1);

			VoyageService.save(voyagePlan, function() {
				$scope.message = "Voyage plan saved successfully";
			});
		};
	};

	embryo.voyagePlanForm = {};
	embryo.voyagePlanForm.init = function(containerSelector) {
		// TODO remove when DynamicListView is introduced for voyagePlanForm
		$(containerSelector).find('tr:last-child').addClass('emptyRow').find('button').hide();
		$(containerSelector).find('.emptyRow').find('a.dropdown-toggle').hide();
		$(containerSelector).find('.emptyRow').prev('tr').find('a.dropdown-toggle').hide();

		$(containerSelector).find('.emptyRow input[type="text"]').keydown(embryo.voyagePlanForm.copyEmptyRow);

		var $rows = $(containerSelector).find('.table tr:not(.emptyRow)');
		embryo.voyagePlanForm.registerHandlers($rows);

		// Initialize typeahead for all input fields not being empty row
		embryo.typeahead.create(containerSelector + ' tr:not(.emptyRow) input.typeahead-textfield');

		$(containerSelector).find(containerSelector).closest('button[type="submit"]').click(
				embryo.voyagePlanForm.prepareRequest);

		// TODO if berth not typed in, but longitude and lattitude is typed in,
		// then
		// make it impossible to type in berth (until longitude and lattitude
		// are
		// again deleted)
	};

	embryo.voyagePlanForm.copyEmptyRow = function(event) {
		var $row = $(event.target).closest('tr');

		// create new row by copy and modify before insertion into document
		var $newRow = $row.clone(true);
		var columnIndex = $row.find('input').index(event.target);
		$newRow.find('input').eq(columnIndex).val("");
		$row.after($newRow);

		// enableRow must be called after copying new row
		embryo.voyagePlanForm.enableRow($row);

		// if user typed into berth field, then give field focus and trigger
		// drowdown
		if ($(event.target).is('.typeahead-textfield')) {
			$(event.target).focus();
			$(event.target).prev('.tt-hint').trigger('focused');
		}
	};
	embryo.voyagePlanForm.registerHandlers = function($rows) {
		var formObject = this;

		$rows.each(function() {
			$(this).find('input.typeahead-textfield').bind("typeahead:autocompleted typeahead:selected",
					formObject.onBerthSelected($(this)));

			$(this).find('input.lat input.lon').change(formObject.lonLanChanged);
			$(this).find('button').click(formObject.onDelete);
		});
	};

	embryo.voyagePlanForm.onBerthSelected = function($row) {
		return function(event, datum) {
			$row.find('input.lat').val(datum.latitude);
			$row.find('input.lon').val(datum.longitude);
		};
	};

	embryo.voyagePlanForm.onDelete = function(event) {
		event.preventDefault();
		event.stopPropagation();
		var $rowToDelete = $(event.target).closest('tr');
		$rowToDelete.next().find("input:first").focus();
		$rowToDelete.remove();
	};

	embryo.voyagePlanForm.lonLanChanged = function(event) {
		var $lonLan = $(event.target);
		var $inputs = $lonLan.closest('tr').find('input');

		$lan = $inputs.eq(1);
		$lon = $inputs.eq(2);
		if (($lan.val() != null && $lan.val().length > 0) || ($lon.val() != null && $lon.val().length > 0)) {
			$inputs.eq(0).prop('disabled', true);
		} else {
			$inputs.eq(0).removeProp('disabled');
		}
	};
	embryo.voyagePlanForm.deleteRowIfEmpty = function(event) {
		var $row = $(event.target).closest('tr');
		if (!$row.find('input[type="text"]').is(function() {
			// return true if value is present
			return this.value != null && this.value.length > 0;
		})) {
			// if no values are present then delete row
			$row.remove();
		}
	};

	embryo.voyagePlanForm.enableRow = function($row) {
		embryo.typeahead.create($row.find('input.typeahead-textfield')[0]);

		$row.find('button').show();
		$row.prev('tr').find('a.dropdown-toggle').show();
		$row.removeClass('emptyRow');
		$row.find('input, button').unbind('keydown', embryo.voyagePlanForm.copyEmptyRow);

		embryo.voyagePlanForm.registerHandlers($row);
	};

	embryo.voyagePlanForm.prepareRequest = function(containerSelector) {
		var $modalBody = $(containerSelector);
		var $rows = $modalBody.find('tbody tr');
		$modalBody.find('input[name="voyageCount"]').val($rows.length);

		var regex = new RegExp('\\d+', 'g');
		$rows.each(function(index, row) {
			$(row).find('input[name]').each(function(indeks, input) {
				var nameAttr = $(input).attr("name");
				var result = nameAttr.replace(regex, "" + index);
				$(input).attr("name", result);
			});
		});

		return false;
	};

	embryo.typeahead = {};

	embryo.typeahead.init = function(inputSelector, jsonUrl) {

		// Initialize existing typeahead fields
		embryo.typeahead.create(inputSelector);
	};

	// Initialize create function, which can be used both when initializing new
	// rows and during this first initialization
	embryo.typeahead.create = function(selector) {
		$(selector).each(function() {
			var jsonUrl = $(this).attr('data-json');

			// ttl value should be set higher (see doc)
			$(this).typeahead({
				name : 'berths',
				prefetch : {
					url : jsonUrl,
					ttl : 3600000
				},
				remote : {
					url : jsonUrl
				}
			});
		});

	};
}());
