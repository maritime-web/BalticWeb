function createIceEggHtml(p) {
    function s(v) {
        return Math.round(v * p.size / 200.0);
    }

    function f(v) {
        if (v == -9) return "&middot;";
        return v;
    }

    var html = "<div style=\"background-image:url('egg.png'); width:"+s(140)+"px; height: "+s(200)+"px; "+
        "background-size: 100% 100%; text-align:center; font-family:sans-serif; font-size: "+s(20)+"px;\">";

    html += "<div style=\"height: "+s(50)+"px; padding-top: "+s(25)+"px\">"+f(p.CT)+"</div>";
    html += "<div style=\"height: "+s(45)+"px; padding-top: "+s(15)+"px\">"+f(p.CA)+" "+f(p.CB)+" "+f(p.CC)+"</div>"
    html += "<div style=\"height: "+s(15)+"px; padding-top: "+s(5)+"px\">"+f(p.SA)+" "+f(p.SB)+" "+f(p.SC)+"</div>";
    html += "<div style=\"height: "+s(15)+"px; padding-top: "+s(5)+"px\">"+f(p.FA)+" "+f(p.FB)+" "+f(p.FC)+"</div>";
    html += "</div>";
    return html;
}

(function ( $ ) {
    $.fn.iceEgg = function(p) {
        function s(v) {
            return Math.round(v * p.size / 200.0);
        }

        function f(v) {
            if (v == -9) return "&middot;";
            return v;
        }

        var html = "<div style=\"background-image:url('egg.png'); width:"+s(140)+"px; height: "+s(200)+"px; "+
            "background-size: 100% 100%; text-align:center; font-family:sans-serif; font-size: "+s(20)+"px;\">";

        html += "<div style=\"height: "+s(50)+"px; padding-top: "+s(25)+"px\">"+f(p.CT)+"</div>";
        html += "<div style=\"height: "+s(45)+"px; padding-top: "+s(15)+"px\">"+f(p.CA)+" "+f(p.CB)+" "+f(p.CC)+"</div>"
        html += "<div style=\"height: "+s(15)+"px; padding-top: "+s(5)+"px\">"+f(p.SA)+" "+f(p.SB)+" "+f(p.SC)+"</div>";
        html += "<div style=\"height: "+s(15)+"px; padding-top: "+s(5)+"px\">"+f(p.FA)+" "+f(p.FB)+" "+f(p.FC)+"</div>";
        html += "</div>";

        console.log(html);
        this.html(html);
        return this;
    };
}( jQuery ));


$(function() {
    $("#newEgg").iceEgg({
        size: 75, observationDate: "2013-07-19",
        "FA":"3","SC":"-9","OBS_METHOD":"6","AREA":578137910,"POLY_TYPE":"I","CT":"12","FB":"2","FC":"1","CB":"-9","AREA_ID":"901","CA":"-9","PERIMETER":236116,"CF":"19","CC":"-9","CD":"-9","CN":"-9","HATCH":"8","SA":"95","SB":"87"}
    );
})