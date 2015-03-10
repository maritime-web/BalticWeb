(function () {
    "use strict";

    var module = angular.module('embryo.sar.service', []);

    embryo.sar = {}
    // A way to create an enumeration like construction in JavaScript
    embryo.sar.types = Object.freeze({"RapidResponse": "rr", "DatumPoint": "dp", "DatumLine": "dl", "BackTrack": "bt"})

    function Leeway(x, y, divergence, text) {
        this.x = x;
        this.y = y;
        this.divergence = divergence;
        this.text = text;

        // TODO create object prototype
        this.calculate = function () {

        }
    }

    var searchObjectTypes = [];
    searchObjectTypes.push(Object.freeze(new Leeway(0.011, 0.068, 30, "Person in water (PIW)")));
    searchObjectTypes.push(Object.freeze(new Leeway(0.029, 0.039, 20, "Raft (4-6 person), unknown drift anker status")));
    searchObjectTypes.push(Object.freeze(new Leeway(0.018, 0.027, 16, "Raft (4-6 person) with drift anker")));
    searchObjectTypes.push(Object.freeze(new Leeway(0.038, -0.041, 20, "Raft (4-6 person) without drift anker")));
    searchObjectTypes.push(Object.freeze(new Leeway(0.036, -0.086, 14, "Raft (15-25 person), unknown drift anker status")));
    searchObjectTypes.push(Object.freeze(new Leeway(0.031, -0.070, 12, "Raft (15-25 person) with drift anker")));
    searchObjectTypes.push(Object.freeze(new Leeway(0.039, -0.060, 12, "Raft (15-25 person) without drift anker")));
    searchObjectTypes.push(Object.freeze(new Leeway(0.034, 0.040, 22, "Dinghy (Flat buttom)")));
    searchObjectTypes.push(Object.freeze(new Leeway(0.030, 0.080, 15, "Dinghy (Keel)")));
    searchObjectTypes.push(Object.freeze(new Leeway(0.017, undefined, 15, "Dinghy (Capsized)")));
    searchObjectTypes.push(Object.freeze(new Leeway(0.011, 0.240, 15, "Kayak with Person")));
    searchObjectTypes.push(Object.freeze(new Leeway(0.020, undefined, 15, "Surfboard with Person")));
    searchObjectTypes.push(Object.freeze(new Leeway(0.023, 0.100, 12, "Windsurfer with Person. Mast and sail in water")));
    searchObjectTypes.push(Object.freeze(new Leeway(0.030, undefined, 48, "Sailboat (Long keel)")));
    searchObjectTypes.push(Object.freeze(new Leeway(0.040, undefined, 48, "Sailboat (Fin keel)")));
    searchObjectTypes.push(Object.freeze(new Leeway(0.069, -0.080, 19, "Motorboat")));
    searchObjectTypes.push(Object.freeze(new Leeway(0.042, undefined, 48, "Fishing Vessel")));
    searchObjectTypes.push(Object.freeze(new Leeway(0.040, undefined, 33, "Trawler")));
    searchObjectTypes.push(Object.freeze(new Leeway(0.028, undefined, 48, "Coaster")));
    searchObjectTypes.push(Object.freeze(new Leeway(0.020, undefined, 10, "Wreckage")));

    module.service('SarService', function ($http) {
        var selectedSar;
        var listeners = {};

        function notifyListeners() {
            for (var key in listeners) {
                listeners[key](selectedSar);
            }
        }

        var service = {
            sarTypes: function () {
                return sarTypes;
            },
            searchObjectTypes: function () {
                return searchObjectTypes;
            },
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
