(function() {
    var module = angular.module('embryo.ice', []);

    function convert(data, delta, exponent) {
        function convertPolygon(input) {
            var result = [];

            var factor = Math.pow(10, exponent);

            current = { x: 0, y: 0 }

            for (var i in input) {
                var value = {
                    x: input[i].x / factor,
                    y: input[i].y / factor
                }

                if (delta) {
                    current = {
                        x: value.x + current.x,
                        y: value.y + current.y
                    }
                    result.push(current);
                } else {
                    result.push(value);
                }
            }

            return result;
        }

        for (var k in data) {
            for (var i in data[k].fragments) {
                for (var j in data[k].fragments[i].polygons) {
                    data[k].fragments[i].polygons[j] = convertPolygon(data[k].fragments[i].polygons[j])
                }
            }
        }
    }

    module.service('IceService', function() {
        return {
            list: function(callback) {
                $.ajax({
                    url: embryo.baseUrl+"rest/ice/list",
                    data: { },
                    success: function(data) {
                        callback(null, data);
                    },
                    error: function(error) {
                        callback(error);
                    }
                });
            },
            shapes: function(argument, callback) {
                var r;
                if (typeof(argument) == "string") {
                    r = {
                        ids: argument,
                        delta: embryo.ice.delta,
                        exponent: embryo.ice.exponent
                    }
                } else {
                    r = argument;
                }

                $.ajax({
                    url: embryo.baseUrl+"rest/shapefile/multiple/" + r.ids,
                    data: {
                        delta: r.delta,
                        exponent: r.exponent
                    },
                    success: function(data) {
                        convert(data, r.delta, r.exponent);
                        callback(null, data);
                    },
                    error: function(error) {
                        callback(error);
                    }
                });
            }
        };
    });

    embryo.ice = {
        delta: true,
        exponent: 3
    };

    module.run(function(IceService) {
        embryo.ice.service = IceService;
    })
})()