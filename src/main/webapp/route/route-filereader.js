angular.module('maritimeweb.route').factory("fileReader",
    ["$q", "$log", function ($q, $log) {

        var onLoad = function (reader, deferred, scope) {
            return function () {
                scope.$apply(function () {
                    deferred.resolve(reader.result);
                });
            };
        };

        var onError = function (reader, deferred, scope) {
            return function () {
                scope.$apply(function () {
                    deferred.reject(reader.result);
                });
            };
        };

        var onProgress = function (reader, scope) {
            return function (event) {
                scope.$broadcast("fileProgress",
                    {
                        total: event.total,
                        loaded: event.loaded
                    });
            };
        };

        var getReader = function (deferred, scope) {
            var reader = new FileReader();
            reader.onload = onLoad(reader, deferred, scope);
            reader.onerror = onError(reader, deferred, scope);
            reader.onprogress = onProgress(reader, scope);
            return reader;
        };

        var transformRtzXMLtoJSON = function (data) {
            var parser = new X2JS();
            var json = parser.xml_str2json(data);
            return json;
        };

        var readAsDataURL = function (file, scope) {
            var deferred = $q.defer();
            var reader = getReader(deferred, scope);
            //reader.readAsDataURL(file);
            reader.readAsBinaryString(file);

            return deferred.promise;
        };

        return {
            readAsDataUrl: readAsDataURL,
            transformRtzXMLtoJSON: transformRtzXMLtoJSON
        };
    }
    ]);
angular.module('maritimeweb.route').directive("ngFileSelect", function () {
    return {
        link: function ($scope, el) {
            el.bind("change", function (e) {
                $scope.file = (e.srcElement || e.target).files[0];
                $scope.getFile();
            })
        }
    }
});
