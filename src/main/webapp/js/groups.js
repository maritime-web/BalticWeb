embryo.focusGroup = function(id, handler) {
    $(document).on("focusGroup"+id, handler);
}

embryo.unfocusGroup = function(id, handler) {
    $(document).on("unfocusGroup"+id, handler);
}

$(function() {
    function updateNavs() {
        var hash = window.location.hash;
        if (hash == "") hash = "#vessels";
        $("#navigationBar li").removeClass("active");
        $("#navigationBar a[href=\""+hash+"\"]").parents("li").addClass("active");
        
        var groups = [];

        $("#navigationBar ul a").each(function(k, v) {
            groups.push($(v).attr("href").substring(1));
        });

        var selectedId = hash.substring(1);

        for (var i in groups) {
            var j = groups[i];
                
            if (j == selectedId) {
                $(document).trigger($.Event("focusGroup"+j));
            } else {
                $(document).trigger($.Event("unfocusGroup"+j));
            }
        }
    }

    $(window).on('hashchange', updateNavs);
    embryo.authenticated(function() {
        setTimeout(updateNavs, 500); // indtil vi finder paa noget bedre
    });
});




