/**
 * Developed by siyfion
 * Located at github: https://github.com/Siyfion/angular-typeahead
 * Fetched 7th of august 2013 from above location
 */
angular.module('siyfion.ngTypeahead', [])
  .directive('ngTypeahead', function () {
    return {
      restrict: 'C',
      scope: {
        datasets: '='
      },
      link: function (scope, element) {
        element.typeahead(scope.datasets);
      }
    };
  });
