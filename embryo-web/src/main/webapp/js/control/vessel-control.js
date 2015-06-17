embryo.vessel = {};

embryo.eventbus.VesselSelectedEvent = function(id) {
    var event = jQuery.Event("VesselSelectedEvent");
    event.vesselId = id;
    return event;
};

embryo.eventbus.VesselUnselectedEvent = function() {
    var event = jQuery.Event("VesselUnselectedEvent");
    return event;
};

embryo.eventbus.registerShorthand(embryo.eventbus.VesselSelectedEvent, "vesselSelected");
embryo.eventbus.registerShorthand(embryo.eventbus.VesselUnselectedEvent, "vesselUnselected");

$(function() {

    var vessels = null;
    var vesselLayer;
    var selectedId = null;

    embryo.postLayerInitialization(function(){
        vesselLayer = new VesselLayer({clusteringEnabled: true});
        addLayerToMap("vessel", vesselLayer, embryo.map);


        vesselLayer.select(function(id) {
            if (selectedId != id && selectedId != null)
                embryo.eventbus.fireEvent(embryo.eventbus.VesselUnselectedEvent());
            if (id)
                embryo.eventbus.fireEvent(embryo.eventbus.VesselSelectedEvent(id));
            else
                embryo.eventbus.fireEvent(embryo.eventbus.VesselUnselectedEvent());
            selectedId = id;
        });
    })

    embryo.vessel.lookupVessel = function(id) {
        for ( var i in vessels) {
            if (vessels[i].mmsi == id)
                return vessels[i];
        }
        return null;
    };

    embryo.vessel.allVessels = function() {
        return vessels;
    };

    embryo.vessel.goToVesselLocation = function(vessel) {
        if (vessel.type != null)
            embryo.map.setCenter(vessel.x, vessel.y, 8);
    };

    embryo.vessel.selectVessel = function(vessel) {
        if (vessel.type) {
            vesselLayer.select(vessel.mmsi);
        } else {
            vesselLayer.select(null);
            embryo.eventbus.fireEvent(embryo.eventbus.VesselSelectedEvent(vessel.mmsi));
        }
    };

    embryo.vessel.setMarkedVessel = function(markedVesselId) {
        vesselLayer.markedVesselId = markedVesselId;
        vesselLayer.draw(vessels);
    };


    embryo.mapInitialized(function() {
        embryo.subscription.service.subscribe({
            subscriber: "vesselLayerController",
            name: "VesselService.list",
            fn: embryo.vessel.service.list,
            interval: embryo.loadFrequence,
            success: function (data) {
                vessels = data;
                vesselLayer.draw(vessels);
            }
        });
    });
});

$(function() {

    var module = angular.module('embryo.vessel.control', [ 'embryo.vessel.service' ]);

    embryo.vessel.aisToArray = function(aisObject) {
        var result = [];
        $.each(aisObject, function(k, v) {
            if (v != null && v != "")
                result.push({
                    text : k,
                    value : v
                });
            ;
        });
        return result;
    };

    var navStatusTexts = {
        0: "Under way using engine",
        1: "At anchor",
        2: "Not under command",
        3: "Restricted manoeuvrability",
        4: "Constrained by her draught",
        5: "Moored",
        6: "Aground",
        7: "Engaged in fishing",
        8: "Under way",
        12: "Power-driven vessel pushing ahead or towing alongside",
        14: "Ais SART",
        15: "Undefined"
    }

    embryo.vessel.navStatusText = function (navStatus) {
        if (navStatus && navStatusTexts.hasOwnProperty(navStatus)) {
            return navStatusTexts[navStatus]
        }
        return null;
    };


    embryo.vessel.createSorter = function(nameSequence) {
        var sorter = function(service1, service2) {
            var i1 = "" + nameSequence.indexOf(service1.name);
            var i2 = "" + nameSequence.indexOf(service2.name);
            return i1 - i2;
        };
        return sorter;
    };

    embryo.vessel.createFilter = function(nameSequence, subFilter) {
        var filter = function(provider, index, array) {
            return nameSequence.indexOf(provider.title) >= 0 && subFilter(provider, index, array);
        };
        return filter;
    };

    embryo.vessel.controllers = {};
    embryo.vessel.controllers.service = function(serv, scope) {
        var available = serv.available(scope.vesselOverview, scope.vesselDetails);

        return {
            service : serv,
            scope : scope,
            name : serv.title,
            type : (available.action ? 'edit' : 'view'),
            statusText : function() {
                switch (available) {
                case false:
                    return 'NOT AVAILABLE';
                case true:
                    return 'AVAILABLE';
                default:
                    return (available.text);
                }
            },
            statusClass : function() {
                switch (available) {
                case false:
                    return 'label-default';
                case true:
                    return 'label-success';
                default:
                    if (available.klass) {
                        return "label-" + available.klass;
                    }
                    return 'label-default';
                }
            },
            alert : function() {
                if (this.service.close && this.service.shown
                        && this.service.shown(this.scope.vesselOverview, this.scope.vesselDetails)) {
                    return 'alert-success';
                } else if (this.service.hide && this.service.shown
                        && this.service.shown(this.scope.vesselOverview, this.scope.vesselDetails)) {
                    return 'alert-warning';
                }
                return '';
            },
            text : function() {
                if (this.type === 'edit') {
                    return 'edit';
                }
                if (this.service.hide && this.service.shown
                        && this.service.shown(this.scope.vesselOverview, this.scope.vesselDetails)) {
                    return "hide";
                }

                return "view";
            },
            toggle : function($event) {
                $event.preventDefault();
                if (this.service.close) {
                    if (this.service.shown(this.scope.vesselOverview, this.scope.vesselDetails)) {
                        this.scope.vesselInformation.hide(this.service);
                    } else {
                        this.scope.vesselInformation.show(this.service, this.scope.vesselOverview,
                                this.scope.vesselDetails);
                    }
                } else if (this.service.hide) {
                    if (this.service.shown(this.scope.vesselOverview, this.scope.vesselDetails)) {
                        this.service.hide(this.scope.vesselOverview, this.scope.vesselDetails);
                    } else {
                        this.scope.vesselInformation.hideAll();
                        this.service.show(this.scope.vesselOverview, this.scope.vesselDetails);
                    }
                }

            }

        };
    };

    function vesselAis(data) {
        if (!data.aisVessel) {
            return null;
        }

        return embryo.vessel.aisToArray({
            "MMSI" : data.aisVessel.mmsi,
            "Class" : data.aisVessel["class"],
            "Call Sign" : data.aisVessel.callsign,
            "Vessel Type" : data.aisVessel.vesselType,
            "Cargo" : data.aisVessel.cargo != "N/A" && data.aisVessel.cargo != "Undefined" ? data.aisVessel.cargo : null,
            "Country" : data.aisVessel.country,
            "SOG" : data.aisVessel.sog,
            "COG" : data.aisVessel.cog,
            "Destination" : data.aisVessel.destination,
            "Nav Status": embryo.vessel.navStatusText(data.aisVessel.navStatus),
            "ETA": data.aisVessel.eta ? formatTime(data.aisVessel.eta) + " UTC" : ""
        });
    }

    var awNameSequence = [ "Vessel Information", "Schedule", "Route", "Reports" ];
    var awSorter = embryo.vessel.createSorter(awNameSequence);
    var awFilter = embryo.vessel.createFilter(awNameSequence, function(provider, index, array) {
        return provider.type === 'view';
    });

    function initSelectedSections(scope) {
        if (!scope.vesselOverview || !scope.vesselDetails) {
            return [];
        }

        var sections = [ {
            name : "ArcticWeb Reporting",
            services : [],
            type : "view"
        }, {
            name : "Additional Information",
            services : [],
            type : "view"
        } ];

        var vesselInformation = scope.vesselInformation.getVesselInformation().filter(awFilter);
        for ( var i in vesselInformation) {
            sections[0].services.push(embryo.vessel.controllers.service(vesselInformation[i], scope));
        }
        var selectedActions = embryo.vessel.actions.selectedVessel();
        sections[0].services.push(embryo.vessel.controllers.service(selectedActions[1], scope));
        sections[0].services.sort(awSorter);

        for ( var index = 3; index < selectedActions.length; index++) {
            sections[1].services.push(embryo.vessel.controllers.service(selectedActions[index], scope));
        }

        return sections;
    }

    module.controller("VesselController", [
            '$scope',
            'VesselService',
            'VesselInformation',
            function($scope, VesselService, VesselInformation) {
                this.scope = $scope;

                $scope.selected = {};
                $scope.vesselScope = {};
                $scope.vesselInformation = VesselInformation;

                embryo.vesselSelected(function(e) {
                    VesselInformation.hideAll();
                    $scope.selected.vesselName = "loading data";
                    $scope.selected.open = true;
                    $scope.selected.vesselInformation = VesselInformation;

                    VesselService.details(e.vesselId, function(vesselDetails) {
                        $scope.selected.vesselAis = vesselAis(vesselDetails);
                        $scope.selected.vesselOverview = embryo.vessel.lookupVessel(e.vesselId);
                        $scope.selected.vesselName = $scope.selected.vesselOverview.name;
                        $scope.selected.vesselDetails = vesselDetails;
                    }, function(errorMsg, status) {
                        embryo.messagePanel.show({
                            text : errorMsg,
                            type : "error"
                        });
                    });
                    if (!$scope.$$phase) {
                        $scope.$apply(function() {
                        });
                    }
                });

                embryo.vesselUnselected(function(e) {
                    $scope.selected = {
                        open : false
                    };
                    if (!$scope.$$phase) {
                        $scope.$apply(function() {
                        });
                    }

                });

                $scope.viewAis = function($event) {
                    $event.preventDefault();
                    VesselInformation.hideAll();
                    embryo.controllers.ais.show($scope.selected.vesselDetails.aisVessel);
                };

                $scope.$watch('selected.vesselOverview', function(newValue, oldValue) {
                    $scope.selected.sections = initSelectedSections($scope.selected);
                }, true);

                $scope.$watch('selected.vesselDetails', function(newValue, oldValue) {
                    $scope.selected.sections = initSelectedSections($scope.selected);
                }, true);

                $scope.selectedDrawnOnMap = function() {
                    var selectedActions = embryo.vessel.actions.selectedVessel();

                    for ( var index in selectedActions) {
                        if (typeof selectedActions[index] == 'object') {
                            if (selectedActions[index] && selectedActions[index].shown
                                    && selectedActions[index].shown()) {
                                return true;
                            }
                        }
                    }
                    return false;
                };
                $scope.clearSelectedOnMap = function($event) {
                    $event.preventDefault();

                    var selectedActions = embryo.vessel.actions.selectedVessel();
                    for ( var index in selectedActions) {
                        if (typeof selectedActions[index] == 'object') {
                            if (selectedActions[index] && selectedActions[index].hideAll) {
                                selectedActions[index].hideAll();
                            }
                        }
                    }
                };
                $scope.$on("$destroy", function() {
                    VesselInformation.hideAll();
                });
            } ]);

    module.controller("SearchVesselController", [ '$scope', 'VesselService', function($scope, VesselService) {
        $scope.searchResults = [];
        $scope.searchResultsLimit = 10;

        $scope.$watch('searchField', function(newValue, oldValue) {
            if (newValue && newValue !== oldValue) {
                VesselService.clientSideSearch($scope.searchField, function(searchResults) {
                    $scope.searchResults = searchResults;
                });
            }
        });
        $scope.select = function($event, vessel) {
            $event.preventDefault();
            embryo.vessel.goToVesselLocation(vessel);
            embryo.vessel.selectVessel(vessel);
        };
    } ]);

    module.controller("MapInformationController", [ '$scope', function($scope) {
        $scope.selectedDrawnOnMap = function() {
            var selectedActions = embryo.vessel.actions.selectedVessel();

            for ( var index in selectedActions) {
                if (typeof selectedActions[index] == 'object') {
                    if (selectedActions[index] && selectedActions[index].shown && selectedActions[index].shown()) {
                        return true;
                    }
                }
            }
            return false;
        };
        $scope.clearSelectedOnMap = function($event) {
            $event.preventDefault();

            var selectedActions = embryo.vessel.actions.selectedVessel();
            for ( var index in selectedActions) {
                if (typeof selectedActions[index] == 'object') {
                    if (selectedActions[index] && selectedActions[index].hideAll) {
                        selectedActions[index].hideAll();
                    }
                }
            }
        };
    } ]);

    embryo.ready(function() {
        $('#vcpSearch').on('hidden', function() {
            $("#searchField").blur();
        });

        $('#vcpSearch').on('shown', function() {
            $("#searchField").focus();
        });
    });

    embryo.authenticated(function() {
        if (!embryo.authentication.shipMmsi) {
            embryo.groupChanged(function(e) {
                if (e.groupId == "vessel")
                    $("#searchField").focus();
            });
        }
    });
});
