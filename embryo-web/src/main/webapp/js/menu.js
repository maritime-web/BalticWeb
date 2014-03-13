embryo.eventbus.GroupChangedEvent = function(id) {
    var event = jQuery.Event("GroupChangedEvent");
    event.groupId = id;
    return event;
};

embryo.eventbus.registerShorthand(embryo.eventbus.GroupChangedEvent, "groupChanged");

(function() {
    "use strict";
    
    var menuModule = angular.module('embryo.menu',[ 'embryo.authentication']);

    var embryoAuthenticated = false;

    embryo.authenticated(function() {
        embryoAuthenticated = true;
    })

    embryo.ready(function(){
        $("#navigationBar .pull-right a.dropdown-toggle").click(function(e) {
            e.preventDefault();
        });
    });

    
    embryo.MenuCtrl = function($scope, Subject, $location, $element, $timeout) {
        
        $scope.$watch(function() {
            return $location.absUrl();
        }, function (url) {

            $(".navtabs a", $element).each(function(k, v) {
                var href = $(v).attr("href");
                if (url.indexOf(href, url.length - href.length) >= 0) {
                    $(v).parent("li").addClass("active");
                } else {
                    $(v).parent("li").removeClass("active");
                }
            });

            function authenticated() {
                embryo.eventbus.fireEvent(embryo.eventbus.GroupChangedEvent(url.substring(url.lastIndexOf("/")+1)));
            }

            if (!embryoAuthenticated) {
                embryo.authenticated(authenticated);
            } else {
                authenticated();
            }
        });        
    };
})();
