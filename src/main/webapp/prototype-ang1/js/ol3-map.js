var maritimeweb = maritimeweb || {};
maritimeweb = {
    clientBBOX: function(){

        var bounds = maritimeweb.map.getView().calculateExtent(maritimeweb.map.getSize());
        var extent = ol.proj.transformExtent(bounds, 'EPSG:3857', 'EPSG:4326');
        var l = Math.floor(extent[0] * 100) / 100;
        var b = Math.floor(extent[1] * 100) / 100;
        var r = Math.ceil(extent[2] * 100) / 100;
        var t = Math.ceil(extent[3] * 100) / 100;
        var search_bbox = b + "|" + l + "|" + t + "|" + r + "";
       // console.log("Moveend= " + search_bbox + " " + extent);

        return search_bbox;
    },
    testFunction: function(){
        console.log("testing in namespace")
    },
    imageForVessel: function (vo) {
        var colorName;

        switch (vo.type) {
            case "0" : colorName = "blue"; break;
            case "1" : colorName = "gray"; break;
            case "2" : colorName = "green"; break;
            case "3" : colorName = "orange"; break;
            case "4" : colorName = "purple"; break;
            case "5" : colorName = "red"; break;
            case "6" : colorName = "turquoise"; break;
            case "7" : colorName = "yellow"; break;
            default :
                colorName = "gray";
        }

        if (vo.moored){
            return {
                name: "vessel_" + colorName + "_moored.png",
                width: 12,
                height: 12,
                xOffset: -6,
                yOffset: -6
            };
        } else {
            return {
                name: "vessel_" + colorName + ".png",
                width: 20,
                height: 10,
                xOffset: -10,
                yOffset: -5
            };
        }
    },
    createMinimalVesselFeature: function(vessel) {
        var colorHex = maritimeweb.colorHexForVessel(vessel);
        var shadedColor = maritimeweb.shadeBlend(-0.15, colorHex);
        var defaultStyle = new ol.style.Style({
            fill: new ol.style.Fill({
                color: colorHex
            }),
            stroke: new ol.style.Stroke({
                    color: shadedColor,
                    width: 10
                }
            )
        });

        var pointStyle = {fill:true, stroke: true, color: colorHex, fillColor: colorHex,
            strokeColor: shadedColor, strokeWidth: 1, pointRadius: 2};
        //var vesselPosition = new ol.geom.Circle(ol.proj.transform([vessel.x, vessel.y], 'EPSG:4326', 'EPSG:3857'), 10);
        var vesselPosition = new ol.geom.Point(ol.proj.transform([vessel.x, vessel.y], 'EPSG:4326', 'EPSG:900913'));

        //console.log("Created vessel " + vessel.type + " " + vessel.x + " " + vessel.y );
        return new ol.Feature({
            geometry: vesselPosition,
            style: defaultStyle
        });
    },

    /**
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
     shadeBlend: function(p,c0,c1) {
        var n=p<0?p*-1:p,u=Math.round,w=parseInt;
        if(c0.length>7){
            var f=c0.split(","),t=(c1?c1:p<0?"rgb(0,0,0)":"rgb(255,255,255)").split(","),R=w(f[0].slice(4)),G=w(f[1]),B=w(f[2]);
            return "rgb("+(u((w(t[0].slice(4))-R)*n)+R)+","+(u((w(t[1])-G)*n)+G)+","+(u((w(t[2])-B)*n)+B)+")"
        }else{
            var f=w(c0.slice(1),16),t=w((c1?c1:p<0?"#000000":"#FFFFFF").slice(1),16),R1=f>>16,G1=f>>8&0x00FF,B1=f&0x0000FF;
            return "#"+(0x1000000+(u(((t>>16)-R1)*n)+R1)*0x10000+(u(((t>>8&0x00FF)-G1)*n)+G1)*0x100+(u(((t&0x0000FF)-B1)*n)+B1)).toString(16).slice(1)
        }
    },

    /**
     * Given a vessels type number betweeen 0-7, return a color in RGB hex format.
     * @param vo = a vessel
     * @returns a color in hex format i.e. #0000ff, #737373, #40e0d0
     *
     */
    colorHexForVessel: function(vo) {
        var colorName;

        switch (vo.type) {
            case "0" : colorName = "#0000ff"; break; // blue
            case "1" : colorName = "#737373"; break; // grey
            case "2" : colorName = "#00cc00"; break; // green
            case "3" : colorName = "#ffa500"; break; // orange
            case "4" : colorName = "#800080"; break; // purple
            case "5" : colorName = "#ff0000"; break; // red
            case "6" : colorName = "#40e0d0"; break; // turquoise
            case "7" : colorName = "#ffff00"; break; // yellow
            default :
                colorName = "#737373"; // grey
        }
        return colorName;
    }


}


var thunderforestAttributions = [
    new ol.Attribution({
        html: 'Tiles &copy; <a href="http://www.thunderforest.com/">Thunderforest</a>'
    }),
    ol.source.OSM.ATTRIBUTION
];

var openseaMapAttributions = [
    new ol.Attribution({
        html: '<a href="http://www.openseamap.org/">www.openseamap.org</a> - <a rel="license" href="http://creativecommons.org/licenses/by-sa/2.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-sa/2.0/80x15.png" /></a><br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-sa/2.0/">Creative Commons Attribution-ShareAlike 2.0 Generic License</a> - '
    }),
    ol.source.OSM.ATTRIBUTION
];


maritimeweb.groupBaseMaps = new function () {
    return new ol.layer.Group({
        'title': 'Base maps',
        layers: [
            new ol.layer.Tile({
                title: 'OpenStreetMap',
                type: 'base',
                visible: true,
                source: new ol.source.OSM()
            }),
            new ol.layer.Tile({
                title: 'Stamen - Water color',
                type: 'base',
                visible: false,
                source: new ol.source.Stamen({
                    layer: 'watercolor'
                })
            }),
            new ol.layer.Tile({
                title: 'Stamen - Toner',
                type: 'base',
                visible: false,
                source: new ol.source.Stamen({
                    layer: 'toner'
                })
            }),
            new ol.layer.Tile({
                title: 'MapQuest - OSM',
                type: 'base',
                visible: false,
                source: new ol.source.MapQuest({
                    layer: 'osm'
                })
            }),
            new ol.layer.Tile({
                title: 'MapQuest - Satellite',
                type: 'base',
                visible: false,
                source: new ol.source.MapQuest({
                    layer: 'sat'
                })
            }),
            new ol.layer.Tile({
                title: 'MapQuest - Hybrid',
                type: 'base',
                visible: false,
                source: new ol.source.MapQuest({
                    layer: 'hyb'
                })
            }),
            new ol.layer.Tile({
                title: 'Thunderforest - OpenCycleMap',
                type: 'base',
                visible: false,
                source: new ol.source.OSM({
                    url: 'http://{a-c}.tile.thunderforest.com/cycle/{z}/{x}/{y}.png',
                    attributions: thunderforestAttributions
                })
            }),
            new ol.layer.Tile({
                title: 'Thunderforest - Outdoors',
                type: 'base',
                visible: false,
                source: new ol.source.OSM({
                    url: 'http://{a-c}.tile.thunderforest.com/outdoors/{z}/{x}/{y}.png',
                    attributions: thunderforestAttributions
                })
            }),
            new ol.layer.Tile({
                title: 'Thunderforest - Landscape',
                type: 'base',
                visible: false,
                source: new ol.source.OSM({
                    url: 'http://{a-c}.tile.thunderforest.com/landscape/{z}/{x}/{y}.png',
                    attributions: thunderforestAttributions
                })
            }),
            new ol.layer.Tile({
                title: 'Thunderforest - Transport',
                type: 'base',
                visible: false,
                source: new ol.source.OSM({
                    url: 'http://{a-c}.tile.thunderforest.com/transport/{z}/{x}/{y}.png',
                    attributions: thunderforestAttributions
                })
            }),
            new ol.layer.Tile({
                title: 'Thunderforest - Transport Dark',
                type: 'base',
                visible: false,
                source: new ol.source.OSM({
                    url: 'http://{a-c}.tile.thunderforest.com/transport-dark/{z}/{x}/{y}.png',
                    attributions: thunderforestAttributions
                })
            })
        ]
    });

};

maritimeweb.groupAtons = new function () {
    var group =  new ol.layer.Group({
        title: 'Atons',
        layers: [
            new ol.layer.Tile({
                title: 'Seamark - OpenSeaMap.org',
                visible: false,
                source: new ol.source.XYZ({
                    url: 'http://t1.openseamap.org/seamark/{z}/{x}/{y}.png',
                    attributions: openseaMapAttributions//,
                    //crossOrigin: 'null'
                })
            }),
            new ol.layer.Tile({
                title: 'Countries',
                source: new ol.source.TileWMS({
                    url: 'http://demo.opengeo.org/geoserver/wms',
                    params: {'LAYERS': 'ne:ne_10m_admin_1_states_provinces_lines_shp'},
                    serverType: 'geoserver'
                }),
                visible: false
            })
        ]
    });
    return group;
};


maritimeweb.groupOverlays = new function () {
    var group =  new ol.layer.Group({
        title: 'Weather Forecasts',
        layers: [
            new ol.layer.Tile({
                title: 'Gust  - openportguide.de',
                opacity: 0.4,
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/tiles/actual/gust/5/{z}/{x}/{y}.png'
                    //url: 'http://weather.openportguide.de/tiles/actual/air_temperature/wind_stream/5/{z}/{x}/{y}.png'
                }),
                visible: false
            }),
            new ol.layer.Tile({
                title: 'Wind - openportguide.de',
                source: new ol.source.XYZ({
                    url: 'http://weather.openportguide.de/tiles/actual/wind_stream/5/{z}/{x}/{y}.png'
                }),
                visible: false
            }),
            new ol.layer.Tile({
                title: 'Air temperature - openportguide.de',
                source: new ol.source.XYZ({
                    //url: 'http://weather.openportguide.de/tiles/actual/wind_stream/5/{z}/{x}/{y}.png'
                    url: 'http://weather.openportguide.de/tiles/actual/air_temperature/5/{z}/{x}/{y}.png'
                }),
                visible: false
            }),
            new ol.layer.Tile({
                title: 'Significant Wave Height - openportguide.org',
                source: new ol.source.XYZ({
                    url: 'http://www.openportguide.org//tiles/actual/significant_wave_height/5/{z}/{x}/{y}.png'
                }),
                visible: false
            })



        ]
    });
    return group;
};



maritimeweb.EPSG4326 = function() { return new ol.Projection("EPSG:4326")};
maritimeweb.EPSG900913  = function() { return  new ol.Projection("EPSG:900913")};

/*
maritimeweb.iconFeature = function() {
    return new ol.Feature({
        geometry: new ol.geom.Point(ol.proj.transform([18.0704, 57.678], 'EPSG:4326', 'EPSG:900913')),
        name: 'Speed vessel',
        speed: 40,
        course: 350
    });
};

maritimeweb.iconFeature1  = function() {
    return new ol.Feature({
        geometry: new ol.geom.Point(ol.proj.transform([18.1234, 55.678], 'EPSG:4326', 'EPSG:900913')),
        name: 'Large Vessel',
        speed: 30,
        course: 20
    });
};*/

maritimeweb.center = new function () {
        return [22.0, 59.0];
}
console.log("loading OL3 map");


var zoomslider = new ol.control.ZoomSlider();
var seaScaleLine = new ol.control.ScaleLine({
    className: 'ol-scale-line',
    units: 'nautical', // 'degrees', 'imperial', 'nautical', 'metric', 'us'
    minWidth: 80,
//        target: 'scaleline',
    target: document.getElementById('scale-line')
});
var metricScaleLine = new ol.control.ScaleLine({
    units: 'metric', // 'degrees', 'imperial', 'nautical', 'metric', 'us'
    minWidth: 80
});

var mousePosition = new ol.control.MousePosition({
    coordinateFormat: ol.coordinate.createStringXY(4),
    projection: 'EPSG:4326',
    //target: 'mouseposition',
    target: document.getElementById('mouse-position'),
    className: 'mouse-position',
    undefinedHTML: '&nbsp;'
});
var balticExtent = ol.proj.transformExtent([9, 53, 31, 66], 'EPSG:4326', 'EPSG:3857');


// Add Layers to map-------------------------------------------------------------------------------------------------------
layer_default_osm = new ol.layer.Tile({
    source: new ol.source.OSM({layer: 'sat'}),
    preload: Infinity
});



maritimeweb.map = new function () {
    var map = new ol.Map({
        controls: ol.control.defaults().extend([
            new ol.control.OverviewMap({collapsed: false}),
            seaScaleLine,
            // metricScaleLine,
            zoomslider,
            new ol.control.FullScreen(),
            mousePosition,
            new ol.control.LayerSwitcher()

        ]),
        target: 'map',
        layers: [
            maritimeweb.groupBaseMaps,
            maritimeweb.groupOverlays,
            maritimeweb.groupAtons

        ],
        view: new ol.View({
            center: ol.proj.fromLonLat(maritimeweb.center),
            zoom: 7,
            minZoom: 6,
            extent: balticExtent
        })
    });
    return map;
}


console.log("loaded OL3 map center = " + maritimeweb.map);


