// Constructs the embryo namespace
// Includes message panel and event bus.

embryo = {};

embryo.baseUrl = "";
// embryo.baseUrl = "http://localhost:8080/arcticweb/";
embryo.baseUrlForAngularResource = "";
// embryo.baseUrlForAngularResource = "http://localhost\\:8080/arcticweb/";

embryo.loadFrequence = 120 * 1000;

embryo.defaultFontFamily = "'Lucida Grande', Verdana, Geneva, Lucida, Arial, Helvetica, sans-serif";

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

$(function() {
    var interval = setInterval(function() {
        var allIncludesLoaded = true;

        $("ng-include").each(function (k, v) {
            var l = $(v).html().trim().length;
            if (l == 0) allIncludesLoaded = false;
        });

        $("div[x-ng-include]").each(function (k, v) {
            var l = $(v).html().trim().length;
            if (l == 0) allIncludesLoaded = false;
        });

        if (allIncludesLoaded) {
            clearInterval(interval);
            embryo.eventbus.fireEvent(embryo.eventbus.EmbryoReadyEvent());
        }
        
    }, 100);
});

embryo.messagePanel = {
    render: function(id, msg) {
        switch (msg.type) {
        case "error":
            setTimeout(function() {
                embryo.messagePanel.remove(id);
            }, 30000);
            return "<div id="+id+"><div class='alert alert-error' style=display:inline-block>"+msg.text+"</div></div>";
        case "success":
            setTimeout(function() {
                embryo.messagePanel.remove(id);
            }, 10000);
            return "<div id="+id+"><div class='alert alert-success' style=display:inline-block>"+msg.text+"</div></div>";
        default:
            return "<div id="+id+"><div class='alert' style=display:inline-block>"+msg.text+"</div></div>";
        }
    },
    show: function(msg) {
        var html = $("#messagePanel").html();
        var id = ("_"+Math.random()).replace(".", "_");
        html += embryo.messagePanel.render(id, msg);
        $("#messagePanel").html(html);
        return id;
    },
    replace: function(id, msg) {
        embryo.messagePanel.remove(id);
        var html = $("#messagePanel").html();
        html += embryo.messagePanel.render(id, msg);
        $("#messagePanel").html(html);
        return id;
    },
    remove: function(id) {
        $("#"+id).remove();
    }
}
