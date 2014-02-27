(function() {
    var module = angular.module('embryo.ice', []);

    function convert(data, delta, exponent) {
        function convertPolygon(input) {
            var result = [];

            var factor = Math.pow(10, exponent);

            current = {
                x : 0,
                y : 0
            }

            for ( var i in input) {
                var value = {
                    x : input[i].x / factor,
                    y : input[i].y / factor
                }

                if (delta) {
                    current = {
                        x : value.x + current.x,
                        y : value.y + current.y
                    }
                    result.push(current);
                } else {
                    result.push(value);
                }
            }

            return result;
        }

        for ( var k in data) {
            for ( var i in data[k].fragments) {
                for ( var j in data[k].fragments[i].polygons) {
                    data[k].fragments[i].polygons[j] = convertPolygon(data[k].fragments[i].polygons[j])
                }
            }
        }
    }

    module.service('IceService', function($http) {
        return {
            providers : function(success, error) {
                $http.get(embryo.baseUrl + "rest/ice/provider/list", {
                    timeout : embryo.defaultTimeout
                }).success(success).error(function(data, status, headers, config) {
                    error(embryo.ErrorService.extractError(data, status, config));
                });
            },
            getSelectedProvider : function(defaultProvider) {
                var value = getCookie("selectedProvider");
                if (!value) {
                    setCookie("selectedProvider", defaultProvider, 365);
                    value = defaultProvider;
                }
                return value;
            },
            setSelectedProvider : function(rovider) {
                setCookie("selectedProvider");
            },
            listnew : function(provider, callback) {
                $.ajax({
                    url : embryo.baseUrl + "rest/ice/provider/" + provider + "/observations",
                    timeout : embryo.defaultTimeout,
                    data : {},
                    success : function(data) {
                        callback(null, data);
                    },
                    error : function(error) {
                        callback(error);
                    }
                });
            },
            list : function(callback) {
                $.ajax({
                    url : embryo.baseUrl + "rest/ice/list",
                    timeout : embryo.defaultTimeout,
                    data : {},
                    success : function(data) {
                        callback(null, data);
                    },
                    error : function(error) {
                        callback(error);
                    }
                });
            },
            shapes : function(name, arguments, callback) {
                var r = (typeof (arguments) != "object") ? {} : arguments;
                if (!r.delta) {
                    r.delta = embryo.ice.delta;
                }
                if (!r.exponent) {
                    r.exponent = embryo.ice.exponent;
                }

                $.ajax({
                    url : embryo.baseUrl + "rest/shapefile/multiple/" + name,
                    timeout : embryo.defaultTimeout,
                    data : r,
                    success : function(data) {
                        convert(data, r.delta, r.exponent);
                        callback(null, data);
                    },
                    error : function(error) {
                        callback(error);
                    }
                });
            }
        };
    });

    embryo.ice = {
        delta : true,
        exponent : 3
    };

    module.run(function(IceService) {
        embryo.ice.service = IceService;
    })
})();
