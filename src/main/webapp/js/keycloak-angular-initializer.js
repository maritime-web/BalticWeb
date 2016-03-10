var keycloakInitialize = function (module, moduleName) {
    var auth = {};

    angular.element(document).ready(function () {
        var keycloakAuth = new Keycloak('keycloak.json');
        auth.loggedIn = false;

        console.log('*** Keycloak instantiated. Initializing <<<<<<<<');
        keycloakAuth.init({onLoad: 'check-sso', checkLoginIframe: false })
            .success(function () {
                console.log('*** Keycloak Initialized *******************************');

                auth.loggedIn = keycloakAuth.authenticated;
                auth.authz = keycloakAuth;

                module.factory('Auth', function() {
                    return auth;
                });
                angular.bootstrap(document, [moduleName]);
        }).error(function () {
            console.log('*** ERROR Initializing Keycloak');
            window.location.reload();
        });
    });

    module.factory('authInterceptor', function($q, Auth) {
        return {
            request: function (config) {
                var isHtmlRequest = function(config) {
                    return config.url.indexOf(".html") > 0;
                };

                var shouldAddTokenToRequest = function(config) {
                    return !isHtmlRequest(config) && Auth.authz.token;
                };

                if (!isHtmlRequest(config)) {
                    console.log('*** Intercepted call to : ' + config.url);
                }

                var deferred = $q.defer();
                if (shouldAddTokenToRequest(config)) {
                    console.log('*** UPDATING TOKEN');
                    //console.log('*** Auth.authz.tokenParsed: ' + JSON.stringify(Auth.authz.tokenParsed));

                    Auth.authz.updateToken(5).success(function() {
                        config.headers = config.headers || {};
                        config.headers.Authorization = 'Bearer ' + Auth.authz.token;

                        console.log('***  ***');
                        console.log('*** Auth.authz.tokenParsed: ' + JSON.stringify(Auth.authz.tokenParsed));
                        console.log("authenticated? " + Auth.authz.authenticated);
                        console.log("isTokenExpired? " + Auth.authz.isTokenExpired());
                        console.log("timeSkew: " + Auth.authz.timeSkew);
                        console.log('***  ***');

                        deferred.resolve(config);
                    }).error(function() {
                        deferred.reject('Failed to refresh token');
                    });
                } else {
                    deferred.resolve(config);
                }
                return deferred.promise;
            }
        };
    });

    module.config(function($httpProvider) {
        $httpProvider.interceptors.push('authInterceptor');
    });
};