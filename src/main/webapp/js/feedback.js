var module = angular.module('embryo.feedback', ['embryo.base']);

module.controller('embryo.FeedbackCtrl', ['$scope', '$http', function($scope, $http) {
    $scope.sendFeedback = function($event) {
        $event.preventDefault();
        $http.post(embryo.baseUrl + 'rest/feedback', $scope.feedback).success(function() {
            $scope.message = "Thank you for your feedback! You will receive an answer as soon as possible.";
        }).error(function(error) {
            $scope.alertMessages = ['Something went wrong when sending feedback. The error was: ' + error];
        });
    };
    
    $scope.userTypes = ['Ship', 'Shore', 'Maritime Pilot', 'Coastal Control', 'Authority', 'Other'];
    $scope.feedback = {
    		userType: 'Ship'
    };
}]);