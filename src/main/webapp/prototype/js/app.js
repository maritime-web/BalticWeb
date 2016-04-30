
// Define all modules here
angular.module('maritimeweb.map', []);
angular.module('maritimeweb.app', []);
angular.module('maritimeweb.vessel', []);
angular.module('maritimeweb.nw-nm', []);

(function () {
    "use strict";

    var moduleName = 'maritimeweb';
    var module = angular.module(moduleName, ['ngAnimate', 'ngSanitize', 'ui.bootstrap',
        'maritimeweb.map', 'maritimeweb.app', 'maritimeweb.vessel', 'maritimeweb.nw-nm']);
    

        keycloakInitialize(module, moduleName, false);
})();

