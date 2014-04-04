embryo.authentication = {
    currentPageRequiresAuthentication : true
};

embryo.security = {};

embryo.security.permissions = {
    admin : "Administration"
};

embryo.eventbus.AuthenticatedEvent = function() {
    var event = jQuery.Event("AuthenticatedEvent");
    return event;
};

embryo.eventbus.registerShorthand(embryo.eventbus.AuthenticatedEvent, "authenticated");

(function() {
    "use strict";

    var module = angular.module('embryo.authentication', [ 'embryo.base', 'ngCookies', 'ui.bootstrap.modal', 'ui.bootstrap.tpls' ]);

    embryo.RequestAccessCtrl = function($scope, $http) {
        $scope.request = {};
        $scope.message = null;
        $scope.alertMessages = null;
        $("#rPreferredLogin").focus();

        $scope.sendRequest = function() {
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

                $http.post(embryo.baseUrl + "rest/request-access/save", $scope.request).success(function() {
                    $scope.message = "Request for access has been sent. We will get back to you via email.";
                }).error(function() {
                    $scope.alertMessages = embryo.ErrorService.extractError(data, status);
                    $scope.alertMessages.push("Request for access has failed. Please try again.");
                });
            }
        };
    };

    embryo.ChangePasswordCtrl = function($scope, $http, $routeParams) {
        $scope.request = {};
        $scope.message = null;
        $scope.alertMessages = null;

        var pwf = $('#passwordfield');
        pwf.focus();

        $scope.user = null;

        var uuid = $routeParams.uuid;

        $http.get('/rest/authentication/change-password?uuid=' + uuid).success(function(data) {
            if (!data) {
                $scope.alertMessages = [ 'Did not find any user matching the URL. Perhaps the password has already been changed?' ];
            } else {
                $scope.user = data;
            }
        }).error(function(data, status) {
            $scope.alertMessages = embryo.ErrorService.extractError(data, status);
        });

        $scope.changePassword = function() {
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
                    password : $scope.change.password,
                    uuid : uuid
                };
                $http.post('/rest/authentication/change-password', data).success(function(data) {
                    $scope.message = 'Your password has now been updated.';
                    $scope.user = null;
                }).error(function(data, status) {
                    $scope.alertMessages = embryo.ErrorService.extractError(data, status);
                });
            }

        };
    };

    function clearSessionData($cookieStore, $rootScope) {
        sessionStorage.clear();
        localStorage.clear();
        $cookieStore.remove('embryo.authentication');
        $rootScope.authentication = $rootScope.initialAuthentication;
        embryo.authentication = $rootScope.initialAuthentication;
    }

    module.provider('Subject', function() {
        function Subject($http, $rootScope, $cookieStore) {
            $rootScope.initialAuthentication = embryo.authentication;
            var authentication = $cookieStore.get('embryo.authentication');
            if (typeof authentication !== 'undefined') {
                embryo.authentication = authentication;
            }
            $rootScope.authentication = embryo.authentication;

            this.roles = function() {
                return typeof embryo.authentication.permissions === 'undefined' ? [] : embryo.authentication.permissions;
            };

            this.authorize = function(permission) {
                var index = null, permissions = this.roles();

                for (index in permissions) {
                    if (permissions[index] == permission) {
                        return true;
                    }
                }
                return false;
            };

            this.isLoggedIn = function(permission) {
                return this.roles().length > 0;
            };

            this.login = function(username, password, success, error) {
                var data = {
                    params : {
                        userName : username,
                        password : password
                    }
                };
                $http.get(embryo.baseUrl + "rest/authentication/login", data).success(function(details) {
                    $cookieStore.put('embryo.authentication', details);
                    sessionStorage.clear();
                    $rootScope.authentication = details;
                    embryo.authentication = details;
                    success(details);
                }).error(function(data, status) {
                    if (error) {
                        error(data, status);
                    }
                });
            };

            this.logout = function(success, error) {
                $http.get(embryo.baseUrl + "rest/authentication/logout").success(function() {
                    clearSessionData($cookieStore, $rootScope);
                    success();
                }).error(error);
            };
        }

        this.$get = function($http, $rootScope, $cookieStore) {
            return new Subject($http, $rootScope, $cookieStore);
        };
    });

    module.directive('passwordMatch', [ function() {
        return {
            restrict : 'A',
            scope : true,
            require : 'ngModel',
            link : function(scope, elem, attrs, control) {
                var checker = function() {
                    // get the value of the first password
                    var e1 = scope.$eval(attrs.ngModel);

                    // get the value of the other password
                    var e2 = scope.$eval(attrs.passwordMatch);
                    return e1 == e2;
                };
                scope.$watch(checker, function(n) {
                    // set the form control to valid if both
                    // passwords are the same, else invalid
                    control.$setValidity("passwordMatch", n);
                });
            }
        };
    } ]);

    module.directive('requiresPermission', [ '$rootScope', 'Subject', function($rootScope, Subject) {
        return {
            restrict : 'A',
            link : function(scope, element, attrs) {
                var prevDisp = element.css('display');
                $rootScope.$watch("authentication", function(roles) {
                    if (!Subject.authorize(attrs.requiresPermission)) {
                        element.css('display', 'none');
                    } else {
                        element.css('display', prevDisp);
                    }
                });
            }
        };
    } ]);

    module.directive('requiresAuthenticated', [ '$rootScope', 'Subject', function($rootScope, Subject) {
        return {
            restrict : 'A',
            link : function(scope, element, attrs) {
                var prevDisp = element.css('display');
                $rootScope.$watch("authentication", function(authentication) {
                    if (!Subject.isLoggedIn()) {
                        element.css('display', 'none');
                    } else {
                        element.css('display', prevDisp);
                    }
                });
            }
        };
    } ]);

    module.directive('requiresUnauthenticated', [ '$rootScope', 'Subject', function($rootScope, Subject) {
        return {
            restrict : 'A',
            link : function(scope, element, attrs) {
                var prevDisp = element.css('display');
                $rootScope.$watch("authentication", function(roles) {
                    if (Subject.isLoggedIn()) {
                        element.css('display', 'none');
                    } else {
                        element.css('display', prevDisp);
                    }
                });
            }
        };
    } ]);

    function detectBrowser() {
        if (browser.isIE() && browser.ieVersion() <= 7) {
            $("#ie7ver").html(browser.ieVersion());
            $("#ie7").show();
        } else if (browser.isIE() && browser.ieVersion() <= 9) {
            $("#ie89ver").html(browser.ieVersion());
            $("#ie89").show();
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

    embryo.LoginModalCtrl = function($scope, $http, $modalInstance, $location, Subject, msg) {
        $scope.msg = msg;
        $scope.focusMe = true;

        if (browser.isIE() && browser.ieVersion() <= 7) {
            $scope.ie7ver = browser.ieVersion();
        } else if (browser.isIE() && browser.ieVersion() <= 9) {
            $scope.ie89ver = browser.ieVersion();
        }

        $scope.useCookies = useCookies();
        $scope.user = {};
        $scope.forgot = false;

        $scope.login = function() {
            var messageId = embryo.messagePanel.show({
                text : "Logging in ..."
            });
            rememberUseCookies();

            $scope.$close();

            Subject.login($scope.user.name, $scope.user.pwd, function() {
                var path = location.pathname;
                if (path.indexOf("index.html") >= 0 || path.indexOf("content.html") >= 0 || path.indexOf(".html") < 0) {
                    location.href = "map.html#/vessel";
                }

                embryo.messagePanel.replace(messageId, {
                    text : "Succesfully logged in.",
                    type : "success"
                });
                embryo.eventbus.fireEvent(embryo.eventbus.AuthenticatedEvent());
            }, function(data, status) {
                // updateNavigationBar();
                embryo.messagePanel.replace(messageId, {
                    text : "Log in failed. (" + status + ")",
                    type : "error"
                });

                var errorMessage = "";
                if (status == 401) {
                    errorMessage = "Wrong user name or password";
                } else {
                    errorMessage = embryo.ErrorService.extractError(data, status)[0];
                }
                setTimeout(function() {
                    $scope.loginDlg({
                        msg : errorMessage
                    });
                }, 1000);
            });

            $("#userName").val("");
            $("#password").val("");
            $("#error").css("display", "none");
            $("#loginWrongLoginOrPassword").css("display", "none");

        };

        $scope.sendPassword = function() {
            var data = {
                emailAddress : $scope.user.email
            };
            $http.post(embryo.baseUrl + "rest/forgot-password/request", data).success(function(details) {
                $scope.msg = 'E-mail sent!';
                $scope.user.email = '';
            }).error(function(data, status) {
                $scope.msg = data;
            });
        };

        $scope.passwordEnabled = function() {
            return $scope.user.email && angular.element('.emailfield').$valid;
        };

        $scope.forgotPassword = function() {
            $scope.forgot = true;

        };

        $scope.back = function() {
            $scope.forgot = false;
        };
    };

    function logout(event) {
        event.preventDefault();

        var messageId = embryo.messagePanel.show({
            text : "Logging out ..."
        });

        embryo.security.Subject.logout(function() {
            var path = location.pathname;
            if (path.indexOf("index.html") < 0 && path.indexOf(".html") >= 0) {
                location = ".";
                // setTimeout(function() {
                // location = ".";
                // }, 100);
            }
        }, function(data, status) {
            embryo.messagePanel.replace(messageId, {
                text : "Logout failed. (" + status + ")",
                type : "error"
            });
        });
    }

    module.config([ '$httpProvider', function($httpProvider) {
        $httpProvider.responseInterceptors.push([ '$location', '$q', '$cookieStore', '$rootScope', function($location, $q, $cookieStore, $rootScope) {
            function success(response) {
                return response;
            }
            function error(response) {
                if (response.status === 401) {
                    embryo.messagePanel.show({
                        text : "Session Lost. You will be logged out ..."
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
            return function(promise) {
                return promise.then(success, error);
            };
        } ]);
    } ]);

    module.run([ 'Subject', '$rootScope', '$location', '$modal', function(Subject, $rootScope, $location, $modal) {
        embryo.ready(function() {
            embryo.security.Subject = Subject;

            $rootScope.Subject = Subject;
            $rootScope.logout = logout;

            $rootScope.loginDlg = function(config) {
                return $modal.open({
                    controller : embryo.LoginModalCtrl,
                    templateUrl : "loginDialog.html",
                    resolve : {
                        msg : function() {
                            return typeof config === "object" ? config.msg : null;
                        }
                    }
                });
            };

            if (Subject.isLoggedIn()) {
                embryo.eventbus.fireEvent(embryo.eventbus.AuthenticatedEvent());
            } else if (embryo.authentication.currentPageRequiresAuthentication) {
                $rootScope.loginDlg();
            }
        });
    } ]);

    embryo.security.routeSecurityResolver = function(access) {
        return {
            load : function($q, Subject, $rootScope) {
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
