var moduleName = 'maritimeweb';

// Define all modules here
angular.module(moduleName + '.map', []);
angular.module(moduleName + '.app', ['angular-growl']);
angular.module(moduleName + '.route', ['angular-growl', 'ngFileUpload']);
angular.module(moduleName + '.vessel', ['yaru22.angular-timeago']);
angular.module(moduleName + '.nw-nm', []);

(function () {
    "use strict";

    var module = angular.module(moduleName, [
        'ngAnimate',
        'ngRoute',
        'ngSanitize',
        'ui.bootstrap',
        'jsonFormatter',
        moduleName + '.map',
        moduleName + '.app',
        moduleName + '.vessel',
        moduleName + '.nw-nm',
        moduleName + '.route',
        'ngFlag',
        'chart.js',
        'yaru22.angular-timeago',
        'iso-3166-country-codes']);
    keycloakInitialize(module, moduleName, false);
})();

