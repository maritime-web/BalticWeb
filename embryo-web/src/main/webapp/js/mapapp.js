(function() {
    "use strict";
    angular.module('embryo.map', [ 'ui.bootstrap', 'embryo.menu', 'embryo.routeService', 'embryo.greenpos',
            'embryo.vessel.controller', 'embryo.ice', 'embryo.msi', 'embryo.routeUpload', 'embryo.schedule',
            'embryo.routeEdit', 'embryo.decimal', 'embryo.authentication', 'embryo.shape', 'embryo.zoom' ]);
    
    angular.element(document).ready(function () {
        angular.bootstrap(document, ['embryo.map']);
    });
})();

