(function () {
	
    var module = angular.module('embryo.selectarea.service', []);
    
    var selectionGroupPath = 'rest/selectiongroup/';
    
    module.service('SelectAreaService', ['$http', function ($http) {
        	
        	// Structure definition
            function SelectionGroup (name) {
            	this.name = name;
            	this.active = false;
            	this.squares = [];
            	this.getStatusLabel = function() {
            		return this.active ? "Active" : "Inactive";
            	};
            };
            
            var selectionGroups = [];
            
            // Service implementation
            var service = {
            		getSelectionGroups : function(success, error) {
            			var messageId = embryo.messagePanel.show({text : "Retrieving selection areas..."});
            			
            			$http.get(embryo.baseUrl + selectionGroupPath + 'list', {
    						
							timeout : embryo.defaultTimeout
						}).success(function(selectionGroupsFromService) {
							 
							embryo.messagePanel.replace(messageId, {text : "Selection areas retrieved.", type : "success"});
							
							selectionGroups = [];
							for(key in selectionGroupsFromService) {
								var selectionGroupFromService = selectionGroupsFromService[key];
								
								var selectionGroup = new SelectionGroup(selectionGroupFromService.name);

								if(selectionGroupFromService.polygonsAsJson) {
									selectionGroup.squares = JSON.parse(selectionGroupFromService.polygonsAsJson);
								}
								
								selectionGroup.active = selectionGroupFromService.active;

								selectionGroups.push(selectionGroup);
							}
							
							success(selectionGroups);
						}).error(function(data, status, headers, config) {
						    
							var errorMsg = embryo.ErrorService.errorStatus(data, status, "requesting selection areas.");
						    embryo.messagePanel.replace(messageId, {text : errorMsg, type : "error"});
						    
						    error(errorMsg, status);
						});
            		},
            		
            		updateSelectionGroups : function(selectionGroups, callback, error) {
//            			console.log("inside SERVICE updateSelectionGroups...");
            			
                        $http.post(embryo.baseUrl + selectionGroupPath + 'update', selectionGroups)
                        	.success(function(success) {
                        		embryo.messagePanel.show({text : "Selection areas updated."});
                        	})
                        	.error(function(data, status, headers, config) {
                        		error(embryo.ErrorService.extractError(data, status, config));
	                        }
	                   );
                    },
                    
            		addSelectionGroup : function() {
            			var newSelectionGroup = new SelectionGroup("New Group");
            			selectionGroups.push(newSelectionGroup);
            			return newSelectionGroup;
            		}
            };
            
            return service;
            
        } ]);
})();

