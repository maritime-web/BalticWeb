$(function() {
    var users = {
        orasila: {
            shipMmsi: "123451234",
            role: "Sailor"
        },
        oratank: {
            shipMmsi: "987698776",
            role: "Sailor"
        },
        dma: {
            shipMmsi: "",
            role: "Administrator"
        }
    };

    function updateTable() {
        var html = "";

        $.each(users, function(k, v) {
            html += "<tr><td>"+k+"</td><td>"+v.shipMmsi+"</td><td>"+v.role+"</td><td><a href=# id="+k+" class=edit>edit</a> - <a href=# id="+k+" class=delete>delete</a></td></tr>";
        });

        $("#activeUsersTable").html(html);

        $("#activeUsersTable .edit").click(function(e) {
            e.preventDefault();
            var user = users[$(this).attr("id")];
            $("#editUserDialog").modal("show");
            $("#eLogin").val($(this).attr("id"));
            $("#ePassword").val("");
            $("#ePasswordAgain").val("");
            $("#eShipMmsi").val(user.shipMmsi);
            $("#eRole").val(user.role);

            $("#editUserDialog .btn-primary").off("click");
            $("#editUserDialog .btn-primary").click(function() {
                $("#editUserDialog").modal("hide");
                alert("saving "+$("#eLogin").val());
            })
        })

        $("#activeUsersTable .delete").click(function(e) {
            e.preventDefault();
            var login = $(this).attr("id");
            $("#deleteUserDialog").modal("show");
            $("#deleteUserDialog .btn-primary").off("click");
            $("#deleteUserDialog .btn-primary").click(function() {
                $("#deleteUserDialog").modal("hide");
                delete users[login];
                updateTable();
            })
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

    $("#createNewUserDialog .btn-primary").click(function(e) {
        e.preventDefault();
        $("#createNewUserDialog").modal("hide");
        var login = $("#cLogin").val();
        var password = $("#cPassword").val();
        var passwordAgain = $("#cPasswordAgain").val();
        var role = $("#cRole").val();
        var shipMmsi = $("#cShipMmsi").val();

        if (password == passwordAgain && password != "" && users[login] == null && login != "") {
            users[login] = {
                shipMmsi: shipMmsi,
                role: role
            }

            feedback("User "+login+" created.");
        } else {
            feedback("User not created. Passwords must match and user may not already exist.")
        }

        updateTable();

        $("#createNewUserDialog input").val("");
    })

});
