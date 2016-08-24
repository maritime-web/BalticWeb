angular.module('maritimeweb.vessel')

/** Service for accessing AIS vessel data **/
    .service('VesselService', ['$http', 'growl', '$uibModal',
        function ($http, growl, $uibModal) {

            /** Returns the AIS vessels within the bbox */
            this.getVesselsInArea = function (zoomLvl, bbox) {
                var url = '';
                if (zoomLvl > 8) { // below  zoom level 8 a more detailed and data rich overview is created.
                    url += "rest/vessel/listarea?area=" + bbox;
                } else {
                    url += "rest/vessel/overview?area=" + bbox;
                }
                return $http.get(url);
            };

            /** Returns the details for the given MMSI **/
            this.details = function (vessel) {
                console.log("getting details in VesselService");
                return $http.get('/rest/vessel/details?mmsi=' + encodeURIComponent(vessel.mmsi));
            };
            /** Returns the details for the given MMSI **/
            this.detailsMMSI = function (mmsi) {
                console.log("getting details in VesselService");
                return $http.get('/rest/vessel/details?mmsi=' + encodeURIComponent(mmsi));
            };

            /** Open the message details dialog **/
            this.showVesselInfoFromMMsi = function (mmsi) {
                console.log("showVesselInfoFromMMsi mmsi=" + mmsi);

                var message = this.detailsMMSI(mmsi);
                return $uibModal.open({
                    controller: "VesselDialogCtrl",
                    templateUrl: "/vessel/vessel-details-dialog.html",
                    size: 'lg',
                    keyboard: 'true',
                    backdrop: 'static',
                    animation: 'true',
                    resolve: {
                        message: function () {
                            return message;
                        }
                    }
                });
            };


            /** Saves the vessel details **/
            this.saveDetails = function (details) {
                return $http.post("/rest/vessel/save-details", details);
            };

            /** Returns the historical tracks for the given MMSI **/
            this.historicalTrack = function (mmsi) {
                return $http.get('/rest/vessel/historical-track?mmsi=' + encodeURIComponent(mmsi));
            };


            /** Returns the image and type text for the given vessel **/
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

        }]);