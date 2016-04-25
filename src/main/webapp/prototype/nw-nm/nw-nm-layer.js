/**
 * Defines the main NW-NM message layer
 *
 * TODO: Lifecycle management, convert to use backend data...
 * @type {layerNwNm}
 */
angular.module('maritimeweb.nw-nm.layer',[])

    .service('NwNmLayer', ['$http', '$timeout', function($http, $timeout) {

        var that = this;

        // Actual layer
        var nwnmLayer;

        // Message list
        var messageList = [];

        // Messages with no geometry
        var generalMessages = [];

        // Use for reading GeoJSON
        var projMercator = 'EPSG:3857';
        var proj4326 = 'EPSG:4326';
        var geoJsonFormat = new ol.format.GeoJSON();

        // Batch up loading of messages
        loadTimer = undefined;


        /***************************/
        /** Data methods          **/
        /***************************/

        /**
         * Returns the published NW-NM messages
         * @returns the published NW-NM messages
         */
        this.getPublishedNwNm = function () {
            // Possible parameters: "lang", "mainType" and "wkt". Refer to:
            // http://niord.e-navigation.net/api.html#!/message_list/search
            return $http.get('http://niord.e-navigation.net/rest/public/v1/messages?lang=en');
        };


        /***************************/
        /** Utility methods       **/
        /***************************/

        /** Converts a GeoJSON feature to an OL feature **/
        this.gjToOlFeature = function (feature) {
            return geoJsonFormat.readFeature(feature, {
                dataProjection: proj4326,
                featureProjection: projMercator
            });
        };


        /** Converts xy extent array in mercator to a lon-lat extent array */
        this.toLonLatExtent = function(xyExtent) {
            if (xyExtent && xyExtent.length == 4) {
                var minPos = this.toLonLat([xyExtent[0], xyExtent[1]]);
                var maxPos = this.toLonLat([xyExtent[2], xyExtent[3]]);
                return [minPos[0], minPos[1], maxPos[0], maxPos[1]];
            }
            return null;
        };

        /** Returns the center of the extent */
        this.getExtentCenter = function (extent) {
            var x = extent[0] + (extent[2]-extent[0]) / 2.0;
            var y = extent[1] + (extent[3]-extent[1]) / 2.0;
            return [x, y];
        };

        /** Returns a "sensible" center point of the geometry. Used e.g. for placing labels **/
        this.getGeometryCenter = function (g) {
            var point;
            try {
                switch (g.getType()) {
                    case 'MultiPolygon':
                        var poly = g.getPolygons().reduce(function(left, right) {
                            return left.getArea() > right.getArea() ? left : right;
                        });
                        point = poly.getInteriorPoint().getCoordinates();
                        break;
                    case 'MultiLineString':
                        var lineString = g.getLineStrings().reduce(function(left, right) {
                            return left.getLength() > right.getLength() ? left : right;
                        });
                        point = this.getExtentCenter(lineString.getExtent());
                        break;
                    case 'Polygon':
                        point = g.getInteriorPoint().getCoordinates();
                        break;
                    case 'Point':
                        point = g.getCoordinates();
                        break;
                    case 'LineString':
                    case 'MultiPoint':
                    case 'GeometryCollection':
                        point = this.getExtentCenter(g.getExtent());
                        break;
                }
            } catch (ex) {
            }
            return point;
        };

        /***************************/
        /** Construct Layer       **/
        /***************************/

        var nwStyle = new ol.style.Style({
            fill: new ol.style.Fill({ color: 'rgba(255, 0, 255, 0.2)' }),
            stroke: new ol.style.Stroke({ color: '#8B008B', width: 1 }),
            image: new ol.style.Icon({
                anchor: [0.5, 0.5],
                scale: 0.3,
                src: '/img/nwnm/msi.png'
            })
        });

        var nmStyle = new ol.style.Style({
            fill: new ol.style.Fill({ color: 'rgba(255, 0, 255, 0.2)' }),
            stroke: new ol.style.Stroke({ color: '#8B008B', width: 1 }),
            image: new ol.style.Icon({
                anchor: [0.5, 0.5],
                scale: 0.3,
                src: '/img/nwnm/nm.png'
            })
        });

        var bufferedStyle = new ol.style.Style({
            fill: new ol.style.Fill({
                color: 'rgba(100, 50, 100, 0.2)'
            }),
            stroke: new ol.style.Stroke({
                color: 'rgba(100, 50, 100, 0.6)',
                width: 1
            })
        });

        // Construct the layer
        var features = new ol.Collection();
        nwnmLayer = new ol.layer.Vector({
            source: new ol.source.Vector({
                features: features,
                wrapX: false
            }),
            style: function(feature) {
                var featureStyle;
                if (feature.get('parentFeatureId')) {
                    featureStyle = bufferedStyle;
                } else {
                    var message = feature.get('message');
                    featureStyle = message.mainType == 'NW' ? nwStyle : nmStyle;
                }
                return [ featureStyle ];
            }
        });
        nwnmLayer.set('name', "NW-NM");
        nwnmLayer.setVisible(true);
        //map.addLayer(olLayer);


        /***************************/
        /** Message List Handling **/
        /***************************/

        this.updateLayerFromMessageList = function (messages) {
            nwnmLayer.getSource().clear();
            messageList = messages;
            generalMessages.length = 0;
            if (messageList && messageList.length > 0) {
                angular.forEach(messageList, function (message) {
                    if (message.geometry && message.geometry.features.length > 0) {

                        angular.forEach(message.geometry.features, function (gjFeature) {
                            var olFeature = that.gjToOlFeature(gjFeature);
                            olFeature.set('message', message);
                            nwnmLayer.getSource().addFeature(olFeature);
                        });
                    } else if (showGeneral == 'true') {
                        generalMessages.push(message);
                    }
                });
            }
        };

        // Reload the messages
        this.loadMessages = function () {
            loadTimer = undefined;

            // Load at most scope.maxAtonNo AtoN's
            that.getPublishedNwNm().success(that.updateLayerFromMessageList);
        };


        // When the map extent changes, reload the messages using a timer to batch up changes
        this.mapChanged = function () {
            if (nwnmLayer.getVisible()) {
                // Make sure we reload at most every half second
                if (loadTimer) {
                    $timeout.cancel(loadTimer);
                }
                loadTimer = $timeout(that.loadMessages, 500);
            }
        };

        // Listen for visibility changes of the layer
        nwnmLayer.on('change:visible', this.loadMessages);

        
        /***************************/
        /** Map methods           **/
        /***************************/
        

        /** Attaches the layer of this service to the given map */
        this.addLayerToMap = function (map) {
            this.map = map;
            this.map.addLayer(nwnmLayer);
            this.map.on('moveend', this.mapChanged);
        };

    }]);

