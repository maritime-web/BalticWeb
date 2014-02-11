(function() {
    "use strict";

    var berthUrl = embryo.baseUrl + 'rest/berth/search';

    var scheduleModule = angular.module('embryo.schedule', [ 'embryo.scheduleService', 'embryo.routeService',
            'siyfion.typeahead', 'embryo.position', 'ui.bootstrap.collapse' ]);

    embryo.ScheduleCtrl = function($scope, VesselService, ScheduleService, RouteService) {
        var loadSchedule = function() {
            if ($scope.mmsi) {
                ScheduleService.getYourSchedule($scope.mmsi, function(schedule) {
                    $scope.idsOfVoyages2Delete = [];
                    $scope.voyages = schedule.voyages.slice();
                    $scope.voyages.push({});
                    $("#schedulePanel").css("display", "block");
                }, function(error) {
                    $scope.alertMessages = error;
                });
            }
        };

        embryo.controllers.schedule = {
            title : "Schedule",
            available : function(vesselOverview, vesselDetails) {
                if (vesselOverview.inAW) {
                    if (vesselDetails.additionalInformation.routeId)
                        return {
                            text : "ACTIVE",
                            klass : "success",
                            action : "edit"
                        }
                    else
                        return {
                            text : "INACTIVE",
                            action : "edit"
                        }
                }
                return false;
            },
            show : function(vesselOverview, vesselDetails) {
                $scope.mmsi = vesselDetails.mmsi;
                $scope.vesselDetails = vesselDetails;
                $scope.activeRouteId = vesselDetails.additionalInformation.routeId;
                loadSchedule();
                $scope.$apply(function() {
                });
            },
            close : function() {
                $scope.reset();
            }
        };

        $scope.options = {
            "Yes" : true,
            "No" : false
        };

        $scope.berths = {
            name : 'embryo_berths7',
            prefetch : {
                url : berthUrl,
                // 1 time
                ttl : 3600000
            },
            remote : berthUrl
        };

        $scope.getLastVoyage = function() {
            if (!$scope.voyages) {
                return null;
            }
            return $scope.voyages[$scope.voyages.length - 1];
        };

        $scope.$watch($scope.getLastVoyage, function(newValue, oldValue) {
            // add extra empty voyage on initialization
            if (newValue && Object.keys(newValue).length > 0 && Object.keys(oldValue).length === 0) {
                $scope.voyages.push({});
            }
        }, true);

        $scope.del = function(index) {
            if ($scope.voyages[index].maritimeId) {
                $scope.idsOfVoyages2Delete.push($scope.voyages[index].maritimeId);
            }
            $scope.voyages.splice(index, 1);
        };

        $scope.berthSelected = function(voyage, datum) {
            if (typeof datum !== 'undefined') {
                voyage.latitude = datum.latitude;
                voyage.longitude = datum.longitude;
            }
        };

        $scope.isActive = function(voyage) {
            if (!voyage || !voyage.route || !voyage.route.id) {
                return false;
            }

            if (!$scope.activeRoute) {
                return false;
            }

            return $scope.activeRouteId === voyage.route.id;
        };

        $scope.editRoute = function(index) {
            var context = {
                mmsi : $scope.mmsi,
                routeId : $scope.voyages[index].route ? $scope.voyages[index].route.id : null,
                voyageId : $scope.voyages[index].maritimeId,
                dep : $scope.voyages[index].berthName,
                etdep : $scope.voyages[index].departure,
            };
            if (index < $scope.voyages.length - 1) {
                context.des = $scope.voyages[index + 1].berthName;
                context.etdes = $scope.voyages[index + 1].arrival;
            }

            embryo.controllers.editroute.show(context);
        };

        $scope.uploadRoute = function(voyage) {
            embryo.controllers.uploadroute.show({
                vesselDetails : $scope.vesselDetails,
                voyageId : voyage.maritimeId
            });
        };

        $scope.activate = function(voyage) {
            resetMessages();
            RouteService.setActiveRoute(voyage.route.id, true, function() {
                VesselService
                        .updateVesselDetailParameter($scope.mmsi, "additionalInformation.routeId", voyage.route.id);
                $scope.activeRouteId = voyage.route.id;
            }, function(error) {
                $scope.alertMessages = error;
            });
        };
        $scope.deactivate = function(voyage) {
            resetMessages();
            RouteService.setActiveRoute(voyage.route.id, false, function() {
                VesselService.updateVesselDetailParameter($scope.mmsi, "additionalInformation.routeId", "");
                $scope.activeRouteId = null;
            }, function(error) {
                $scope.alertMessages = error;
            });
        };

        function resetMessages() {
            $scope.message = null;
            $scope.alertMessages = null;
        }

        $scope.reset = function() {
            resetMessages();
            $scope.idsOfVoyages2Delete = [];
            loadSchedule();
        };

        $scope.saveable = function() {
            if ($scope.scheduleEditForm.$invalid) {
                return false;
            }

            if (!(($scope.voyages && $scope.voyages.length >= 1) || ($scope.idsOfVoyages2Delete && $scope.idsOfVoyages2Delete.length > 0))) {
                return false;
            }

            return true;
        };

        $scope.save = function() {
            var index;
            // remove last empty element
            var scheduleRequest = {
                mmsi : $scope.mmsi,
                voyages : $scope.voyages.slice(0, $scope.voyages.length - 1),
                toDelete : $scope.idsOfVoyages2Delete
            };

            for (index in scheduleRequest.voyages) {
                delete scheduleRequest.voyages[index].route;
            }

            resetMessages();
            ScheduleService.save(scheduleRequest, function() {
                $scope.message = "Schedule saved successfully";
                loadSchedule();
            }, function(error) {
                $scope.alertMessages = error;
            });
        };
    };

    embryo.ScheduleViewCtrl = function($scope, ScheduleService, RouteService) {
        $scope.collapse = false;
        
        var loadSchedule = function() {
            if ($scope.mmsi) {
                ScheduleService.getSchedule($scope.mmsi, function(schedule) {
                    $scope.voyages = schedule.voyages.slice();
                    $("#scheduleViewPanel").css("display", "block");
                });
            }
        };

        $scope.routeLayer = RouteLayerSingleton.getInstance();

        $scope.scheduleLayer = new ScheduleLayer("#000000");
        addLayerToMap("vessel", $scope.scheduleLayer, embryo.map);

        embryo.controllers.scheduleView = {
            title : "Schedule",
            available : function(vesselOverview, vesselDetails) {
                return vesselDetails.additionalInformation.schedule;
            },
            show : function(vesselOverview, vesselDetails) {
                $scope.collapse = false;
                $scope.mmsi = vesselDetails.mmsi;
                $scope.activeRouteId = vesselDetails.additionalInformation.routeId;
                loadSchedule();
                $scope.$apply(function() {
                });
            },
            close : function() {
                $scope.routeLayer.clear();
                $scope.scheduleLayer.clear();
            }
        };

        $scope.isActive = function(voyage) {
            if (!voyage || !voyage.route || !voyage.route.id) {
                return false;
            }

            if (!$scope.activeRoute) {
                return false;
            }

            return $scope.activeRouteId === voyage.route.id;
        };

        $scope.formatLatitude = function(latitude) {
            return formatLatitude(latitude);
        };

        $scope.formatLongitude = function(latitude) {
            return formatLongitude(latitude);
        };

        $scope.formatDateTime = function(timeInMillis) {
            return formatTime(timeInMillis);
        };

        $scope.view = function() {
            $scope.collapse = true;
            $scope.scheduleLayer.draw($scope.voyages);
            $scope.scheduleLayer.zoomToExtent();
        };

        $scope.viewRoute = function(voyage) {
            $scope.collapse = true;
            RouteService.getRoute(voyage.route.id, function(route) {
                var routeType = embryo.route.service.getRouteType($scope.mmsi, voyage.route.id); 
                $scope.routeLayer.draw(route, routeType);
                $scope.routeLayer.zoomToExtent();
            });
        };

    };

}());
