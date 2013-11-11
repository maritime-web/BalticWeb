/**
 * Developed by siyfion Located at github:
 * https://github.com/Siyfion/angular-typeahead Fetched 4th of september 2013
 * from above location
 * Modified a great deal to match our own needs. 
 */

(function() {
    "use strict";

angular.module('siyfion.typeahead', []).directive('siyTypeahead', function() {
    
	return {
		restrict : 'ACE',
		scope : {
			datasets : '=',
			ngModel : '=',
			fnSelected : '&',
			fnAutocompleted : '&'
		},
		link : function(scope, element) {
			var index, inputs;

			element.typeahead(scope.datasets);

			element.on('typeahead:selected', function(event, datum, dataset) {
				scope.$apply(function() {
					if (scope.fnSelected) {
						scope.fnSelected({
							datum : datum
						});
					}
				});
			});
			element.on('typeahead:autocompleted', function(event, datum) {
				scope.$apply(function() {
					if (scope.fnAutocompleted) {
						scope.fnAutocompleted({
							datum : datum
						});
					}
				});
			});

			scope.$watch('ngModel', function(newValue, oldValue) {
				if (typeof scope.ngModel !== 'undefined') {
					element.typeahead('setQuery', scope.ngModel);
				} else {
					element.typeahead('setQuery', "");
				}
			}, true);

			element.on('typeahead:closed', function(event) {
				var inputView = element.data('ttView').inputView;
				scope.$apply(function() {
					if(inputView.getInputValue()){
						scope.ngModel = inputView.getInputValue();
					}
				});
			});
		}
	};
});

}());
