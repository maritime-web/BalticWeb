maritimeweb = {};
maritimeweb.endpoint = "/";
maritimeweb.baseUrl = "/";
maritimeweb.defaultTimeout = 6000;

angular.module('maritimeweb.vessel.service',[])
    .service('vesselService', function ($window, $q, $http) {
    var promise = null;
    console.log("Vessel service");
    this.getVesselsInArea = function (zoomLvl,bbox) {
        //if (!promise) { Promise can be cached.
            promise = $q(function (resolve, reject) {
                var url = maritimeweb.endpoint;
                if(zoomLvl > 8){ // below  zoom level 8 a more detailed and data rich overview is created.
                    url += "rest/vessel/listarea?area="+ bbox;
                }else{
                    url += "rest/vessel/overview?area="+ bbox;
                }
                $http({
                    method: 'GET',
                    timeout: maritimeweb.timeout,
                    cache: false,
                    url: url
                }).then(function successCallback(response) {
                    // this callback will be called asynchronously
                    // when the response is available
                    console.log('returning ais data.')
                    return resolve(response.data);
                }, function errorCallback(response) {
                    // called asynchronously if an error occurs
                    // or server returns response with an error status.
                    // if no connection use some sort of caching strategy.
                    return reject('Could not retrieve ais data in Area. Code=' +response.status);
                });

            });
      //  }
        return promise;
    };
    this.details = function(mmsi){
        var promiseResponse = $q(function (resolve, reject) {
            $http({
                method: 'GET',
                timeout: maritimeweb.timeout,
                cache: false,
                params : {
                    mmsi : mmsi
                },
                url: maritimeweb.baseUrl + "rest/vessel/details"
            }).then(function successCallback(response) {
                // this callback will be called asynchronously
                // when the response is available
                console.log('returning ais data.')
                return resolve(response.data);
            }, function errorCallback(response) {
                // called asynchronously if an error occurs
                // or server returns response with an error status.
                // if no connection use some sort of caching strategy.
                return reject('Could not retrieve vessels details data. Code=' +response.status);
            });

        });
        return promiseResponse;

    };

        this.historicalTrack = function(vesselId) {
            var promiseResponse = $q(function (resolve, reject) {
                $http({
                    method: 'GET',
                    timeout: maritimeweb.timeout,
                    cache: false,
                    params : {
                        mmsi : vesselId
                    },
                    url: maritimeweb.baseUrl + "rest/vessel/historical-track"
                }).then(function successCallback(response) {
                    // this callback will be called asynchronously
                    // when the response is available
                    console.log('returning ais data.')
                    return resolve(response.data);
                }, function errorCallback(response) {
                    // called asynchronously if an error occurs
                    // or server returns response with an error status.
                    // if no connection use some sort of caching strategy.

                    return reject('Could not retrieve historicalTrack. Code=' +response.status);
                });
            });
            return promiseResponse;
        };

        this.saveDetails = function(details) {
            $http.post(maritimeweb.baseUrl + "rest/vessel/save-details", details).success(success).error(
                function(data, status, headers, config) {
                    error(maritimeweb.ErrorService.extractError(data, status, config), status);
                });
        };
        /*
        this.updateVesselDetailParameter = function(mmsi, name, value) {
            //var s = subscriptions[mmsi];
            if (s) {
                eval("s.vesselDetails." + name + "='" + value + "'");
                this.fireVesselDetailsUpdate(s.vesselDetails);
            }
        };
        this.fireVesselDetailsUpdate = function(vesselDetails) {
            //var s = subscriptions[vesselDetails.aisVessel.mmsi];
            if (s) {
                s.vesselDetails = vesselDetails;
                for ( var i in s.callbacks) {
                    // function x(){
                    // var count = i;
                    // s.callbacks[count]();
                    // }
                    // setTimeout(x, 10);

                    (function(callback) {
                        setTimeout(function() {
                            callback(null, s.vesselDetails)
                        }, 10);
                    })(s.callbacks[i]);
                }
            }
        };

        */

});