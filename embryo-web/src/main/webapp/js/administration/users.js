$(function() {
    "use strict";

    angular.module('embryo.administration.users', [ 'embryo.userService', 'ui.bootstrap.modal',
            'ui.bootstrap.tpls', 'embryo.authentication' ]);

    embryo.UsersCtrl = function($scope, UserService, $modal) {
        var editUser;
        var userList = [];
        $scope.users = userList;
        $scope.message = null; 
        $scope.alertMessages = null;

        function loadUsers() {
            UserService.userList(function(users) {
                userList = users;
                $scope.users = users;
            }, function(error) {
                $scope.alertMessages = error;
            });
        }

        function loadSourceFilters() {
            UserService.sourceFilters(function (filters) {
                var sourceFilters = [];
                for (var index in filters) {
                    sourceFilters.push({
                        name: filters[index],
                        value: filters[index]
                    });
                }
                $scope.sourceFilters = filters;
            }, function (error) {
                $scope.alertMessages = error;
            });
        }


        loadSourceFilters();
        loadUsers();

        $scope.roleText = function(logicalName) {
            if (logicalName == "Reporting") {
                return "Reporting Authority";
            }
            return logicalName;
        };

        function match(propertyValue, searchStr) {
            if (!propertyValue) {
                return false;
            }
            var value = ("" + propertyValue).toLowerCase();
            return ((value.indexOf(searchStr) == 0) || (value.indexOf(" " + searchStr) >= 0));
        }

        $scope.search = function() {
            if ($scope.searchString == null || $scope.searchString == "") {
                $scope.users = userList;
                return;
            }

            var users = [];
            var searchStr = $scope.searchString.toLowerCase();

            for ( var index in userList) {
                var user = userList[index];
                if (match(user.login, searchStr) || match(user.shipMmsi, searchStr) || match(user.email, searchStr)) {
                    users.push(user);
                }
            }
            $scope.users = users;
        };

        $scope.edit = function($event, user) {
            $event.preventDefault();
            
            editUser = user;
            $scope.message = null;
            $scope.alertMessages = null;
            $scope.editUser = {
            	login 			: user.login,
                email 			: user.email,
                role 			: user.role,
                shipMmsi 		: user.shipMmsi,
                aisFilterName: user.aisFilterName
            };
            $scope.action = "Edit";
            $("#cLogin").focus();
        };

        $scope.create = function() {
            $scope.message = null;
            $scope.alertMessages = null;
            $scope.editUser = {};
            $scope.action = "Create";
            $("#cLogin").focus();
        };

        $scope.submitCreate = function() {
            $scope.message = "Saving " + $scope.editUser.login + " ...";
            $scope.alertMessages = null;
            
            UserService.create($scope.editUser, function() {
                $scope.message = "User " + $scope.editUser.login + " created.";
                $scope.action = "Edit";
                loadUsers();
            }, function(error) {
                $scope.message = null;
                $scope.alertMessages = error;
            });
        };

        function showModal(title, messages) {
            return $modal.open({
                controller : embryo.ConfirmModalCtrl,
                templateUrl : "confirmDialog.html",
                resolve : {
                    title : function() {
                        return title;
                    },
                    messages : function() {
                        return messages;
                    }
                }
            });
        }

        $scope.submitEdit = function() {
            function save() {
                $scope.message = "Saving " + $scope.editUser.login + " ...";
                $scope.alertMessages = null;
                UserService.edit($scope.editUser, function() {
                    $scope.message = "User " + $scope.editUser.login + " saved.";
                    $scope.action = null;
                    loadUsers();
                }, function(error) {
                    $scope.message = null;
                    $scope.alertMessages = error;
                });
            }
            var warnings = [];
            if (editUser.role != $scope.editUser.role) {
                var msg = "You are about to change the role from " + editUser.role + " to " + $scope.editUser.role
                        + ". ";
                if (editUser.role == 'Sailor') {
                    msg += "All information related to vessel with MMSI " + editUser.shipMmsi + " will be deleted.";
                }
                warnings.push(msg);
            }
            if (editUser.role == 'Sailor' && $scope.editUser.role == 'Sailor'
                    && editUser.shipMmsi != $scope.editUser.shipMmsi) {
                warnings.push("You are about to change the MMSI from " + editUser.shipMmsi + " to "
                        + $scope.editUser.shipMmsi);
            }
            if (warnings.length > 0) {
                warnings.push("Please confirm changes");
                showModal("Save Modified User", warnings).result.then(save);
            } else {
                save();
            }
        };

        $scope.del = function($event, user) {
            $event.preventDefault();

            var messages = [ "This will delete user " + user.login + (user.shipMmsi ? " / " + user.shipMmsi : "") ];
            showModal("Delete User", messages).result.then(function() {
                $scope.message = "Deleting " + user.login + " ...";
                UserService.deleteUser(user.login, function() {
                    $scope.message = "User " + user.login + " deleted.";
                    loadUsers();
                }, function(error) {
                    $scope.message = null;
                    $scope.alertMessages = error;
                });
            });
        };
    };

    embryo.ConfirmModalCtrl = function($scope, $modalInstance, title, messages) {
        $scope.title = title;
        $scope.messages = messages;
    };

    function fixScrollables() {
        $(".scrollable").each(function(elem) {
            var rect = this.getBoundingClientRect();
            $(this).css("overflow", "auto");
            $(this).css("max-height", ($(window).height() - rect.top - 20) + "px");
        });
    }
    $(window).resize(fixScrollables);
    setTimeout(fixScrollables, 100);
}());
