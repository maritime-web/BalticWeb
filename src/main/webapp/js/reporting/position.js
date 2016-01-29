(function() {
    "use strict";

    embryo.position = {};

    embryo.position.parseLatitude = function (value) {
        if ($.trim(value).indexOf(" ") < 0) {
            var parsed = parseFloat(value);
            if (parsed == value) {
                return parsed;
            }
        }
        var parts = splitFormattedPos(value);
        return parseLat(parts[0], parts[1], parts[2]);
    };

    embryo.position.parseLongitude = function (value) {
        if ($.trim(value).indexOf(" ") < 0) {
            var parsed = parseFloat(value);
            if (parsed == value) {
                return parsed;
            }
        }
        var parts = splitFormattedPos(value);
        return parseLon(parts[0], parts[1], parts[2]);
    };

    function splitFormattedPos(posStr) {
        var parts = [];
        parts[2] = posStr.substring(posStr.length - 1);
        posStr = posStr.substring(0, posStr.length - 1);
        var posParts = $.trim(posStr).split(" ");
        if (posParts.length != 2) {
            throw "Format exception";
        }
        parts[0] = posParts[0];
        parts[1] = posParts[1];
        return parts;
    }

    function parseString(str){
        str = $.trim(str);
        if (str == null || str.length == 0) {
            return null;
        }
        return str;
    }

    function parseLat(hours, minutes, northSouth) {
        var h = parseInt(hours, 10);
        var m = parseFloat(minutes);
        var ns = parseString(northSouth);
        if (h == null || m == null || ns == null) {
            throw "Format exception";
        }
        ns = ns.toUpperCase();
        if (!(ns == "N") && !(ns == "S")) {
            throw "Format exception";
        }
        var lat = h + m / 60.0;
        if (ns == "S") {
            lat *= -1;
        }
        return lat;
    }

    function parseLon(hours, minutes, eastWest) {
        var h = parseInt(hours, 10);
        var m = parseFloat(minutes);
        var ew = parseString(eastWest);
        if (h == null || m == null || ew == null) {
            throw "Format exception";
        }
        ew = ew.toUpperCase();        
        if (!(ew == "E") && !(ew == "W")) {
            throw "Format exception";
        }
        var lon = h + m / 60.0;
        if (ew == "W") {
            lon *= -1;
        }
        return lon;
    }

    var module = angular.module('embryo.position', []);

    function positionDirective(formatter1, parser) {
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

    module.directive('latitude', function() {
        return positionDirective(formatLatitude, embryo.position.parseLatitude);
    });

    module.directive('longitude', function() {
        return positionDirective(formatLongitude, embryo.position.parseLongitude);
    });

}());
