$(function() {

    embryo.IceController = function($scope, IceService, $timeout) {
        $scope.selectedProvider = {
            key : null
        };

        function loadProviders() {
            IceService.providers(function(providers) {
                $scope.providers = providers;
                if (providers.length > 0) {
                    var providerKey = getCookie("dma-ice-provider-" + embryo.authentication.userName);
                    for ( var index in providers) {
                        if (providers[index].key == providerKey) {
                            $scope.selectedProvider = providers[index];
                        }
                    }
                    if (!$scope.selectedProvider.key) {
                        $scope.selectedProvider = providers[0];
                    }
                }
            }, function(error) {
                alert(error);
            });
        }

        $scope.getSelected = function() {
            return $scope.selectedProvider;
        }

        $scope.$watch($scope.getSelected, function(newValue, oldValue) {
            if (newValue.key) {
                setCookie("dma-ice-provider-" + embryo.authentication.userName, newValue.key, 30);
                requestIceObservations();
                $("#iceControlPanel .collapse").data("collapse", null);
                openCollapse("#iceControlPanel #icpIceMaps");
            }
        }, true);

        embryo.authenticated(function() {
            loadProviders();
            $timeout(loadProviders, 24 * 60 * 1000 * 60);
            $timeout(requestIceObservations, 2 * 60 * 1000 * 60);
        });

        function requestIceObservations() {
            var messageId = embryo.messagePanel.show({
                text : "Requesting list of ice charts ..."
            })

            embryo.ice.service
                    .listnew(
                            $scope.selectedProvider.key,
                            function(error, data) {
                                if (data) {
                                    embryo.messagePanel.replace(messageId, {
                                        text : "List of " + data.length + " ice charts downloaded.",
                                        type : "success"
                                    });

                                    data.sort(function(a, b) {
                                        return b.date - a.date;
                                    });

                                    var regions = [];

                                    for ( var i in data) {
                                        if (regions.indexOf(data[i].region) < 0)
                                            regions.push(data[i].region);
                                    }

                                    var sortFunction = function(reg1, reg2) {
                                        if (reg1.indexOf("All Arctic") >= 0 && reg2.indexOf("All Arctic") < 0) {
                                            return 1;
                                        } else if (reg1.indexOf("All Arctic") < 0 && reg2.indexOf("All Arctic") >= 0) {
                                            return -1;
                                        } else if (reg1.indexOf("Overview") >= 0 && reg2.indexOf("Overview") < 0) {
                                            return 1;
                                        } else if (reg1.indexOf("Overview") < 0 && reg2.indexOf("Overview") >= 0) {
                                            return -1;
                                        }
                                        return reg1.localeCompare(reg2);
                                    }

                                    regions.sort(sortFunction);

                                    var html = "";

                                    for ( var j in regions) {
                                        var region = regions[j];
                                        html += "<tr><td colspan=4><h5>" + region + "</h5></td></tr>";

                                        for ( var i in data) {
                                            if (data[i].region == region)
                                                html += "<tr><td>"
                                                        + data[i].source
                                                        + "</td><td>"
                                                        + formatTime(data[i].date)
                                                        + "</td><td>"
                                                        + formatSize(data[i].size)
                                                        + "</td><td><a mid="
                                                        + i
                                                        + " href=# class='download'>download</a><span class='zoomhide'>"
                                                        + "<a href=# class='zoom'>zoom</a> / <a href=# class='hideIce'>hide</a></span></td></tr>";
                                        }

                                    }

                                    $("#icpIceMaps table").html(html);

                                    // $("#icpIceMaps
                                    // td:first").css("border-top", "none");
                                    $("#icpIceMaps table span.zoomhide").css("display", "none");
                                    $("#icpIceMaps table a.download").click(function(e) {
                                        e.preventDefault();
                                        var row = $(this).parents("tr");
                                        requestShapefile(data[$(this).attr("mid")].shapeFileName, function() {
                                            $("#icpIceMaps table tr").removeClass("alert");
                                            $(row).addClass("alert");
                                            $("#icpIceMaps table span.zoomhide").css("display", "none");
                                            $("#icpIceMaps table a.download").css("display", "block");
                                            $("span.zoomhide", row).css("display", "block");
                                            $("a.download", row).css("display", "none");
                                        });
                                        // "201304100920_CapeFarewell_RIC,201308141200_Greenland_WA,201308132150_Qaanaaq_RIC,201308070805_NorthEast_RIC");
                                        // alert(data[$(this).attr("href")].shapeFileName);
                                    });
                                    $("#icpIceMaps table a.zoom").click(function(e) {
                                        e.preventDefault();
                                        embryo.map.zoomToExtent(iceLayer.layers);
                                    });
                                    $("#icpIceMaps table a.hideIce").click(function(e) {
                                        e.preventDefault();
                                        iceLayer.clear();
                                        $("span.zoomhide").css("display", "none");
                                        var row = $(this).parents("tr");
                                        $("a.download", row).css("display", "block");
                                    });
                                } else {
                                    embryo.messagePanel.replace(messageId, {
                                        text : "Server returned error code: " + data.status
                                                + " requesting list of ice observations.",
                                        type : "error"
                                    })
                                }
                            })
        }

    }

    function createIceTable(d) {
        function c(v) {
            switch (v) {
            case "00":
                return "Ice Free";
            case "1":
                return "Open Water";
            case "2":
                return "Bergy Water";
            case "10":
                return "1/10";
            case "12":
                return "1/10 to 2/10";
            case "13":
                return "1/10 to 3/10";
            case "20":
                return "2/10";
            case "23":
                return "2/10 to 3/10";
            case "24":
                return "2/10 to 4/10";
            case "30":
                return "3/10";
            case "34":
                return "3/10 to 4/10";
            case "35":
                return "3/10 to 5/10";
            case "40":
                return "4/10";
            case "45":
                return "4/10 to 5/10";
            case "46":
                return "4/10 to 6/10";
            case "50":
                return "5/10";
            case "56":
                return "5/10 to 6/10";
            case "57":
                return "5/10 to 7/10";
            case "60":
                return "6/10";
            case "67":
                return "6/10 to 7/10";
            case "68":
                return "6/10 to 8/10";
            case "70":
                return "7/10";
            case "78":
                return "7/10 to 8/10";
            case "79":
                return "7/10 to 9/10";
            case "80":
                return "8/10";
            case "81":
                return "8/10 to 10/10";
            case "89":
                return "8/10 to 9/10";
            case "90":
                return "9/10";
            case "91":
                return "9/10 to 10/10, 9+/10";
            case "92":
                return "10/10";
                // case "99": return "Unknown/Undetermined";
                // case "-9": return "Null Value";
            default:
                return "n/a";
            }
        }

        function s(v) {
            switch (v) {
            case "00":
                return "Ice Free";
            case "80":
                return "No stage of development";
            case "81":
                return "New Ice (<10 cm)";
            case "82":
                return "Nilas Ice Rind (<10 cm)";
            case "83":
                return "Young Ice (10 to 30 cm)";
            case "84":
                return "Grey Ice (10 to 15 cm)";
            case "85":
                return "Grey – White Ice (15 to 30 cm)";
            case "86":
                return "First Year Ice (>30 cm) or Brash Ice";
            case "87":
                return "Thin First Year Ice (30 to 70 cm)";
            case "88":
                return "Thin First Year Ice (stage 1)";
            case "89":
                return "Thin First Year Ice (stage 2)";
            case "90":
                return "Code not currently assigned";
            case "91":
                return "Medium First Year Ice (70 to 120 cm)";
            case "92":
                return "Code not currently assigned";
            case "93":
                return "Thick First Year Ice (>120 cm)";
            case "94":
                return "Code not currently assigned";
            case "95":
                return "Old Ice";
            case "96":
                return "Second Year Ice";
            case "97":
                return "Multi-Year Ice";
            case "98":
                return "Glacier Ice (Icebergs)";
                // case "99": return "Unknown/Undetermined";
                // case "-9": return "";
            default:
                return "n/a";
            }
        }

        function f(v) {
            switch (parseFloat(v)) {
            case 11:
                return "Strips and Patches (1/10)";
            case 12:
                return "Strips and Patches (2/10)";
            case 13:
                return "Strips and Patches (3/10)";
            case 14:
                return "Strips and Patches (4/10)";
            case 15:
                return "Strips and Patches (5/10)";
            case 16:
                return "Strips and Patches (6/10)";
            case 17:
                return "Strips and Patches (7/10)";
            case 18:
                return "Strips and Patches (8/10)";
            case 19:
                return "Strips and Patches (9/10)";
            case 20:
                return "Strips and Patches (10/10)";
            case 0:
                return "Pancake Ice";
            case 1:
                return "Shuga/Small Ice Cake, Brash Ice";
            case 2:
                return "Ice Cake";
            case 3:
                return "Small Floe";
            case 4:
                return "Medium Floe";
            case 5:
                return "Big Floe";
            case 6:
                return "Vast Floe";
            case 7:
                return "Giant Floe";
            case 8:
                return "Fast Ice";
            case 9:
                return "Growlers, Floebergs, Floebits";
            case 10:
                return "Icebergs";
            default:
                return "n/a";
            }
            return v;
        }

        var html = "";

        function o(egenskaber) {
            $.each(egenskaber, function(k, v) {
                html += "<tr><th>" + k + "</th><td>" + v + "</td></tr>";
            });
        }

        html += "<tr><th colspan=2 style=background-color:#eee>Total</th></tr>";

        o({
            "Concentration" : c(d.CT),
            "Stage of Development (S0)" : s(d.CN),
            "Stage of Development (Sd)" : s(d.CD),
            "Form of Ice" : f(d.CF)
        });

        html += "<tr><th colspan=2 style=background-color:#eee>Thickest Partial</th></tr>";

        o({
            "Concentration" : c(d.CA),
            "Stage of Development" : s(d.SA),
            "Form of Ice" : f(d.FA)
        });

        html += "<tr><th colspan=2 style=background-color:#eee>Second Thickest Partial</th></tr>";

        o({
            "Concentration" : c(d.CB),
            "Stage of Development" : s(d.SB),
            "Form of Ice" : f(d.FB)
        });

        html += "<tr><th colspan=2 style=background-color:#eee>Third Thickest Partial</th></tr>";

        o({
            "Concentration" : c(d.CC),
            "Stage of Development" : s(d.SC),
            "Form of Ice" : f(d.FC)
        });

        return html;
    }

    function showIceInformation(iceDescription) {
        $("a[href=#icpSelectedIce]").html("Selected Ice Observation");
        $("#icpSelectedIce table").html(createIceTable($.extend(iceDescription, {
            size : 160
        })));
        $("#icpSelectedIce p").html("Source: " + iceDescription.source);
        openCollapse("#icpSelectedIce");
    }

    function hideIceInformation() {
        closeCollapse("#icpSelectedIce");
    }

    var iceLayer = new IceLayer();

    addLayerToMap("ice", iceLayer, embryo.map)

    iceLayer.select(function(ice) {
        if (ice != null) {
            showIceInformation(ice);
        } else {
            hideIceInformation();
        }
    })

    function requestShapefile(name, onSuccess) {
        embryo.logger.log("Requesting " + name + " data ...");

        var messageId = embryo.messagePanel.show({
            text : "Requesting " + name + " data ..."
        });

        embryo.ice.service.shapes(name, {
            parts : name.indexOf("aari.aari_arc") >= 0 ? 2 : 0
        }, function(error, data) {
            if (data) {
                messageId = embryo.messagePanel.replace(messageId, {
                    text : "Drawing " + name,
                });

                var totalPolygons = 0;
                var totalPoints = 0;
                for ( var k in data) {
                    var s = data[k];
                    for ( var i in s.fragments) {
                        totalPolygons += s.fragments[i].polygons.length;
                        for ( var j in s.fragments[i].polygons)
                            totalPoints += s.fragments[i].polygons[j].length;
                    }
                }

                function finishedDrawing() {
                    embryo.messagePanel.replace(messageId, {
                        text : totalPolygons + " polygons. " + totalPoints + " points drawn.",
                        type : "success"
                    });
                    embryo.logger.log(totalPolygons + " polygons. " + totalPoints + " points drawn.");

                    if (onSuccess) {
                        onSuccess();
                    }
                }

                // Draw shapfile a bit later, just let the browser update the
                // view and show above message
                window.setTimeout(function() {
                    iceLayer.draw(data, finishedDrawing);
                }, 10);
            } else {
                embryo.messagePanel.replace(messageId, {
                    text : "Server returned error code: " + error.status + " requesting ice data.",
                    type : "error"
                })
                embryo.logger.log("Server returned error code: " + error.status + " requesting ice data.");
            }
        });
    }

    embryo.groupChanged(function(e) {
        if (e.groupId == "ice") {
            $("#iceControlPanel").css("display", "block");
            $("#iceControlPanel .collapse").data("collapse", null);
            openCollapse("#iceControlPanel #icpIceMaps");
        } else {
            $("#iceControlPanel").css("display", "none");
        }
    });

    embryo.ready(function() {
        function fixAccordionSize() {
            $("#iceControlPanel .accordion-inner").css("overflow", "auto");
            $("#iceControlPanel .accordion-inner").css("max-height", Math.max(100, $(window).height() - 233) + "px");
        }

        $(window).resize(fixAccordionSize);

        fixAccordionSize();
    });
});