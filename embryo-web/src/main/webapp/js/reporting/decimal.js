(function() {
    "use strict";
    
    embryo.decimal = {};

    embryo.decimal.parse = function (value) {
        if(!value && value != 0){
            return value;
        }
        if(value == ""){
            return null;
        }
        return parseFloat(value.replace(/,/, '.'));
    };
    
    var module = angular.module('embryo.decimal', []);

    function courseDirective(parser) {
        return {
            require : '^ngModel',
            restrict : 'A',
            link : function(scope, element, attr, ngModelController) {
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
                    ngModelController.$viewValue = ngModelController.$modelValue;
                    ngModelController.$render();
                });
            }
        };
    }
    
    module.directive('embryoDecimal', function() {
        return courseDirective(embryo.decimal.parse);
    });
}());
