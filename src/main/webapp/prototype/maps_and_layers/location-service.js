angular.module('maritimeweb.location.service',[]).service('locationService', function ($window, $q) {
    var promise = null;
    console.log("location SErvice");
    this.get = function () {
        if (!promise) {
            promise = $q(function (resolve, reject) {
                $window.navigator.geolocation.getCurrentPosition(function (position) {
                    console.log('Got current position', position.coords);
                    resolve({
                        'latitude': position.coords.latitude,
                        'longitude': position.coords.longitude
                    });
                }, function () {
                    reject('Unable to get current position');
                });
            });
        }
        return promise;
    };
});