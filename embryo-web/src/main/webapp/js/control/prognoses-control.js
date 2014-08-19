$(function(){
    
    var icePrognosesLayer = new IcePrognosesLayer();
    addLayerToMap("ice", icePrognosesLayer, embryo.map);

    var wavePrognosesLayer = new WavePrognosesLayer();
    addLayerToMap("wave", wavePrognosesLayer, embryo.map);

    var module = angular.module('embryo.prognoses.control', [ 'embryo.metoc', 'ui.bootstrap.accordion', 'embryo.control', 'embryo.weather.service' ]);

    module.controller("PrognosesController", [ '$scope', function($scope) {
        $scope.selected = {};

        $scope.$on("$destroy", function() {
            embryo.controllers.settings.close();
        });
    } ]);

    module.controller('WavePrognosisCtrl', ['$scope', 'WeatherService', function($scope, WeatherService) {

        $scope.reloadMap = function(wipe) {
            if($scope.$parent.prognosisSelected) {
                wavePrognosesLayer.draw($scope.data, $scope.current);
            } else if(wipe === true) {
                wavePrognosesLayer.clear();
            }
        };

        $scope.getPrognosis = function(p, $event) {
            $event.preventDefault();
            if(p.name == $scope.$parent.prognosisSelected) {
                $scope.$parent.prognosisSelected = null;
                $scope.reloadMap(true);
                return;
            }
            WeatherService.getWavePrognosis(p.name, function(prognosis) {
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

                for(var i = 0; i < $scope.wavePrognoses.length; i++) {
                    $scope.wavePrognoses[i].selected = false;
                }
                $scope.$parent.prognosisSelected = p.name;
                
                $scope.reloadMap();
            }, function(error, status) {
                $scope.errorMsg = error;
            });
        };

    }]);
    
    module.controller('WavePrognosesCtrl', ['$scope', 'WeatherService', function($scope, WeatherService) {
        
        WeatherService.listWavePrognoses(function(prognoses) {
            $scope.errorMsg = null;
            $scope.wavePrognoses = [];
            for(var i = 0; i < prognoses.length; i++) {
                $scope.wavePrognoses.push({
                    name : prognoses[i],
                    selected : false
                });
            }
        }, function(error, status) {
           $scope.errorMsg = error;
        });
    }]);

    module.controller('IcePrognosisCtrl', ['$scope', 'IceService', function($scope, IceService) {
        
        $scope.selectedVariable = 'iceConcentration';
        
        $scope.reloadMap = function(wipe) {
            if($scope.$parent.prognosisSelected) {
                icePrognosesLayer.draw($scope.data, $scope.current, $scope.selectedVariable);
            } else if(wipe === true) {
                icePrognosesLayer.clear();
            }
        };
        
        $scope.$watch('selectedVariable', $scope.reloadMap);
        
        $scope.getPrognosis = function(p, $event) {
            $event.preventDefault();
            if(p.name == $scope.$parent.prognosisSelected) {
                $scope.$parent.prognosisSelected = null;
                $scope.reloadMap(true);
                return;
            }
            IceService.getIcePrognosis(p.name, function(prognosis) {
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
                
                for(var i = 0; i < $scope.icePrognoses.length; i++) {
                    $scope.icePrognoses[i].selected = false;
                }
                $scope.$parent.prognosisSelected = p.name;
            }, function(error, status) {
                $scope.errorMsg = error;
            });
            
        };
     }]);

    module.controller('IcePrognosesCtrl', ['$scope', 'IceService', function($scope, IceService) {
         
        IceService.listIcePrognoses(function(prognoses) {
            $scope.errorMsg = null;
            $scope.icePrognoses = [];
            for(var i = 0; i < prognoses.length; i++) {
                $scope.icePrognoses.push({
                    name : prognoses[i],
                    selected : false
                });
            }
        }, function(error, status) {
           $scope.errorMsg = error;
        });
   }]);

});