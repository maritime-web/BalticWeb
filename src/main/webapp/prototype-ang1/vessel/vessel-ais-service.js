angular.module('maritimeweb.vessel.service',[])
    .service('vesselService', function ($window, $q, $http) {
    var promise = null;
    console.log("Vessel service");
    this.getVesselsInArea = function () {
        //if (!promise) { Promise can be cached.
            promise = $q(function (resolve, reject) {
                // Simple GET request example:
                $http({
                    method: 'GET',
                    timeout: maritimeweb.timeout,
                    cache: false,
                    url: maritimeweb.endpoint +"rest/vessel/listarea?area="+ maritimeweb.clientBBOX()
                }).then(function successCallback(response) {
                    // this callback will be called asynchronously
                    // when the response is available
                    console.log('returning ais data.')
                    return resolve(response.data);
                }, function errorCallback(response) {
                    // called asynchronously if an error occurs
                    // or server returns response with an error status.
                    return reject('Could nor retrieve ais data. Code=' +response.status);
                });

            });
      //  }
        return promise;
    };

    this.getVesselsOverviewInArea = function () {
        //if (!promise) { Promise can be cached.
        promise = $q(function (resolve, reject) {
            // Simple GET request example:
            $http({
                method: 'GET',
                timeout: maritimeweb.timeout,
                cache: false,
                url: maritimeweb.endpoint +"rest/vessel/listarea?area="+ maritimeweb.clientBBOX()
            }).then(function successCallback(response) {
                // this callback will be called asynchronously
                // when the response is available
                console.log('returning ais data.')
                return resolve(response.data);
            }, function errorCallback(response) {
                // called asynchronously if an error occurs
                // or server returns response with an error status.
                return reject('Could nor retrieve ais data. Code=' +response.status);
            });

        });
        //  }
        return promise;
    };
});