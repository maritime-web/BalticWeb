(function() {
    "use strict";

    var module = angular.module('embryo.reportControl', [ 'embryo.UserService', 'embryo.reportComp', 'embryo.greenposService' ]);

    embryo.ReportController = function($scope, GreenposService, UserService) {
        $scope.user = UserService;
        
    };
}());
