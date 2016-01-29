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
(function () {
    var module = angular.module('embryo.subscription.service', [ 'embryo.storageServices' ]);

    module.service('SubscriptionService', [
        '$http',
        '$interval',
        function ($http, $interval) {
            var subscriptions = {};
            var interval = 2 * 60 * 1000 * 60;

            function notifySubscribers(key, error) {
                if (subscriptions[key]) {
                    for (var i in subscriptions[key].callbacks) {
                        if (subscriptions[key].callbacks[i]) {
                            if (error && subscriptions[key].callbacks[i].error) {
                                subscriptions[key].callbacks[i].error(error);
                            } else if (!error && subscriptions[key].value != null && subscriptions[key].callbacks[i].success) {
                                subscriptions[key].callbacks[i].success(subscriptions[key].value);
                            }
                        }
                    }
                    }
                }

            function getLoader(callbackConfig) {
                var f = function () {
                    var arguments = [];
                    var key = getKey(callbackConfig);
                    if (callbackConfig.params) {
                        for (var index in callbackConfig.params) {
                            arguments.push(callbackConfig.params[index]);
                        }
                    }
                    arguments.push(function (value) {
                        subscriptions[key].value = value;
                        notifySubscribers(key);
                    });
                    arguments.push(function (error) {
                        notifySubscribers(key, error);
                    });
                    callbackConfig.fn.apply(callbackConfig.fn, arguments);
                };
                return f;
            }

            function getKey(callbackConfig) {
                return callbackConfig.name;
            }

            service = {
                subscribe: function (callbackConfig) {
                    var id;
                    var key = getKey(callbackConfig);
                    if (!subscriptions[key]) {
                        subscriptions[key] = {
                            callbacks: [],
                            loader: null,
                            interval: null,
                            $interval: null,
                            value: null
                        };
                    }

                        var length = subscriptions[key].callbacks.length;
                        for (var index = 0; index < length; index++) {
                            if (callbackConfig.subscriber === subscriptions[key].callbacks[index].subscriber) {
                                id = index;
                            }
                        }
                    if (!id && id != 0) {
                        id = subscriptions[key].callbacks.push(callbackConfig);
                        if (subscriptions[key].$interval == null) {
                            // first subscriber for key with a callbackConfig.interval value will win
                            // following subscribers for same key value will use interval of first subscriber
                            subscriptions[key].interval = callbackConfig.interval ? callbackConfig.interval : interval;
                            subscriptions[key].loader = getLoader(callbackConfig);
                            subscriptions[key].$interval = $interval(subscriptions[key].loader, subscriptions[key].interval);
                            subscriptions[key].loader();
                        }
                    }
                    if (subscriptions[key].value) {
                        callbackConfig.success(subscriptions[key].value);
                    }

                    var subscription = { name: callbackConfig.name, id: id};
                    return subscription;
                },
                unsubscribe: function (unsubscription) {
                    var key = getKey(unsubscription);
                    subscriptions[key].callbacks.splice(unsubscription.id, 1);
                    var allDead = subscriptions[key].callbacks.length == 0;
                    if (allDead) {
                        clearInterval(subscriptions[key].$interval);
                        delete subscriptions[key];
                    }
                },
                update: function (subscriptionConfig) {
                    function reload(subscriptionConfig) {
                        var key = getKey(subscriptionConfig);
                        $interval.cancel(subscriptions[key].$interval);
                        subscriptions[key].$interval = $interval(subscriptions[key].loader, subscriptions[key].interval);
                        subscriptions[key].loader();
                    }

                    if (subscriptionConfig) {
                        reload(subscriptionConfig);
                    } else {
                        var keys = Object.keys(subscriptions);
                        for (var index in keys) {
                            var key = keys[index];
                            if (subscriptions[key].$interval && subscriptions[key].callbacks && subscriptions[key].callbacks.length > 0) {
                                reload(subscriptions[key].callbacks[0]);
                            }
                        }

                    }
                }
            };

            return service;
        } ]);

    module.run(function (SubscriptionService) {
        if (!embryo.subscription)
            embryo.subscription = {};
        embryo.subscription.service = SubscriptionService;
    })
})();
