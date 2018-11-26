(function () {
    'use strict';

    angular
        .module('maritimeweb.near-miss')
        .directive('nearMissDataSetDisplay', nearMissDataSetDisplay);

    function nearMissDataSetDisplay() {
        return {
            restrict: 'E',
            templateUrl: './near-miss/near-miss-data-set-display.html',
            scope: {
                data: '=?'
            },
            link: link
        };

        function link(scope) {
            scope.animate = function(nm) {
                scope.data.animateNearMiss(nm);
                scope.data.zoomNearMiss(null);
            };

            scope.zoom = function(nm) {
                scope.data.zoomNearMiss(nm);
                scope.data.animateNearMiss(null);
            };
        }
    }
})();