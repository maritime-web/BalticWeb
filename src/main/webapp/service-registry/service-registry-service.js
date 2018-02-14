angular.module('maritimeweb.serviceregistry')

/** Service for accessing AIS vessel data **/
    .service('ServiceRegistryService', ['$http', 'growl',
        function ($http, growl) {

            this.getServiceInstances = function (wkt) {
                var params = wkt ? '?wkt=' + encodeURIComponent(wkt) : '';
                var request = '/rest/service/lookup/' + params;
                return $http.get(request);
            };


            this.mcStylePurple = new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: 'rgba(180, 0, 180, 0.5)',
                        width: 1
                    }),
                    fill: new ol.style.Fill({
                        color: 'rgba(180, 0, 180, 0.40)'
                    })
                });

            this.greenServiceStyle = new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: 'rgba(0, 255, 10, 0.8)',
                        width: 3
                    }),
                    fill: new ol.style.Fill({
                        color: 'rgba(5, 200, 10, 0.005)'
                    })
                });


            this.highlightServiceRed =  new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: 'rgba(255, 0, 10, 0.5)',
                        width: 4
                    }),
                    fill: new ol.style.Fill({
                        color: 'rgba(255, 0, 10, 0.005)'
                    })
                });


            /**
             * Convert a Service registry instance to a OpenLayer tile layer.
             * @param aServiceInstance
             * @param daysAgo i.e. 0, 1, 2 days ago. 0 = today, 2 = two days ago.
             * @returns {ol.layer.Tile}
             */
            this.createWKTFromService = function (aServiceInstance, daysAgo) {
                //   $log.debug("createTileLayerFromService  aServiceInstance.url=" + aServiceInstance.url);
                var url = aServiceInstance.url; // http://satellite.e-navigation.net:8080/BalticSea.latest.terra.250m/{z}/{x}/{y}.png
                var description = "";
                var timeAgo;

                var mcNoGoAttributions = [

                        '<div class="panel panel-info">' +
                        '<div class="panel-heading">Satellite image from NASA</div>' +
                        '<div class="panel-body">' +
                        '<span>We acknowledge the use of data products or imagery from the Land, Atmosphere Near real-time Capability for EOS (LANCE) system operated by the NASA/GSFC/Earth Science Data and Information System (ESDIS) with funding provided by NASA/HQ.</span>' +
                        '</div>' +
                        '</div>'

                    ,
                    ol.source.OSM.ATTRIBUTION
                ];
                return new ol.layer.Tile({
                    id: aServiceInstance.instanceId,
                    title: aServiceInstance.name, // 'NASA: one day ago - Aqua Satellite image',
                    description: description,

                    zIndex: 0,
                    source: new ol.source.XYZ({
                        urls: [url],
                        attributions: mcNoGoAttributions,
                        minZoom: 3,
                        maxZoom: 8,
                        tilePixelRatio: 1.000000
                    }),
                    visible: false
                });
            };

        }]);