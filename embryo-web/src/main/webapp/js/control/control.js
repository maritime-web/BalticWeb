$(function() {

    var module = angular.module('embryo.control', [ 'ui.bootstrap.accordion' ]);

    module.controller("LeftBarController", [ '$scope', function($scope) {
        this.setSelected = function(selected) {
            this.selected = selected;
        };
    } ]);

    module.directive('eLeftBar', [ '$timeout', '$window', function($timeout, $window) {
        return {
            restrict : 'A',
            controller : "LeftBarController",
            link : function(scope, element, attrs) {
                // add scroll bars
                function fixAccordionSize() {
                    var $bodies = $(element).find('.panel-body');
                    $bodies.css("overflow", "auto");
                    $bodies.css("max-height", Math.max(100, $(window).height() - 233) + "px");
                }
                jQuery($window).resize(fixAccordionSize);
                fixAccordionSize();

                // open first
                $timeout(function() {
                    var headers = element.find("div h4 a");
                    headers.first().click();
                }, 5);
            }
        };
    } ]);

    module.directive('eLeftBarSelection', [ function() {
        return {
            require : '^eLeftBar',
            scope : true,
            restrict : 'A',
            link : function(scope, element, attrs, leftBarCtrl) {
                attrs.$observe('eLeftBarSelection', function(newValue) {
                    var value = !newValue ? null : angular.fromJson(newValue)
                    leftBarCtrl.setSelected(value);
                });
            }
        }
    } ]);

    module.directive('eLeftBarShow', [ '$timeout', function($timeout) {
        return {
            require : '^eLeftBar',
            restrict : 'A',
            link : function(scope, element, attrs, leftBarCtrl) {
                scope.ctrl = leftBarCtrl
                scope.$watch('ctrl.selected', function(newValue, oldValue) {
                    if ((newValue && !oldValue) || (!newValue && oldValue)) {
                        // Force to execute click with a timeout to avoid
                        // $digest in progress exception.
                        // Timeout of 250 ms used for new value to force open
                        // close animation.
                        var ts = newValue ? 250 : 5;
                        var aElem = element.find("div h4 a");
                        $timeout(function() {
                            aElem.click();
                        }, ts);
                    }
                });

            }
        };
    } ]);

    module.directive('eLeftBarSelected', [ function() {
        return {
            require : '^eLeftBar',
            scope : {
                fn : '=eLeftBarSelected'
            },
            restrict : 'A',
            link : function(scope, element, attrs, leftBarCtrl) {
                scope.ctrl = leftBarCtrl;
                scope.$watch('ctrl.selected', function(newValue, oldValue) {
                    if ((newValue && !oldValue) || (!newValue && oldValue)) {
                        if (typeof scope.fn === 'function') {
                            scope.fn(newValue);
                        }
                    }
                });
            }
        }
    } ]);
});
