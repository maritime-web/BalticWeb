/*
 * Dependencies:
 * 
 * aisview.js
 * aisviewUI.js
 * position.js
 * ....
 */

(function() {
    "use strict";

    var storageModule = angular.module('embryo.storageServices', []);

    storageModule.factory('SessionStorageService', function() {
        return {
            getItem : function(key, callback, remoteCall) {
                var dataStr = sessionStorage.getItem(key);
                if (!dataStr || dataStr === 'undefined') {
                    if (remoteCall) {
                        var onSuccess = function(data) {
                            // only cache objects with values
                            if (data && Object.keys(data).length > 0) {
                                var dataStr = JSON.stringify(data);
                                sessionStorage.setItem(key, dataStr);
                            }
                            callback(data);
                        };

                        remoteCall(onSuccess);
                    } else {
                        callback(null);
                    }
                } else {
                    var data = JSON.parse(dataStr);
                    callback(data);
                }
            },
            setItem : function(key, data) {
                var dataStr = JSON.stringify(data);
                sessionStorage.setItem(key, dataStr);
            },
            removeItem : function(key) {
                sessionStorage.removeItem(key);
            }
        };
    });

    storageModule.factory('LocalStorageService', function() {
        return {
            getItem : function(key, callback, remoteCall) {
                var dataStr = localStorage.getItem(key);
                if (!dataStr || dataStr === 'undefined') {
                    if (remoteCall) {
                        var onSuccess = function(data) {
                            // only cache objects with values
                            if (data && Object.keys(data).length > 0) {
                                var dataStr = JSON.stringify(data);
                                localStorage.setItem(key, dataStr);
                            }
                            callback(data);
                        };
                        remoteCall(onSuccess);
                    } else {
                        callback(null);
                    }
                } else {
                    callback(JSON.parse(dataStr));
                }
            },
            setItem : function(key, data) {
                var dataStr = JSON.stringify(data);
                localStorage.setItem(key, dataStr);
            }
        };
    });
}());
