embryo.focusGroup = function(id, handler) {
    $(document).on("focusGroup"+id, handler);
}

embryo.unfocusGroup = function(id, handler) {
    $(document).on("unfocusGroup"+id, handler);
}

$(function() {
    var hash = window.location.hash;

    $("#navigationBar li").removeClass("active");
    
    var selectedId = null;
    
    if (hash.indexOf("#/") == 0)
        selectedId = hash.substring(2);
    
    var groups = [];
    
    $("#navigationBar ul a").each(function(k, v) {
        var j = $(v).attr("href");
        
        if (j.indexOf("map.html#/") == 0) {
            if (j.substring(10) == selectedId) {
                $(v).parents("li").addClass("active");
            }
        }
        
    });
});

embryo.ready(function() {
    function updateNavs() {
        embryo.mapPanel.selectControl.unselectAll();
        var hash = window.location.hash;

        $("#navigationBar li").removeClass("active");
        
        var selectedId = null;

        if (hash.indexOf("#/") == 0)
            selectedId = hash.substring(2);

        var groups = [];

        $("#navigationBar ul a").each(function(k, v) {
            var j = $(v).attr("href");

            if (j.indexOf("map.html#/") == 0) {
                groups.push(j.substring(10));
                if (j.substring(10) == selectedId) {
                    $(v).parents("li").addClass("active");
                }

            }

        });

        for (var i in groups) {
            var j = groups[i];
                
            if (j == selectedId) {
                $(document).trigger($.Event("focusGroup"+j));
            } else {
                $(document).trigger($.Event("unfocusGroup"+j));
            }
        }
    }

    // angular kills hashchange - temp work around

    $("#navigationBar a").click(function() {
        setTimeout(updateNavs, 100);
    });

    // $(window).on('hashchange', updateNavs);

    embryo.authenticated(function() {
        setTimeout(updateNavs, 500); // indtil vi finder paa noget bedre
    });
});




