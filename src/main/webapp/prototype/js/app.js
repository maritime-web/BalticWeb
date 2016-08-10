
// Define all modules here
angular.module('maritimeweb.map', []);
angular.module('maritimeweb.app', ['angular-growl']);
angular.module('maritimeweb.route', ['angular-growl']);
angular.module('maritimeweb.vessel', ['yaru22.angular-timeago']);
angular.module('maritimeweb.nw-nm', []);

(function () {
    "use strict";

    var moduleName = 'maritimeweb';
    var module = angular.module(moduleName, ['ngAnimate', 'ngSanitize', 'ui.bootstrap',
        'maritimeweb.map', 'maritimeweb.app', 'maritimeweb.vessel', 'maritimeweb.nw-nm', 'ngFlag', 'chart.js', 'yaru22.angular-timeago', 'maritimeweb.route']);
        keycloakInitialize(module, moduleName, false);
})();

