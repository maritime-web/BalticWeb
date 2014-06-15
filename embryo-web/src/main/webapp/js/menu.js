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
    
    function templateFn(expr) {
        return function(element, attr) {
            var ngIf = attr.ngIf;
            var value = typeof expr === 'function' ? expr(attr) : expr;

            /**
             * Make sure to combine with existing ngIf!
             */
            if (ngIf) {
                value += ' && ' + ngIf;
            }

            var inner = element.get(0);
            // we have to clear all the values because angular
            // is going to merge the attrs collection
            // back into the element after this function finishes
            angular.forEach(inner.attributes, function(attr, key) {
                attr.value = '';
            });
            attr.$set('ng-if', value);
            return inner.outerHTML;
        }
    }

    menuModule.directive('eLocationEnabled', [ '$location', function($location) {
        return {
            restrict : 'A',
            replace : true,
            scope : true,
            template : templateFn('location'),
            link : function(scope, element, attrs) {
                scope.$watch(function() {
                    return $location.absUrl();
                }, function(url) {
                    var loc = attrs.eLocationEnabled;
                    scope.location = url.indexOf(loc, url.length - loc.length) >= 0;
                });
            }
        };
    } ]);
    
    var embryoAuthenticated = false;

    embryo.authenticated(function() {
        embryoAuthenticated = true;
    });
    
    embryo.MenuCtrl = function($scope, Subject, $location) {
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
