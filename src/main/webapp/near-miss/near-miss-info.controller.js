(function () {

    angular.module('maritimeweb.near-miss')
        .controller("NearMissInfoController", NearMissInfoController);

    NearMissInfoController.$inject = ['state'];

    function NearMissInfoController(state) {
        console.log(state);
        var vm = this;
        vm.mmsi = state.mmsi;
        vm.vesselStates = state.vesselStates;
    }
})();
