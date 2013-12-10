/*globals angular, moment, jQuery */
/*jslint vars:true */

/**
 * @license angular-bootstrap-datetimepicker  v0.1.5
 * (c) 2013 Knight Rider Consulting, Inc. http://www.knightrider.com
 * License: MIT
 */

/**
 * 
 * @author Dale "Ducky" Lotts
 * @since 2013-Jul-8
 */

(function() {
    "use strict";

    var module = angular.module('embryo.datepicker', []);

    module.directive('datetimepicker', function() {
        return {
            require : '^ngModel',
            restrict : 'E',
            replace : true,
            template : '<div class="input-append date">'
                    + '<input type="text" class="input-medium" data-format="yyyy-MM-dd hh:mm" />'
                    + '<span class="add-on">' + ' <i data-time-icon="icon-time" data-date-icon="icon-calendar"></i>'
                    + '</span>' + '</div>',
            //              
            // '<div>' +
            // '<input type="text" readonly data-date-format="yyyy-mm-dd hh:ii"
            // name="recipientDateTime" data-date-time required>'+
            // '</div>',
            link : function(scope, element, attrs, ngModelController) {
                $(element).datetimepicker({
                    language : 'en-US',
                    pickSeconds : false
                });

                var picker = $(element).data('datetimepicker');

                ngModelController.$formatters.push(function(modelValue) {
                    if (!modelValue) {
                        picker.setLocalDate(null);
                    } else {
                        picker.setLocalDate(new Date(modelValue));
                    }
                });

                ngModelController.$parsers.push(function(valueFromInput) {
                    if (!picker.getLocalDate()) {
                        return null;
                    } 
                    return picker.getLocalDate().getTime();
                });

                element.bind('changeDate', function(e) {
                    var val = $(element).find('input').val();
                    ngModelController.$setViewValue(val);
                });
                
                element.bind('blur change', function() {
                    var millis = !picker.getLocalDate() ? null : picker.getLocalDate().getTime();
                    ngModelController.$modelValue = millis;
                    ngModelController.$render();
                });
            }
        }
    });

}());
