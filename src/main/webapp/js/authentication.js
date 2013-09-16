embryo.authentication = {
    currentPageRequiresAuthentication: true
};

embryo.eventbus.AuthenticatedEvent = function() {
    var event = jQuery.Event("AuthenticatedEvent");
    return event;
};

embryo.eventbus.registerShorthand(embryo.eventbus.AuthenticatedEvent, "authenticated");

embryo.ready(function() {
    
    function updateNavigationBar() {
        // is user logged in ?
        
        if (embryo.authentication.userName == null) {
            var html = "";
            
            html += "<span>";
            html += "<a href=#signIn>Sign in</a> | <a href=#requestLogin>Request login</a>"
            html += "</span>";
            
            $("#authentication").html(html);
            
            $("#authentication a[href=\"#signIn\"]").click(function() {
                $("#login").modal("show");
                return false;
            });
            
            $("#authentication a[href=\"#requestLogin\"]").click(function() {
                $("#request").modal("show");
                return false;
            });
            
        } else {
            var html = "";
            
            html += "<i class='icon-user icon-white' style='vertical-align:middle; margin-bottom: 4px'></i>";
            html += " <span>";
            html += embryo.authentication.userName + " | <a href=#>Log out</a>"
            html += "</span>";
            
            $("#authentication").html(html);
            
            $("#authentication a").click(function(e) {
                e.preventDefault();

                var messageId = embryo.messagePanel.show( { text: "Logging out ..." })
                
                $.ajax({
                    url: embryo.baseUrl+"rest/authentication/logout",
                    data: { 
                    },
                    success: function(data) {
                        location.reload();
                        
                        /*
                        embryo.authentication = {};
                        embryo.messagePanel.replace(messageId, { text: "Succesfully logged out.", type: "success" });
                        updateNavigationBar();
                        */
                    },
                    error: function(data) {
                        embryo.messagePanel.replace(messageId, { text: "Logout failed. ("+data.status+")", type: "error" })
                    }
                });
            });
        }
    }

    $("#login").on("shown", function() {
        $("#userName").focus();
    });
    
    $("#login button.btn-primary").click(function(e) {
        e.preventDefault();
        console.log("Logging in ...");
        var messageId = embryo.messagePanel.show( { text: "Logging in ..." })
        
        $("#login").modal("hide");
        
        $.ajax({
            url: embryo.baseUrl+"rest/authentication/login",
            data: { 
                userName: $("#userName").val(),
                password: $("#password").val()
            },
            success: function(data) {
                console.log("Logged in.");
                embryo.authentication = data;
                embryo.messagePanel.replace(messageId, { text: "Succesfully logged in.", type: "success" });
                updateNavigationBar();
                embryo.eventbus.fireEvent(embryo.eventbus.AuthenticatedEvent());
            },
            error: function(data) {
                console.log("Log in failed.");
                embryo.messagePanel.replace(messageId, { text: "Login failed. ("+data.status+")", type: "error" })
            }
        });
        
        $("#userName").val("");
        $("#password").val("");
    });
    
    updateNavigationBar();
    
    if (embryo.authentication.userName == null) {
        var messageId = embryo.messagePanel.show( { text: "Refreshing ..." })

        $.ajax({
            url: embryo.baseUrl+"rest/authentication/details",
            data: { 
            },
            success: function(data) {
                embryo.authentication = data;
                embryo.messagePanel.replace(messageId, { text: "Refresh succesful.", type: "success" });
                updateNavigationBar();
                embryo.eventbus.fireEvent(embryo.eventbus.AuthenticatedEvent());
            },
            error: function(data) {
                if (data.status == 401 && embryo.authentication.currentPageRequiresAuthentication) {
                    embryo.messagePanel.remove(messageId);
                    $("#login").modal("show");
                } else {
                    embryo.messagePanel.replace(messageId, { text: "Refresh failed. ("+data.status+")", type: "error" })
                }
            }
        });
    }
});
