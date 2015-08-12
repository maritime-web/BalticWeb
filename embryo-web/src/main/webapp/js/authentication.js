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
        function Subject($http, $rootScope, $cookieStore) {
            $rootScope.initialAuthentication = embryo.authentication;
            var authentication = $cookieStore.get('embryo.authentication');
            if (typeof authentication !== 'undefined') {
                embryo.authentication = authentication;
            }
            $rootScope.authentication = embryo.authentication;
            var details = embryo.authentication;

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
                /* This is what should be done, but since it's asynchronous (and the directives aren't),
                 * it requires a bit of thought.
                 $http.get(embryo.baseUrl + 'rest/authentication/isloggedin').error(function(response) {
                 if(response.status === 401) {
                 // This means false
                 }
                 });
                 // Otherwise true
                 */
                return this.roles().length > 0;
            };

            this.login = function (username, password, success, error) {
                var data = {
                    params: {
                        userName: username,
                        password: password
                    }
                };
                $http.get(embryo.baseUrl + "rest/authentication/login", data).success(function (details) {
                    $cookieStore.put('embryo.authentication', details);
                    sessionStorage.clear();
                    $rootScope.authentication = details;
                    embryo.authentication = details;

                    embryo.eventbus.fireEvent(embryo.eventbus.AuthenticatedEvent());

                    // EMBRYO-325
                    embryo.eventbus.fireEvent(embryo.eventbus.AuthenticationChangedEvent());
                    success(details);
                }).error(function (data, status) {
                    if (error) {
                        error(data, status);
                    }
                });
            };

            this.logout = function (success, error) {
                $http.get(embryo.baseUrl + "rest/authentication/logout").success(function () {
                    clearSessionData($cookieStore, $rootScope);
                    embryo.eventbus.fireEvent(embryo.eventbus.AuthenticationChangedEvent());
                    success();
                }).error(error);
            };

            this.authenticationChanged = function (callback) {
                embryo.authenticationChanged(callback);
            };

            this.getDetails = function () {
                return embryo.authentication;
            };
        }

        this.$get = function ($http, $rootScope, $cookieStore) {
            return new Subject($http, $rootScope, $cookieStore);
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

    function detectBrowser() {
        if (browser.isIE() && browser.ieVersion() <= 8) {
            $("#ie78ver").html(browser.ieVersion());
            $("#ie78").show();
        } else if (browser.isIE() && browser.ieVersion() == 9) {
            $("#ie9ver").html(browser.ieVersion());
            $("#ie9").show();
        }
    }

    function useCookies() {
        return "true" == getCookie("cookies-accepted");
    }

    function rememberUseCookies() {
        $('#cookiesUsage').css("display", "none");
        if ("true" != getCookie("cookies-accepted")) {
            setCookie("cookies-accepted", "true", 365);
        }
    }

    embryo.LoginModalCtrl = function ($scope, $http, $modalInstance, $location, Subject, msg) {
        function resetMsgs() {
            $scope.infoMsg = null;
            $scope.msg = null;
        }

        $scope.msg = msg;
        $scope.focusMe = true;

        if (browser.isIE() && browser.ieVersion() <= 8) {
            $scope.ie78ver = browser.ieVersion();
        } else if (browser.isIE() && browser.ieVersion() == 9) {
            $scope.ie9ver = browser.ieVersion();
        }

        $scope.useCookies = useCookies();
        $scope.user = {};
        $scope.forgot = false;

        $scope.login = function () {
            resetMsgs();

            var messageId = embryo.messagePanel.show({
                text: "Logging in ..."
            });
            rememberUseCookies();

            $scope.$close();

            Subject.login($scope.user.name, $scope.user.pwd, function () {
                var path = location.pathname;
                if (path.indexOf("index.html") >= 0 || path.indexOf("content.html") >= 0 || path.indexOf(".html") < 0) {
                    location.href = "map.html#/vessel";
                }

                embryo.messagePanel.replace(messageId, {
                    text: "Succesfully logged in.",
                    type: "success"
                });
            }, function (data, status) {
                // updateNavigationBar();
                embryo.messagePanel.replace(messageId, {
                    text: "Log in failed. (" + status + ")",
                    type: "error"
                });

                var errorMessage = "";
                if (status == 401) {
                    errorMessage = "Wrong user name or password";
                } else {
                    errorMessage = embryo.ErrorService.extractError(data, status)[0];
                }
                setTimeout(function () {
                    $scope.loginDlg({
                        msg: errorMessage
                    });
                }, 1000);
            });

            $("#userName").val("");
            $("#password").val("");
            $("#error").css("display", "none");
            $("#loginWrongLoginOrPassword").css("display", "none");

        };

        $scope.sendPassword = function () {
            resetMsgs();
            var data = {
                emailAddress: $scope.user.email
            };
            $http.post(embryo.baseUrl + "rest/forgot-password/request", data).success(function (details) {
                $scope.infoMsg = 'E-mail sent!';
                $scope.user.email = '';
            }).error(function (data, status) {
                $scope.msg = data;
            });
        };

        $scope.passwordEnabled = function () {
            return $scope.user.email && angular.element('.emailfield').$valid;
        };

        $scope.forgotPassword = function () {
            resetMsgs();
            $scope.forgot = true;

        };

        $scope.back = function () {
            resetMsgs();
            $scope.forgot = false;
        };

        $scope.cancel = function (cb) {
            var path = location.pathname;
            if (path.indexOf("index.html") < 0 && path.indexOf(".html") >= 0) {
                location = ".";
            }
            cb('aborted');
        };
    };

    function logout(event) {
        event.preventDefault();

        var messageId = embryo.messagePanel.show({
            text: "Logging out ..."
        });

        embryo.security.Subject.logout(function () {
            var path = location.pathname;
            if (path.indexOf("index.html") < 0 && path.indexOf(".html") >= 0) {
                location = ".";
                // setTimeout(function() {
                // location = ".";
                // }, 100);
            }
        }, function (data, status) {
            embryo.messagePanel.replace(messageId, {
                text: "Logout failed. (" + status + ")",
                type: "error"
            });
        });
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

    module.run([ 'Subject', '$rootScope', '$location', '$modal', function (Subject, $rootScope, $location, $modal) {
        embryo.security.Subject = Subject;

        embryo.ready(function () {
            $rootScope.Subject = Subject;
            $rootScope.logout = logout;

            $rootScope.loginDlg = function (config) {
                return $modal.open({
                    controller: embryo.LoginModalCtrl,
                    templateUrl: "loginDialog.html",
                    windowClass: "embryo-small-modal",
                    resolve: {
                        msg: function () {
                            return typeof config === "object" ? config.msg : null;
                        }
                    }
                });
            };

            if (Subject.isLoggedIn()) {
                embryo.eventbus.fireEvent(embryo.eventbus.AuthenticatedEvent());

                // EMBRYO-325
                embryo.eventbus.fireEvent(embryo.eventbus.AuthenticationChangedEvent());
            } else if (embryo.authentication.currentPageRequiresAuthentication) {
                $rootScope.loginDlg();
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
