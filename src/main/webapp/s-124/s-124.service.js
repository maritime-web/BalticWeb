(function () {
    'use strict';

    angular
        .module('maritimeweb.s-124')
        .service('S124Service', S124Service);

    S124Service.$inject = ['$http', 'ServiceRegistryService', '$q'];

    function S124Service($http, ServiceRegistryService, $q) {
        var that = this;
        that.serviceID = serviceID;
        that.serviceVersion = serviceVersion;
        that.getS124ServiceInstances = getS124ServiceInstances;
        that.getS124Messages = getS124Messages;
        that.getAreaHeading = getAreaHeading;

        function serviceID(){ return 'urn:mrn:mcl:service:design:sma:s-124'} //TODO 2018-10-03 Design ID unknown so this is best guess
        function serviceVersion(){ return 'v1'} //TODO 2018-10-03 Version unknown so this is best guess

        function getS124ServiceInstances(wkt) {
            var dummyServiceInstance = {
                instanceId: 'urn:mrn:mcl:service:instance:dma:nw-nm-test',
                name: 'Dummy S-124 instance'
            };
            var response = {data: [dummyServiceInstance], status: 200};
            return $q.when(response);
            // return ServiceRegistryService.getServiceInstancesForDesign(this.serviceID(), this.serviceVersion(), wkt)
        }

        function getS124Messages(instanceIds, wkt, status, messageId) {
            var params = messageId ? 'id=' + messageId : '';
            angular.forEach(instanceIds, function (instanceId) {
                params += '&instanceId=' + encodeURIComponent(instanceId);
            });
            // params += '&instanceId=' + encodeURIComponent('urn:mrn:mcl:service:instance:dma:nw-nm-test');

            if (status) {
                params += '&status=' + status;
            }
            if (wkt) {
                params += '&wkt=' + encodeURIComponent(wkt);
            }

            return $http.get('/rest/s-124/messages?' + params);
        }

        function getAreaHeading(message) {
            return message.areaHeading;
        }
    }
})();