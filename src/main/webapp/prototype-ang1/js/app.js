
angular.module("maritimeweb", ['ngAnimate', 'ui.bootstrap'])
    .controller("MapController", function($scope, $http, $timeout) {

        $scope.alerts = [
            { type: 'success', msg: 'Well done! You successfully read this important alert message.', timeout: 2000 }
        ];

        $scope.closeAlert = function(index) {
            $scope.alerts.splice(index, 1);
        };

        $scope.vesselsOnMap = [];
        var vesselVectorLayer = {};
        var firstRun = true;
        var loadTimer;

        var refreshVessels = function(evt) {

            $scope.clientBBox = maritimeweb.clientBBOX();
            $scope.zoomLvl = maritimeweb.map.getView().getZoom();
            $scope.alerts.push({msg: 'Fetching vessel data',
                type: 'info',
                timeout: 2000
            });


            $http.get( "/rest/vessel/listarea?area="+ maritimeweb.clientBBOX(), {
                timeout : 6000
            }).success(function (vessels) {
                $scope.vesselsonmap = [];
                console.log(vessels.length + " vessels loaded  " + evt );
                $scope.vessels = vessels;
                $scope.vesselsStatus = "OK";
                $scope.lastFetchTimestamp = new Date();

                maritimeweb.groupVessels.getLayers().remove(vesselVectorLayer);

                for(var i = 0; i< vessels.length; i++){
                    var vesselData = {  name: vessels[i].name || "",
                                        type: vessels[i].type,
                                        x: vessels[i].x,
                                        y: vessels[i].y,
                                        angle: vessels[i].angle,
                                        mmsi: vessels[i].mmsi || "",
                                        callSign: vessels[i].callSign || "",
                                        moored: vessels[i].moored || false,
                                        inAW: vessels[i].inAW || false
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

                vesselVectorLayer = new ol.layer.Vector({
                    name: "vesselVectorLayer",
                    title: "Vessels - AIS data",
                    source: vectorSource
                });
                $scope.vesselsStatus = "OK";
                firstRun = false;

                maritimeweb.groupVessels.getLayers().push(vesselVectorLayer);
                $scope.alerts.push({msg: 'retrieved vessels',
                    type: 'success',
                    timeout: 2000
                });

            }).error(function (data, status) {
                console.log("could not retrieve ais data" + status);
                $scope.alerts.push({msg: 'could not retrieve ais data',
                    type: 'danger',
                    timeout: 5000
                });
                $scope.vesselsStatus = status;

            });

        };

        // When the map extent changes, reload the Vessels's using a timer to batch up changes
        var mapChanged = function () {

            if (!firstRun) { // for anything but the first run, check if the layer is visible.
                if (!maritimeweb.isLayerVisible('vesselVectorLayer')) {
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
        // update the map when a move ends.
        maritimeweb.map.on('moveend', mapChanged );
        maritimeweb.groupVessels.on('change:visible', mapChanged);

    });

