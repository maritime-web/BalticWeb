/*globals angular, moment, jQuery */
/*jslint vars:true */

(function() {
    "use strict";

    var module = angular.module('embryo.datepicker', []);

    module.directive('datetimepicker', function() {
        return {
            require : '^ngModel',
            restrict : 'E',
            replace : true,
            template : '<div class="input-group date" data-date-format="YYYY-MM-DD hh:mm">'
                    + '<input type="text" class="input-sm form-control" data-format="yyyy-MM-dd hh:mm" />'
                    + '<span class="input-group-addon">' + ' <span class="fa fa-calendar"></span>'
                    + '</span>' + '</div>',
            //              
            // '<div>' +
            // '<input type="text" readonly data-date-format="yyyy-mm-dd hh:ii"
            // name="recipientDateTime" data-date-time required>'+
            // '</div>',
            link : function(scope, element, attrs, ngModelController) {
                $(element).datetimepicker({
                    language : 'en-US',
                    pick12HourFormat : false,
                    useSeconds : false,
                    useCurrent : true,
                    showToday : true
                });
                var picker = $(element).data('DateTimePicker');
                
                picker.setDate(new Date());

                ngModelController.$formatters.push(function(modelValue) {
                    if (!modelValue) {
                        picker.setDate(null);
                    } else {
                        picker.setDate(new Date(modelValue));
                    }
                });

                ngModelController.$parsers.push(function(valueFromInput) {
                    if (!picker.getDate()) {
                        return null;
                    } 
                    return Date.parse(picker.getDate());
                });

                element.bind('changeDate', function(e) {
                    var val = $(element).find('input').val();
                    ngModelController.$setViewValue(val);
                });
                
                element.bind('blur change', function() {
                    var millis = null;
                    var date = picker.getDate();
                    if(date) {
                        millis = Date.parse(date);
                    }
                    console.log('DATE: ' + date);
                    ngModelController.$modelValue = millis;
                    ngModelController.$render();
                });
            }
        };
    });

}());
