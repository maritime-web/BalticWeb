(function() {
    "use strict";

    var indexApp = angular.module('embryo.content', [ 'ngRoute' , 'embryo.menu', 'ui.bootstrap']);

    indexApp.config([ '$routeProvider', function($routeProvider) {
        $routeProvider.when('/disclaimer', {
            templateUrl : 'partials/disclaimer.html'
        }).when('/cookies', {
            templateUrl : 'partials/cookies.html'
        }).when('/requestAccess', {
            templateUrl : 'partials/security/access.html'
        }).when('/changePassword/:uuid', {
			templateUrl : 'partials/security/changepassword.html'        	
        }).otherwise({
		    controller : function(){
		        window.location.replace('/');
		    }, 
		    template : "<div></div>"
		});
    } ]);

    $(function() {
        embryo.authentication.currentPageRequiresAuthentication = false;
    });
})();

