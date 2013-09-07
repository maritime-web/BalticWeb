$(function() {
    var searchResultsLimit = 10;
    var lastSearch = "";

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

    function search(arg) {
	$("#searchResultsTop").empty();
	$("#searchResultsContainer").empty();
	$("#searchMatch").html('');

	if (arg.length > 0) {
	    // Show loader
	    $("#searchLoad").css('display', 'block');
            
	    // Load search results
	    $.getJSON(embryo.baseUrl + searchUrl, {
		argument : arg
	    }, function(result) {
	        var html = "";

		var s = "s";
                
		// Show search results
		$("#searchResults").css('visibility', 'visible');
                
		// Search results
		var searchResults = [];
                
		// Get vessels
		for (vesselId in result.vessels) {
		    var vesselJSON = result.vessels[vesselId];
		    var vessel = new Vessel(vesselId, vesselJSON, 1);
		    searchResults.push(vessel);
		}
                
		// Add search result to list
		if (searchResults.length <= searchResultsLimit && searchResults.length != 0) {
		    if (searchResults.length == 1) {
			s = "";
		    }
                    
		    $("#searchResultsTop").html("<div class='information'>Search results: </div>");
		    
                    $.each(searchResults, function(key, value) {
			searchResults.push(value);
			html += searchResultToHTML(value, key);
		    });
                    
		}

                $("#searchResultsContainer").html(html);
                
                console.log(html);
                
		$("#searchMatch").html(result.vesselCount + " vessel" + s + " match.");
                
		// Hide loader
		$("#searchLoad").css('display', 'none');
                
                $(".searchResultItem").click(function(e) {
                    var t = $(this);
                    t.addClass("btn-info");
                    setTimeout(function() { 
                        t.removeClass("btn-info");
                    }, 2000);
                    embryo.vessel.goToVesselLocation(searchResults[$(this).attr("id")]);
                    embryo.vessel.selectVessel(searchResults[$(this).attr("id")]);
                });

	    });
	} else {
	    // Hide results
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
        }, 200);
    });

    $('#vcpSearch').on('hidden', function () {
        $("#searchField").blur();
    });

    $('#vcpSearch').on('shown', function () {
        $("#searchField").focus();
    });

    
});
