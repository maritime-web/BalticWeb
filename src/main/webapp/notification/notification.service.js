/**
 * Inspired by http://www.codelord.net/2015/05/04/angularjs-notifying-about-changes-from-services-to-controllers/
 */
(function () {
    'use strict';

    angular
        .module('maritimeweb.notification')
        .service('NotifyService', NotifyService);

    NotifyService.$inject = ['$rootScope'];

    function NotifyService($rootScope) {
        var stateMap = new Map();

        this.subscribe = function (scope, event, callback) {
            var handler = $rootScope.$on(event, callback);
            scope.$on('$destroy', handler);
            return handler;
        };

        this.notify = function (event, data) {
            stateMap.set(event, data);
            $rootScope.$emit(event, data);
        };

        this.hasOccurred = function (event) {
            return stateMap.has(event);
        };

        this.getLatest = function (event) {
            return stateMap.get(event);
        }
    }
})();
