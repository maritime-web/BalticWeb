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
                if (attrs.openFirst) {
                    $timeout(function() {
                        var headers = element.find("div h4 a");
                        headers.first().click();
                    }, 5);
                }
            }
        };
    } ]);

    module.directive('eLeftBarOpenOnInit', [ '$timeout', function($timeout) {
        return {
            require : '^eLeftBar',
            restrict : 'A',
            link : function(scope, element, attrs) {
                // open first
                $timeout(function() {
                    $(element.get(0)).find("h4 a.accordion-toggle").click();
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
                var aElem = $(element).find("div h4 a.accordion-toggle");
                aElem.on('click', function(e) {
                    e.preventDefault();
                });
                scope.ctrl = leftBarCtrl;
                scope.$watch('ctrl.selected', function(newValue, oldValue) {
                    if ((newValue && !oldValue) || (!newValue && oldValue)) {
                        // Force to execute click with a timeout to avoid
                        // $digest in progress exception.
                        // Timeout of 250 ms used for new value to force open
                        // close animation.
                        var ts = newValue ? 250 : 5;
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


    module.directive('eLeftBarContentChange', ['$timeout', function($timeout) {
        return {
            require : '^eLeftBar',
            restrict : 'A',
            link : function(scope, element, attrs) {
                scope.observed = {};

                attrs.$observe('eLeftBarContentChange', function(newValue) {
                    scope.observed.value = !newValue ? null : angular.fromJson(newValue)
                });

                scope.$watch('observed.value', function(newValue, oldValue) {
                    if (newValue) {
                        $timeout(function () {
                            $(element.get(0)).find(".collapsing").css("height", "auto");
                            $(element.get(0)).find(".collapsing").addClass("collapse in").removeClass("collapsing");
                        }, 500);
                    }
                }, true);
            }
        }
    } ]);
});
