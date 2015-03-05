$(function () {

//    var msiLayer = new MsiLayer();
//    addLayerToMap("msi", msiLayer, embryo.map);

    var module = angular.module('embryo.sar.views', ["firebase", 'embryo.sar.service', 'embryo.common.service']);

    function SarTypeData(id, text, img) {
        this.id = id;
        this.text = text;
        this.img = img;
    }

    var sarTypes = embryo.sar.types;
    var sarTypeDatas = [];
    sarTypeDatas.push(new SarTypeData(sarTypes.RapidResponse, "Rapid response", "/img/sar/generic.png"));
    sarTypeDatas.push(new SarTypeData(sarTypes.DatumPoint, "Datum point", "/img/sar/datumpoint.png"));
    sarTypeDatas.push(new SarTypeData(sarTypes.DatumLine, "Datum line", "/img/sar/datumline.png"));
    sarTypeDatas.push(new SarTypeData(sarTypes.BackTrack, "Back track", "/img/sar/generic.png"));

    module.controller("SAROperationEditController", ['$scope', 'ViewService', 'SarService', function ($scope, ViewService, SarService) {
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

        $scope.sarTypes = sarTypes;
        $scope.sarTypeDatas = sarTypeDatas;
        $scope.sar = {
            selectedType: $scope.sarTypeDatas[0]
        }

        $scope.back = function () {
            if ($scope.page === 'sar') {
                $scope.page = 'sarInputs';
            } else if ($scope.page === 'sarInputs') {
                $scope.page = 'typeSelection';
            }
        }

        $scope.next = function () {
            if ($scope.page === 'typeSelection') {
                $scope.page = 'sarInputs';
            }
            else if ($scope.page === 'sarInputs') {
                //calculate
                $scope.page = 'sar';
            }
        }


    }]);


});
