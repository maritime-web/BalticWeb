(function () {

    angular.module('maritimeweb.near-miss')
        .controller("NearMissMapController", NearMissMapController);

    NearMissMapController.$inject = ['$scope', 'NotifyService'];

    function NearMissMapController($scope, NotifyService) {
        var vm = this;
        vm.searchResult = undefined;

        NotifyService.subscribe($scope, 'NearMissResult', function (e, result) {
            vm.searchResult = result;
        });
    }
})();
