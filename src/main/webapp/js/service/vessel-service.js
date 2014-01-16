(function() {
    var module = angular.module('embryo.vessel', []);

    var subscriptions = {};

    module.service('VesselService', function() {
        return {
            list : function(callback) {
                $.ajax({
                    url : embryo.baseUrl + "rest/vessel/list",
                    timeout : embryo.defaultTimeout,
                    success : function(data) {
                        callback(null, data);
                    },
                    error : function(data) {
                        callback(data);
                    }
                });
            },
            details : function(mmsi, callback) {
                $.ajax({
                    url : embryo.baseUrl + "rest/vessel/details",
                    timeout : embryo.defaultTimeout,
                    data : {
                        mmsi : mmsi
                    },
                    success : function(data) {
                        callback(null, data);
                    },
                    error : function(data) {
                        callback(data);
                    }
                });
            },
            saveDetails : function(details, callback) {
                $.ajax({
                    url : embryo.baseUrl + "rest/vessel/save-details",
                    type : "POST",
                    contentType : "application/json",
                    data : JSON.stringify(details),
                    success : function(data) {
                        callback(null, data);
                    },
                    error : function(jqXHR, textStatus, errorThrown) {
                        callback(jqXHR, null);
                    }
                })
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
                    return  ((value.indexOf(searchStr) == 0) || (value.indexOf(" " + searchStr) >= 0));
                }

                $.each(embryo.vessel.allVessels(), function(k, v) {
                    var searchStr = argument.toLowerCase();
                    if(match(v.name, searchStr) || match(v.mmsi, searchStr) || match(v.callSign, searchStr)){
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
                        that.details(vesselOverview.mmsi, function(error, vesselDetails) {
                            if (vesselDetails) {
                                embryo.vesselDetails = vesselDetails;
                                s.vesselOverview = vesselOverview;
                                s.vesselDetails = vesselDetails;
                                for ( var i in s.callbacks)
                                    if (s.callbacks[i])
                                        s.callbacks[i](null, vesselOverview, vesselDetails);
                            } else {
                                for ( var i in s.callbacks)
                                    if (s.callbacks[i])
                                        s.callbacks[i](error);
                            }
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
            historicalTrack : function(vesselId, callback) {
                $.ajax({
                    url : embryo.baseUrl + "rest/vessel/historical-track",
                    timeout : embryo.defaultTimeout,
                    data : {
                        mmsi : vesselId
                    },
                    success : function(data) {
                        callback(null, data);
                    },
                    error : function(data) {
                        callback(data);
                    }
                });
            }
        };
    });

    module.run(function(VesselService) {
        if (!embryo.vessel)
            embryo.vessel = {};
        embryo.vessel.service = VesselService;
    })
})();
