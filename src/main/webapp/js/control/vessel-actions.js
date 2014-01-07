embryo.vessel.actions = {
    activeItem: null,
    activeItemRow: null,
    hide: function() {
        if (this.activeItemRow) {
            if(this.isHideable(this.activeItemRow)){
                $("#"+this.activeItemRow).find("a").text("view");
            }
            $("#"+this.activeItemRow).removeClass("alert");
        }
        if (this.activeItem && this.activeItem.hide) this.activeItem.hide();
        if (this.activeItem && this.activeItem.close) this.activeItem.close();
        this.activeItem = null;
        this.activeItemRow = null;
        $(".reportingPanel").css("display", "none");
    },
    isMarkedActiveItem : function(rowId){
        return this.activeItemRow == rowId;
    },
    isHideable : function(rowId){
        return $("#"+rowId).find("a").text() == "hide";
    },
    markActiveItem: function() {
        if (this.activeItemRow) {
            $("#"+this.activeItemRow).addClass("alert");
        }
    },
    setup: function(id, items, vesselOverview, vesselDetails) {
        var html = "";
        var inTable = false;
        for (var i in items) {
            var item = items[i];
            if (item) {
                switch (typeof(item)) {
                case "string":
                    if (inTable) {
                        inTable = false;
                        html += "</table>"
                    }
                    html += "<h4>" + item + "</h4>";
                    break;
                case "object":
                    if (!inTable) {
                        inTable = true;
                        html += "<table class='table table-condensed'>"
                    }

                    html += "<tr id=" + $(id).attr("id") + "_" + i + "><th>"+item.title+"</th>";

                    var available = false;

                    if (item.available) {
                        available = item.available(vesselOverview, vesselDetails);
                    }

                    switch (available) {
                        case false:
                            html += "<td><span class='label'>NOT AVAILABLE</span></td><td></td>"
                            break;
                        case true:
                            html += "<td><span class='label label-success'>AVAILABLE</span></td><td><a href=# aid="+i+">view</a></td>"
                            break;
                        default:
                            var klass = "";
                            if (available.klass) klass = "label-"+available.klass;
                            html += "<td><span class='label "+klass+"'>"+available.text+"</span></td><td><a href=# aid="+i+">"+available.action+"</a></td>"
                            break;
                    }

                    html += "</tr>";
                    break;
                }
            }
        }
        if (inTable) {
            html += "</table>"
        }
        $(id).html(html);
        this.markActiveItem();
        var that = this;
        $("a", id).click(function(e) {
            e.preventDefault();

            var rowId = $(this).parents("tr").attr("id");
            if(that.isMarkedActiveItem(rowId) && that.isHideable(rowId)){
                that.hide();
            }else{
                that.hide();
                that.activeItemRow = rowId;
                that.activeItem = items[$(this).attr("aid")];
                if(that.activeItem && that.activeItem.hide){
                    $(this).text("hide");
                }
                that.markActiveItem();
                that.activeItem.show(vesselOverview, vesselDetails);
            }
        })
    }
}

embryo.ready(function() {
    $(".embryo-close-panel").click(function(e) {
        e.preventDefault();
        embryo.vessel.actions.hide();
    });
});

embryo.authenticated(function() {
    embryo.vessel.actions.yourVessel = [
        "ArcticWeb Reporting",
        embryo.controllers.vesselInformationEdit,
        embryo.controllers.schedule,
        embryo.controllers.greenpos,
        "Additional Information",
        embryo.additionalInformation.historicalTrack,
        embryo.additionalInformation.nearestShips,
        embryo.additionalInformation.distanceCircles,
        //embryo.additionalInformation.metoc
    ];

    embryo.vessel.actions.selectedVessel = [
        "ArcticWeb Reporting",
        embryo.controllers.vesselInformationView,
        embryo.controllers.scheduleView,
        embryo.additionalInformation.route,
        (embryo.authentication.permissions.indexOf("Reporting") >= 0) ? embryo.controllers.greenposListView : null,
        "Additional Information",
        embryo.additionalInformation.historicalTrack,
        embryo.additionalInformation.nearestShips,
        embryo.additionalInformation.distanceCircles
    ];
});
