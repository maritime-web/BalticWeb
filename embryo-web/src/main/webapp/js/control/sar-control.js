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
    SARTypeTxt[embryo.sar.Operation.RapidResponse] = "Rapid response";
    SARTypeTxt[embryo.sar.Operation.DatumPoint] = "Datum point";
    SARTypeTxt[embryo.sar.Operation.DatumLine] = "Datum line";
    SARTypeTxt[embryo.sar.Operation.BackTrack] = "Back track";

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
    AllocationStatusTxt[embryo.sar.effort.Status.DraftModifiedZone] = "Draft Zone";

    var AllocationStatusLabel = {};
    AllocationStatusLabel[embryo.sar.effort.Status.Active] = "label-success";
    AllocationStatusLabel[embryo.sar.effort.Status.DraftSRU] = "label-danger";
    AllocationStatusLabel[embryo.sar.effort.Status.DraftZone] = "label-danger";
    AllocationStatusLabel[embryo.sar.effort.Status.DraftModifiedZone] = "label-danger";

    function clone(object) {
        return JSON.parse(JSON.stringify(object));
    }

    module.controller("SARLayerControl", ['SarService', 'LivePouch', '$timeout', '$log',
        function (SarService, LivePouch, $timeout, $log) {
        var sarDocuments = [];

        SarLayerSingleton.getInstance().modified = function (zoneUpdate) {
            $log.debug("zone updated on map");
            $log.debug(zoneUpdate)
            LivePouch.get(zoneUpdate._id).then(function (zone) {
                zone.area = clone(zoneUpdate.area);
                zone.status = embryo.sar.effort.Status.DraftModifiedZone;
                LivePouch.put(zone).then(function () {
                    $log.debug("success saving updated zone")
                }).catch(function (error) {
                    $log.error("error saving updated zone")
                    $log.error(error);
                })
            })
        }

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

        function loadSarDocuments() {
            LivePouch.allDocs({
                include_docs: true,
                startkey: 'sar',
                endkey: 'sarx'
            }).then(function (result) {
                var documents = []
                for (var index in result.rows) {
                    if (result.rows[index].doc.docType != embryo.sar.Type.EffortAllocation ||
                        result.rows[index].doc.status == embryo.sar.effort.Status.Active ||
                        result.rows[index].doc.status == embryo.sar.effort.Status.DraftZone ||
                        result.rows[index].doc.status == embryo.sar.effort.Status.DraftModifiedZone) {
                        documents.push(result.rows[index].doc);
                    }
                }
                sarDocuments = documents;
                SarLayerSingleton.getInstance().draw(sarDocuments);
                $log.debug("loadSarDocuments");
                $log.debug(sarDocuments)
            }).catch(function (err) {
                // TODO ERROR MESSAGE
                $log.error("allDocs err")
                $log.error(err);
            });
        }

        LivePouch.changes({
            since: 'now',
            live: true,
            filter: function (doc) {
                return doc._id.startsWith("sar") &&
                    (doc.docType != embryo.sar.Type.EffortAllocation ||
                    doc.status == embryo.sar.effort.Status.Active ||
                    doc.status == embryo.sar.effort.Status.DraftZone)
            }
        }).on('change', function (result) {
            // We don't expect many SAR documents / objects at the same time
            // To achieve cleaner code, we therefore just load all SAR documents again
            // and redraw them, when one document is updated, created or deleted.
            loadSarDocuments();
        });

        loadSarDocuments();

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

                    $log.debug("viewProviders=")
                    $log.debug(viewProviders)

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
                $log.error("allDocs err")
                $log.error(err);
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
                $log.debug($scope.newSarProvider)
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
                            $log.debug("operationcontrol")
                            $log.debug(res);
                        })
                    })
                    loadEffortAllocations($scope.selected.sarId);
                    listen4EffortAllocationChanges($scope.selected.sarId);
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

            function loadEffortAllocations(sarId) {

                $log.debug("loadEffortAllocations, $scope.selected.sarId=" + sarId)

                // find docs where sarId === selectedSarId
                LivePouch.query('sareffortview', {
                    key: sarId,
                    include_docs: true
                }).then(function (result) {
                    var allocations = [];
                    for (var index in result.rows) {
                        allocations.push(result.rows[index].doc)
                    }

                    $log.debug("allocations")
                    $log.debug(allocations)

                    $scope.allocations = allocations
                    if (!$scope.$$phase) {
                        $scope.$apply(function () {
                        })
                    }
                    $log.debug("loadAllocations")
                    $log.debug($scope.allocations);
                }).catch(function (error) {
                    $log.error("sareffortview error in controller.js")
                    $log.error(error)
                });
            }

            function listen4EffortAllocationChanges(sarId) {
                LivePouch.changes({
                    since: 'now',
                    live: true,
                    include_docs: true,
                    /*
                     filter: function (doc) {
                     return (doc.docType == embryo.sar.Type.EffortAllocation && doc.effSarId == $scope.selected.sarId);
                     }*/
                    filter: "_view",
                    view: "sareffortview",
                    key: sarId
                }).on('change', function (change) {
                    $log.debug("listen4EffortAllocationChanges, change=")
                    $log.debug(change)
                    loadEffortAllocations(sarId);
                }).on('delete', function (deleted) {
                    $log.debug("listen4EffortAllocationChanges, deleted=")
                    $log.debug(deleted)
                    loadEffortAllocations(sarId);
                });
            }



        }]);

    module.controller("LogControl", ['$scope', 'Subject', 'SarService', 'LivePouch', '$log',
        function ($scope, Subject, SarService, LivePouch, $log) {
        $scope.messages = []

        // create a design doc
        var ddoc = {
            _id: '_design/sarlogview',
            views: {
                sarlogview: {
                    map: function (doc) {
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
                $log.debug("ddoc update")
                $log.debug(result);
            }).catch(function (error) {
                $log.error("ddoc update error")
                $log.error(error)
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
                $log.error("sarlogview error")
                $log.error(error)
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
