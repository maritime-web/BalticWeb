$(function() {

    var prognosesLayer = new PrognosesLayer();
    addLayerToMap("prognoses", prognosesLayer, embryo.map);

    var module = angular.module('embryo.prognoses.control', [ 'embryo.metoc', 'ui.bootstrap.accordion', 'embryo.control', 'embryo.prognoses.service' ]);

    module.controller("PrognosesController", [ '$scope', function($scope) {
        $scope.selected = {};

        $scope.prognosisSelected = '';

        $scope.$on("$destroy", function() {
            embryo.controllers.settings.close();
        });
    } ]);

    module.controller('WavePrognosisCtrl', [ '$scope', 'PrognosesService', function($scope, PrognosesService) {

        $scope.reloadMap = function(wipe) {
            if ($scope.pc.prognosisSelected) {
                prognosesLayer.drawWavePrognosis($scope.data, $scope.current);
            } else if (wipe === true) {
                prognosesLayer.clear();
            }
        };

        $scope.getPrognosis = function(p, $event) {
            $event.preventDefault();
            if (p.name == $scope.pc.prognosisSelected) {
                $scope.pc.prognosisSelected = '';
                $scope.reloadMap(true);
                return;
            }
            PrognosesService.getWavePrognosis(p.name, function(prognosis) {
                $scope.errorMsg = null;
                $scope.data = prognosis;
                var time = prognosis.metadata.time;
                $scope.start = 0;
                $scope.end = time.length - 1;
                $scope.current = $scope.start;

                $scope.updateCurrentDate = function() {
                    var t = time[$scope.current];
                    $scope.currentDate = formatTime(t);
                    $scope.reloadMap();
                };
                $scope.$watch('current', $scope.updateCurrentDate);
                $scope.updateCurrentDate();

                for (var i = 0; i < $scope.wavePrognoses.length; i++) {
                    $scope.wavePrognoses[i].selected = false;
                }
                $scope.pc.prognosisSelected = p.name;

                $scope.reloadMap();
            }, function(error, status) {
                $scope.errorMsg = error;
            });
        };

    } ]);

    module.controller('WavePrognosesCtrl', [ '$scope', 'PrognosesService', function($scope, PrognosesService) {

        PrognosesService.listWavePrognoses(function(prognoses) {
            $scope.errorMsg = null;
            $scope.wavePrognoses = [];
            for (var i = 0; i < prognoses.length; i++) {
                $scope.wavePrognoses.push({
                    name : prognoses[i],
                    selected : false
                });
            }
        }, function(error, status) {
            $scope.errorMsg = error;
        });
    } ]);
    
    module.controller('CurrentPrognosisCtrl', [ '$scope', 'PrognosesService', function($scope, PrognosesService) {
        
        $scope.reloadMap = function(wipe) {
            if ($scope.pc.prognosisSelected) {
                prognosesLayer.drawCurrentPrognosis($scope.data, $scope.current);
            } else if (wipe === true) {
                prognosesLayer.clear();
            }
        };

        $scope.getPrognosis = function(p, $event) {
            $event.preventDefault();
            if (p.name == $scope.pc.prognosisSelected) {
                $scope.pc.prognosisSelected = '';
                $scope.reloadMap(true);
                return;
            }
            PrognosesService.getCurrentPrognosis(p.name, function(prognosis) {
                $scope.errorMsg = null;
                $scope.data = prognosis;
                var time = prognosis.metadata.time;
                $scope.start = 0;
                $scope.end = time.length - 1;
                $scope.current = $scope.start;

                $scope.updateCurrentDate = function() {
                    var t = time[$scope.current];
                    $scope.currentDate = formatTime(t);
                    $scope.reloadMap();
                };
                $scope.$watch('current', $scope.updateCurrentDate);
                $scope.updateCurrentDate();

                for (var i = 0; i < $scope.currentPrognoses.length; i++) {
                    $scope.currentPrognoses[i].selected = false;
                }
                $scope.pc.prognosisSelected = p.name;

                $scope.reloadMap();
            }, function(error, status) {
                $scope.errorMsg = error;
            });
        };    }]);
    
    module.controller('CurrentPrognosesCtrl', [ '$scope', 'PrognosesService', function($scope, PrognosesService) {
        PrognosesService.listCurrentPrognoses(function(prognoses) {
            $scope.errorMsg = null;
            $scope.currentPrognoses = [];
            for (var i = 0; i < prognoses.length; i++) {
                $scope.currentPrognoses.push({
                    name : prognoses[i],
                    selected : false
                });
            }
        }, function(error, status) {
            $scope.errorMsg = error;
        });
    }]);

    module.controller('IcePrognosisCtrl', [ '$scope', 'PrognosesService', function($scope, PrognosesService) {

        $scope.reloadMap = function(wipe) {
            if ($scope.pc.prognosisSelected) {
                prognosesLayer.drawIcePrognosis($scope.data, $scope.current, $scope.selectedVariable);
            } else if (wipe === true) {
                prognosesLayer.clear();
            }
        };

        $scope.$watch('selectedVariable', $scope.reloadMap);

        $scope.getPrognosis = function(p, $event) {
            $event.preventDefault();
            if (p.name == $scope.pc.prognosisSelected) {
                $scope.pc.prognosisSelected = '';
                $scope.reloadMap(true);
                return;
            }
            PrognosesService.getIcePrognosis(p.name, function(prognosis) {
                $scope.errorMsg = null;
                $scope.data = prognosis;
                var time = prognosis.metadata.time;
                $scope.start = 0;
                $scope.end = time.length - 1;
                $scope.current = $scope.start;
                $scope.selectedVariable = 'iceConcentration';

                $scope.updateCurrentDate = function() {
                    var t = time[$scope.current];
                    $scope.currentDate = formatTime(t);
                    $scope.reloadMap();
                };
                $scope.$watch('current', $scope.updateCurrentDate);
                $scope.updateCurrentDate();

                for (var i = 0; i < $scope.icePrognoses.length; i++) {
                    $scope.icePrognoses[i].selected = false;
                }
                $scope.pc.prognosisSelected = p.name;
            }, function(error, status) {
                $scope.errorMsg = error;
            });

        };
    } ]);
    
    module.controller('IcePrognosesCtrl', [ '$scope', 'PrognosesService', function($scope, PrognosesService) {

        PrognosesService.listIcePrognoses(function(prognoses) {
            $scope.errorMsg = null;
            $scope.icePrognoses = [];
            for (var i = 0; i < prognoses.length; i++) {
                $scope.icePrognoses.push({
                    name : prognoses[i],
                    selected : false
                });
            }
        }, function(error, status) {
            $scope.errorMsg = error;
        });
    } ]);

});