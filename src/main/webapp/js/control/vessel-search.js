$(function() {
    var searchResultsLimit = 10;
    var lastSearch = "";

    function searchResultToHTML(vessel){
        var html = "<p><div class='btn searchResultItem' id='" + vessel.id + "'>" +
            "<div class='panelText btn-block'>" + vessel.vesselName + "</div>";

        html +=	"</div><p>";

        return html;
    }

    var latestSearch = "";

    function search(arg) {
        $("#searchResultsTop").empty();
        $("#searchResultsContainer").empty();
        $("#searchMatch").html('');

        if (arg.length > 0) {
            $("#searchLoad").css('display', 'block');

            latestSearch = arg;

            embryo.vessel.service.clientSideSearch(arg, function(searchResults) {
                var html = "";

                var s = "s";

                $("#searchResults").css('visibility', 'visible');

                if (searchResults.length <= searchResultsLimit && searchResults.length != 0) {
                    if (searchResults.length == 1) {
                        s = "";
                    }

                    $("#searchResultsTop").html("<div class='information'>Search results: </div>");

                    $.each(searchResults, function(key, value) {
                        html += searchResultToHTML(value);
                    })
                }

                $("#searchResultsContainer").html(html);

                $("#searchMatch").html(searchResults.length + " vessel" + s + " match.");

                $("#searchLoad").css('display', 'none');

                $(".searchResultItem").click(function(e) {
                    var vessel = embryo.vessel.lookupVessel($(this).attr("id"));

                    if (vessel) {
                        embryo.vessel.goToVesselLocation(vessel);
                        embryo.vessel.selectVessel(vessel);

                        var t = $(this);
                        t.addClass("btn-info");
                        $("#vcpSearch").on("hidden.selected", function() {
                            t.removeClass("btn-info");
                            $("#vcpSearch").off("hidden.selected");
                        })
                    }
                })

            })
        } else {
            $("#searchMatch").html('');
            $("#searchResults").css('visibility', 'hidden');
        }
    }
    
    embryo.authenticated(function() {
        setInterval(function() {
            var val = $("#searchField").val();
            if (val != lastSearch) {
                lastSearch = val;
                search(val);
            }
        }, 500);
    });

    embryo.ready(function() {
        $('#vcpSearch').on('hidden', function () {
            $("#searchField").blur();
        });
        
        $('#vcpSearch').on('shown', function () {
            $("#searchField").focus();
        });
    });
    
});
