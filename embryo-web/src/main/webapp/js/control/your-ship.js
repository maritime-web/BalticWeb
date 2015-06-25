$(function() {

    var yourShipRouteLayer;
    embryo.postLayerInitialization(function(){
        yourShipRouteLayer = new RouteLayer();
        addLayerToMap("vessel", yourShipRouteLayer, embryo.map);
    })

    var module = angular.module('embryo.yourvessel.control', [ 'embryo.scheduleService', 'embryo.vessel.service' ]);

    function yourAis(data) {
        if (!data.aisVessel) {
            return null;
        }
        return embryo.vessel.aisToArray({
            "MMSI" : data.aisVessel.mmsi,
            "Call Sign" : data.aisVessel.callsign,
            "Country" : data.aisVessel.country,
            "Destination" : data.aisVessel.destination,
            "Nav Status": embryo.vessel.navStatusText(data.aisVessel.navStatus),
            "ETA": data.aisVessel.eta ? formatTime(data.aisVessel.eta) + " UTC" : ""
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
            name : "Additional Information ",
            services : [],
            type : "view"
        } ];

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

                VesselService.subscribe(mmsi, function (error, vesselDetails) {
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

    module.controller("YourVesselControl", [ '$scope', 'VesselService', 'Subject', 'VesselInformation', 'SubscriptionService',
        function ($scope, VesselService, Subject, VesselInformation, SubscriptionService) {
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

            var listSubscription = SubscriptionService.subscribe({
                subscriber: "YourVesselControl",
                name: "VesselService.list",
                fn: VesselService.list,
                interval: embryo.loadFrequence,
                success: function (vesselList) {
                    // we don't need the vessel list, we just need to know, that a new version of vesselOverview
                    // information for this particular vessel is available.
                    VesselService.clientSideMmsiSearch(mmsi, function (vesselOverview) {
                        $scope.vesselOverview = vesselOverview;
                        $scope.vesselScope.yourVesselName = vesselOverview.name;
                    })
                }
            });

            var detailsSubscription = VesselService.subscribe(mmsi, function (error, vesselDetails) {
                    if (!error) {
                        $scope.yourAis = yourAis(vesselDetails);
                        $scope.vesselDetails = vesselDetails;
                    }
                });

                $scope.viewAis = function($event) {
                    $event.preventDefault();
                    VesselInformation.hideAll();
                    embryo.controllers.ais.show($scope.vesselDetails.aisVessel);
                }

            $scope.$on("$destroy", function () {
                SubscriptionService.unsubscribe(listSubscription);
                VesselService.unsubscribe(detailsSubscription);
            })
        } ]);

    module.controller("ZoomYourVesselCtrl", [ '$scope', 'Subject', function($scope, Subject) {
        $scope.zoomToYourVessel = function() {
            embryo.vessel.goToVesselLocation(embryo.vessel.lookupVessel(Subject.getDetails().shipMmsi));
        }
    } ]);
});
