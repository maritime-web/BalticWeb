embryo.authentication = {
    currentPageRequiresAuthentication: true
};

embryo.security = {};

embryo.security.permissions = {
    admin: "Administration"
};

embryo.eventbus.AuthenticatedEvent = function () {
    var event = jQuery.Event("AuthenticatedEvent");
    return event;
};

embryo.eventbus.registerShorthand(embryo.eventbus.AuthenticatedEvent, "authenticated");

embryo.eventbus.AuthenticationChangedEvent = function () {
    var event = jQuery.Event("AuthenticationChangedEvent");
    return event;
};
embryo.eventbus.registerShorthand(embryo.eventbus.AuthenticationChangedEvent, "authenticationChanged");

(function () {
    "use strict";

    var serviceModule = angular.module('embryo.authentication.service', [ 'ngCookies' ]);

    embryo.RequestAccessCtrl = function ($scope, $http) {
        $scope.request = {};
        $scope.message = null;
        $scope.alertMessages = null;
        $("#rPreferredLogin").focus();

        $scope.sendRequest = function () {
            $scope.message = null;
            $scope.alertMessages = null;
            if ($scope.request.mmsiNumber) {
                var x = $scope.request.mmsiNumber;
                $scope.request.mmsiNumber = parseInt(x);
                if ($scope.request.mmsiNumber != x) {
                    $scope.alertMessages = [ "MMSI must be only digits." ];
                    return;
                }
            }

            if (!$scope.request.emailAddress) {
                $scope.alertMessages = [ "A proper email address is required." ];
            } else {
                $scope.message = "Sending request for access.";

                $http.post(embryo.baseUrl + "rest/request-access/save", $scope.request).success(function () {
                    $scope.message = "Request for access has been sent. We will get back to you via email.";
                }).error(function (data, status) {
                    $scope.alertMessages = embryo.ErrorService.extractError(data, status);
                    $scope.alertMessages.push("Request for access has failed. Please try again.");
                });
            }
        };
    };

    embryo.ChangePasswordCtrl = function ($scope, $http, $routeParams) {
        $scope.request = {};
        $scope.message = null;
        $scope.alertMessages = null;

        var pwf = $('#passwordfield');
        pwf.focus();

        $scope.user = null;

        var uuid = $routeParams.uuid;

        $http
            .get('/rest/authentication/change-password?uuid=' + uuid)
            .success(
            function (data) {
                if (!data) {
                    $scope.alertMessages = [ 'Did not find any user matching the URL. Perhaps the password has already been changed?' ];
                } else {
                    $scope.user = data;
                }
            }).error(function (data, status) {
                $scope.alertMessages = embryo.ErrorService.extractError(data, status);
            });

        $scope.changePassword = function () {
            if (!$scope.change.password) {
                $scope.alertMessages = $scope.alertMessages.concat('You must enter a password.');
            }
            if (!$scope.change.passwordrepeat) {
                $scope.alertMessages = $scope.alertMessages.concat('You must repeat the password.');
            }
            if ($scope.change.password != $scope.change.passwordrepeat) {
                $scope.alertMessages = $scope.alertMessages.concat('The two passwords must match.');
            }

            if (!$scope.alertMessages) {
                var data = {
                    password: $scope.change.password,
                    uuid: uuid
                };
                $http.post('/rest/authentication/change-password', data).success(function (data) {
                    $scope.message = 'Your password has now been updated.';
                    $scope.user = null;
                }).error(function (data, status) {
                    $scope.alertMessages = embryo.ErrorService.extractError(data, status);
                });
            }

        };
    };

    function clearSessionData($cookieStore, $rootScope) {
        sessionStorage.clear();
        $cookieStore.remove('embryo.authentication');
        $rootScope.authentication = $rootScope.initialAuthentication;
        embryo.authentication = $rootScope.initialAuthentication;
    }

    serviceModule.provider('Subject', function () {
        function Subject($http, $rootScope, $cookieStore, Auth) {
            if (Auth) {
                console.log("######## AUTH injected ##########");
            }
            $rootScope.initialAuthentication = embryo.authentication;
            var authentication = $cookieStore.get('embryo.authentication');
            if (typeof authentication !== 'undefined') {
                embryo.authentication = authentication;
            }
            $rootScope.authentication = embryo.authentication;

            $rootScope.$watch(Auth.loggedIn, function(){
                console.log("==========!!!!!!!! Logged In status changed to " + Auth.loggedIn);
                if (Auth.loggedIn) {
                    $http.get(embryo.baseUrl + "rest/authentication/details").success(function (details) {
                        $cookieStore.put('embryo.authentication', details);
                        sessionStorage.clear();
                        $rootScope.authentication = details;
                        embryo.authentication = details;
                        console.log("++++++++++++++USER DATA. " + JSON.stringify(details));

                        embryo.eventbus.fireEvent(embryo.eventbus.AuthenticatedEvent());
                        embryo.eventbus.fireEvent(embryo.eventbus.AuthenticationChangedEvent());
                    }).error(function (data, status) {
                        console.log("FAILED TO GET USER DATA. Status: " + status);
                    });
                }
            });

            this.roles = function () {
                return typeof embryo.authentication.permissions === 'undefined' ? []
                    : embryo.authentication.permissions;
            };

            this.authorize = function (permissions) {
                var roles = this.roles();

                var allowed = permissions.split(",");

                for (var i in allowed) {
                    for (var j in roles) {
                        if (roles[j] == allowed[i].trim()) {
                            return true;
                        }
                    }
                }

                return false;
            };

            this.isLoggedIn = function () {
                return Auth.loggedIn;
            };

            this.login = function () {
                if (!Auth.loggedIn) {
                    Auth.authz.login({scope: 'offline_access'});
                }
            };

            this.logout = function () {
                clearSessionData($cookieStore, $rootScope);
                embryo.eventbus.fireEvent(embryo.eventbus.AuthenticationChangedEvent());
                console.log("LOGGING OUT location.href: " + location.href);

                Auth.authz.logout({"redirectUri": location.origin});
            };

            this.authenticationChanged = function (callback) {
                embryo.authenticationChanged(callback);
            };

            this.getDetails = function () {
                return embryo.authentication;
            };
        }

        this.$get = function ($http, $rootScope, $cookieStore, Auth) {
            return new Subject($http, $rootScope, $cookieStore, Auth);
        };
    });

    var moduleDirectives = angular.module('embryo.authentication.directives', [ 'embryo.authentication.service' ]);

    moduleDirectives.directive('passwordMatch', [ function () {
        return {
            restrict: 'A',
            scope: true,
            require: 'ngModel',
            link: function (scope, elem, attrs, control) {
                var checker = function () {
                    // get the value of the first password
                    var e1 = scope.$eval(attrs.ngModel);

                    // get the value of the other password
                    var e2 = scope.$eval(attrs.passwordMatch);
                    return e1 == e2;
                };
                scope.$watch(checker, function (n) {
                    // set the form control to valid if both
                    // passwords are the same, else invalid
                    control.$setValidity("passwordMatch", n);
                });
            }
        };
    } ]);

    function templateFn(expr) {
        return function (element, attr) {
            var ngIf = attr.ngIf;
            var value = typeof expr === 'function' ? expr(attr) : expr;

            /**
             * Make sure to combine with existing ngIf!
             */
            if (ngIf) {
                value += ' && ' + ngIf;
            }

            var inner = element.get(0);
            // we have to clear all the values because angular
            // is going to merge the attrs collection
            // back into the element after this function finishes
            angular.forEach(inner.attributes, function (attr, key) {
                attr.value = '';
            });
            attr.$set('ng-if', value);
            return inner.outerHTML;
        };
    }

    moduleDirectives.directive('requiresAuthenticated', [ 'Subject', function (Subject) {
        return {
            restrict: 'A',
            replace: true,
            template: templateFn('user.isLoggedIn()'),
            link: function (scope, element, attrs) {
                scope.user = Subject;
            }
        };

    } ]);

    moduleDirectives.directive('requiresUnauthenticated', [ 'Subject', '$animate', function (Subject, $animate) {
        return {
            restrict: 'A',
            replace: true,
            template: templateFn('!user.isLoggedIn()'),
            link: function (scope, element, attrs) {
                scope.user = Subject;
            }
        };
    } ]);

    moduleDirectives.directive('requiresPermissions', [
        'Subject',
        '$animate',
        function (Subject, $animate) {
            return {
                transclude: 'element',
                priority: 600,
                terminal: true,
                restrict: 'A',
                $$tlb: true,
                link: function ($scope, $element, $attr, ctrl, $transclude) {
                    var block = null, childScope = null;
                    var value = $attr.requiresPermissions;
                    $scope.$watch(function () {
                        return Subject.authorize(value);
                    }, function (condition) {
                        if (condition) {
                            if (!childScope) {
                                childScope = $scope.$new();
                                $transclude(childScope, function (clone) {
                                    block = {
                                        startNode: clone[0],
                                        endNode: clone[clone.length++] = document
                                            .createComment(' end requiresPermission: '
                                                + $attr.requiresPermission + ' ')
                                    };
                                    $animate.enter(clone, $element.parent(), $element);
                                });
                            }
                        } else {
                            if (childScope) {
                                childScope.$destroy();
                                childScope = null;
                            }
                            if (block) {
                                $animate.leave(getBlockElements(block));
                                block = null;
                            }
                        }
                    });
                }
            };
        } ]);

    function logout(event) {
        event.preventDefault();
        embryo.security.Subject.logout();
    }

    var module = angular.module('embryo.authentication', [ 'embryo.base', 'ui.bootstrap.modal', 'ui.bootstrap.tpls',
        'embryo.authentication.service', 'embryo.authentication.directives' ]);

    module.config([
        '$httpProvider',
        function ($httpProvider) {
            $httpProvider.responseInterceptors.push([ '$location', '$q', '$cookieStore', '$rootScope',
                function ($location, $q, $cookieStore, $rootScope) {
                    function success(response) {
                        return response;
                    }

                    function error(response) {
                        if (response.status === 401 && !(response.data.error && response.data.error == 'login failed')) {
                            embryo.messagePanel.show({
                                text: "Session Lost. You will be logged out ..."
                            });
                            clearSessionData($cookieStore, $rootScope);
                            var path = location.pathname;
                            if (path.indexOf("index.html") < 0 && path.indexOf(".html") >= 0) {
                                location = ".";
                            }
                            return $q.reject(response);
                        } else {
                            return $q.reject(response);
                        }
                    }

                    return function (promise) {
                        return promise.then(success, error);
                    };
                } ]);
        } ]);

    module.run([ 'Subject', '$rootScope', function (Subject, $rootScope) {
        embryo.security.Subject = Subject;

        embryo.ready(function () {
            $rootScope.Subject = Subject;
            $rootScope.logout = logout;

            $rootScope.login = function () {
                Subject.login();
            };

            if (Subject.isLoggedIn()) {
                embryo.eventbus.fireEvent(embryo.eventbus.AuthenticatedEvent());

                // EMBRYO-325
                embryo.eventbus.fireEvent(embryo.eventbus.AuthenticationChangedEvent());
            } else if (embryo.authentication.currentPageRequiresAuthentication) {
                Subject.login();
            }
        });
    } ]);

    embryo.security.routeSecurityResolver = function (access) {
        return {
            load: function ($q, Subject, $rootScope) {
                if ((access && Subject.authorize(access)) || Subject.isLoggedIn()) {
                    var deferred = $q.defer();
                    deferred.resolve();
                    return deferred.promise;
                } else {
                    location.href = "/";
                }
            }
        };
    };
}());
