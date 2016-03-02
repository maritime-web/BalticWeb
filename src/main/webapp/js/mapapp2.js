(function () {
    "use strict";
    var module = angular.module('embryo.mapapp', ['embryo.map', 'ui.bootstrap', 'embryo.menu', 'embryo.yourvessel.control', 'embryo.vessel.control',
        'embryo.reporting.control', 'embryo.greenpos', 'embryo.vessel.controller', 'embryo.ice.control',
        'embryo.msi.controllers', 'embryo.routeUpload', 'embryo.schedule', 'embryo.routeEdit', 'embryo.decimal',
        'embryo.authentication', 'embryo.shape', 'embryo.zoom', 'embryo.metoc', 'embryo.weather.control',
        'embryo.controller.reporting', 'embryo.aisinformation', 'embryo.forecast.control', 'embryo.satellite-ice.control',
        'embryo.areaselect.control', 'embryo.sar.controllers', 'embryo.sar.views']);


    var auth = {};
    var logout = function(){
        console.log('*** LOGOUT');
        auth.loggedIn = false;
        auth.authz = null;
        window.location = auth.logoutUrl;
    };

    angular.element(document).ready(function () {
        var keycloakAuth = new Keycloak('keycloak.json');
        auth.loggedIn = false;

        keycloakAuth.init({ onLoad: 'login-required', checkLoginIframe: 'false' }).success(function () {
            auth.loggedIn = true;
            auth.authz = keycloakAuth;
            auth.logoutUrl = keycloakAuth.authServerUrl + "/realms/demo/tokens/logout?redirect_uri=/index.html";
            module.factory('Auth', function() {
                return auth;
            });
            angular.bootstrap(document, ['embryo.mapapp']);
        }).error(function () {
            console.log('*** ERROR Initializing Keycloak');
            window.location.reload();
        });
    });

    module.factory('authInterceptor', function($q, Auth) {
        return {
            request: function (config) {
                var deferred = $q.defer();
                if (Auth.authz.token) {
                    console.log('*** UPDATING TOKEN');
                    console.log('*** Auth.authz.tokenParsed.iat: ' + Auth.authz.tokenParsed.iat);
                    console.log('*** Auth.authz.tokenParsed: ' + JSON.stringify(Auth.authz.tokenParsed));
                    console.log('*** Auth.authz.timeSkew: ' + Auth.authz.timeSkew);

                    Auth.authz.updateToken(800).success(function() {
                        console.log('*** TOKEN UPDATED');
                        console.log('*** Auth.authz.tokenParsed.iat: ' + Auth.authz.tokenParsed.iat);
                        console.log('*** Auth.authz.tokenParsed: ' + JSON.stringify(Auth.authz.tokenParsed));
                        console.log('*** Auth.authz.timeSkew: ' + Auth.authz.timeSkew);
                        config.headers = config.headers || {};
                        config.headers.Authorization = 'Bearer ' + Auth.authz.token;

                        deferred.resolve(config);
                    }).error(function() {
                        deferred.reject('Failed to refresh token');
                    });
                }
                return deferred.promise;
            }
        };
    });

    module.config(function($httpProvider) {
        $httpProvider.interceptors.push('authInterceptor');
    });
})();
