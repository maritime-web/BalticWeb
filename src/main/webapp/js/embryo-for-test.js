var embryo = {};

embryo.map = {};

// embryo.eventbus is necessary to construct a completely loose coupling between
// all components in the application.
// The implementation is based on jQuery, and as such embryo.eventbus is just a
// wrapper around jQuery enabling object oriented like code

embryo.eventbus = {};

embryo.eventbus.registerHandler = function(eventType, handler) {
    var type = eventType().type;
    $(document).on(type, handler);
};

embryo.eventbus.fireEvent = function(event) {
    $(document).trigger(event);
};

embryo.eventbus.registerShorthand = function(eventType, name) {
    embryo[name] = function(handler) {
        embryo.eventbus.registerHandler(eventType, handler);
    }
};

embryo.eventbus.EmbryoReadyEvent = function() {
    var event = jQuery.Event("EmbryoReadyEvent");
    return event;
};

embryo.eventbus.registerShorthand(embryo.eventbus.EmbryoReadyEvent, "ready");
embryo.eventbus.registerShorthand(embryo.eventbus.EmbryoReadyEvent, "authenticated");


embryo.eventbus.MapInitialized = function() {
    var event = jQuery.Event("MapInitializedEvent");
    return event;
};

embryo.eventbus.PostLayerInitialize = function () {
    var event = jQuery.Event("PostLayerInitializeEvent");
    return event;
};


embryo.eventbus.registerShorthand(embryo.eventbus.MapInitialized, "mapInitialized");
embryo.eventbus.registerShorthand(embryo.eventbus.PostLayerInitialize, "postLayerInitialization");
