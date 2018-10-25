/**
 * Displays milliseconds as a UTC datetime
 */
(function () {
    angular.module('maritimeweb.s-124')
        .filter('toDateTime', ['moment', function (moment) {
            return function (value) {
                return moment(value).utc().format("D MMMM YYYY HH:mm");
            };
        }]);
})();