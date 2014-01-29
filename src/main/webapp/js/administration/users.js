$(function() {
    "use strict";

    var scheduleModule = angular.module('embryo.users', [ 'embryo.userService', 'ui.bootstrap.modal',
            'ui.bootstrap.tpls' ]);

    embryo.UsersCtrl = function($scope, UserService, $modal, $log) {
        var userList = [];
        $scope.users = userList;

        function loadUsers() {
            UserService.userList(function(users) {
                userList = users;
                $scope.users = users;
            }, function(error) {
                $scope.alertMessages = error;
            });
        }

        embryo.authenticated(function() {
            loadUsers();
        });

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

        $scope.edit = function(user) {
            $scope.message = null;
            $scope.alertMessages = null;
            $scope.editUser = {
                login : user.login,
                email : user.email,
                role : user.role,
                shipMmsi : user.shipMmsi
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
            if ($scope.editUser.password != $scope.passwordAgain || !$scope.editUser.password) {
                $scope.alertMessages = [ "User not created. Passwords must match" ];
                return;
            }
            if (!$scope.editUser.login) {
                $scope.alertMessages = [ "User not created. User login must be provided." ];
                return;
            }

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

        $scope.submitEdit = function() {
            $scope.message = "Saving " + $scope.editUser.login + " ...";
            $scope.alertMessages = null;
            UserService.edit($scope.editUser, function() {
                $scope.message = "User " + $scope.editUser.login + " saved.";
                loadUsers();
            }, function(error) {
                $scope.message = null;
                $scope.alertMessages = error;
            });
        };

        $scope.del = function(user) {
            var modalInstance = $modal.open({
                controller : embryo.DeleteModalCtrl,
                templateUrl : "deleteUserDialog.html",
                resolve : {
                    user : function() {
                        return user;
                    }
                }
            });
            modalInstance.result.then(function(user) {
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

    embryo.DeleteModalCtrl = function($scope, $modalInstance, user) {
        $scope.userToDelete = user;
        $scope.ok = function() {
            $modalInstance.close($scope.userToDelete);
        };
        $scope.cancel = function() {
            $modalInstance.dismiss('cancel');
        };
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
