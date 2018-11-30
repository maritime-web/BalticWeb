(function () {
    'use strict';

    angular
        .module('maritimeweb.near-miss')
        .service('NearMissService', NearMissService);

    NearMissService.$inject = ['$http', 'ServiceRegistryService', '$q', '$uibModal', 'NotifyService', 'growl', 'moment'];

    function NearMissService($http, ServiceRegistryService, $q, $uibModal, NotifyService, growl, moment) {
        var that = this;
        that.serviceID = serviceID;
        that.serviceVersion = serviceVersion;
        that.getNearMisserviceInstance = getNearMisserviceInstance;
        that.getNearMissForInterval = getNearMissForInterval;

        function serviceID(){ return 'urn:mrn:mcp:service:design:dma:near-miss'} //TODO 2018-11-06 Design ID unknown so this is best guess
        function serviceVersion(){ return '0.1'} //TODO 2018-11-06 Version unknown so this is best guess

        function getNearMisserviceInstance(mmsi) {
            var dummyServiceInstance = {
                // instanceId: 'urn:mrn:mcl:service:instance:dma:nw-nm-test',
                instanceId: 'urn:mrn:mcl:service:instance:dma:near-miss:219945000',
                name: 'Dummy Near miss instance 219945000'
            };
            var response = {data: [dummyServiceInstance], status: 200};
            return $q.when(response);
            // return ServiceRegistryService.getServiceInstancesForDesign(this.serviceID(), this.serviceVersion(), {mmsi: mmsi})
        }

        function getNearMissForInterval(instanceId, interval) {
            growl.info("Calling Near Miss service with parameters:<br/> From: " + interval.from.toISOString() + " <br/> To: " + interval.to.toISOString());

            var pathParam = encodeURIComponent(instanceId);
            // params += '&instanceId=' + encodeURIComponent('urn:mrn:mcl:service:instance:dma:nw-nm-test');

            var params = '?from=' + encodeURIComponent(interval.from.toISOString());
            params += '&to=' + encodeURIComponent(interval.to.toISOString());

            function timeIntervalFilter(state) {
                var stateTime = moment(state.time);
                return stateTime.isBetween(interval.from, interval.to);
            }

            // TODO Static data for now 2018-11-27
            return $http.get('/near-miss/near-miss-demo-result.json')
                .then(function (response) {
                    response.data.vesselStates = response.data.vesselStates.filter(timeIntervalFilter);
                    return response;
                })

            // return $http.get('/rest/near-miss/' + pathParam +'?' + params);
        }
    }
})();