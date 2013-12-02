$(function() {
    function updateTable() {
        $.ajax({
            url: embryo.baseUrl + "rest/user/list",
            data: {
            },
            success: function(users) {
                var html = "";

                $.each(users, function(k, v) {
                    html += "<tr><td>"+v.login+"</td><td>"+(v.shipMmsi ? v.shipMmsi : "-")+"</td><td>"+v.role+"</td><td><a href=# id="+v.login+">delete</a></td></tr>";
                });

                $("#activeUsersTable").html(html);

                $("#activeUsersTable a").click(function(e) {
                    e.preventDefault();
                    var login = $(this).attr("id");
                    $("#deleteUserDialog").modal("show");
                    $("#deleteUserDialog .btn-primary").off("click");
                    $("#deleteUserDialog .btn-primary").click(function() {
                        $("#deleteUserDialog").modal("hide");

                        feedback("Deleting "+login+" ...");

                        $.ajax({
                            url: embryo.baseUrl + "rest/user/delete",
                            data: {
                                login: login
                            },
                            success: function() {
                                feedback("User "+login+" deleted.");
                                updateTable();
                            },
                            error: function() {
                                feedback("User "+login+" not deleted - check server side error log.");
                            }
                        })
                    })
                })

            }
        })

    }

    function feedback(text) {
        if (text) {
            $("#feedback").css("display", "block");
            $("#feedback").html(text);
        } else {
            $("#feedback").css("display", "none");
        }
    }

    updateTable();

    feedback();

    $("#createNewUserDialogButton").click(function(e) {
        e.preventDefault();
        $("#createNewUserDialog").modal("show");
        $("#cLogin").focus();
    })

    $("#createNewUserDialog .btn-primary").click(function(e) {
        e.preventDefault();
        $("#createNewUserDialog").modal("hide");
        var login = $("#cLogin").val();
        var password = $("#cPassword").val();
        var passwordAgain = $("#cPasswordAgain").val();
        var role = $("#cRole").val();
        var shipMmsi = $("#cShipMmsi").val();

        if (password == passwordAgain && password != "" && login != "") {
            var user = {
                login: login,
                shipMmsi: shipMmsi != "" ? parseInt(shipMmsi) : null,
                password: password,
                role: role
            }

            feedback("Creating "+login+" ...");

            $.ajax({
                url: embryo.baseUrl + "rest/user/save",
                method: "POST",
                contentType: "application/json; charset=utf-8",
                data: JSON.stringify(user),
                success: function() {
                    feedback("User "+login+" created.");
                    updateTable();
                },
                error: function() {
                    feedback("User "+login+" not created - check server side error log.");
                }
            })

        } else {
            feedback("User not created. Passwords must match and user may not already exist.")
        }

        $("#createNewUserDialog input").val("");
    })

});
