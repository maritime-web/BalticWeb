$(function () {

//    var msiLayer = new MsiLayer();
//    addLayerToMap("msi", msiLayer, embryo.map);

    var module = angular.module('embryo.sar.views', ["firebase", 'embryo.sar.service', 'embryo.common.service']);

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


    module.controller("SAROperationEditController", ['$scope', 'ViewService', 'SarService', function ($scope, ViewService, SarService) {
        var now = Date.now();

        $scope.provider = {
            doShow: false,
            title: "Create SAR",
            type: "new",
            show: function (context) {
                $scope.page = context && context.page ? context.page : 'typeSelection'
                this.doShow = true;
            },
            close: function () {
                this.doShow = false;
            }
        };
        ViewService.addViewProvider($scope.provider);

        $scope.searchObjects = SarService.searchObjectTypes();
        $scope.sarTypes = SarService.sarTypes();
        $scope.sarTypeDatas = sarTypeDatas;

        $scope.selectedType = $scope.sarTypeDatas[0];
        $scope.sar = {
            searchObject: $scope.searchObjects[0],
            sruErr: 0.1,
            safetyFactor: 1.0,
            startTs: now + 1000 * 60 * 60

        }

        if ($scope.selectedType.id != embryo.sar.types.DatumLine) {
            if (!$scope.sar.lastKnownPosition) {
                $scope.sar.lastKnownPosition = {};
            }
            if (!$scope.sar.lastKnownPosition.ts) {
                $scope.sar.lastKnownPosition.ts = now;
            }
        } else {


        }

        if (!$scope.sar.surfaceDriftPoints) {
            $scope.sar.surfaceDriftPoints = [{}];
        }
        if (!$scope.sar.surfaceDriftPoints[0].ts) {
            $scope.sar.surfaceDriftPoints[0].ts = now;
        }

        $scope.back = function () {
            switch ($scope.page) {
                case ("sarResult") :
                {
                    $scope.page = 'sarInputs';
                    break;
                }
                case ("sarInputs") :
                {
                    $scope.page = 'typeSelection';
                    break;
                }
            }
        }

        $scope.next = function () {
            $scope.page = 'sarInputs';
        }

        function clone(object) {
            return JSON.parse(JSON.stringify(object));
        }

        $scope.createSarOperation = function () {
            //var sar = clone($scope.sar);
            var sar = $scope.sar;
            sar.type = $scope.selectedType.id;
            $scope.sarOperation = SarService.createSarOperation(sar);
            $scope.page = 'sarResult';
        }

        $scope.formatTs = formatTime;
        $scope.formatPos = function (position) {
            return "(" + formatLatitude(position.lat) + ", " + formatLongitude(position.lon) + ")";
        }

        $scope.addPoint = function () {
            $scope.sar.surfaceDriftPoints.push({});
        }

        $scope.removePoint = function () {
            if ($scope.sar.surfaceDriftPoints.length > 1) {
                $scope.sar.surfaceDriftPoints.splice($scope.sar.surfaceDriftPoints.length - 1, 1);
            }
        }

    }]);


});
