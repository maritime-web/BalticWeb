(function() {
    "use strict";

    var indexApp = angular.module('embryo.content', [ 'ngRoute' , 'embryo.menu', 'embryo.feedback', 'ui.bootstrap']);

    indexApp.config([ '$routeProvider', function($routeProvider) {
        $routeProvider.when('/feedback', {
            templateUrl: 'partials/front/feedback.html'
        }).when('/disclaimer', {
            templateUrl: 'partials/front/disclaimer.html'
        }).when('/cookies', {
            templateUrl: 'partials/front/cookies.html'
        }).when('/requestAccess', {
            templateUrl: 'partials/common/access.html'
        }).when('/changePassword/:uuid', {
            templateUrl: 'partials/common/changepassword.html'
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

