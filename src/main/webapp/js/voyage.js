/*
 * Dependencies:
 * 
 * aisview.js
 * aisviewUI.js
 * route.js
 * ....
 */

"use strict";

embryo.voyageplan = {};

embryo.voyageplan.Ctrl = function($scope, RouteService, Route) {

	// bad style to access DOM.
	// I should pass in the voyage object, when this controller is modified to
	// work without wicket and jQuery
	$scope.uploadRoute = function($event) {

		var $row = $($event.target).closest('tr');
		var $td = $($row.find('td')[0]);
		var voyageName = $td.find('.typeahead-textfield').val();
		var voyageId = $td.find('input[type="hidden"]').val();

		$('.voyagePlan').modal('hide');

		var button = $($event.target).closest('div').find('.btn');
		$('#routeUpload').scope().open({
			preSelectedVoyage : {
				id : voyageId,
				name : voyageName
			},
			onclose : function(params) {
				if (params.route.id) {
					console.log(params.route.id);
					button.attr('data-routeId', params.route.id);
				}
				$('.voyagePlan').modal('show');
			}
		});

	};

	$scope.drawRoute = function($event) {
		$('.voyagePlan').modal('hide');

		var button = $($event.target).closest('div').find('.btn');
		var routeId = button.attr('data-routeId');
		if (routeId) {
			console.log("Drawing route with id: " + routeId);
			embryo.route.fetch(routeId, embryo.route.draw);
		} else {
			alert('No route exists for this voyage');
		}
	};

	$scope.activateRoute = function($event) {
		var button = $($event.target).closest('div').find('.btn');
		var routeId = button.attr('data-routeId');
		if (routeId) {
			console.log("Activating route with id: " + routeId);
			Route.activate(routeId, function() {
				var activeRoute = Route.getActive(function() {
					RouteService.setActive(activeRoute);
					embryo.route.drawActiveRoute();
				});
			});
		} else {
			alert('No route exists for this voyage');
		}
	};

	$scope.editRoute = function($event) {
		$('.voyagePlan').modal('hide');

		var $row = $($event.target).closest('tr');
		var $td = $($row.find('td')[0]);
		var voyageName = $td.find('.typeahead-textfield').val();
		var voyageId = $td.find('input[type="hidden"]').val();

		var button = $($event.target).closest('div').find('.btn');
		var routeId = button.attr('data-routeId');
		if (routeId) {
			var route = Route.get({
				id : routeId
			}, function() {
				RouteService.editRoute(route);
			});

			$('#routeEditModal').scope().open({
				onclose : function(params) {
					if (params.route.id) {
						button.attr('data-routeId', params.route.id);
					}
					$('.voyagePlan').modal('show');
				}

			});
		} else {
			alert('No route exists for this voyage');
		}
	};

	$scope.isLast = function() {
		return false;
	};

};

embryo.voyagePlanForm = {};
embryo.voyagePlanForm.init = function(containerSelector) {
	// TODO remove when DynamicListView is introduced for voyagePlanForm
	$(containerSelector).find('tr:last-child').addClass('emptyRow').find(
			'button').hide();

	$(containerSelector).find('.emptyRow input[type="text"]').keydown(
			embryo.voyagePlanForm.copyEmptyRow);

	var $rows = $(containerSelector).find('.table tr:not(.emptyRow)');
	embryo.voyagePlanForm.registerHandlers($rows);

	// Initialize typeahead for all input fields not being empty row
	embryo.typeahead.create(containerSelector
			+ ' tr:not(.emptyRow) input.typeahead-textfield');

	$(containerSelector).find(containerSelector).closest(
			'button[type="submit"]')
			.click(embryo.voyagePlanForm.prepareRequest);

	// TODO if berth not typed in, but longitude and lattitude is typed in, then
	// make it impossible to type in berth (until longitude and lattitude are
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
		$(this).find('input.typeahead-textfield').bind(
				"typeahead:autocompleted typeahead:selected",
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
	if (($lan.val() != null && $lan.val().length > 0)
			|| ($lon.val() != null && $lon.val().length > 0)) {
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
	$row.removeClass('emptyRow');
	$row.find('input, button').unbind('keydown',
			embryo.voyagePlanForm.copyEmptyRow);

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
				ttl : 30000
			},
			remote : {
				url : jsonUrl
			}
		});
	});

};