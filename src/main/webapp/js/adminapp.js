	(function() {
	    "use strict";
	    var app = angular.module('embryo.administration', [ 'ui.bootstrap', 'ngRoute', 'embryo.administration.log',
	            'embryo.administration.users', 'embryo.menu']);
	
	    app.config([ '$routeProvider', function($routeProvider) {
	        $routeProvider.when('/log', {
	            templateUrl : 'partials/admin/log.html',
	            resolve: embryo.security.routeSecurityResolver(embryo.security.permissions.admin)
	        }).when('/users', {
	            templateUrl : 'partials/admin/users.html',
	            resolve: embryo.security.routeSecurityResolver(embryo.security.permissions.admin)
	        }).when('/settings', {
	        	templateUrl : 'partials/admin/settings.html',
	        	resolve: embryo.security.routeSecurityResolver()
	        }).otherwise({
	            controller : function() {
	                window.location.replace('/');
	            },
	            template : "<div></div>"
	        });
	    } ]);
	}());

