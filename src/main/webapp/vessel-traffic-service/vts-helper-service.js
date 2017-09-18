/** Helper service for VTS
 *
 * Debug tool. <- set debugMode true/false.
 *
 * Validation help with generic input types.
 * Minimap init and population.
 * Route intersect calc.
 *
 *
 * **/

angular.module('maritimeweb.vts-report').service('VtsHelperService', ['$window', '$http', 'growl', '$q',
    function ($window, $http, growl, $q) {

        //DEBUG TOOLS
        var debugMode = false;
        if (debugMode == true) console.log("****** DEBUG MODE IS ON! *******");
        //VTS areas WKT can be quite extensive so need to keep an eye on available localstorage
        this.displayTotalSumOfBytesInLocalStorage = function () { //called from global validate in ctrl.
            var totalBytes = 0;
            var totalEntries = 0;
            if (debugMode) {
                for (var i = 0, len = localStorage.length; i < len; ++i) {
                    totalBytes += localStorage.getItem(localStorage.key(i)).length;
                    totalEntries++;
                }
                console.log("LocalStorage info:\nTotal entries:", totalEntries, "\nTotal bytes:", totalBytes, "(max. 3000000!)");
            }
        };
        //DEBUG TOOLS END


        Number.prototype.validateBetween = function (a, b) {
            min = Math.min.apply(Math, [a, b]);
            max = Math.max.apply(Math, [a, b]);
            var minValid = this >= min;
            var maxValid = this <= max;
            return {"minValid": minValid, "maxValid": maxValid};
        };

        this.validateNumber = function (inputString, min, max, decimals) { // maxnums is total allowed numbers, valtype is "int" ||"float" to test on, returned is always array [(true/false), (value as string)].
            var errorMsg = "",
                isValid = false, outputString = "", hasPeriod = false;

            inputString = inputString.toString();
            if (decimals > 0) {
                inputString = inputString.replace(/[^0-9.]/g, '');
            } else {
                inputString = inputString.replace(/[^0-9]/g, ''); //no decimals
            }
            hasPeriod = inputString.indexOf(".") > -1;
            if (!decimals || decimals == "") decimals = 0;
            inputString = String(inputString); //force to string
            if (inputString == "." || inputString == "0.0") inputString = "0.";
            if (inputString.length < 1) errorMsg = "Missing number to validate on in function 'validateNumber'.";
            if (inputString.length > 0) {
                var inputFloat = parseFloat(inputString); //float it
                if (isNaN(inputFloat)) inputFloat = 0.0;
                var retvalidation = inputFloat.validateBetween(min, max, true);
                if (!retvalidation.minValid) errorMsg += "\nValue is smaller than allowed!";
                if (!retvalidation.maxValid) errorMsg += "\nValue is larger than allowed!";
                if (retvalidation.minValid && retvalidation.maxValid) isValid = true;
                outputString = inputString; //swap!
                if (hasPeriod === true) {
                    var tmpStr2 = "";
                    var numStr = outputString.split('.');
                    var tmpStr = numStr[0];
                    try {
                        tmpStr2 = numStr[1].substring(0, decimals);
                    } catch (noDecimals) {
                    }
                    outputString = tmpStr + "." + tmpStr2;
                }
                if (debugMode) {
                    console.log("tmpStr:", tmpStr);
                    console.log("tmpStr2:", tmpStr2);
                }
            }
            if (debugMode) console.log(errorMsg);
            return {"valid": isValid, "val": outputString};
        };


        //only number between and including 0.0 to 359.9, 1 decimal place with cleanup (int or float)
        this.VTSValidation360 = function (str) {
            str = str.toString();
            var isValid = false;
            if (str == "." || str == "0.0") str = "0.";
            if (str.length > 5) str = str.substring(0, 5); //cant be longer than 359.9
            if (str.indexOf(".") !== -1 && str.length > 2 && (str.substring(str.length - 1, str.length) != ".")) { //has period - treat as float
                var inputFloat = parseFloat(str);
                if (inputFloat.isNaN) inputFloat = 0.0;
                if (inputFloat > 0.0 && (inputFloat < 360.0)) isValid = true;
                str = inputFloat + "";
                str = str.substring(0, str.indexOf(".") + 2); //only one decimal returned
            } else if (str.indexOf(".") < 0 && str.length > 0) { //No period, treat as int
                if (str.length > 3) str = str.substring(0, 3);
                var inputInt = parseInt(str);
                if (inputInt.isNaN) inputInt = 0;
                if (inputInt > -1 && (inputInt < 360)) isValid = true;
                str = inputInt;
            } else if (str.length < 1) {
                isValid = false;
            }
            return [isValid, str];
        };


        //validates 0-90 or 0-180 degrees, 0.0001-60.0000 minutes
        this.positionDegMinValidation = function (input, testfor) {
            var output = {valid: false, value: "", decimals: 0};
            if (input != null && input != "") {
                // try {
                //first determine validation
                if (testfor != "decimalminutes") input = input.toString().replace(/\D/g, '');
                var re;
                if (testfor == 90) {
                    re = new RegExp(/^([0-9]|[0-8]\d|90)$/);
                    input = input.substring(0, 2);
                }
                if (testfor == 180) {
                    re = new RegExp(/^(0{0,2}[0-9]|0?[1-9][0-9]|1[0-7][0-9]|180)$/);
                    input = input.substring(0, 3);
                }
                output = {valid: false, value: input, decimals: 0};

                if (testfor == "decimalminutes") {
                    re = new RegExp(/^([1-9]|[0-5]\d|60)(\.[0-9]{1,4})?$/);
                }
                var m = re.exec(input); //executes the validation
                (m == null) ? output.valid = false : output.valid = true;
                return output;
            } else {
                return output;
            }
        };


        this.setSelectionRange = function (input, selectionStart, selectionEnd) {
            input = input.toString();
            if (input.setSelectionRange) {
                window.setTimeout(function () {
                    input.setSelectionRange(selectionStart, selectionEnd); //set caret
                }, 0);
            }
        };


        var convertPosToLonLat = function () {
            var wpPosArrArr = JSON.parse($window.localStorage.getItem('route_oLpoints'));
            var wpLonLatArr = []; //calculate intersect of route with VTS area

            var wpLonLatWKT = "LINESTRING("; //begin LINESTRING
            for (var i = 0; i != wpPosArrArr.length; i++) {
                var lonlat = ol.proj.transform([wpPosArrArr[i][0], wpPosArrArr[i][1]], 'EPSG:3857', 'EPSG:4326');
                wpLonLatWKT += lonlat[0] + " " + lonlat[1] + ","
                wpLonLatArr.push(lonlat);
            }
            wpLonLatWKT = wpLonLatWKT.slice(0, -1) + ")"; //remove trailing comma and terminate LINESTRING
            return [wpLonLatWKT, wpLonLatArr];

        };

        //MINIMAP
        var redrawMiniMapTimer;
        var minimap;

        function redrawMiniMapLoad() {//force drawing of map because timing issue
            var mapcounter = 30;
            try {
                window.dispatchEvent(new Event('resize'));
            } catch (errorTrap) {
            }
            mapcounter--;
            if (mapcounter == 0) clearInterval(redrawMiniMapTimer);
        }

        var vectorSource = new ol.source.Vector({
            title: 'vectorLayer',
            style: new ol.style.Style({
                fill: new ol.style.Fill({
                    color: 'rgba(238, 153, 0, 0.4)',
                    weight: 1
                }),
                stroke: new ol.style.Stroke({
                    color: '#c88500',
                    width: 3
                })
            })
        });


        window.addEventListener('resize', function () { //centers vessel and area
            try {
                var extent = vectorSource.getExtent();
                if (!debugMode) minimap.getView().fit(extent, minimap.getSize());
            } catch (cantExtent) {
            }
        });


        //MINIMAP
        this.miniMapUpdate = function (areaWKT, AISpos, AISheading) { //creates area and ais vessel icon on map
            var routeRet = convertPosToLonLat();
            var routeWKT = routeRet[0];
            var routeArr = routeRet[1];

            if (!areaWKT || areaWKT == "" || areaWKT.length < 9) {
                areaWKT = "POLYGON((12 56,12 55,13 55,13 56,12 56))"; //just a default area
            }
            vectorSource.clear();
            var format = new ol.format.WKT();
            var areafeature = format.readFeature(areaWKT, {
                dataProjection: 'EPSG:4326',
                featureProjection: 'EPSG:3857'
            });
            var routefeature = format.readFeature(routeWKT, {
                dataProjection: 'EPSG:4326',
                featureProjection: 'EPSG:3857'
            });

            areafeature.setStyle(
                new ol.style.Style({
                    fill: new ol.style.Fill({
                        color: 'rgba(238, 153, 0,0.4)',
                        weight: 1
                    })
                })
            );
            routefeature.setStyle(
                new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: '#bb0000',
                        width: 3
                    })
                })
            );
            vectorSource.addFeature(areafeature);
            vectorSource.addFeature(routefeature);


            //Find ETA of intersect at VTS area line
            var routeline = turf.lineString(routeArr);

            //make WKT to poly
            var tmpWKT = areaWKT.replace("POLYGON((", '').replace('))', '');
            tmpWKT = tmpWKT.replace(/,([\s])+/g, ',');
            var tmpPoly = tmpWKT.split(',');
            var areaAsPoly = [], tmpAreaAsPoly = [];
            for (var i = 0; i != tmpPoly.length; i++) {
                var tmpPos = tmpPoly[i].split(" ");
                tmpAreaAsPoly.push([parseFloat(tmpPos[0]), parseFloat(tmpPos[1])]);
            }
            if (tmpAreaAsPoly[0] != tmpAreaAsPoly[tmpAreaAsPoly.length]) tmpAreaAsPoly.push(tmpAreaAsPoly[0]); //last pos must be same as first pos


            areaAsPoly.push(tmpAreaAsPoly); //inception array
            var vtsarea = turf.polygon(areaAsPoly);
            // var vtsarea = turf.polygon([[[12,56],[12,55],[13,55],[13,56],[12,56]]]);
            var intersection = turf.lineIntersect(vtsarea, routeline);
            console.log("intersection:", intersection);
            // alert("TODO:line length, see ETA based on RTZ time, if no time added, make a new rtz with times added for august 2019");
            //TODO: Make option in main map to display all VTS areas in visible map area, obey zoom level, cutoff at
            //TODO: make an RTZ with times, without times and passing 2 VTS areas. (Tallin/Helsinki)
            //TODO: if no route and no ETA for waypoints, and no AIS ETA, use nothing.
            //TODO: if no route and no ETA for waypoints, use AIS ETA at whatever port.
            //TODO: if route and has ETA at first waypoint inside VTS and last waypoint before VTS area, calculate ETA according to length of line using waypoint ETA before intersect.


            if (AISpos && AISpos.length > 1) {//only if there is a pos
                function degToRad(deg) {
                    return (deg * 0.01745329251) - 1.5707963268; //convert to rad, then add rad offset of 90 deg
                }

                var vesselfeature = new ol.Feature({
                    geometry: new ol.geom.Point(ol.proj.transform(AISpos, 'EPSG:4326', 'EPSG:3857')),
                    name: 'Vessel'
                });
                var iconStyle = new ol.style.Style({
                    image: new ol.style.Icon(({
                        anchor: [10, 5],
                        anchorXUnits: 'pixels',
                        anchorYUnits: 'pixels',
                        opacity: 0.75,
                        src: 'img/vessel_red.png',
                        rotation: (AISheading != null && parseInt(AISheading) > -1 && parseInt(AISheading) < 360) ? degToRad(parseInt(AISheading)) : 0
                    }))
                });
                vectorSource.addFeature(vesselfeature);
                vesselfeature.setStyle(iconStyle);
            }

            try {
                var extent = vectorSource.getExtent();
                minimap.getView().fit(extent, minimap.getSize());
            } catch (cantExtent) {
            }
        };

        this.miniMapLoad = function () { //when user selets a VTS centre
            redrawMiniMapTimer = setInterval(function () {
                redrawMiniMapLoad();
            }, 300); //makes sure it displays right
            var view = new ol.View({
                center: [0, 0],
                zoom: 5
            });
            minimap = new ol.Map({
                controls: ol.control.defaults({
                    attribution: false,
                    zoom: false
                }),
                zoom: 3.5,
                minZoom: 0,
                maxZoom: 4,
                center: [0.0, 0.0],
                layers: [
                    new ol.layer.Tile({
                        title: 'OpenStreetMap',
                        type: 'base',
                        visible: true,
                        source: new ol.source.OSM()
                    }),
                    new ol.layer.Vector({
                        source: vectorSource
                    })
                ],
                target: 'minimap',
                view: view
            });
        };
        //END MINIMAP


    }
]);



