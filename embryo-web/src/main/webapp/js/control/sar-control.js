$(function () {

//    var msiLayer = new MsiLayer();
//    addLayerToMap("msi", msiLayer, embryo.map);

    var module = angular.module('embryo.sar.controllers', ['embryo.sar.service', 'embryo.common.service', 'embryo.storageServices']);

    module.controller("SARControl", ['$scope', function ($scope) {
        $scope.selected = {
            open: false
        }
    }]);

    module.controller("SARLayerControl", ['SarService', 'LivePouch', function (SarService, LivePouch) {
        // TODO Rewrite this to make it work this.
        SarService.sarSelected("SARLayerControl", function (sarId) {


            LivePouch.get(sarId).then(function (sar) {
                SarLayerSingleton.getInstance().draw(sar);

                var center = null;
                if (sar.output.datum) {
                    center = sar.output.datum;
                } else if (sar.output.downWind) {
                    center = sar.output.downWind;
                }
                embryo.map.setCenter(center.lon, center.lat, 8);
            });
        });

    }]);

    module.controller("OperationsControl", ['$scope', 'SarService', 'ViewService', '$log', 'LivePouch',
        function ($scope, SarService, ViewService, $log, LivePouch) {

            var subscription = ViewService.subscribe({
                name: "OperationsControl",
                onNewProvider: function () {

                    var viewProviders = ViewService.viewProviders();
                    $log.debug('onNewProvider, viewProvider=', viewProviders);
                    for (var index in viewProviders) {
                        if (viewProviders[index].type() === 'new') {
                            $scope.newSarProvider = viewProviders[index];
                        }
                    }
                }
            });

            $scope.$on("$destroy", function () {
                ViewService.unsubscribe(subscription);
            })

            LivePouch.changes({
                since: 'now',
                live: true,
                include_docs: true
            }).on('change', function (change) {
                $scope.sars = change.doc.operations;
                $scope.$apply({})
            })

            LivePouch.get("sar-operations").catch(function (err) {
                LivePouch.put({
                    _id: "sar-operations",
                    operations: []
                });
                $scope.sars = [];
            }).then(function (res) {
                $scope.sars = res.operations;
                $scope.$apply({})
            })

            $scope.sars = []

            $scope.view = function ($event, sar) {
                $event.preventDefault();
                SarService.selectSar(sar.id);
            }

            $scope.edit = function ($event, sar) {
                $event.preventDefault();
                $scope.newSarProvider.show({sarId: sar.id});
            }

            $scope.newSar = function () {
                $scope.newSarProvider.show({});
            }
        }]);

    module.controller("OperationControl", ['$scope', 'SarService', 'ViewService', '$log', 'LivePouch', '$timeout',
        function ($scope, SarService, ViewService, $log, LivePouch, $timeout) {

            var subscription = ViewService.subscribe({
                name: "OperationControl",
                onNewProvider: function () {

                    var viewProviders = ViewService.viewProviders();
                    $log.debug('onNewProvider, viewProvider=', viewProviders);
                    for (var index in viewProviders) {
                        if (viewProviders[index].type() === 'new') {
                            $scope.newSarProvider = viewProviders[index];
                        }
                    }
                }
            });

            $scope.$on("$destroy", function () {
                ViewService.unsubscribe(subscription);
            })

            SarService.sarSelected("OperationControl", function (sarId) {
                $scope.selected.open = true;
                if (!$scope.$$phase) {
                    $scope.$apply(function () {
                    });
                }

                LivePouch.get(sarId).catch(function (err) {
                    $log.error(err)
                    throw err;
                }).then(function (res) {
                    $timeout(function () {
                        $scope.selected.sar = res;
                        $scope.sar = res;
                    })
                })

            })

            $scope.edit = function ($event) {
                $event.preventDefault();
                $scope.newSarProvider.show({sarId: $scope.sar._id});
            }

            $scope.formatTs = formatTime;
            $scope.formatDecimal = embryo.Math.round10;

            $scope.formatPos = function (position) {
                if (!position) {
                    return ""
                }
                return formatLatitude(position.lat) + ", " + formatLongitude(position.lon);
            }

            $scope.formatLat = formatLatitude;
            $scope.formatLon = formatLongitude;

            $scope.confirmEnd = function () {
                $scope.newSarProvider.show({sarId: $scope.sar._id, page: "end"});
            }
        }]);

    module.controller("LogControl", ['$scope', 'Subject', 'SarService', function ($scope, Subject, SarService) {
        SarService.sarSelected("LogCtrl", function (selectedSarId) {
            $scope.selectedSarId = selectedSarId;
            if (!$scope.selectedSarId) {
                $scope.logs = null;
                return;
            }
        });
        $scope.send = function () {
            if ($scope.msg) {
                $scope.logs.$add({user: Subject.getDetails().userName, stmt: $scope.msg});
                $scope.msg = null;
            }
        }


    }]);


});
