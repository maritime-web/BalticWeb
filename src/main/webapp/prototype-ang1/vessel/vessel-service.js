angular.module('maritimeweb.vessel.service', []).service('vesselService', [
        '$http',
        function($http, $scope) {
            return {
                    list : function(success, error) {
                        console.log("listing ais data WS-Client");
                            $http.get( maritimeweb.endpoint +"rest/vessel/listarea?area="+ maritimeweb.clientBBOX(), {
                            timeout : 6000
                        }).success(function(success){console.log("returner success...");return success;})
                            .error(function (data, status) {
                            console.log("could not retrieve ais data" + status);
                            $scope.alerts.push({msg: 'could not retrieve ais data',
                                type: 'danger',
                                timeout: 5000
                            });
                            $scope.vesselsStatus = status;

                        });
                    },
                    details : function(mmsi, success, error) {
                        $http.get(maritimeweb.baseUrl + "rest/vessel/details", {
                            timeout : maritimeweb.defaultTimeout,
                            params : {
                                mmsi : mmsi
                            }
                        }).success(success).error(function(data, status, headers, config) {
                            error(maritimeweb.ErrorService.errorStatus(data, status, "loading vessel data"), status);
                        });
                    },
                    saveDetails : function(details, success, error) {
                        $http.post(maritimeweb.baseUrl + "rest/vessel/save-details", details).success(success).error(
                                function(data, status, headers, config) {
                                    error(maritimeweb.ErrorService.extractError(data, status, config), status);
                                });
                    },
                    clientSideSearch : function(argument, callback) {
                        if (argument == null || argument == "")
                            return [];

                        var result = [];

                        function match(propertyValue, searchStr) {
                            if (!propertyValue) {
                                return false;
                            }
                            var value = ("" + propertyValue).toLowerCase();
                            return ((value.indexOf(searchStr) == 0) || (value.indexOf(" " + searchStr) >= 0));
                        }

                      /*  $.each(embryo.vessel.allVessels(), function(k, v) {
                            var searchStr = argument.toLowerCase();
                            if (match(v.name, searchStr) || match(v.mmsi, searchStr) || match(v.callSign, searchStr)) {
                                result.push(v);
                            }
                        });
                        */
                        callback(result);
                    },
                    updateVesselDetailParameter : function(mmsi, name, value) {
                        var s = subscriptions[mmsi];
                        if (s) {
                            eval("s.vesselDetails." + name + "='" + value + "'");
                            this.fireVesselDetailsUpdate(s.vesselDetails);
                        }
                    },
                    fireVesselDetailsUpdate : function(vesselDetails) {
                        var s = subscriptions[vesselDetails.aisVessel.mmsi];
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
                    },
                    historicalTrack : function(vesselId, success, error) {
                        $http.get(maritimeweb.baseUrl + "rest/vessel/historical-track", {
                            timeout : maritimeweb.defaultTimeout,
                            params : {
                                mmsi : vesselId
                            }
                        }).success(success).error(function(data, status, headers, config) {
                            error(maritimeweb.ErrorService.errorStatus(data, status, "loading historical track"), status);
                        });
                    }
                };
        }
]);