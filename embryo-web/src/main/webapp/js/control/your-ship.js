$(function() {

    //var yourShipRouteLayer = RouteLayerSingleton.getInstance();
    var yourShipRouteLayer = new RouteLayer();
    addLayerToMap("vessel", yourShipRouteLayer, embryo.map);

    var module = angular.module('embryo.yourvessel.control', [ 'embryo.scheduleService', 'embryo.vessel.service' ]);

    function yourAis(data) {
        if (!data.ais) {
            return null;
        }
        return embryo.vessel.aisToArray({
            "MMSI" : data.ais.mmsi,
            "Call Sign" : data.ais.callsign,
            "Country" : data.ais.country,
            "Destination" : data.ais.destination,
            "Nav Status" : data.ais.navStatus,
            "ETA" : data.ais.eta
        });
    }

    var awNameSequence = [ "Vessel Information", "Schedule", "Reporting" ];
    var awSorter = embryo.vessel.createSorter(awNameSequence);
    var awFilter = embryo.vessel.createFilter(awNameSequence, function(provider, index, array) {
        return provider.type === 'edit';
    });

    function initSections(scope) {
        if (!scope.vesselOverview || !scope.vesselDetails) {
            return [];
        }

        var sections = [ {
            name : "ArcticWeb Reporting",
            services : [],
            type : "edit"
        }, {
            name : "Additional Information",
            services : [],
            type : "view"
        } ];

        function editProvidersFilter(provider, index, array) {
            return provider.type === 'edit'
        }
        var vesselInformation = embryo.vessel.information.getVesselInformation().filter(awFilter);
        for ( var i in vesselInformation) {
            sections[0].services.push(embryo.vessel.controllers.service(vesselInformation[i], scope));
        }
        sections[0].services.sort(awSorter);

        sections[1].services.push(embryo.vessel.controllers
                .service(embryo.additionalInformation.historicalTrack, scope));
        sections[1].services.push(embryo.vessel.controllers.service(embryo.additionalInformation.nearestShips, scope));
        sections[1].services.push(embryo.vessel.controllers
                .service(embryo.additionalInformation.distanceCircles, scope));

        return sections;
    }

    module.controller("YourVesselLayerControl", [ '$scope', 'VesselService', 'Subject', 'RouteService',
            function($scope, VesselService, Subject, RouteService) {
                var mmsi = Subject.getDetails().shipMmsi;

                VesselService.subscribe(mmsi, function(error, vesselOverview, vesselDetails) {
                    if (!error) {
                        embryo.vessel.setMarkedVessel(mmsi);

                        if (vesselDetails.additionalInformation.routeId) {
                            RouteService.getRoute(vesselDetails.additionalInformation.routeId, function(route) {
                                route.active = true;
                                route.own = true;
                                yourShipRouteLayer.clear();
                                yourShipRouteLayer.draw([route], "active");
                            });
                        } else {
                            yourShipRouteLayer.hideFeatures(function(feature) {
                                return feature.attributes.featureType === "route" && feature.attributes.data.active && feature.attributes.data.own;
                            });
                        }
                    }
                });
            } ]);

    module.controller("YourVesselControl", [ '$scope', 'VesselService', 'Subject', 'VesselInformation',
            function($scope, VesselService, Subject, VesselInformation) {
                var mmsi = Subject.getDetails().shipMmsi;

                $scope.$watch('vesselOverview', function(newValue, oldValue) {
                    $scope.sections = initSections($scope);
                }, true);

                $scope.$watch('vesselDetails', function(newValue, oldValue) {
                    $scope.sections = initSections($scope);
                }, true);

                $scope.$watch(function() {
                    return embryo.vessel.information.getVesselInformation().length;
                }, function(newValue, oldValue) {
                    if (newValue > 0 && newValue !== oldValue) {
                        $scope.sections = initSections($scope);
                    }
                });

                VesselService.subscribe(mmsi, function(error, vesselOverview, vesselDetails) {
                    if (!error) {
                        $scope.yourAis = yourAis(vesselDetails);
                        $scope.vesselOverview = vesselOverview;
                        $scope.vesselScope.yourVesselName = vesselOverview.name;
                        $scope.vesselDetails = vesselDetails;
                    }
                });

                $scope.viewAis = function($event) {
                    $event.preventDefault();
                    VesselInformation.hideAll();
                    embryo.controllers.ais.show($scope.vesselDetails.aisVessel);
                }
            } ]);

    module.controller("ZoomYourVesselCtrl", [ '$scope', 'Subject', function($scope, Subject) {
        $scope.zoomToYourVessel = function() {
            embryo.vessel.goToVesselLocation(embryo.vessel.lookupVessel(Subject.getDetails().shipMmsi));
        }
    } ]);
});
