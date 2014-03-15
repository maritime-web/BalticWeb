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

// embryo.ready
(function() {

    if (embryo.authentication.userName == null) {
        var messageId = embryo.messagePanel.show({
            text : "Refreshing ..."
        })

        $.ajax({
            url : embryo.baseUrl + "rest/authentication/details",
            data : {},
            success : function(data) {
                embryo.authentication = data;
                embryo.messagePanel.replace(messageId, {
                    text : "Refresh succesful.",
                    type : "success"
                });
                updateNavigationBar();
                embryo.eventbus.fireEvent(embryo.eventbus.AuthenticatedEvent());
            },
            error : function(data) {
                if (data.status == 401 && embryo.authentication.currentPageRequiresAuthentication) {
                    embryo.messagePanel.remove(messageId);
                    clearMessages();
                    detectBrowser();
                    $("#login").modal("show");
                } else {
                    embryo.messagePanel.replace(messageId, {
                        text : "Refresh failed. (" + data.status + ")",
                        type : "error"
                    })
                }
            }
        });
    }
});

(function() {
    "use strict";
    var module = angular.module('embryo.authentication', [ 'embryo.base', 'ngCookies', 'ui.bootstrap.modal',
            'ui.bootstrap.tpls' ]);

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

    module.service('Subject', [
            '$cookieStore',
            '$http',
            '$rootScope',
            function($cookieStore, $http, $rootScope) {
                $rootScope.initialAuthentication = embryo.authentication;

                var authentication = $cookieStore.get('embryo.authentication');
                if (typeof authentication !== 'undefined') {
                    embryo.authentication = authentication;
                }
                $rootScope.authentication = embryo.authentication;

                return {
                    roles : function() {
                        return typeof embryo.authentication.permissions === 'undefined' ? []
                                : embryo.authentication.permissions;
                    },
                    authorize : function(permission) {
                        var index, permissions = this.roles();

                        for (index in permissions) {
                            if (permissions[index] == permission) {
                                return true;
                            }
                        }
                        return false;
                    },
                    isLoggedIn : function(permission) {
                        return this.roles().length > 0;
                    },
                    login : function(username, password, success, error) {
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
                            embryo.eventbus.fireEvent(embryo.eventbus.AuthenticatedEvent());
                            success(details);
                        }).error(function(data, status) {
                            if (error) {
                                error(data, status);
                            }
                        });
                    },
                    logout : function(success, error) {
                        $http.get(embryo.baseUrl + "rest/authentication/logout").success(function() {
                            sessionStorage.clear();
                            localStorage.clear();
                            $cookieStore.remove('embryo.authentication');
                            $rootScope.authentication = $rootScope.initialAuthentication;

                            embryo.authentication = $rootScope.initialAuthentication;
                            success();
                        }).error(error);
                    },
                };
            } ]);

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

    embryo.LoginModalCtrl = function($scope, $modalInstance, $location, Subject, msg) {
        $scope.msg = msg;
        $scope.focusMe = true;

        if (browser.isIE() && browser.ieVersion() <= 7) {
            $scope.ie7ver = browser.ieVersion();
        } else if (browser.isIE() && browser.ieVersion() <= 9) {
            $scope.ie89ver = browser.ieVersion();
        }

        $scope.useCookies = useCookies();
        $scope.user = {};

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

        }
    };

    function clearMessages() {
        $("#ie89").hide();
        $("#ie7").hide();
        $("#loginWrongLoginOrPassword").hide();
    }

    function logout(event) {
        event.preventDefault();

        var messageId = embryo.messagePanel.show({
            text : "Logging out ..."
        })

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
            })
        });
    }

    module.run([ 'Subject', '$rootScope', '$location', '$modal', function(Subject, $rootScope, $location, $modal) {
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

        embryo.ready(function() {
            embryo.security.Subject = Subject;

            $rootScope.Subject = Subject;
            $rootScope.logout = logout;

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
                if (Subject.authorize(access)) {
                    var deferred = $q.defer();
                    deferred.resolve();
                    return deferred.promise;
                } else {
                    location.href = "/";
                }
            }
        }
    }
}());
