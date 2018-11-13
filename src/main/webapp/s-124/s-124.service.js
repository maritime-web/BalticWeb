(function () {
    'use strict';

    angular
        .module('maritimeweb.s-124')
        .service('S124Service', S124Service);

    S124Service.$inject = ['$http', 'ServiceRegistryService', '$q', '$uibModal', 'NotifyService'];

    function S124Service($http, ServiceRegistryService, $q, $uibModal, NotifyService) {
        var that = this;
        that.serviceID = serviceID;
        that.serviceVersion = serviceVersion;
        that.getS124ServiceInstances = getS124ServiceInstances;
        that.getS124Messages = getS124Messages;
        that.getAreaHeading = getAreaHeading;
        that.showMessageInfo = showMessageInfo;

        function serviceID(){ return 'urn:mrn:mcp:service:design:dma:s-124'} //TODO 2018-10-25 Design ID unknown so this is best guess
        function serviceVersion(){ return '0.1'} //TODO 2018-10-25 Version unknown so this is best guess

        function getS124ServiceInstances(wkt) {
/*
            var dummyServiceInstance = {
                // instanceId: 'urn:mrn:mcl:service:instance:dma:nw-nm-test',
                instanceId: 'urn:mrn:mcl:service:instance:dma:tiles-service:terra:baltic',
                name: 'Dummy S-124 instance'
            };
            var response = {data: [dummyServiceInstance], status: 200};
            return $q.when(response);
*/
            return ServiceRegistryService.getServiceInstancesForDesign(this.serviceID(), this.serviceVersion(), {wkt: wkt})
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