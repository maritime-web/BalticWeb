// Constructs the embryo namespace
// Includes message panel and event bus.

embryo = {};

embryo.baseMap = "world_merc";
// embryo.baseMap = "osm";

embryo.baseUrl = "";
// embryo.baseUrl = "http://localhost:8080/arcticweb/";
embryo.baseUrlForAngularResource = "";
// embryo.baseUrlForAngularResource = "http://localhost\\:8080/arcticweb/";

embryo.projection = "EPSG:900913";

embryo.loadFrequence = 15 * 60 * 1000;
embryo.defaultTimeout = 30000;

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

        $("ng-include").each(function(k, v) {
            var l = $(v).html().trim().length;
            if (l == 0)
                allIncludesLoaded = false;
        });

        $("div[x-ng-include]").each(function(k, v) {
            var l = $(v).html().trim().length;
            if (l == 0)
                allIncludesLoaded = false;
        });

        if (allIncludesLoaded) {
            clearInterval(interval);
            embryo.eventbus.fireEvent(embryo.eventbus.EmbryoReadyEvent());
        }

    }, 100);
});

embryo.messagePanel = {
    render : function(id, msg) {
        switch (msg.type) {
        case "error":
            setTimeout(function() {
                embryo.messagePanel.remove(id);
            }, 30000);
            return "<div id=" + id + "><div class='alert alert-error' style=display:inline-block>" + msg.text
                    + "</div></div>";
        case "success":
            setTimeout(function() {
                embryo.messagePanel.remove(id);
            }, 10000);
            return "<div id=" + id + "><div class='alert alert-success' style=display:inline-block>" + msg.text
                    + "</div></div>";
        default:
            return "<div id=" + id + "><div class='alert' style=display:inline-block>" + msg.text + "</div></div>";
        }
    },
    show : function(msg) {
        var html = $("#messagePanel").html();
        var id = ("_" + Math.random()).replace(".", "_");
        html += embryo.messagePanel.render(id, msg);
        $("#messagePanel").html(html);
        return id;
    },
    replace : function(id, msg) {
        embryo.messagePanel.remove(id);
        var html = $("#messagePanel").html();
        html += embryo.messagePanel.render(id, msg);
        $("#messagePanel").html(html);
        return id;
    },
    remove : function(id) {
        $("#" + id).remove();
    }
};

embryo.ErrorService = {
    statusTxt : {
        400 : "Bad request",
        401 : "Unauthorized",
        402 : "Payment Required ",
        403 : "Forbidden",
        404 : "Not Found",
        405 : "Method Not Allowed",
        406 : "Not Acceptable",
        407 : "Proxy Authentication Required",
        408 : "Request Timeout",
        409 : "Conflict",
        410 : "Gone",
        415 : "Unsupported Media Type",
        419 : "Authentication Timeout",
        500 : "Internal Server Error",
        501 : "Not Implemented",
        502 : "Bad Gateway",
        503 : "Service Unavailable",
        504 : "Gateway Timeout",
        511 : "Network Authentication Required",
        522 : "Connection timed out",
        524 : "A timeout occurred"
    },

    extractError : function(data, status, config) {
        var message = status + " " + this.statusTxt[status] + ". ";
        var texts = [ message ];
        if (data instanceof Array) {
            texts = texts.concat(data);
        }
        return texts;
    }

};

embryo.controllers = {};

$(function() {
    if (window.applicationCache) {
        window.applicationCache.addEventListener('updateready', function(e) {
            if (window.applicationCache.status == window.applicationCache.UPDATEREADY) {
                window.location.reload();
            }
        })
    }
})
