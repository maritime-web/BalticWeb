/**
 * Developed by siyfion
 * Located at github: https://github.com/Siyfion/angular-typeahead
 * Fetched 4th of september 2013 from above location
 */
angular.module('siyfion.ngTypeahead', [])
  .directive('ngTypeahead', function () {
    return {
      restrict: 'ACE',
      scope: {
        datasets: '=',
        ngModel: '='
      },
      link: function (scope, element) {
        element.typeahead(scope.datasets);

        // Updates the ngModel binding when a value is manually selected from the dropdown.
        // ToDo: Think about how the value could be updated on user entry...
        element.bind('typeahead:selected', function (object, datum) {
          scope.$apply(function() {
            scope.ngModel = datum;
          });
        });

        // Updates the ngModel binding when a query is autocompleted.
        element.bind('typeahead:autocompleted', function (object, datum) {
          scope.$apply(function() {
            scope.ngModel = datum;
          });
        });
      }
    };
  });