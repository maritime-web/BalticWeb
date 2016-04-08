
/*
maritimeweb.groupOverlays.getLayers().push(new ol.layer.Tile({
    title: 'FLERE Lande',
    source: new ol.source.TileWMS({
        url: 'http://demo.opengeo.org/geoserver/wms',
        params: {'LAYERS': 'ne:ne_10m_admin_1_states_provinces_lines_shp'},
        serverType: 'geoserver'
    })
}));

maritimeweb.map.getView().on('propertychange', function(e) {
    switch (e.key) {
        case 'resolution':
            console.log(e.oldValue);
            break;
    }
});

*/


console.log("maritimeweb - map" + maritimeweb.map);
console.log("maritimeweb - BBOX metode " + maritimeweb.clientBBOX());
console.log("maritimeweb - center = " + maritimeweb.center);



angular.module("maritimeweb", [])
    .controller("VesselsController", function($scope, $http) {


        $scope.vesselsOnMap = [];
        var vesselVectorLayer = {};

        var refreshVessels = function(evt) {
            var layers = maritimeweb.groupAtons.getLayers();

            console.log("layers=" + layers + "length=" + layers.length + " toString " + layers.toString);
            angular.forEach(layers, function(layers){
                console.log(layers + "name=" +layers.name  + " value=" + layers.value + " layer.title=" + layers.title);
            } );
            /*if(('vesselVectorLayer').getVisibility()){
                console.log("Vessel layer selected. !!!");
            }*/
            $scope.clientBBox = maritimeweb.clientBBOX();

            $http.get( "/rest/vessel/listarea?area="+ maritimeweb.clientBBOX(), {
                timeout : 3000
            }).success(function (vessels) {
                $scope.vesselsOnMap = [];
                console.log(vessels.length + " vessels loaded  " + evt );
                $scope.vessels = vessels;
                $scope.vesselsStatus = "OK";
                $scope.lastFetchTimestamp = new Date();
                // success(vessels);
                maritimeweb.map.removeLayer(vesselVectorLayer);
                maritimeweb.groupAtons.getLayers().remove(vesselVectorLayer);


                for(var i = 0; i< $scope.vessels.length; i++){
                    var testVessel = new Vessel($scope.vessels[i].name, $scope.vessels[i].type, $scope.vessels[i].x,$scope.vessels[i].y);
                    var vesselMinimalFeature = maritimeweb.createMinimalVesselFeature(testVessel);
                    $scope.vesselsOnMap.push(vesselMinimalFeature);
                }
                /*
                angular.forEach($scope.vesselsOnMap, function(name, type, x ,y ){
                    console.log("name=" + name.name + "=" + type + "=" + x + "=" + y);
                } );*/
                var vectorSource = new ol.source.Vector({
                    features: $scope.vesselsOnMap //add an array of vessels
                });
                var iconStyle = new ol.style.Style({
                    image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
                        anchor: [0.85, 0.5],
                        opacity: 0.85,
                        src: 'img/vessel_red.png'
                    }))
                });
                vesselVectorLayer = new ol.layer.Vector({
                    name: "vesselVectorLayer",
                    title: "Vessels - AIS data",
                    source: vectorSource,
                    style: iconStyle
                });

                maritimeweb.groupAtons.getLayers().push(vesselVectorLayer);

            }).error(function (data, status) {
                console.log("could not retrieve ais data" + status);
                $scope.vesselsStatus = status;

            });

        };
        //
        maritimeweb.map.on('moveend', refreshVessels );
        $scope.testVar = "Andreas";

        var Vessel = function(name, type, x,y){
            this.name = name;
            this.type = type;
            this.x = x;
            this.y = y;
        };

        /*
         "angle": 220.4,
         "x": 11.865178,
         "y": 55.942253,
         "name": "ISEFJORD",
         "type": "3",
         "mmsi": 219018998,
         "callSign": "XPE5739",
         "moored": false,
         "inAW": false

         */
        var Vessel = function( name, type,  x, y, angle, mmsi, callSign, moored, inAW){
            this.angle = angle;
            this.x = x;
            this.y = y;
            this.name = name;
            this.type = type;
            this.mmsi = mmsi;
            this.callSign = callSign;
            this.moored = moored;
            this.inAW = inAW;
        };





// random test data.






//map.addLayer(vectorLayer);

    });