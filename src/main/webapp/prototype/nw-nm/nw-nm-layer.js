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
        var nwLayer;
        var nmLayer;
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
        var nwFeatures = new ol.Collection();
        nwLayer = new ol.layer.Vector({
            title: 'Navigational Warnings',
            source: new ol.source.Vector({
                features: nwFeatures,
                wrapX: false
            }),
            style: function(feature) {
                var featureStyle;
                if (feature.get('parentFeatureId')) {
                    featureStyle = bufferedStyle;
                } else {
                    featureStyle = nwStyle;
                }
                return [ featureStyle ];
            }
        });
        nwLayer.setVisible(true);

        var nmFeatures = new ol.Collection();
        nmLayer = new ol.layer.Vector({
            title: 'Notices to Mariners',
            source: new ol.source.Vector({
                features: nmFeatures,
                wrapX: false
            }),
            style: function(feature) {
                var featureStyle;
                if (feature.get('parentFeatureId')) {
                    featureStyle = bufferedStyle;
                } else {
                    featureStyle = nmStyle;
                }
                return [ featureStyle ];
            }
        });
        nmLayer.setVisible(true);


        /***************************/
        /** Message List Handling **/
        /***************************/

        this.updateLayerFromMessageList = function (messages) {
            nwLayer.getSource().clear();
            nmLayer.getSource().clear();
            messageList = messages;
            generalMessages.length = 0;
            if (messageList && messageList.length > 0) {
                angular.forEach(messageList, function (message) {
                    if (message.geometry && message.geometry.features.length > 0) {

                        angular.forEach(message.geometry.features, function (gjFeature) {
                            var olFeature = that.gjToOlFeature(gjFeature);
                            olFeature.set('message', message);
                            if (message.mainType == 'NW') {
                                nwLayer.getSource().addFeature(olFeature);
                            } else {
                                nmLayer.getSource().addFeature(olFeature);
                            }
                        });
                    } else {
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


        nwnmLayer = new ol.layer.Group({
            title: 'NW-NM',
            layers: [ nwLayer, nmLayer ]
        });
        nwnmLayer.set('name', "NW");
        nwnmLayer.setVisible(true);

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

