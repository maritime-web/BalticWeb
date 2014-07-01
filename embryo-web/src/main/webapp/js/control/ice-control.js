$(function() {

    embryo.IceController = function($scope, IceService, $timeout) {
        $scope.selectedProvider = {
            key : null
        };

        function loadProviders() {
            var messageId = embryo.messagePanel.show({
                text : "Requesting ice chart providers ..."
            });
            IceService.providers(function(providers) {
                embryo.messagePanel.replace(messageId, {
                    text : "List of " + providers.length + " ice chart providers downloaded",
                    type : "success"
                });
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
            }, function(errorMsg, status) {
                embryo.messagePanel.replace(messageId, {
                    text : errorMsg,
                    type : "error"
                });
            });
        }

        $scope.getSelected = function() {
            return $scope.selectedProvider;
        };

        $scope.$watch($scope.getSelected, function(newValue, oldValue) {
            if (newValue.key) {
                setCookie("dma-ice-provider-" + embryo.authentication.userName, newValue.key, 30);
                requestAllIceObservations();
                $("#iceControlPanel .collapse").data("collapse", null);
                openCollapse("#iceControlPanel #icpIceMaps");
            }
        }, true);

        function init() {
            loadProviders();
            $timeout(loadProviders, 24 * 60 * 1000 * 60);
            $timeout(requestAllIceObservations, 2 * 60 * 1000 * 60);
        }

        if (typeof embryo.authentication.permissions === 'undefined') {
            embryo.authenticated(function() {
                init();
            });
        } else {
            init();
        }

        function requestAllIceObservations() {
            requestIceChartObservations();
            requestIcebergObservations();
        }

        function requestIceChartObservations() {
            requestIceObservations('iceChart');
        }

        function requestIcebergObservations() {
            requestIceObservations('iceberg');
        }

        function requestIceObservations(chartType) {
            var messageId = embryo.messagePanel.show({
                text : "Requesting list of ice charts ..."
            });

            var divId = (chartType == 'iceberg' ? '#icpIcebergs' : '#icpIceMaps');
            embryo.ice.service
                    .listByProvider(
                            chartType,
                            $scope.selectedProvider.key,
                            function(data) {
                                embryo.messagePanel.replace(messageId, {
                                    text : "List of " + data.length
                                            + (chartType == "iceberg" ? " icebergs" : " ice charts") + " downloaded.",
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
                                };

                                regions.sort(sortFunction);

                                var html = "";

                                for ( var j in regions) {
                                    var region = regions[j];
                                    html += "<tr><td colspan=4><h4>" + region + "</h4></td></tr>";

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

                                $(divId + " table").html(html);

                                function registerClicks() {
                                    // $("#icpIceMaps
                                    // td:first").css("border-top", "none");
                                    $(divId + " table span.zoomhide").css("display", "none");
                                    $(divId + " table a.download").on('click', function(e) {
                                        e.preventDefault();
                                        var row = $(this).parents("tr");
                                        requestShapefile(chartType, data[$(this).attr("mid")], function() {
                                            $(divId + "table tr").removeClass("alert");
                                            $(row).addClass("alert");
                                            $(divId + " table span.zoomhide").css("display", "none");
                                            $(divId + " table a.download").css("display", "block");
                                            $("span.zoomhide", row).css("display", "block");
                                            $("a.download", row).css("display", "none");
                                        });
                                        // "201304100920_CapeFarewell_RIC,201308141200_Greenland_WA,201308132150_Qaanaaq_RIC,201308070805_NorthEast_RIC");
                                        // alert(data[$(this).attr("href")].shapeFileName);
                                    });
                                    $(divId + " table a.zoom").click(function(e) {
                                        e.preventDefault();
                                        embryo.map.zoomToExtent(iceLayer.layers);
                                    });
                                    $(divId + " table a.hideIce").click(function(e) {
                                        e.preventDefault();
                                        iceLayer.clear(chartType);
                                        $(divId + " span.zoomhide").css("display", "none");
                                        var row = $(this).parents("tr");
                                        $("a.download", row).css("display", "block");
                                    });
                                }

                                registerClicks();

                                setTimeout(registerClicks, 1000);
                            }, function(errorMsg, status) {
                                embryo.messagePanel.replace(messageId, {
                                    text : errorMsg,
                                    type : "error"
                                });
                            });
        }

        function requestShapefile(chartType, x, onSuccess) {
            var messageId = embryo.messagePanel.show({
                text : "Requesting " + name + " data ..."
            });

            var name = x.shapeFileName;

            embryo.shape.service.shape(name, {
                parts : name.indexOf("aari.aari_arc") >= 0 ? 2 : 0
            }, function(data) {
                messageId = embryo.messagePanel.replace(messageId, {
                    text : "Drawing " + name,
                });

                var totalPolygons = 0;
                var totalPoints = 0;
                if (chartType == 'iceberg') {
                    totalPoints = data.fragments.length;
                } else {
                    for ( var i in data.fragments) {
                        totalPolygons += data.fragments[i].polygons.length;
                        for ( var j in data.fragments[i].polygons)
                            totalPoints += data.fragments[i].polygons[j].length;
                    }
                }

                function finishedDrawing() {
                    embryo.messagePanel.replace(messageId, {
                        text : totalPolygons + " polygons. " + totalPoints + " points drawn.",
                        type : "success"
                    });

                    if (onSuccess) {
                        onSuccess();
                    }
                }

                data.information = {
                    region : x.region,
                    date : x.date
                };
                // Draw shapefile a bit later, just let the browser update the
                // view and show above message
                window.setTimeout(function() {
                    iceLayer.draw(chartType, [ data ], finishedDrawing);
                }, 10);
            }, function(errorMsg, status) {
                if (status == 410) {
                    errorMsg = errorMsg + " Refreshing ice chart list ... ";
                }
                embryo.messagePanel.replace(messageId, {
                    text : errorMsg,
                    type : "error"
                });

                requestIceObservations();
            });
        }

    };

    function createTableHeaderRow(headline) {
        return '<tr><th colspan="2" style="background-color:#eee;">' + headline + '</th></tr>';
    }

    function createTableRow(props) {
        var result = '';
        $.each(props, function(k, v) {
            result += "<tr><th>" + k + "</th><td>" + v + "</td></tr>";
        });
        return result;
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

        html += createTableHeaderRow('Total');

        html += createTableRow({
            "Concentration" : c(d.CT),
            "Stage of Development (S0)" : s(d.CN),
            "Stage of Development (Sd)" : s(d.CD),
            "Form of Ice" : f(d.CF)
        });

        html += createTableHeaderRow('Thickest Partial');

        html += createTableRow({
            "Concentration" : c(d.CA),
            "Stage of Development" : s(d.SA),
            "Form of Ice" : f(d.FA)
        });

        html += createTableHeaderRow('Second Thickest Partial');

        html += createTableRow({
            "Concentration" : c(d.CB),
            "Stage of Development" : s(d.SB),
            "Form of Ice" : f(d.FB)
        });

        html += createTableHeaderRow('Third Thickest Partial');

        html += createTableRow({
            "Concentration" : c(d.CC),
            "Stage of Development" : s(d.SC),
            "Form of Ice" : f(d.FC)
        });

        return html;
    }

    function createIcebergTable(desc) {
        function getIcebergSize(size) {
            switch (size) {
            case 'S':
                return 'Small';
            case 'M':
                return 'Medium';
            case 'L':
                return 'Large';
            case 'VL':
                return 'Very large';
            default:
                return 'Uncategorized';
            }
        }

        var html = '';

        html += createTableHeaderRow('Iceberg');

        html += createTableRow({
            'Area (m2)' : desc.Area_m2,
            'Longest diameter (m)' : desc.Adj_Size_m,
            'Size category' : getIcebergSize(desc.Size_Catg)
        });

        return html;
    }

    function showIceInformation(iceDescription) {
        $("a[href=#icpSelectedIce]").html("Selected Ice Observation");
        if (iceDescription.type == 'iceberg') {
            $("#icpSelectedIce table").html(createIcebergTable(iceDescription));
        } else {
            $("#icpSelectedIce table").html(createIceTable($.extend(iceDescription, {
                size : 160
            })));
        }

        var source = "Region: " + iceDescription.information.region + "<br/>";
        source += ("Created: " + formatTime(iceDescription.information.date) + " UTC");
        if (iceDescription.type == 'iceberg') {
            source += ("<br/>Position:" + formatLatitude(iceDescription.Lat) + ', ' + formatLongitude(iceDescription.Long));
        }
        $("#icpSelectedIce p").html(source);
        openCollapse("#icpSelectedIce");
    }

    function hideIceInformation() {
        closeCollapse("#icpSelectedIce");
    }

    var iceLayer = new IceLayer();

    addLayerToMap("ice", iceLayer, embryo.map);

    iceLayer.select(function(ice) {
        var lon = null, lat = null;
        if (ice && ice.type == 'iceberg') {
            lon = ice.Long;
            lat = ice.Lat;
        }
        iceLayer.selectIceberg(lon, lat);
        if (ice != null) {
            showIceInformation(ice);
        } else {
            hideIceInformation();
        }
    });

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
            $("#iceControlPanel .e-accordion-inner").css("overflow", "auto");
            $("#iceControlPanel .e-accordion-inner").css("max-height", Math.max(100, $(window).height() - 233) + "px");
        }

        $(window).resize(fixAccordionSize);

        fixAccordionSize();
    });
});
