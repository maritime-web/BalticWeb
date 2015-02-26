(function () {
    "use strict";

    var module = angular.module('embryo.sar.service', []);

    module.service('SarService', function ($http) {
        var selectedSar;
        var listeners = {};

        function notifyListeners() {
            for (var key in listeners) {
                listeners[key](selectedSar);
            }
        }

        var service = {
            selectedSar: function (sar) {
                selectedSar = sar;
                notifyListeners();
            },
            registerSelectedSarListener: function (name, fn) {
                listeners[name] = fn;

                if (selectedSar) {
                    fn(selectedSar);
                }
            }
        };

        return service;
    });
})();
