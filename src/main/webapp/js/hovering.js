$(function() {
    var hoverHandlers = [];

    embryo.hover = function(handler) {
        hoverHandlers.push(handler);
    }

    embryo.highlight(function(e) {
        var lonlatCenter = e.feature.geometry.getBounds().getCenterLonLat();
        var pixelTopLeft = new OpenLayers.Pixel(0, 0);
        var lonlatTopLeft = embryo.map.internalMap.getLonLatFromPixel(pixelTopLeft);
        pixelTopLeft = embryo.map.internalMap.getPixelFromLonLat(lonlatTopLeft);
        var pixel = embryo.map.internalMap.getPixelFromLonLat(lonlatCenter);
        
        var x = pixel.x - pixelTopLeft.x + $("#map").position().left;
        var y = pixel.y - pixelTopLeft.y + $("#map").position().top;
        
        var html;
        
        for (var i in hoverHandlers) {
            var html1 = hoverHandlers[i](e);
            if (html1 != null) html = html1;
        }
        
        if (html != null) {
            $("#hoveringBox").css("top", y + "px");
            $("#hoveringBox").css("left", x + "px");
            $("#hoveringBox").html(html);
            $("#hoveringBox").css("display", "block");
        } else {
            $("#hoveringBox").css("display", "none");
        }
    });
    
    embryo.unhighlight(function(e) {
        $("#hoveringBox").css("display", "none");
    });
});
