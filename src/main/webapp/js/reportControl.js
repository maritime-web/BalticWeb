(function() {
    "use strict";

    var module = angular.module('embryo.reportControl', [ 'embryo.reportComp', 'embryo.greenposService' ]);

    embryo.ReportController = function($scope, GreenposService) {
        $scope.authenticated = function() {
            return true;
        };        
    };

    
    
}());
