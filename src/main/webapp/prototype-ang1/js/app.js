
angular.module("maritimeweb", ['ngAnimate', 'ui.bootstrap', 'maritimeweb.location.service','maritimeweb.vessel.service'])
    .controller("MapController", function($scope, $http, $timeout, vesselService, locationService) {


        console.log("vesselService=" + vesselService);
        $scope.alerts = [
            { type: 'success', msg: 'Well done! You successfully read this important alert message.', timeout: 2000 }
        ];

        $scope.closeAlert = function(index) {
            $scope.alerts.splice(index, 1);
        };

        $scope.layerGroupAtons = maritimeweb.groupAtons.getLayers().getArray();
        $scope.layerGroupVessels = maritimeweb.groupVessels.getLayers().getArray();
        $scope.layerGroupBasemaps = maritimeweb.groupBaseMaps.getLayers().getArray();
        $scope.layerGroupWeather = maritimeweb.groupWeather.getLayers().getArray();

        $scope.myPosition = {};
        $scope.vesselsOnMap = [];
        //var vesselVectorLayer = {};
        var firstRun = true;
        var loadTimer;

        var refreshVessels = function(evt) {

            $scope.clientBBox = maritimeweb.clientBBOX();
            $scope.zoomLvl = maritimeweb.map.getView().getZoom();
            $scope.alerts.push({msg: 'Fetching vessel data',
                type: 'info',
                timeout: 2000
            });



            $scope.vesselsonmap = [];
          //  var randomVessel = vesselService.details('213');
           // randomVessel.then(function(data){console.log("details = " + data);});

             var promise = vesselService.list().then(function(data){
                 console.log("this is it. Promise resolved");
                 $scope.vessels = data;
             });

           /* promise.then(function(result){
                 console.log("retrieved data");

                 $scope.vessels = result.data;
             });*/
            console.log($scope.vessels.length + " vessels loaded  " + evt );

            $scope.vesselsStatus = "OK";
            $scope.lastFetchTimestamp = new Date();


                for(var i = 0; i< $scope.vessels.length; i++){
                    var vesselData = {  name: $scope.vessels[i].name || "",
                                        type: $scope.vessels[i].type,
                                        x: $scope.vessels[i].x,
                                        y: $scope.vessels[i].y,
                                        angle: $scope.vessels[i].angle,
                                        mmsi: $scope.vessels[i].mmsi || "",
                                        callSign: $scope.vessels[i].callSign || "",
                                        moored: $scope.vessels[i].moored || false,
                                        inAW: $scope.vessels[i].inAW || false
                    };

                    var vesselFeature;
                    if($scope.zoomLvl > 8) {
                        vesselFeature = maritimeweb.createVesselFeature(vesselData);
                    }else{
                        vesselFeature = maritimeweb.createMinimalVesselFeature(vesselData);
                    }
                    $scope.vesselsonmap.push(vesselFeature);
                }

                var vectorSource = new ol.source.Vector({
                    features: $scope.vesselsonmap //add an array of vessel features
                });

                maritimeweb.groupVessels.getLayers().remove(maritimeweb.layerVessels);
                maritimeweb.layerVessels = new ol.layer.Vector({
                    name: "vesselVectorLayer",
                    title: "Vessels - AIS data dynamic",
                    source: vectorSource
                });

                /*
                maritimeweb.layerVessels.source = vectorSource;
                maritimeweb.layerVessels.render*/
                $scope.vesselsStatus = "OK";
                firstRun = false;

                maritimeweb.groupVessels.getLayers().push(maritimeweb.layerVessels);
                $scope.alerts.push({msg: 'retrieved vessels',
                    type: 'success',
                    timeout: 2000
                });



        };

        // When the map extent changes, reload the Vessels's using a timer to batch up changes
        var mapChanged = function () {

            if (!firstRun) { // for anything but the first run, check if the layer is visible.
                if (!maritimeweb.isLayerVisible('vesselVectorLayer', maritimeweb.groupVessels)) {
                    console.log("     --- dont Update layer not visible BREAK");
                    return null;
                }

            }
            // Make sure we reload at most every  second
            if (loadTimer) {
                console.log("     --- too fast");
                $timeout.cancel(loadTimer);
            }
            loadTimer = $timeout(refreshVessels, 1000);

        };


        locationService.get().then(function(result){
            $scope.myPosition =  [ result.longitude,result.latitude];

            console.log("$scope.myPosition" + $scope.myPosition);
          //  maritimeweb.map.getView().setCenter(ol.proj.fromLonLat($scope.myPosition ));
         //   maritimeweb.map.getView().setZoom(10);
        });


        var panTomyPosition = document.getElementById('pan-to-myposition');
        panTomyPosition.addEventListener('click', function() {
            maritimeweb.panToPosition($scope.myPosition);
        }, false);


        // update the map when a move ends.
        maritimeweb.map.on('moveend', mapChanged );
        maritimeweb.groupVessels.on('change:visible', mapChanged);

    });

