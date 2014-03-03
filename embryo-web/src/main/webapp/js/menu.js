embryo.eventbus.GroupChangedEvent = function(id) {
    var event = jQuery.Event("GroupChangedEvent");
    event.groupId = id;
    return event;
};

embryo.eventbus.registerShorthand(embryo.eventbus.GroupChangedEvent, "groupChanged");

(function() {
    "use strict";
    
    var menuModule = angular.module('embryo.menu',[]);

    var embryoAuthenticated = false;

    embryo.authenticated(function() {
        embryoAuthenticated = true;
    })

    embryo.MenuCtrl = function($scope, $location, $element, $timeout) {
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

            $(".navtabs a", $element).each(function(k, v) {
                var href = $(v).attr("href");
                if (url.indexOf(href, url.length - href.length) >= 0) {
                    $(v).parent("li").addClass("active");
                } else {
                    $(v).parent("li").removeClass("active");
                }
            });

            $("li", $element).each(function(k, v) {
                if ($(v).attr("requires-permission")) $(v).css("display", "none");
            })

            function authenticated() {
                embryo.eventbus.fireEvent(embryo.eventbus.GroupChangedEvent(url.substring(url.lastIndexOf("/")+1)));
                $("li", $element).each(function(k, v) {
                    if (embryo.authentication.permissions.indexOf($(v).attr("requires-permission")) >= 0) $(v).css("display", "block");
                })
            }

            if (!embryoAuthenticated) {
                embryo.authenticated(authenticated);
            } else {
                authenticated();
            }

        });
    };
})();
