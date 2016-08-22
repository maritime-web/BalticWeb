angular.module('maritimeweb.app').config(['growlProvider', function (growlProvider) {
    growlProvider.globalTimeToLive(8000);
    growlProvider.globalPosition('bottom-right');
}]);