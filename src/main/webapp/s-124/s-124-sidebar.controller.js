(function () {

    angular.module('maritimeweb.s-124')
        .controller("S124SidebarController", S124SidebarController);

    S124SidebarController.$inject = ['$scope', 'S124Service', '$window', 'growl', '$log'];

    function S124SidebarController($scope, S124Service, $window, growl, $log) {
        var vm = this;
        vm.messages = [];
        vm.servicesStatus = "true";
        vm.services = [];
        vm.refreshServices = refreshServices;
        vm.selected = selected;
        vm.areaHeading = areaHeading;
        vm.showDetails = showDetails;

        var wkt = "POLYGON((-14.475675390625005 40.024168123114805,-14.475675390625005 68.88565248991273,59.92373867187499 68.88565248991273,59.92373867187499 40.024168123114805,-14.475675390625005 40.024168123114805))";
        function refreshServices() {

            function emptyResult(status) {
                return status === 204;
            }

            function foundServices(status) {
                return status === 200;
            }

            function updateServiceStatus(newStatus) {
                vm.servicesStatus = newStatus;
                $window.localStorage[S124Service.serviceID()] = newStatus;
            }

            S124Service.getS124ServiceInstances(wkt)
                .then(function (response) {
                    var services = response.data;
                    var status = response.status;
                    vm.services.length = 0;

                    if (emptyResult(status)) {
                        updateServiceStatus("false");
                    }

                    var chosenServiceInstances = [];
                    if (foundServices(status)) {
                        updateServiceStatus("true");

                        angular.forEach(services, function (service) {
                            vm.services.push(service);
                            service.selected = $window.localStorage[service.instanceId] === 'true';
                            if (service.selected) {
                                chosenServiceInstances.push(service.instanceId);
                            }
                        });

                        loadMessages({instances: chosenServiceInstances, wkt: wkt, append: false});
                    }
                })
                .catch(function (response) {
                    var error = response.data;
                    growl.error("Error finding S-124 services from Service Register.");
                    $window.localStorage[S124Service.serviceID()] = 'false';
                    vm.servicesStatus = 'false';

                    $log.debug("Error getting S-124 services. Reason=" + error);
                });

        }

        function loadMessages(options) {
            var instances = options.instances;
            var wkt = options.wkt;
            var append = options.append || false;
            S124Service.getS124Messages(instances, wkt)
                .then(function (response) {
                    if (append) {
                        vm.messages = vm.messages.concat(response.data);
                    } else {
                        vm.messages = response.data;
                    }
                })
                .catch(function (response) {
                    growl.error("Error loading S-124 messages.");
                    $log.debug("Error loading S-124 messages. Details=" + response.data);
                });

        }

        function getAllSelectedServiceInstances() {
            return vm.services
                .filter(function (service) { return service.selected})
                .map(function(service) {return service.instanceId;});
        }

        function selected(service) {
            $window.localStorage[service.instanceId] = service.selected;
            if (service.selected) {
                loadMessages({instances: [service.instanceId], wkt: wkt, append: true});
            } else {
                loadMessages({instances: getAllSelectedServiceInstances(), wkt: wkt, append: false});
            }
        }
        
        function areaHeading(index) {
            return S124Service.getAreaHeading(vm.messages[index]);
        }

        function showDetails(msg) {

        }
    }
})();
