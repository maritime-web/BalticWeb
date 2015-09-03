$(function () {

//    var msiLayer = new MsiLayer();
//    addLayerToMap("msi", msiLayer, embryo.map);

    var module = angular.module('embryo.sar.controllers', ['embryo.sar.service', 'embryo.common.service']);

    module.controller("SARControl", ['$scope', function ($scope) {
        $scope.selected = {
            open: false
        }
    }]);

    module.controller("SARLayerControl", ['SarService', function (SarService) {
        // TODO Rewrite this to make it work this.
        SarService.sarSelected(function (error, sarOperation) {
            SarLayerSingleton.getInstance().draw(sarOperation);
        });
    }]);

    module.controller("OperationsControl", ['$scope', 'SarService', 'ViewService', '$log',
        function ($scope, SarService, ViewService, $log) {

            var db = new PouchDB('arcticweb');

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

            db.changes({
                since: 'now',
                live: true,
                include_docs: true
            }).on('change', function (change) {
                console.log("before changes")
                console.log(change);
                console.log("after changes")

                $scope.sars = change.doc.operations;
                $scope.$apply({})
            })

            db.get("sar-operations").catch(function (err) {
                db.put({
                    _id: "sar-operations",
                    operations: []
                });
                $scope.sars = [];
            }).then(function (res) {
                console.log(res);
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

    module.controller("OperationControl", ['$scope', 'SarService', 'ViewService', '$log',
        function ($scope, SarService, ViewService, $log) {

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

            SarService.sarSelected("OperationControl", function (sar) {
                $scope.selected.open = true;
                console.log("SAR SELECTED")
                console.log(sar)

                $scope.selected.sar = sar;
                $scope.sar = sar;

                $scope.$apply(function () {
                })
            })

            $scope.edit = function ($event, sar) {
                $event.preventDefault();
                $scope.newSarProvider.show({sarId: sar.id});
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
