(function () {

    angular.module('maritimeweb.s-124')
        .controller("S124SidebarController", S124SidebarController);

    S124SidebarController.$inject = ['$scope', 'S124Service', '$window', 'growl', '$log', 'NotifyService'];

    function S124SidebarController($scope, S124Service, $window, growl, $log, NotifyService) {
        var vm = this;
        vm.messages = [];
        vm.servicesStatus = "true";
        vm.services = [];
        vm.wkt = {};
        vm.reloadClass = "";
        vm.refreshServices = refreshServices;
        vm.selected = selected;
        vm.areaHeading = areaHeading;
        vm.showDetails = showDetails;

        var getMapState = function () {
            return JSON.parse($window.localStorage.getItem('mapState-storage'));
        };
        $scope.$watch(getMapState, refreshServices, true);

        // var wkt = "POLYGON((-14.475675390625005 40.024168123114805,-14.475675390625005 68.88565248991273,59.92373867187499 68.88565248991273,59.92373867187499 40.024168123114805,-14.475675390625005 40.024168123114805))";
        function refreshServices(mapState) {
            if (mapState) {
                vm.wkt = mapState['wktextent'];
            }

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

            growl.info("Looking for S-124 services in the area");
            S124Service.getS124ServiceInstances(vm.wkt)
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
                                growl.info("Loading S-124 messages from the " + service.name + " service");
                            }
                        });

                        NotifyService.notify("S-124-Services-Loaded", vm.services);

                        loadMessages({instances: chosenServiceInstances, wkt: vm.wkt, append: false});
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
            if (instances.length === 0) {
                return;
            }

            var wkt = options.wkt;
            var append = options.append || false;
            S124Service.getS124Messages(instances, wkt)
                .then(function (response) {
                    if (append) {
                        vm.messages = vm.messages.concat(response.data);
                    } else {
                        vm.messages = response.data;
                    }

                    NotifyService.notify("S-124-Messages-Loaded", vm.messages);

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

        function clearMessagesFor(service) {
            vm.messages = vm.messages.filter(function (m) { return m.serviceInstanceId !== service.instanceId });
            NotifyService.notify("S-124-Messages-Loaded", vm.messages);
        }

        function selected(service) {
            $window.localStorage[service.instanceId] = service.selected;
            if (service.selected) {
                loadMessages({instances: [service.instanceId], wkt: vm.wkt, append: true});
            } else {
                clearMessagesFor(service);
                // loadMessages({instances: getAllSelectedServiceInstances(), wkt: vm.wkt, append: false});
            }
        }
        
        function areaHeading(index) {
            return S124Service.getAreaHeading(vm.messages[index]);
        }

        function showDetails(msg) {
            S124Service.showMessageInfo(msg);
        }
    }
})();
