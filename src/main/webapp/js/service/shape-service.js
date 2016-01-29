(function() {
    var module = angular.module('embryo.shape', []);

    module.service('ShapeService', ['$http', function($http) {
        function convert(data, delta, exponent) {
            function convertPolygon(input) {
                var result = [];

                var factor = Math.pow(10, exponent);

                current = {
                    x : 0,
                    y : 0
                };

                for ( var i in input) {
                    var value = {
                        x : input[i].x / factor,
                        y : input[i].y / factor
                    };

                    if (delta) {
                        current = {
                            x : value.x + current.x,
                            y : value.y + current.y
                        };
                        result.push(current);
                    } else {
                        result.push(value);
                    }
                }

                return result;
            }

            function convertShape(shape){
                for ( var i in shape.fragments) {
                    for ( var j in shape.fragments[i].polygons) {
                        shape.fragments[i].polygons[j] = convertPolygon(shape.fragments[i].polygons[j]);
                    }
                }
            }
            
            if(Object.prototype.toString.call( data ) === '[object Array]'){
                for ( var k in data) {
                    convertShape(data[k]);
                }
            }else{
                convertShape(data);
            }
        }

        function retrieveShapes(url, name, arguments, success, error){
            var r = (typeof (arguments) != "object") ? {} : arguments;
            if (!r.delta) {
                r.delta = embryo.shape.delta;
            }

            $http.get(url, {
                timeout : embryo.defaultTimeout,
                params : r
            }).success(function(data) {
                convert(data, r.delta, !data.exponent ? r.exponent : data.exponent);
                success(data);
            }).error(function(data, status, headers, config) {
                if(error){
                    error(embryo.ErrorService.errorStatus(data, status, "requesting shape data"), status);
                }
            });
        };
        return {
            staticShapes : function(name, arguments, success, error) {
                var url = embryo.baseUrl + "rest/shapefile/static/multiple/" + name;
                retrieveShapes(url, name, arguments, success, error);                
            },
            shapes : function(name, arguments, success, error) {
                var url = embryo.baseUrl + "rest/shapefile/multiple/" + name;
                retrieveShapes(url, name, arguments, success, error);                
            },
            shape : function(name, arguments, success, error) {
                var url = embryo.baseUrl + "rest/shapefile/single/" + name;
                retrieveShapes(url, name, arguments, success, error);                
            }

        };
    }]);

    embryo.shape = {
        delta : true
    };

    module.run(function(ShapeService) {
        embryo.shape.service = ShapeService;
    });
})();
