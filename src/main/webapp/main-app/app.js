var moduleName = 'maritimeweb';

// Define all modules here
angular.module('maritimeweb.map', []);
angular.module('maritimeweb.app', ['angular-growl','g1b.datetime-inputs']);
angular.module('maritimeweb.route', ['angular-growl', 'ngFileUpload']);
angular.module('maritimeweb.vessel', ['yaru22.angular-timeago']);
angular.module('maritimeweb.nw-nm', []);
angular.module('maritimeweb.no-go-area', []);
angular.module('maritimeweb.weather', []);
angular.module('maritimeweb.vts-report', []);
angular.module('maritimeweb.vts-map', []);
angular.module('maritimeweb.serviceregistry', []);
angular.module('maritimeweb.nasa-satellite', ['yaru22.angular-timeago']);

(function () {
    "use strict";

    var module = angular.module('maritimeweb', [
        'ngAnimate',
        'ngRoute',
        'ngSanitize',
        'ui.bootstrap',
        'jsonFormatter',
        'maritimeweb.map',
        'maritimeweb.app',
        'maritimeweb.vessel',
        'maritimeweb.nw-nm',
        'maritimeweb.s-124',
        'maritimeweb.no-go-area',
        'maritimeweb.weather',
        'maritimeweb.vts-report',
        'maritimeweb.vts-map',
        'maritimeweb.serviceregistry',
        'maritimeweb.route',
        'maritimeweb.nasa-satellite',
        'maritimeweb.near-miss',
        'ngFlag',
        'chart.js',
        'yaru22.angular-timeago',
        'iso-3166-country-codes']);
    keycloakInitialize(module, 'maritimeweb', false);
})();

