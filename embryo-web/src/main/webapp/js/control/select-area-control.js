$(function() {
	
	var module = angular.module('embryo.areaselect.control', [ 'embryo.selectarea.service' ]);

	var selectionLayer = new SelectAreaLayer();
	addLayerToMap("area", selectionLayer, embryo.map);
	
//	var projectionFrontend = new OpenLayers.Projection("EPSG:4326");
//	var projectionBackend = new OpenLayers.Projection("EPSG:900913");
	
	module.controller("SelectAreaController", [
        '$scope',
        'SelectAreaService',
        function($scope, SelectAreaService) {
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
//	                        	console.log("SG from service -> " + selectionGroup.active);
//	                        	console.log("SG from service poly -> " + selectionGroup.squares);
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
//            	console.log("inside method getSelectionGroups =D");
            	return $scope.selectionGroups;
            };
            
            $scope.printSelectionGroupsToConsole = function() {
            	
	        	for(key in $scope.selectionGroups) {
	                
	            	var selectionGroup = $scope.selectionGroups[key];
	            	console.log("group -> " + selectionGroup.name + " has edit mode -> " + selectionGroup.editMode + ", active -> " + selectionGroup.active);
	            	console.log("gruppen har -> " + selectionGroup.squares.length + " firkanter");
	            	console.log("gruppens firkanter i JSON -> " + JSON.stringify(selectionGroup.squares));
	        	}
	        	
	        	selectionLayer.printFeaturesToConsole();
            };
        	
            $scope.createGroup = function(){
            	
            	var newSelectionGroup = SelectAreaService.addSelectionGroup();
            	$scope.editSelectionGroup(newSelectionGroup);
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
            
            $scope.selectionGroupClear = function(selectionGroup) {
            	selectionLayer.clearFeatures();
            	selectionGroup.squares = [];
            };
            
            $scope.selectionGroupDelete = function(selectionGroup) {
            	selectionLayer.clearFeatures();
            	
            	for (var i = 0; i < $scope.selectionGroups.length; i++) {
        		   if ($scope.selectionGroups[i].name === selectionGroup.name) {
        		      $scope.selectionGroups.splice(i,1);
        		      break;
        		   }
            	}
            };
            
            $scope.editSelectionGroup = function(selectionGroup) {
//            	console.log("inside editSelectionGroup...");
//            	console.log("clicked edit on -> " + selectionGroup.name);
            	selectionGroup.editMode = true;
            	selectionLayer.activateModify();
            	
            	for(key in $scope.selectionGroups) {
            		currentGroup = $scope.selectionGroups[key];
            		if(currentGroup.name != selectionGroup.name) {
            			currentGroup.editMode = false;
            		}
            	}
            	
            	$scope.printSelectionGroupsToConsole();

            	selectionLayer.draw(selectionGroup);
            };
            
            var isSelectionGroupUnique = function() {
//            	console.log("inside isSelectionGroupUnique...");
            	
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
            /*
            var mapPolygonsToSelectionSquares = function(selectionGroup) {
            	
            	// Get the updated polygons from the layer
            	var polygons = selectionLayer.getPolygonsBySelectionGroup();
            	for(key in polygons) {
            		
            		var square = polygons[key];

            		var squareBounds = square.geometry.getBounds();
        			
        			console.log("mapping of all -> " + JSON.stringify(selectionGroup.squares));
        			console.log("mapping of LEFT -> " + selectionGroup.squares.left);
        			console.log("mapping of RIGHT -> " + selectionGroup.squares.right);
        			console.log("mapping of BOTTOM -> " + selectionGroup.squares.bottom);
        			console.log("mapping of TOP -> " + selectionGroup.squares.top);
        			
        			var boundsTransformed = squareBounds.transform(projectionFrontend, projectionBackend);
        	        console.log("transformation of all -> " + JSON.stringify(boundsTransformed));
        	        
        	        selectionGroup.squares = boundsTransformed;
            	}
            	
            	//$scope.printSelectionGroupsToConsole();
            	
            };*/
            
            $scope.selectionGroupDone = function(selectionGroup) {
//            	console.log("inside saveSelectionGroup...");
//            	console.log("name -> " + selectionGroup.name);
            	$scope.alertMessages = [];
            	
            	if(isSelectionGroupUnique()) {
            	
            		//mapPolygonsToSelectionSquares(selectionGroup);
            		
            		selectionGroup.squares = selectionLayer.getSquareBounds();
            		selectionLayer.deactivateModify();
            		selectionGroup.editMode = false;
            		selectionLayer.clearFeatures();
            	} else {
            		
            		$scope.alertMessages = ["The group name chosen is not unique."];
            	}
            	
            };
            
            $scope.updateSelectionGroups = function() {
            	
//            	console.log("inside CONTROL updateSelectionGroups...");
            	
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
//            	console.log("json for service - > " + JSON.stringify(selectionGroupsForService));
            	SelectAreaService.updateSelectionGroups(
            			selectionGroupsForService, 
            			function() {}, 
            			function(error) {
            				$scope.alertMessages.push(error);
            			});
            };
          
        } ]);
});