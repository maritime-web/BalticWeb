var moduleName = 'maritimeweb';

// Define all modules here
angular.module('maritimeweb.map', []);
angular.module('maritimeweb.app', ['angular-growl']);
angular.module('maritimeweb.route', ['angular-growl', 'ngFileUpload']);
angular.module('maritimeweb.vessel', ['yaru22.angular-timeago']);
angular.module('maritimeweb.nw-nm', []);
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
        'maritimeweb.route',
        'maritimeweb.nasa-satellite',
        'ngFlag',
        'chart.js',
        'yaru22.angular-timeago',
        'iso-3166-country-codes']);
    keycloakInitialize(module, 'maritimeweb', false);
})();

