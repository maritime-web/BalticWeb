(function() {
    "use strict";
    
    embryo.course = {};

    embryo.course.parse = function (value) {
        if(value === ""){
            return null;
        }
        if(!value){
            return value;
        }
        return parseInt(value, 10);
    };

    var module = angular.module('embryo.course', []);

    function courseDirective(formatter1, parser) {
        function formatter(value) {
            if (value || value === 0) return formatter1(value);
            return null;
        }

        return {
            require : '^ngModel',
            restrict : 'A',
            link : function(scope, element, attr, ngModelController) {
                ngModelController.$formatters.push(function(modelValue) {
                    if (!modelValue) {
                        return null;
                    }
                    return formatter(modelValue);
                });

                ngModelController.$parsers.push(function(valueFromInput) {
                    try {
                        return parser(valueFromInput);
                    } catch (e) {
                        return null;
                    }
                });

                element.bind('change', function(event) {
                    if (!ngModelController.$modelValue) {
                        ngModelController.$viewValue = null;
                    }
                    ngModelController.$viewValue = formatter(ngModelController.$modelValue);
                    ngModelController.$render();
                });
            }
        };
    }
    
    module.directive('embryoCourse', function() {
        return courseDirective(formatCourse, embryo.course.parse);
    });


}());
