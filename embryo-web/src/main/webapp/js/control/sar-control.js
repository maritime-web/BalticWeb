$(function () {

//    var msiLayer = new MsiLayer();
//    addLayerToMap("msi", msiLayer, embryo.map);

    var module = angular.module('embryo.sar.controllers', ["firebase", 'embryo.sar.service', 'embryo.common.service']);

    module.controller("SARLayerControl", ['$scope', function ($scope) {
        MsiService.subscribe(function (error, warnings) {
            msiLayer.draw(warnings);
        });
    }]);

    module.controller("OperationsControl", ['$scope', '$firebase', 'SarService', 'ViewService', '$log',
        function ($scope, $firebase, SarService, ViewService, $log) {

            var subscription = ViewService.subscribe({
                name: "OperationsControl",
                onNewProvider: function () {

                    var viewProviders = ViewService.viewProviders();
                    $log.debug('onNewProvider, viewProvider=', viewProviders);
                    for (var index in viewProviders) {
                        if (viewProviders[index].type() === 'new') {
                            $scope.newSarProvider = viewProviders[index];
                        }
                    }
                }
            });

            $scope.$on("$destroy", function () {
                ViewService.unsubscribe(subscription);
            })


            $scope.sars = []
            var sarsRef = new Firebase('//incandescent-torch-4183.firebaseio.com/sars2');
            var sync = $firebase(sarsRef);
            $scope.sars = sync.$asArray();

//        var logRef = new Firebase('//incandescent-torch-4183.firebaseio.com/logs2');
//        $scope.logs = $firebase(logRef).$asArray();


            var logRef = new Firebase('//incandescent-torch-4183.firebaseio.com').child("messages");
            //logRef.set({bb:"hep"});
            $scope.logs = $firebase(logRef).$asObject();


            console.log(sarsRef);
            console.log(sync);

            $scope.view = function ($event, sar) {
                $event.preventDefault();
                console.log(sar)
                console.log("sar")
                SarService.selectedSar(sar.name);
            }

            $scope.newSar = function () {
                //var newLogs = {};
                //newLogs[$scope.newName] = "";
                //$scope.sars.$add({"name": $scope.newName, active: false});

                //console.log(newLogs);

                //logRef.update(newLogs);

                $scope.newSarProvider.show({});

//            console.log($scope.logSync);
//            $scope.logSync.child;

                $scope.newName = null;
            }

        }]);

    module.controller("LogControl", ['$scope', 'Subject', '$firebase', 'SarService', function ($scope, Subject, $firebase, SarService) {
        console.log(SarService);
        SarService.registerSelectedSarListener("LogCtrl", function (selectedSarId) {
            $scope.selectedSarId = selectedSarId;
            if (!$scope.selectedSarId) {
                $scope.logs = null;
                return;
            }
            var logRef = new Firebase('//incandescent-torch-4183.firebaseio.com/messages/' + selectedSarId);
            $scope.logs = $firebase(logRef).$asArray();
        });
        $scope.send = function () {
            if ($scope.msg) {
                $scope.logs.$add({user: Subject.getDetails().userName, stmt: $scope.msg});
                $scope.msg = null;
            }
        }


    }]);


});
