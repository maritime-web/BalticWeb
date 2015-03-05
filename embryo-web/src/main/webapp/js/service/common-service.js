(function () {
    "use strict";

    var module = angular.module('embryo.common.service', []);

    function ProviderWrapper(provider, service) {
        var that = this;
        that.provider = provider;
        that.ViewService = service;

        that.type = function () {
            return provider.type;
        }

        that.show = function (context) {
            var viewProviders = that.ViewService.viewProviders();
            for (var index in viewProviders) {
                var vi = viewProviders[index];
                if (vi.title !== provider.title || (vi.type && provider.type ? vi.type !== provider.type : true)) {
                    that.hide(vi);
                }
            }
            provider.show(context);
        }

        that.hide = function () {
            if (provider.hide)
                provider.hide();
            if (provider.close)
                provider.close();
        }

        that.hideAll = function () {
            that.ViewService.hideAll();
        }
    }

    module.service('ViewService', ['$log', function ($log) {
        var viewProviders = [];
        var subscriptions = [];
        var that = this;

        function notifySubscribers() {
            for (var i in subscriptions) {
                if (subscriptions[i]) {
                    subscriptions[i].onNewProvider();
                }
            }
        }

        that.subscribe = function (config) {
            var id = null;
            for (var index in subscriptions) {
                var vi = subscriptions[index];
                if (vi.name === config.name) {
                    id = index;
                    $log.warn("Overwriting existing onNewProvider config with name " + config.name);
                    subscriptions[index] = config
                }
            }

            if (!id && id != 0) {
                id = subscriptions.push(config);
            }

            if (viewProviders.length > 0) {
                notifySubscribers();
            }

            var subscription = {id: id};
            return subscription;
        }

        that.unsubscribe = function (subscription) {
            subscriptions.splice(subscription.id, 1);
        },

            that.addViewProvider = function (provider) {
                for (var index in viewProviders) {
                    var vi = viewProviders[index];
                    if (vi.title === provider.title) {
                        if (vi.type && !provider.type || !vi.type && provider.type) {
                            $log.warn("Providers with same titles, but only one have a type");
                        }
                        if (vi.type && provider.type ? vi.type === info.type : true) {
                            // already added. Replace
                            $log.warn("Replacing provider " + provider.title + ". Should previously have been removed.");
                            viewProviders[index] = provider;
                            return;
                        }
                    }
                }
                viewProviders.push(new ProviderWrapper(provider, that));
                notifySubscribers();
                //embryo.eventbus.fireEvent(embryo.eventbus.VesselInformationAddedEvent());
            }

        that.viewProviders = function () {
            return viewProviders;
        }

        that.hideAll = function () {
            for (var index in viewProviders) {
                that.hide(viewProviders[index]);
            }
        }

    }]);

})();
