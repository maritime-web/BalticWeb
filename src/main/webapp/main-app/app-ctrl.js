angular.module('maritimeweb.app')

    .controller("AppController", [
        '$scope', '$http', '$window', '$timeout', 'Auth', 'MapService',
        'VesselService', 'NwNmService', 'SatelliteService', 'ServiceRegistryService', 'mapVtsAreaService', 'growl', '$uibModal', '$log', '$interval', 'VtsHelperService',
        function ($scope, $http, $window, $timeout, Auth, MapService, VesselService, NwNmService, SatelliteService, ServiceRegistryService, mapVtsAreaService, growl, $uibModal, $log, $interval, VtsHelperService) {

            // Cancel any pending NW-NN queries
            var loadTimerService = undefined;
            $scope.$on("$destroy", function () {
                if (loadTimerService) {
                    $timeout.cancel(loadTimerService);
                }
            });

            /** Sidemenu collapse handler **/
            $scope.$watch(function(){
                return MapService.sidebarCollapsed; //collapsed state, map-directive -> map-service -> here
            }, function (newValue) {
                $scope.sidebarCollapsed = MapService.sidebarCollapsed;
            });
            $scope.sidebarUncollapse = function(){ //map-directive collapses on map click
                MapService.sidebarUnCollapse(); //is watched to change state in $scope
            };
            /** END Sidemenu collapse handler **/

            $scope.welcomeToBalticWebModal = function (size) {
                $uibModal.open({
                    animation: 'true',
                    templateUrl: 'partials/welcome.html',
                    controller: 'AcceptTermsCtrl',
                    size: size
                })
            };

            $scope.loggedIn = Auth.loggedIn;

            /** Logs the user in via Keycloak **/
            $scope.login = function () {
                Auth.authz.login();
            };

            /** Logs the user out via Keycloak **/
            $scope.logout = function () {
                Auth.authz.logout();
                $window.localStorage.setItem('mmsi', 0);
            };

            /** Returns the user name ,**/
            $scope.userName = function () {
                if (Auth.authz.tokenParsed) {
                    return Auth.authz.tokenParsed.name
                        || Auth.authz.tokenParsed.preferred_username;
                }
                return undefined;
            };

            /** Returns the mrn **/
            $scope.userMrn = function () {
                if (Auth.authz.tokenParsed) {
                    return Auth.authz.tokenParsed.preferred_username;
                }
                return undefined;
            };
            var lolcat = $scope.userMrn();
            console.log("uncle ben:",lolcat)


            /** Enters the Keycloak account management **/
            $scope.accountManagement = function () {
                Auth.authz.accountManagement();
            };

            // Map state and layers
            $scope.mapState = JSON.parse($window.localStorage.getItem('mapState-storage')) ? JSON.parse($window.localStorage.getItem('mapState-storage')) : {};
            $scope.mapBackgroundLayers = MapService.createStdBgLayerGroup();
            //$scope.mapWeatherLayers = MapService.createStdWeatherLayerGroup();
            $scope.mapMiscLayers = MapService.createStdMiscLayerGroup();
            //$scope.mapTrafficLayers = ""; // is set in the ais-vessel-layer
            $scope.mapSeaMapLayer = MapService.createSuperSeaMapLayerGroup();
            // $scope.mapMCLayers = MapService.createMCLayerGroup();
            // $scope.mapNoGoLayer =  MapService.createNoGoLayerGroup(); // is set in the no-go-layer
            //$scope.mcServiceRegistryInstances = ServiceRegistryService.getServiceInstances('POLYGON((9.268411718750002%2053.89831670389188%2C9.268411718750002%2057.58991390302003%2C18.392557226562502%2057.58991390302003%2C18.392557226562502%2053.89831670389188%2C9.268411718750002%2053.89831670389188))');
            $scope.mcServiceRegistryInstances = [];


            var accepted_terms = $window.localStorage.getItem('terms_accepted_ttl');
            $log.info("accepted_terms ttl = " + accepted_terms);
            var now = new Date();

            if (accepted_terms == null || (new Date(accepted_terms).getTime() < now )) {
                $scope.welcomeToBalticWebModal('lg');
            } else {
                growl.info("Welcome back");
            }


            /**************************************/
            /** Vessel sidebar functionality      **/
            /**************************************/

            // Vessels
            $scope.vessels = [];
            $scope.vesselsinfo = {};
            $scope.vesselsinfo.maxnumberexceeded = false; // flag to indicate if more vessels are presented than displayed.
            $scope.vesselsinfo.actualnumberofvessels = 0;


            /** Returns the icon to use for the given vessel **/
            $scope.iconForVessel = function (vo) {
                return '/img/' + VesselService.imageAndTypeTextForVessel(vo).name;
            };

            /** Returns the lat-lon attributes of the vessel */
            $scope.toLonLat = function (vessel) {
                return {lon: vessel.x, lat: vessel.y};
            };


            /**************************************/
            /** Vessel Traffic Service Report functionality      **/
            /**************************************/
            $scope.activateVTSForm = function (size) { //if vts_current_id is set in localstorage, it opens that areas report form
                if(!size) size='lg';
                growl.info('Activating Vessel Traffic Control');
                $uibModal.open({
                    animation: 'true',
                    templateUrl: 'vessel-traffic-service/vessel-traffic-service-form.html',
                    controller: 'VesselTrafficServiceReportCtrl',
                    size: size
                })
            };
            $scope.activateVTSFormTopMenu = function(){
                VtsHelperService.showVtsCenterSelect = true; //display the select when using top-menu only
                $scope.activateVTSForm();
            };

            $scope.mapSetInView = function(id){
                $window.localStorage.setItem('vts_zoomto_area_id', id);
            };

            $scope.setActiveVtsIdAndOpenForm = function(id){
                console.log("opening VTS with ID:",id);
                localStorage.setItem('vts_current_id', id);
                VtsHelperService.showVtsCenterSelect = false; //display the select when using top-menu only
                $scope.activateVTSForm();
            };


            function checkToActivateVTSForm() { //if refresh without proper close, open form again in same area.
                var vts_current_id = $window.localStorage['vts_current_id'];
                try {
                    if (vts_current_id && parseInt(vts_current_id) > 0){
                        $scope.activateVTSForm();
                    }
                }catch(undefinedError){}
            }
            setTimeout(function(){ checkToActivateVTSForm(); }, 2000);


            /**********************************************/
            /** Vessel Traffic Service Map functionality **/
            /**********************************************/
            //load state for show/hide vts areas on map
            $scope.vts_map_show = $window.localStorage['vts_map_show'];
            ($scope.vts_map_show == true || $scope.vts_map_show == "true") ? $scope.vts_map_show = true : $scope.vts_map_show = false; //is string, need bool

            //load state for only display areas intersecting route
            $scope.vts_onroute_only = $window.localStorage['vts_onroute_only'];
            ($scope.vts_onroute_only == true || $scope.vts_onroute_only == "true") ? $scope.vts_onroute_only = true : $scope.vts_onroute_only = false; //is string, need bool
            $scope.vtsShowIntersecting = $scope.vts_onroute_only; //inits listener in vts-layer.js
            $scope.vtsRouteWKT = ($scope.vts_route_enabled == true)? mapVtsAreaService.returnRouteAsWKT : ""; //watched by directive to populate map on change
            $scope.vtsAreasArr = []; //is watched by directive mapVtsAreaLayer to populate map on change
            $scope.vtsLayerEnabled = $scope.vts_map_show; //is listened to
            $scope.vtsSidemenuListArr = [];

            //Skips null items when populating dropdowns
            $scope.isItemNull = function(item) {
                if (item === null) return false;
                return true;
            };

            /** populate the sidebar with VTS areas on map as selectable list **/
            $scope.testforEnableFilterCheckbox = function(){ //just test if there is a route in localstorage - enables/disables the filter checkbox
                var tmpStr = $window.localStorage['route_oLpoints'];
                (tmpStr && tmpStr.length>9) ? $scope.vts_route_enabled = true : $scope.vts_route_enabled = false;
            };
            $scope.testforEnableFilterCheckbox();

            /** Toggle the enabled status of the layer **/
            $scope.vtsMapToggle = function () {
                $scope.vts_map_show = mapVtsAreaService.toggleVtsAreasLayerEnabled(); //also saves to localstorage
                $scope.vtsLayerEnabled = $scope.vts_map_show; //triggers layer visibility
                if ($scope.vts_map_show == true) {
                    $scope.reloadVtsAreas(); //http service call, triggers update of map
                }else{
                    $scope.vtsSidemenuListArr = []; //clear the list
                }
            };

            $scope.vtsRouteIntersectToggle = function () { //checkbox
                $scope.vts_onroute_only = mapVtsAreaService.toggleVtsAreasOnlyIntersectingRouteEnabled(); //also saves to localstorage
                $scope.vtsShowIntersecting = $scope.vts_onroute_only;
                $scope.populateVtsSidemenuList();
            };


            //Skips null items when populating lists
            $scope.isListItemNull = function(item) {
                if (item === null) return false;
                return true;
            };

            $scope.populateVtsSidemenuList = function(){ //called on load and when route changes
                $scope.vtsSidemenuListArr = []; //reset
                var count = $scope.vtsAreasArr.length;
                var ins = window.localStorage['vts_intersectingareas'];

                //get intersecting IDs into an array
                var intersectingAreasArr = null;
                if(ins && ins.length>2) intersectingAreasArr = JSON.parse(ins);

                //filter to display intersecting IDs only
                var filter=false, f = window.localStorage['vts_onroute_only'];
                if(f && f=="true") filter = true;

                if($scope.vtsAreasArr.length>0){
                    for(var i=0;i!=$scope.vtsAreasArr.length;i++){
                        var tmpObj = {};
                        if((filter==true && intersectingAreasArr && intersectingAreasArr.length>0)){
                            for(var y=0;y!=intersectingAreasArr.length;y++) { //only adds intersecting areas to arr
                                if (parseInt($scope.vtsAreasArr[i].id) == parseInt(intersectingAreasArr[y])) {
                                    tmpObj = {
                                        id: $scope.vtsAreasArr[i].id,
                                        shortname: $scope.vtsAreasArr[i].shortname,
                                        showButton: true
                                    };
                                }
                            }
                        }else{
                            tmpObj = { //add all areas to arr
                                id: $scope.vtsAreasArr[i].id,
                                shortname: $scope.vtsAreasArr[i].shortname,
                                showButton: false
                            };
                        }
                        if(tmpObj && tmpObj.id) $scope.vtsSidemenuListArr.push(tmpObj);
                    }

                }
            };

            //need to know which areas are intersecting the route, if changes, this updates the sidemenu list.
            $scope.$watch(function () { return window.localStorage['vts_intersectingareas']; },function(newVal,oldVal){
                if(oldVal !== newVal && newVal === undefined){
                }else{
                    $scope.testforEnableFilterCheckbox();
                    $scope.populateVtsSidemenuList();
                }
            });

            $scope.reloadVtsAreas = function () {
                mapVtsAreaService.getVtsAreas()
                    .success(function (data, status) {
                        if (status == 204) {
                        }
                        if (status == 200) {
                            $scope.vtsAreasArr = data.VtsJsObjects;

                            $scope.vtsAreasArr.sort(function(a, b) {
                                var textA = a.shortname.toUpperCase();
                                var textB = b.shortname.toUpperCase();
                                return (textA < textB) ? -1 : (textA > textB) ? 1 : 0;
                            });
                            $scope.populateVtsSidemenuList();
                            // $scope.populateVtsSidemenuList(window.localStorage['vts_intersectingareas'],$scope.vtsAreasArr); //init after areas fetched from service
                        }
                        $scope.vtsRouteWKT = mapVtsAreaService.returnRouteAsWKT();
                    })
                    .error(function (error) {
                        $log.debug("Error getting VTS service. Reason=" + error);
                    })
            };

            if ($scope.vts_map_show) {
                $scope.reloadVtsAreas();
            }

            /**************************************/
            /** NOGO Service                     **/
            /**************************************/

            $scope.nogo = {};
            $scope.nogo.ship = {};
            $scope.nogo.ship.draught = 6;
            $scope.nogo.time = new Date();
            $scope.nogo.timeAgoString = "just now";
            $scope.nogo.loading = false;
            $scope.nogo.animating = false;


            $scope.checkNoGoService = function () {
                $log.info("main app controller - check no go service");
                $scope.mapNoGoLayer.setVisible(false);
                $scope.mapNoGoLayer.setVisible(true);
            };

            $scope.disableNoGoService = function () {
                $log.info("main app controller - disable no go service");
                $scope.mapNoGoLayer.setVisible(false);
            };

            $scope.enableNoGoService = function () {
                $log.info("main app controller - enable no go service");
                $scope.mapNoGoLayer.setVisible(true);
            };

            $scope.clearNoGo = function () {
                $log.info("Clear no go");
                serviceAvailableLayer.getSource().clear();
                boundaryLayer.getSource().clear();
                $scope.nogo.loading = false;
            };

            $scope.getNextNoGoArea = function () {
                if (!$scope.nogo.time || ("" == $scope.nogo.time)) {
                    $scope.nogo.time = new Date();
                }
                $scope.nogo.time.setHours($scope.nogo.time.getHours() + 1);
                console.log("getNextNoGoArea " + $scope.nogo.time);
                $scope.checkNoGoService();
            };

            $scope.getNextNoGoAreaIncreaseDraught = function () {
                if (!$scope.nogo.time) {
                    $scope.nogo.time = new Date();
                }
                $scope.nogo.ship.draught = $scope.nogo.ship.draught + 0.5;
                $scope.checkNoGoService();
            };

            $scope.doGruntAnimation = function () {
                $log.info("doGruntAnimation");
                $interval($scope.getNextNoGoArea, 2200, 8);
            };

            $scope.doIncreaseDraughtAnimation = function () {
                $log.info("doIncreaseDraughtAnimation");
                $interval($scope.getNextNoGoAreaIncreaseDraught, 2200, 8);
            };


            /**************************************/
            /** NW-NM sidebar functionality      **/
            /**************************************/

            $scope.nwNmServices = [];
            $scope.satelliteInstances = [];
            $scope.nwNmMessages = [];
            $scope.nwNmLanguage = 'en';
            $scope.nwNmType = {
                NW: $window.localStorage['nwNmShowNw'] != 'false',
                NM: $window.localStorage['nwNmShowNm'] == 'true'
            };

            /**
             * Computes the current NW-NM service boundary
             */
            $scope.currentNwNmBoundary = function () {
                return $scope.mapState['wktextent'];
            };


            /** Schedules reload of the NW-NM services **/
            $scope.refreshNwNmServices = function () {
                if (loadTimerService) {
                    $timeout.cancel(loadTimerService);
                }
                loadTimerService = $timeout(function () {
                    $scope.loadServicesFromRegistry();
                }, 1000);
            };

            // Refresh the service list every time the NW-NM boundary changes
            $scope.$watch($scope.currentNwNmBoundary, $scope.refreshNwNmServices);


            /** Loads the  services **/
            $scope.loadServicesFromRegistry = function () {
                var wkt = $scope.currentNwNmBoundary();
                // var wkt = "POLYGON((-14.475675390625005 40.024168123114805,-14.475675390625005 68.88565248991273,59.92373867187499 68.88565248991273,59.92373867187499 40.024168123114805,-14.475675390625005 40.024168123114805))";

                NwNmService.getNwNmServices(wkt)
                    .success(function (services, status) {
                        //$log.debug("NVNM Status " + status);
                        $scope.nwNmServices.length = 0;

                        // Update the selected status from localstorage
                        var instanceIds = [];
                        if (status == 204) {
                            $scope.nwNmServicesStatus = 'false';
                            $window.localStorage[NwNmService.serviceID()] = 'false';
                            $scope.nwNmMessages = [];

                        }

                        if (status == 200) {
                            $scope.nwNmServicesStatus = 'true';
                            $window.localStorage[NwNmService.serviceID()] = 'true';


                            angular.forEach(services, function (service) {
                                $scope.nwNmServices.push(service);
                                service.selected = $window.localStorage[service.instanceId] == 'true';
                                if (service.selected) {
                                    instanceIds.push(service.instanceId);
                                }
                            });

                            // Load messages for all the selected service instances
                            var mainType = null;
                            if ($scope.nwNmType.NW && !$scope.nwNmType.NM) {
                                mainType = 'NW';
                            } else if (!$scope.nwNmType.NW && $scope.nwNmType.NM) {
                                mainType = 'NM';
                            }
                            if ($window.localStorage[NwNmService.serviceID()]) {
                                NwNmService
                                    .getPublishedNwNm(instanceIds, $scope.nwNmLanguage, mainType, wkt)
                                    .success(function (messages) {
                                        $scope.nwNmMessages = messages;
                                    });
                            }
                        }
                    })
                    .error(function (error) {
                        growl.error("Error getting NW NM service from Service Register.");
                        $window.localStorage[NwNmService.serviceID()] = 'false';
                        $scope.nwNmServicesStatus = 'false';

                        $log.debug("Error getting NW NM service. Reason=" + error);
                    })
            };


            /** Called when the NW-NM type selection has been changed **/
            $scope.nwNmTypeChanged = function () {
                $window.localStorage['nwNmShowNw'] = '' + $scope.nwNmType.NW;
                $window.localStorage['nwNmShowNm'] = '' + $scope.nwNmType.NM;
                $scope.loadServicesFromRegistry();
            };


            /** Update the selected status of the service **/
            $scope.nwNmSelected = function (service) {
                $window.localStorage[service.instanceId] = service.selected;
                $scope.loadServicesFromRegistry();
            };


            /** Show the details of the message */
            $scope.showNwNmDetails = function (message) {
                NwNmService.showMessageInfo(message);
            };


            /** Returns the area heading for the message with the given index */
            $scope.nwnmAreaHeading = function (index) {
                var msg = $scope.nwNmMessages[index];
                return NwNmService.getAreaHeading(msg);
            };


            /** Toggle the selected status of the layer **/
            $scope.toggleLayer = function (layer) {
                (layer.getVisible() == true) ? layer.setVisible(false) : layer.setVisible(true); // toggle layer visibility
                if (layer.getVisible()) {
                    growl.info('Activating ' + layer.get('title') + ' layer');
                    $window.localStorage.setItem(layer.get('title'), true);
                }else{
                    $window.localStorage.setItem(layer.get('title'), false);
                }
            };

            /** Toggle the selected status of the service **/
            $scope.toggleService = function (service) {
                service.selected = (service.selected != true); // toggle layer visibility
                if (service.selected) {
                    growl.info('Activating ' + service.name + ' layer');
                }
            };

            /** Toggle the selected status of the service **/
            $scope.switchBaseMap = function (basemap) {
                angular.forEach($scope.mapBackgroundLayers.getLayers().getArray(), function (value) { // disable every basemaps
                    // console.log("disabling " + value.get('title'));
                    value.setVisible(false)
                });
                basemap.setVisible(true);// activate selected basemap
                growl.info('Activating map ' + basemap.get('title'));
            };

            /** Toggle the selected status of the service **/
            $scope.toggleSeaMap = function () {
                $log.debug(" Toogle sea maps");
                if ($scope.loggedIn) {
                    angular.forEach($scope.mapBackgroundLayers.getLayers().getArray(), function (value) { // disable every basemaps
                        // console.log("disabling " + value.get('title'));
                        value.setVisible(false)
                    });
                    angular.forEach($scope.mapSeaMapLayer.getLayers().getArray(), function (value) { // disable/enable every basemaps
                        $log.debug(value + " value.getVisible()=" + value.getVisible());
                        value.setVisible(!value.getVisible());
                        if (!value.getVisible()) {
                            $scope.mapBackgroundLayers.getLayers().getArray()[0].setVisible(true); // default to standard map when disabling
                        }
                    });
                    growl.info('Activating combined nautical chart');
                } else {
                    growl.info("You need to login to access Nautical charts");
                    $scope.mapBackgroundLayers.getLayers().getArray()[0].setVisible(true);
                }
            };

            /** Toggle the selected status of the service **/
            $scope.switchService = function (groupLayers, layerToBeActivated) {
                angular.forEach(groupLayers, function (layerToBeDisabled) { // disable every basemaps
                    layerToBeDisabled.setVisible(false);
                    //$log.debug(" ol disabling " + layerToBeDisabled.get('id'));
                    $window.localStorage.setItem(layerToBeDisabled.get('id'), false);
                });

                layerToBeActivated.selected = (layerToBeActivated.selected != true); // toggle service visibility. if already active
                if (layerToBeActivated.selected) {
                    layerToBeActivated.setVisible(true);// activate selected basemap
                    growl.info('Activating map ' + layerToBeActivated.get('title'));
                    $window.localStorage.setItem(layerToBeActivated.get('id'), true);
                }


            };

            $scope.showVesselDetails = function (vessel) {
                $log.info("mmsi" + vessel);
                //var vesselDetails = VesselService.details(vessel.mmsi);
                VesselService.showVesselInfoFromMMsi(vessel);
                //console.log("App Ctr received = vesselDetails" +JSON.stringify(vesselDetails));
                //growl.info("got vesseldetails " + JSON.stringify(vesselDetails));
                growl.info("Vessel details retrieved");

            };

            /**
             * store all features in local storage, on a server or right now. Throw them on the root scope.
             */
            $scope.redirectToFrontpage = function () {
                $scope.loading = true;
                $log.debug("redirect to Frontpage");
                var redirect = function () {
                    //$rootScope.showgraphSidebar = true; // rough enabling of the sidebar
                    // TODO use routing...
                    $scope.loading = false;
                    $window.location.href = '#';
                };
                $timeout(redirect, 100);
            };

        }]);

