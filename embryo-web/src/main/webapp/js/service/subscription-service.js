/*
 * Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
(function() {
    var module = angular.module('embryo.subscription.service', [ 'embryo.storageServices' ]);

    module.service('SubscriptionService', [
            '$http',
            '$interval',
            'CookieService',
            function($http, $interval, CookieService) {
                var subscriptions = {};
                var interval = 2 * 60 * 1000 * 60;

                function notifySubscribers(key, error) {
                    if (subscriptions[key]) {
                        for ( var i in subscriptions[key].callbacks) {
                            if (subscriptions[key].callbacks[i] && subscriptions[key].callbacks[i].callback) {
                                if (error) {
                                    subscriptions[key].callbacks[i].callback(error);
                                } else {
                                    if (subscriptions[key].value != null) {
                                        subscriptions[key].callbacks[i].callback(null, subscriptions[key].value);
                                    }
                                }
                            }
                        }
                    }
                }

                function getLoader(callbackConfig) {
                    var f = function() {
                        var arguments = [];
                        var key = getKey(callbackConfig);
                        if(callbackConfig.args){
                            for(var index in callbackConfig.args){
                                arguments.push(callbackConfig.args[index]);
                            }
                        }
                        arguments.push(function(value){
                            subscriptions[key].value = value;
                            notifySubscribers(key);
                        });
                        arguments.push(function(error) {
                            notifySubscribers(key, error);
                        });
                        callbackConfig.fn.apply(callbackConfig.obj, arguments);
                    };
                    return f;
                }

                function getKey(callbackConfig){
                    return callbackConfig.obj.name + callbackConfig.fn.name;
                }

                service = {
                    subscribe : function(callbackConfig) {
                        var id;
                        var key = getKey(callbackConfig);
                        if (!subscriptions[key]) {
                            subscriptions[key] = {
                                callbacks : [],
                                interval : null,
                                value : null
                            };
                        }

                        for ( var index in subscriptions[key].callbacks) {
                            if (callbackConfig.name === subscriptions[key].callbacks[index].name) {
                                id = index;
                            }
                        }
                        if (!id) {
                            id = subscriptions[key].callbacks.push(callbackConfig);
                            if (subscriptions[key].interval == null) {
                                subscriptions[key].interval = $interval(getLoader(callbackConfig), interval);
                                getLoader(callbackConfig)();
                            }
                        }
                        if (subscriptions[key].value) {
                            callbackConfig.fn(null, subscriptions[key].value);
                        }

                        var subscription = { obj : callbackConfig.obj, fn : callbackConfig.fn, id : id};
                        return subscription;
                    },
                    unsubscribe : function(unsubscription) {
                        var key = getKey(unsubscription);
                        subscriptions[key].callbacks.splice(unsubscription.id, 1);
                        var allDead = subscriptions[key].callbacks.length == 0;
                        if (allDead) {
                            clearInterval(subscriptions[key].interval);
                            delete subscriptions[key];
                        }
                    },
                    update : function(subscriptionConfig) {
                        function reload(subscriptionConfig) {
                            var key = getKey(subscriptionConfig);
                            $interval.cancel(subscriptions[key].interval);
                            subscriptions[key].interval = $interval(getLoader(subscriptionConfig), interval);
                            getLoader(subscriptionConfig)();
                        }

                        if (subscriptionConfig) {
                            reload(subscriptionConfig);
                        } else {
                            var keys = Object.keys(subscriptions);
                            for ( var index in keys) {
                                var key = keys[index];
                                if (subscriptions[key].interval && subscriptions[key].callbacks && subscriptions[key].callbacks.length > 0) {
                                    reload(subscriptions[key].callbacks[0]);
                                }
                            }

                        }
                    }
                };

                return service;
            } ]);
})();
