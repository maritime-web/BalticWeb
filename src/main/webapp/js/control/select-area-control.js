$(function() {

    var module = angular.module('embryo.areaselect.control', [ 'embryo.selectarea.service' , 'embryo.subscription.service']);

    var selectionLayer;
    embryo.postLayerInitialization(function(){
        selectionLayer = new SelectAreaLayer();
        addLayerToMap("area", selectionLayer, embryo.map);
    })

//	var projectionFrontend = new OpenLayers.Projection("EPSG:4326");
//	var projectionBackend = new OpenLayers.Projection("EPSG:900913");
	
	module.controller("SelectAreaController", [
        '$scope',
        'SelectAreaService',
        'SubscriptionService',
        function ($scope, SelectAreaService, SubscriptionService) {
            this.scope = $scope;
            $scope.selectionGroups = [];
            $scope.alertMessages = [];
            
            
            var getSelectionGroupsFromService = function() {
	            SelectAreaService.getSelectionGroups(
	            		// Callback success <- called by SelectAreaService
	            		function(selectionGroupsFromService) {
	            			$scope.errorMsg = null;
	            			$scope.selectionGroups = [];
	            			$scope.selectionGroups = selectionGroupsFromService;
	            			
	            			for(key in $scope.selectionGroups) {
	                        	var selectionGroup = $scope.selectionGroups[key];
	                            selectionGroup.editMode = false;
	                        }
	            		}, 
	            		// Callback error <- called by SelectAreaService
	            		function(error, status) {
	            			$scope.errorMsg = error;
	            		}
	            );
            };
            
            getSelectionGroupsFromService();
            
            $scope.getSelectionGroups = function() {
            	return $scope.selectionGroups;
            };

            $scope.createGroup = function(){
            	var newSelectionGroup = SelectAreaService.addSelectionGroup();
            	$scope.editSelectionGroup(null, newSelectionGroup);
            };
            
            $scope.activate = function(){
            	selectionLayer.activateModify();
            };
            
            $scope.deactivate = function(){
            	selectionLayer.deactivateModify();
            };
            
            $scope.alreadyInEditMode = function(){
            	for(key in $scope.selectionGroups) {
            		if($scope.selectionGroups[key].editMode == true) {
            			return true;
            		}
            	}
            };
            
            $scope.printToConsole = function() {
            	selectionLayer.printFeaturesToConsole();
            };
            
            $scope.selectionGroupClear = function($event, selectionGroup) {
            	$event.preventDefault();
            	selectionLayer.clearFeatures();
            	selectionGroup.squares = [];
            	
            };
            
            $scope.selectionGroupDelete = function($event, selectionGroup) {
            	$event.preventDefault();
            	selectionLayer.clearFeatures();
                selectionLayer.deactivateModify();


                for (var i = 0; i < $scope.selectionGroups.length; i++) {
        		   if ($scope.selectionGroups[i].name === selectionGroup.name) {
        		      $scope.selectionGroups.splice(i,1);
        		      break;
        		   }
            	}
            };
            
            $scope.editSelectionGroup = function($event, selectionGroup) {
            	if($event) {
            		$event.preventDefault();
            	}
            	selectionGroup.editMode = true;
            	selectionLayer.activateModify();
            	
            	for(key in $scope.selectionGroups) {
            		currentGroup = $scope.selectionGroups[key];
            		if(currentGroup.name != selectionGroup.name) {
            			currentGroup.editMode = false;
            		}
            	}
            	
            	selectionLayer.draw(selectionGroup);
            };
            
            var isSelectionGroupUnique = function() {
            	for (var i = 0; i < $scope.selectionGroups.length; i++) {
                    for (var j = 0; j < $scope.selectionGroups.length; j++) {
                        if (i != j) {
                            if ($scope.selectionGroups[i].name == $scope.selectionGroups[j].name) {
                                return false;
                            }
                        }
                    }
                }
                return true; 
            };
                       
            $scope.selectionGroupDone = function($event, selectionGroup) {
            	$event.preventDefault();
            	$scope.alertMessages = [];
            	if(isSelectionGroupUnique()) {
            	
            		//mapPolygonsToSelectionSquares(selectionGroup);
            		
            		selectionGroup.squares = selectionLayer.getSquareBounds();
            		selectionLayer.deactivateModify();
            		selectionGroup.editMode = false;
            		selectionLayer.clearFeatures();
            	} else {
            		$scope.alertMessages = ["The chosen area name is not unique."];
            	}
            };
            
            $scope.updateSelectionGroups = function() {
                var selectionGroupsForService = [];
            	
            	for(key in $scope.selectionGroups) {
            		var selectionGroup = $scope.selectionGroups[key];
            		
            		var SelectionGroupForService = {
        				id 				: 1,
        				name 			: selectionGroup.name,
        				active 			: selectionGroup.active,
        				polygonsAsJson	: JSON.stringify(selectionGroup.squares)
            		};
            		
            		selectionGroupsForService.push(SelectionGroupForService);
            	}
            	SelectAreaService.updateSelectionGroups(
            			selectionGroupsForService,
                    function () {
                        //TODO: only update if modified
                        SubscriptionService.update({name: 'VesselService.list'});
                    },
            			function(error) {
            				$scope.alertMessages.push(error);
            			});
            };

            $scope.$on("$destroy", function () {
                selectionLayer.clearFeatures();
                selectionLayer.deactivateModify();
                selectionLayer.activateSelectable();
            });
          
        } ]);
});