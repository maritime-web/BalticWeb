embryo.eventbus.GroupChangedEvent = function(id) {
    var event = jQuery.Event("GroupChangedEvent");
    event.groupId = id;
    return event;
};

embryo.eventbus.registerShorthand(embryo.eventbus.GroupChangedEvent, "groupChanged");

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

        embryo.eventbus.fireEvent(embryo.eventbus.GroupChangedEvent(selectedId));
        
    }

    // $(window).on('hashchange', updateNavs);

    embryo.authenticated(function() {
        setTimeout(function() { 
            // angular kills hashchange - temp work around
            
            $("#navigationBar a").click(function() {
                setTimeout(updateNavs, 100);
            });
            
            updateNavs();
        }, 500); // indtil vi finder paa noget bedre
    });
});




