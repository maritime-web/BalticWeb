$(function () {

//    var msiLayer = new MsiLayer();
//    addLayerToMap("msi", msiLayer, embryo.map);

    var module = angular.module('embryo.sar.controllers', ['embryo.sar.service', 'embryo.common.service', 'embryo.storageServices']);

    module.controller("SARControl", ['$scope', function ($scope) {
        $scope.selected = {
            open: false
        }
    }]);

    var SARTypeTxt = {};
    SARTypeTxt[embryo.sar.types.RapidResponse] = "Rapid response";
    SARTypeTxt[embryo.sar.types.DatumPoint] = "Datum point";
    SARTypeTxt[embryo.sar.types.DatumLine] = "Datum line";
    SARTypeTxt[embryo.sar.types.BackTrack] = "Back track";

    var SARStatusTxt = {};
    SARStatusTxt[embryo.SARStatus.STARTED] = "Active";
    SARStatusTxt[embryo.SARStatus.ENDED] = "Ended";

    var SARStatusLabel = {};
    SARStatusLabel[embryo.SARStatus.STARTED] = "label-success";
    SARStatusLabel[embryo.SARStatus.ENDED] = "label-default";

    var AllocationStatusTxt = {};
    AllocationStatusTxt[embryo.sar.effort.Status.Active] = "Active";
    AllocationStatusTxt[embryo.sar.effort.Status.DraftSRU] = "Draft SRU";
    AllocationStatusTxt[embryo.sar.effort.Status.DraftZone] = "Draft Zone";

    var AllocationStatusLabel = {};
    AllocationStatusLabel[embryo.sar.effort.Status.Active] = "label-success";
    AllocationStatusLabel[embryo.sar.effort.Status.DraftSRU] = "label-danger";
    AllocationStatusLabel[embryo.sar.effort.Status.DraftZone] = "label-danger";

    module.controller("SARLayerControl", ['SarService', 'LivePouch', '$timeout', function (SarService, LivePouch, $timeout) {
        var sars = [];
        SarService.sarSelected("SARLayerControl", function (sarId) {
            if (sarId) {
                $timeout(function () {
                    LivePouch.get(sarId).then(function (sar) {
                        var center = null;
                        if (sar.output.datum) {
                            center = sar.output.datum;
                        } else if (sar.output.downWind) {
                            center = sar.output.downWind.datum;
                        }
                        embryo.map.setCenter(center.lon, center.lat, 8);
                    }, 100);
                })
            }
        });

        LivePouch.changes({
            since: 'now',
            live: true,
            include_docs: true,
            filter: function (doc) {
                return doc._id.startsWith("sar-")
            }
        }).on('create', function (create) {
            sars.push(create.doc);
            SarLayerSingleton.getInstance().draw(sars);
        }).on('update', function (update) {
            console.log("update")

            var index = SarService.findSarIndex(sars, update.doc._id);
            sars[index] = update.doc;
            SarLayerSingleton.getInstance().draw(sars);
        }).on('delete', function (del) {
            var index = SarService.findSarIndex(sars, del.doc._id);
            sars = sars.splice(index, 1);
            SarLayerSingleton.getInstance().draw(sars);
        });

        LivePouch.allDocs({
            include_docs: true,
            startkey: 'sar-',
            endkey: 'sar-X'
        }).then(function (result) {
            var operations = []
            for (var index in result.rows) {
                operations.push(result.rows[index].doc);
            }
            sars = operations;
            SarLayerSingleton.getInstance().draw(sars);
        }).catch(function (err) {
            // TODO ERROR MESSAGE
            console.log("allDocs err")
            console.log(err);
        });
    }]);

    module.controller("OperationsControl", ['$scope', 'SarService', 'ViewService', '$log', 'LivePouch',
        function ($scope, SarService, ViewService, $log, LivePouch) {

            $scope.sars = []

            $scope.SARStatusTxt = SARStatusTxt;
            $scope.SARStatusLabel = SARStatusLabel;

            var subscription = ViewService.subscribe({
                name: "OperationsControl",
                onNewProvider: function () {

                    var viewProviders = ViewService.viewProviders();
                    $log.debug('onNewProvider, viewProvider=', viewProviders);

                    console.log("viewProviders=")
                    console.log(viewProviders)

                    for (var index in viewProviders) {
                        if (viewProviders[index].type() === 'newSar') {
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
                include_docs: true,
                filter: function (doc) {
                    return doc._id.startsWith("sar-")
                }
            }).on('create', function (create) {
                $scope.sars.push(SarService.toSmallSarObject(create.doc));
                $scope.$apply({})
            }).on('update', function (update) {
                var index = SarService.findSarIndex($scope.sars, update.doc._id);
                $scope.sars[index] = SarService.toSmallSarObject(update.doc);
                $scope.$apply({})
            }).on('delete', function (del) {
                var index = SarService.findSarIndex($scope.sars, del.doc._id);
                $scope.sars = $scope.sars.splice(index, 1);
                $scope.$apply({})
            });


            LivePouch.allDocs({
                include_docs: true,
                startkey: 'sar-',
                endkey: 'sar-X'
            }).then(function (result) {
                var operations = []
                for (var index in result.rows) {

                    operations.push({
                        id: result.rows[index].doc._id,
                        name: result.rows[index].doc.input.no,
                        status: result.rows[index].doc.status
                    })
                }
                $scope.sars = operations;
                $scope.$apply(function () {
                })
            }).catch(function (err) {
                console.log("allDocs err")
                console.log(err);
            });

            $scope.view = function ($event, sar) {
                $event.preventDefault()
                $scope.selected.sarIdentifier = sar.name;
                SarService.selectSar(sar.id);
            }

            $scope.edit = function ($event, sar) {
                $event.preventDefault();
                $scope.newSarProvider.show({sarId: sar.id});
            }

            $scope.newSar = function () {
                console.log($scope.newSarProvider)
                $scope.newSarProvider.show({});
            }
        }]);

    module.controller("OperationControl", ['$scope', 'SarService', 'ViewService', '$log', 'LivePouch', '$timeout',
        function ($scope, SarService, ViewService, $log, LivePouch, $timeout) {

            $scope.SARTypeTxt = SARTypeTxt;
            $scope.SARStatusTxt = SARStatusTxt;
            $scope.SARStatusLabel = SARStatusLabel;
            $scope.SARStatus = embryo.SARStatus;
            $scope.AllocationStatus = embryo.sar.effort.Status;

            $scope.AllocationStatusTxt = AllocationStatusTxt;
            $scope.AllocationStatusLabel = AllocationStatusLabel;


            var subscription = ViewService.subscribe({
                name: "OperationControl",
                onNewProvider: function () {
                    var viewProviders = ViewService.viewProviders();
                    $log.debug('OperationControl.onNewProvider, viewProvider=', viewProviders);
                    for (var index in viewProviders) {
                        if (viewProviders[index].type() === 'newSar') {
                            $scope.newSarProvider = viewProviders[index];
                        } else if (viewProviders[index].type() === 'effort') {
                            $scope.effortAllocationProvider = viewProviders[index];
                        }
                    }
                }
            });

            $scope.$on("$destroy", function () {
                ViewService.unsubscribe(subscription);
            })

            SarService.sarSelected("OperationControl", function (sarId) {
                $scope.selected.open = !!sarId;
                if (!$scope.$$phase) {
                    $scope.$apply(function () {
                    });
                }

                $scope.selected.sarId = sarId;
                if (sarId) {
                    LivePouch.get(sarId).catch(function (err) {
                        $log.error(err)
                        throw err;
                    }).then(function (res) {
                        $timeout(function () {
                            $scope.selected.sar = res;
                            $scope.sar = res;
                            console.log("operationcontrol")
                            console.log(res);
                        })
                    })
                    loadEffortAllocations();
                } else {
                    $scope.selected.sar = null;
                    $scope.sar = null;
                }
            })

            $scope.edit = function () {
                $scope.newSarProvider.show({sarId: $scope.sar._id});
            }

            $scope.effort = function () {
                $scope.effortAllocationProvider.show({sarId: $scope.sar._id});
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

            $scope.calculateAllocation = function ($event, allocation) {
                $event.preventDefault();
                $scope.effortAllocationProvider.show({allocationId: allocation._id, page: "effort"})
            }

            function loadEffortAllocations() {
                // find docs where sarId === selectedSarId
                LivePouch.query('sareffortview', {
                    key: $scope.selected.sarId,
                    include_docs: true
                }).then(function (result) {
                    var allocations = [];
                    for (var index in result.rows) {
                        allocations.push(result.rows[index].doc)
                    }
                    $scope.allocations = allocations
                    $scope.$apply(function () {
                    })
                    console.log("loadAllocations")
                    console.log($scope.allocations);
                }).catch(function (error) {
                    console.log("sareffortview error in controller.js")
                    console.log(error)
                });

                LivePouch.changes({
                    since: 'now',
                    live: true,
                    include_docs: true,
                    filter: "_view",
                    view: "sareffortview",
                    key: $scope.selected.sarId
                }).on('create', function (create) {
                    $scope.allocations.push(create.doc);
                    $scope.$apply({})
                }).on('update', function (update) {
                    var index = SarService.findSarIndex($scope.sars, update.doc._id);
                    $scope.sars[index] = SarService.toSmallSarObject(update.doc);
                    $scope.$apply({})
                }).on('delete', function (del) {
                    var index = SarService.findSarIndex($scope.sars, del.doc._id);
                    $scope.sars = $scope.sars.splice(index, 1);
                    $scope.$apply({})
                });
            }



        }]);

    module.controller("LogControl", ['$scope', 'Subject', 'SarService', 'LivePouch', function ($scope, Subject, SarService, LivePouch) {
        $scope.messages = []

        // create a design doc
        var ddoc = {
            _id: '_design/sarlogview',
            views: {
                sarlogview: {
                    map: function (doc) {
                        console.log(doc)
                        if (doc.msgSarId) {
                            emit(doc.msgSarId);
                        }
                    }.toString()
                }
            }
        }

        $scope.formatTs = formatTime;

        // TODO move to CouchDB server
        LivePouch.get('_design/sarlogview').then(function (existing) {
            ddoc._rev = existing._rev;
            LivePouch.put(ddoc).then(function (result) {
                console.log("ddoc update")
                console.log(result);
            }).catch(function (error) {
                console.log("ddoc update error")
                console.log(error)
            });
        })


        function displayMessages(selectedSarId) {
            // find docs where sarId === selectedSarId
            LivePouch.query('sarlogview', {
                key: selectedSarId,
                include_docs: true
            }).then(function (result) {
                var messages = [];
                for (var index in result.rows) {
                    messages.push(result.rows[index].doc)
                }
                $scope.messages = messages
                $scope.$apply(function () {
                })
            }).catch(function (error) {
                console.log("sarlogview error")
                console.log(error)
            });
        }

        function registerListeners() {
            if ($scope.changes) {
                $scope.changes.cancel();
            }

            $scope.changes = LivePouch.changes({
                since: 'now',
                live: true,
                include_docs: true,
                filter: "_view",
                view: "sarlogview"
            }).on('create', function (create) {
                $scope.messages.push(create.doc);
                $scope.$apply(function () {
                })
            });
        }

        SarService.sarSelected("LogCtrl", function (selectedSarId) {
            if (selectedSarId) {
                if ($scope.selectedSarId != selectedSarId) {
                    $scope.selectedSarId = selectedSarId
                    registerListeners();
                    displayMessages(selectedSarId);
                }
            } else {
                $scope.logs = null;
            }
        });

        $scope.send = function () {
            var msg = $scope.msg;
            $scope.msg = null;

            var msgObject = {
                msgSarId: $scope.selectedSarId,
                user: Subject.getDetails().userName,
                ts: Date.now(),
                value: msg
            }
            LivePouch.post(msgObject).then(function (result) {

            }).catch(function (err) {

            });
        }
    }]);
});
