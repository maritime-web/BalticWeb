/**
 * Defines the main NW-NM message layer. navigational warnings and notices to mariners
 *
 * TODO: Lifecycle management, convert to use backend data...
 */
angular.module('maritimeweb.nasa-satellite')

/** Service for retrieving NASA services **/
    .service('NASASatelitteService', ['$http',
        function($http) {


            /**
             * Get NASA Services
             */
            this.getNasaServices = function (wkt) {
                var params = wkt ? '?wkt=' + encodeURIComponent(wkt) : '';
                var pathParam1 = encodeURIComponent('urn:mrn:mcl:service:instance:dma:tiles-service');
                var pathParam2 = encodeURIComponent('0.1');
                var request = '/rest/service/lookup/' + pathParam1 + '/' + pathParam2 + params;
                return $http.get(request);
            };


            this.getDayOfYear = function(){
                var now = new Date();
                var start = new Date(now.getFullYear(), 0, 0);
                var diff = now - start;
                var oneDay = 1000 * 60 * 60 * 24;
                var day = Math.floor(diff / oneDay);
                return day;
            };

            // http://stackoverflow.com/questions/8619879/javascript-calculate-the-day-of-the-year-1-366
            this.isLeapYear = function(anydate) {
                var year = date.getFullYear();
                if((year & 3) != 0) return false;
                return ((year % 100) != 0 || (year % 400) == 0);
            };

            // Get Day of Year
            this.getDOY = function(anydate) {
                var dayCount = [0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334];
                var mn = anydate.getMonth();
                var dn = anydate.getDate();
                var dayOfYear = dayCount[mn] + dn;
                if(mn > 1 && this.isLeapYear()) dayOfYear++;
                return dayOfYear;
            };
        }])


    /**
     * The map-nw-nm-layer directive supports drawing a list of messages or a single message on a map layer
     */
    .directive('mapSatelliteLayer', ['$rootScope', '$timeout', 'MapService',
        function ($rootScope, $timeout, MapService) {
            return {
                restrict: 'E',
                require: '^olMap',
                template: "",
                scope: {
                    name:           '@',
                    // Specify the "message" attribute for showing the geometry of a single message
                    message:        '=?',
                    // Specify the "messageList" and "services" attributes for showing and loading messages within map bounds
                    services:       '=?',
                    messageList:    '=?',

                    language:       '@',
                    showGeneral:    '@',
                    fitExtent:      '@',
                    maxZoom:        '@'
                },
                link: function(scope, element, attrs, ctrl) {
                    var olScope = ctrl.getOpenlayersScope();
                    var nwLayer;




                        /***************************/
                        /** Map creation          **/
                        /***************************/

                    var nasaAttributions = [
                        new ol.Attribution({
                            html: '<div class="panel panel-info">' +
                            '<div class="panel-heading">Satellite image from NASA</div>' +
                            '<div class="panel-body">' +
                            '<span>We acknowledge the use of data products or imagery from the Land, Atmosphere Near real-time Capability for EOS (LANCE) system operated by the NASA/GSFC/Earth Science Data and Information System (ESDIS) with funding provided by NASA/HQ.</span>' +
                            '</div>' +
                            '</div>'

                        }),
                        ol.source.OSM.ATTRIBUTION
                    ];

                    var satelliteLayer =  new ol.layer.Group({
                        title: 'Weather Forecasts',
                        layers: [
                            new ol.layer.Tile({
                                title: 'NASA: one day ago - Aqua Satellite image',

                                source: new ol.source.XYZ({
                                    urls:[
                                        'http://satellite.e-navigation.net:8080/BalticSea.2016300.aqua.250m/{z}/{x}/{y}.png'
                                    ],
                                    attributions: nasaAttributions,
                                    minZoom: 3,
                                    maxZoom: 8,
                                    tilePixelRatio: 1.000000
                                }),
                                visible: false

                            }),
                            new ol.layer.Tile({
                                title: 'NASA: one day ago - Terra satellite image',

                                source: new ol.source.XYZ({
                                    urls:[
                                        'http://satellite.e-navigation.net:8080/BalticSea.2016300.terra.250m/{z}/{x}/{y}.png'
                                    ],
                                    attributions: nasaAttributions,
                                    minZoom: 3,
                                    maxZoom: 8,
                                    tilePixelRatio: 1.000000
                                }),
                                visible: false
                            })
                        ]
                    });
                    satelliteLayer.setVisible(true);
                    map.addLayer(satelliteLayer);
                    }
                };
            }
        ]);