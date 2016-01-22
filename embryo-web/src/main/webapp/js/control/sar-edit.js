$(function () {

//    var msiLayer = new MsiLayer();
//    addLayerToMap("msi", msiLayer, embryo.map);

    var module = angular.module('embryo.sar.views', ['embryo.sar.service', 'embryo.common.service', 'ui.bootstrap.typeahead', 'embryo.validation.compare', 'embryo.datepicker', 'embryo.position']);

    module.directive('lteq', function () {
        return {
            require: 'ngModel',
            link: function (scope, element, attr, ngModelController) {
                var path = attr.lteq.split('.');

                function comparedTo() {
                    var value = scope;
                    for (var index in path) {
                        var v = path[index];
                        value = value[v];
                    }
                    return value
                }

                function valid(value1, value2) {
                    return value1 <= value2;
                }

                //For DOM -> model validation
                ngModelController.$parsers.unshift(function (value) {
                    var otherValue = comparedTo();
                    ngModelController.$setValidity('lteq', valid(value, otherValue));
                    return value;
                });

                //For model -> DOM validation
                ngModelController.$formatters.unshift(function (value) {
                    var otherValue = comparedTo();
                    ngModelController.$setValidity('lteq', valid(value, otherValue));
                    return value;
                });
            }
        };
    });

    function SarTypeData(text, img) {
        this.text = text;
        this.img = img;
    }

    //var sarTypes = embryo.sar.Operation;
    var sarTypeDatas = {}
    sarTypeDatas[embryo.sar.Operation.RapidResponse] = new SarTypeData("Rapid response", "/img/sar/generic.png");
    sarTypeDatas[embryo.sar.Operation.DatumPoint] = new SarTypeData("Datum point", "/img/sar/datumpoint.png");
    sarTypeDatas[embryo.sar.Operation.DatumLine] = new SarTypeData("Datum line", "/img/sar/datumline.png");
    sarTypeDatas[embryo.sar.Operation.BackTrack] = new SarTypeData("Back track", "/img/sar/generic.png")

    module.controller("SAROperationEditController", ['$scope', 'ViewService', 'SarService', '$q', 'LivePouch',
        function ($scope, ViewService, SarService, $q, LivePouch) {

            $scope.alertMessages = [];

            function initNewSar() {
                var now = Date.now();
                $scope.sar = {
                    type: embryo.sar.Operation.RapidResponse,
                    no: SarService.createSarId(),
                    searchObject: $scope.searchObjects[0].id,
                    yError: 0.1,
                    safetyFactor: 1.0,
                    startTs: now + 1000 * 60 * 60
                }

                $scope.sarOperation = {}

                if (!$scope.sar.surfaceDriftPoints) {
                    $scope.sar.surfaceDriftPoints = [{}];
                }
                if (!$scope.sar.surfaceDriftPoints[0].ts) {
                    $scope.sar.surfaceDriftPoints[0].ts = now;
                }

                if ($scope.sar.type != embryo.sar.Operation.DatumLine) {
                    if (!$scope.sar.lastKnownPosition) {
                        $scope.sar.lastKnownPosition = {};
                }
                    if (!$scope.sar.lastKnownPosition.ts) {
                        $scope.sar.lastKnownPosition.ts = now;
                    }
                }
            }

        $scope.provider = {
            doShow: false,
            title: "Create SAR",
            type: "newSar",
            show: function (context) {
                $scope.page = context && context.page ? context.page : 'typeSelection'
                if (context.sarId) {
                    LivePouch.get(context.sarId).then(function (sarOperation) {
                        $scope.sarOperation = sarOperation;
                        $scope.sar = sarOperation.input;
                    })
                } else {
                    initNewSar();
                }

                this.doShow = true;
            },
            close: function () {
                this.doShow = false;
            }
        };
        ViewService.addViewProvider($scope.provider);

        $scope.close = function ($event) {
            $event.preventDefault();
            $scope.provider.close();
        };

        $scope.searchObjects = SarService.searchObjectTypes();
            $scope.sarTypes = embryo.sar.Operation;
            $scope.sarTypeValues = [embryo.sar.Operation.RapidResponse, embryo.sar.Operation.DatumPoint, embryo.sar.Operation.DatumLine, embryo.sar.Operation.BackTrack];
            $scope.sarTypeDatas = sarTypeDatas;

            $scope.tmp = {
                sarTypeData: $scope.sarTypeDatas[0]
        }

        $scope.getDirections = function (query) {
            return function () {
                var deferred = $q.defer();
                var result = SarService.queryDirections(query);
                deferred.resolve(result);
                return deferred.promise;
            }().then(function (res) {
                return res;
            });

        }

        $scope.back = function () {
            switch ($scope.page) {
                case ("sarResult") :
                {
                    $scope.alertMessages = []
                    $scope.page = 'sarInputs';
                    break;
                }
                case ("sarInputs") :
                {
                    $scope.alertMessages = []
                    $scope.page = 'typeSelection';
                    break;
                }
            }
        }

        $scope.next = function () {
            $scope.page = 'sarInputs';
        }


        $scope.createSarOperation = function () {
            var sarInput = $scope.sar;
            try {
                $scope.alertMessages = [];
                // retain PouchDB fields like _id and _rev
                var calculatedOperation = SarService.createSarOperation(sarInput);
                $scope.sarOperation['@type'] = calculatedOperation['@type'];
                $scope.sarOperation.coordinator = calculatedOperation.coordinator;
                $scope.sarOperation.input = calculatedOperation.input;
                $scope.sarOperation.output = calculatedOperation.output;
                if (!$scope.sarOperation.coordinator) {
                    $scope.sarOperation.coordinator = calculatedOperation.coordinator;
                }

                $scope.tmp.searchObject = SarService.findSearchObjectType($scope.sarOperation.input.searchObject);
                $scope.page = 'sarResult';
            } catch (error) {
                if (typeof error === 'object' && error.message) {
                    $scope.alertMessages.push("Internal error: " + error.message);
                    throw error;
                } else if (typeof error === 'string') {
                    $scope.alertMessages.push("Internal error: " + error);
                }
            }
        }

        $scope.formatTs = formatTime;
        $scope.formatPos = function (position) {
            if (!position) {
                return ""
            }
            return "(" + formatLatitude(position.lat) + ", " + formatLongitude(position.lon) + ")";
        }

        $scope.formatDecimal = embryo.Math.round10;

        $scope.addPoint = function () {
            $scope.sar.surfaceDriftPoints.push({});
        }

        $scope.removePoint = function () {
            if ($scope.sar.surfaceDriftPoints.length > 1) {
                $scope.sar.surfaceDriftPoints.splice($scope.sar.surfaceDriftPoints.length - 1, 1);
            }
        }

        $scope.finish = function () {
            if (!$scope.sarOperation._id) {
                $scope.sarOperation._id = "sar-" + Date.now();
                $scope.sarOperation.status = embryo.SARStatus.STARTED;
            }

            LivePouch.put($scope.sarOperation).then(function () {
                SarService.selectSar($scope.sarOperation._id);
                $scope.provider.doShow = false;
            }).catch(function (err) {

            });
        }

            $scope.end = function () {
                var id = $scope.sarOperation._id;

                LivePouch.get(id).then(function (sar) {
                    sar.status = embryo.SARStatus.ENDED;
                    LivePouch.put(sar).then(function () {
                        $scope.provider.doShow = false;
                        SarService.selectSar(null);
                    }).catch(function (err) {
                        console.log(err)
                    });
                });
            }

            $scope.getUsers = function (query) {
                UserPouch.get(query).then(function (sar) {
                    sar.status = embryo.SARStatus.ENDED;
                    LivePouch.put(sar).then(function () {
                        $scope.provider.doShow = false;
                        SarService.selectSar(null);
                    }).catch(function (err) {
                        console.log(err)
                });
                });
        }

    }]);

    module.controller("SARCoordinatorController", ['$scope', 'LivePouch', function ($scope, LivePouch) {
        $scope.coordinator = {
            user: {}
        }

        $scope.assign = function () {
            var sarOperation = clone($scope.sarOperation);
            sarOperation.coordinator = $scope.coordinator.user;
            LivePouch.put(sarOperation).then(function () {
                $scope.provider.close();
            }).catch(function (error) {
                $scope.alertMessages = [error.toString()];
            })
        }
    }]);

    module.controller("SARUsersController", ['$scope', '$q', 'UserPouch', 'VesselService', 'SarService',
        function ($scope, $q, UserPouch, VesselService, SarService) {

            $scope.getUsers = function (query) {
                return function () {
                    var deferred = $q.defer();
                    UserPouch.query("users/userView", {
                        startkey: query.toLowerCase(),
                        endkey: query.toLowerCase() + "\uffff",
                        include_docs: true
                    }).then(function (result) {
                        var users = SarService.extractDbDocs(result);
                        deferred.resolve(users);
                    });
                    return deferred.promise;
                }().then(function (res) {
                    return res;
                });
                ;
            }
            $scope.getUsersAndVessels = function (query) {
                var vessels = []
                VesselService.clientSideSearch(query, function (match) {
                    vessels = match;
                })

                return function () {
                    var deferred = $q.defer();
                    UserPouch.query("users/userView", {
                        startkey: query.toLowerCase(),
                        endkey: query.toLowerCase() + "\uffff",
                        include_docs: true
                    }).then(function (result) {
                        var users = SarService.mergeQueries(result, vessels);
                        deferred.resolve(users);
                    });
                    return deferred.promise;
                }().then(function (res) {
                    return res;
                });
                ;
            }
        }]);


    var targetText = {};
    targetText[embryo.sar.effort.TargetTypes.PersonInWater] = "Person in Water (PIW)";
    targetText[embryo.sar.effort.TargetTypes.Raft1Person] = "Raft 1 person";
    targetText[embryo.sar.effort.TargetTypes.Raft4Persons] = "Raft 4 persons";
    targetText[embryo.sar.effort.TargetTypes.Raft6Persons] = "Raft 6 persons";
    targetText[embryo.sar.effort.TargetTypes.Raft8Persons] = "Raft 8 persons";
    targetText[embryo.sar.effort.TargetTypes.Raft10Persons] = "Raft 10 persons";
    targetText[embryo.sar.effort.TargetTypes.Raft15Persons] = "Raft 15 persons";
    targetText[embryo.sar.effort.TargetTypes.Raft20Persons] = "Raft 20 persons";
    targetText[embryo.sar.effort.TargetTypes.Raft25Persons] = "Raft 25 persons";
    targetText[embryo.sar.effort.TargetTypes.Motorboat15] = "Motorboat <= 15 feet";
    targetText[embryo.sar.effort.TargetTypes.Motorboat20] = "Motorboat 20 feet";
    targetText[embryo.sar.effort.TargetTypes.Motorboat33] = "Motorboat 33 feet";
    targetText[embryo.sar.effort.TargetTypes.Motorboat53] = "Motorboat 53 feet";
    targetText[embryo.sar.effort.TargetTypes.Motorboat78] = "Motorboat 78 feet";
    targetText[embryo.sar.effort.TargetTypes.Sailboat15] = "Sailboat 15 feet";
    targetText[embryo.sar.effort.TargetTypes.Sailboat20] = "Sailboat 20 feet";
    targetText[embryo.sar.effort.TargetTypes.Sailboat25] = "Sailboat 25 feet";
    targetText[embryo.sar.effort.TargetTypes.Sailboat30] = "Sailboat 30 feet";
    targetText[embryo.sar.effort.TargetTypes.Sailboat40] = "Sailboat 40 feet";
    targetText[embryo.sar.effort.TargetTypes.Sailboat50] = "Sailboat 50 feet";
    targetText[embryo.sar.effort.TargetTypes.Sailboat70] = "Sailboat 70 feet";
    targetText[embryo.sar.effort.TargetTypes.Sailboat83] = "Sailboat 83 feet";
    targetText[embryo.sar.effort.TargetTypes.Ship120] = "Ship 120 feet";
    targetText[embryo.sar.effort.TargetTypes.Ship225] = "Ship 225 feet";
    targetText[embryo.sar.effort.TargetTypes.Ship330] = "Ship >= 300 feet";
    targetText[embryo.sar.effort.TargetTypes.Boat17] = "Boat <= 17 feet";
    targetText[embryo.sar.effort.TargetTypes.Boat23] = "Boat 23 feet";
    targetText[embryo.sar.effort.TargetTypes.Boat40] = "Boat 40 feet";
    targetText[embryo.sar.effort.TargetTypes.Boat79] = "Boat 79 feet";

    function targetTypes(sruType) {

        console.log("targetTypes(" + sruType + ")")

        if (sruType === embryo.sar.effort.SruTypes.MerchantVessel) {
            return [
                embryo.sar.effort.TargetTypes.PersonInWater,
                embryo.sar.effort.TargetTypes.Raft4Persons,
                embryo.sar.effort.TargetTypes.Raft6Persons,
                embryo.sar.effort.TargetTypes.Raft15Persons,
                embryo.sar.effort.TargetTypes.Raft25Persons,
                embryo.sar.effort.TargetTypes.Boat17,
                embryo.sar.effort.TargetTypes.Boat23,
                embryo.sar.effort.TargetTypes.Boat40,
                embryo.sar.effort.TargetTypes.Boat79,
            ];
        }

        return [
            embryo.sar.effort.TargetTypes.PersonInWater,
            embryo.sar.effort.TargetTypes.Raft1Person,
            embryo.sar.effort.TargetTypes.Raft4Persons,
            embryo.sar.effort.TargetTypes.Raft6Persons,
            embryo.sar.effort.TargetTypes.Raft8Persons,
            embryo.sar.effort.TargetTypes.Raft10Persons,
            embryo.sar.effort.TargetTypes.Raft15Persons,
            embryo.sar.effort.TargetTypes.Raft20Persons,
            embryo.sar.effort.TargetTypes.Raft25Persons,
            embryo.sar.effort.TargetTypes.Motorboat15,
            embryo.sar.effort.TargetTypes.Motorboat20,
            embryo.sar.effort.TargetTypes.Motorboat33,
            embryo.sar.effort.TargetTypes.Motorboat53,
            embryo.sar.effort.TargetTypes.Motorboat78,
            embryo.sar.effort.TargetTypes.Sailboat15,
            embryo.sar.effort.TargetTypes.Sailboat20,
            embryo.sar.effort.TargetTypes.Sailboat25,
            embryo.sar.effort.TargetTypes.Sailboat30,
            embryo.sar.effort.TargetTypes.Sailboat40,
            embryo.sar.effort.TargetTypes.Sailboat50,
            embryo.sar.effort.TargetTypes.Sailboat70,
            embryo.sar.effort.TargetTypes.Sailboat83,
            embryo.sar.effort.TargetTypes.Ship120,
            embryo.sar.effort.TargetTypes.Ship225,
            embryo.sar.effort.TargetTypes.Ship330
        ];
    }

    var typeText = {}
    typeText[embryo.sar.effort.SruTypes.MerchantVessel] = "Merchant vessel";
    typeText[embryo.sar.effort.SruTypes.SmallerVessel] = "Small vessel (40 feet)";
    typeText[embryo.sar.effort.SruTypes.Ship] = "Ship (50 feet)";

    var AllocationStatusTxt = {};
    AllocationStatusTxt[embryo.sar.effort.Status.Active] = "Shared";
    AllocationStatusTxt[embryo.sar.effort.Status.DraftSRU] = "No sub area";
    AllocationStatusTxt[embryo.sar.effort.Status.DraftZone] = "Not shared";
    AllocationStatusTxt[embryo.sar.effort.Status.DraftPattern] = "Not shared";
    AllocationStatusTxt[embryo.sar.effort.Status.DraftModifiedOnMap] = "Not shared";

    var AllocationStatusLabel = {};
    AllocationStatusLabel[embryo.sar.effort.Status.Active] = "label-success";
    AllocationStatusLabel[embryo.sar.effort.Status.DraftSRU] = "label-danger";
    AllocationStatusLabel[embryo.sar.effort.Status.DraftZone] = "label-danger";
    AllocationStatusLabel[embryo.sar.effort.Status.DraftPattern] = "label-danger";
    AllocationStatusLabel[embryo.sar.effort.Status.DraftModifiedOnMap] = "label-danger";

    function clone(object) {
        return JSON.parse(JSON.stringify(object));
    }

    module.controller("SarEffortAllocationController", ['$scope', 'ViewService', 'SarService', 'LivePouch',
        function ($scope, ViewService, SarService, LivePouch) {
            $scope.alertMessages = [];
            $scope.message = null;
            $scope.srus = [];

            $scope.AllocationStatus = embryo.sar.effort.Status;

            $scope.AllocationStatusTxt = AllocationStatusTxt;
            $scope.AllocationStatusLabel = AllocationStatusLabel;

            $scope.fatigues = [0.5, 1.0];
            $scope.targetText = targetText;

            $scope.typeText = typeText;
            $scope.sruTypes = [
                embryo.sar.effort.SruTypes.MerchantVessel,
                /*                embryo.sar.effort.SruTypes.SmallerVessel,
                 embryo.sar.effort.SruTypes.Ship*/
            ]

            $scope.visibilityValues = [1, 3, 5, 10, 15, 20];


            function loadAllocation(allocationId) {
                // find docs where sarId === selectedSarId
                LivePouch.get(allocationId).then(function (allocation) {
                    $scope.effort = allocation;
                    $scope.initEffortAllocation();
                }).catch(function (error) {
                    console.log("loadAllocation error")
                    console.log(error)
                });
            }

            function patternsMap(patterns) {
                var result = {};
                for (var index in patterns) {
                    var pattern = patterns[index];
                    if (!result[pattern.effId] || pattern.status != embryo.sar.effort.Status.Active) {
                        result[pattern.effId] = {
                            id: pattern._id,
                            status: pattern.status
                        };
                    }
                }
                return result
            }

            function loadSRUs() {
                //TODO request both search pattern and zones
                // build 2 structures
                // one array of zones/srus
                // one object/array of search patterns
                // use the latter for determining the button to display and implementing the next action

                $scope.srus = []
                // find docs where sarId === selectedSarId
                LivePouch.query('sar/effortView', {
                    key: $scope.sarId,
                    include_docs: true
                }).then(function (result) {
                    var srus = [];
                    var patterns = [];
                    for (var index in result.rows) {
                        if (result.rows[index].doc['@type'] === embryo.sar.Type.SearchPattern) {
                            patterns.push(result.rows[index].doc)
                        } else {
                            srus.push(result.rows[index].doc)
                        }
                    }
                    $scope.srus = srus
                    $scope.patterns = patternsMap(patterns);


                }).catch(function (error) {
                    console.log("sareffortview error")
                    console.log(error)
                });
            }

            $scope.provider = {
                doShow: false,
                title: "SarEffortAllocation",
                type: "effort",
                show: function (context) {
                    $scope.alertMessages = null;
                    $scope.message = null;
                    $scope.page = context && context.page ? context.page : 'SRU';
                    if (context && context.sarId) {
                        $scope.sarId = context.sarId
                        $scope.toSrus();
                    } else if (context && context.allocationId) {
                        loadAllocation(context.allocationId)
                    }

                    this.doShow = true;

                },
                close: function () {
                    SarLayerSingleton.getInstance().removeTemporarySearchPattern();
                    this.doShow = false;
                }
            };
            ViewService.addViewProvider($scope.provider);

            $scope.close = function ($event) {
                $event.preventDefault();
                $scope.provider.close();
            };

            $scope.newUnit = function () {
                $scope.sru = {
                    fatigue: 1.0,
                    type: embryo.sar.effort.SruTypes.MerchantVessel,
                    time: 1
                }
                $scope.page = 'editUnit';
            };


            $scope.editSRU = function ($event, SRU) {
                $event.preventDefault();
                $scope.sru = clone(SRU);
                $scope.page = 'editUnit';
            }

            $scope.toConfirmDelSRU = function ($event, SRU) {
                $event.preventDefault();
                $scope.sru = SRU;
                $scope.alertMessages = null;
                $scope.message = null;
                $scope.page = 'deleteSRU';
            }

            $scope.removeSRU = function (SRU) {
                LivePouch.query('sar/effortView', {
                    key: SRU.sarId,
                    include_docs: true
                }).then(function (result) {
                    var toRemove = [];
                    for (var index in result.rows) {
                        var doc = result.rows[index].doc;
                        if (doc['@type'] === embryo.sar.Type.EffortAllocation && doc._id == SRU._id
                            || doc['@type'] === embryo.sar.Type.SearchPattern && doc.effId === SRU._id) {
                            toRemove.push(doc)
                            doc._deleted = true;
                        }
                    }

                    //delete allocations and search patterns
                    return LivePouch.bulkDocs(toRemove)
                }).then(function () {
                    $scope.toSrus();
                }).catch(function (error) {
                    $scope.alertMessages = ["Internal error removing SRU", error];
                });
            }

            $scope.toSrus = function () {
                $scope.alertMessages = null;
                $scope.message = null;
                loadSRUs();
                $scope.page = "SRU";
            }

            $scope.initEffortAllocation = function () {
                $scope.alertMessages = null;
                $scope.message = null;
                if (!$scope.effort) {
                    $scope.effort = {}
                }
                $scope.targetTypes = targetTypes($scope.effort.type);

                var type = $scope.effort.type;

                var latest = SarService.latestEffortAllocationZone($scope.srus, function (zone) {
                    return !type || zone.type === type;
                });

                if (!$scope.effort.target) {
                    $scope.effort.target = latest ? latest.target : embryo.sar.effort.TargetTypes.PersonInWater;
                }
                if (!$scope.effort.visibility) {
                    $scope.effort.visibility = latest ? latest.visibility : $scope.visibilityValues[0];
                }
                if (!$scope.effort.pod) {
                    $scope.effort.pod = latest ? latest.pod : 78;
                }
                if (!$scope.effort.waterElevation && latest) {
                    $scope.effort.waterElevation = latest.waterElevation;
                }
                if (!$scope.effort.wind && latest) {
                    $scope.effort.wind = latest.wind;
                }

                if ($scope.effort.status === embryo.sar.effort.Status.Active) {
                    $scope.message = "Sub area is edited by creating a copy of the existing shared sub area. \n";
                    $scope.message += "Write new values below, Calculate sub area, drag and shape sub area on the map and Share it. ";
                    $scope.message += "Your will hereby also replace the existing shared sub area. ";
                }
            }

            $scope.toSubAreaCalculation = function (effort) {
                $scope.effort = clone(effort);
                $scope.initEffortAllocation();
                $scope.page = "effort";
            }

            $scope.confirmActivation = function (effort) {
                $scope.effort = effort;
                $scope.page = "activate";
            }

            $scope.calculate = function () {

                if ($scope.effort.status === embryo.sar.effort.Status.Active) {
                    delete $scope.effort._rev;
                    delete $scope.effort.area;
                    $scope.effort._id = "sarEf-" + Date.now();
                }

                LivePouch.get($scope.effort.sarId).then(function (sar) {
                    var allocation = null;
                    try {
                        allocation = SarService.calculateEffortAllocations($scope.effort, sar);
                    } catch (error) {
                        console.log(error)
                        $scope.alertMessages = ["internal error", error];
                    }

                    if (allocation) {
                        LivePouch.put(allocation).then(function () {
                            // TODO fix problem. View closing after first save
                            $scope.provider.close();
                        }).catch(function (error) {
                            $scope.alertMessages = ["internal error", error];
                        });
                    }
                }).catch(function (error) {
                    $scope.alertMessages = ["internal error", error];
                });
            }

            $scope.activate = function () {
                // CONFIRM calculation and movement within circle before sending to other vessels
                // this to minimize data traffic
                function persist(eff) {
                    var effort = clone(eff);
                    effort.status = embryo.sar.effort.Status.Active;
                    // FIXME can not rely on local computer time
                    effort.modified = Date.now();
                    LivePouch.put(effort).then(function () {
                        $scope.toSrus();
                    }).catch(function (error) {
                        console.log("error saving effort allocation")
                        console.log(error)
                    })
                }

                function deleteEffortAllocationsForSameUser(effort) {
                    LivePouch.query('sar/effortView', {
                        key: effort.sarId,
                        include_docs: true
                    }).then(function (result) {
                        var efforts = [];
                        for (var index in result.rows) {
                            var doc = result.rows[index].doc;

                            if (doc['@type'] === embryo.sar.Type.EffortAllocation && doc.name == effort.name
                                && doc._id !== effort._id) {

                                efforts.push(doc)
                                doc._deleted = true;
                            } else if (doc['@type'] === embryo.sar.Type.SearchPattern && doc.name === effort.name) {
                                efforts.push(doc)
                                doc._deleted = true;
                            }
                        }

                        //delete allocations and search patterns
                        return LivePouch.bulkDocs(efforts)
                    }).then(function () {
                        persist(effort);
                    }).catch(function (err) {
                        console.log(err)
                    });
                }

                deleteEffortAllocationsForSameUser($scope.effort);
            }

            function pattern(type, text) {
                return {
                    type: type,
                    label: text
                }
            }

            function initSearchPattern(zone, latest) {
                var SearchPattern = embryo.sar.effort.SearchPattern;

                $scope.sp = {
                    type: latest && latest.type ? latest.type : embryo.sar.effort.SearchPattern.ParallelSweep
                };

                $scope.SearchPattern = embryo.sar.effort.SearchPattern;
                $scope.other = {
                    corners: SarService.searchPatternCspLabels(zone)
                };
                $scope.patterns = [
                    pattern(SearchPattern.ParallelSweep, "Parallel sweep search"),
                    pattern(SearchPattern.CreepingLine, "Creeping line search"),
                    pattern(SearchPattern.ExpandingSquare, "Expanding square search"),
                    pattern(SearchPattern.SectorPattern, "Sector pattern search"),
                    pattern(SearchPattern.TrackLineReturn, "Track line search, return"),
                    pattern(SearchPattern.TrackLine, "Track line search, non-return"),
                ]

                $scope.spImages = {};
                $scope.spImages[SearchPattern.ParallelSweep] = "img/sar/parallelsweepsearch.png";
                $scope.spImages[SearchPattern.CreepingLine] = "img/sar/creepinglinesearch.png";
                $scope.spImages[SearchPattern.ExpandingSquare] = "img/sar/expandingsquaresearch.png";
                $scope.spImages[SearchPattern.SectorPattern] = "img/sar/.png";
                $scope.spImages[SearchPattern.TrackLineReturn] = "img/sar/tracklinesearchreturn.png";
                $scope.spImages[SearchPattern.TrackLine] = "img/sar/tracklinesearchnonreturn.png";

            }

            function findNewestSearchPattern(zone, init) {
                LivePouch.query('sar/searchPattern', {
                    key: zone.sarId,
                    include_docs: true
                }).then(function (result) {
                    var patterns = [];
                    for (var index in result.rows) {
                        patterns.push(result.rows[index].doc);
                    }
                    init(zone, SarService.findLatestModified(patterns));
                }).catch(function (error) {
                    console.log("sarsearchpattern error")
                    console.log(error)
                });

            }

            $scope.createSearchPattern = function (zone) {
                $scope.page = "searchPattern";
                $scope.sp = {};
                $scope.zone = zone;
                LivePouch.get(zone.sarId).then(function (sar) {
                    $scope.sar = sar;
                    findNewestSearchPattern(zone, initSearchPattern);
                })
            }

            $scope.editSearchPattern = function (zone, spId) {
                $scope.sp = {};
                LivePouch.get(spId).then(function (pattern) {
                    $scope.page = "searchPattern";
                    $scope.zone = zone;
                    initSearchPattern(zone)
                    $scope.origPattern = clone(pattern);
                    $scope.sp = clone(pattern);
                    $scope.searchPattern = pattern;

                    if (pattern.wps && pattern.wps.length > 0) {
                        $scope.sp.csp = {
                            lon: pattern.wps[0].longitude,
                            lat: pattern.wps[0].latitude
                        };
                    }
                    return LivePouch.get(zone.sarId)
                }).then(function (sar) {
                    $scope.sar = sar;
                }).catch(function (error) {
                    // FIXME don't treat error as a string be default.
                    $scope.errorMessages = [error];
                });
            }

            $scope.calculateCSP = function () {
                if ($scope.sp && $scope.sp.cornerKey && $scope.sp.cornerKey !== "") {
                    $scope.sp.csp = SarService.calculateCSP($scope.zone, $scope.sp.cornerKey)
                } else {
                    $scope.sp.csp = null;
                }

                this.generateSearchPattern();
            }

            $scope.generateSearchPattern = function () {
                if (($scope.sp.type === embryo.sar.effort.SearchPattern.ParallelSweep || $scope.sp.type === embryo.sar.effort.SearchPattern.CreepingLine)
                    && $scope.sp.csp && $scope.sp.csp.lon && $scope.sp.csp.lat) {
                    $scope.searchPattern = SarService.generateSearchPattern($scope.zone, $scope.sp);
                    SarLayerSingleton.getInstance().drawTemporarySearchPattern($scope.searchPattern);
                } else if ($scope.sp.type === embryo.sar.effort.SearchPattern.ExpandingSquare) {
                    var spCopy = clone($scope.sp);
                    spCopy.sar = $scope.sar;
                    $scope.searchPattern = SarService.generateSearchPattern($scope.zone, spCopy);
                    SarLayerSingleton.getInstance().drawTemporarySearchPattern($scope.searchPattern);
                } else {
                    SarLayerSingleton.getInstance().removeTemporarySearchPattern();
                }
            }

            $scope.cancelPattern = function () {
                $scope.sp = {}
                SarLayerSingleton.getInstance().removeTemporarySearchPattern();
                $scope.toSrus();

            }

            function saveSearchPattern(pattern) {
                LivePouch.put(pattern).then(function () {
                    SarLayerSingleton.getInstance().removeTemporarySearchPattern();
                    $scope.toSrus();
                }).catch(function (err) {
                    // FIXME, don't just assume error is a String
                    console.log(err);
                    $scope.errorMessages = [err];
                });
            }

            $scope.draftSearchPattern = function () {
                var pattern = clone($scope.searchPattern);
                pattern.status = embryo.sar.effort.Status.DraftPattern;
                saveSearchPattern(pattern);
            }

            $scope.shareSearchPattern = function () {
                var pattern = clone($scope.searchPattern);
                pattern.status = embryo.sar.effort.Status.Active;
                saveSearchPattern(pattern);
            }

            $scope.$on("$destroy", function () {
                SarLayerSingleton.getInstance().removeTemporarySearchPattern();
            })
        }]);

    module.controller("SarSruController", ['$scope', 'SarService', 'LivePouch', function ($scope, SarService, LivePouch) {
        $scope.alertMessages = null;
        $scope.message = null;

        $scope.participant = {
            user: {}
        }
        if ($scope.sru.name) {
            $scope.participant.user.name = $scope.sru.name;
        }
        if ($scope.sru.mmsi) {
            $scope.participant.user.mmsi = $scope.sru.mmsi;
        }

        $scope.saveUnit = function () {
            // If active, then make a new copy in status draft
            // the copy will replace the active zone, when itself being activated
            // TODO move much of this code into SarService where easier to unit test.
            var sru = clone($scope.sru);
            sru.name = $scope.participant.user.name;
            sru.mmsi = $scope.participant.user.mmsi;
            if (sru.status === embryo.sar.effort.Status.Active) {
                delete sru._id;
                delete sru._rev;
                delete sru.area;
            }
            if (sru.status === embryo.sar.effort.Status.DraftZone) {
                delete sru.area;
                sru.status = embryo.sar.effort.Status.DraftSRU;
            }
            if (!sru._id) {
                sru.sarId = $scope.sarId;
                sru._id = "sarEf-" + Date.now();
                sru['@type'] = embryo.sar.Type.EffortAllocation;
                sru.status = embryo.sar.effort.Status.DraftSRU;
            }
            var sru2 = $scope.sru;
            // FIXME can not rely on local computer time
            sru.modified = Date.now();
            $scope.sru = null;
            LivePouch.put(sru).then(function (result) {
                $scope.toSrus();
            }).catch(function (error) {
                console.log(error)
                $scope.sru = sru2;
                $scope.alertMessages = ["internal error", error];
            });

        }
    }]);

});
