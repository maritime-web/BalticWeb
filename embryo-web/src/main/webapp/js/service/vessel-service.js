(function() {
    var module = angular.module('embryo.vessel.service', []);

    var subscriptions = {};
    
    embryo.getMaxSpeed = function(vessel) {

    	if(vessel.awsog) {
    		return vessel.awsog;
    	} else if (vessel.ssog) {
    		return vessel.ssog
    	} else if (vessel.sog) {
    		return vessel.sog;
    	} else {
    		return 0.0;
    	}
    };
    
    module.service('VesselService', [
            '$http',
            function($http) {
                return {
                    list : function(success, error) {
                        var messageId = embryo.messagePanel.show({
                            text: "Loading vessels ..."
                        });

                        $http.get(embryo.baseUrl + "rest/vessel/list", {
                            timeout : embryo.defaultTimeout
                        }).success(function (vessels) {
                            embryo.messagePanel.replace(messageId, {
                                text: vessels.length + " vessels loaded.",
                                type: "success"
                            });
                            success(vessels);
                        }).error(function (data, status) {
                            var errorMsg = embryo.ErrorService.errorStatus(data, status, "loading vessels")
                            embryo.messagePanel.replace(messageId, {
                                text: errorMsg,
                                type: "error"
                            });
                            if (error) {
                                error(errorMsg, status);
                            }
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
                    subscribe : function(mmsi, callback) {
                        if (subscriptions[mmsi] == null)
                            subscriptions[mmsi] = {
                                callbacks : [],
                                vesselDetails : null,
                                interval : null
                            };

                        var s = subscriptions[mmsi];
                        var id = s.callbacks.push(callback) - 1;
                        var that = this;

                        function lookup() {
                            that.details(mmsi, function (vesselDetails) {
                                embryo.vesselDetails = vesselDetails;
                                s.vesselDetails = vesselDetails;
                                for ( var i in s.callbacks)
                                    if (s.callbacks[i])
                                        s.callbacks[i](null, vesselDetails);
                            }, function (error) {
                                for (var i in s.callbacks)
                                    if (s.callbacks[i])
                                        s.callbacks[i](error);
                            })
                        }

                        if (s.interval == null) {
                            s.interval = setInterval(lookup, embryo.loadFrequence);
                            lookup(mmsi, callback);
                        }

                        if (s.vesselDetails) {
                            callback(null, s.vesselDetails)
                        }
                        return {
                            id : id,
                            mmsi : mmsi
                        }
                    },
                    unsubscribe: function (unsubscription) {
                        var s = subscriptions[unsubscription.mmsi];
                        s.callbacks.splice(unsubscription.id, 1);
                        var allDead = s.callbacks.length == 0;
                        if (allDead) {
                            clearInterval(s.interval);
                            subscriptions[unsubscription.mmsi] = null
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

    embryo.eventbus.VesselInformationAddedEvent = function() {
        var event = jQuery.Event("VesselInformationAddedEvent");
        return event;
    };

    embryo.eventbus.registerShorthand(embryo.eventbus.VesselInformationAddedEvent, "vesselInformationAddedEvent");

    module.service('VesselInformation', function() {
        var vesselInformations = [];
        var that = this;

        this.addInformationProvider = function(info) {
            for ( var index in vesselInformations) {
                var vi = vesselInformations[index];
                if (vi.title === info.title && (vi.type && info.type ? vi.type === info.type : true)) {
                    // already added. Replace
                    vesselInformations[index] = info;
                    return;
                }
            }
            vesselInformations.push(info);
            embryo.eventbus.fireEvent(embryo.eventbus.VesselInformationAddedEvent());
        }

        this.getVesselInformation = function() {
            return vesselInformations;
        }

        this.show = function(provider, vesselOverview, vesselDetails) {
            for ( var index in vesselInformations) {
                var vi = vesselInformations[index];
                if (vi.title !== provider.title || (vi.type && provider.type ? vi.type !== provider.type : true)) {
                    that.hide(vi);
                }
            }
            provider.show(vesselOverview, vesselDetails);
        }

        this.hide = function(provider) {
            if (provider.hide)
                provider.hide();
            if (provider.close)
                provider.close();
        }

        this.hideAll = function() {
            for ( var index in vesselInformations) {
                that.hide(vesselInformations[index]);
            }
        }

    });

    module.run(function(VesselService, VesselInformation) {
        if (!embryo.vessel)
            embryo.vessel = {};
        embryo.vessel.service = VesselService;
        embryo.vessel.information = VesselInformation;
    })
})();
