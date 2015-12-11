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
    AllocationStatusTxt[embryo.sar.effort.Status.Active] = "Shared";
    AllocationStatusTxt[embryo.sar.effort.Status.DraftSRU] = "No sub area";
    AllocationStatusTxt[embryo.sar.effort.Status.DraftZone] = "Not shared";
    AllocationStatusTxt[embryo.sar.effort.Status.DraftModifiedOnMap] = "Not shared";

    var AllocationStatusLabel = {};
    AllocationStatusLabel[embryo.sar.effort.Status.Active] = "label-success";
    AllocationStatusLabel[embryo.sar.effort.Status.DraftSRU] = "label-danger";
    AllocationStatusLabel[embryo.sar.effort.Status.DraftZone] = "label-danger";
    AllocationStatusLabel[embryo.sar.effort.Status.DraftModifiedOnMap] = "label-danger";

    function clone(object) {
        return JSON.parse(JSON.stringify(object));
    }

    module.controller("SARLayerControl", ['SarService', 'LivePouch', '$timeout', '$log',
        function (SarService, LivePouch, $timeout, $log) {
        var sarDocuments = [];

        SarLayerSingleton.getInstance().modified = function (zoneUpdate) {
            $log.debug("zone updated on map");
            $log.debug(zoneUpdate);
            LivePouch.get(zoneUpdate._id).then(function (zone) {
                angular.extend(zone.area, clone(zoneUpdate.area));
                // SAR features on map should not be redrawn, when one of the features
                // have been modified by dragging/resizing it on the map.
                // Special status therefore introduced for this scenario.
                zone.status = embryo.sar.effort.Status.DraftModifiedOnMap;
                // FIXME can not rely on local computer time
                zone.modified = Date.now();
                LivePouch.put(zone).then(function () {
                    $log.debug("success saving updated zone")
                }).catch(function (error) {
                    $log.error("error saving updated zone");
                    $log.error(error);
                })
            })
        };

        SarService.sarSelected("SARLayerControl", function (sarId) {
            if (sarId) {
                $timeout(function () {
                    LivePouch.get(sarId).then(function (sar) {
                        SarLayerSingleton.getInstance().zoomToSarOperation(sar);
                    }, 5);
                })
            }
        });

        function loadSarDocuments() {
            LivePouch.allDocs({
                include_docs: true,
                startkey: 'sar',
                endkey: 'sarx'
            }).then(function (result) {
                var documents = [];
                for (var index in result.rows) {
                    var doc = result.rows[index].doc;
                    if ((doc['@type'] === embryo.sar.Type.Log && doc.lat && doc.lon) ||
                        doc['@type'] != embryo.sar.Type.EffortAllocation ||
                        doc.status == embryo.sar.effort.Status.Active ||
                        doc.status == embryo.sar.effort.Status.DraftZone ||
                        doc.status == embryo.sar.effort.Status.DraftModifiedOnMap) {
                        documents.push(doc);
                    }
                }
                sarDocuments = documents;
                SarLayerSingleton.getInstance().draw(sarDocuments);
                $log.debug("loadSarDocuments");
                $log.debug(sarDocuments)
            }).catch(function (err) {
                // TODO ERROR MESSAGE
                $log.error("allDocs err");
                $log.error(err);
            });
        }

        LivePouch.changes({
            since: 'now',
            live: true,
            filter: function (doc) {
                return doc._id.startsWith("sar") &&
                    (doc['@type'] != embryo.sar.Type.EffortAllocation ||
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

            $scope.sars = [];

            $scope.SARStatusTxt = SARStatusTxt;
            $scope.SARStatusLabel = SARStatusLabel;

            var subscription = ViewService.subscribe({
                name: "OperationsControl",
                onNewProvider: function () {
                    $scope.newSarProvider = ViewService.viewProviders()['newSar'];
                }
            });

            $scope.$on("$destroy", function () {
                ViewService.unsubscribe(subscription);
            });

            function loadSarOperations() {
                LivePouch.allDocs({
                    include_docs: true,
                    startkey: 'sar-',
                    endkey: 'sar-X'
                }).then(function (result) {
                    var operations = []
                    for (var index in result.rows) {
                        operations.push(SarService.toSmallSarObject(result.rows[index].doc));
                    }
                    $scope.sars = operations;
                    $scope.$apply(function () {
                    })
                }).catch(function (err) {
                    $log.error("allDocs err")
                    $log.error(err);
                });
            }

            loadSarOperations();

            LivePouch.changes({
                since: 'now',
                live: true,
                include_docs: true,
                filter: function (doc) {
                    return doc["@type"] && doc["@type"] == embryo.sar.Type.SearchArea;
                }
            }).on('change', function () {
                loadSarOperations();
            });


            $scope.view = function ($event, sar) {
                $event.preventDefault();
                $scope.selected.sarIdentifier = sar.name;
                SarService.selectSar(sar.id);
            };

            $scope.edit = function ($event, sar) {
                $event.preventDefault();
                $scope.newSarProvider.show({sarId: sar.id});
            };

            $scope.newSar = function () {
                $log.debug($scope.newSarProvider);
                $scope.newSarProvider.show({});
            }
        }]);

    module.controller("OperationControl", ['$scope', 'SarService', 'ViewService', '$log', 'LivePouch', '$timeout',
        function ($scope, SarService, ViewService, $log, LivePouch, $timeout) {

            $scope.SARTypeTxt = SARTypeTxt;
            $scope.SARStatusTxt = SARStatusTxt;
            $scope.SARStatusLabel = SARStatusLabel;
            $scope.SARStatus = embryo.SARStatus;

            var subscription = ViewService.subscribe({
                name: "OperationControl",
                onNewProvider: function () {
                    $scope.newSarProvider = ViewService.viewProviders()['newSar'];
                }
            });

            $scope.$on("$destroy", function () {
                ViewService.unsubscribe(subscription);
            });

            SarService.sarSelected("OperationControl", function (sarId) {
                $scope.selected.open = !!sarId;
                if (!$scope.$$phase) {
                    $scope.$apply(function () {
                    });
                }

                $scope.selected.sarId = sarId;
                if (sarId) {
                    LivePouch.get(sarId).catch(function (err) {
                        $log.error(err);
                        throw err;
                    }).then(function (res) {
                        $timeout(function () {
                            $scope.selected.sar = res;
                            $scope.sar = res;
                            $log.debug("operationcontrol");
                            $log.debug(res);
                        })
                    })
                } else {
                    $scope.selected.sar = null;
                    $scope.sar = null;
                }
            });

            $scope.formatTs = formatTime;
            $scope.formatDecimal = embryo.Math.round10;

            $scope.formatPos = function (position) {
                if (!position) {
                    return ""
                }
                return formatLatitude(position.lat) + ", " + formatLongitude(position.lon);
            };

            $scope.formatLat = formatLatitude;
            $scope.formatLon = formatLongitude;

            $scope.edit = function () {
                $scope.newSarProvider.show({sarId: $scope.sar._id});
            };

            $scope.newCoordinator = function () {
                $scope.newSarProvider.show({sarId: $scope.sar._id, page: "coordinator"});
            }

            $scope.confirmEnd = function () {
                $scope.newSarProvider.show({sarId: $scope.sar._id, page: "end"});
            }
        }]);

    module.controller("EffortAllocationControl", ['$scope', 'SarService', 'ViewService', '$log', 'LivePouch',
        function ($scope, SarService, ViewService, $log, LivePouch) {

            $scope.SARTypeTxt = SARTypeTxt;
            $scope.SARStatusTxt = SARStatusTxt;
            $scope.SARStatusLabel = SARStatusLabel;
            $scope.SARStatus = embryo.SARStatus;
            $scope.AllocationStatus = embryo.sar.effort.Status;

            $scope.AllocationStatusTxt = AllocationStatusTxt;
            $scope.AllocationStatusLabel = AllocationStatusLabel;


            var subscription = ViewService.subscribe({
                name: "EffortAllocationControl",
                onNewProvider: function () {
                    $scope.effortAllocationProvider = ViewService.viewProviders()['effort'];
                }
            });

            $scope.$on("$destroy", function () {
                ViewService.unsubscribe(subscription);
            });

            SarService.sarSelected("EffortAllocationControl", function (sarId) {
                if (sarId) {
                    loadEffortAllocations(sarId);
                    listen4EffortAllocationChanges(sarId);
                }
            });

            $scope.effort = function () {
                $scope.effortAllocationProvider.show({sarId: $scope.selected.sar._id});
            };

            $scope.formatTs = formatTime;
            $scope.formatDecimal = embryo.Math.round10;

            function loadEffortAllocations(sarId) {

                $log.debug("loadEffortAllocations, sarId=" + sarId);

                // find docs where sarId === selectedSarId
                LivePouch.query('sar/effortView', {
                    key: sarId,
                    include_docs: true
                }).then(function (result) {
                    var allocations = [];
                    var patterns = [];
                    for (var index in result.rows) {
                        if (result.rows[index].doc['@type'] === embryo.sar.Type.SearchPattern) {
                            patterns.push(result.rows[index].doc)
                        } else {
                            allocations.push(result.rows[index].doc)
                        }
                    }

                    $scope.selected.allocations = allocations
                    $scope.patterns = patterns;
                    if (!$scope.$$phase) {
                        $scope.$apply(function () {
                        })
                    }
                }).catch(function (error) {
                    $log.error("sareffortview error in controller.js");
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
                     return (doc['@type'] == embryo.sar.Type.EffortAllocation && doc.sarId == $scope.selected.sarId);
                     }*/
                    filter: "_view",
                    view: "sar/effortView",
                    key: sarId
                }).on('change', function (change) {
                    $log.debug("listen4EffortAllocationChanges, change=");
                    $log.debug(change);
                    loadEffortAllocations(sarId);
                }).on('delete', function (deleted) {
                    $log.debug("listen4EffortAllocationChanges, deleted=");
                    $log.debug(deleted);
                    loadEffortAllocations(sarId);
                });
            }
        }]);

    module.controller("LogControl", ['$scope', 'Subject', 'SarService', 'LivePouch', '$log', function ($scope, Subject, SarService, LivePouch, $log) {
        $scope.messages = [];

        $scope.formatTs = formatTime;
        $scope.position = {use: false}
        $scope.noPosition = function () {
            $scope.position.use = false;
            delete $scope.msg.latitude;
            delete $scope.msg.longitude;
        }


        $scope.isParticipant = function () {
            var allocations = $scope.selected.allocations;
            for (var index in allocations) {
                if (allocations[index].mmsi == Subject.getDetails().shipMmsi || allocations[index].name === Subject.getDetails().userName) {
                    return true;
                }
            }
            var coordinator = $scope.selected.sar.coordinator;

            return coordinator.mmsi == Subject.getDetails().shipMmsi || coordinator.name === Subject.getDetails().userName;
        }

        function displayMessages(selectedSarId) {
            // find docs where sarId === selectedSarId
            LivePouch.query('sar/logView', {
                key: selectedSarId,
                include_docs: true
            }).then(function (result) {
                var messages = [];
                for (var index in result.rows) {
                    messages.push(result.rows[index].doc)
                }
                messages.sort(function (msg1, msg2) {
                    return msg2.ts - msg1.ts;
                })

                $scope.messages = messages
                $scope.$apply(function () {
                })
            }).catch(function (error) {
                $log.error("sarlogview error");
                $log.error(error)
            });
        }

        function registerListeners(sarId) {
            if ($scope.changes) {
                $scope.changes.cancel();
            }

            $scope.changes = LivePouch.changes({
                since: 'now',
                live: true,
                include_docs: true,
                filter: "_view",
                view: "sar/logView",
                key: sarId
            }).on('change', function (create) {
                displayMessages(sarId)
            });
        }

        SarService.sarSelected("LogCtrl", function (selectedSarId) {
            if (selectedSarId) {
                if ($scope.selectedSarId != selectedSarId) {
                    $scope.selectedSarId = selectedSarId
                    registerListeners(selectedSarId);
                    displayMessages(selectedSarId);
                }
            } else {
                $scope.logs = null;
            }
        });

        $scope.send = function () {
            var msg = $scope.msg;
            $scope.msg = {};

            msg._id = "sarMsg-" + Date.now() + Math.random();
            msg.sarId = $scope.selectedSarId;
            msg.user = Subject.getDetails().userName;
            msg.ts = Date.now();
            msg["@type"] = embryo.sar.Type.Log

            LivePouch.post(msg).then(function (result) {
                $scope.noPosition();
            }).catch(function (err) {

            });
        }
    }]);
});
