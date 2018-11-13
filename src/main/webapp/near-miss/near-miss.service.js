(function () {
    'use strict';

    angular
        .module('maritimeweb.near-miss')
        .service('NearMissService', NearMissService);

    NearMissService.$inject = ['$http', 'ServiceRegistryService', '$q', '$uibModal', 'NotifyService', 'growl'];

    function NearMissService($http, ServiceRegistryService, $q, $uibModal, NotifyService, growl) {
        var that = this;
        that.serviceID = serviceID;
        that.serviceVersion = serviceVersion;
        that.getNearMisserviceInstance = getNearMisserviceInstance;
        that.getNearMissForInterval = getNearMissForInterval;
        that.showMessageInfo = showMessageInfo;

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
            var pathParam = encodeURIComponent(instanceId);
            // params += '&instanceId=' + encodeURIComponent('urn:mrn:mcl:service:instance:dma:nw-nm-test');

            var params = '?from=' + encodeURIComponent(interval.from.toISOString());
            params += '&to=' + encodeURIComponent(interval.to.toISOString());

            growl.info("Calling Near Miss service with parameters:<br/> From: " + interval.from.toISOString() + " <br/> To: " + interval.to.toISOString());
            //dummy data for now TODO Change when service is available
            //sensor_type, mmsi,     name,       loa, beam, cog, hdg, sog, is_near_miss, latitude,          longitude,         position_time,      esz_center_latitude, esz_center_longitude, esz_orient_major, esz_a, esz_b FROM VESSEL_STATE;
            // GPS         219945000 OWN SHIP II 165    32    0    3   19   FALSE        56.155277777777776 11.732222222222223 2018-10-08 00:52:20 56.155277777777776   11.732222222222223    0                 241.0  40.0

            var response = {data: {"vessel-states": [
                {lat: 56.155277777777776, lon: 11.732222222222223, time: "2018-10-08 00:52:20", sog: 19, cog: 0, hdg: 0, "safety-zone": {a: 241.0, b: 40.0}, dimensions: {beam: 32, loa: 165}},
                {mmsi: 219945000, lat: 56.157277777777776, lon: 11.734222222222223, time: "2018-10-08 00:52:20", sog: 19, cog: 0, hdg: 0, "near-miss-flag": true, dimensions: {beam: 32, loa: 165}}
                ]}, status: 200};

            // return $q.when(response);
            return $http.get('/near-miss/near-miss-demo-result.json');
            // return $http.get('/rest/near-miss/' + pathParam +'?' + params);
        }

        /** Open the message details dialog **/
        function showMessageInfo(message) {
            return $uibModal.open({
                controller: "S124MessageDetailsDialogController",
                controllerAs: 'vm',
                templateUrl: "s-124/s-124-message-details-dialog.html",
                size: 'lg',
                resolve: {
                    message: function () {
                        return message;
                    }
                }
            });
        }

    }
})();