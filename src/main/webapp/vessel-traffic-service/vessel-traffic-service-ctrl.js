/**
 * Handles the VTS report interface - uses the bootstrap modal form.
 * Uses
 *
 * Minimap is init and populated from helper service.
 * Uses localstorage for referencing loaded route objects. (RTZ)
 *
 * Please note that there are 2 services related to VTS: mapVtsAreaService and VesselTrafficServiceReportCtrl.
 * They are different from each by that the mapservice has a map layer, much like other mapping services,
 * and the VesselTrafficServiceReport is the form to write reports to VTS centres.
 *
 */


angular.module('maritimeweb.app').controller('VesselTrafficServiceReportCtrl', ['$scope', '$uibModalInstance', '$window', '$sce', 'growl', '$http', 'Auth', 'VesselService', 'VtsHelperService',
    function ($scope, $uibModalInstance, $window, $sce, growl, $http, Auth, VesselService, VtsHelperService) {




        //Populate the interface using json object from service - anon allowed
        var VTSData = []; //local array
        var getInterfaceObjectsFromService = function () {
            $http({ // get the VTS definition objects from service - builds the correct buttons/fields
                url: '/rest/vtsinterface',
                method: "GET",
                data: "",
                headers: {'Content-Type': 'application/text'}
            })
                .then(function (data) {
                        var parsedData = JSON.parse(JSON.stringify(data));
                        growl.success("VTS interface population service was successful.");
                        $scope.VTSCenterData = parsedData.data.VtsJsObjects;
                        VTSData = $scope.VTSCenterData; //place in local
                        VTSData.unshift(null); //has to start from item 1 - OL3 vector features cannot have ID=0.
                        angular.element(document.querySelector('#modalbody')).removeClass('ng-hide'); //is hidden until angular is ready
                        $scope.vtsTimeStamp = moment.utc().format('HH : mm');//display date and time in utc
                        $scope.vtsDateStamp = moment()._locale._weekdaysShort[moment().day()] + " " + moment.utc().format('DD MMM YYYY');
                        angular.element(document.querySelector(".datetime-input.date .display .date")).html(moment().format('DD MMM YYYY')); //update timepicker display with now
                        angular.element(document.querySelector(".datetime-input.time .display .time")).html(moment.utc().format('HH : mm')); //update timepicker display with now
                        VtsHelperService.miniMapLoad(); //must be loaded before populating
                    },
                    function (data) { // error
                        growl.error("VTS interface population service could not be contacted. Please check your internet connection and try again.\nRetrying in 5 seconds.");
                    });
        };
        getInterfaceObjectsFromService();

        //User credentials and login  ******************************************************************************
        $scope.login = function () {
            Auth.authz.login(); //calls login function
        };
        $scope.isLoggedIn = Auth.loggedIn; //declare scope login state
        $scope.showReportInterface = true;


        //Declarations and settings ********************************************************************************
        VTSData = $scope.VTSCenterData; //local var so interface doesnt update all the time
        $scope.VTSID = -1; //for reference when validating - -1 is pristine

        //DWT multiplier may be needed to force display of fuel types, according to National Single Window project. Not implemented in BalticWeb yet.
        $scope.vesselTypes = [
            {type: "General Cargo", DWTmultiplier: 0.5285},
            {type: "Bulk Carrier", DWTmultiplier: 0.5285},
            {type: "Container Ship", DWTmultiplier: 0.8817},
            {type: "Oil Tanker", DWTmultiplier: 0.5354},
            {type: "LNG transport", DWTmultiplier: 1.13702},
            {type: "LPG transport", DWTmultiplier: 0.8447},
            {type: "Fishing boat (A)", DWTmultiplier: 0}, //0=ignore
            {type: "Fishing boat (B)", DWTmultiplier: 0},
            {type: "Fishing Processer (B)", DWTmultiplier: 0},
            {type: "Non-Fishing Processer (C)", DWTmultiplier: 0},
            {type: "Oil Industry Vessel", DWTmultiplier: 999999}, //999999=always show fuel details
            {type: "Passenger Ship", DWTmultiplier: 8.9393},
            {type: "Ferry", DWTmultiplier: 0},
            {type: "Tug boat", DWTmultiplier: 0},
            {type: "Barge", DWTmultiplier: 0},
            {type: "Other", DWTmultiplier: 999999}
        ];
        /*
         A regression analysis performed by "The National Institute for Land and Infrastructure Management in Japan":
         Source (23.08.2011): nilim.go.jp/lab/bcg/siryou/tnn/tnn0309pdf/ks0309010.pdf
         *********************************
         1 GT to DWT multiplier - example: General Cargo, 300 GrossTonnage = (300 * 0.5285) = 158.55 DeadWeightTonnage
         *********************************
         Oil Tankers 0.5354
         Bulkers 0.5285
         General Cargo 0.5285
         Container 0.8817
         Passenger 8.9393
         LNG 1.13702
         LPG 0.8447
         Car Carrier 2.7214
         RoRo 1.7803 (roll on roll off)
         *********************************
         */


        $scope.fuelTypes = [
            {name: "HFO", description: "Heavy Fuel Oil"},
            {name: "IFO", description: "Intermediate Fuel Oil"},
            {name: "MDO", description: "Marine Diesel Oil"},
            {name: "MGO", description: "Marine Gas Oil"},
            {name: "LPG", description: "Liquid Petroleum Gas"},
            {name: "LNG", description: "Liquid Natural Gas"},
            {name: "Other", description: ""}
        ];

        //displays as vessel type input but is really a cargo definition
        $scope.cargoTypes = ["None", "Ballast", "Bulk - grain", "Bulk - other than grain", "Chemicals", "Container/Trailer", "General Cargo", "Gas", "Oil", "Passenger", "Reefer", "Other"];

        //Specific VTS center route dropdowns (They usually have predefined routes and abbreviations as specified in their pilot/master's guide )
        // CURRENTLY NOT IN USE - PENDING DECISION REGARDING STANDARD.
        // $scope.BELTREPRoutes = ["", ""];
        // $scope.SOUNDREPRoutes = ["", ""];


        //Input validation area

        //ETA at VTS
        $scope.vtsForceUTCTimeCheckBoxState = false; //checkbox to activate UTC timezone
        $scope.vtsUtcTime = moment.utc().format('HH : mm');
        $scope.vtsLocalDate = moment().format('DD MMM YYYY');
        $scope.setvtsvesseletaTimeDateValid = false;
        $scope.detectedRouteETA = null; //If a route is loaded, with valid ETAs, this will contain the calculated ETA

        //vessel information
        $scope.vtsvesselnameinput = "";
        $scope.vtsvesselcallsigninput = "";
        $scope.vtsvesselmmsiinput = "";
        $scope.vtsvesselmmsilabel = "MMSI: ";
        $scope.vtsvesselimoinput = "";
        $scope.vtsvesselimolabel = "IMO";
        $scope.vtsvesseldraughtinput = "";
        $scope.vtsvesselairdraughtinput = "";
        $scope.vtsvesselpersonsinput = "";
        $scope.vtsvessellengthinput = "";
        $scope.vtsvesseldeadweightinput = "";
        $scope.vtsvesselgrosstonnageinput = "";
        $scope.vtsvesseldefectsinput = "";
        $scope.vtsvesseltypeholder = "";
        $scope.vtsvesselfuelquantityinput = "";
        $scope.vtsvesselspeedinput = "";

        //fuel information
        $scope.vtsvesselfueltype00_0input = "";
        $scope.vtsvesselfueltype00_1input = "";
        $scope.vtsvesselfueltype00_2input = "";
        $scope.vtsvesselfueltype01_0input = "";
        $scope.vtsvesselfueltype01_1input = "";
        $scope.vtsvesselfueltype01_2input = "";
        $scope.vtsvesselfueltype02_0input = "";
        $scope.vtsvesselfueltype02_1input = "";
        $scope.vtsvesselfueltype02_2input = "";
        $scope.vtsvesselfueltype03_0input = "";
        $scope.vtsvesselfueltype03_1input = "";
        $scope.vtsvesselfueltype03_2input = "";
        $scope.vtsvesselfueltype04_0input = "";
        $scope.vtsvesselfueltype04_1input = "";
        $scope.vtsvesselfueltype04_2input = "";
        $scope.vtsvesselfueltype05_0input = "";
        $scope.vtsvesselfueltype05_1input = "";
        $scope.vtsvesselfueltype05_2input = "";
        $scope.vtsvesselfueltype06_0input = "";
        $scope.vtsvesselfueltype06_1input = "";
        $scope.vtsvesselfueltype06_2input = "";

        //Cargo information
        $scope.selectedCargoType = "";
        $scope.selectedDangerousCargoType = "";
        $scope.vtsvesselcargossnidinput = "";
        $scope.cargoAddToManifestDisabled = true;
        $scope.vtsdangerouscargotonnageinput = "";
        $scope.vtsvesselcargotypeholder = "";
        $scope.vtsdangerouscargooverboard = "";
        $scope.vtscargodesignatedpersonashorenameinput = "";
        $scope.vtscargodesignatedpersonashoretelephoneinput = "";
        $scope.vtscargodesignatedpersonashoreemailinput = "";
        $scope.vtsDangCargoCheckBoxState = false; //checkbox to activate dangerous cargo in case is not default
        $scope.vtsDangCargoCheckDisabled = true;
        // $scope.setvtsCargoSSNIDValid = false; //SafeSeaNet manifest ID reference - has full manifest - should be autofilled through service at login - requires cooperation with SSN
        $scope.setvtsCargoTypeValid = false;
        $scope.setvtsCargoTonnageValidState = 'invalid'; //'invalid, valid, neutral' - can be not mandatory
        $scope.setvtsvesselContactDetailsValid = false;
        $scope.setvtsvesselDPANameValid = false;
        $scope.setvtsvesselDPAPhoneValid = false;
        $scope.setvtsvesselDPAEmailValid = false;

        //vessel information - should probably be autofilled through service at login if available
        $scope.setvtsvesselnameValid = false;
        $scope.setvtsvesselcallsignValid = false;
        $scope.setvtsvesselMMSIValid = false;
        $scope.setvtsvesselIMOValid = false;
        $scope.setvtsvesselDraughtValid = false;
        $scope.setvtsvesselAirDraughtValid = false;
        $scope.setvtsvesselPersonsValid = false;
        $scope.setvtsvesselLengthValid = false;
        $scope.setvtsVesselTypeValid = false;
        $scope.setvtsvesselDeadWeightValid = false;

        $scope.fuelDetailsValid = false; //never used in UI

        //voyage information - should be autofilled through service at login if available
        $scope.vtsvesselposlondegreesinput = "";
        $scope.vtsvesselposlonminutesinput = "";
        $scope.vtsvesselposlatdegreesinput = "";
        $scope.vtsvesselposlatminutesinput = "";
        $scope.vtscargoadditionalcontactdetailsinput = "";
        $scope.vtsvesseltruecourseinput = "";
        $scope.vtsvesselportofdestinationinput = "";
        $scope.vtsvesselportofdestinationetalabel = "";

        $scope.setvtsvesselPosLonDegreesValid = false;
        $scope.setvtsvesselPosLatDegreesValid = false;
        $scope.setvtsvesselPosLonMinutesValid = false;
        $scope.setvtsvesselPosLatMinutesValid = false;
        $scope.showCourseOverGround = false;
        $scope.showPortOfDestinationEta = false;
        $scope.showPortOfDestination = false;
        $scope.showVesselCurrentPosition = false;
        $scope.showTrueCourse = false;
        $scope.showRoute = false;

        $scope.setvtsvesselSpeedValid = false;
        $scope.setvtsvesselTrueCourseValid = false;
        $scope.setvtsvesselCourseOverGroundValid = false;
        $scope.setvtsEtaDateValid = false;
        $scope.setvtsEtaTimeValid = false;
        $scope.setvtsvesselPortOfDestinationValid = false;
        $scope.setvtsvesselRouteValid = false;
        $scope.courseOverGroundValid = true;

        $scope.VTSReadyToSend = false; //global readystate

        $scope.areaWKT = "";
        $scope.showVtsAreaMiniMap = false; //only displays if there is a WKT of VTS area extent available in model

        $scope.reportSummary = { //what is sent to the VTS

            //VTS information
            vtsShortName: "",
            vtsCallSign: "",
            vtsEmail: "",

            //Vessel information
            vesselName: "",
            vesselCallSign: "",
            vesselMMSI: 0,
            vesselIMO: 0,
            vesselDraught: 0, //Metres, 1 decimal
            vesselAirDraught: 0, //Metres, 1 decimal
            vesselPersonsOnboard: 0,
            vesselLength: 0, //Metres, 1 decimal
            vesselDeadWeight: 0, //Tonnes, not mandatory yet
            vesselGRT: 0, //Tonnes (GrossTonnage, a.k.a GT)
            vesselDefects: "", //String, any length
            vesselType: "", //String, predefined by dropdown.

            //Fuel and fuel types - always in Tonnes (metric)
            fuelTotalFuel: 0, //Tonnes - all fuel added up
            fuelTypeHFORegular: 0, //Tonnes - regular means it is not low sulphur. (Heavy Fuel Oil)
            fuelTypeHFOLowSulphur: 0,
            fuelTypeHFOUltraLowSulphur: 0,
            fuelTypeIFORegular: 0, //(Intermediate Fuel Oil)
            fuelTypeIFOLowSulphur: 0,
            fuelTypeIFOUltraLowSulphur: 0,
            fuelTypeMDORegular: 0, //(Marine Diesel Oil)
            fuelTypeMDOLowSulphur: 0,
            fuelTypeMDOUltraLowSulphur: 0,
            fuelTypeMGORegular: 0, //(Marine Gas Oil)
            fuelTypeMGOLowSulphur: 0,
            fuelTypeMGOUltraLowSulphur: 0,
            fuelTypeLPG: 0, //(Liquid Petroleum Gas)
            fuelTypeLNG: 0, //(Liquid Natural Gas)

            //Cargo Information in Tonnes, 3 decimals
            cargoEntries: [
                // {
                // imoClass:"",
                // tonnage:"",
                // note:""
                // }
            ],
            cargoType: "", //String, predefined by dropdown. Certain cargotypes demand listing of dangerous cargo/goods (DG/DC)
            cargoDangerousCargoOnBoard: false, //any cargoIMOClass on board makes this true
            cargoDangerousCargoTotalTonnage: 0, //sum of all dangerous cargo
            cargoIMOClassesOnBoard: "", //String of IMO DG Classes ex: "1,4,6,7" - required by some VTS
            cargoPollutantOrDCLostOverBoard: "", //String, description of what, where, how, when
            cargoAdditionalContactInformation: "", //String - additional contact information for owner or handler of cargo on shore
            cargoDPAName: "", //String - Name of Designated Person Ashore (DPA) or Cargo Agent.
            cargoDPATelephone: "", //String telephone number of DPA
            cargoDPAEmail: "", //String email of DPA

            //Voyage information
            voyagePositionLon: 0, //Longitude position of vessel, format is degrees and decimal minutes, ex: 12,59.9999 - (0-90),(0-60).(0-9999) (N/S)
            voyagePositionLat: 0, //Latitude position of vessel, format is degrees and decimal minutes, ex: 55,39.9999 - (0-180),(0-60).(0-9999) (E/W)
            voyageSpeed: 0, //Current speed of vessel, in knots
            voyageTrueCourse: 0, //Current true heading, 0-360 degrees, 1 decimal
            voyageCourseOverGround: 0, //COG from AIS
            voyageVTSETADateTime: "", //String - Arrival date at VTS area, DD-MM-YYYY HH:mm
            voyagePortOfDestination: "", //String - name of port
            voyagePortOfDestinationEta: "" //string ETA from AIS - is only visible if AIS data is used - cannot be edited.
        };

        $scope.aisData = { //data prefetched from AIS - used with MMSI
            vesselName: "",
            vesselCallsign: "",
            vesselImo: "",
            vesselCog: "", //course over ground
            vesselDestination: "",
            vesselDraught: "",
            vesselHeading: "", //used in minimap
            vesselLat: "", //used in minimap and position
            vesselLon: "", //used in minimap and position
            vesselLength: "",
            vesselNavStatus: "",
            vesselRot: "", //rate of turn
            vesselSog: "", //speed over ground
            vesselType: "",
            vesselWidth: ""
        };
        $scope.aisdataReady = false;


        //test if all items check out, then display "SEND" button
        $scope.VTSValidationAllDone = function () {

            //Validation exceptions for not required fields
            if (!VTSData[$scope.VTSID].showMaxDraught) $scope.setvtsvesselDraughtValid = true;
            if (!VTSData[$scope.VTSID].showAirDraught) $scope.setvtsvesselAirDraughtValid = true;
            if (!VTSData[$scope.VTSID].showDeadWeightTonnage) $scope.setvtsvesselDeadWeightValid = true;
            if (!VTSData[$scope.VTSID].showVesselType) $scope.setvtsVesselTypeValid = true;
            if (!VTSData[$scope.VTSID].showVesselLength) $scope.setvtsVesselLengthValid = true;
            if (!VTSData[$scope.VTSID].showFuelDetails) $scope.fuelDetailsValid = true;
            if (!$scope.showCourseOverGround) $scope.courseOverGroundValid = true;
            if (!$scope.showVesselCurrentPosition) {
                $scope.setvtsvesselPosLonDegreesValid = true;
                $scope.setvtsvesselPosLatDegreesValid = true;
                $scope.setvtsvesselPosLonMinutesValid = true;
                $scope.setvtsvesselPosLatMinutesValid = true;
            }
            if (!$scope.showTrueCourse) $scope.setvtsvesselTrueCourseValid = true;
            //if(!$scope.showRoute) $scope.setvtsvesselRouteValid = true; //TODO: import RTZ or use VTS routes from model/service.
            if (!$scope.showPortOfDestination) $scope.setvtsvesselPortOfDestinationValid = true;

            //exception for cargo information - subject to change
            // if($scope.showCargoContactInformationInput == false) $scope.setvtsvesselContactDetailsValid = true;

            //grouped valid states to make easier debug
            var group1valid = false,
                group2valid = false,
                group3valid = false,
                group4valid = false,
                group5valid = true, //position not mandatory but subject to change
                group6valid = true, //position not mandatory but subject to change
                group7valid = false,
                group8valid = false;

            if ($scope.setvtsvesselnameValid && $scope.setvtsvesselcallsignValid && $scope.setvtsvesselMMSIValid) group1valid = true;
            if ($scope.setvtsvesselIMOValid && $scope.setvtsvesselDraughtValid && $scope.setvtsvesselAirDraughtValid) group2valid = true;
            if ($scope.setvtsvesselPersonsValid && $scope.setvtsVesselTypeValid && $scope.setvtsVesselLengthValid) group3valid = true;
            if ($scope.fuelDetailsValid && $scope.setvtsCargoTypeValid) group4valid = true;
            // if($scope.setvtsvesselPosLonDegreesValid && $scope.setvtsvesselPosLatDegreesValid) group5valid = true; //position not mandatory but subject to change
            // if($scope.setvtsvesselPosLonMinutesValid && $scope.setvtsvesselPosLatMinutesValid) group6valid = true;
            if ($scope.setvtsvesselSpeedValid && $scope.setvtsvesselPortOfDestinationValid) group7valid = true;
            if ($scope.setvtsvesseletaTimeDateValid && $scope.setvtsvesselCourseOverGroundValid && $scope.setvtsvesselTrueCourseValid) group8valid = true;

            VtsHelperService.displayTotalSumOfBytesInLocalStorage(); //only if debugmode
            console.log("");
            console.log("");
            if (group1valid) console.log("group1valid", group1valid);
            if (group2valid) console.log("group2valid", group2valid);
            if (group3valid) console.log("group3valid", group3valid);
            if (group4valid) console.log("group4valid", group4valid);
            if (group5valid) console.log("group5valid", group5valid);
            if (group6valid) console.log("group6valid", group6valid);
            if (group7valid) console.log("group7valid", group7valid);
            if (group8valid) console.log("group8valid", group8valid);

            if (group1valid == false || group2valid == false || group3valid == false || group4valid == false || group5valid == false || group6valid == false || group7valid == false || group8valid == false) {
                $scope.VTSReadyToSend = false;
            } else {
                if ($scope.isLoggedIn) {
                    $scope.VTSReadyToSend = true;
                }
            }
            console.log("VTSReadyToSend", $scope.VTSReadyToSend);

        };


        $scope.toggleDateValid = function (valid) {
            $scope.setvtsvesseletaTimeDateValid = valid;
            if (valid == true) {
                angular.element(document.querySelector(".vts-timedatepicker.datetime-input.date .display")).addClass("vts-datetime-picker-box-highlight-valid");
                angular.element(document.querySelector(".vts-timedatepicker.datetime-input.date .display")).removeClass("vts-datetime-picker-box-highlight-invalid");
            } else {
                angular.element(document.querySelector(".vts-timedatepicker.datetime-input.date .display")).removeClass("vts-datetime-picker-box-highlight-valid");
                angular.element(document.querySelector(".vts-timedatepicker.datetime-input.date .display")).addClass("vts-datetime-picker-box-highlight-invalid");
            }
        };
        $scope.toggleTimeValid = function (valid) {
            $scope.setvtsvesseletaTimeDateValid = valid;
            if (valid == true) {
                angular.element(document.querySelector(".vts-timedatepicker.datetime-input.time .display")).addClass("vts-datetime-picker-box-highlight-valid");
                angular.element(document.querySelector(".vts-timedatepicker.datetime-input.time .display")).removeClass("vts-datetime-picker-box-highlight-invalid");
            } else {
                angular.element(document.querySelector(".vts-timedatepicker.datetime-input.time .display")).removeClass("vts-datetime-picker-box-highlight-valid");
                angular.element(document.querySelector(".vts-timedatepicker.datetime-input.time .display")).addClass("vts-datetime-picker-box-highlight-invalid");
            }
            $scope.VTSValidationAllDone();
        };

        $scope.validateEtaTimeDate = function () {
            var isSameDay = moment(moment($scope.vtsLocalDate, 'DD MMM YYYY')).isSame(moment(moment().format('MMM DD YYYY')));
            var isAfterDay = moment(moment($scope.vtsLocalDate, 'DD MMM YYYY')).isAfter(moment(moment().format('YYYY-MM-DD')));
            var isAfterTime = $scope.vtsUtcTime.replace(":", "").replace("  ", "") > moment.utc().format('HH:mm').replace(":", "");
            if (isAfterDay) {
                $scope.toggleDateValid(true);
            } else if (isSameDay && isAfterTime) {
                $scope.toggleDateValid(true);
            } else {
                $scope.toggleDateValid(false);
            }
            if (isAfterDay) {
                $scope.toggleTimeValid(true);
            } else if (isAfterTime && isSameDay) {
                $scope.toggleTimeValid(true);
            } else {
                $scope.toggleTimeValid(false);
            }
        };

        // $scope.dateInputChange = function (now) {
        //     $scope.vtsLocalDate = moment(now._d).format('DD MMM YYYY');
        //     console.log("date:",$scope.vtsLocalDate);
        //     $scope.vtsDateStamp = moment()._locale._weekdaysShort[moment().day()] + " " + $scope.vtsLocalDate;
        //     console.log("date2:",$scope.vtsDateStamp);
        //
        //     //angular.element(document.querySelector("#timedateutclabel")).html($scope.vtsLocalDate); //force to short month name
        //     angular.element(document.querySelector(".datetime-input.date .display .date")).html(moment().format('DD MMM YYYY')); //update timepicker display with now
        //     $scope.validateEtaTimeDate();
        // };



        $scope.dateInputChange = function (now) {
            $scope.vtsLocalDate = moment(now._d).format('DD MMM YYYY');
            $scope.vtsDateStamp = moment()._locale._weekdaysShort[moment().day()] + " " + $scope.vtsLocalDate;
            angular.element(document.querySelector(".datetime-input.date .display .date")).html($scope.vtsLocalDate); //update timepicker display correct month format
            angular.element(document.querySelector("#timedateutclabel")).html($scope.vtsDateStamp + " - " + $scope.vtsUtcTime + " UTC"); //workaround to force update
            $scope.validateEtaTimeDate();
        };

        $scope.timeInputChange = function (now) {
            $scope.vtsUtcTime = moment.utc(now._d).format('HH : mm'); //update selected time
            angular.element(document.querySelector(".vts-timedatepicker.datetime-input.time .display .time")).html($scope.vtsUtcTime);
            //display time and date on label
            angular.element(document.querySelector("#timedateutclabel")).html($scope.vtsDateStamp + " - " + $scope.vtsUtcTime + " UTC"); //update timepicker with now
            $scope.validateEtaTimeDate();
        };

        $scope.timeInputClick = function () { //styles the timepicker display
            angular.element(document.querySelector(".vts-timedatepicker.datetime-input.time .edit-popover .header")).addClass('ng-hide'); //remove redundant datepicker
            angular.element(document.querySelector(".vts-timedatepicker.datetime-input.time .edit-popover")).addClass('time-picker-mod'); //make smaller
            angular.element(document.querySelector(".vts-timedatepicker.datetime-input.time .timer .timer-seconds")).addClass('ng-hide'); //remove seconds
            angular.element(document.querySelector(".vts-timedatepicker.datetime-input.time .clear-button")).addClass('ng-hide'); //remove clear button
            angular.element(document.querySelector(".vts-timedatepicker.datetime-input.time .timer div:nth-child(4)")).addClass("ng-hide");
        };

        //length greater than 1
        $scope.VTSVesselNameValidation = function (validate) {
            ($scope.vtsvesselnameinput.length > 1) ? $scope.setvtsvesselnameValid = true : $scope.setvtsvesselnameValid = false;
            if (validate) $scope.VTSValidationAllDone(); //If all done, display send button
        };

        //length greater than 1
        $scope.VTSVesselCallsignValidation = function (validate) {
            ($scope.vtsvesselcallsigninput.length > 1) ? $scope.setvtsvesselcallsignValid = true : $scope.setvtsvesselcallsignValid = false;
            if (validate) $scope.VTSValidationAllDone();
        };

        //Only 9 numbers, no letters
        $scope.VTSVesselMMSIValidation = function () {
            var input = $scope.vtsvesselmmsiinput;
            if (input.length == 9) {
                $scope.setvtsvesselMMSIValid = true;
                $scope.getAisDataByMmsi(); //fetches data, loads into placeholders, but does not populate
            } else if (input.length > 9) {
                input = input.substring(0, 9);
            } else {
                $scope.setvtsvesselMMSIValid = false;
                $scope.aisdataReady = false;
                $scope.clearAISPlaceholders();
            }
            input = input.toString().replace(/\D/g, '');
            $scope.vtsvesselmmsiinput = input;
            $scope.vtsvesselmmsilabel = $sce.trustAsHtml("MMSI: " + input); //send to label
            $scope.VTSValidationAllDone();
        };

        //Only 10 chars, "IMO" (hardcoded) followed by 7 numbers.
        $scope.VTSVesselIMOValidation = function (validate) {
            try {
                var input = $scope.vtsvesselimoinput;
                input = input.toString().replace(/\D/g, '');
                if (input.length == 7) {
                    $scope.setvtsvesselIMOValid = true;
                } else if ($scope.vtsvesselimoinput.length > 7) {
                    input = input.substring(0, 7);
                    $scope.setvtsvesselIMOValid = true;
                } else {
                    $scope.setvtsvesselIMOValid = false;
                }
                $scope.vtsvesselimoinput = input; //send cleaned to input field
                $scope.vtsvesselimolabel = $sce.trustAsHtml("IMO" + input); //send to label
                if (validate) $scope.VTSValidationAllDone();
            } catch (do_nothing_imo) {
            }
        };

        //max 99.9
        $scope.VTSVesselDraughtValidation = function (validate) {
            if ($scope.showMaxDraught) {
                var test = VtsHelperService.validateNumber($scope.vtsvesseldraughtinput, 0, 99, 1);
                $scope.vtsvesseldraughtinput = test.val;
                (test.valid == true) ? $scope.setvtsvesselDraughtValid = true : $scope.setvtsvesselDraughtValid = false;
                if (validate) $scope.VTSValidationAllDone();
            }
        };

        //max 99.9
        $scope.VTSVesselAirDraughtValidation = function (validate) {
            var test = VtsHelperService.validateNumber($scope.vtsvesselairdraughtinput, 0, 99, 3);
            (test.valid == true) ? $scope.setvtsvesselAirDraughtValid = true : $scope.setvtsvesselAirDraughtValid = false;
            $scope.vtsvesselairdraughtinput = test.val;
            if (validate) $scope.VTSValidationAllDone();
        };

        //minimum 1 person on board
        $scope.VTSVesselPersonsValidation = function () {
            var persons = $scope.vtsvesselpersonsinput;
            persons = persons.toString().replace(/\D/g, '');
            if (persons.length > 5) persons = persons.substring(0, 5);
            (parseInt(persons) > 0) ? $scope.setvtsvesselPersonsValid = true : $scope.setvtsvesselPersonsValid = false;
            $scope.vtsvesselpersonsinput = persons; //send cleaned to input field
            $scope.VTSValidationAllDone();
        };

        $scope.selectVesselTypeChange = function (selectedItem) {
            $scope.setvtsVesselTypeValid = true;
            $scope.vtsvesseltypeholder = selectedItem.type;
        };

        //cant be longer than 999m
        $scope.VTSVesselLengthValidation = function (validate) {
            var inputStr = $scope.vtsvessellengthinput + "", isValid = false;
            if (inputStr == "." || inputStr == "0.0") {
                $scope.vtsvessellengthinput = "0.";
                inputStr = "0."
            }
            if (inputStr.length > 5) inputStr = inputStr.substring(0, 5); //cant be longer than 999.9
            if (inputStr.indexOf(".") !== -1 && inputStr.length > 2 && (inputStr.substring(inputStr.length - 1, inputStr.length) != ".")) { //has period - treat as float
                var inputFloat = parseFloat(inputStr);
                if (inputFloat.isNaN) inputFloat = 0.0;
                if (inputFloat > 0.1 && inputFloat < 1000.0) isValid = true;
                $scope.setvtsvesselLengthValid = isValid;
                inputStr = inputFloat;
            } else if (inputStr.indexOf(".") < 0 && inputStr.length > 0) { //No period, treat as int
                var inputInt = parseInt(inputStr);
                if (inputInt.isNaN) inputInt = 0;
                if (inputInt > 1 && inputInt < 999) isValid = true;
                $scope.setvtsvesselLengthValid = isValid;
                inputStr = inputInt;
            } else if (inputStr.length < 1) {
                $scope.setvtsvesselLengthValid = false;
            }
            $scope.vtsvessellengthinput = inputStr;
            if (validate) $scope.VTSValidationAllDone();
        };

        $scope.VTSVesselFuelQuantityValidation = function () {
            //may be neccessary
        };
        if ($scope.showFuelQuantity) {
            $scope.vtsvesselfuelquantityinput = $scope.vtsvesselfuelquantityinput.toString().replace(/\D/g, '');
        }

        //any number
        $scope.VTSVesselDWTValidation = function () {
            if ($scope.showDeadWeightTonnage) {
                var input = $scope.vtsvesseldeadweightinput.toString().replace(/\D/g, '');
                var inputInt = parseInt(input);
                (inputInt > 0 && input != "") ? $scope.setvtsvesselDeadWeightValid = true : $scope.setvtsvesselDeadWeightValid = false;
                $scope.vtsvesseldeadweightinput = input;
                $scope.VTSValidationAllDone();
            }
        };

        //just numbers - never mandatory input
        $scope.VTSVesselGTValidation = function () {
            if ($scope.showGrossTonnage) {
                $scope.vtsvesselgrosstonnageinput = $scope.vtsvesselgrosstonnageinput.toString().replace(/\D/g, '');
            }
        };


        $scope.retDecMinutesFromDecDegrees = function (decDeg) {
            var decMin = (decDeg + "").substring(0, (decDeg + "").indexOf("."));
            decMin = Math.round((60 * (decDeg - decMin)) * 10000) / 10000;
            decMin = Math.abs(decMin); //positive number
            return decMin
        };


        $scope.getLocation = function () {
            if (navigator.geolocation) {
                navigator.geolocation.getCurrentPosition(function (position) {

                    var lon = position.coords.longitude;
                    $scope.vtsvesselposlondegreesinput = (lon + "").substring(0, (lon + "").indexOf("."));
                    (position.coords.longitude > 0) ? $scope.vtsCurrentPosCompassAppendEW = "E" : $scope.vtsCurrentPosCompassAppendEW = "W"; //set N or S
                    lon = $scope.retDecMinutesFromDecDegrees(lon);
                    $scope.vtsvesselposlonminutesinput = lon;

                    var lat = position.coords.latitude;
                    $scope.vtsvesselposlatdegreesinput = (lat + "").substring(0, (lat + "").indexOf("."));
                    (position.coords.latitude > 0) ? $scope.vtsCurrentPosCompassAppendNS = "N" : $scope.vtsCurrentPosCompassAppendNS = "S"; //set E or W
                    lat = $scope.retDecMinutesFromDecDegrees(lat);
                    $scope.vtsvesselposlatminutesinput = lat;

                    //show to user is ok
                    $scope.setvtsvesselPosLonDegreesValid = true;
                    $scope.setvtsvesselPosLatDegreesValid = true;
                    $scope.setvtsvesselPosLatMinutesValid = true;
                    $scope.setvtsvesselPosLonMinutesValid = true;
                    $scope.VTSValidationAllDone();
                });
            } else {
                growl.error('Unable to retrieve current position, perhaps your browser is blocking this feature.');
            }
        };


        $scope.VTSVesselCurrentPositionLonDegValidation = function (validate) {
            if ($scope.showVesselCurrentPosition) { //only validate if displayed
                var retVal = VtsHelperService.positionDegMinValidation($scope.vtsvesselposlondegreesinput, 90);
                if ($scope.vtsvesselposlondegreesinput.substring(0, 1) == "-") {
                    $scope.vtsCurrentPosCompassAppendEW = "W";
                    $scope.vtsvesselposlondegreesinput = "-" + retVal.value;
                } else {
                    $scope.vtsCurrentPosCompassAppendEW = "E";
                    $scope.vtsvesselposlondegreesinput = retVal.value;
                }
                $scope.setvtsvesselPosLonDegreesValid = retVal.valid;
                if (validate) $scope.VTSValidationAllDone();
            }
        };
        $scope.VTSVesselCurrentPositionLonMinValidation = function (validate) {
            var selStart = 0;
            if (/[^0-9.]/g.test($scope.vtsvesselposlonminutesinput)) selStart = document.activeElement.selectionStart;
            if ($scope.showVesselCurrentPosition) { //only validate if displayed
                var retVal = VtsHelperService.positionDegMinValidation($scope.vtsvesselposlonminutesinput.toString().replace(/[^0-9.]/g, ''), "decimalminutes");
                $scope.vtsvesselposlonminutesinput = retVal.value;
                if (selStart != 0) VtsHelperService.setSelectionRange(document.activeElement, selStart - 1, selStart - 1); //put caret back if not entered number
                $scope.setvtsvesselPosLonMinutesValid = retVal.valid;
                if (validate) $scope.VTSValidationAllDone();
            }
        };
        $scope.VTSVesselCurrentPositionLatDegValidation = function (validate) {
            if ($scope.showVesselCurrentPosition) { //only validate if displayed
                var retVal = VtsHelperService.positionDegMinValidation($scope.vtsvesselposlatdegreesinput, 180);
                if ($scope.vtsvesselposlatdegreesinput.substring(0, 1) == "-") {
                    $scope.vtsCurrentPosCompassAppendNS = "S";
                    $scope.vtsvesselposlatdegreesinput = "-" + retVal.value;
                } else {
                    $scope.vtsCurrentPosCompassAppendNS = "N";
                    $scope.vtsvesselposlatdegreesinput = retVal.value;
                }
                $scope.setvtsvesselPosLatDegreesValid = retVal.valid;
                if (validate) $scope.VTSValidationAllDone();
            }
        };
        $scope.VTSVesselCurrentPositionLatMinValidation = function (validate) {
            var selStart = 0;
            if (/[^0-9.]/g.test($scope.vtsvesselposlonminutesinput)) selStart = document.activeElement.selectionStart;
            if ($scope.showVesselCurrentPosition) { //only validate if displayed
                var retVal = VtsHelperService.positionDegMinValidation($scope.vtsvesselposlatminutesinput.toString().replace(/[^0-9.]/g, ''), "decimalminutes");
                $scope.vtsvesselposlatminutesinput = retVal.value;
                if (selStart != 0) VtsHelperService.setSelectionRange(document.activeElement, selStart - 1, selStart - 1); //put caret back if not entered number
                $scope.setvtsvesselPosLatMinutesValid = retVal.valid;
                if (validate) $scope.VTSValidationAllDone();
            }
        };

        //fuel types - number 999999999, no decimal. One filled makes all other neutral because only one field is mandatory
        $scope.VTSFuelTypeValidation = function (field) {
            var strValue = "";
            var totFuel = 0;
            var FTC = $scope.fuelTypes.length + 1; //Fuel Type Count

            if (field) {
                strValue = document.querySelector('#vtsparentdiv #fueltype' + field).value.toString().replace(/\D/g, '');
                if (strValue != "") {
                    if (parseInt(strValue).isNaN) {
                        strValue = "";
                    } //validate to NaN
                }
                angular.element(document.querySelector('#vtsparentdiv  #fueltype' + field)).val(strValue).$apply;
            }

            //get totFuel - cycle through all boxes and add the fuel.
            for (var i = 0; i != FTC; i++) {
                for (var j = 0; j != 3; j++) {
                    try {
                        var val = document.querySelector('#vtsparentdiv #fueltype0' + i + "_" + j).value.toString().replace(/\D/g, '');
                        if (val.isNaN || val === "") {
                        } else {
                            totFuel += parseInt(val);
                        }
                        (totFuel > 0) ? $scope.fuelDetailsValid = true : $scope.fuelDetailsValid = false

                    } catch (exceptionNoElements) {
                    }
                }
            }

            //set/reset valid colours - must be more than 1 ton of fuel on board at any time.
            if (totFuel > 0) {
                for (var i = 0; i != FTC; i++) {
                    for (var j = 0; j != 3; j++) {
                        try {
                            angular.element(document.querySelector('#fueltype0' + i + "_" + j)).removeClass("vtsinvalid");
                            angular.element(document.querySelector('#fueltype0' + i + "_" + j)).addClass('vtsvalid');
                        } catch (exceptionNoElements) {
                        }
                    }
                }
            } else {
                for (var i = 0; i != FTC; i++) {
                    for (var j = 0; j != 3; j++) {
                        try {
                            angular.element(document.querySelector('#fueltype0' + i + "_" + j)).removeClass("vtsvalid");
                            angular.element(document.querySelector('#fueltype0' + i + "_" + j)).addClass('vtsinvalid');
                        } catch (exceptionNoElements) {
                        }
                    }
                }
            }

            //display total tonnage of fuel
            $scope.vtsTotalFuel = parseInt(totFuel).toLocaleString().replace(/,/g, '.'); // totfuel kilo/mega/giga seperator to period from comma: 9.999.999,999
            $scope.VTSValidationAllDone();

        };

        $scope.selectVesselCargoChange = function (selectedItem) {
            if (selectedItem != "None" && selectedItem != "Bulk - grain" && selectedItem != "Ballast" && selectedItem != "Passenger" && selectedItem != "Bulk - other than grain"
                && selectedItem != "Reefer" && selectedItem != "Container/Trailer" && selectedItem != "General Cargo") {
                $scope.clearHighlightedDangerousCargoTypeSelector();
                $scope.showCargoTypeFields = true;
                $scope.cargoAddToManifestDisabled = true;
                $scope.showCargoTypesCheckbox = true;
                $scope.vtsDangCargoCheckBoxState = true;
                $scope.vtsDangCargoCheckDisabled = true; //cannot turn off
                $scope.showCargoContactInformationInput = true;
            } else {
                $scope.showCargoTypeFields = false;
                (selectedItem == "None") ? $scope.showCargoTypesCheckbox = false : $scope.showCargoTypesCheckbox = true;
                $scope.vtsDangCargoCheckBoxState = false;
                $scope.vtsDangCargoCheckDisabled = false;
                if ((selectedItem == "None" || selectedItem == "Ballast")) {
                    $scope.showCargoContactInformationInput = false;
                } else {
                    $scope.showCargoContactInformationInput = true;
                }
            }
            $scope.setvtsCargoTypeValid = true;
            $scope.vtsvesselcargotypeholder = selectedItem;
            $scope.VTSCargoContactInformationChange();
            $scope.VTSValidationAllDone();
        };

        $scope.VTSDangerousCargoCheckbox = function (check) {
            if (check == true) {
                $scope.showCargoTypeFields = true;
                $scope.showCargoContactInformationInput = true;
            } else {
                $scope.showCargoTypeFields = false;
                if ($scope.vtsvesselcargotypeholder == "Ballast") $scope.showCargoContactInformationInput = false;
            }
            $scope.VTSCargoContactInformationChange();
        };

        $scope.clearHighlightedDangerousCargoTypeSelector = function () {
            $scope.dgSelectedType1 = false;
            $scope.dgSelectedType21 = false;
            $scope.dgSelectedType22 = false;
            $scope.dgSelectedType23 = false;
            $scope.dgSelectedType3 = false;
            $scope.dgSelectedType41 = false;
            $scope.dgSelectedType42 = false;
            $scope.dgSelectedType43 = false;
            $scope.dgSelectedType51 = false;
            $scope.dgSelectedType52 = false;
            $scope.dgSelectedType61 = false;
            $scope.dgSelectedType62 = false;
            $scope.dgSelectedType7 = false;
            $scope.dgSelectedType8 = false;
            $scope.dgSelectedType9 = false;
            $scope.vtsdangerouscargotonnageinput = ""; //clear tonnage input
            $scope.vtsdangerouscargonoteinput = ""; //clear note
        };

        $scope.VTSDangerousCargoTypeSelection = function (number) {
            $scope.clearHighlightedDangerousCargoTypeSelector();
            $scope.selectedDangerousCargoType = number;
            $scope.vtsdangerouscargoselectedinput = number;
            $scope.cargoAddToManifestDisabled = false;

            switch (number) {
                case "": //clear everything
                    $scope.vtsSelectedDangerousCargoDescription = "You can now add another entry to the dangerous cargo manifest.";
                    $scope.vtsdangerouscargoselectedinput = "";
                    $scope.vtsdangerouscargotonnageinput = "";
                    $scope.cargoAddToManifestDisabled = true;
                    $scope.vtsdangerouscargonoteinput = "";
                    break;
                case "1":
                    $scope.dgSelectedType1 = true;
                    $scope.vtsSelectedDangerousCargoDescription = "Explosives - all types";
                    break;
                case "2.1":
                    $scope.dgSelectedType21 = true;
                    $scope.vtsSelectedDangerousCargoDescription = "Flammable gas";
                    break;
                case "2.2":
                    $scope.dgSelectedType22 = true;
                    $scope.vtsSelectedDangerousCargoDescription = "Non flammable, non poisonous gas or Oxygen";
                    break;
                case "2.3":
                    $scope.dgSelectedType23 = true;
                    $scope.vtsSelectedDangerousCargoDescription = "Poisonous gas";
                    break;
                case "3":
                    $scope.dgSelectedType3 = true;
                    $scope.vtsSelectedDangerousCargoDescription = "Flammable liquid";
                    break;
                case "4.1":
                    $scope.vtsSelectedDangerousCargoDescription = "Flammable solids or substances - readily combustible";
                    $scope.dgSelectedType41 = true;
                    break;
                case "4.2":
                    $scope.dgSelectedType42 = true;
                    $scope.vtsSelectedDangerousCargoDescription = "Flammable solids - spontaneously combustible";
                    break;
                case "4.3":
                    $scope.dgSelectedType43 = true;
                    $scope.vtsSelectedDangerousCargoDescription = "Flammable substances - Dangerous When Wet";
                    break;
                case "5.1":
                    $scope.dgSelectedType51 = true;
                    $scope.vtsSelectedDangerousCargoDescription = "Oxydizing agents";
                    break;
                case "5.2":
                    $scope.dgSelectedType52 = true;
                    $scope.vtsSelectedDangerousCargoDescription = "Organic peroxides";
                    break;
                case "6.1":
                    $scope.dgSelectedType61 = true;
                    $scope.vtsSelectedDangerousCargoDescription = "Toxic substances";
                    break;
                case "6.2":
                    $scope.dgSelectedType62 = true;
                    $scope.vtsSelectedDangerousCargoDescription = "Infectious substances";
                    break;
                case "7":
                    $scope.dgSelectedType7 = true;
                    $scope.vtsSelectedDangerousCargoDescription = "Radioactive substances";
                    break;
                case "8":
                    $scope.dgSelectedType8 = true;
                    $scope.vtsSelectedDangerousCargoDescription = "Corrosive substances";
                    break;
                case "9":
                    $scope.dgSelectedType9 = true;
                    $scope.vtsSelectedDangerousCargoDescription = "Other dangerous or polluting substances";
                    break;
            }

            window.setTimeout(function () { //sets focus on tonnage input
                var el = document.getElementById('vts-cargo-tonnage-input');
                el.focus(); //set focus
                el.setSelectionRange(0, el.value.length); //select value
                $scope.validateDangerousCargoTonnage(); //clears selection when no parameter
            }, 0);
            if (number != "") {
                angular.element(document.querySelector(".vts-cargo-tonnage-input-selector")).addClass("vts-quick-pulse");
                window.setTimeout(function () { //sets focus on tonnage input
                    angular.element(document.querySelector(".vts-cargo-tonnage-input-selector")).removeClass("vts-quick-pulse");
                }, 1000);
            }
            ($scope.reportSummary.cargoEntries.length == 0 ) ? $scope.setvtsCargoTonnageValidState = 'invalid' : $scope.setvtsCargoTonnageValidState = 'neutral';

        };
        $scope.validateDangerousCargoTonnage = function () {
            var test = VtsHelperService.validateNumber($scope.vtsdangerouscargotonnageinput, 0, 999999999, 3);
            $scope.vtsdangerouscargotonnageinput = test.val;
            if (test.valid == true) {
                $scope.setvtsCargoTonnageValidState = 'valid';
                $scope.cargoAddToManifestDisabled = false;
            } else {
                $scope.setvtsCargoTonnageValidState = 'invalid';
            }
        };

        $scope.cargoRemoveFromManifest = function (number) {
            $scope.reportSummary.cargoEntries.splice(number, 1);
            $scope.cargoAddToManifestListFunction();
        };

        $scope.cargoAddToManifestListFunction = function () {
            var tmpArr = $scope.reportSummary.cargoEntries;
            var IMOtypesofDG = "", totalDGTonnage = 0.0;
            for (var i = 0; i != tmpArr.length; i++) {
                IMOtypesofDG += tmpArr[i].imoClass;
                (i == tmpArr.length - 1) ? IMOtypesofDG += "" : IMOtypesofDG += " - ";
                totalDGTonnage += parseFloat(tmpArr[i].tonnage)

            }

            $scope.vtsdangerouscargotypeslabel = IMOtypesofDG;
            $scope.vtsdangerouscargotonnagelabel = totalDGTonnage;


            //prepare a nice list to display in GUI
            var entries = "";
            for (var i = $scope.reportSummary.cargoEntries.length - 1; i != -1; i--) {
                if ($scope.reportSummary.cargoEntries.length - 1 == i) {
                    entries += "<div class='pulse-text-once vts-cargoentry-listitem'>"; //latest one flashes so user sees addition
                } else {
                    entries += "<div class='vts-cargoentry-listitem'>";
                }
                var imgName = ""; //add the image - pending on type and animation
                var ics = tmpArr[i].imoClass;
                if (ics == "1" || ics == "2.2" || ics == "9") ics += ".animated";
                imgName = 'img/cargo_labels/dg.' + ics + '.gif';
                entries += "<button class='btn btn-default vts-btn-highlight-danger vts-lesspadding' id='myAnchor'  type='button' ng-click='cargoRemoveFromManifest(\"" + i + "\")' ><i class='glyphicon glyphicon-trash' aria-hidden='true'></i> Remove</button>";
                entries += "&nbsp;<img style='height:26px;' src=" + imgName + ">";
                entries += "&nbsp;<span class='bold vts-force-spacing-100'>" + tmpArr[i].imoClass + "</span> - ";
                entries += "&nbsp;&nbsp;&nbsp;&nbsp;<span class='vts-force-spacing-100'><strong>" + tmpArr[i].tonnage + "</strong> MT</span>";
                //note is not mandatory
                if (tmpArr[i].note && tmpArr[i].note != "" && tmpArr[i].note != "undefined") entries += " - <span class='italic'>" + tmpArr[i].note + "</span>";
                entries += "</div>"; //close it
            }
            $scope.cargoManifestList = entries; //display in GUI using compile directive to allow buttons to work

        };

        $scope.cargoAddToManifestFunction = function () { //adds to the list then calls to update the GUI
            var repEn = {//add to report summary
                imoClass: $scope.vtsdangerouscargoselectedinput,
                tonnage: $scope.vtsdangerouscargotonnageinput + "",
                note: (($scope.vtsdangerouscargonoteinput != "" && typeof $scope.vtsdangerouscargonoteinput !== "undefined") ? ($scope.vtsdangerouscargonoteinput + "") : "")
            };
            $scope.reportSummary.cargoEntries.push(repEn);
            $scope.selectedDangerousCargoType = "";
            $scope.VTSDangerousCargoTypeSelection(""); //clears everything
            $scope.vtsdangerouscargoselectedinput = "";
            window.setTimeout(function () { //sets focus on additional input
                var el = document.getElementById('vts-selected-cargo-input');
                el.focus();
                el.blur();
            }, 500);
            $scope.setvtsCargoTonnageValidState = 'neutral'; // is no longer mandatory
            $scope.cargoAddToManifestListFunction(); //update GUI
        };


        $scope.VTSCargoContactInformationChange = function () {
            var input = $scope.vtscargoadditionalcontactdetailsinput;
            if (input.length > 5 || input.length == 0) { // not mandatory but still min. chars - subject to change
                $scope.setvtsvesselContactDetailsValid = true;
            } else {
                $scope.setvtsvesselContactDetailsValid = false;
            }
            $scope.VTSValidationAllDone();
        };

        $scope.VTSDangerousCargoContactNameValidation = function () {
            var input = $scope.vtscargodesignatedpersonashorenameinput;
            if (input != "" && input.length > 5) {
                $scope.setvtsvesselDPANameValid = true;
            } else {
                $scope.setvtsvesselDPANameValid = false;
            }
            $scope.VTSValidationAllDone();
        };


        $scope.VTSDangerousCargoContactPhonenumberValidation = function () {
            var input = $scope.vtscargodesignatedpersonashoretelephoneinput;
            input = input.replace(/[^-+()0-9]/g, '');
            $scope.vtscargodesignatedpersonashoretelephoneinput = input;
            if (input != "" && input.length > 5) {
                $scope.setvtsvesselDPAPhoneValid = true;
            } else {
                $scope.setvtsvesselDPAPhoneValid = false;
            }
            $scope.VTSValidationAllDone();
        };

        $scope.VTSDangerousCargoContactEmailValidation = function () {
            var input = $scope.vtscargodesignatedpersonashoreemailinput;
            var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
            if (input != "" && input.length > 5) {
                (re.test(input)) ? $scope.setvtsvesselDPAEmailValid = true : $scope.setvtsvesselDPAEmailValid = false;
            } else {
                $scope.setvtsvesselDPAEmailValid = false;
            }
            $scope.VTSValidationAllDone();
        };

        $scope.VTSTrueCourseValidation = function (validate) {
            try {
                var inputStr = $scope.vtsvesseltruecourseinput + "";
                var ret = VtsHelperService.VTSValidation360(inputStr);
                $scope.setvtsvesselTrueCourseValid = ret[0];
                $scope.vtsvesseltruecourseinput = ret[1];
                if (validate) $scope.VTSValidationAllDone();
            } catch (donothing_truecourse) {
            }
        };

        $scope.VTSCourseOverGroundValidation = function (validate) { //only appears in UI when using AIS data
            try {
                var inputStr = $scope.vtsvesselcourseovergroundinput + "";
                var ret = VtsHelperService.VTSValidation360(inputStr);
                (ret[1] == "") ? $scope.setvtsvesselCourseOverGroundValid = true : $scope.setvtsvesselCourseOverGroundValid = ret[0];
                $scope.vtsvesselcourseovergroundinput = ret[1];
                if (validate) $scope.VTSValidationAllDone();
            } catch (donothing_courseoverground) {
            }
        };

        $scope.VTSVesselPortOfDestinationValidation = function (validate) {
            if ($scope.showPortOfDestination) { //only validate if displayed
                $scope.showPortOfDestinationEta = true;
                var input = $scope.vtsvesselportofdestinationinput;
                (input != "" && input.length > 2) ? $scope.setvtsvesselPortOfDestinationValid = true : $scope.setvtsvesselPortOfDestinationValid = false;
                if (validate) $scope.VTSValidationAllDone();
            } else {
                $scope.showPortOfDestinationEta = false;
            }
        };

        //cant be faster than 50 knots
        $scope.VTSVesselSpeedValidation = function () {
            var speed = $scope.vtsvesselspeedinput.toString().replace(/\D/g, '');
            if (speed != "" && speed != null) {
                var speedInt = parseInt(speed);
                if (speedInt > 49) speedInt = 50;
                (speedInt > 0) ? $scope.setvtsvesselSpeedValid = true : $scope.setvtsvesselSpeedValid = false;
                $scope.vtsvesselspeedinput = speedInt;
                $scope.VTSValidationAllDone();
            }
        };

        //END input validation area


        $scope.cargoInputKeyUp = function (evt) {
            if (evt.keyCode == 13 && !$scope.cargoAddToManifestDisabled) {
                $scope.cargoAddToManifestFunction();
            }
        };

        //Change VTS center dropdown **********************************************************************************

        //clicking on dropdown menu to open the VTS form reates the input html according to VTSCenterData available - made as html insert because so many unknowns.
        $scope.selectVTSCenterChange = function (selectedItem) {

            //For debug purposes
            // $scope.isLoggedIn = true; //

            var html = "",
                vtsID = 0;
            for (var i = 1; i != VTSData.length; i++) {
                console.log("ID:", i," becomes:", VTSData[i].id);
                if (selectedItem == VTSData[i].shortname) {
                    console.log("ID:", i," becomes:", VTSData[i].id ," - ", selectedItem, " - ", VTSData[i].shortname);
                    vtsID = VTSData[i].id;
                    $scope.VTSID = vtsID;
                }
            }

            //field logic - some fields are not required by certain VTS
            $scope.showMaxDraught = VTSData[vtsID].showMaxDraught;
            $scope.showAirDraught = VTSData[vtsID].showAirDraught;
            $scope.showFuelQuantity = VTSData[vtsID].showFuelQuantity;
            $scope.showVesselType = VTSData[vtsID].showVesselType;
            $scope.showVesselLength = VTSData[vtsID].showVesselLength;
            $scope.showDeadWeightTonnage = VTSData[vtsID].showDeadWeightTonnage;
            $scope.showGrossTonnage = VTSData[vtsID].showGrossTonnage;
            $scope.showFuelDetails = VTSData[vtsID].showFuelDetails;
            $scope.showCargoType = VTSData[vtsID].showCargoType;
            $scope.showVesselCurrentPosition = VTSData[vtsID].showVesselCurrentPosition; //input for vessel pos
            $scope.showTrueCourse = VTSData[vtsID].showTrueCourse;
            $scope.showPortOfDestination = VTSData[vtsID].showPortOfDestination;
            $scope.showRoute = VTSData[vtsID].showRoute;


            //reset validations
            $scope.VTSVesselDraughtValidation(false);
            $scope.VTSVesselAirDraughtValidation(false);
            $scope.VTSVesselDWTValidation(false);
            $scope.selectedVesselType = '0'; //reset select
            $scope.setvtsVesselTypeValid = false; //UI update
            $scope.VTSVesselLengthValidation(false);
            $scope.VTSFuelTypeValidation(false);
            $scope.VTSVesselDWTValidation(false);
            $scope.VTSVesselGTValidation(false);
            $scope.VTSVesselFuelQuantityValidation(false);
            $scope.VTSDangerousCargoContactNameValidation(false);
            $scope.VTSDangerousCargoContactPhonenumberValidation(false);
            $scope.VTSDangerousCargoContactEmailValidation(false);
            $scope.VTSCargoContactInformationChange(false);
            $scope.VTSVesselCurrentPositionLonDegValidation(false);
            $scope.VTSVesselCurrentPositionLonMinValidation(false);
            $scope.VTSVesselCurrentPositionLatDegValidation(false);
            $scope.VTSVesselCurrentPositionLatMinValidation(false);
            $scope.VTSVesselPortOfDestinationValidation(false);
            $scope.VTSVesselSpeedValidation(false);
            $scope.VTSTrueCourseValidation(false);
            $scope.VTSCourseOverGroundValidation(false);

            $scope.VTSSelectedTrafficCenterName = VTSData[vtsID].name;
            if (VTSData[vtsID].iconImage != "") {
                html = "<span style='min-width:24px;display: inline-block; text-align: left; '><img style='height:20px;' src='" + VTSData[vtsID].iconImage + "'></span>";
                $scope.VTSSelectedTrafficCenterLogo = $sce.trustAsHtml(html);
                html = "";
            }
            if (VTSData[vtsID].VTSGuideLink != "") {
                html = "<span style='min-width:200px;max-width:100%;display: inline-block; text-align: right; right:0;float:right;'><a target='_blank' href='" + VTSData[vtsID].VTSGuideLink + "'>View Master&#39;s Guide online</a></span>";
                $scope.VTSSelectedTrafficCenterGuide = $sce.trustAsHtml(html);
                html = "";
            }

            //create contact information for VTS center in compact form
            html += "<span style='min-width:220px;max-width:220px;display: inline-block; text-align: left;'>Call sign: &#34;" + VTSData[vtsID].callsign + "&#34;</span>";
            if (VTSData[vtsID].vhfchannel1 != "") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>-</span><span style='min-width:140px;max-width:140px;display: inline-block;'>VHF ch. " + VTSData[vtsID].vhfchannel1 + "</span>";
            if (VTSData[vtsID].vhfchannel2 != "") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>-</span><span style='min-width:140px;max-width:140px;display: inline-block;'>VHF ch. " + VTSData[vtsID].vhfchannel2 + "</span>";

            //radio channels VHF
            //if more than 2 channels, add them to their own div - very rare that happens.
            if (VTSData[vtsID].vhfchannel3 != "" || VTSData[vtsID].vhfchannel4 != "") html += "<div><span style='min-width:220px;max-width:220px;display: inline-block; text-align: left;'>&nbsp;</span>";
            if (VTSData[vtsID].vhfchannel3 != "") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>&nbsp;</span><span style='min-width:140px;max-width:140px;display: inline-block;'>VHF ch. " + VTSData[vtsID].vhfchannel3 + "</span>";
            if (VTSData[vtsID].vhfchannel4 != "") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>-</span><span style='min-width:140px;max-width:140px;display: inline-block;'>VHF ch. " + VTSData[vtsID].vhfchannel4 + "</span>";
            if (VTSData[vtsID].vhfchannel3 != "" || VTSData[vtsID].vhfchannel4 != "") html += "</div>";

            //There is always a reserve channel or two
            if (VTSData[vtsID].vhfreservechannel1 != "" || VTSData[vtsID].vhfreservechannel2 != "") html += "<div><span style='min-width:220px;max-width:220px;display: inline-block; text-align: left;font-style:italic;color:#999999'>Reserve channels:</span>";
            if (VTSData[vtsID].vhfreservechannel1 != "") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>&nbsp;</span><span style='min-width:140px;max-width:140px;display: inline-block;font-style:italic;color:#999999'>VHF " + VTSData[vtsID].vhfreservechannel1 + "</span>";
            if (VTSData[vtsID].vhfreservechannel2 != "") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>-</span><span style='min-width:140px;max-width:140px;display: inline-block;font-style:italic;color:#999999'>VHF " + VTSData[vtsID].vhfreservechannel2 + "</span>";
            if (VTSData[vtsID].vhfreservechannel1 != "" || VTSData[vtsID].vhfreservechannel2 != "") html += "</div>";

            //Email and telephone
            if (VTSData[vtsID].email != "" || VTSData[vtsID].telephone != "" || VTSData[vtsID].telephone2 != "") html += "<div>";
            if (VTSData[vtsID].email != "") html += "<span style='min-width:220px;max-width:200px;display: inline-block;'>Email: " + VTSData[vtsID].email + "</span>";
            if (VTSData[vtsID].telephone != "") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>-</span><span style='min-width:220px;max-width:220px;display: inline-block;'>Phone: " + VTSData[vtsID].telephone + "</span>";
            if (VTSData[vtsID].telephone2 != "") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>-</span><span style='min-width:220px;max-width:220px;display: inline-block;'>Phone2: " + VTSData[vtsID].telephone2 + "</span>";
            if (VTSData[vtsID].email != "" || VTSData[vtsID].telephone != "" || VTSData[vtsID].telephone2 != "") html += "</div>";

            //Not all VTS have a fax
            if (VTSData[vtsID].fax != "") html += "<div><span style='min-width:220px;max-width:220px;display: inline-block;'>Fax: " + VTSData[vtsID].fax + "</span></div>";

            //displays the form fields
            $scope.VTSSelected = true;

            //draws the vts area on minimap
            (VTSData[vtsID].areaWKT != "") ? $scope.areaWKT = VTSData[vtsID].areaWKT : VTSData[vtsID].areaWKT = "";

            //display time input as invalid
            angular.element(document.querySelector(".datetime-input.date .display")).addClass("vts-datetime-picker-box-highlight-invalid");
            angular.element(document.querySelector(".datetime-input.time .display")).addClass("vts-datetime-picker-box-highlight-invalid");

            $scope.VTSSelectedTrafficCenterData = $sce.trustAsHtml(html);
            $scope.VTSSelectedTrafficCenterShortname = $sce.trustAsHtml(VTSData[vtsID].shortname);

            $scope.VTSFuelTypeValidation(); //disables all fuel inputs if nothing
            $scope.VTSValidationAllDone(); //make sure to see if is ready to send

            //places VTS area in minimap
            VtsHelperService.miniMapUpdate(VTSData[vtsID].areaWKT, null, null);

            //gets ETA from route if loaded
            $scope.detectedRouteETA = VtsHelperService.returnDetectedRouteETA();
            if($scope.detectedRouteETA && $scope.detectedRouteETA.length>10){
                var routeETATime = $scope.detectedRouteETA.substring(14,$scope.detectedRouteETA.length);
                var routeETADate = $scope.detectedRouteETA.substring(0, 11);
                $scope.vtsTimeStamp = routeETATime;
                $scope.vtsDateStamp = routeETADate;
                angular.element(document.querySelector(".datetime-input.date .display .date")).html($scope.vtsDateStamp); //update timepicker display with now
                angular.element(document.querySelector(".datetime-input.time .display .time")).html($scope.vtsTimeStamp); //update timepicker display with now

            }

            console.log("ReturningETA:",$scope.detectedRouteETA );

            //debugging
            $scope.vtsvesselmmsiinput = "265177000";
            $scope.VTSVesselMMSIValidation();

        };

        $scope.selectedVesselType = "";
        $scope.vtsTotalFuel = ""; //added up and displayed from validation


        //Send form ****************************************************************************************************

        $scope.sendVTSForm = function () {

            $scope.reportSummary.vtsShortName = "" + VTSData[$scope.VTSID].shortname;
            $scope.reportSummary.vtsCallSign = "" + VTSData[$scope.VTSID].callsign;
            $scope.reportSummary.vtsEmail = "" + VTSData[$scope.VTSID].email;

            //vessel info
            $scope.reportSummary.vesselName = "" + $scope.vtsvesselnameinput;
            $scope.reportSummary.vesselCallSign = "" + $scope.vtsvesselcallsigninput;
            $scope.reportSummary.vesselMMSI = "" + $scope.vtsvesselmmsiinput;
            $scope.reportSummary.vesselIMO = "" + $scope.vtsvesselimoinput;
            $scope.reportSummary.vesselDraught = "" + $scope.vtsvesseldraughtinput;
            $scope.reportSummary.vesselAirDraught = "" + $scope.vtsvesselairdraughtinput;
            $scope.reportSummary.vesselPersonsOnboard = "" + $scope.vtsvesselpersonsinput;
            $scope.reportSummary.vesselLength = "" + $scope.vtsvessellengthinput;
            $scope.reportSummary.vesselDeadWeight = "" + $scope.vtsvesseldeadweightinput;
            $scope.reportSummary.vesselGRT = "" + $scope.vtsvesselgrosstonnageinput;
            $scope.reportSummary.vesselDefects = "" + $scope.vtsvesseldefectsinput;
            $scope.reportSummary.vesselType = "" + $scope.vtsvesseltypeholder;

            //fuel
            $scope.reportSummary.fuelTotalFuel = "" + (parseFloat($scope.vtsTotalFuel)); //all fuel added up
            $scope.reportSummary.fuelTypeHFORegular = "" + $scope.vtsvesselfueltype00_0input;
            $scope.reportSummary.fuelTypeHFOLowSulphur = "" + $scope.vtsvesselfueltype00_1input;
            $scope.reportSummary.fuelTypeHFOUltraLowSulphur = "" + $scope.vtsvesselfueltype00_2input;
            $scope.reportSummary.fuelTypeIFORegular = "" + $scope.vtsvesselfueltype01_0input;
            $scope.reportSummary.fuelTypeIFOLowSulphur = "" + $scope.vtsvesselfueltype01_1input;
            $scope.reportSummary.fuelTypeIFOUltraLowSulphur = "" + $scope.vtsvesselfueltype01_2input;
            $scope.reportSummary.fuelTypeMDORegular = "" + $scope.vtsvesselfueltype02_0input;
            $scope.reportSummary.fuelTypeMDOLowSulphur = "" + $scope.vtsvesselfueltype02_1input;
            $scope.reportSummary.fuelTypeMDOUltraLowSulphur = "" + $scope.vtsvesselfueltype02_2input;
            $scope.reportSummary.fuelTypeMGORegular = "" + $scope.vtsvesselfueltype03_0input;
            $scope.reportSummary.fuelTypeMGOLowSulphur = "" + $scope.vtsvesselfueltype03_1input;
            $scope.reportSummary.fuelTypeMGOUltraLowSulphur = "" + $scope.vtsvesselfueltype03_2input;
            $scope.reportSummary.fuelTypeLPG = "" + $scope.vtsvesselfueltype04_0input;
            $scope.reportSummary.fuelTypeLNG = "" + $scope.vtsvesselfueltype05_0input;

            //cargo
            $scope.reportSummary.cargoType = $scope.vtsvesselcargotypeholder;
            $scope.reportSummary.cargoDangerousCargoTotalTonnage = (typeof $scope.vtsdangerouscargotonnagelabel !== "undefined") ? ("" + ($scope.vtsdangerouscargotonnagelabel)) : "";
            if ($scope.reportSummary.vtsdangerouscargotonnagelabel != "" && typeof $scope.vtsdangerouscargotonnagelabel !== "undefined") $scope.reportSummary.cargoDangerousCargoOnBoard = true;
            $scope.reportSummary.cargoIMOClassesOnBoard = "" + ($scope.vtsdangerouscargotypeslabel);
            $scope.reportSummary.cargoPollutantOrDCLostOverBoard = "" + ($scope.vtsdangerouscargooverboard);
            $scope.reportSummary.cargoAdditionalContactInformation = "" + ($scope.vtscargoadditionalcontactdetailsinput);

            $scope.reportSummary.cargoDPAName = "" + ($scope.vtscargodesignatedpersonashorenameinput);
            $scope.reportSummary.cargoDPATelephone = "" + ($scope.vtscargodesignatedpersonashoretelephoneinput);
            $scope.reportSummary.cargoDPAEmail = "" + ($scope.vtscargodesignatedpersonashoreemailinput);

            //voyage info
            $scope.reportSummary.voyagePositionLon = "" + ($scope.vtsvesselposlondegreesinput + "," + $scope.vtsvesselposlonminutesinput);
            $scope.reportSummary.voyagePositionLat = "" + ($scope.vtsvesselposlatdegreesinput + "," + $scope.vtsvesselposlatminutesinput);
            $scope.reportSummary.voyageSpeed = "" + (parseFloat($scope.vtsvesselspeedinput));
            $scope.reportSummary.voyageTrueCourse = "" + (parseFloat($scope.vtsvesseltruecourseinput));
            $scope.reportSummary.voyagePortOfDestination = "" + ($scope.vtsvesselportofdestinationinput);
            ($scope.showPortOfDestinationEta) ? $scope.reportSummary.voyagePortOfDestinationEta = "" + ($scope.vtsvesselportofdestinationetalabel) : ""; //only if used AIS data
            $scope.reportSummary.voyageVTSETADateTime = "" + ($scope.vtsLocalDate + " - " + $scope.vtsUtcTime);


            // debug
            console.log("VTS REPORT:", $scope.reportSummary);
            // console.log("post:"+JSON.stringify($scope.reportSummary)); //debug
            //Send form endpoint *************************************************************************************
            $http({
                url: '/rest/vtsemail',
                method: "POST",
                data: JSON.stringify($scope.reportSummary),
                headers: {'Content-Type': 'application/json'}
            })
                .then(function (data) {
                        console.log("JSON string sent", JSON.stringify(data)); //used for debug
                        $uibModalInstance.close('forceclose', 'forceclose'); //close VTS interface
                        growl.success("VTS report was successfully sent to " + $scope.reportSummary.vtsShortName + "!");
                    },
                    function (data) { // optional
                        console.log(data);
                        alert("Your report could not be sent. Please check your internet connection and try again.\nIf this message persists with an established internet connection, please contact the Department of E-Navigation: sfs@dma.dk");
                    });
        };

        $scope.populateInputsWithAisData = function () { //only populates empty fields
            if ($scope.vtsvesselnameinput == "") $scope.vtsvesselnameinput = $scope.aisData.vesselName;
            if ($scope.aisData.vesselName != "") $scope.VTSVesselNameValidation(false); //skips global validator

            if ($scope.vtsvesselcallsigninput == "") $scope.vtsvesselcallsigninput = $scope.aisData.vesselCallsign;
            if ($scope.aisData.vesselCallsign != "") $scope.VTSVesselCallsignValidation(false);

            if ($scope.vtsvesselimoinput == "") $scope.vtsvesselimoinput = $scope.aisData.vesselImo;
            if ($scope.aisData.vesselImo != "") $scope.VTSVesselIMOValidation(false);

            if ($scope.vtsvesselportofdestinationinput == "") $scope.vtsvesselportofdestinationinput = $scope.aisData.vesselDestination;
            if ($scope.aisData.vesselDestination != "") $scope.VTSVesselPortOfDestinationValidation(false);

            try {
                var weekDay = moment()._locale._weekdaysShort[moment($scope.aisData.vesselDestinationEta).day()];
                var formattedEta = weekDay + " - " + moment($scope.aisData.vesselDestinationEta).format("DD/MM/YYYY - MM:HH") + " UTC";
                $scope.showPortOfDestinationEta = true;
                $scope.vtsvesselportofdestinationetalabel = formattedEta; //needs no validation
            } catch (InvalidTimeStamp) {
                $scope.showPortOfDestinationEta = false;
            }

            if ($scope.vtsvesseldraughtinput == "") $scope.vtsvesseldraughtinput = $scope.aisData.vesselDraught;
            if ($scope.aisData.vesselDraught != "") $scope.VTSVesselDraughtValidation(false);

            if ($scope.vtsvessellengthinput == "") $scope.vtsvessellengthinput = $scope.aisData.vesselLength;
            if ($scope.aisData.vesselLength != "") $scope.VTSVesselLengthValidation(false);

            if ($scope.aisData.vesselCOG != "") {
                $scope.showCourseOverGround = true; //display in UI
                if ($scope.vtsvesselcourseovergroundinput == "") $scope.vtsvesselcourseovergroundinput = $scope.aisData.vesselLength;
                $scope.VTSCourseOverGroundValidation(false);
            }

            //add to select if not already there, then set selected and validate
            if ($scope.aisData.vesselType != "" && $scope.aisData.vesselType != null) {
                var detected = false;
                for (var i = 0; i != $scope.vesselTypes.length; i++) {
                    if ($scope.vesselTypes[i].type == $scope.aisData.vesselType) {
                        $scope.selectedVesselType = $scope.vesselTypes[i]; //set select to correct position
                        detected = true;
                        break;
                    }
                }
                if (!detected) {
                    $scope.vesselTypes.unshift({type: $scope.aisData.vesselType, DWTmultiplier: 999999});
                    $scope.selectedVesselType = $scope.vesselTypes[0]; //set select to correct position
                }
                $scope.setvtsVesselTypeValid = true;
                $scope.vtsvesseltypeholder = $scope.aisData.vesselType;
            }

            if ($scope.aisData.lon != "" && $scope.aisData.lon != null) {
                $scope.vtsvesselposlondegreesinput = $scope.placeholderAisVesselLonDegrees;
                $scope.vtsvesselposlonminutesinput = $scope.placeholderAisVesselLonDecimalMinutes;
                ($scope.aisData.lon > 0) ? $scope.vtsCurrentPosCompassAppendEW = "E" : $scope.vtsCurrentPosCompassAppendEW = "W";
                $scope.VTSVesselCurrentPositionLonDegValidation(false);
                $scope.VTSVesselCurrentPositionLonMinValidation(false);
            }
            if ($scope.aisData.lat != "" && $scope.aisData.lat != null) {
                $scope.vtsvesselposlatdegreesinput = $scope.placeholderAisVesselLatDegrees;
                $scope.vtsvesselposlatminutesinput = $scope.placeholderAisVesselLatDecimalMinutes;
                ($scope.aisData.lat > 0) ? $scope.vtsCurrentPosCompassAppendNS = "N" : $scope.vtsCurrentPosCompassAppendNS = "S";
                $scope.VTSVesselCurrentPositionLatDegValidation(false);
                $scope.VTSVesselCurrentPositionLatMinValidation(false);
            }


            $scope.VTSValidationAllDone(); //finally validate all values for ready to send
        };

        //Fetches AIS data  - changes background colour of affected inputs and adds placeholder
        $scope.getAisDataByMmsi = function () {
            if ($scope.setvtsvesselMMSIValid && $scope.isLoggedIn) {
                VesselService.detailsMMSI($scope.vtsvesselmmsiinput).then(function (vesselDetails) {
                    console.log("Vessel info from AIS:", vesselDetails.data.aisVessel);

                    $scope.aisData.vesselName = vesselDetails.data.aisVessel.name;
                    $scope.aisData.vesselCallsign = vesselDetails.data.aisVessel.callsign;
                    $scope.aisData.vesselImo = vesselDetails.data.aisVessel.imoNo;
                    $scope.aisData.vesselCog = vesselDetails.data.aisVessel.cog;
                    $scope.aisData.vesselDestination = vesselDetails.data.aisVessel.destination;
                    $scope.aisData.vesselDestinationEta = vesselDetails.data.aisVessel.eta;
                    $scope.aisData.vesselDraught = vesselDetails.data.aisVessel.draught;
                    $scope.aisData.vesselLength = vesselDetails.data.aisVessel.length;
                    $scope.aisData.vesselCOG = vesselDetails.data.aisVessel.cog;
                    $scope.aisData.vesselType = vesselDetails.data.aisVessel.vesselType;
                    $scope.aisData.lon = vesselDetails.data.aisVessel.lon;
                    $scope.aisData.lat = vesselDetails.data.aisVessel.lat;

                    //load the placeholders
                    $scope.placeholderAisVesselName = $scope.aisData.vesselName;
                    $scope.placeholderAisVesselCallsign = $scope.aisData.vesselCallsign;
                    $scope.placeholderAisVesselImo = $scope.aisData.vesselImo;
                    $scope.placeholderAisVesselCog = $scope.aisData.vesselCog;
                    $scope.placeholderAisVesselDestination = $scope.aisData.vesselDestination;
                    $scope.placeholderAisVesselDraught = $scope.aisData.vesselDraught;
                    $scope.placeholderAisVesselLength = $scope.aisData.vesselLength;
                    $scope.vtsvesselcourseovergroundinput = $scope.aisData.vesselCOG; //only displayed when using AIS

                    var lon = $scope.aisData.lon;
                    $scope.placeholderAisVesselLonDegrees = (lon + "").substring(0, (lon + "").indexOf("."));
                    ($scope.aisData.lon > 0) ? $scope.vtsCurrentPosCompassAppendEW = "E" : $scope.vtsCurrentPosCompassAppendEW = "W";
                    lon = $scope.retDecMinutesFromDecDegrees(lon);
                    $scope.placeholderAisVesselLonDecimalMinutes = lon;

                    var lat = $scope.aisData.lat;
                    $scope.placeholderAisVesselLatDegrees = (lat + "").substring(0, (lat + "").indexOf("."));

                    ($scope.aisData.lat > 0) ? $scope.vtsCurrentPosCompassAppendNS = "N" : $scope.vtsCurrentPosCompassAppendNS = "S";
                    lat = $scope.retDecMinutesFromDecDegrees(lat);
                    $scope.placeholderAisVesselLatDecimalMinutes = lat;

                    var tmpPos = [$scope.aisData.lon, $scope.aisData.lat];
                    VtsHelperService.miniMapUpdate(VTSData[$scope.VTSID].areaWKT, tmpPos, $scope.placeholderAisVesselCog);

                    $scope.aisdataReady = true;


                }, function (reason) {
                    $scope.aisdataReady = false;
                    $scope.clearAISPlaceholders();
                    growl.error('AIS data fetch failed: ' + reason);
                });
            }
            $scope.clearAISPlaceholders = function () {
                $scope.placeholderAisVesselName = "";
                $scope.placeholderAisVesselCallsign = "";
                $scope.placeholderAisVesselImo = "";
                $scope.placeholderAisVesselCog = "";
                $scope.placeholderAisVesselDestination = "";
                $scope.placeholderAisVesselDraught = "";
                $scope.placeholderAisVesselLength = "";
                $scope.vtsvesselcourseovergroundinput = "";
                $scope.placeholderAisVesselLonDegrees = "";
                $scope.placeholderAisVesselLonDecimalMinutes = "";
                $scope.placeholderAisVesselLatDegrees = "";
                $scope.placeholderAisVesselLatDecimalMinutes = "";
            };

            $scope.setLocationByAIS = function () {
                if ($scope.setvtsvesselMMSIValid && $scope.aisdataReady) {
                    var lon = $scope.aisData.lon;
                    $scope.vtsvesselposlondegreesinput = (lon + "").substring(0, (lon + "").indexOf("."));
                    ($scope.aisData.lon > 0) ? $scope.vtsCurrentPosCompassAppendEW = "E" : $scope.vtsCurrentPosCompassAppendEW = "W";
                    lon = $scope.retDecMinutesFromDecDegrees(lon);
                    $scope.vtsvesselposlonminutesinput = lon;
                    $scope.VTSVesselCurrentPositionLonDegValidation(false);
                    $scope.VTSVesselCurrentPositionLonMinValidation(false);

                    var lat = $scope.aisData.lat;
                    $scope.vtsvesselposlatdegreesinput = (lat + "").substring(0, (lat + "").indexOf("."));
                    ($scope.aisData.lat > 0) ? $scope.vtsCurrentPosCompassAppendNS = "N" : $scope.vtsCurrentPosCompassAppendNS = "S";
                    lat = $scope.retDecMinutesFromDecDegrees(lat);
                    $scope.vtsvesselposlatminutesinput = lat;
                    $scope.VTSVesselCurrentPositionLatDegValidation(false);
                    $scope.VTSVesselCurrentPositionLatMinValidation(false);
                }
            }

        };

        //Modal form control ******************************************************************************************
        $scope.hideVTSForm = function () {
            $uibModalInstance.close();
        };

        $scope.$on('modal.closing', function (event, reason, closed) {
            // console.log('modal.closing: ' + (closed ? 'close' : 'dismiss') + '(' + reason + ')');
            var message = "You are about to leave the VTS interface. Any changes you have made may be lost.\nClick OK to exit or Cancel to remain in the VTS interface.";
            switch (reason) {
                case "forceclose":
                    $scope.VTSID = -1;
                    message = "forceclose";
                    break;
                case "backdrop click":
                    message = ""; //will do nothing
                    break;
                case "cancel":
                    message = "Any changes will be lost, are you sure?";
                    break;
                case "escape key press":
                    message = "Any changes will be lost, are you sure?";
                    break;
            }
            if (message === "") {
                event.preventDefault();
            } else {
                if ($scope.VTSID != -1) { //ignore exit warning if pristine
                    $scope.showReportInterface = false;
                    event.preventDefault();

                    // if (!confirm(message)) {
                    //     event.preventDefault();
                    // }
                }
            }
        });

        //Skips null items when populating dropdowns
        $scope.isListItemNull = function(item) {
            if (item === null) return false;
            return true;
        };


        console.log("TODO (final task): function to save and load state of report in localstorage");
        $scope.exitInterface = function(state){
            if(state==true){
                // alert("add function to exit after save to localstorage - also load from localstorage..");
                $uibModalInstance.close('forceclose', 'forceclose'); //close VTS interface
                growl.error("VTS report interface was saved and exited, no report was sent.");
            }else{
                $scope.showReportInterface = !state; //will cancel exit
            }
        }

    }]).directive('compile', ['$compile', function ($compile) { //so buttons added after DOM was loaded will work
    return function (scope, element, attrs) {
        scope.$watch(
            function (scope) {
                return scope.$eval(attrs.compile);
            },
            function (value) {
                element.html(value);
                $compile(element.contents())(scope);
            }
        );
    };
}]);
