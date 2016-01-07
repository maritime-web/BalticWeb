// Constructs the embryo namespace
// Includes message panel and event bus.

embryo = {};

// fn to add blank (noOp) function for all console methods
var key, names = [ "log" ];
embryo.logger = {};

for (key in names) {
    if (window.console && window.console[names[key]]) {
        embryo.logger[names[key]] = function() {
            return window.console[names[key]](arguments);
        };
    } else {
        embryo.logger[names[key]] = function() {
        };
    }
}

embryo.baseMap = "world_merc";
// embryo.baseMap = "osm";

embryo.baseUrl = "";
embryo.baseUrlForAngularResource = "";

embryo.projection = "EPSG:900913";

embryo.loadFrequence = 15 * 60 * 1000;
embryo.defaultTimeout = 60000;

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
    };
};

embryo.eventbus.EmbryoReadyEvent = function() {
    var event = jQuery.Event("EmbryoReadyEvent");
    return event;
};

embryo.eventbus.registerShorthand(embryo.eventbus.EmbryoReadyEvent, "ready");

$(function() {
    var interval = setInterval(function() {
        var allIncludesLoaded = true;

        if ($("ng-include,div[x-ng-include]").length == 0) {
            allIncludesLoaded = false;
        }

        if (allIncludesLoaded) {
            $("ng-include").each(function(k, v) {
                var html = $(v).html();
                // IE 8 doesn't support trim(), so we use the jQuery version instead
                var l = $.trim(html).length;
                if (l == 0)
                    allIncludesLoaded = false;
            });
        }

        if (allIncludesLoaded) {
            $("div[x-ng-include]").each(function(k, v) {
                var html = $(v).html();
                // IE 8 doesn't support trim(), so we use the jQuery version instead
                var l = $.trim(html).length;
                if (l == 0)
                    allIncludesLoaded = false;
            });
        }

        

        if (allIncludesLoaded) {
            function fixControlPanels(){
                $(".controlPanel a").off('click').on('click',function(e) {
                    e.preventDefault();
                });
            }
            fixControlPanels();
            setTimeout(fixControlPanels, 1000);
            
            clearInterval(interval);
            embryo.eventbus.fireEvent(embryo.eventbus.EmbryoReadyEvent());
        }
    }, 100);
}());

embryo.messagePanel = {
    render : function(id, msg) {
        switch (msg.type) {
        case "error":
            setTimeout(function() {
                embryo.messagePanel.remove(id);
            }, 30000);
            return "<div id=" + id + "><div class='alert alert-danger alert-dismissible e-small-font' style=display:inline-block data-dismiss='alert'>" + msg.text
                    + "</div></div>";
        case "success":
            setTimeout(function() {
                embryo.messagePanel.remove(id);
            }, 10000);
            return "<div id=" + id + "><div class='alert alert-success alert-dismissible e-small-font' style=display:inline-block data-dismiss='alert'>" + msg.text
                    + "</div></div>";
        default:
            return "<div id=" + id + "><div class='alert alert-warning alert-dismissible e-small-font' style=display:inline-block data-dismiss='alert'>" + msg.text + "</div></div>";
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
    },
    errorStatus : function(data, status, msg) {
        return "Server returned error code: " + status + " " + msg + ". ";
    }

};

embryo.controllers = {};

$(function() {
    if (window.applicationCache) {
        window.applicationCache.addEventListener('updateready', function(e) {
            if (window.applicationCache.status == window.applicationCache.UPDATEREADY) {
                window.location.reload();
            }
        });
    }
});

$(function() {
    "use strict";

    embryo.templateFn = function (expr) {
        return function (element, attr) {
            var ngIf = attr.ngIf;
            var value = typeof expr === 'function' ? expr(attr) : expr;

            /**
             * Make sure to combine with existing ngIf!
             */
            if (ngIf) {
                value += ' && ' + ngIf;
            }

            var inner = element.get(0);
            // we have to clear all the values because angular
            // is going to merge the attrs collection
            // back into the element after this function finishes
            angular.forEach(inner.attributes, function (attr, key) {
                attr.value = '';
            });
            attr.$set('ng-if', value);
            return inner.outerHTML;
        };
    }

    var module = angular.module('embryo.base', []);

    module.directive('focus', function($timeout) {
        return function(scope, element, attrs) {
            scope.$watch(attrs.focus, function(newValue) {
                $timeout(function() {
                    newValue && element.focus();
                }, 100);
            }, true);
        };
    });
}());
