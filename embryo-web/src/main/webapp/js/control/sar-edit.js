$(function () {

//    var msiLayer = new MsiLayer();
//    addLayerToMap("msi", msiLayer, embryo.map);

    var module = angular.module('embryo.sar.views', ['embryo.sar.service', 'embryo.common.service', 'ui.bootstrap.typeahead', 'embryo.validation.compare', 'embryo.datepicker']);


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

    //var sarTypes = embryo.sar.types;
    var sarTypeDatas = {}
    sarTypeDatas[embryo.sar.types.RapidResponse] = new SarTypeData("Rapid response", "/img/sar/generic.png");
    sarTypeDatas[embryo.sar.types.DatumPoint] = new SarTypeData("Datum point", "/img/sar/datumpoint.png");
    sarTypeDatas[embryo.sar.types.DatumLine] = new SarTypeData("Datum line", "/img/sar/datumline.png");
    sarTypeDatas[embryo.sar.types.BackTrack] = new SarTypeData("Back track", "/img/sar/generic.png")

    module.controller("SAROperationEditController", ['$scope', 'ViewService', 'SarService', '$q', 'LivePouch',
        function ($scope, ViewService, SarService, $q, LivePouch) {

            $scope.alertMessages = [];

            function initNewSar() {
                var now = Date.now();
                $scope.sar = {
                    type: embryo.sar.types.RapidResponse,
                    no: "AW-" + Date.now(),
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

                if ($scope.sar.type != embryo.sar.types.DatumLine) {
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
            type: "new",
            show: function (context) {
                $scope.page = context && context.page ? context.page : 'typeSelection'
                if (context.sarId) {
                    LivePouch.get(context.sarId).then(function (sarOperation) {
                        $scope.sarOperation = sarOperation;
                        $scope.sar = sarOperation.input;
                        $scope.$apply(function () {
                        });
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
            $scope.sarTypes = embryo.sar.types;
            $scope.sarTypeValues = [embryo.sar.types.RapidResponse, embryo.sar.types.DatumPoint, embryo.sar.types.DatumLine, embryo.sar.types.BackTrack];
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
                $scope.sarOperation.input = calculatedOperation.input;
                $scope.sarOperation.output = calculatedOperation.output;
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
    }]);

    function SelectionType(value, text) {
        this.value = value;
        this.text = text;
    }


    module.controller("SarEffortAllocationController", ['$scope', 'ViewService', 'SarService', 'LivePouch',
        function ($scope, ViewService, LivePouch) {
            $scope.alertMessages = [];

            $scope.fatigues = [0.5, 1.0];

            $scope.targetTypes = [new SelectionType(embryo.sar.effort.TargetTypes.PersonInWater, "Person in Water (PIW)"),
                new SelectionType(embryo.sar.effort.TargetTypes.Raft1Person, "Raft 1 person"),
                new SelectionType(embryo.sar.effort.TargetTypes.Raft4Persons, "Raft 4 persons"),
                new SelectionType(embryo.sar.effort.TargetTypes.Raft6Persons, "Raft 6 persons"),
                new SelectionType(embryo.sar.effort.TargetTypes.Raft8Persons, "Raft 8 persons"),
                new SelectionType(embryo.sar.effort.TargetTypes.Raft10Persons, "Raft 10 persons"),
                new SelectionType(embryo.sar.effort.TargetTypes.Raft15Persons, "Raft 15 persons"),
                new SelectionType(embryo.sar.effort.TargetTypes.Raft20Persons, "Raft 20 persons"),
                new SelectionType(embryo.sar.effort.TargetTypes.Raft25Persons, "Raft 25 persons"),
                new SelectionType(embryo.sar.effort.TargetTypes.Motorboat15, "Motorboat <= 15 feet"),
                new SelectionType(embryo.sar.effort.TargetTypes.Motorboat20, "Motorboat 20 feet"),
                new SelectionType(embryo.sar.effort.TargetTypes.Motorboat33, "Motorboat 33 feet"),
                new SelectionType(embryo.sar.effort.TargetTypes.Motorboat53, "Motorboat 53 feet"),
                new SelectionType(embryo.sar.effort.TargetTypes.Motorboat78, "Motorboat 78 feet"),
                new SelectionType(embryo.sar.effort.TargetTypes.Sailboat15, "Sailboat 15 feet"),
                new SelectionType(embryo.sar.effort.TargetTypes.Sailboat20, "Sailboat 20 feet"),
                new SelectionType(embryo.sar.effort.TargetTypes.Sailboat25, "Sailboat 25 feet"),
                new SelectionType(embryo.sar.effort.TargetTypes.Sailboat30, "Sailboat 30 feet"),
                new SelectionType(embryo.sar.effort.TargetTypes.Sailboat40, "Sailboat 40 feet"),
                new SelectionType(embryo.sar.effort.TargetTypes.Sailboat50, "Sailboat 50 feet"),
                new SelectionType(embryo.sar.effort.TargetTypes.Sailboat70, "Sailboat 70 feet"),
                new SelectionType(embryo.sar.effort.TargetTypes.Sailboat83, "Sailboat 83 feet"),
                new SelectionType(embryo.sar.effort.TargetTypes.Ship120, "Ship 120 feet"),
                new SelectionType(embryo.sar.effort.TargetTypes.Ship225, "Ship 225 feet"),
                new SelectionType(embryo.sar.effort.TargetTypes.Ship330, "Ship >= 300 feet")
            ]

            $scope.vesselTypes = [
                new SelectionType(embryo.sar.effort.VesselTypes.SmallerVessel, "Small vessel (40 feet)"),
                new SelectionType(embryo.sar.effort.VesselTypes.Ship, "Ship (50 feet)")
            ]

            $scope.visibilityValues = [1, 3, 5, 10, 15, 20];


            $scope.provider = {
                doShow: false,
                title: "SarEffortAllocation",
                type: "effort",
                show: function (context) {
                    $scope.page = 'edit';
                    if (context && context.sarId) {

                    }
                    $scope.effort = {
                        fatigue: 1.0,
                        vesType: embryo.sar.effort.VesselTypes.SmallerVessel,
                        target: embryo.sar.effort.TargetTypes.PersonInWater,
                        time: 1,
                        visibility: 1,
                        pod: 78
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

            $scope.calculate = function () {

            }


        }]);

});
