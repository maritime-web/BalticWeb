(function () {

    angular.module('maritimeweb.s-124')
        .controller("S124MessageDetailsDialogController", S124MessageDetailsDialogController);

    S124MessageDetailsDialogController.$inject = ['message', 'moment'];

    function S124MessageDetailsDialogController(message, moment) {
        var vm = this;
        vm.msg = message;
        vm.hasGeometry = hasGeometry;


        function hasGeometry() {
            return vm.msg
                && vm.msg.navigationalWarningFeaturePart
                && vm.msg.navigationalWarningFeaturePart.length > 0
                && vm.msg.navigationalWarningFeaturePart[0].geometries;
        }

    }
})();
