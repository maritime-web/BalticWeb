(function() {
    "use strict";

    var module = angular.module('embryo.reportComp', [ 'embryo.shipService', 'embryo.voyageService',
            'embryo.routeService', 'embryo.greenposService' ]);

    module.controller('ReportCompCtrl', function($scope, ShipService, VoyageService) {
        ShipService.getYourShip(function(ship) {
            $scope.ship = ship;
        });
        VoyageService.getYourActive(function(voyage) {
            $scope.voyage = voyage;
        });
    });

}());
