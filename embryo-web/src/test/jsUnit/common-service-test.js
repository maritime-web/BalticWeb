"use strict";

describe("embryo.common.service", function () {
    describe("ViewService", function () {
        var cut, $log;
        beforeEach(module('embryo.common.service'));

        beforeEach(inject(function (ViewService, _$log_) {
            cut = ViewService;
            $log = _$log_;
        }));

        afterEach(function () {
            console.log($log.debug.logs);
        });

        describe("adding subscribers", function () {

            it("should register subscribers returning a subscribtion id", function () {
                var subscriber = {
                    name: "a subscriber",
                    onNewProvider: function () {
                        //do something
                    }
                };

                var subscriptionId = cut.subscribe(subscriber);

                expect(subscriptionId).toEqual(subscriber);
            });

            it("should overwrite existing subscriber when new subscriber has same name", function () {
                var subscriber = {
                    name: "a subscriber",
                    onNewProvider: function () {
                        //do something
                    }
                };

                var newSubscriber = {
                    name: "a subscriber",
                    onNewProvider: function () {
                        //do something
                    }
                };
                spyOn(subscriber, 'onNewProvider');
                spyOn(newSubscriber, 'onNewProvider');

                cut.subscribe(subscriber);
                cut.subscribe(newSubscriber);

                cut.addViewProvider({
                    title: 'T', type: 'TY', show: function () {
                    }, close: function () {
                    }
                });

                expect(subscriber.onNewProvider).not.toHaveBeenCalled();
                expect(newSubscriber.onNewProvider).toHaveBeenCalled();
            });

            it("should reject subscribers without a configuration", function () {
                expect(cut.subscribe).toThrowError();
            });

            it("should reject subscribers without a name", function () {
                expect(function () {
                    cut.subscribe({});
                }).toThrow("Subscribers must have a name");
            });

            it("should reject subscribers without a onNewProvider variable", function () {
                expect(function () {
                    cut.subscribe({name: 'a subscriber name'});
                }).toThrow("onNewProvider function required");
            });

            it("should reject subscribers if their onNewProvider variable is not a function", function () {
                expect(function () {
                    cut.subscribe({name: 'a subscriber name', onNewProvider: 'not afunction'});
                }).toThrow("onNewProvider function required");
            });
        });

        describe("removing subscribers", function () {

            it("should remove subscriber matching a given subscribtion id", function () {
                var subscriberOne = {
                    name: "subscriber one", onNewProvider: function () {
                    }
                };
                var subscriberTwo = {
                    name: "subscriber two", onNewProvider: function () {
                    }
                };
                spyOn(subscriberOne, "onNewProvider").and.callThrough();
                spyOn(subscriberTwo, "onNewProvider").and.callThrough();
                var subscriptionIdOne = cut.subscribe(subscriberOne);
                var subscriptionIdTwo = cut.subscribe(subscriberTwo);

                cut.unsubscribe(subscriptionIdOne);
                cut.unsubscribe(subscriptionIdTwo);
                cut.addViewProvider({
                    title: 'T', type: 'TY', show: function () {
                    }, close: function () {
                    }
                });

                expect(subscriberOne.onNewProvider).not.toHaveBeenCalled();
                expect(subscriberTwo.onNewProvider).not.toHaveBeenCalled();
            });
        });

        describe("adding view providers", function () {
            it("should add a new provider to the viewProviders array", function () {
                cut.addViewProvider({
                    title: '', type: '', show: function () {
                    }, close: function () {
                    }
                });

                expect(Object.keys(cut.viewProviders()).length).toEqual(1);
            });

            it("should wrap new providers", function () {
                cut.addViewProvider({
                    title: '', type: 'TT', show: function () {
                    }, close: function () {
                    }
                });

                expect(cut.viewProviders()['TT'].type).toBeDefined();
                expect(cut.viewProviders()['TT'].show).toBeDefined();
                expect(cut.viewProviders()['TT'].hide).toBeDefined();
            });

            it("should notify subscribers when adding a new provider", function () {
                var subscriber = {
                    name: "a subscriber", onNewProvider: function () {
                    }
                };
                spyOn(subscriber, 'onNewProvider');

                cut.subscribe(subscriber);
                cut.addViewProvider({
                    title: '', type: '', show: function () {
                    }, close: function () {
                    }
                });

                expect(subscriber.onNewProvider).toHaveBeenCalled();
            });

            it("should reject view providers without a title", function () {
                expect(function () {
                    cut.addViewProvider({});
                }).toThrow("view providers must have a title");
            });

            it("should reject view providers without a type", function () {
                expect(function () {
                    cut.addViewProvider({title: ''});
                }).toThrow("view providers must have a type");
            });

            it("should reject view providers without a show function", function () {
                expect(function () {
                    cut.addViewProvider({title: '', type: '', show: ''});
                }).toThrow("provider must have a show function");
            });

            it("should reject view providers without a close or hide function", function () {
                expect(function () {
                    cut.addViewProvider({
                        title: '', type: '', show: function () {
                        }, hide: ''
                    });
                }).toThrow("provider must have a hide or close function");
            });
        });
    })
});