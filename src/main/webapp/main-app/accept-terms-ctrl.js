angular.module('maritimeweb.app').controller('AcceptTermsCtrl', ['$scope', '$uibModalInstance', '$window', function ($scope, $uibModalInstance, $window) {
    $scope.accept = function () {
        var ttl = new Date();
        var numberOfDaysToAdd = 14;
        ttl.setDate(ttl.getDate() + numberOfDaysToAdd);
        $window.localStorage.setItem('terms_accepted_ttl', ttl); // storing today date plus 14 days. Don't show the first-run modal for the next 14 days.
        $uibModalInstance.close();
    };
    $scope.refuse = function () {
        $window.location.href = '/partials/refuse.html';
    };
}]);
