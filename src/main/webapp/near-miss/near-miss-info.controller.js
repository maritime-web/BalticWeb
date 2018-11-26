(function () {

    angular.module('maritimeweb.near-miss')
        .controller("NearMissInfoController", NearMissInfoController);

    NearMissInfoController.$inject = ['state'];

    function NearMissInfoController(state) {
        var vm = this;
        vm.mmsi = state.mmsi;
        vm.vesselStates = state.vesselStates;
    }
})();
