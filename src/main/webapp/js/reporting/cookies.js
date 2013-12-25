(function() {
    "use strict";

    var module = angular.module('embryo.cookies', []);

    embryo.CookiesViewCtrl = function($scope) {
        embryo.controllers.cookies = {
            show : function(context) {
                $("#cookiesViewPanel").css("display", "block");
            }
        };
    };
}());
