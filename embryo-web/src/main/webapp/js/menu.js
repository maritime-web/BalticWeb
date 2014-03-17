embryo.eventbus.GroupChangedEvent = function(id) {
    var event = jQuery.Event("GroupChangedEvent");
    event.groupId = id;
    return event;
};

embryo.eventbus.registerShorthand(embryo.eventbus.GroupChangedEvent, "groupChanged");

(function() {
    "use strict";

    var menuModule = angular.module('embryo.menu', [ 'ui.bootstrap', 'embryo.authentication' ]);
    menuModule.directive('showActive', [ '$location', function($location) {
        return {
            restrict : 'A',
            link : function(scope, element) {
                scope.$watch(function() {
                    return $location.absUrl();
                }, function(url) {
                    var menuItems = angular.element(element).children();
                    menuItems.each(function(index, item){
                        var menuItem = angular.element(item);
                        var href = menuItem.find("a").attr('href');
                        if (url.indexOf(href, url.length - href.length) >= 0) {
                            menuItem.addClass("active");
                        } else {
                            menuItem.removeClass("active");
                        }
                    });
                });
            }
        };
    } ]);

    var embryoAuthenticated = false;

    embryo.authenticated(function() {
        embryoAuthenticated = true;
    })
    
    embryo.MenuCtrl = function($scope, Subject, $location, $timeout) {
        $scope.$watch(function() {
            return $location.absUrl();
        }, function(url) {
            function authenticated() {
                embryo.eventbus.fireEvent(embryo.eventbus.GroupChangedEvent(url.substring(url.lastIndexOf("/") + 1)));
            }

            if (!embryoAuthenticated) {
                embryo.authenticated(authenticated);
            } else {
                authenticated();
            }
        });
    };
})();
