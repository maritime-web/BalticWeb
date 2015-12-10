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

    storageModule.factory('CookieService', [ function() {
        return {
            get : function(c_name) {
                var i, x, y, ARRcookies = document.cookie.split(";");
                for (i = 0; i < ARRcookies.length; i++) {
                    x = ARRcookies[i].substr(0, ARRcookies[i].indexOf("="));
                    y = ARRcookies[i].substr(ARRcookies[i].indexOf("=") + 1);
                    x = x.replace(/^\s+|\s+$/g, "");
                    if (x == c_name) {
                        var cvalue = unescape(y);
                        if (cvalue.indexOf("json:") === 0) {
                            cvalue = cvalue.substring(5);
                            cvalue = angular.fromJson(cvalue);
                        }
                        return cvalue;
                    }
                }
            },
            set : function(c_name, value, exdays) {
                var exdate = new Date();
                exdate.setDate(exdate.getDate() + exdays);
                var c_value = escape(typeof value === 'object' ? "json:" + angular.toJson(value) : value);
                c_value += ((exdays == null) ? "" : "; expires=" + exdate.toUTCString());
                document.cookie = c_name + "=" + c_value;
            }

        };
    } ]);



    storageModule.factory('LivePouch', ['$location', function ($location) {
        var liveDb = new PouchDB('embryo-live');

        // make sure this works in development environment as well as other environments
        var couchUrl = "http://localhost:5984/embryo-live"
        var url = $location.absUrl() ? $location.absUrl().toLocaleLowerCase() : "";
        if(url.indexOf("localhost:") < 0 && url.indexOf("127.0.0.1:") < 0){
            couchUrl = $location.protocol() + "://" + $location.host() + "/couchdb/embryo-live";
        }

        var sync = liveDb.sync(couchUrl, {
            live: true,
            retry: true
        })

        return liveDb;
    }]);

    storageModule.factory('UserPouch', ['$location', function ($location) {
        // make sure this works in development environment as well as other environments
        var userDB = new PouchDB('embryo-user');

        var couchUrl = "http://localhost:5984/embryo-user"
        var url = $location.absUrl() ? $location.absUrl().toLocaleLowerCase() : "";
        if(url.indexOf("localhost:") < 0 && url.indexOf("127.0.0.1:") < 0){
            couchUrl = $location.protocol() + "://" + $location.host() + "/couchdb/embryo-user";
        }

        var handler = userDB.replicate.from(couchUrl, {
            retry: true
        })

        return userDB;
    }]);


}());
