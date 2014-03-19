(function() {
    var module = angular.module('embryo.vessel', []);

    var subscriptions = {};

    module.service('VesselService', [
            '$http',
            function($http) {
                return {
                    list : function(success, error) {
                        $http.get(embryo.baseUrl + "rest/vessel/list", {
                            timeout : embryo.defaultTimeout
                        }).success(success).error(function(data, status, headers, config) {
                            error(embryo.ErrorService.errorStatus(data, status, "loading vessels"), status);
                        });
                    },
                    details : function(mmsi, success, error) {
                        $http.get(embryo.baseUrl + "rest/vessel/details", {
                            timeout : embryo.defaultTimeout,
                            params : {
                                mmsi : mmsi
                            }
                        }).success(success).error(function(data, status, headers, config) {
                            error(embryo.ErrorService.errorStatus(data, status, "loading vessel data"), status);
                        });
                    },
                    saveDetails : function(details, success, error) {
                        $http.post(embryo.baseUrl + "rest/vessel/save-details", details).success(success).error(
                                function(data, status, headers, config) {
                                    error(embryo.ErrorService.extractError(data, status, config), status);
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

                        $.each(embryo.vessel.allVessels(), function(k, v) {
                            var searchStr = argument.toLowerCase();
                            if (match(v.name, searchStr) || match(v.mmsi, searchStr) || match(v.callSign, searchStr)) {
                                result.push(v);
                            }
                        });

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
                        var s = subscriptions[vesselDetails.ais.mmsi];
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
                                        callback(null, s.vesselOverview, s.vesselDetails)
                                    }, 10);
                                })(s.callbacks[i]);
                            }
                        }
                    },
                    subscribe : function(mmsi, callback) {
                        if (subscriptions[mmsi] == null)
                            subscriptions[mmsi] = {
                                callbacks : [],
                                vesselOverview : null,
                                vesselDetails : null,
                                interval : null
                            };

                        var s = subscriptions[mmsi];

                        var id = s.callbacks.push(callback);

                        var that = this;

                        function lookupStepTwo(vesselOverview) {
                            if (vesselOverview) {
                                that.details(vesselOverview.mmsi, function(vesselDetails) {
                                    embryo.vesselDetails = vesselDetails;
                                    s.vesselOverview = vesselOverview;
                                    s.vesselDetails = vesselDetails;
                                    for ( var i in s.callbacks)
                                        if (s.callbacks[i])
                                            s.callbacks[i](null, vesselOverview, vesselDetails);
                                }, function(error, status) {
                                    for ( var i in s.callbacks)
                                        if (s.callbacks[i])
                                            s.callbacks[i](error);
                                })
                            } else {
                                for ( var i in s.callbacks)
                                    if (s.callbacks[i])
                                        s.callbacks[i]("unable to find " + mmsi);
                            }
                        }

                        function lookup() {
                            that.clientSideMmsiSearch(mmsi, lookupStepTwo);
                        }

                        if (s.interval == null) {
                            s.interval = setInterval(lookup, embryo.loadFrequence);
                            lookup(mmsi, callback);
                        }

                        if (s.vesselDetails) {
                            callback(null, s.vesselOverview, s.vesselDetails)
                        }
                        return {
                            id : id,
                            mmsi : mmsi
                        }
                    },
                    unsubscribe : function(id) {
                        var s = subscriptions[id.mmsi];
                        s.callbacks[id.id] = null;
                        var allDead = true;
                        for ( var i in s.callbacks)
                            allDead &= s.callbacks[i] == null;
                        if (allDead) {
                            clearInterval(s.interval);
                            subscriptions[id.mmsi] = null
                        }
                    },
                    clientSideMmsiSearch : function(mmsi, callback) {
                        var that = this;
                        var result = [];

                        if (embryo.vessel.allVessels()) {
                            $.each(embryo.vessel.allVessels(), function(k, v) {
                                if (mmsi == v.mmsi) {
                                    result.push(v);
                                }
                            })
                            callback(result[0]);
                        } else {
                            setTimeout(function() {
                                that.clientSideMmsiSearch(mmsi, callback);
                            }, 100);
                        }
                    },
                    historicalTrack : function(vesselId, success, error) {
                        $http.get(embryo.baseUrl + "rest/vessel/historical-track", {
                            timeout : embryo.defaultTimeout,
                            params : {
                                mmsi : vesselId
                            }
                        }).success(success).error(function(data, status, headers, config) {
                            error(embryo.ErrorService.errorStatus(data, status, "loading historical track"), status);
                        });
                    }
                };
            } ]);

    module.run(function(VesselService) {
        if (!embryo.vessel)
            embryo.vessel = {};
        embryo.vessel.service = VesselService;
    })
})();
