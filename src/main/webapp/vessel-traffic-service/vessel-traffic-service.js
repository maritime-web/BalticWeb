angular.module('maritimeweb.app').controller('VesselTrafficServiceCtrl', ['$scope', '$uibModalInstance', '$window', '$sce', 'growl', '$http', 'Auth', 'VesselService',
    function ($scope, $uibModalInstance, $window, $sce, growl, $http, Auth, VesselService) {



        //Populate the interface using json object from service - anon allowed
        var VTSData = []; //local array
        var getInterfaceObjectsFromService = function(){
            $http({ // get the VTS definition objects from service - builds the correct buttons/fields
                url: '/rest/vtsinterface',
                method: "GET",
                data: "",
                headers:{'Content-Type': 'application/text'}
            })
                .then(function(data) {
                        var parsedData = JSON.parse(JSON.stringify(data));
                        growl.success("VTS interface population service was successful.");
                        $scope.VTSCenterData = parsedData.data.VtsJsObjects;
                        VTSData = $scope.VTSCenterData; //place in local
                        angular.element(document.querySelector('#modalbody')).removeClass('ng-hide'); //hidden until angular is ready so it doesnt pop up and down
                        //display date and time in utc
                        $scope.vtsTimeStamp = moment.utc().format('HH : mm');
                        $scope.vtsDateStamp = moment()._locale._weekdaysShort[moment().day()] + " " + moment.utc().format('DD MMM YYYY');
                        angular.element(document.querySelector(".datetime-input.date .display .date")).html(moment().format('DD MMM YYYY')); //update timepicker display with now
                        angular.element(document.querySelector(".datetime-input.time .display .time")).html(moment.utc().format('HH : mm')); //update timepicker display with now
                    },
                    function(data) { // error
                        growl.error("VTS interface population service could not be contacted. Please check your internet connection and try again.\nRetrying in 5 seconds.");
                    });
        };
        getInterfaceObjectsFromService();


        // console.log("vessel mmsi 219157000:",VesselService.detailsMMSI('219157000'));

        //User credentials and login  ******************************************************************************
        $scope.login = function () {
            Auth.authz.login(); //calls login function
        };
        $scope.isLoggedIn = Auth.loggedIn; //declare scope login state


        //Declarations and settings ********************************************************************************
        VTSData = $scope.VTSCenterData; //local var so interface doesnt update all the time
        $scope.VTSID = -1; //for reference when validating - -1 is pristine

        //DWT multiplier may be needed to force display of fuel types, according to National Single Window project. Not implemented in BalticWeb yet.
        $scope.vesselTypes = [
            {type:"General Cargo", DWTmultiplier:0.5285},
            {type:"Bulk Carrier", DWTmultiplier:0.5285},
            {type:"Container Ship", DWTmultiplier:0.8817},
            {type:"Oil Tanker", DWTmultiplier:0.5354},
            {type:"LNG transport", DWTmultiplier:1.13702},
            {type:"LPG transport", DWTmultiplier:0.8447},
            {type:"Fishing boat (A)", DWTmultiplier:0}, //0=ignore
            {type:"Fishing boat (A)", DWTmultiplier:0},
            {type:"Fishing Processer (B)", DWTmultiplier:0},
            {type:"Non-Fishing Processer (C)", DWTmultiplier:0},
            {type:"Oil Industry Vessel", DWTmultiplier:999999}, //999999=always show fuel details
            {type:"Passenger Ship", DWTmultiplier:8.9393},
            {type:"Ferry", DWTmultiplier:0},
            {type:"Tug boat", DWTmultiplier:0},
            {type:"Barge", DWTmultiplier:0},
            {type:"Other", DWTmultiplier:999999}
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
            {name:"HFO", description:"Heavy Fuel Oil"},
            {name:"IFO", description:"Intermediate Fuel Oil"},
            {name:"MDO", description:"Marine Diesel Oil"},
            {name:"MGO", description:"Marine Gas Oil"},
            {name:"LPG", description:"Liquid Petroleum Gas"},
            {name:"LNG", description:"Liquid Natural Gas"},
            {name:"Other", description:""}
        ];

        //displays as vessel type input but is really a cargo definition
        $scope.cargoTypes = ["None", "Ballast", "Bulk - grain", "Bulk - other than grain", "Chemicals", "Container/Trailer", "General Cargo", "Gas", "Oil", "Passenger", "Reefer", "Other"];

        //Specific VTS center route dropdowns (They usually have predefined routes and abbreviations as specified in their pilot/master's guide )
        $scope.BELTREPRoutes = ["", ""];
        $scope.SOUNDREPRoutes = ["", ""];


    //Input validation area

        //ETA at VTS
        $scope.vtsForceUTCTimeCheckBoxState = false; //checkbox to activate UTC timezone
        $scope.vtsUtcTime = moment.utc().format('HH : mm');
        $scope.vtsLocalDate = moment().format('DD MMM YYYY');
        $scope.setvtsvesseletaTimeDateValid = false;

        //vessel information
        $scope.vtsvesselnameinput = ""; $scope.vtsvesselcallsigninput = ""; $scope.vtsvesselmmsiinput = ""; $scope.vtsvesselmmsilabel = "MMSI: "; $scope.vtsvesselimoinput = "";
        $scope.vtsvesselimolabel = "IMO"; $scope.vtsvesseldraughtinput = ""; $scope.vtsvesselairdraughtinput = ""; $scope.vtsvesselpersonsinput = ""; $scope.vtsvessellengthinput = "";
        $scope.vtsvesseldeadweightinput = ""; $scope.vtsvesselgrosstonnageinput = ""; $scope.vtsvesseldefectsinput = ""; $scope.vtsvesseltypeholder = "";

        //fuel information
        $scope.vtsvesselfueltype00_0input = ""; $scope.vtsvesselfueltype00_1input = ""; $scope.vtsvesselfueltype00_2input = "";
        $scope.vtsvesselfueltype01_0input = ""; $scope.vtsvesselfueltype01_1input = ""; $scope.vtsvesselfueltype01_2input = "";
        $scope.vtsvesselfueltype02_0input = ""; $scope.vtsvesselfueltype02_1input = ""; $scope.vtsvesselfueltype02_2input = "";
        $scope.vtsvesselfueltype03_0input = ""; $scope.vtsvesselfueltype03_1input = ""; $scope.vtsvesselfueltype03_2input = "";
        $scope.vtsvesselfueltype04_0input = ""; $scope.vtsvesselfueltype04_1input = ""; $scope.vtsvesselfueltype04_2input = "";
        $scope.vtsvesselfueltype05_0input = ""; $scope.vtsvesselfueltype05_1input = ""; $scope.vtsvesselfueltype05_2input = "";
        $scope.vtsvesselfueltype06_0input = ""; $scope.vtsvesselfueltype06_1input = ""; $scope.vtsvesselfueltype06_2input = "";

        //Cargo information
        $scope.selectedCargoType = "";
        $scope.vtsvesselcargossnidinput = "";
        $scope.vtsdangerouscargotype01input = ""; $scope.vtsdangerouscargotype02input = ""; $scope.vtsdangerouscargotype03input = ""; $scope.vtsdangerouscargotype04input = "";
        $scope.vtsdangerouscargotype05input = ""; $scope.vtsdangerouscargotype06input = ""; $scope.vtsdangerouscargotype07input = ""; $scope.vtsdangerouscargotype08input = "";
        $scope.vtsdangerouscargotype09input = ""; $scope.vtsdangerouscargotype10input = "";
        $scope.vtsvesselcargotypeholder = "";
        $scope.vtsdangerouscargooverboard = ""; $scope.vtscargodesignatedpersonashorenameinput = ""; $scope.vtscargodesignatedpersonashoretelephoneinput = "";  $scope.vtscargodesignatedpersonashoreemailinput = "";
        $scope.vtsDangCargoCheckBoxState = false; //checkbox to activate dangerous cargo in case is not default
        $scope.vtsDangCargoCheckDisabled = true;

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

        //cargo information
        // $scope.setvtsCargoSSNIDValid = false; //SafeSeaNet manifest ID reference - has full manifest - should be autofilled through service at login - requires cooperation with SSN
        $scope.setvtsCargoTypeValid = false;
        $scope.setvtsvesselContactDetailsValid = false;
        $scope.setvtsvesselDPANameValid = false;
        $scope.setvtsvesselDPAPhoneValid = false;
        $scope.setvtsvesselDPAEmailValid = false;

        //voyage information - should be autofilled through service at login if available
        $scope.vtsvesselposlondegreesinput = "";
        $scope.vtsvesselposlonminutesinput = "";
        $scope.vtsvesselposlatdegreesinput = "";
        $scope.vtsvesselposlatminutesinput = "";
        $scope.vtscargoadditionalcontactdetailsinput = "";
        $scope.vtsvesseltrueheadinginput = "";
        $scope.vtsvesselportofdestinationinput = "";

        $scope.setvtsvesselPosLonDegreesValid = false;
        $scope.setvtsvesselPosLatDegreesValid = false;
        $scope.setvtsvesselPosLonMinutesValid = false;
        $scope.setvtsvesselPosLatMinutesValid = false;

        $scope.setvtsvesselSpeedValid = false;
        $scope.setvtsvesselTrueHeadingValid = false;
        $scope.setvtsEtaDateValid = false;
        $scope.setvtsEtaTimeValid = false;
        $scope.setvtsvesselPortOfDestinationValid = false;
        $scope.setvtsvesselRouteValid = false;

        $scope.VTSReadyToSend = false; //global readystate

        $scope.reportSummary = { //what is sent to the VTS

            //VTS information
            vtsShortName:"",
            vtsCallSign:"",
            vtsEmail:"",

            //Vessel information
            vesselName:"",
            vesselCallSign:"",
            vesselMMSI:0,
            vesselIMO:0,
            vesselDraught:0, //Metres, 1 decimal
            vesselAirDraught:0, //Metres, 1 decimal
            vesselPersonsOnboard:0,
            vesselLength:0, //Metres, 1 decimal
            vesselDeadWeight:0, //Tonnes, not mandatory yet
            vesselGRT:0, //Tonnes (GrossTonnage, a.k.a GT)
            vesselDefects:"", //String, any length
            vesselType:"", //String, predefined by dropdown.

            //Fuel and fuel types - always in Tonnes (metric)
            fuelTotalFuel:0, //Tonnes - all fuel added up
            fuelTypeHFORegular:0, //Tonnes - regular means it is not low sulphur. (Heavy Fuel Oil)
            fuelTypeHFOLowSulphur:0,
            fuelTypeHFOUltraLowSulphur:0,
            fuelTypeIFORegular:0, //(Intermediate Fuel Oil)
            fuelTypeIFOLowSulphur:0,
            fuelTypeIFOUltraLowSulphur:0,
            fuelTypeMDORegular:0, //(Marine Diesel Oil)
            fuelTypeMDOLowSulphur:0,
            fuelTypeMDOUltraLowSulphur:0,
            fuelTypeMGORegular:0, //(Marine Gas Oil)
            fuelTypeMGOLowSulphur:0,
            fuelTypeMGOUltraLowSulphur:0,
            fuelTypeLPG:0, //(Liquid Petroleum Gas)
            fuelTypeLNG:0, //(Liquid Natural Gas)

            //Cargo Information in Tonnes, 1 decimal
            cargoType:"", //String, predefined by dropdown. Certain cargotypes demand listing of dangerous cargo/goods (DG/DC)
            cargoIMOClass01:0, //Explosives
            cargoIMOClass02:0, //Gases
            cargoIMOClass03:0, //Flammable liquids
            cargoIMOClass04:0, //Flammable solids or substances not liquid
            cargoIMOClass05:0, //Oxydizing substances
            cargoIMOClass06:0, //Toxic substances
            cargoIMOClass07:0, //Radioactive substances
            cargoIMOClass08:0, //Corrosives
            cargoIMOClass09:0, //Miscellaneous dangerous substances and articles
            cargoDangerousCargoOnBoard:false, //any cargoIMOClass on board makes this true
            cargoDangerousCargoTotalTonnage:0, //sum of all dangerous cargo
            cargoIMOClassesOnBoard:"", //String of IMO DG Classes ex: "1,4,6,7" - required by some VTS
            cargoPollutantOrDCLostOverBoard:"", //String, description of what, where, how, when
            cargoAdditionalContactInformation:"", //String - additional contact information for owner or handler of cargo on shore
            cargoDPAName:"", //String - Name of Designated Person Ashore (DPA) or Cargo Agent.
            cargoDPATelephone:"", //String telephone number of DPA
            cargoDPAEmail:"", //String email of DPA

            //Voyage information
            voyagePositionLon:0, //Longitude position of vessel, format is degrees and decimal minutes, ex: 12,59.9999 - (0-90),(0-60).(0-9999) (N/S)
            voyagePositionLat:0, //Latitude position of vessel, format is degrees and decimal minutes, ex: 55,39.9999 - (0-180),(0-60).(0-9999) (E/W)
            voyageSpeed:0, //Current speed of vessel, in knots
            voyageTrueHeading:0, //Current true heading, 0-360 degrees, 1 decimal
            voyageVTSETADateTime:"", //String - Arrival date at VTS area, DD-MM-YYYY HH:mm
            voyagePortOfDestination:"" //String - name of port
        };

        $scope.aisData = { //data prefetched from AIS - used with MMSI
            vesselName:"",
            vesselCallsign:"",
            vesselImo:"",
            vesselCog:"", //course over ground
            vesselDestination:"",
            vesselDraught:"",
            vesselHeading:"",
            vesselLat:"",
            vesselLon:"",
            vesselLength:"",
            vesselNavStatus:"",
            vesselRot:"", //rate of turn
            vesselSog:"", //speed over ground
            vesselType:"",
            vesselWidth:""
        };

        //Validation area *****************************************************************************************

        //allows only string of max number val 99.9 to pass
        $scope.VTSValidation999 = function(str){
            str = str.replace(/[^0-9.]/, '');
            if(str==".")str="0."; //cant start with period
            if(str.length > 1) { //has to start with zero if period is first char
                if (str.substring(0,1) == ".") str = "0" + str;
            }
            //remove all periods but the first
            if(str.length > 1) {
                for (var i = 0; i != str.length - 1; i++) {
                    if (str.substring(i, i + 1) == ".") {
                        if (i != str.length - 1) {
                            var da = str.substring(0, i);
                            var db = str.substring(i + 1, str.length).replace(".", "");
                            db = db.substring(0,1); //only one decimal place allowed
                            str = da +"."+ db;
                        }
                        break;
                    }
                }
            }
            if(str.length > 4) str = str.substring(0,4); //cant be more than 4 chars
            return str;
        };

        //test if all items check out, then display "SEND" button
        $scope.VTSValidationAllDone = function(){

            //Validation exceptions for not required fields
            if(!VTSData[$scope.VTSID].showMaxDraught) $scope.setvtsvesselDraughtValid = true;
            if(!VTSData[$scope.VTSID].showAirDraught) $scope.setvtsvesselAirDraughtValid = true;
            if(!VTSData[$scope.VTSID].showDeadWeightTonnage) $scope.setvtsvesselDeadWeightValid = true;
            if(!VTSData[$scope.VTSID].showVesselType) $scope.setvtsVesselTypeValid = true;
            if(!VTSData[$scope.VTSID].showVesselLength) $scope.setvtsVesselLengthValid = true;
            if(!VTSData[$scope.VTSID].showFuelDetails) $scope.fuelDetailsValid = true;

            //exception for cargo information
            if($scope.showCargoContactInformationInput == false) $scope.setvtsvesselContactDetailsValid = true;
            if($scope.showCargoContactInformationInput == false) $scope.setvtsvesselContactDetailsValid = true;
            if($scope.showCargoContactInformationInput == false) $scope.setvtsvesselContactDetailsValid = true;
            if($scope.showCargoContactInformationInput == false) $scope.setvtsvesselContactDetailsValid = true;

            //grouped valid states into 3 to make less confusing
            var group1valid = false,
                group2valid = false,
                group3valid = false,
                group4valid = false,
                group5valid = false,
                group6valid = false,
                group7valid = false,
                group8valid = false;

            if($scope.setvtsvesselnameValid && $scope.setvtsvesselcallsignValid && $scope.setvtsvesselMMSIValid) group1valid = true;
            if($scope.setvtsvesselIMOValid && $scope.setvtsvesselDraughtValid && $scope.setvtsvesselAirDraughtValid) group2valid = true;
            if($scope.setvtsvesselPersonsValid && $scope.setvtsVesselTypeValid && $scope.setvtsVesselLengthValid) group3valid = true;
            if($scope.fuelDetailsValid && $scope.setvtsCargoTypeValid && $scope.setvtsvesselContactDetailsValid) group4valid = true;
            if($scope.setvtsvesselPosLonDegreesValid && $scope.setvtsvesselPosLatDegreesValid) group5valid = true;
            if($scope.setvtsvesselPosLonMinutesValid && $scope.setvtsvesselPosLatMinutesValid) group6valid = true;
            if($scope.setvtsvesselSpeedValid && $scope.setvtsvesselTrueHeadingValid && $scope.setvtsvesselPortOfDestinationValid) group7valid = true;
            if($scope.setvtsvesseletaTimeDateValid) group8valid = true;

            console.log("");
            console.log("");
            console.log("");
            console.log("group1valid",group1valid);
            console.log("group2valid",group2valid);
            console.log("group3valid",group3valid);
            console.log("group4valid",group4valid);
            console.log("group5valid",group5valid);
            console.log("group6valid",group6valid);
            console.log("group7valid",group7valid);
            console.log("group8valid",group8valid);

            if(group1valid==false || group2valid==false || group3valid==false || group4valid==false || group5valid==false || group6valid==false || group7valid==false || group8valid==false) {
                $scope.VTSReadyToSend = false;
            }else{
                if($scope.isLoggedIn){
                    $scope.VTSReadyToSend = true;
                }
            }
            console.log("VTSReadyToSend",$scope.VTSReadyToSend);

        };



        $scope.toggleDateValid = function(valid){
            $scope.setvtsvesseletaTimeDateValid = valid;
            if(valid==true){
                angular.element(document.querySelector(".vts-timedatepicker.datetime-input.date .display")).addClass("vts-datetime-picker-box-highlight-valid");
                angular.element(document.querySelector(".vts-timedatepicker.datetime-input.date .display")).removeClass("vts-datetime-picker-box-highlight-invalid");
            }else{
                angular.element(document.querySelector(".vts-timedatepicker.datetime-input.date .display")).removeClass("vts-datetime-picker-box-highlight-valid");
                angular.element(document.querySelector(".vts-timedatepicker.datetime-input.date .display")).addClass("vts-datetime-picker-box-highlight-invalid");
            }
        };
        $scope.toggleTimeValid = function(valid){
            $scope.setvtsvesseletaTimeDateValid = valid;
            if(valid==true){
                angular.element(document.querySelector(".vts-timedatepicker.datetime-input.time .display")).addClass("vts-datetime-picker-box-highlight-valid");
                angular.element(document.querySelector(".vts-timedatepicker.datetime-input.time .display")).removeClass("vts-datetime-picker-box-highlight-invalid");
            }else{
                angular.element(document.querySelector(".vts-timedatepicker.datetime-input.time .display")).removeClass("vts-datetime-picker-box-highlight-valid");
                angular.element(document.querySelector(".vts-timedatepicker.datetime-input.time .display")).addClass("vts-datetime-picker-box-highlight-invalid");
            }
            $scope.VTSValidationAllDone();
        };

        $scope.validateEtaTimeDate = function(){
            var isSameDay = moment(moment($scope.vtsLocalDate, 'DD MMM YYYY')).isSame(moment(moment().format('MMM DD YYYY')));
            var isAfterDay = moment(moment($scope.vtsLocalDate, 'DD MMM YYYY')).isAfter(moment(moment().format('YYYY-MM-DD')));
            var isAfterTime = $scope.vtsUtcTime.replace(":","").replace("  ","") > moment.utc().format('HH:mm').replace(":","");
            if(isAfterDay){
                $scope.toggleDateValid(true);
            }else if(isSameDay && isAfterTime){
                $scope.toggleDateValid(true);
            }else{
                $scope.toggleDateValid(false);
            }
            if(isAfterDay){
                $scope.toggleTimeValid(true);
            }else if(isAfterTime && isSameDay){
                $scope.toggleTimeValid(true);
            }else{
                $scope.toggleTimeValid(false);
            }
        };

        $scope.dateInputChange = function (now) {
            $scope.vtsLocalDate = moment(now._d).format('DD MMM YYYY');
            $scope.vtsDateStamp = moment()._locale._weekdaysShort[moment().day()] + " " + $scope.vtsLocalDate;
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

        $scope.timeInputClick = function(){ //styles the timepicker display
            angular.element(document.querySelector(".vts-timedatepicker.datetime-input.time .edit-popover .header")).addClass('ng-hide'); //remove redundant datepicker
            angular.element(document.querySelector(".vts-timedatepicker.datetime-input.time .edit-popover")).addClass('time-picker-mod'); //make smaller
            angular.element(document.querySelector(".vts-timedatepicker.datetime-input.time .timer .timer-seconds")).addClass('ng-hide'); //remove seconds
            angular.element(document.querySelector(".vts-timedatepicker.datetime-input.time .clear-button")).addClass('ng-hide'); //remove clear button
            angular.element(document.querySelector(".vts-timedatepicker.datetime-input.time .timer div:nth-child(4)")).addClass("ng-hide");
        };

        //length greater than 1
        $scope.VTSVesselNameValidation = function(validate){
            ($scope.vtsvesselnameinput.length > 1) ? $scope.setvtsvesselnameValid = true : $scope.setvtsvesselnameValid = false;
            if(validate) $scope.VTSValidationAllDone(); //If all done, display send button
        };

        //length greater than 1
        $scope.VTSVesselCallsignValidation = function(validate){
            ($scope.vtsvesselcallsigninput.length > 1) ? $scope.setvtsvesselcallsignValid = true : $scope.setvtsvesselcallsignValid = false;
            if(validate) $scope.VTSValidationAllDone();
        };

        //Only 9 numbers, no letters
        $scope.VTSVesselMMSIValidation = function(){
            var input = $scope.vtsvesselmmsiinput;

            if(input.length == 9){
                $scope.setvtsvesselMMSIValid = true;
                $scope.getAisDataByMmsi(); //fetches data, loads into placeholders, but does not populate
            } else if(input.length > 9){
                input = input.substring(0,9);
            }else{
                $scope.setvtsvesselMMSIValid = false;
            }
            input = input.toString().replace(/\D/g, '');
            $scope.vtsvesselmmsiinput = input;
            $scope.vtsvesselmmsilabel = $sce.trustAsHtml("MMSI: "+input); //send to label
            $scope.VTSValidationAllDone();
        };



        // $scope.aisData = { //data prefetched from AIS - used with MMSI
        //     vesselName:"",
        //     vesselCallsign:"",
        //     vesselImo:"",
        //     vesselCog:"", //course over ground
        //     vesselDestination:"",
        //     vesselDraught:"",
        //     vesselHeading:"",
        //     vesselLat:"",
        //     vesselLon:"",
        //     vesselLength:"",
        //     vesselNavStatus:"",
        //     vesselRot:"", //rate of turn
        //     vesselSog:"", //speed over ground
        //     vesselType:"",
        //     vesselWidth:""
        // };

        $scope.populateInputsWithAisData = function(){

            $scope.vtsvesselnameinput = $scope.aisData.vesselName;
            if($scope.aisData.vesselName != "") $scope.VTSVesselNameValidation(false); //skips global validator

            $scope.vtsvesselcallsigninput = $scope.aisData.vesselCallsign;
            if($scope.aisData.vesselCallsign != "") $scope.VTSVesselCallsignValidation(false);

            $scope.vtsvesselimoinput = $scope.aisData.vesselImo;
            if($scope.aisData.vesselImo != "") $scope.VTSVesselIMOValidation(false);

            $scope.vtsvesselportofdestinationinput = $scope.aisData.vesselDestination;
            if($scope.aisData.vesselDestination != "") $scope.VTSVesselPortOfDestinationValidation(false);

            $scope.vtsvesseltrueheadinginput = $scope.aisData.vesselCog;
            if($scope.aisData.vesselCog != "") $scope.VTSTrueHeadingValidation(false);


            $scope.VTSValidationAllDone(); //finally validate all values for ready to send
        };

        //Fetches AIS data  - changes background colour or affected inputs and adds placeholder if they are empty
        $scope.getAisDataByMmsi = function(){
            if($scope.setvtsvesselMMSIValid && $scope.isLoggedIn) {
                VesselService.detailsMMSI($scope.vtsvesselmmsiinput).then(function (vesselDetails) {
                    console.log("Vessel info from AIS:", vesselDetails.data.aisVessel);

                    $scope.aisData.vesselName = vesselDetails.data.aisVessel.name;
                    $scope.aisData.vesselCallsign = vesselDetails.data.aisVessel.callsign;
                    $scope.aisData.vesselImo = vesselDetails.data.aisVessel.imoNo;
                    $scope.aisData.vesselCog = vesselDetails.data.aisVessel.cog;
                    $scope.aisData.vesselDestination = vesselDetails.data.aisVessel.destination;

                    //load the placeholders
                    $scope.placeholderAisVesselName = $scope.aisData.vesselName;
                    $scope.placeholderAisVesselCallsign = $scope.aisData.vesselCallsign;
                    $scope.placeholderAisVesselImo = $scope.aisData.vesselImo;
                    $scope.placeholderAisVesselCog = $scope.aisData.vesselCog;
                    $scope.placeholderAisVesselDestination = $scope.aisData.vesselDestination;



                }, function (reason) {
                    console.log('AIS data fetch failed: ' + reason);
                });
            }
        };

        //Only 10 chars, "IMO" (hardcoded) followed by 7 numbers.
        $scope.VTSVesselIMOValidation = function(validate){
            var input = $scope.vtsvesselimoinput;
            input = input.toString().replace(/\D/g, '');
            if(input.length == 7){
                $scope.setvtsvesselIMOValid = true;
            } else if($scope.vtsvesselimoinput.length > 7){
                input = input.substring(0,7);
                $scope.setvtsvesselIMOValid = true;
            }else{
                $scope.setvtsvesselIMOValid = false;
            }
            $scope.vtsvesselimoinput = input; //send cleaned to input field
            $scope.vtsvesselimolabel = $sce.trustAsHtml("IMO"+input); //send to label
            if(validate) $scope.VTSValidationAllDone();
        };

        //max 99.9
        $scope.VTSVesselDraughtValidation = function() {
            var draught = $scope.VTSValidation999($scope.vtsvesseldraughtinput);
            var draughtFloat = parseFloat(draught);
            if (isNaN(draughtFloat)) draughtFloat = 0;
            if(draughtFloat > 0.1 && draughtFloat < 100) {
                $scope.setvtsvesselDraughtValid = true
            }else{
                $scope.setvtsvesselDraughtValid = false;
            }
            $scope.vtsvesseldraughtinput = draught;
            $scope.VTSValidationAllDone();
        };

        //max 99.9
        $scope.VTSVesselAirDraughtValidation = function() {
            var draught = $scope.VTSValidation999($scope.vtsvesselairdraughtinput);
            var draughtFloat = parseFloat(draught);
            if(draughtFloat > 0 && draughtFloat < 100) {
                $scope.setvtsvesselAirDraughtValid = true
            }else{
                $scope.setvtsvesselAirDraughtValid = false;
            }
            $scope.vtsvesselairdraughtinput = draught;
            $scope.VTSValidationAllDone();
        };

        //minimum 1 person on board
        $scope.VTSVesselPersonsValidation = function(){
            var persons = $scope.vtsvesselpersonsinput;
            persons = persons.toString().replace(/\D/g, '');
            if(persons.length>5) persons = persons.substring(0,5);
            if(parseInt(persons) > 0){
                $scope.setvtsvesselPersonsValid = true
            } else {
                $scope.setvtsvesselPersonsValid = false
            }
            $scope.vtsvesselpersonsinput = persons; //send cleaned to input field
            $scope.VTSValidationAllDone();
        };

        $scope.selectVesselTypeChange = function (selectedItem) {
            $scope.setvtsVesselTypeValid = true;
            $scope.vtsvesseltypeholder = selectedItem.type;
        };

        //cant be longer than 999m
        $scope.VTSVesselLengthValidation = function(){
            var GT = $scope.vtsvessellengthinput.toString().replace(/\D/g, '');
            var GTint = parseInt(GT);
            if (isNaN(GTint)) GTint = 0;
            (GTint > 1 && GTint < 999) ? $scope.setvtsvesselLengthValid = true : $scope.setvtsvesselLengthValid = false;
            $scope.vtsvessellengthinput = GT;
            $scope.VTSValidationAllDone();
        };

        //any number
        $scope.VTSVesselDWTValidation = function() {
            var GT = $scope.vtsvesseldeadweightinput.toString().replace(/\D/g, '');
            var GTint = parseInt(GT);
            (GTint>0) ? $scope.setvtsvesselDeadWeightValid = true : $scope.setvtsvesselDeadWeightValid = false;
            $scope.vtsvesseldeadweightinput = GT;
            $scope.VTSValidationAllDone();
        };

        //just numbers - not mandatory input
        $scope.VTSVesselGTValidation = function() {
            $scope.vtsvesselgrosstonnageinput = $scope.vtsvesselgrosstonnageinput.toString().replace(/\D/g, '');
        };

            $scope.getLocation = function(){
            if (navigator.geolocation) {
                navigator.geolocation.getCurrentPosition(function (position) {
                    $scope.retMinutesFromDecDegrees = function(decDeg){
                        var deg = (decDeg+"").substring(0,(decDeg+"").indexOf("."));
                        deg = Math.round((60*(decDeg-deg))*10000)/10000;
                        return deg
                    };
                    var lon = position.coords.longitude;
                    $scope.vtsvesselposlondegreesinput = (lon+"").substring(0,(lon+"").indexOf("."));
                    $scope.vtsvesselposlonminutesinput = $scope.retMinutesFromDecDegrees(lon);
                    var lat = position.coords.latitude;
                    $scope.vtsvesselposlatdegreesinput = (lat+"").substring(0,(lat+"").indexOf("."));
                    $scope.vtsvesselposlatminutesinput = $scope.retMinutesFromDecDegrees(lat);

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

        //validates 0-90 or 0-180 degrees, 0.0001-60.0000 minutes
        $scope.VTSPositionDegValidationHelper = function(input,testfor){
            //first determine validation
            if(testfor!="decimalminutes") input = input.toString().replace(/\D/g, '');
            var re;
            if(testfor==90){
                re = new RegExp(/^([0-9]|[0-8]\d|90)$/);
                input = input.substring(0,2);
            }
            if(testfor==180){
                re = new RegExp(/^(0{0,2}[0-9]|0?[1-9][0-9]|1[0-7][0-9]|180)$/);
                input = input.substring(0,3);
            }
            var output = {valid:false,value:input,decimals:0};

            if(testfor=="decimalminutes"){
                input = input.replace(/[^0-9.]/g, '');
                re = new RegExp(/^([1-9]|[0-5]\d|60)(\.[0-9]{4,})?$/);
            }

            var m = re.exec(input); //executes the validation
            (m == null) ? output.valid = false : output.valid = true;
            return output;
        };
        $scope.VTSPositionLonDegValidation = function(){
            var retVal = $scope.VTSPositionDegValidationHelper($scope.vtsvesselposlondegreesinput,90);
            $scope.vtsvesselposlondegreesinput = retVal.value;
            $scope.setvtsvesselPosLonDegreesValid = retVal.valid;
            $scope.VTSValidationAllDone();
        };
        $scope.VTSPositionLatDegValidation = function(){
            var retVal = $scope.VTSPositionDegValidationHelper($scope.vtsvesselposlatdegreesinput,180);
            $scope.vtsvesselposlatdegreesinput = retVal.value;
            $scope.setvtsvesselPosLatDegreesValid = retVal.valid;
            $scope.VTSValidationAllDone();
        };
        $scope.VTSPositionLatMinValidation = function(){
            var retVal = $scope.VTSPositionDegValidationHelper($scope.vtsvesselposlatminutesinput,"decimalminutes");
            $scope.vtsvesselposlatminutesinput = retVal.value;
            $scope.setvtsvesselPosLatMinutesValid = retVal.valid;
            // console.log($scope.vtsvesselposlatminutesinput.match(new RegExp(/^([1-9]|[1-5]\d|60)(\.[0-9]{1,4})?$/))); //may need to revert
            $scope.VTSValidationAllDone();
        };
        $scope.VTSPositionLonMinValidation = function(){
            var retVal = $scope.VTSPositionDegValidationHelper($scope.vtsvesselposlonminutesinput,"decimalminutes");
            $scope.vtsvesselposlonminutesinput = retVal.value;
            $scope.setvtsvesselPosLonMinutesValid = retVal.valid;
            $scope.VTSValidationAllDone();
        };

        //fuel types - number 999999999, no decimal. One filled makes all other neutral because only one field is mandatory
        $scope.VTSFuelTypeValidation = function(field){
            var strValue = "";
            var totFuel = 0;
            var FTC = $scope.fuelTypes.length + 1; //Fuel Type Count

            if(field) {
                strValue = document.querySelector('#vtsparentdiv #fueltype' + field).value.toString().replace(/\D/g, '');
                if (strValue != "") {
                    if (parseInt(strValue).isNaN) {
                        strValue = "";
                    } //validate to NaN
                }
                angular.element(document.querySelector('#vtsparentdiv  #fueltype' + field)).val(strValue).$apply;
            }

            //get totFuel - cycle through all boxes and add the fuel.
            for(var i = 0;i!=FTC;i++){
                for(var j=0;j!=3;j++) {
                    try {
                        var val = document.querySelector('#vtsparentdiv #fueltype0'+i+"_"+j).value.toString().replace(/\D/g, '');
                        if(val.isNaN || val===""){}else{totFuel += parseInt(val);}
                        (totFuel > 0) ? $scope.fuelDetailsValid = true : $scope.fuelDetailsValid = false

                    }catch(exceptionNoElements){}
                }
            }

            //set/reset valid colours - must be more than 1 ton of fuel on board at any time.
            if(totFuel>0) {
                for (var i = 0; i != FTC; i++) {
                    for (var j = 0; j != 3; j++) {
                        try {
                            angular.element(document.querySelector('#fueltype0' + i + "_" + j)).removeClass("vtsinvalid");
                            angular.element(document.querySelector('#fueltype0' + i + "_" + j)).addClass('vtsvalid');
                        }catch(exceptionNoElements){}
                    }
                }
            }else{
                for (var i = 0; i != FTC; i++) {
                    for (var j = 0; j != 3; j++) {
                        try{
                            angular.element(document.querySelector('#fueltype0'+i+"_"+j)).removeClass("vtsvalid");
                            angular.element(document.querySelector('#fueltype0'+i+"_"+j)).addClass('vtsinvalid');
                        }catch(exceptionNoElements){}
                    }
                }
            }

            //display total tonnage of fuel
            $scope.vtsTotalFuel = parseInt(totFuel).toLocaleString().replace(/,/g, '.'); // totfuel kilo/mega/giga seperator to period from comma: 9.999.999,999
            $scope.VTSValidationAllDone();

        };

        $scope.selectVesselCargoChange = function (selectedItem) {
            if(selectedItem!="None" && selectedItem!="Bulk - grain" && selectedItem != "Ballast" && selectedItem != "Passenger" && selectedItem != "Bulk - other than grain"
                && selectedItem != "Reefer" && selectedItem != "Container/Trailer" && selectedItem != "General Cargo") {
                $scope.showCargoTypeFields = true;
                $scope.showCargoTypesCheckbox = true;
                $scope.vtsDangCargoCheckBoxState = true;
                $scope.vtsDangCargoCheckDisabled = true; //cannot turn off
                $scope.showCargoContactInformationInput = true;
            }else{
                $scope.showCargoTypeFields = false;
                (selectedItem=="None") ? $scope.showCargoTypesCheckbox = false : $scope.showCargoTypesCheckbox = true;
                $scope.vtsDangCargoCheckBoxState = false;
                $scope.vtsDangCargoCheckDisabled = false;
                if((selectedItem=="None" || selectedItem=="Ballast")){
                    $scope.showCargoContactInformationInput = false;
                }else{
                    $scope.showCargoContactInformationInput = true;
                }
            }
            $scope.setvtsCargoTypeValid = true;
            $scope.vtsvesselcargotypeholder = selectedItem;
            $scope.VTSCargoContactInformationChange();
            $scope.VTSValidationAllDone();
        };

        $scope.VTSDangerousCargoCheckbox = function(check){
            if(check == true){
                $scope.showCargoTypeFields = true;
                $scope.showCargoContactInformationInput = true;
            }else{
                $scope.showCargoTypeFields = false;
                if($scope.vtsvesselcargotypeholder == "Ballast") $scope.showCargoContactInformationInput = false;
            }
            $scope.VTSCargoContactInformationChange();
        };

        $scope.VTSCargoContactInformationChange = function(){
            var input = $scope.vtscargoadditionalcontactdetailsinput;
            if(input!="" && input.length>5) {
                $scope.setvtsvesselContactDetailsValid = true;
            }else{
                $scope.setvtsvesselContactDetailsValid = false;
            }
            $scope.VTSValidationAllDone();
        };

        $scope.VTSDangerousCargoContactNameValidate = function(){
            var input = $scope.vtscargodesignatedpersonashorenameinput;
            if(input!="" && input.length>5) {
                $scope.setvtsvesselDPANameValid = true;
            }else{
                $scope.setvtsvesselDPANameValid = false;
            }
            $scope.VTSValidationAllDone();
        };


        $scope.VTSDangerousCargoContactPhonenumberValidate = function(){
            var input = $scope.vtscargodesignatedpersonashoretelephoneinput;
            input = input.replace(/[^-+()0-9]/g, '');
            $scope.vtscargodesignatedpersonashoretelephoneinput = input;
            if(input!="" && input.length>5) {
                $scope.setvtsvesselDPAPhoneValid = true;
            }else{
                $scope.setvtsvesselDPAPhoneValid = false;
            }
            $scope.VTSValidationAllDone();
        };


        $scope.VTSDangerousCargoContactEmailValidate = function(){
            var input = $scope.vtscargodesignatedpersonashoreemailinput;
            var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
            if(input!="" && input.length>5) {
                (re.test(input)) ? $scope.setvtsvesselDPAEmailValid = true : $scope.setvtsvesselDPAEmailValid = false;
            }else{
                $scope.setvtsvesselDPAEmailValid = false;
            }
            $scope.VTSValidationAllDone();
        };

        $scope.VTSDangerousCargoTypeValidation = function (selectedItem) {
            var totalDGTonnage = 0;
            var IMOtypesofDG = "";
            var input;

            if(selectedItem == 1) {
                input = $scope.vtsdangerouscargotype01input.toString().replace(/\D/g, '');
                $scope.vtsdangerouscargotype01input = input;
            }
            if(selectedItem == 2) {
                input = $scope.vtsdangerouscargotype02input.toString().replace(/\D/g, '');
                $scope.vtsdangerouscargotype02input = input;
            }
            if(selectedItem == 3) {
                input = $scope.vtsdangerouscargotype03input.toString().replace(/\D/g, '');
                $scope.vtsdangerouscargotype03input = input;
            }
            if(selectedItem == 4) {
                input = $scope.vtsdangerouscargotype04input.toString().replace(/\D/g, '');
                $scope.vtsdangerouscargotype04input = input;
            }
            if(selectedItem == 5) {
                input = $scope.vtsdangerouscargotype05input.toString().replace(/\D/g, '');
                $scope.vtsdangerouscargotype05input = input;
            }
            if(selectedItem == 6) {
                input = $scope.vtsdangerouscargotype06input.toString().replace(/\D/g, '');
                $scope.vtsdangerouscargotype06input = input;
            }
            if(selectedItem == 7) {
                input = $scope.vtsdangerouscargotype07input.toString().replace(/\D/g, '');
                $scope.vtsdangerouscargotype07input = input;
            }
            if(selectedItem == 8) {
                input = $scope.vtsdangerouscargotype08input.toString().replace(/\D/g, '');
                $scope.vtsdangerouscargotype08input = input;
            }
            if(selectedItem == 9) {
                input = $scope.vtsdangerouscargotype09input.toString().replace(/\D/g, '');
                $scope.vtsdangerouscargotype09input = parseInt(input);
            }

            //display the types of cargo
            if($scope.vtsdangerouscargotype01input != "") {
                input = parseInt($scope.vtsdangerouscargotype01input);
                if(input.isNaN)input=0;
                if(input>0){
                    IMOtypesofDG = IMOtypesofDG + " - 1";
                    totalDGTonnage += parseInt(input);
                }
            }
            if($scope.vtsdangerouscargotype02input != "") {
                input = parseInt($scope.vtsdangerouscargotype02input);
                if(input.isNaN)input=0;
                if(input>0){
                    IMOtypesofDG = IMOtypesofDG + " - 2";
                    totalDGTonnage += parseInt(input);
                }
            }
            if($scope.vtsdangerouscargotype03input != "") {
                input = parseInt($scope.vtsdangerouscargotype03input);
                if(input.isNaN)input=0;
                if(input>0){
                    IMOtypesofDG = IMOtypesofDG + " - 3";
                    totalDGTonnage += parseInt(input);
                }
            }
            if($scope.vtsdangerouscargotype04input != "") {
                input = parseInt($scope.vtsdangerouscargotype04input);
                if(input.isNaN)input=0;
                if(input>0){
                    IMOtypesofDG = IMOtypesofDG + " - 4";
                    totalDGTonnage += parseInt(input);
                }
            }
            if($scope.vtsdangerouscargotype05input != "") {
                input = parseInt($scope.vtsdangerouscargotype05input);
                if(input.isNaN)input=0;
                if(input>0){
                    IMOtypesofDG = IMOtypesofDG + " - 5";
                    totalDGTonnage += parseInt(input);
                }
            }
            if($scope.vtsdangerouscargotype06input != "") {
                input = parseInt($scope.vtsdangerouscargotype06input);
                if(input.isNaN)input=0;
                if(input>0){
                    IMOtypesofDG = IMOtypesofDG + " - 6";
                    totalDGTonnage += parseInt(input);
                }
            }
            if($scope.vtsdangerouscargotype07input != "") {
                input = parseInt($scope.vtsdangerouscargotype07input);
                if(input.isNaN)input=0;
                if(input>0){
                    IMOtypesofDG = IMOtypesofDG + " - 7";
                    totalDGTonnage += parseInt(input);
                }
            }
            if($scope.vtsdangerouscargotype08input != "") {
                input = parseInt($scope.vtsdangerouscargotype08input);
                if(input.isNaN)input=0;
                if(input>0){
                    IMOtypesofDG = IMOtypesofDG + " - 8";
                    totalDGTonnage += parseInt(input);
                }
            }
            if($scope.vtsdangerouscargotype09input != "") {
                input = parseInt($scope.vtsdangerouscargotype09input);
                if(input.isNaN)input=0;
                if(input>0){
                    IMOtypesofDG = IMOtypesofDG + " - 9";
                    totalDGTonnage += parseInt(input);
                }
            }

            $scope.vtsdangerouscargotypeslabel = IMOtypesofDG.substring(3,IMOtypesofDG.length);
            $scope.vtsdangerouscargotonnagelabel = totalDGTonnage;

        };

        //only number between and including 0 to 360 with cleanup
        $scope.VTSTrueHeadingValidation = function(validate){
            var inputStr = $scope.vtsvesseltrueheadinginput.toString();
            console.log("true heading validate:",inputStr);
            if(inputStr == "." || inputStr == "0.0") {
                $scope.vtsvesseltrueheadinginput = "0.";
                inputStr = "0."
            }
            if(inputStr.length>5) inputStr = inputStr.substring(0,5); //cant be longer than 359.9

            if(inputStr.indexOf(".") !== -1 && inputStr.length > 2 && (inputStr.substring(inputStr.length-1,inputStr.length) != ".")) { //has period - treat as float
                var inputFloat = parseFloat(inputStr);
                if (inputFloat.isNaN) inputFloat = 0.0;
                var isValid = false;
                if (inputFloat > 0.0 && (inputFloat < 360.0)) isValid = true;
                $scope.setvtsvesselTrueHeadingValid = isValid;
                inputStr = inputFloat;
            }else if(inputStr.indexOf(".") < 0 && inputStr.length>0){ //No period, treat as int
                var inputInt = parseInt(inputStr);
                if(inputInt.isNaN) inputInt = 0;
                var isValid = false;
                if (inputInt > 0 && (inputInt < 360)) isValid = true;
                $scope.setvtsvesselTrueHeadingValid = isValid;
                inputStr = inputInt;
            }

            $scope.vtsvesseltrueheadinginput = inputStr;

            if(validate) $scope.VTSValidationAllDone();
        };

        $scope.VTSVesselPortOfDestinationValidation = function(validate){
            var input = $scope.vtsvesselportofdestinationinput;
            (input != "" && input.length > 2) ? $scope.setvtsvesselPortOfDestinationValid = true : $scope.setvtsvesselPortOfDestinationValid = false;
            if(validate) $scope.VTSValidationAllDone();
        };

        //cant be faster than 50 knots
        $scope.VTSVesselSpeedValidation = function(){
            var speed = $scope.vtsvesselspeedinput.toString().replace(/\D/g, '');
            var speedInt = parseInt(speed);
            if(speedInt > 49) speedInt = 50;
            (speedInt>0) ? $scope.setvtsvesselSpeedValid = true : $scope.setvtsvesselSpeedValid = false;
            $scope.vtsvesselspeedinput = speedInt;
            $scope.VTSValidationAllDone();
        };

        //END input validation area


    //Change VTS center dropdown **********************************************************************************

    //clicking on dropdown menu to open the VTS form reates the input html according to VTSCenterData available - made as html insert because so many unknowns.
    $scope.selectVTSCenterChange = function (selectedItem) {

        //For debug purposes
        // $scope.isLoggedIn = true; //

        var html = "",
            vtsID=0;

        for(var i=0;i!=VTSData.length;i++){
            if(selectedItem == VTSData[i].shortname){
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

        //reset validations
        $scope.VTSVesselDraughtValidation();
        $scope.VTSVesselAirDraughtValidation();
        $scope.VTSVesselDWTValidation();
        $scope.selectedVesselType = '0'; //reset select
        $scope.setvtsVesselTypeValid = false; //UI update
        $scope.VTSVesselLengthValidation();
        $scope.VTSFuelTypeValidation();

        $scope.VTSSelectedTrafficCenterName = VTSData[vtsID].name;
        if(VTSData[vtsID].iconImage!=""){
            html = "<span style='min-width:24px;display: inline-block; text-align: left; '><img style='height:20px;' src='"+VTSData[vtsID].iconImage+"'></span>";
            $scope.VTSSelectedTrafficCenterLogo = $sce.trustAsHtml(html);
            html = "";
        }
        if(VTSData[vtsID].VTSGuideLink!=""){
            html = "<span style='min-width:200px;max-width:100%;display: inline-block; text-align: right; right:0;float:right;'><a target='_blank' href='"+VTSData[vtsID].VTSGuideLink+"'>View Master&#39;s Guide online</a></span>";
            $scope.VTSSelectedTrafficCenterGuide = $sce.trustAsHtml(html);
            html = "";
        }

        //create contact information for VTS center in compact form
        html += "<span style='min-width:220px;max-width:220px;display: inline-block; text-align: left;'>Call sign: &#34;"+VTSData[vtsID].callsign+"&#34;</span>";
        if(VTSData[vtsID].vhfchannel1!="") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>-</span><span style='min-width:140px;max-width:140px;display: inline-block;'>VHF ch. "+VTSData[vtsID].vhfchannel1+"</span>";
        if(VTSData[vtsID].vhfchannel2!="") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>-</span><span style='min-width:140px;max-width:140px;display: inline-block;'>VHF ch. "+VTSData[vtsID].vhfchannel2+"</span>";

        //radio channels VHF
        //if more than 2 channels, add them to their own div - very rare that happens.
        if(VTSData[vtsID].vhfchannel3!="" || VTSData[vtsID].vhfchannel4!="") html += "<div><span style='min-width:220px;max-width:220px;display: inline-block; text-align: left;'>&nbsp;</span>";
        if(VTSData[vtsID].vhfchannel3!="") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>&nbsp;</span><span style='min-width:140px;max-width:140px;display: inline-block;'>VHF ch. "+VTSData[vtsID].vhfchannel3+"</span>";
        if(VTSData[vtsID].vhfchannel4!="") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>-</span><span style='min-width:140px;max-width:140px;display: inline-block;'>VHF ch. "+VTSData[vtsID].vhfchannel4+"</span>";
        if(VTSData[vtsID].vhfchannel3!="" || VTSData[vtsID].vhfchannel4!="") html += "</div>";

        //There is always a reserve channel or two
        if(VTSData[vtsID].vhfreservechannel1!="" || VTSData[vtsID].vhfreservechannel2!="") html += "<div><span style='min-width:220px;max-width:220px;display: inline-block; text-align: left;font-style:italic;color:#999999'>Reserve channels:</span>";
        if(VTSData[vtsID].vhfreservechannel1!="") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>&nbsp;</span><span style='min-width:140px;max-width:140px;display: inline-block;font-style:italic;color:#999999'>VHF "+VTSData[vtsID].vhfreservechannel1+"</span>";
        if(VTSData[vtsID].vhfreservechannel2!="") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>-</span><span style='min-width:140px;max-width:140px;display: inline-block;font-style:italic;color:#999999'>VHF "+VTSData[vtsID].vhfreservechannel2+"</span>";
        if(VTSData[vtsID].vhfreservechannel1!="" || VTSData[vtsID].vhfreservechannel2!="") html += "</div>";

        //Email and telephone
        if(VTSData[vtsID].email!="" || VTSData[vtsID].telephone!="" || VTSData[vtsID].telephone2!="" ) html += "<div>";
        if(VTSData[vtsID].email!="") html += "<span style='min-width:220px;max-width:200px;display: inline-block;'>Email: "+VTSData[vtsID].email+"</span>";
        if(VTSData[vtsID].telephone!="") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>-</span><span style='min-width:220px;max-width:220px;display: inline-block;'>Phone: "+VTSData[vtsID].telephone+"</span>";
        if(VTSData[vtsID].telephone2!="") html += "<span style='min-width:60px;max-width:60px;display: inline-block; text-align: center;'>-</span><span style='min-width:220px;max-width:220px;display: inline-block;'>Phone2: "+VTSData[vtsID].telephone2+"</span>";
        if(VTSData[vtsID].email!="" || VTSData[vtsID].telephone!="" || VTSData[vtsID].telephone2!="") html += "</div>";

        //Not all VTS have a fax
        if(VTSData[vtsID].fax!="") html += "<div><span style='min-width:220px;max-width:220px;display: inline-block;'>Fax: "+VTSData[vtsID].fax+"</span></div>";

        //displays the form fields
        $scope.VTSSelected = true;

        //display time input as invalid
        angular.element(document.querySelector(".datetime-input.date .display")).addClass("vts-datetime-picker-box-highlight-invalid");
        angular.element(document.querySelector(".datetime-input.time .display")).addClass("vts-datetime-picker-box-highlight-invalid");


        $scope.VTSSelectedTrafficCenterData = $sce.trustAsHtml(html);
        $scope.VTSSelectedTrafficCenterShortname = $sce.trustAsHtml(VTSData[vtsID].shortname);

        $scope.VTSFuelTypeValidation(); //disables all fuel inputs if nothing
        $scope.VTSValidationAllDone(); //just in case
    };

    $scope.selectedVesselType = "";
    $scope.vtsTotalFuel=""; //added up and displayed from validation



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
        $scope.reportSummary.cargoIMOClass01 = "" + (($scope.vtsdangerouscargotype01input!="") ? (parseFloat($scope.vtsdangerouscargotype01input)) : 0);
        $scope.reportSummary.cargoIMOClass02 = "" + (($scope.vtsdangerouscargotype02input!="") ? (parseFloat($scope.vtsdangerouscargotype02input)) : 0);
        $scope.reportSummary.cargoIMOClass03 = "" + (($scope.vtsdangerouscargotype03input!="") ? (parseFloat($scope.vtsdangerouscargotype03input)) : 0);
        $scope.reportSummary.cargoIMOClass04 = "" + (($scope.vtsdangerouscargotype04input!="") ? (parseFloat($scope.vtsdangerouscargotype04input)) : 0);
        $scope.reportSummary.cargoIMOClass05 = "" + (($scope.vtsdangerouscargotype05input!="") ? (parseFloat($scope.vtsdangerouscargotype05input)) : 0);
        $scope.reportSummary.cargoIMOClass06 = "" + (($scope.vtsdangerouscargotype06input!="") ? (parseFloat($scope.vtsdangerouscargotype06input)) : 0);
        $scope.reportSummary.cargoIMOClass07 = "" + (($scope.vtsdangerouscargotype07input!="") ? (parseFloat($scope.vtsdangerouscargotype07input)) : 0);
        $scope.reportSummary.cargoIMOClass08 = "" + (($scope.vtsdangerouscargotype08input!="") ? (parseFloat($scope.vtsdangerouscargotype08input)) : 0);
        $scope.reportSummary.cargoIMOClass09 = "" + (($scope.vtsdangerouscargotype09input!="") ? (parseFloat($scope.vtsdangerouscargotype09input)) : 0);
        $scope.reportSummary.cargoDangerousCargoTotalTonnage = "" + ($scope.vtsdangerouscargotonnagelabel);
        if($scope.reportSummary.vtsdangerouscargotonnagelabel != "" ) $scope.reportSummary.cargoDangerousCargoOnBoard = true;
        $scope.reportSummary.cargoIMOClassesOnBoard = "" + ($scope.vtsdangerouscargotypeslabel);
        $scope.reportSummary.cargoPollutantOrDCLostOverBoard = "" + ($scope.vtsdangerouscargooverboard);
        $scope.reportSummary.cargoAdditionalContactInformation = "" + ($scope.vtscargoadditionalcontactdetailsinput);

        $scope.reportSummary.cargoDPAName = "" + ($scope.vtscargodesignatedpersonashorenameinput);
        $scope.reportSummary.cargoDPATelephone = "" + ($scope.vtscargodesignatedpersonashoretelephoneinput);
        $scope.reportSummary.cargoDPAEmail = "" + ($scope.vtscargodesignatedpersonashoreemailinput);

        //voyage info
        $scope.reportSummary.voyagePositionLon = "" + ($scope.vtsvesselposlondegreesinput +","+ $scope.vtsvesselposlonminutesinput);
        $scope.reportSummary.voyagePositionLat = "" + ($scope.vtsvesselposlatdegreesinput +","+ $scope.vtsvesselposlatminutesinput);
        $scope.reportSummary.voyageSpeed = "" + (parseFloat($scope.vtsvesselspeedinput));
        $scope.reportSummary.voyageTrueHeading = "" + (parseFloat($scope.vtsvesseltrueheadinginput));
        $scope.reportSummary.voyagePortOfDestination = "" + ($scope.vtsvesselportofdestinationinput);
        $scope.reportSummary.voyageVTSETADateTime = "" + ($scope.vtsLocalDate + " - " + $scope.vtsUtcTime);


        // debug
        console.log("VTS REPORT:",$scope.reportSummary);

        //Send form endpoint *************************************************************************************
        $http({
            url: '/rest/vtsemail',
            method: "POST",
            data: JSON.stringify($scope.reportSummary),
            headers:{'Content-Type': 'application/json'}
        })
        .then(function(data) {
                //var parsedData = JSON.parse(JSON.stringify(data)); //used for debug
                $uibModalInstance.close('forceclose','forceclose'); //close VTS interface
                growl.success("VTS report was successfully sent to "+$scope.reportSummary.vtsShortName+"!");
            },
            function(data) { // optional
                console.log(data);
                alert("Your report could not be sent. Please check your internet connection and try again.\nIf this message persists with an established internet connection, please contact the Department of E-Navigation: sfs@dma.dk");
            });


    };

    //Modal form control ******************************************************************************************
    $scope.hideVTSForm = function () {
        $uibModalInstance.close();
    };

    $scope.$on('modal.closing', function(event, reason, closed) {
        // console.log('modal.closing: ' + (closed ? 'close' : 'dismiss') + '(' + reason + ')');
        var message = "You are about to leave the VTS interface. Any changes you have made may be lost.\nClick OK to exit or Cancel to remain in the VTS interface.";
        switch (reason){
            case "forceclose":
                $scope.VTSID=-1;
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
        if(message===""){
            event.preventDefault();
        }else{
            if($scope.VTSID!=-1) { //ignore exit warning if pristine
                if (!confirm(message)) {
                    event.preventDefault();
                }
            }
        }
    });

}]);
