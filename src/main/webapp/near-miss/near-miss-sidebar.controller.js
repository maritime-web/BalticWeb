(function () {

    angular.module('maritimeweb.near-miss')
        .controller("NearMissSidebarController", NearMissSidebarController);

    NearMissSidebarController.$inject = ['$scope', '$window', 'growl', '$log', 'NotifyService', 'moment', 'NearMissService'];

    function NearMissSidebarController($scope, $window, growl, $log, NotifyService, moment, NearMissService) {
        var vm = this;
        vm.servicesStatus = "true";
        vm.service = undefined;
        vm.mmsi = 219945000;
        vm.showEvents = showEvents;
        vm.validate = validate;
        vm.from = moment.utc().subtract(1, 'hours');
        vm.to = moment.utc();
        vm.searchDisabled = true;
        vm.searchResult = undefined;

        $scope.$watch(vm.mmsi, refreshService, true);

        function refreshService() {
            function emptyResult(status) {
                return status === 204;
            }

            function foundServices(status) {
                return status === 200;
            }

            function updateServiceStatus(newStatus) {
                vm.servicesStatus = newStatus;
                $window.localStorage[NearMissService.serviceID()] = newStatus;
            }

            growl.info("Looking for Near Miss service for MMSI: " + vm.mmsi);
            NearMissService.getNearMisserviceInstance(vm.mmsi)
                .then(function (response) {
                    var services = response.data;
                    var status = response.status;
                    vm.service = services && services.length > 0 ? services[0] : null;

                    if (emptyResult(status)) {
                        updateServiceStatus("false");
                    }

                    if (foundServices(status)) {
                        updateServiceStatus("true");
                        vm.searchDisabled = false;
                    }

                })
                .catch(function (response) {
                    var error = response.data;
                    growl.error("Error finding Near Miss services from Service Register.");
                    updateServiceStatus("false");

                    $log.debug("Error getting Near Miss services. Reason=" + error);
                });
        }

        function validate(dateTime) {
            $log.info(dateTime);
        }

        function showEvents() {
            var interval = {from: vm.from, to: vm.to};
            NearMissService.getNearMissForInterval(vm.service.instanceId, interval)
                .then(function (response) {
                    var vesselStates = response.data.vesselStates;
                    vm.searchResult = new NearMissDataSetModel({mmsi: vm.mmsi, vesselStates: vesselStates});
                    NotifyService.notify('NearMissResult', vm.searchResult);

                })
                .catch(function (response) {
                    growl.error("Error loading Near Miss data.");
                    $log.debug("Error loading Near Miss data. Details=" + response.data);
                });
        }
    }
})();
