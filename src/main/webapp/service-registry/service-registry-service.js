angular.module('maritimeweb.serviceregistry')

/** Service for accessing AIS vessel data **/
    .service('ServiceRegistryService', ['$http', 'growl',
        function ($http, growl) {

            this.getServiceInstances = function (wkt) {
                var params = wkt ? '?wkt=' + encodeURIComponent(wkt) : '';
                var request = '/rest/service/lookup/' + params;
                return $http.get(request);
            };

        }]);