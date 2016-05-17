var keycloakInitialize = function (module, moduleName, loginRequired, loggedInPage) {
    var auth = {};

    angular.element(document).ready(function () {
        var keycloakAuth = new Keycloak('keycloak.json');
        auth.loggedIn = false;

        var initOptions = {onLoad: loginRequired ? 'login-required' : 'check-sso', checkLoginIframe: false};
        keycloakAuth.init(initOptions)
            .success(function () {
                console.log('*** Keycloak Initialized');

                auth.loggedIn = keycloakAuth.authenticated;
                auth.authz = keycloakAuth;

                module.factory('Auth', function () {
                    return auth;
                });
                angular.bootstrap(document, [moduleName]);
                if (auth.loggedIn && loggedInPage) {
                    window.location.replace(loggedInPage);
                }
            })
            .error(function () {
                console.log('*** ERROR Initializing Keycloak');
                window.location.reload();
            });
    });

    module.factory('authInterceptor', function ($q, Auth) {
        return {
            request: function (config) {
                var isHtmlRequest = function (config) {
                    return config.url.indexOf(".html") > 0;
                };

                var shouldAddTokenToRequest = function (config) {
                    return !isHtmlRequest(config) && Auth.authz.token;
                };

                if (!isHtmlRequest(config)) {
                    //console.log('*** Intercepted call to : ' + config.url);
                }

                var deferred = $q.defer();
                if (shouldAddTokenToRequest(config)) {

                    Auth.authz.updateToken(5).success(function () {
                        config.headers = config.headers || {};
                        config.headers.Authorization = 'Bearer ' + Auth.authz.token;
                        console.log('*** Auth.authz.tokenParsed: ' + JSON.stringify(Auth.authz.tokenParsed));
                        deferred.resolve(config);
                    }).error(function () {
                        deferred.reject('Failed to refresh token');
                    });
                } else {
                    deferred.resolve(config);
                }
                return deferred.promise;
            }
        };
    });

    module.config(function ($httpProvider) {
        $httpProvider.interceptors.push('authInterceptor');
    });
};