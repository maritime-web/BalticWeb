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

    function SarTypeData(id, text, img) {
        this.id = id;
        this.text = text;
        this.img = img;
    }

    //var sarTypes = embryo.sar.types;
    var sarTypeDatas = [];
    sarTypeDatas.push(new SarTypeData(embryo.sar.types.RapidResponse, "Rapid response", "/img/sar/generic.png"));
    sarTypeDatas.push(new SarTypeData(embryo.sar.types.DatumPoint, "Datum point", "/img/sar/datumpoint.png"));
    sarTypeDatas.push(new SarTypeData(embryo.sar.types.DatumLine, "Datum line", "/img/sar/datumline.png"));
    sarTypeDatas.push(new SarTypeData(embryo.sar.types.BackTrack, "Back track", "/img/sar/generic.png"));


    module.controller("SAROperationEditController", ['$scope', 'ViewService', 'SarService', '$q', 'LivePouch',
        function ($scope, ViewService, SarService, $q, LivePouch) {
        var now = Date.now();

            $scope.alertMessages = [];

            function initNewSar() {
                $scope.sar = {
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

                if ($scope.tmp.sarTypeData.id != embryo.sar.types.DatumLine) {
                    if (!$scope.sar.lastKnownPosition) {
                        $scope.sar.lastKnownPosition = {};
                }
                    if (!$scope.sar.lastKnownPosition.ts) {
                        $scope.sar.lastKnownPosition.ts = now;
                    }
                } else {


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
        $scope.sarTypes = SarService.sarTypes();
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
            sarInput.type = $scope.tmp.sarTypeData.id;
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


});
