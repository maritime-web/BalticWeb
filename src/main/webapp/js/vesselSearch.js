$(function() {
    var searchResultsLimit = 10;
    var lastSearch = "";

    function doServerSideSearch(argument, callback) {
        $.getJSON(embryo.baseUrl + searchUrl, {
            argument : argument
        }, function(result) {
            var searchResults = [];

            for (vesselId in result.vessels) {
                var vesselJSON = result.vessels[vesselId];
                var vessel = new Vessel(vesselId, vesselJSON);
                searchResults.push(vessel);
            }

            callback(searchResults);
        })
    }

    function searchResultToHTML(vessel, key){
        var html =
                "<p><div class='btn searchResultItem' id='" + key + "'>" +
            "<div class='panelText btn-block'>" + vessel.vesselName + "</div>";

        if (searchResultsShowPositon){
            html += "<div class='smallText'>" + vessel.lon + ", " + vessel.lat + "</div>";
        }

        html +=	"</div><p>";

        return html;
    }

    var latestSearch = "";

    function search(arg) {
        $("#searchResultsTop").empty();
        $("#searchResultsContainer").empty();
        $("#searchMatch").html('');

        if (arg.length > 0) {
            // Show loader
            $("#searchLoad").css('display', 'block');

            latestSearch = arg;
            // Load search results

            embryo.vessel.searchVessels(arg, function(searchResults) {
                var html = "";

                var s = "s";

                $("#searchResults").css('visibility', 'visible');

                if (searchResults.length <= searchResultsLimit && searchResults.length != 0) {
                    if (searchResults.length == 1) {
                        s = "";
                    }

                    $("#searchResultsTop").html("<div class='information'>Search results: </div>");

                    $.each(searchResults, function(key, value) {
                        html += searchResultToHTML(value, key);
                    });

                }

                $("#searchResultsContainer").html(html);

                $("#searchMatch").html(searchResults.length + " vessel" + s + " match.");

                $("#searchLoad").css('display', 'none');

                $(".searchResultItem").click(function(e) {
                    var t = $(this);
                    t.addClass("btn-info");
                    $("#vcpSearch").on("hidden.selected", function() {
                        t.removeClass("btn-info");
                        $("#vcpSearch").off("hidden.selected");
                    })
                    embryo.vessel.goToVesselLocation(searchResults[$(this).attr("id")]);
                    embryo.vessel.selectVessel(searchResults[$(this).attr("id")]);
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
