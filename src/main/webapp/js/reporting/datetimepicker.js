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
            template : '<div class="input-group date" data-date-format="YYYY-MM-DD HH:mm">'
                    + '<input type="text" class="input-sm form-control" />' + '<span class="input-group-addon">'
                    + ' <span class="fa fa-calendar"></span>' + '</span>' + '</div>',
            //              
            // '<div>' +
            // '<input type="text" readonly data-date-format="yyyy-mm-dd hh:ii"
            // name="recipientDateTime" data-date-time required>'+
            // '</div>',
            link : function(scope, element, attrs, ngModelController) {
                $(element).datetimepicker({
                    language : 'da',
                    pick12HourFormat : false,
                    useSeconds : false,
                    useCurrent : true,
                    showToday : true,
                    icons : {
                        time : 'fa fa-clock-o',
                        date : 'fa fa-calendar',
                        up : 'fa fa-chevron-up',
                        down : 'fa fa-chevron-down'
                    }
                });
                var picker = $(element).data('DateTimePicker');

                ngModelController.$formatters.push(function(modelValue) {
                    var adjustedDate
                    if (!modelValue) {
                        adjustedDate = null;
                        picker.setDate(null);
                    } else {
                        adjustedDate = adjustDateForUTC(modelValue);
                        picker.setDate(moment(adjustedDate).format('YYYY-MM-DD HH:mm'));
                    }
                    return adjustedDate
                });

                ngModelController.$parsers.push(function(valueFromInput) {
                    if (!picker.getDate()) {
                        return null;
                    }
                    var value = adjustDateForLocal(picker.getDate().valueOf());
                    return value;
                });

                element.bind('changeDate', function(e) {
                    var val = $(element).find('input').val();
                    ngModelController.$setViewValue(val);

                    if (!scope.$$phase) {
                        scope.$apply(function () {
                        });
                    }
                });

                element.bind('blur change dp.change dp.hide', function() {
                    var millis = null;
                    var date = picker.getDate();
                    if (date) {
                        millis = adjustDateForLocal(date.valueOf());
                    }

                    ngModelController.$setViewValue(millis);
                    ngModelController.$modelValue = millis;
                    ngModelController.$render();

                    if (!scope.$$phase) {
                        scope.$apply(function () {
                        });
                    }

                });
            }
        };
    });

}());
