angular.module('maritimeweb.vessel.layer', []).service('vesselLayer', function () {


    /*
     Create a vessel feature for any openlayers 3 map.
     */
    this.createVesselFeature = function (vessel) {
        var image = this.imageAndTypeTextForVessel(vessel);

        var colorHex = this.colorHexForVessel(vessel);
        var shadedColor = this.shadeBlend(-0.15, colorHex);
        //var radians = (vessel.angle-90) * (Math.PI / 180);
        var markerStyle = new ol.style.Style({
            image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
                anchor: [0.85, 0.5],
                opacity: 0.85,
                id: vessel.id,
                rotation: vessel.radian,
                src: 'img/' + image.name
            }))
            //,
            //text: new ol.style.Text({
            //    text: vessel.name, // attribute code
            //    size: 8,
            //    fill: new ol.style.Fill({
            //        color: 'blue' // black text //
            //    })
            //})
        });

        var vesselPosition = new ol.geom.Point(ol.proj.transform([vessel.x, vessel.y], 'EPSG:4326', 'EPSG:900913'));
        var markerVessel = new ol.Feature({
            name: vessel.name,
            id: vessel.id,
            type: image.type,
            angle: vessel.angle,
            radian: vessel.radian,          // (vessel.angle * (Math.PI / 180)),
            callSign: vessel.callSign,
            mmsi: vessel.mmsi,
            latitude: vessel.y,
            longitude: vessel.x,
            geometry: vesselPosition
        });
        markerVessel.setStyle(markerStyle);


        return markerVessel;
    };

    /*
     Given vessel type return the name of the appropiate vessel image icon and name in text.


     <table class="table table-condensed">
     <tr>
     <td><img src="img/vessel_blue.png" /></td>
     <td>Passenger</td>
     </tr>
     <tr>
     <td><img src="img/vessel_green.png" /></td>
     <td>Cargo</td>
     </tr>
     <tr>
     <td><img src="img/vessel_red.png" /></td>
     <td>Tanker</td>
     </tr>
     <tr>
     <td><img src="img/vessel_yellow.png" /></td>
     <td>High speed craft and WIG</td>
     </tr>
     <tr>
     <td><img src="img/vessel_orange.png" /></td>
     <td>Fishing</td>
     </tr>
     <tr>
     <td><img src="img/vessel_purple.png" /></td>
     <td>Sailing and pleasure</td>
     </tr>
     <tr>
     <td><img src="img/vessel_turquoise.png" /></td>
     <td>Pilot, tug and others</td>
     </tr>
     <tr>
     <td><img src="img/vessel_gray.png" /></td>
     <td>Undefined / unknown</td>
     </tr>
     <tr>
     <td><img src="img/vessel_white.png" /></td>
     <td>Sailing</td>
     </tr>
     <tr>
     <td><img src="img/vessel_white_moored.png" /></td>
     <td>Anchored/Moored</td>
     </tr>
     <tr>
     <td class="aw">BW</td>
     <td>Vessel participating in BalticWeb</td>
     </tr>
     */
    this.imageAndTypeTextForVessel = function (vo) {
        var colorName;
        var vesselType;
        switch (vo.type) {
            case "0" :
                colorName = "blue";
                vesselType = "Passenger";
                break;
            case "1" :
                colorName = "gray";
                vesselType = "Undefined / unknown";
                break;
            case "2" :
                colorName = "green";
                vesselType = "Cargo";
                break;
            case "3" :
                colorName = "orange";
                vesselType = "Fishing";
                break;
            case "4" :
                colorName = "purple";
                vesselType = "Sailing and pleasure";
                break;
            case "5" :
                colorName = "red";
                vesselType = "Tanker";
                break;
            case "6" :
                colorName = "turquoise";
                vesselType = "Pilot, tug and others";
                break;
            case "7" :
                colorName = "yellow";
                vesselType = "High speed craft and WIG";
                break;
            default :
                colorName = "gray";
                vesselType = "Undefined / unknown";
        }

        if (vo.moored) {
            return {
                name: "vessel_" + colorName + "_moored.png",
                type: vesselType,
                width: 12,
                height: 12,
                xOffset: -6,
                yOffset: -6
            };
        } else {
            return {
                name: "vessel_" + colorName + ".png",
                type: vesselType,
                width: 20,
                height: 10,
                xOffset: -10,
                yOffset: -5
            };
        }

    };
    /*
     Create a simplified vessel feature, with only lat,lon,type.
     */
    this.createMinimalVesselFeature = function (vessel) {
        var colorHex = this.colorHexForVessel(vessel);
        var shadedColor = this.shadeBlend(-0.15, colorHex);

        var markerStyle = new ol.style.Style({
            image: new ol.style.Circle({
                radius: 3,
                stroke: new ol.style.Stroke({
                    color: shadedColor,
                    width: 1

                }),
                fill: new ol.style.Fill({
                    color: colorHex // attribute colour
                })
            })
        });

        var vesselPosition = new ol.geom.Point(ol.proj.transform([vessel.x, vessel.y], 'EPSG:4326', 'EPSG:900913'));
        var markerVessel = new ol.Feature({
            geometry: vesselPosition,
            type: vessel.type
        });
        markerVessel.setStyle(markerStyle);
        return markerVessel;

    };

    /** TODO: Remove this method and create a methos similar to colorHexForVessel:
     * a simple function that given one color can darken or lighten it.
     * Given two colors, the function mixes the two, and returns the blended color.
     * This funtion is bluntly copy/pasted from http://stackoverflow.com/questions/5560248/programmatically-lighten-or-darken-a-hex-color-or-rgb-and-blend-colors
     * by http://stackoverflow.com/users/693927/pimp-trizkit
     * usage
     * var color1 = "#FF343B";
     * var color2 = "#343BFF";
     * var color3 = "rgb(234,47,120)";
     * var color4 = "rgb(120,99,248)";
     * var shadedcolor1 = shadeBlend(0.75,color1);
     * var shadedcolor3 = shadeBlend(-0.5,color3);
     * var blendedcolor1 = shadeBlend(0.333,color1,color2);
     * var blendedcolor34 = shadeBlend(-0.8,color3,color4); // Same as using 0.8
     * @param p percentage of shade or highlight
     * @param c0 first color
     * @param c1 OPTIONAL second color, only for blending
     * @returns A string with a color.
     */
    this.shadeBlend = function (p, c0, c1) {
        var n = p < 0 ? p * -1 : p, u = Math.round, w = parseInt;
        if (c0.length > 7) {
            var f = c0.split(","), t = (c1 ? c1 : p < 0 ? "rgb(0,0,0)" : "rgb(255,255,255)").split(","), R = w(f[0].slice(4)), G = w(f[1]), B = w(f[2]);
            return "rgb(" + (u((w(t[0].slice(4)) - R) * n) + R) + "," + (u((w(t[1]) - G) * n) + G) + "," + (u((w(t[2]) - B) * n) + B) + ")"
        } else {
            var f = w(c0.slice(1), 16), t = w((c1 ? c1 : p < 0 ? "#000000" : "#FFFFFF").slice(1), 16), R1 = f >> 16, G1 = f >> 8 & 0x00FF, B1 = f & 0x0000FF;
            return "#" + (0x1000000 + (u(((t >> 16) - R1) * n) + R1) * 0x10000 + (u(((t >> 8 & 0x00FF) - G1) * n) + G1) * 0x100 + (u(((t & 0x0000FF) - B1) * n) + B1)).toString(16).slice(1)
        }
    };

    /**
     * Given a vessels type number betweeen 0-7, return a color in RGB hex format.
     * @param vo = a vessel
     * @returns a color in hex format i.e. #0000ff, #737373, #40e0d0
     *
     */
    this.colorHexForVessel = function (vo) {
        var colorName;

        switch (vo.type) {
            case "0" :
                colorName = "#0000ff";
                break; // blue
            case "1" :
                colorName = "#737373";
                break; // grey
            case "2" :
                colorName = "#00cc00";
                break; // green
            case "3" :
                colorName = "#ffa500";
                break; // orange
            case "4" :
                colorName = "#800080";
                break; // purple
            case "5" :
                colorName = "#ff0000";
                break; // red
            case "6" :
                colorName = "#40e0d0";
                break; // turquoise
            case "7" :
                colorName = "#ffff00";
                break; // yellow
            default :
                colorName = "#737373"; // grey
        }
        return colorName;
    };

});
