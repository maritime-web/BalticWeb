(function () {
    "use strict";

    var module = angular.module('embryo.common.service', []);

    function ProviderWrapper(provider, service) {
        if (provider.show === undefined || typeof provider.show !== 'function') throw "provider must have a show function";
        if ((provider.hide === undefined || typeof provider.hide !== 'function') && (provider.close === undefined || typeof provider.close !== 'function')) throw "provider must have a hide or close function";

        var that = this;
        that.provider = provider;
        that.ViewService = service;

        that.type = function () {
            return provider.type;
        };

        that.show = function (context) {
            that.ViewService.hideAll();

            provider.show(context);
        };

        that.hide = function () {
            if (provider.hide)
                provider.hide();
            if (provider.close)
                provider.close();
        };
    }

    module.service('ViewService', ['$log', function ($log) {
        var viewProviders = {};
        var subscriptions = {};
        var that = this;

        that.subscribe = function (subscriberWanabe) {
            validateSubscriber(subscriberWanabe);
            subscriptions[subscriberWanabe.name] = subscriberWanabe;
            notifySubscribers();

            $log.debug("adding subscribtion for " + subscriberWanabe.name);
            return subscriberWanabe;
        };

        function validateSubscriber(subscriberWanabe) {
            if (subscriberWanabe.name === undefined) throw "Subscribers must have a name";
            if (subscriberWanabe.onNewProvider === undefined || typeof subscriberWanabe.onNewProvider != 'function') throw "onNewProvider function required";

            if (subscriptions.hasOwnProperty(subscriberWanabe.name)) {
                $log.warn("Overwriting existing subscriber (" + subscriberWanabe.name);
            }
        }

        function notifySubscribers() {
            if (Object.keys(viewProviders).length <= 0) return;

            for (var key in subscriptions) {
                subscriptions[key].onNewProvider();
            }
        }

        that.unsubscribe = function (subscriber) {
            delete subscriptions[subscriber.name]
        };

        that.addViewProvider = function (provider) {
            validateViewProvider(provider);

            viewProviders[provider.type] = new ProviderWrapper(provider, that);
            notifySubscribers();
        };

        function validateViewProvider(provider) {
            if (provider.title === undefined) throw "view providers must have a title";
            if (provider.type === undefined) throw "view providers must have a type";

            $log.debug("Adding provider " + provider.type);
            if (viewProviders.hasOwnProperty(provider.type)) {
                $log.warn("Replacing provider identified by " + provider.type);
            }
        }

        that.viewProviders = function () {
            return viewProviders;
        };

        that.hideAll = function () {
            for (var key in viewProviders) {
                viewProviders[key].hide();
            }
        }
    }]);
})();
