embryo.authenticated(function() {
    function refreshLogEntries() {
        $.ajax({
            url: embryo.baseUrl + "rest/log/list",
            data: {
            },
            success: function(data) {
                data.sort(function(a,b) {
                    return b.date - a.date;
                });

                var services = [];

                for (var i in data) {
                    if (services.indexOf(data[i].service) < 0) services.push(data[i].service);
                }

                services.sort();

                var html = "<tr><th>Date</th><th>Status</th><th>Message</th></tr>";

                $.each(services, function(i, service) {
                    html += "<tr><td colspan=3><h5>" + service + "</h5></td></tr>";

                    var count = 0;

                    $.each(data, function(k, v) {
                        if (v.service == service && count < 5) {
                            var status = v.status;

                            switch (status) {
                                case "OK": status = "<span class='label label-success'>"+status+"</span>"; break;
                                case "ERROR": status = "<span class='label label-important'>"+status+"</span>"; break;
                            }

                            html += "<tr><td>"+formatTime(v.date)+"</td><td>"+status+"</td><td>"+v.message+"</td></tr>";

                            if (v.stackTrace) {
                                html += "<tr><td colspan=3><pre>"+v.stackTrace+"</pre></td></tr>";
                            }

                            count ++;
                        }
                    })
                })

                $("#latestLogEntries").html(html);
            },
            error: function(error) {
            }
        });
    }

    setInterval(refreshLogEntries, 60*1000);

    refreshLogEntries();

})
