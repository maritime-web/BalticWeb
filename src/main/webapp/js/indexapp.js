(function() {
    "use strict";

    var moduleName = 'embryo.front';
    var indexApp = angular.module('embryo.front', [ 'embryo.menu', 'ui.bootstrap.carousel' ]);

    keycloakInitialize(indexApp, moduleName);

    $(function() {
        embryo.authentication.currentPageRequiresAuthentication = false;
    });
})();

